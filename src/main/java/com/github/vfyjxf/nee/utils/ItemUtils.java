package com.github.vfyjxf.nee.utils;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.me.ItemRepo;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue;

/**
 * @author vfyjxf
 */
public final class ItemUtils {

    public static boolean matches(ItemStack stack1, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack1, stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    public static boolean contains(ItemStack based, ItemStack stack) {
        if (ItemStack.areItemsEqual(based, stack)) {
            if (based.getTagCompound() == null && stack.getTagCompound() == null) {
                return based.areCapsCompatible(stack);
            } else if (based.getTagCompound() == null) {
                return based.areCapsCompatible(stack);
            } else if (stack.getTagCompound() != null) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                return based.areCapsCompatible(stack) &&
                        based.getTagCompound()
                                .getKeySet()
                                .stream()
                                .allMatch(keyName -> {
                                            if (!tagCompound.hasKey(keyName)) {
                                                return false;
                                            } else {
                                                return tagCompound.getTag(keyName).equals(based.getTagCompound().getTag(keyName));
                                            }
                                        }
                                );
            }
        }
        return false;
    }

    public static ItemStack getFirstStack(IGuiIngredient<ItemStack> ingredient) {
        if (ingredient.getAllIngredients().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ingredient.getAllIngredients()
                .stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getFirstStack(List<ItemStack> ingredients) {
        if (ingredients.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ingredients
                .stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    public static List<IAEItemStack> getStorage(ItemRepo repo) {
        if (repo == null) return Collections.emptyList();
        IItemList<IAEItemStack> all = getPrivateValue(ItemRepo.class, repo, "list");
        if (all == null) return Collections.emptyList();
        else return StreamSupport.stream(all.spliterator(), false).collect(Collectors.toList());
    }

}
