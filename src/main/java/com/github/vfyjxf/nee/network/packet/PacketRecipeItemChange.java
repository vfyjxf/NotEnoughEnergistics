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

import java.util.ArrayList;
import java.util.List;

/**
 * @author vfyjxf
 */
public class PacketRecipeItemChange implements IMessage, IMessageHandler<PacketRecipeItemChange, IMessage> {

    private ItemStack stack;
    private List<Integer> craftingSlots;

    public PacketRecipeItemChange() {

    }

    public PacketRecipeItemChange(ItemStack stack, List<Integer> craftingSlots) {
        this.stack = stack;
        this.craftingSlots = craftingSlots;
    }

    public ItemStack getStack() {
        return stack;
    }

    public List<Integer> getCraftingSlots() {
        return craftingSlots;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stack = ByteBufUtils.readItemStack(buf);
        int craftingSlotsSize = buf.readInt();
        this.craftingSlots = new ArrayList<>(craftingSlotsSize);
        for (int i = 0; i < craftingSlotsSize; i++) {
            int slotNumber = buf.readInt();
            craftingSlots.add(slotNumber);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.stack);
        buf.writeInt(this.craftingSlots.size());
        for (Integer craftingSlot : this.craftingSlots) {
            buf.writeInt(craftingSlot);
        }
    }

    @Override
    public IMessage onMessage(PacketRecipeItemChange message, MessageContext ctx) {
        Container container = ctx.getServerHandler().playerEntity.openContainer;
        if (container instanceof ContainerPatternTerm || GuiUtils.isPatternTermExContainer(container)) {
            ItemStack nextStack = message.getStack();
            if (nextStack != null) {
                for (Integer craftingSlot : message.getCraftingSlots()) {
                    Slot currentSlot = container.getSlot(craftingSlot);
                    currentSlot.putStack(nextStack);
                }
            }
        }
        return null;
    }
}
