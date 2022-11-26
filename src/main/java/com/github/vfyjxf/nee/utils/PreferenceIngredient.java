package com.github.vfyjxf.nee.utils;

import com.github.vfyjxf.nee.integration.IToolHelper;
import com.github.vfyjxf.nee.integration.RecipeToolManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Objects;

public class PreferenceIngredient {

    private final ItemStack identifier;
    private final boolean isTool;
    @Nullable
    private final String acceptedType;

    public PreferenceIngredient(ItemStack identifier, boolean isTool, @Nullable String acceptedType) {
        this.identifier = identifier;
        this.isTool = isTool;
        this.acceptedType = acceptedType;
    }

    public ItemStack create(int count) {
        if (isTool) {
            IToolHelper helper = RecipeToolManager.INSTANCE.getToolHelper(identifier.getItem());
            if (helper != null) {
                ItemStack toolStack = helper.getToolStack(identifier);
                toolStack.setCount(1);
                return toolStack;
            }
        } else {
            ItemStack stack = identifier.copy();
            stack.setCount(count);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public boolean matches(ItemStack stack, String recipeType) {
        if (acceptedType == null || recipeType == null || acceptedType.equals(recipeType)) {
            return ItemUtils.contains(identifier, stack);
        } else {
            return false;
        }
    }

    public ItemStack getIdentifier() {
        return identifier;
    }

    public boolean isTool() {
        return isTool;
    }

    @Nullable
    public String getAcceptedType() {
        return acceptedType;
    }

    public NBTTagCompound toTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("identifier", identifier.writeToNBT(new NBTTagCompound()));
        tag.setBoolean("isTool", isTool);
        if (acceptedType != null) {
            tag.setString("acceptedType", acceptedType);
        }
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreferenceIngredient that = (PreferenceIngredient) o;
        return isTool == that.isTool && identifier.equals(that.identifier) && Objects.equals(acceptedType, that.acceptedType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, isTool, acceptedType);
    }
}
