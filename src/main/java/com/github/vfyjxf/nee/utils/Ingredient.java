package com.github.vfyjxf.nee.utils;

import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;

public class Ingredient {

    private final IGuiIngredient<ItemStack> ingredient;
    private ItemStack craftableIngredient = ItemStack.EMPTY;
    private long requireCount;
    private long currentCount = 0;


    public Ingredient(IGuiIngredient<ItemStack> ingredient) {
        this.ingredient = ingredient;
        this.requireCount = ingredient.getAllIngredients().get(0).getCount();
    }

    public ItemStack getCraftableIngredient() {
        return craftableIngredient;
    }

    public IGuiIngredient<ItemStack> getIngredient() {
        return ingredient;
    }

    public long getMissingCount() {
        return requireCount - currentCount;
    }

    public long getRequireCount() {
        return requireCount;
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public boolean requiresToCraft() {
        return this.getMissingCount() > 0;
    }

    public boolean isCraftable() {
        return !this.craftableIngredient.isEmpty();
    }

    public void addCount(long count) {
        this.currentCount += count;
    }

    public void setRequireCount(long requireCount) {
        this.requireCount = requireCount;
    }

    public void setCurrentCount(long currentCount) {
        this.currentCount = currentCount;
    }

    public void setCraftableIngredient(ItemStack craftableIngredient) {
        this.craftableIngredient = craftableIngredient;
    }

}
