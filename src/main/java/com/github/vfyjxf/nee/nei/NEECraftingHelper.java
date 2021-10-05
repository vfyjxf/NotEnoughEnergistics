package com.github.vfyjxf.nee.nei;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIRecipe;
import appeng.util.Platform;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingHelper;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;


public class NEECraftingHelper implements IOverlayHandler {
    public static IngredientTracker tracker = null;
    public static int stackIndex = 1;
    public static boolean noPreview = false;

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        if (firstGui instanceof GuiCraftingTerm && NEECraftingHandler.isCraftingTableRecipe(recipe) && container instanceof ContainerCraftingTerm) {
            tracker = createTracker(firstGui, recipe, recipeIndex);
            boolean doCraftingHelp = Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.preview")) || Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.nopreview"));
            noPreview = Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.nopreview"));
            if (doCraftingHelp && !tracker.getRequireToCraftStacks().isEmpty()) {
                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingHelper(tracker.getRequireToCraftStacks().get(0), noPreview));
                stackIndex = 1;
            } else {
                moveItem(firstGui, recipe, recipeIndex);
            }
        }
    }

    private IngredientTracker createTracker(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        IngredientTracker tracker = new IngredientTracker(firstGui, recipe, recipeIndex);

        //check stacks in player's inventory and crafting grid
        for (Slot slot : (List<Slot>) firstGui.inventorySlots.inventorySlots) {
            boolean canGetStack = slot != null && slot.getHasStack() && slot.getStack().stackSize > 0 && slot.isItemValid(slot.getStack()) && slot.canTakeStack(Minecraft.getMinecraft().thePlayer);
            if (canGetStack) {
                tracker.addAvailableStack(slot.getStack());
            }
        }

        return tracker;
    }

    /**
     * Copied from GTNewHorizons/Applied-Energistics-2-Unofficial
     */
    private void moveItem(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        try {
            final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
            PacketNEIRecipe packet = new PacketNEIRecipe(packIngredients(firstGui, ingredients, false));
            if (packet.size() >= 32 * 1024) {
                AELog.warn("Recipe for " + recipe.getRecipeName() + " has too many variants, reduced version will be used");
                packet = new PacketNEIRecipe(packIngredients(firstGui, ingredients, true));
            }
            NetworkHandler.instance.sendToServer(packet);
        } catch (final Exception | Error ignored) {
        }
    }

    /**
     * Copied from GTNewHorizons/Applied-Energistics-2-Unofficial
     */
    private boolean testSize(final NBTTagCompound recipe) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DataOutputStream outputStream = new DataOutputStream(bytes);
        CompressedStreamTools.writeCompressed(recipe, outputStream);
        return bytes.size() > 3 * 1024;
    }

    /**
     * Copied from GTNewHorizons/Applied-Energistics-2-Unofficial
     */
    private NBTTagCompound packIngredients(GuiContainer gui, List<PositionedStack> ingredients, boolean limited) throws IOException {
        final NBTTagCompound recipe = new NBTTagCompound();
        for (final PositionedStack positionedStack : ingredients) {
            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;
            if (positionedStack.items != null && positionedStack.items.length > 0) {
                for (final Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots) {
                    if (slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix) {
                        if (slot.getSlotIndex() == col + row * 3) {
                            final NBTTagList tags = new NBTTagList();
                            final List<ItemStack> list = new LinkedList<>();

                            // prefer pure crystals.
                            for (int x = 0; x < positionedStack.items.length; x++) {
                                if (Platform.isRecipePrioritized(positionedStack.items[x])) {
                                    list.add(0, positionedStack.items[x]);
                                } else {
                                    list.add(positionedStack.items[x]);
                                }
                            }

                            for (final ItemStack is : list) {
                                final NBTTagCompound tag = new NBTTagCompound();
                                is.writeToNBT(tag);
                                tags.appendTag(tag);
                                if (limited) {
                                    final NBTTagCompound test = new NBTTagCompound();
                                    test.setTag("#" + slot.getSlotIndex(), tags);
                                    if (testSize(test)) {
                                        break;
                                    }
                                }
                            }

                            recipe.setTag("#" + slot.getSlotIndex(), tags);
                            break;
                        }
                    }
                }
            }
        }
        return recipe;
    }

    @SubscribeEvent
    public void onActionPerformedEventPre(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        //make nei's transfer system doesn't require presses shift
        if (event.gui instanceof GuiRecipe) {
            GuiRecipe guiRecipe = (GuiRecipe) event.gui;
            GuiButton[] overlayButtons = null;
            final int OVERLAY_BUTTON_ID_START = 4;
            try {
                overlayButtons = (GuiButton[]) ReflectionHelper.findField(GuiRecipe.class, "overlayButtons").get(guiRecipe);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (overlayButtons != null && event.button.id >= OVERLAY_BUTTON_ID_START && event.button.id < OVERLAY_BUTTON_ID_START + overlayButtons.length) {
                if (guiRecipe.firstGui instanceof GuiPatternTerm || guiRecipe.firstGui instanceof GuiCraftingTerm || GuiUtils.isPatternTermExGui(guiRecipe.firstGui)) {
                    int recipesPerPage = -1;
                    IRecipeHandler handler = null;
                    try {
                        recipesPerPage = (int) ReflectionHelper.findMethod(GuiRecipe.class, guiRecipe, new String[]{"getRecipesPerPage"}).invoke(guiRecipe);
                        handler = (IRecipeHandler) ReflectionHelper.findField(GuiRecipe.class, "handler").get(guiRecipe);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (recipesPerPage >= 0 && handler != null) {
                        int recipe = guiRecipe.page * recipesPerPage + event.button.id - OVERLAY_BUTTON_ID_START;
                        final IOverlayHandler overlayHandler = handler.getOverlayHandler(guiRecipe.firstGui, recipe);
                        Minecraft.getMinecraft().displayGuiScreen(guiRecipe.firstGui);
                        overlayHandler.overlayRecipe(guiRecipe.firstGui, guiRecipe.currenthandlers.get(guiRecipe.recipetype), recipe, NEIClientUtils.shiftKey());
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

}
