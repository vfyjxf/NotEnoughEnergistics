package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.config.PreferenceList;
import com.github.vfyjxf.nee.utils.PreferenceIngredient;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A list for storing player-preferred Ingredient
 */
public class PreferenceHelper {

    private PreferenceHelper() {

    }

    public static ItemStack getFromPreference(List<ItemStack> ingredients, ItemStack origin, String recipeType) {

        PreferenceIngredient preference = PreferenceList.INSTANCE.getPreferenceList().stream()
                .filter(preferenceIngredient -> ingredients.stream()
                        .anyMatch(itemStack -> preferenceIngredient.matches(itemStack, recipeType))
                ).findAny()
                .orElse(null);


        ItemStack preferItem = preference != null ? preference.getIdentifier() : ItemStack.EMPTY;

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
        return PreferenceList.INSTANCE.getPreferenceList()
                .stream()
                .anyMatch(preference -> preference.matches(stack, null));
    }

    public static PreferenceIngredient getPreferIngredient(ItemStack stack, String recipeType) {
        return PreferenceList.INSTANCE.getPreferenceList()
                .stream()
                .filter(preference -> preference.matches(stack, recipeType))
                .findAny()
                .orElse(null);
    }

}
