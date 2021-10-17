package com.github.vfyjxf.nee.jei;

import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketJEIRecipe;
import appeng.util.Platform;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingHelper;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.vfyjxf.nee.client.MouseHandler.craftingHelperNoPreview;
import static com.github.vfyjxf.nee.client.MouseHandler.craftingHelperPreview;

public class CraftingHelperTransferHandler implements IRecipeTransferHandler<ContainerCraftingTerm> {

    public static IngredientTracker tracker = null;
    public static int stackIndex = 1;
    public static boolean noPreview = false;


    @Override
    public Class<ContainerCraftingTerm> getContainerClass() {
        return ContainerCraftingTerm.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerCraftingTerm container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        tracker = crateTracker(container, recipeLayout, player);
        if (doTransfer) {
            tracker = crateTracker(container, recipeLayout, player);
            boolean doCraftingHelp = Keyboard.isKeyDown(craftingHelperPreview.getKeyCode()) || Keyboard.isKeyDown(craftingHelperNoPreview.getKeyCode());
            noPreview = craftingHelperNoPreview.isKeyDown();
            if (doCraftingHelp && !tracker.getRequireToCraftStacks().isEmpty()) {
                for (ItemStack requireToCraftStack : tracker.getRequireToCraftStacks()) {
                    if (!requireToCraftStack.isEmpty()) {
                        NEENetworkHandler.getInstance().sendToServer(new PacketCraftingHelper(requireToCraftStack, noPreview));
                        stackIndex = 1;
                        break;
                    }
                }

            } else {
                moveItems(container, recipeLayout);
            }
        } else {
            return new CraftingHelperTooltipError(tracker, true);
        }

        return null;
    }

    private IngredientTracker crateTracker(ContainerCraftingTerm container, IRecipeLayout recipeLayout, EntityPlayer player) {
        IngredientTracker tracker = new IngredientTracker(recipeLayout);
        //check stacks in player's inventory and crafting grid
        List<ItemStack> inventoryStacks = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < player.inventory.getSizeInventory(); slotIndex++) {
            if (!player.inventory.getStackInSlot(slotIndex).isEmpty()) {
                inventoryStacks.add(player.inventory.getStackInSlot(slotIndex).copy());
            }
        }

        //check stacks in crafting grid
        final IItemHandler craftMatrix = container.getInventoryByName("crafting");
        for (int slotIndex = 0; slotIndex < craftMatrix.getSlots(); slotIndex++) {
            if (!craftMatrix.getStackInSlot(slotIndex).isEmpty()) {
                inventoryStacks.add(craftMatrix.getStackInSlot(slotIndex).copy());
            }
        }

        for (int i = 0; i < tracker.getIngredients().size(); i++) {
            for (ItemStack stack : inventoryStacks) {
                tracker.addAvailableStack(stack);
            }
        }

        return tracker;
    }

    private void moveItems(ContainerCraftingTerm container, IRecipeLayout recipeLayout) {

        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

        final NBTTagCompound recipe = new NBTTagCompound();

        int slotIndex = 0;
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
            if (!ingredient.isInput()) {
                continue;
            }

            for (final Slot slot : container.inventorySlots) {
                if (slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix) {
                    if (slot.getSlotIndex() == slotIndex) {
                        final NBTTagList tags = new NBTTagList();
                        final List<ItemStack> list = new ArrayList<>();
                        final ItemStack displayed = ingredient.getDisplayedIngredient();

                        // prefer currently displayed item
                        if (displayed != null && !displayed.isEmpty()) {
                            list.add(displayed);
                        }

                        // prefer pure crystals.
                        for (ItemStack stack : ingredient.getAllIngredients()) {
                            if (Platform.isRecipePrioritized(stack)) {
                                list.add(0, stack);
                            } else {
                                list.add(stack);
                            }
                        }

                        for (final ItemStack is : list) {
                            final NBTTagCompound tag = new NBTTagCompound();
                            is.writeToNBT(tag);
                            tags.appendTag(tag);
                        }

                        recipe.setTag("#" + slot.getSlotIndex(), tags);
                        break;
                    }
                }
            }

            slotIndex++;
        }

        try {
            NetworkHandler.instance().sendToServer(new PacketJEIRecipe(recipe));
        } catch (IOException e) {
            AELog.debug(e);
        }

    }

}
