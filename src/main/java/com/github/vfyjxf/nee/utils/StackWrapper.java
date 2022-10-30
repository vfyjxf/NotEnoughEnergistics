package com.github.vfyjxf.nee.utils;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StackWrapper {

    @Nonnull
    private final ItemStack stack;
    private final List<ItemStack> ingredients;
    private int count;

    public StackWrapper(@Nonnull ItemStack stack, List<ItemStack> ingredients) {
        this.stack = stack;
        this.ingredients = new ArrayList<>(ingredients);
        this.count = stack.getCount();
    }

    public boolean merge(ItemStack other) {
        if (ItemUtils.matches(stack, other) && count + other.getCount() <= stack.getMaxStackSize()) {
            count += other.getCount();
            return true;
        }
        return false;
    }

    public boolean merge(StackWrapper other) {
        return merge(other.stack);
    }

    public void addCount(int count) {
        this.count += count;
    }

    @Nonnull
    public ItemStack getStack() {
        return stack;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }

    public int getCount() {
        return count;
    }
}
