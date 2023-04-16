package com.github.vfyjxf.nee.integration;

import appeng.api.networking.IGridHost;
import appeng.helpers.DualityInterface;
import com.github.vfyjxf.nee.utils.ReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * All block like ME interface.
 */
public interface IPatternProvider<T extends IGridHost> {

    List<IPatternProvider<?>> PROVIDERS = new ArrayList<>();

    @Nonnull
    Class<? extends IGridHost> getHostClass();

    IItemHandler getPatterns(T tileEntity);

    default ItemStack getTarget(T tileEntity){
        return ItemStack.EMPTY;
    }

    String getName(T tileEntity);

    String uid(T tileEntity);

    static boolean sameGird(DualityInterface dual1, DualityInterface dual2){
        try {
            return (boolean) ReflectionHelper.getMethod(DualityInterface.class, "sameGrid", DualityInterface.class).invoke(dual1, dual2);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

}
