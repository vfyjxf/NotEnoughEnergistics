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
        this.requireCount = ingredient.getAllIngredients()
                .stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .findFirst()
                .map(ItemStack::getCount)
                .orElse(1);
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

    public boolean requiresToCraft() {
        return this.getMissingCount() > 0;
    }

    public boolean isCraftable() {
        return !this.craftableIngredient.isEmpty();
    }

    public void addCount(long count) {
        if (currentCount + count < requireCount) {
            currentCount += count;
        } else {
            currentCount = requireCount;
        }
    }

    public void setCraftableIngredient(ItemStack craftableIngredient) {
        this.craftableIngredient = craftableIngredient;
    }

}
