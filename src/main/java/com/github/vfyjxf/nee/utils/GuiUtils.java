package com.github.vfyjxf.nee.utils;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.helpers.IContainerCraftingPacket;
import codechicken.nei.PositionedStack;
import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeItemChange;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        if (container instanceof ContainerPatternTerm || GuiUtils.isPatternTermExContainer(container)) {
            IContainerCraftingPacket cct = (IContainerCraftingPacket) container;
            IInventory craftMatrix = cct.getInventoryByName("crafting");
            return slot.inventory.equals(craftMatrix);
        }
        return false;
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

    public static void handleRecipeIngredientChange(Slot currentSlot, int dWheel) {

        PositionedStack currentIngredients = NEECraftingHandler.ingredients.get("input" + currentSlot.getSlotIndex());
        if (currentIngredients != null && currentIngredients.items.length > 1) {
            int currentStackIndex = ItemUtils.getIngredientIndex(currentSlot.getStack(), currentIngredients);
            if (currentStackIndex >= 0) {
                int nextStackIndex = dWheel / 120;
                for (int i = 0; i < Math.abs(nextStackIndex); i++) {
                    currentStackIndex = nextStackIndex > 0 ? currentStackIndex + 1 : currentStackIndex - 1;
                    if (currentStackIndex >= currentIngredients.items.length) {
                        currentStackIndex = 0;
                    } else if (currentStackIndex < 0) {
                        currentStackIndex = currentIngredients.items.length - 1;
                    }
                    ItemStack currentStack = currentIngredients.items[currentStackIndex].copy();
                    currentStack.stackSize = currentSlot.getStack().stackSize;
                    NEENetworkHandler.getInstance().sendToServer(new PacketRecipeItemChange(currentStack.writeToNBT(new NBTTagCompound()), currentSlot.slotNumber));
                }
            }
        }

    }

}
