package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.Ingredient;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NEEContainerDrawHandler implements IContainerDrawHandler {

    public static NEEContainerDrawHandler instance = new NEEContainerDrawHandler();

    private final Field overlayButtonsField;
    private final Field handlerField;
    private Method recipesPerPageMethod;

    public NEEContainerDrawHandler() {
        this.overlayButtonsField = ReflectionHelper.findField(GuiRecipe.class, "overlayButtons");
        this.handlerField = ReflectionHelper.findField(GuiRecipe.class, "handler");
        try {
            this.recipesPerPageMethod = GuiRecipe.class.getDeclaredMethod("getRecipesPerPage");
            recipesPerPageMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreDraw(GuiContainer gui) {

    }

    @Override
    public void renderObjects(GuiContainer gui, int mouseX, int mouseY) {

    }

    @Override
    public void postRenderObjects(GuiContainer gui, int mouseX, int mouseY) {

    }

    @Override
    public void renderSlotUnderlay(GuiContainer gui, Slot slot) {

    }

    @Override
    public void renderSlotOverlay(GuiContainer gui, Slot slot) {
        if (gui instanceof GuiRecipe) {
            Minecraft mc = Minecraft.getMinecraft();
            final ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            final int mouseX = Mouse.getX() * i / mc.displayWidth;
            final int mouseY = j - Mouse.getY() * j / mc.displayHeight - 1;
            GuiRecipe guiRecipe = (GuiRecipe) gui;
            GuiButton[] overlayButtons = null;
            try {
                overlayButtons = (GuiButton[]) this.overlayButtonsField.get(guiRecipe);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (overlayButtons != null) {
                for (GuiButton button : overlayButtons) {
                    if (button.visible && button.enabled && GuiUtils.isMouseOverButton(button, mouseX, mouseY)) {
                        final int OVERLAY_BUTTON_ID_START = 4;
                        IRecipeHandler handler = null;
                        int recipesPerPage = -1;
                        try {
                            recipesPerPage = (int) this.recipesPerPageMethod.invoke(guiRecipe);
                            handler = (IRecipeHandler) this.handlerField.get(guiRecipe);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        if (recipesPerPage >= 0 && handler != null) {
                            int recipeIndex = guiRecipe.page * recipesPerPage + button.id - OVERLAY_BUTTON_ID_START;
                            List<PositionedStack> ingredients = handler.getIngredientStacks(recipeIndex);
                            IngredientTracker tracker = createTracker(guiRecipe.firstGui, ingredients);

                            for (int var1 = 0; var1 < ingredients.size(); var1++) {
                                PositionedStack stack = ingredients.get(var1);
                                Ingredient ingredient = tracker.getIngredients().get(var1);
                                Slot stackSlot = guiRecipe.slotcontainer.getSlotWithStack(stack, guiRecipe.getRecipePosition(recipeIndex).x, guiRecipe.getRecipePosition(recipeIndex).y);
                                if (stackSlot.equals(slot)) {
                                    GuiContainer firstGui = guiRecipe.firstGui;
                                    boolean renderAutoAbleItems = firstGui instanceof GuiCraftingTerm && ingredient.isCraftable() && ingredient.requiresToCraft();
                                    boolean renderCraftableItems = (firstGui instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(firstGui)) && ingredient.isCraftable();
                                    boolean renderMissingItems = firstGui instanceof GuiCraftingTerm && !ingredient.isCraftable() && ingredient.requiresToCraft();
                                    if (renderAutoAbleItems || renderCraftableItems) {
                                        Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
                                                slot.xDisplayPosition + 16, slot.yDisplayPosition + 16,
                                                new Color(0.0f, 0.0f, 1.0f, 0.4f).getRGB());
                                    } else if (renderMissingItems) {
                                        Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
                                                slot.xDisplayPosition + 16, slot.yDisplayPosition + 16,
                                                new Color(1.0f, 0.0f, 0.0f, 0.4f).getRGB());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiRecipe) {
            GuiRecipe guiRecipe = (GuiRecipe) event.gui;
            GuiButton[] overlayButtons = null;
            try {
                overlayButtons = (GuiButton[]) ReflectionHelper.findField(GuiRecipe.class, "overlayButtons").get(guiRecipe);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (overlayButtons != null) {
                for (GuiButton button : overlayButtons) {
                    if (button.visible && button.enabled && GuiUtils.isMouseOverButton(button, event.mouseX, event.mouseY)) {
                        drawCraftingHelperTooltip(guiRecipe, event.mouseX, event.mouseY);
                    }
                }
            }
        }

    }

    private void drawCraftingHelperTooltip(GuiRecipe guiRecipe, int mouseX, int mouseY) {
        List<String> tooltips = new ArrayList<>();
        if (guiRecipe.firstGui instanceof GuiCraftingTerm) {
            tooltips.add(String.format("%s" + EnumChatFormatting.GRAY + " + " +
                            EnumChatFormatting.BLUE + I18n.format("neenergistics.gui.tooltip.helper.crafting"),
                    EnumChatFormatting.YELLOW + Keyboard.getKeyName(NEIClientConfig.getKeyBinding("nee.preview"))));
        }
        if (guiRecipe.firstGui instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(guiRecipe.firstGui)) {
            tooltips.add(EnumChatFormatting.BLUE + I18n.format("neenergistics.gui.tooltip.helper.pattern"));
        }
        try {
            ReflectionHelper.findMethod(GuiScreen.class, guiRecipe,
                    new String[]{"drawHoveringText"},
                    List.class,
                    int.class,
                    int.class,
                    FontRenderer.class).invoke(guiRecipe, tooltips, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private IngredientTracker createTracker(GuiContainer firstGui, List<PositionedStack> ingredients) {
        IngredientTracker tracker = new IngredientTracker(firstGui, ingredients);

        //check stacks in player's inventory and crafting grid
        List<ItemStack> inventoryStacks = new ArrayList<>();
        for (Slot slot : (List<Slot>) firstGui.inventorySlots.inventorySlots) {
            boolean canGetStack = slot != null && slot.getHasStack() && slot.getStack().stackSize > 0 && slot.isItemValid(slot.getStack()) && slot.canTakeStack(Minecraft.getMinecraft().thePlayer);
            if (canGetStack) {
                inventoryStacks.add(slot.getStack().copy());
            }
        }

        for (int i = 0; i < tracker.getIngredients().size(); i++) {
            for (ItemStack stack : inventoryStacks) {
                tracker.addAvailableStack(stack);
            }
        }

        return tracker;
    }

}
