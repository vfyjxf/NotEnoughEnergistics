package com.github.vfyjxf.nee.utils;

import appeng.client.gui.AEBaseGui;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vfyjxf
 */
public class GuiUtils {
    public static Slot getSlotUnderMouse(GuiContainer guiContainer, int mouseX, int mouseY) {
        Method getSlotMethod = ReflectionHelper.findMethod(AEBaseGui.class, (AEBaseGui) guiContainer, new String[]{"getSlot"}, int.class, int.class);
        try {
            return (Slot) getSlotMethod.invoke(guiContainer, mouseX, mouseY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isPatternTermExGui(GuiScreen container) {
        try {
            Class<?> patternTermExClz = Class.forName("appeng.client.gui.implementations.GuiPatternTermEx");
            return patternTermExClz.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isPatternTermExContainer(Container container){
        try {
            Class<?> patternTermExClz = Class.forName("appeng.container.implementations.ContainerPatternTermEx");
            return patternTermExClz.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
