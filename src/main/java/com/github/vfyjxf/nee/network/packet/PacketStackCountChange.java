package com.github.vfyjxf.nee.network.packet;

import appeng.container.implementations.ContainerPatternTerm;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author vfyjxf
 */
public class PacketStackCountChange implements IMessage, IMessageHandler<PacketStackCountChange, IMessage> {

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

    @Override
    public IMessage onMessage(PacketStackCountChange message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        player.getServerWorld().addScheduledTask(() -> {
            if (container instanceof ContainerPatternTerm && !((ContainerPatternTerm) container).isCraftingMode()) {
                Slot currentSlot = container.getSlot(message.slotIndex);
                for (int i = 0; i < Math.abs(message.changeCount); i++) {
                    if (message.changeCount > 0) {
                        if (currentSlot.getStack().getCount() == currentSlot.getStack().getMaxStackSize()) {
                            break;
                        }
                        currentSlot.getStack().setCount(currentSlot.getStack().getCount() + 1);
                    } else {
                        if (currentSlot.getStack().getCount() - 1 == 0) {
                            break;
                        }
                        currentSlot.getStack().setCount(currentSlot.getStack().getCount() - 1);
                    }

                }
            }
        });
        return null;
    }
}
