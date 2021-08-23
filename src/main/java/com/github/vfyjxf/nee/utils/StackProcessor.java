package com.github.vfyjxf.nee.utils;

import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;

import java.io.Serializable;

public class StackProcessor implements Serializable {
    public String itemName;
    public String meta;
    public String nbt;
    public String recipeType;
    public ItemStack itemStack;
    public int stackSize;
    public IGuiIngredient<ItemStack> ingredient;

    public StackProcessor(String itemName, String meta, String nbt, String recipeType) {
        this.itemName = itemName;
        this.meta = meta;
        this.nbt = nbt;
        this.recipeType = recipeType;
    }

    public StackProcessor(String itemName, String meta) {
        this.itemName = itemName;
        this.meta = meta;
    }

    public StackProcessor(ItemStack itemStack, String recipeType){
        this.itemStack = itemStack;
        this.recipeType = recipeType;
    }


    public StackProcessor(String itemName) {
        this.itemName = itemName;
    }
    public StackProcessor(IGuiIngredient<ItemStack> ingredient, int stackSize){
        this.ingredient = ingredient;
        this.stackSize = stackSize;
    }
    public StackProcessor(IGuiIngredient<ItemStack> ingredient){
        this.ingredient = ingredient;
    }

}
