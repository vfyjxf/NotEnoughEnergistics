package com.github.vfyjxf.nee.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Objects;

public class BlackIngredient {

    private final ItemStack identifier;
    @Nullable
    private final String acceptedType;

    public BlackIngredient(ItemStack identifier, @Nullable String acceptedType) {
        this.identifier = identifier;
        this.acceptedType = acceptedType;
    }


    public ItemStack getIdentifier() {
        return identifier;
    }

    @Nullable
    public String getAcceptedType() {
        return acceptedType;
    }

    public boolean matches(ItemStack stack, String recipeType) {
        if (acceptedType == null || recipeType == null || acceptedType.equals(recipeType)) {
            return ItemUtils.contains(identifier, stack);
        } else {
            return false;
        }
    }

    public NBTTagCompound toTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("identifier", identifier.writeToNBT(new NBTTagCompound()));
        if (acceptedType != null) {
            tag.setString("acceptedType", acceptedType);
        }
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlackIngredient that = (BlackIngredient) o;
        return identifier.equals(that.identifier) && Objects.equals(acceptedType, that.acceptedType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, acceptedType);
    }
}
