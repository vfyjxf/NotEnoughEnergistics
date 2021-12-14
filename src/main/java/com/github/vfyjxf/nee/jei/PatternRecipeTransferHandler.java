package com.github.vfyjxf.nee.jei;

import appeng.container.me.items.PatternTermContainer;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packets.PacketRecipeTransfer;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.github.vfyjxf.nee.utils.StackProcessor;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.vfyjxf.nee.config.NEEConfig.CLIENT_CONFIG;


/**
 * @author vfyjxf
 */
public class PatternRecipeTransferHandler implements IRecipeTransferHandler<PatternTermContainer> {

    public static final String OUTPUT_KEY = "Outputs";
    public static Map<String, IGuiIngredient<ItemStack>> ingredients = new HashMap<>();

    @Override
    public Class<PatternTermContainer> getContainerClass() {
        return PatternTermContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(PatternTermContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        ResourceLocation recipeType = recipeLayout.getRecipeCategory().getUid();
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);

        if (doTransfer) {
            final Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
            final CompoundNBT recipeInputs = new CompoundNBT();
            final CompoundNBT recipeOutputs = new CompoundNBT();
            int inputIndex = 0;
            int outputIndex = 0;
            List<StackProcessor> tInputs = new ArrayList<>();
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
                final IGuiIngredient<ItemStack> ingredient = entry.getValue();
                if (ingredient != null) {

                    ItemStack displayedIngredient = ingredient.getDisplayedIngredient() == null ? ItemStack.EMPTY : ingredient.getDisplayedIngredient().copy();
                    ItemStack firstIngredient = ingredient.getAllIngredients().isEmpty() ? ItemStack.EMPTY : ingredient.getAllIngredients().get(0).copy();
                    ItemStack currentStack = CLIENT_CONFIG.useDisplayedIngredient() ? displayedIngredient : firstIngredient;

                    if (ingredient.isInput()) {
                        if (isCraftingRecipe) {
                            tInputs.add(new StackProcessor(ingredient, currentStack, currentStack.getCount()));
                        } else {
                            boolean find = false;
                            if (currentStack.isEmpty()) {
                                continue;
                            }
                            for (StackProcessor storedIngredient : tInputs) {
                                ItemStack storedStack = storedIngredient.getIngredient().getDisplayedIngredient();
                                if (storedStack != null && currentStack.sameItem(storedStack) && ItemStack.tagMatches(currentStack, storedStack)) {
                                    if (currentStack.getCount() + storedIngredient.getStackSize() <= storedStack.getMaxStackSize()) {
                                        find = true;
                                        storedIngredient.setStackSize(currentStack.getCount() + storedIngredient.getStackSize());
                                    }
                                }
                            }
                            if (!find) {
                                tInputs.add(new StackProcessor(ingredient, currentStack, currentStack.getCount()));
                            }
                        }
                    } else {
                        if (outputIndex >= 3 || currentStack.isEmpty()) {
                            continue;
                        }
                        recipeOutputs.put(OUTPUT_KEY + outputIndex, currentStack.serializeNBT());
                        outputIndex++;
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
                    if (!currentStack.isEmpty() && ItemUtils.isPreferItems(stack, recipeType)) {
                        currentStack = stack.copy();
                        currentStack.setCount(currentIngredient.getStackSize());
                    }
                }
                if (!currentStack.isEmpty() && ItemUtils.isInBlackList(currentStack, recipeType) && !isCraftingRecipe) {
                    continue;
                }
                recipeInputs.put("#" + inputIndex, currentStack.serializeNBT());
                PatternRecipeTransferHandler.ingredients.put("input" + inputIndex, currentIngredient.getIngredient());
                inputIndex++;
            }
            NEENetworkHandler.getInstance().sendToServer(new PacketRecipeTransfer(recipeInputs, recipeOutputs, isCraftingRecipe));
            if (CLIENT_CONFIG.allowPrintRecipeType()) {
                NotEnoughEnergistics.logger.info("Recipe Type:" + recipeType);
            }
        } else {
            return new CraftingHelperTooltipError(new IngredientTracker(recipeLayout), false);
        }

        return null;
    }

    private boolean isCraftingRecipe(ResourceLocation recipeType) {
        if (!recipeType.equals(VanillaRecipeCategoryUid.INFORMATION) && !recipeType.equals(VanillaRecipeCategoryUid.FUEL)) {
            return recipeType.equals(VanillaRecipeCategoryUid.CRAFTING);
        }
        return false;
    }

}
