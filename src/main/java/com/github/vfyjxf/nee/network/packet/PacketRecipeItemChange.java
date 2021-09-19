package com.github.vfyjxf.nee.network.packet;

import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFake;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author vfyjxf
 */
public class PacketRecipeItemChange implements IMessage, IMessageHandler<PacketRecipeItemChange, IMessage> {

    private NBTTagCompound stack;
    private int slotNumber;

    public NBTTagCompound getStack() {
        return stack;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public PacketRecipeItemChange() {

    }

    public PacketRecipeItemChange(NBTTagCompound stack, int slotNumber) {
        this.stack = stack;
        this.slotNumber = slotNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stack = ByteBufUtils.readTag(buf);
        this.slotNumber = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.stack);
        buf.writeInt(this.slotNumber);
    }

    @Override
    public IMessage onMessage(PacketRecipeItemChange message, MessageContext ctx) {
        Container container = ctx.getServerHandler().player.openContainer;
        if (container instanceof ContainerPatternTerm) {
            Slot currentSlot = container.getSlot(message.getSlotNumber());
            ItemStack nextStack = new ItemStack(message.getStack());
            if (currentSlot instanceof SlotFake && !nextStack.isEmpty()) {
                currentSlot.putStack(nextStack);
            }
        }
        return null;
    }
}
