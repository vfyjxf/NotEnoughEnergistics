package com.github.vfyjxf.nee.jei;

import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.config.ItemCombination;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeTransfer;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.github.vfyjxf.nee.utils.StackProcessor;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author vfyjxf
 */
public class PatternRecipeTransferHandler implements IRecipeTransferHandler<ContainerPatternTerm> {

    public static final String OUTPUT_KEY = "Outputs";
    public static final String INPUT_KEY = "#";
    public static Map<String, List<ItemStack>> ingredients = new HashMap<>();

    public PatternRecipeTransferHandler() {
    }

    @Override
    @Nonnull
    public Class<ContainerPatternTerm> getContainerClass() {
        return ContainerPatternTerm.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerPatternTerm container, IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        String recipeType = recipeLayout.getRecipeCategory().getUid();
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);
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
                    //get itemstack from ingredient
                    ItemStack displayedIngredient = ingredient.getDisplayedIngredient() == null ? ItemStack.EMPTY : ingredient.getDisplayedIngredient().copy();
                    ItemStack firstIngredient = this.getFirstStack(ingredient);
                    ItemStack currentStack = (NEEConfig.useDisplayedIngredient && !displayedIngredient.isEmpty()) ? displayedIngredient : firstIngredient;

                    if (ingredient.isInput()) {
                        if (isCraftingRecipe) {
                            tInputs.add(new StackProcessor(ingredient, currentStack, currentStack.getCount()));
                        } else {
                            //Combine like stacks
                            boolean find = false;
                            if (currentStack.isEmpty()) {
                                continue;
                            }
                            ItemCombination currentValue = ItemCombination.valueOf(NEEConfig.itemCombinationMode);
                            if (currentValue != ItemCombination.DISABLED) {
                                boolean isWhitelist = currentValue == ItemCombination.WHITELIST && Arrays.asList(NEEConfig.itemCombinationWhitelist).contains(recipeType);
                                if (currentValue == ItemCombination.ENABLED || isWhitelist) {
                                    for (StackProcessor storedIngredient : tInputs) {
                                        ItemStack storedStack = storedIngredient.getIngredient().getDisplayedIngredient();
                                        if (storedStack != null && currentStack.isItemEqual(storedStack) && ItemStack.areItemStackTagsEqual(currentStack, storedStack)) {
                                            if (currentStack.getCount() + storedIngredient.getStackSize() <= storedStack.getMaxStackSize()) {
                                                find = true;
                                                storedIngredient.setStackSize(currentStack.getCount() + storedIngredient.getStackSize());
                                            }
                                        }
                                    }
                                }
                            }
                            if (!find) {
                                tInputs.add(new StackProcessor(ingredient, currentStack, currentStack.getCount()));
                            }
                        }
                    } else {
                        if (outputIndex >= 3 || currentStack.isEmpty() || (container.isCraftingMode())) {
                            continue;
                        }
                        recipeOutputs.setTag(OUTPUT_KEY + outputIndex, currentStack.writeToNBT(new NBTTagCompound()));
                        outputIndex++;
                    }
                }

            }
            PatternRecipeTransferHandler.ingredients.clear();
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
                if (!currentStack.isEmpty() && ItemUtils.isInBlackList(currentStack, recipeType) && !isCraftingRecipe) {
                    continue;
                }
                recipeInputs.setTag("#" + inputIndex, currentStack.writeToNBT(new NBTTagCompound()));
                List<ItemStack> ingredientList = new ArrayList<>(currentIngredient.getIngredient().getAllIngredients());
                ingredientList.removeIf(stack -> stack == null || stack.isEmpty());
                PatternRecipeTransferHandler.ingredients.put(INPUT_KEY + inputIndex, ingredientList);
                inputIndex++;
            }
            NEENetworkHandler.getInstance().sendToServer(new PacketRecipeTransfer(recipeInputs, recipeOutputs, isCraftingRecipe));
            if (NEEConfig.allowPrintRecipeType) {
                NotEnoughEnergistics.logger.info(recipeType);
            }
        } else {
            if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
                return new CraftingHelperTooltipError(new IngredientTracker(recipeLayout, (RecipesGui) Minecraft.getMinecraft().currentScreen), false);
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

    private ItemStack getFirstStack(IGuiIngredient<ItemStack> ingredient) {
        if (!ingredient.getAllIngredients().isEmpty()) {
            return ingredient.getAllIngredients()
                    .stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .findFirst()
                    .map(ItemStack::copy)
                    .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

}
