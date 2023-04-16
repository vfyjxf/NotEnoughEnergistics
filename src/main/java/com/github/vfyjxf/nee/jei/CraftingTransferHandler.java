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
import com.github.vfyjxf.nee.helper.PlatformHelper;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.utils.Globals;
import com.github.vfyjxf.nee.utils.GuiUtils;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Loader;
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

    //TODO:Rewrite this, because the recipe length accepted by pae2 varies between 9 and 16.
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
        if (parent != null && GuiUtils.isCraftingTerm(parent)) {
            GuiContainer craftingTerm = (GuiContainer) parent;
            //TODO:Pattern Interface support
            if (doTransfer) {
                boolean preview = KeyBindings.isPreviewKeyDown();
                boolean nonPreview = KeyBindings.isNonPreviewKeyDown();
                if (preview || nonPreview) {
                    RecipeAnalyzer analyzer = createAnalyzer(parent);
                    if (analyzer == null) return null;
                    requester.setRequested(false, nonPreview, initAnalyzer(analyzer, craftingTerm, recipeLayout, player).analyzeRecipe(recipeLayout));
                    requester.requestNext();
                } else {
                    moveItems(container, recipeLayout);
                }
            } else {
                RecipeAnalyzer analyzer = createAnalyzer(parent);
                if (analyzer == null) return null;
                return new CraftingInfoError(initAnalyzer(analyzer, craftingTerm, recipeLayout, player), recipeLayout, true);
            }
        }
        return null;
    }

    private RecipeAnalyzer initAnalyzer(@Nonnull RecipeAnalyzer analyzer, @Nonnull GuiContainer craftingTerm, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
        IItemHandler craftMatrix = CraftingHelper.getCraftMatrix(craftingTerm);
        List<ItemStack> stacks = new ArrayList<>();
        if (craftMatrix != null) stacks.addAll(CraftingHelper.copyAllNonEmpty(craftMatrix));
        if (player.inventory != null) stacks.addAll(CraftingHelper.copyAllNonEmpty(player.inventory));
        stacks.forEach(analyzer::addAvailableIngredient);
        return analyzer;
    }

    private RecipeAnalyzer createAnalyzer(@Nonnull GuiScreen screen) {
        if (screen instanceof GuiCraftingTerm) {
            return new RecipeAnalyzer(((GuiCraftingTerm) screen));
        }
        if (Loader.isModLoaded(Globals.WCT) || PlatformHelper.isWirelessGui(screen)) {
            return createAnalyzer((GuiContainer) screen);
        }
        return null;
    }

    @Optional.Method(modid = Globals.WCT)
    private RecipeAnalyzer createAnalyzer(@Nonnull GuiContainer wirelessTerm) {
        return new RecipeAnalyzer(wirelessTerm);
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

                        recipe.setTag(Globals.INPUT_KEY_HEAD + slot.getSlotIndex(), tags);
                        break;
                    }
                }
            }

            slotIndex++;
        }

        try {
            if (container instanceof ContainerCraftingTerm || PlatformHelper.isWirelessContainer(container)) {
                NetworkHandler.instance().sendToServer(new PacketJEIRecipe(recipe));
            } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                moveItemsForWirelessTerm(recipe);
            }
        } catch (IOException e) {
            AELog.debug(e);
        }

    }

    @Optional.Method(modid = Globals.WCT)
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
