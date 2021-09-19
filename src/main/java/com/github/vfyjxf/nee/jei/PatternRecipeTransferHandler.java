package com.github.vfyjxf.nee.jei;

import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeTransfer;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.github.vfyjxf.nee.utils.StackProcessor;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.vfyjxf.nee.jei.NEEJEIPlugin.registry;

/**
 * @author vfyjxf
 */
public class PatternRecipeTransferHandler implements IRecipeTransferHandler<ContainerPatternTerm> {

    public static final String OUTPUT_KEY = "Outputs";
    public static Map<String, IGuiIngredient<ItemStack>> ingredients = new HashMap<>();

    public PatternRecipeTransferHandler() {
    }

    @Override
    public Class<ContainerPatternTerm> getContainerClass() {
        return ContainerPatternTerm.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerPatternTerm container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        String recipeType = recipeLayout.getRecipeCategory().getUid();
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);
        if (container.isCraftingMode()) {
            if (!isCraftingRecipe && !NEEConfig.allowAutomaticSwitchPatternTerminalMode) {
                return registry.getJeiHelpers().recipeTransferHandlerHelper().createUserErrorWithTooltip(I18n.format("jei.tooltip.nee.need.processing"));
            }
        } else {
            if (isCraftingRecipe && !NEEConfig.allowAutomaticSwitchPatternTerminalMode) {
                return registry.getJeiHelpers().recipeTransferHandlerHelper().createUserErrorWithTooltip(I18n.format("jei.tooltip.nee.need.crating"));
            }
        }
        if (doTransfer) {
            final Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
            final NBTTagCompound recipeInputs = new NBTTagCompound();
            final NBTTagCompound recipeOutputs = new NBTTagCompound();
            int inputIndex = 0;
            int outputIndex = 0;
            List<StackProcessor> tInputs = new ArrayList<>();
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
                final IGuiIngredient<ItemStack> ingredient = entry.getValue();
                if (ingredient != null) {
                    ItemStack currentStack = ingredient.getDisplayedIngredient() == null ? ItemStack.EMPTY : ingredient.getDisplayedIngredient().copy();
                    if (ingredient.isInput()) {
                        if (isCraftingRecipe) {
                            tInputs.add(new StackProcessor(ingredient, currentStack.getCount()));
                        } else {
                            boolean find = false;
                            if (currentStack.isEmpty()) {
                                continue;
                            }
                            for (StackProcessor storedIngredient : tInputs) {
                                ItemStack storedStack = storedIngredient.ingredient.getDisplayedIngredient();
                                if (storedStack != null && currentStack.isItemEqual(storedStack) && ItemStack.areItemStackTagsEqual(currentStack, storedStack)) {
                                    if (currentStack.getCount() + storedIngredient.stackSize <= storedStack.getMaxStackSize()) {
                                        find = true;
                                        storedIngredient.stackSize = currentStack.getCount() + storedIngredient.stackSize;
                                    }
                                }
                            }
                            if (!find) {
                                tInputs.add(new StackProcessor(ingredient, currentStack.getCount()));
                            }
                        }
                    } else {
                        if (outputIndex >= 3 || currentStack.isEmpty() || (container.isCraftingMode() && !NEEConfig.allowAutomaticSwitchPatternTerminalMode)) {
                            continue;
                        }
                        recipeOutputs.setTag(OUTPUT_KEY + outputIndex, currentStack.writeToNBT(new NBTTagCompound()));
                        outputIndex++;
                    }
                }

            }

            for (StackProcessor currentIngredient : tInputs) {
                ItemStack currentStack = currentIngredient.ingredient.getDisplayedIngredient() == null ? ItemStack.EMPTY : currentIngredient.ingredient.getDisplayedIngredient().copy();
                ItemStack preferModItem = ItemUtils.isPreferModItem(currentStack) ? currentStack : ItemUtils.getPreferModItem(currentIngredient.ingredient);

                if (!currentStack.isEmpty()) {
                    currentStack.setCount(currentIngredient.stackSize);
                }

                if (!currentStack.isEmpty() && preferModItem != null && !preferModItem.isEmpty()) {
                    currentStack = preferModItem.copy();
                    currentStack.setCount(currentIngredient.stackSize);
                }
                for (ItemStack stack : currentIngredient.ingredient.getAllIngredients()) {
                    if (ItemUtils.isPreferItems(stack, recipeType) && !currentStack.isEmpty()) {
                        currentStack = stack.copy();
                        currentStack.setCount(currentIngredient.stackSize);
                    }
                }
                if (!currentStack.isEmpty() && ItemUtils.isInBlackList(currentStack, recipeType) && !isCraftingRecipe) {
                    continue;
                }
                recipeInputs.setTag("#" + inputIndex, currentStack.writeToNBT(new NBTTagCompound()));
                PatternRecipeTransferHandler.ingredients.put("input" + inputIndex, currentIngredient.ingredient);
                inputIndex++;
            }
            NEENetworkHandler.getInstance().sendToServer(new PacketRecipeTransfer(recipeInputs, recipeOutputs, isCraftingRecipe));
            if (NEEConfig.allowPrintRecipeType) {
                NotEnoughEnergistics.logger.info(recipeType);
            }
        }

        return null;
    }

    private boolean isCraftingRecipe(String recipeType) {
        if (!recipeType.equals(VanillaRecipeCategoryUid.INFORMATION) && !recipeType.equals(VanillaRecipeCategoryUid.FUEL)) {
            return recipeType.equals(VanillaRecipeCategoryUid.CRAFTING);
        }
        return false;
    }

}
