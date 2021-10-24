package com.github.vfyjxf.nee.utils;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFakeCraftingMatrix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
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

    public static boolean isCraftingSlot(Slot slot) {
        Container container = Minecraft.getMinecraft().player.openContainer;
        if (container instanceof ContainerPatternTerm) {
            SlotFakeCraftingMatrix[] craftingSlots = null;
            try {
                Field craftingSlotsField = ContainerPatternTerm.class.getDeclaredField("craftingSlots");
                craftingSlotsField.setAccessible(true);
                craftingSlots = (SlotFakeCraftingMatrix[]) craftingSlotsField.get(container);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (craftingSlots != null) {
                for (Slot currentSlot : craftingSlots) {
                    if (currentSlot.equals(slot)) {
                        return true;
                    }
                }
            }
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
