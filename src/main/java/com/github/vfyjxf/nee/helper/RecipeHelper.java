package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.jei.NEEJeiPlugin;
import com.github.vfyjxf.nee.utils.ItemUtils;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

public class RecipeHelper {

    private RecipeHelper() {

    }

    public static boolean checkCatalysts(ItemStack stack, IRecipeCategory<?> category) {
        if (stack == null || stack.isEmpty() || category == null)
            return false;

        return NEEJeiPlugin.recipeRegistry.getRecipeCatalysts(category).stream()
                .filter(ItemStack.class::isInstance)
                .map(ItemStack.class::cast)
                .anyMatch(catalyst -> ItemUtils.matches(catalyst, stack));
    }

    public static boolean checkCatalysts(ItemStack stack, String categoryUid) {
        if (stack == null || stack.isEmpty() || categoryUid == null || categoryUid.isEmpty())
            return false;
        IRecipeCategory<?> category = NEEJeiPlugin.recipeRegistry.getRecipeCategory(categoryUid);
        return checkCatalysts(stack, category);
    }

}
