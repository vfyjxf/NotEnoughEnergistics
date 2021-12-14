package com.github.vfyjxf.nee.network.packets;

import appeng.container.AEBaseContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSlotStackChange {

    private ItemStack stack;
    private List<Integer> slots;

    public PacketSlotStackChange() {

    }

    public ItemStack getStack() {
        return stack;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public PacketSlotStackChange(ItemStack stack, List<Integer> craftingSlots) {
        this.stack = stack;
        this.slots = craftingSlots;
    }


    public void encode(PacketBuffer buffer) {
        buffer.writeItemStack(this.stack, false);
        buffer.writeInt(this.slots.size());
        for (Integer craftingSlot : this.slots) {
            buffer.writeInt(craftingSlot);
        }
    }

    public static PacketSlotStackChange decode(PacketBuffer buffer) {
        PacketSlotStackChange packet = new PacketSlotStackChange();
        packet.stack = buffer.readItem();
        int craftingSlotsSize = buffer.readInt();
        packet.slots = new ArrayList<>(craftingSlotsSize);
        for (int i = 0; i < craftingSlotsSize; i++) {
            int slotNumber = buffer.readInt();
            packet.slots.add(slotNumber);
        }
        return packet;
    }

    public static void handle(PacketSlotStackChange packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof AEBaseContainer) {
            ctx.get().enqueueWork(() -> {
                Container container = player.containerMenu;
                ItemStack nextStack = packet.getStack();
                if (nextStack != null) {
                    for (Integer craftingSlot : packet.getSlots()) {
                        Slot currentSlot = container.getSlot(craftingSlot);
                        currentSlot.set(nextStack);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

    }
}
