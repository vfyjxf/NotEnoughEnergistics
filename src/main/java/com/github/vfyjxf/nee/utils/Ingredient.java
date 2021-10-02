package com.github.vfyjxf.nee.utils;

import codechicken.nei.PositionedStack;
import net.minecraft.item.ItemStack;

public class Ingredient {

    private ItemStack craftableIngredient;
    private final int requireCount;
    private int currentCount = 0;

    public Ingredient(PositionedStack ingredients) {
        this.requireCount = ingredients.items[0].stackSize;
    }

    public void setCraftableIngredient(ItemStack craftableIngredient) {
        this.craftableIngredient = craftableIngredient;
    }

    public ItemStack getCraftableIngredient() {
        return craftableIngredient;
    }

    public int getMissingCount(){
        return requireCount - currentCount;
    }

    public boolean requiresToCraft(){
        return this.getMissingCount() > 0;
    }

    public void addCurrentCount(int count){
        currentCount += count;
    }

}
