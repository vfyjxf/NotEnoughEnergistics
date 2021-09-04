package com.github.vfyjxf.nee.network.packet;

import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author vfyjxf
 */
public class PacketRecipeItemChange implements IMessage, IMessageHandler<PacketRecipeItemChange,IMessage> {

    private NBTTagCompound stack;
    private int slotNumber;

    public PacketRecipeItemChange(){

    }

    public PacketRecipeItemChange(NBTTagCompound stack, int slotNumber) {
        this.stack = stack;
        this.slotNumber = slotNumber;
    }

    public NBTTagCompound getStack() {
        return stack;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stack = ByteBufUtils.readTag(buf);
        this.slotNumber = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf,this.stack);
        buf.writeInt(this.slotNumber);
    }

    @Override
    public IMessage onMessage(PacketRecipeItemChange message, MessageContext ctx) {
        Container container = ctx.getServerHandler().playerEntity.openContainer;
        if(container instanceof ContainerPatternTerm || GuiUtils.isPatternTermExContainer(container)){
            Slot currentSlot = container.getSlot(message.getSlotNumber());
            ItemStack nextStack = ItemStack.loadItemStackFromNBT(message.getStack());
            if(nextStack != null){
                currentSlot.putStack(nextStack);
            }
        }
        return null;
    }
}
