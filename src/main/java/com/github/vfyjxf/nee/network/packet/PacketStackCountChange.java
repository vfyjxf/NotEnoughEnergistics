package com.github.vfyjxf.nee.network.packet;

import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

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
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container container = player.openContainer;
        if ((container instanceof ContainerPatternTerm && !((ContainerPatternTerm) container).isCraftingMode()) || GuiUtils.isPatternTermExContainer(container)) {
            Slot currentSlot = container.getSlot(message.getSlotIndex());
            for (int i = 0; i < Math.abs(message.getChangeCount()); i++) {
                int currentStackSize = message.getChangeCount() > 0 ? currentSlot.getStack().stackSize + 1 : currentSlot.getStack().stackSize - 1;
                if (currentStackSize <= currentSlot.getStack().getMaxStackSize() && currentStackSize > 0) {
                    currentSlot.getStack().stackSize = currentStackSize;
                } else {
                    break;
                }
            }
        }
        return null;
    }
}
