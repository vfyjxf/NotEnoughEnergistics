package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.Ingredient;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

/**
 * Please note the difference between the GTNH NEI and the official NEI
 */
public class NEEContainerDrawHandler implements IContainerDrawHandler {

    public static NEEContainerDrawHandler instance = new NEEContainerDrawHandler();

    public static final Map<String, IngredientTracker> trackerMap = new HashMap<>();

    public static boolean isGtnhNei;

    private final List<GuiButton> overlayButtons = new ArrayList<>();

    public Field overlayButtonsField;
    public Method recipesPerPageMethod;
    private boolean drawRequestTooltip;
    private boolean drawMissingTooltip;
    private boolean drawCraftableTooltip;

    public NEEContainerDrawHandler() {
        //gtnh nei support
        isGtnhNei = true;
        try {
            this.overlayButtonsField = GuiRecipe.class.getDeclaredField("overlayButtons");
            this.recipesPerPageMethod = GuiRecipe.class.getDeclaredMethod("getRecipesPerPage");
            this.overlayButtonsField.setAccessible(true);
            this.recipesPerPageMethod.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            isGtnhNei = false;
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

    @SuppressWarnings("ALL")
    @Override
    public void renderSlotOverlay(GuiContainer gui, Slot slot) {
        if (NEEConfig.drawHighlight && gui instanceof GuiRecipe && !overlayButtons.isEmpty()) {
            GuiRecipe guiRecipe = (GuiRecipe) gui;
            for (GuiButton overlayButton : overlayButtons) {
                if (GuiUtils.isMouseOverButton(overlayButton)) {
                    IngredientTracker tracker = trackerMap.get("button" + overlayButton.id);
                    if (tracker != null) {
                        for (Ingredient ingredient : tracker.getIngredients()) {
                            Point point = guiRecipe.getRecipePosition(tracker.getRecipeIndex());
                            Slot currentSlot = guiRecipe.slotcontainer.getSlotWithStack(ingredient.getIngredient(), point.x, point.y);
                            if (slot.equals(currentSlot)) {
                                if (!NEEConfig.enableCraftAmountSettingGui) {
                                    GuiContainer firstGui = guiRecipe.firstGui;
                                    boolean renderAutoAbleItems = (firstGui instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(firstGui)) && ingredient.isCraftable() && ingredient.requiresToCraft();
                                    boolean renderCraftableItems = (firstGui instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(firstGui)) && ingredient.isCraftable();
                                    boolean renderMissingItems = (firstGui instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(firstGui)) && !ingredient.isCraftable() && ingredient.requiresToCraft();
                                    if (renderAutoAbleItems || renderCraftableItems) {
                                        Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
                                                slot.xDisplayPosition + 16, slot.yDisplayPosition + 16,
                                                new Color(0.0f, 0.0f, 1.0f, 0.4f).getRGB());
                                        this.drawCraftableTooltip = renderCraftableItems;
                                        this.drawRequestTooltip = renderAutoAbleItems;
                                    }
                                    if (renderMissingItems) {
                                        Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
                                                slot.xDisplayPosition + 16, slot.yDisplayPosition + 16,
                                                new Color(1.0f, 0.0f, 0.0f, 0.4f).getRGB());
                                        this.drawMissingTooltip = true;
                                    }
                                } else {

                                    if (ingredient.isCraftable()) {
                                        Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
                                                slot.xDisplayPosition + 16, slot.yDisplayPosition + 16,
                                                new Color(0.0f, 0.0f, 1.0f, 0.4f).getRGB());
                                        this.drawRequestTooltip = true;
                                    } else {
                                        Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
                                                slot.xDisplayPosition + 16, slot.yDisplayPosition + 16,
                                                new Color(1.0f, 0.0f, 0.0f, 0.4f).getRGB());
                                        this.drawMissingTooltip = true;
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
            GuiContainer firstGui = guiRecipe.firstGui;
            boolean isGuiPatternTerm = firstGui instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(firstGui);
            boolean isCraftingTerm = firstGui instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(firstGui);
            if (isCraftingTerm || isGuiPatternTerm) {
                if (overlayButtons.isEmpty()) {
                    setOverlayButtons(guiRecipe);
                }

                if (!checkTracker()) {
                    trackerMap.clear();
                    for (GuiButton button : overlayButtons) {
                        if (GuiUtils.isMouseOverButton(button)) {
                            trackerMap.put("button" + button.id, getTracker(guiRecipe, button, isCraftingTerm));
                        }
                    }
                }

                if (this.drawCraftableTooltip || this.drawRequestTooltip || this.drawMissingTooltip) {
                    for (GuiButton button : overlayButtons) {
                        if (button.visible && button.enabled && GuiUtils.isMouseOverButton(button)) {
                            drawCraftingHelperTooltip(guiRecipe, event.mouseX, event.mouseY);
                            this.drawCraftableTooltip = false;
                            this.drawRequestTooltip = false;
                            this.drawMissingTooltip = false;
                        }
                    }

                }
            }
        }
    }


    private void drawCraftingHelperTooltip(GuiRecipe guiRecipe, int mouseX, int mouseY) {
        List<String> tooltips = new ArrayList<>();
        boolean isCraftingTerm = guiRecipe.firstGui instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(guiRecipe.firstGui);
        if (isCraftingTerm) {
            if (this.drawRequestTooltip) {
                tooltips.add(String.format("%s" + EnumChatFormatting.GRAY + " + " +
                                EnumChatFormatting.BLUE + I18n.format("neenergistics.gui.tooltip.helper.crafting"),
                        EnumChatFormatting.YELLOW + Keyboard.getKeyName(NEIClientConfig.getKeyBinding("nee.preview"))));
            }
            if (this.drawMissingTooltip) {
                tooltips.add(EnumChatFormatting.RED + I18n.format("neenergistics.gui.tooltip.missing"));
            }
        }
        boolean isPatternTerm = guiRecipe.firstGui instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(guiRecipe.firstGui);
        if (this.drawCraftableTooltip && isPatternTerm) {
            tooltips.add(EnumChatFormatting.BLUE + I18n.format("neenergistics.gui.tooltip.helper.pattern"));
        }
        //drawHoveringText
        guiRecipe.func_146283_a(tooltips, mouseX, mouseY);
    }

    private void setOverlayButtons(GuiRecipe guiRecipe) {
        List<GuiButton> overlayButtons = null;
        if (isGtnhNei) {
            try {
                overlayButtons = new ArrayList<>(Arrays.asList((GuiButton[]) NEEContainerDrawHandler.instance.overlayButtonsField.get(guiRecipe)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            overlayButtons = new ArrayList<>();
            GuiButton overlay1 = ReflectionHelper.getPrivateValue(GuiRecipe.class, guiRecipe, "overlay1");
            GuiButton overlay2 = ReflectionHelper.getPrivateValue(GuiRecipe.class, guiRecipe, "overlay2");
            overlayButtons.add(overlay1);
            overlayButtons.add(overlay2);
        }
        if (overlayButtons != null) {
            this.overlayButtons.addAll(overlayButtons);
        }
    }

    private boolean checkTracker() {
        for (GuiButton overlayButton : overlayButtons) {
            if (trackerMap.get("button" + overlayButton.id) != null) {
                return true;
            }
        }
        return false;
    }

    private IngredientTracker getTracker(GuiRecipe guiRecipe, GuiButton overlayButton, boolean isCraftingTerm) {
        if (overlayButton.enabled && overlayButton.visible) {
            final int OVERLAY_BUTTON_ID_START = 4;
            IRecipeHandler handler = guiRecipe.currenthandlers.get(guiRecipe.recipetype);
            int recipesPerPage = 2;
            if (isGtnhNei) {
                try {
                    recipesPerPage = (int) NEEContainerDrawHandler.instance.recipesPerPageMethod.invoke(guiRecipe);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (recipesPerPage >= 0 && handler != null) {
                int recipeIndex = guiRecipe.page * recipesPerPage + overlayButton.id - OVERLAY_BUTTON_ID_START;
                return isCraftingTerm ? new IngredientTracker(guiRecipe.firstGui, handler, recipeIndex) : new IngredientTracker(guiRecipe, handler, recipeIndex);
            }
        }
        return null;
    }
}
