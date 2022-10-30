package com.github.vfyjxf.nee.network.packet;

import appeng.container.AEBaseContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vfyjxf
 */
public class PacketSlotStackSwitch implements IMessage {

    private ItemStack stack;
    private List<Integer> slots;

    public PacketSlotStackSwitch() {

    }

    public ItemStack getStack() {
        return stack;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public PacketSlotStackSwitch(ItemStack stack, List<Integer> craftingSlots) {
        this.stack = stack;
        this.slots = craftingSlots;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stack = ByteBufUtils.readItemStack(buf);
        int craftingSlotsSize = buf.readInt();
        this.slots = new ArrayList<>(craftingSlotsSize);
        for (int i = 0; i < craftingSlotsSize; i++) {
            int slotNumber = buf.readInt();
            this.slots.add(slotNumber);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.stack);
        buf.writeInt(this.slots.size());
        for (Integer craftingSlot : this.slots) {
            buf.writeInt(craftingSlot);
        }
    }

    public static class Handler implements IMessageHandler<PacketSlotStackSwitch, IMessage> {

        @Override
        public IMessage onMessage(PacketSlotStackSwitch message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            Container container = player.openContainer;
            player.getServerWorld().addScheduledTask(() -> {
                if (container instanceof AEBaseContainer) {
                    ItemStack nextStack = message.getStack();
                    if (nextStack != null) {
                        for (Integer craftingSlot : message.getSlots()) {
                            Slot currentSlot = container.getSlot(craftingSlot);
                            ItemStack next = nextStack.copy();
                            next.setCount(currentSlot.getStack().getCount());
                            currentSlot.putStack(next);
                        }
                    }
                }
            });
            return null;
        }

    }

}
