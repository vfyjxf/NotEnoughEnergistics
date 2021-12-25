package com.github.vfyjxf.nee.utils;

import codechicken.nei.PositionedStack;
import net.minecraft.item.ItemStack;

public class Ingredient {

    private final long requireCount;
    private final PositionedStack ingredients;
    private ItemStack craftableIngredient;
    private long currentCount = 0;

    public Ingredient(PositionedStack ingredients) {
        this.ingredients = ingredients;
        this.requireCount = ingredients.items[0].stackSize;
    }

    public PositionedStack getIngredients() {
        return ingredients;
    }

    public ItemStack getCraftableIngredient() {
        return craftableIngredient;
    }

    public void setCraftableIngredient(ItemStack craftableIngredient) {
        this.craftableIngredient = craftableIngredient;
    }

    public long getMissingCount() {
        return requireCount - currentCount;
    }

    public long getRequireCount() {
        return requireCount;
    }

    public boolean isCraftable() {
        return this.craftableIngredient != null;
    }

    public boolean requiresToCraft() {
        return this.getMissingCount() > 0;
    }

    public void addCurrentCount(long count) {
        currentCount += count;
    }

}
