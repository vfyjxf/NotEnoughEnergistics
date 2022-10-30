package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.utils.ItemUtils;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A list for storing player-preferred Ingredient
 */
public class PreferenceHelper {

    private PreferenceHelper() {

    }

    public static ItemStack getFromPreference(List<ItemStack> ingredients, ItemStack origin) {

        ItemStack preferItem = ingredients.stream()
                .filter(PreferenceHelper::isPreferItem)
                .findAny()
                .orElse(ItemStack.EMPTY);

        if (!preferItem.isEmpty()) {
            return preferItem;
        }

        return ingredients.stream()
                .filter(PreferenceHelper::isPreferMod)
                .findAny()
                .map(is -> {
                    ItemStack stack = is.copy();
                    stack.setCount(origin.getCount());
                    return stack;
                })
                .orElse(origin);
    }

    public static boolean isPreferMod(@Nonnull ItemStack stack) {
        return NEEConfig.getPriorityMods().contains(stack.getItem().getRegistryName().getNamespace());
    }

    public static boolean isPreferItem(@Nonnull ItemStack stack) {
        return NEEConfig.getPreferenceList()
                .stream()
                .anyMatch(is -> ItemUtils.matches(is, stack));
    }


}
