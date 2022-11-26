package com.github.vfyjxf.nee.integration;

import net.minecraft.item.ItemStack;

public interface IToolHelper {

    boolean isSupport(Class<?> toolClass);

    /**
     * @return A generic tool used in the pattern for recipes based on the OreDictionary.
     */
    default ItemStack getToolStack(ItemStack stack) {
        return stack;
    }

}
