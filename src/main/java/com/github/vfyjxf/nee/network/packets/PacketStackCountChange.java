package com.github.vfyjxf.nee.network.packets;

import appeng.container.AEBaseContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.slot.FakeSlot;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketStackCountChange {
    private int slotIndex;
    private int changeCount;

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public PacketStackCountChange() {

    }

    public PacketStackCountChange(int slotIndex, int changeCount) {
        this.slotIndex = slotIndex;
        this.changeCount = changeCount;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.slotIndex);
        buffer.writeInt(this.changeCount);
    }

    public static PacketStackCountChange decode(PacketBuffer buffer) {
        PacketStackCountChange packet = new PacketStackCountChange();
        packet.slotIndex = buffer.readInt();
        packet.changeCount = buffer.readInt();
        return packet;
    }

    public static void handle(PacketStackCountChange packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof AEBaseContainer) {
            ctx.get().enqueueWork(() -> {
                Container container = player.containerMenu;
                if (container instanceof PatternTermContainer && ((PatternTermContainer) container).isCraftingMode()) {
                    return;
                }
                Slot currentSlot = container.getSlot(packet.getSlotIndex());
                if (currentSlot instanceof FakeSlot && currentSlot.hasItem()) {
                    for (int i = 0; i < Math.abs(packet.getChangeCount()); i++) {
                        int currentStackSize = packet.getChangeCount() > 0 ? currentSlot.getItem().getCount() + 1 : currentSlot.getItem().getCount() - 1;
                        if (currentStackSize <= currentSlot.getItem().getMaxStackSize() && currentStackSize > 0) {
                            ItemStack nextStack = currentSlot.getItem().copy();
                            nextStack.setCount(currentStackSize);
                            currentSlot.set(nextStack);
                        } else {
                            break;
                        }
                    }
                }
            });

            ctx.get().setPacketHandled(true);

        }
    }
}
