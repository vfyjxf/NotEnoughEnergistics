package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.config.IngredientBlackList;
import net.minecraft.item.ItemStack;

public class BlackListHelper {

    private BlackListHelper() {

    }

    public static boolean isBlacklistItem(ItemStack stack, String recipeType) {
        return IngredientBlackList.INSTANCE.getBlackList()
                .stream()
                .anyMatch(blackIngredient -> blackIngredient.matches(stack, recipeType));
    }

    public static boolean isBlacklistItem(ItemStack stack) {
        return isBlacklistItem(stack, null);
    }

}
