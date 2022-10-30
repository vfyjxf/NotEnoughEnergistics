package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.utils.ItemUtils;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IngredientMerger {

    private IngredientMerger() {

    }

    /**
     * @param allStacks A List of elements that need to be merged after copying.
     * @return A merged list
     */
    public static List<ItemStack> merge(List<ItemStack> allStacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack current : allStacks) {
            boolean mergedFlag = merged.stream().anyMatch(stack -> mergeStack(stack, current));
            if (!mergedFlag) merged.add(current);
        }
        return merged;
    }

    /**
     * The unrestricted count form of {@link IngredientMerger#merge(List)}.
     *
     * @param allStacks A List of elements that need to be merged after copying.
     * @return A merged list
     */
    public static List<ItemStack> unlimitedMerge(List<ItemStack> allStacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack current : allStacks) {
            boolean mergedFlag = merged.stream().anyMatch(stack -> unlimitedMergeStack(stack, current));
            if (!mergedFlag) merged.add(current);
        }
        return merged;
    }

    public static boolean mergeStack(ItemStack main, ItemStack other) {
        if (ItemUtils.matches(main, other) && main.getCount() + other.getCount() <= main.getMaxStackSize()) {
            main.setCount(main.getCount() + other.getCount());
            return true;
        }
        return false;
    }

    public static boolean unlimitedMergeStack(ItemStack main, ItemStack other) {
        if (ItemUtils.matches(main, other)) {
            main.setCount(main.getCount() + other.getCount());
            return true;
        }
        return false;
    }


}
