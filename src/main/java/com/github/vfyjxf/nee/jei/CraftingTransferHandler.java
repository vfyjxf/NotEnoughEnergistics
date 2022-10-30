package com.github.vfyjxf.nee.jei;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketJEIRecipe;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.Platform;
import com.github.vfyjxf.nee.config.KeyBindings;
import com.github.vfyjxf.nee.helper.CraftingHelper;
import com.github.vfyjxf.nee.helper.IngredientRequester;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ModIds;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.init.ModNetworking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftingTransferHandler<C extends AEBaseContainer & IContainerCraftingPacket> implements IRecipeTransferHandler<C> {

    private final Class<C> containerClass;
    private final IngredientRequester requester;

    private static boolean isPatternInterfaceExists = false;

    public static final int RECIPE_LENGTH = 9;

    public CraftingTransferHandler(Class<C> containerClass) {
        this.containerClass = containerClass;
        this.requester = IngredientRequester.getInstance();
    }

    @Override
    @Nonnull
    public Class<C> getContainerClass() {
        return this.containerClass;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull C container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        GuiScreen parent = GuiUtils.getParentScreen();
        if (parent instanceof GuiCraftingTerm) {
            GuiCraftingTerm craftingTerm = (GuiCraftingTerm) parent;
            //TODO:Pattern Interface support
            //TODO:Wireless Crafting Term support
            if (doTransfer) {
                boolean preview = KeyBindings.isPreviewKeyDown();
                boolean nonPreview = KeyBindings.isNonPreviewKeyDown();
                if (preview || nonPreview) {
                    requester.setRequested(false, nonPreview, getIngredient(craftingTerm, recipeLayout, player));
                    requester.requestNext();
                } else {
                    moveItems(container, recipeLayout);
                }
            } else {
                return new CraftingInfoError(getIngredient(craftingTerm, recipeLayout, player), true);
            }
        }
        return null;
    }

    private List<RecipeAnalyzer.RecipeIngredient> getIngredient(@Nonnull GuiCraftingTerm craftingTerm, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
        RecipeAnalyzer analyzer = new RecipeAnalyzer(craftingTerm);
        IItemHandler craftMatrix = CraftingHelper.getCraftMatrix(craftingTerm);
        List<ItemStack> stacks = new ArrayList<>();
        if (craftMatrix != null) stacks.addAll(CraftingHelper.copyAllNonEmpty(craftMatrix));
        if (player.inventory != null) stacks.addAll(CraftingHelper.copyAllNonEmpty(player.inventory));
        stacks.forEach(analyzer::addAvailableIngredient);
        return analyzer.analyzeRecipe(recipeLayout);
    }

    /**
     * From  {@link appeng.integration.modules.jei.RecipeTransferHandler#transferRecipe(Container, IRecipeLayout, EntityPlayer, boolean, boolean)}
     */
    private void moveItems(AEBaseContainer container, IRecipeLayout recipeLayout) {

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
            if (container instanceof ContainerCraftingTerm) {
                NetworkHandler.instance().sendToServer(new PacketJEIRecipe(recipe));
            } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                moveItemsForWirelessTerm(recipe);
            }
        } catch (IOException e) {
            AELog.debug(e);
        }

    }

    /*
    private NBTTagCompound packCraftingRecipe(IRecipeLayout recipeLayout) {
        final NBTTagCompound recipe = new NBTTagCompound();
        NBTTagCompound reslut = null;
        String recipeType = recipeLayout.getRecipeCategory().getUid();
        final Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        int inputIndex = 0;

        List<StackProcessor> tInputs = new ArrayList<>();
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
            final IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if (ingredient != null) {
                //get itemstack from ingredient
                ItemStack displayedIngredient = ingredient.getDisplayedIngredient() == null ? ItemStack.EMPTY : ingredient.getDisplayedIngredient().copy();
                ItemStack firstIngredient = ingredient.getAllIngredients().isEmpty() ? ItemStack.EMPTY : ingredient.getAllIngredients().get(0).copy();
                ItemStack currentStack = NEEConfig.useDisplayedIngredient ? displayedIngredient : firstIngredient;
                if (ingredient.isInput()) {
                    tInputs.add(new StackProcessor(ingredient, currentStack, currentStack.getCount()));
                } else {
                    if (!currentStack.isEmpty() && reslut == null) {
                        reslut = currentStack.writeToNBT(new NBTTagCompound());
                        recipe.setTag(OUTPUT_KEY, reslut);
                    }
                }

            }

        }

        for (StackProcessor currentIngredient : tInputs) {
            ItemStack currentStack = currentIngredient.getCurrentStack();
            ItemStack preferModItem = ItemUtils.isPreferModItem(currentStack) ? currentStack : ItemUtils.getPreferModItem(currentIngredient.getIngredient());

            if (!currentStack.isEmpty()) {
                currentStack.setCount(currentIngredient.getStackSize());
            }

            if (!currentStack.isEmpty() && preferModItem != null && !preferModItem.isEmpty()) {
                currentStack = preferModItem.copy();
                currentStack.setCount(currentIngredient.getStackSize());
            }
            for (ItemStack stack : currentIngredient.getIngredient().getAllIngredients()) {
                if (ItemUtils.isPreferItems(stack, recipeType) && !currentStack.isEmpty()) {
                    currentStack = stack.copy();
                    currentStack.setCount(currentIngredient.getStackSize());
                }
            }
            recipe.setTag("#" + inputIndex, currentStack.writeToNBT(new NBTTagCompound()));
            inputIndex++;
        }

        return recipe;
    }

    @Optional.Method(modid = ModIds.WCT)
    private void openWirelessCraftingAmountGui(Container container, IRecipeLayout recipeLayout) {
        if (container instanceof ContainerWCT) {
            ContainerWCT wct = (ContainerWCT) container;
            NEENetworkHandler.getInstance().sendToServer(new PacketOpenCraftAmount(packCraftingRecipe(recipeLayout), wct.isWTBauble(), wct.getWTSlot()));
        }
    }

     */

    @Optional.Method(modid = ModIds.WCT)
    private void moveItemsForWirelessTerm(NBTTagCompound recipe) {
        try {
            ModNetworking.instance().sendToServer(new p455w0rd.wct.sync.packets.PacketJEIRecipe(recipe));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isIsPatternInterfaceExists() {
        return isPatternInterfaceExists;
    }

    public static void setIsPatternInterfaceExists(boolean isPatternInterfaceExists) {
        CraftingTransferHandler.isPatternInterfaceExists = isPatternInterfaceExists;
    }

}
