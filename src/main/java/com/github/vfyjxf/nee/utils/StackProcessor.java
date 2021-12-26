package com.github.vfyjxf.nee.utils;

import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;

import java.io.Serializable;

public class StackProcessor implements Serializable {
    private String itemName;
    private String meta;
    private String nbt;
    private String recipeType;
    private ItemStack currentStack;
    private int stackSize;
    private IGuiIngredient<ItemStack> ingredient;

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

    public StackProcessor(ItemStack currentStack, String recipeType) {
        this.currentStack = currentStack;
        this.recipeType = recipeType;
    }

    public StackProcessor(String itemName) {
        this.itemName = itemName;
    }

    public StackProcessor(IGuiIngredient<ItemStack> ingredient, int stackSize) {
        this.ingredient = ingredient;
        this.stackSize = stackSize;
    }

    public StackProcessor(IGuiIngredient<ItemStack> ingredient, ItemStack currentStack, int stackSize) {
        this.ingredient = ingredient;
        this.stackSize = stackSize;
        this.currentStack = currentStack;
    }

    public StackProcessor(IGuiIngredient<ItemStack> ingredient) {
        this.ingredient = ingredient;
    }

    public String getItemName() {
        return itemName;
    }

    public String getMeta() {
        return meta;
    }

    public String getNbt() {
        return nbt;
    }

    public String getRecipeType() {
        return recipeType;
    }

    public ItemStack getCurrentStack() {
        return currentStack;
    }

    public int getStackSize() {
        return stackSize;
    }

    public IGuiIngredient<ItemStack> getIngredient() {
        return ingredient;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }
}
