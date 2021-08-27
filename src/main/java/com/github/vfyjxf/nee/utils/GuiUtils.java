package com.github.vfyjxf.nee.utils;

import appeng.client.gui.AEBaseGui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vfyjxf
 */
public class GuiUtils {
    public static Slot getSlotUnderMouse(GuiContainer container, int mouseX, int mouseY) {
        Method getSlotMethod = ObfuscationReflectionHelper.findMethod(AEBaseGui.class, "getSlot", Slot.class, int.class, int.class);
        try {
            return (Slot) getSlotMethod.invoke(container, mouseX, mouseY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
