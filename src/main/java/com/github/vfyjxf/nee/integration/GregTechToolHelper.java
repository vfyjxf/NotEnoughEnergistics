package com.github.vfyjxf.nee.integration;

import net.minecraft.item.ItemStack;

public class GregTechToolHelper implements IToolHelper{
    @Override
    public boolean isSupport(Class<?> toolClass) {
        return false;
    }

    @Override
    public ItemStack getToolStack(ItemStack stack) {
        return stack;
    }
}
