package com.github.vfyjxf.nee.utils;


import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

public class PatternContainerControl {

    /**
     * Use in server side to store the container's item handler.
     * and we can use item handler to put item into container.
     */
    private static final Map<String, IItemHandler> containerHandlers = new HashMap<>();


    public static void putHandler(String containerId, IItemHandler handler) {
        containerHandlers.put(containerId, handler);
    }

    public static ItemStack insertPattern(String containerId, int slot, ItemStack stack) {
        IItemHandler handler = containerHandlers.get(containerId);
        if (handler != null) {
            return handler.insertItem(slot, stack, false);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack insertPattern(long containerId, int slot, ItemStack stack) {
        return insertPattern(Long.toString(containerId), slot, stack);
    }

    public static void addPattern(String containerId, ItemStack stack) {
        IItemHandler handler = containerHandlers.get(containerId);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.insertItem(i, stack, true).isEmpty()) {
                    handler.insertItem(i, stack, false);
                    break;
                }
            }
        }
    }

    public static void addPattern(long containerId, ItemStack stack) {
        addPattern(Long.toString(containerId), stack);
    }

    public static ItemStack extractPattern(String containerId, int slot) {
        IItemHandler handler = containerHandlers.get(containerId);
        if (handler != null) {
            return handler.extractItem(slot, 1, false);
        }
        return ItemStack.EMPTY;
    }


}
