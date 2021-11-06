package com.github.vfyjxf.nee.utils;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.helpers.IContainerCraftingPacket;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vfyjxf
 */
public class GuiUtils {

    public static Slot getSlotUnderMouse(GuiContainer guiContainer, int mouseX, int mouseY) {

        if (guiContainer instanceof AEBaseGui) {
            Method getSlotMethod = ReflectionHelper.findMethod(AEBaseGui.class, (AEBaseGui) guiContainer, new String[]{"getSlot"}, int.class, int.class);
            try {
                return (Slot) getSlotMethod.invoke(guiContainer, mouseX, mouseY);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
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

    public static boolean isPatternTermExContainer(Container container) {
        try {
            Class<?> patternTermExClz = Class.forName("appeng.container.implementations.ContainerPatternTermEx");
            return patternTermExClz.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isCraftingSlot(Slot slot) {
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        if (slot != null) {
            if (container instanceof ContainerPatternTerm || GuiUtils.isPatternTermExContainer(container)) {
                IContainerCraftingPacket cct = (IContainerCraftingPacket) container;
                IInventory craftMatrix = cct.getInventoryByName("crafting");
                return craftMatrix.equals(slot.inventory);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isMouseOverButton(GuiButton button, int mouseX, int mouseY) {
        return mouseX >= button.xPosition &&
                mouseY >= button.yPosition &&
                mouseX < button.xPosition + button.width &&
                mouseY < button.yPosition + button.height;
    }

    public static boolean isGuiWirelessCrafting(GuiScreen gui) {

        try {
            Class<?> guiWirelessCraftingClass = Class.forName("net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal");
            return guiWirelessCraftingClass.isInstance(gui);
        } catch (ClassNotFoundException e) {
            return false;
        }

    }

    public static boolean isWirelessCraftingTermContainer(Container container) {
        try {
            Class<?> wirelessCraftingTermContainerClass = Class.forName("net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal");
            return wirelessCraftingTermContainerClass.isInstance(container);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isWirelessGuiCraftConfirm(GuiScreen gui) {
        try {
            Class<?> wirelessGuiCraftConfirmClass = Class.forName("net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm");
            return wirelessGuiCraftConfirmClass.isInstance(gui);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
