package com.github.vfyjxf.nee.network.packet;

import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFake;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author vfyjxf
 */
public class PacketStackSizeChange implements IMessage {

    private int slotIndex;
    private int changeCount;

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public PacketStackSizeChange() {

    }

    public PacketStackSizeChange(int slotIndex, int changeCount) {
        this.slotIndex = slotIndex;
        this.changeCount = changeCount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.slotIndex = buf.readInt();
        this.changeCount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slotIndex);
        buf.writeInt(this.changeCount);
    }

    public static class Handler implements IMessageHandler<PacketStackSizeChange, IMessage> {

        @Override
        public IMessage onMessage(PacketStackSizeChange message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            Container container = player.openContainer;
            player.getServerWorld().addScheduledTask(() -> {
                if (container instanceof AEBaseContainer) {

                    if (container instanceof ContainerPatternTerm && ((ContainerPatternTerm) container).isCraftingMode()) {
                        return;
                    }

                    Slot currentSlot = container.getSlot(message.getSlotIndex());
                    if (currentSlot instanceof SlotFake && currentSlot.getHasStack()) {
                        for (int i = 0; i < Math.abs(message.getChangeCount()); i++) {
                            int currentStackSize = message.getChangeCount() > 0 ? currentSlot.getStack().getCount() + 1 : currentSlot.getStack().getCount() - 1;
                            if (currentStackSize <= currentSlot.getStack().getMaxStackSize() && currentStackSize > 0) {
                                ItemStack nextStack = currentSlot.getStack().copy();
                                nextStack.setCount(currentStackSize);
                                currentSlot.putStack(nextStack);
                            } else {
                                break;
                            }
                        }
                    }
                }
            });
            return null;
        }

    }

}
