package com.github.vfyjxf.nee.utils;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.container.slot.AppEngSlot;
import appeng.helpers.IContainerCraftingPacket;
import com.github.vfyjxf.nee.helper.PlatformHelper;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * @author vfyjxf
 */
public class GuiUtils {

    private GuiUtils(){

    }

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

    public static boolean isWirelessTerminalGuiObject(Object guiObj) {
        try {
            Class<?> wirelessTerminalGuiObjClass = Class.forName("p455w0rd.ae2wtlib.api.WTGuiObject");
            return wirelessTerminalGuiObjClass.isInstance(guiObj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isCraftingTerm(GuiScreen gui) {
        return gui instanceof GuiCraftingTerm || isGuiWirelessCrafting(gui) || PlatformHelper.isWirelessGui(gui);
    }

    @Nullable
    public static GuiScreen getParentScreen() {
        if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
            return ((RecipesGui) Minecraft.getMinecraft().currentScreen).getParentScreen();
        } else {
            return null;
        }
    }

}
