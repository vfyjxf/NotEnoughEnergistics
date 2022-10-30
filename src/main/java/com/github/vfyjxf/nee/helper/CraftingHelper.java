package com.github.vfyjxf.nee.helper;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.AppEngSlot;
import appeng.helpers.IContainerCraftingPacket;
import com.github.vfyjxf.nee.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CraftingHelper {

    private CraftingHelper() {

    }

    public static boolean isSupportedGui(GuiScreen screen) {
        return screen instanceof GuiCraftingTerm || screen instanceof GuiPatternTerm || GuiUtils.isGuiWirelessCrafting(screen);
    }

    public static List<Slot> getCraftingSlots(AEBaseGui gui) {
        Container container = gui.inventorySlots;
        if (container instanceof IContainerCraftingPacket) {
            IItemHandler craftMatrix = ((IContainerCraftingPacket) container).getInventoryByName("crafting");
            return container.inventorySlots.stream()
                    .filter(slot -> slot instanceof AppEngSlot && ((AppEngSlot) slot).getItemHandler().equals(craftMatrix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Nullable
    public static IItemHandler getCraftMatrix(AEBaseGui gui) {
        Container container = gui.inventorySlots;
        if (container instanceof IContainerCraftingPacket) {
            return ((IContainerCraftingPacket) container).getInventoryByName("crafting");
        }
        return null;
    }

    public static List<ItemStack> copyAllNonEmpty(@Nonnull IItemHandler handler) {
        return IntStream.range(0, handler.getSlots())
                .filter(slotIndex -> !handler.getStackInSlot(slotIndex).isEmpty())
                .mapToObj(slotIndex -> handler.getStackInSlot(slotIndex).copy())
                .collect(Collectors.toList());
    }

    public static List<ItemStack> copyAllNonEmpty(@Nonnull IInventory inventory) {
        return IntStream.range(0, inventory.getSizeInventory())
                .filter(slotIndex -> !inventory.getStackInSlot(slotIndex).isEmpty())
                .mapToObj(slotIndex -> inventory.getStackInSlot(slotIndex).copy())
                .collect(Collectors.toList());
    }

    public static boolean isCraftingSlot(Slot slot) {
        Container container = Minecraft.getMinecraft().player.openContainer;
        if (container instanceof IContainerCraftingPacket && slot instanceof AppEngSlot) {
            IItemHandler craftMatrix = ((IContainerCraftingPacket) container).getInventoryByName("crafting");
            return ((AppEngSlot) slot).getItemHandler().equals(craftMatrix);
        }
        return false;
    }

}
