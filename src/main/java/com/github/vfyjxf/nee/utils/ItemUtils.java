package com.github.vfyjxf.nee.utils;

import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * @author vfyjxf
 */
public final class ItemUtils {

    public static int getIngredientIndex(ItemStack stack, List<ItemStack> currentIngredients) {
        for (int i = 0; i < currentIngredients.size(); i++) {

            if (currentIngredients.get(i) == null) {
                continue;
            }

            if (ItemUtils.matches(stack, currentIngredients.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static boolean matches(ItemStack stack1, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack1, stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    public static ItemStack getFirstStack(IGuiIngredient<ItemStack> ingredient) {
        if (ingredient.getAllIngredients().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ingredient.getAllIngredients()
                .stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getFirstStack(List<ItemStack> ingredients) {
        if (ingredients.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ingredients
                .stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

}
