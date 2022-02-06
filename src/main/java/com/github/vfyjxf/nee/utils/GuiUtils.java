package com.github.vfyjxf.nee.utils;

import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.AppEngSlot;
import appeng.helpers.IContainerCraftingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.IItemHandler;

/**
 * @author vfyjxf
 */
public class GuiUtils {

    public static boolean isCraftingSlot(Slot slot) {
        Container container = Minecraft.getMinecraft().player.openContainer;
        if (container instanceof IContainerCraftingPacket && slot instanceof AppEngSlot) {
            IItemHandler craftMatrix = ((IContainerCraftingPacket) container).getInventoryByName("crafting");
            return ((AppEngSlot) slot).getItemHandler().equals(craftMatrix);
        }
        return false;
    }

    public static boolean isGuiWirelessCrafting(Object gui) {
        try {
            Class<?> wirelessCraftingGui = Class.forName("p455w0rd.wct.client.gui.GuiWCT");
            return wirelessCraftingGui.isInstance(gui);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWirelessCraftingTermContainer(Object container) {
        try {
            Class<?> wirelessCraftingContainer = Class.forName("p455w0rd.wct.container.ContainerWCT");
            return wirelessCraftingContainer.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWirelessGuiCraftConfirm(Object gui) {
        try {
            Class<?> guiCraftConfirm = Class.forName("p455w0rd.wct.client.gui.GuiCraftConfirm");
            return guiCraftConfirm.isInstance(gui);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isContainerWirelessCraftingConfirm(Object container) {
        try {
            Class<?> containerWirelessCraftingConfirm = Class.forName("p455w0rd.wct.container.ContainerCraftConfirm");
            return containerWirelessCraftingConfirm.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWCTContainerCraftingConfirm(Container container) {
        try {
            Class<?> wctContainerCrafting = Class.forName("com.github.vfyjxf.nee.container.WCTContainerCraftingConfirm");
            return wctContainerCrafting.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWirelessTerminalGuiObject(Object guiObj) {
        try {
            Class<?> wirelessTerminalGuiObjClass = Class.forName("p455w0rd.ae2wtlib.api.WTGuiObject");
            return wirelessTerminalGuiObjClass.isInstance(guiObj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isContainerCraftingTerm(Container container) {
        return container instanceof ContainerCraftingTerm || isWirelessCraftingTermContainer(container);
    }

    public static boolean isGuiCraftingTerm(GuiScreen gui) {
        return gui instanceof GuiCraftingTerm || isGuiWirelessCrafting(gui);
    }

    public static boolean isGuiCraftConfirm(GuiScreen gui) {
        return gui instanceof GuiCraftConfirm || isWirelessGuiCraftConfirm(gui);
    }

}
