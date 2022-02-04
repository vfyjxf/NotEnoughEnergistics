package com.github.vfyjxf.nee.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;

public class InventoryPatternCrafter extends ItemStackHandler implements Iterable<ItemStack> {


    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        super.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.unmodifiableList(super.stacks).iterator();
    }
}
