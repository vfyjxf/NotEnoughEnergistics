package com.github.vfyjxf.nee.utils;

import appeng.container.slot.AppEngSlot;
import appeng.helpers.IContainerCraftingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vfyjxf
 */
public class GuiUtils {
    public static Slot getSlotUnderMouse(GuiContainer container, int mouseX, int mouseY) {
        Method isPointInRegion = ObfuscationReflectionHelper.findMethod(GuiContainer.class,
                "func_146978_c", boolean.class,
                int.class, int.class, int.class, int.class, int.class, int.class);
        for (Slot slot : container.inventorySlots.inventorySlots) {
            try {
                if ((boolean) isPointInRegion.invoke(container, slot.xPos, slot.yPos, 16, 16, mouseX, mouseY)) {
                    return slot;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isCraftingSlot(Slot slot) {
        Container container = Minecraft.getMinecraft().player.openContainer;
        if (container instanceof IContainerCraftingPacket && slot instanceof AppEngSlot) {
            IItemHandler craftMatrix = ((IContainerCraftingPacket) container).getInventoryByName("crafting");
            return ((AppEngSlot) slot).getItemHandler().equals(craftMatrix);
        }
        return false;
    }

    public static boolean isGuiWirelessCrafting(GuiScreen gui) {
        try {
            Class<?> wirelessCraftingGui = Class.forName("p455w0rd.wct.client.gui.GuiWCT");
            return wirelessCraftingGui.isInstance(gui);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWirelessCraftingTermContainer(Container container) {
        try {
            Class<?> wirelessCraftingContainer = Class.forName("p455w0rd.wct.container.ContainerWCT");
            return wirelessCraftingContainer.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWirelessGuiCraftConfirm(GuiScreen gui) {
        try {
            Class<?> guiCraftConfirm = Class.forName("p455w0rd.wct.client.gui.GuiCraftConfirm");
            return guiCraftConfirm.isInstance(gui);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isContainerWirelessCraftingConfirm(Container container) {
        try {
            Class<?> containerWirelessCraftingConfirm = Class.forName("p455w0rd.wct.container.ContainerCraftConfirm");
            return containerWirelessCraftingConfirm.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
