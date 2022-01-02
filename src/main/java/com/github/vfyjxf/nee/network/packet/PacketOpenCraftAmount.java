package com.github.vfyjxf.nee.network.packet;

import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftingTerm;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.gui.NEEGuiHandler;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import static com.github.vfyjxf.nee.gui.NEEGuiHandler.CRAFTING_AMOUNT_ID;
import static com.github.vfyjxf.nee.gui.NEEGuiHandler.CRAFTING_AMOUNT_WIRELESS_ID;

public class PacketOpenCraftAmount implements IMessage, IMessageHandler<PacketOpenCraftAmount, IMessage> {

    private ItemStack resultStack;

    public PacketOpenCraftAmount() {

    }

    public PacketOpenCraftAmount(ItemStack resultStack) {
        this.resultStack = resultStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.resultStack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.resultStack);
    }

    @Override
    public IMessage onMessage(PacketOpenCraftAmount message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container container = player.openContainer;
        if (container instanceof ContainerCraftingTerm) {
            final ContainerOpenContext context = ((ContainerCraftingTerm) container).getOpenContext();
            if (context != null) {
                final TileEntity tile = context.getTile();

                NEEGuiHandler.openGui(player, CRAFTING_AMOUNT_ID, tile, context.getSide());

                if (player.openContainer instanceof ContainerCraftingAmount) {
                    ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                    if (message.resultStack != null) {
                        cca.setResultStack(message.resultStack);
                        cca.getResultSlot().putStack(message.resultStack);
                    }
                    cca.detectAndSendChanges();
                }
            }
        } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {

            NEEGuiHandler.openGui(player, CRAFTING_AMOUNT_WIRELESS_ID, player.worldObj);

            if (player.openContainer instanceof ContainerCraftingAmount) {
                ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                if (message.resultStack != null) {
                    cca.setResultStack(message.resultStack);
                    cca.getResultSlot().putStack(message.resultStack);
                }
                cca.detectAndSendChanges();
            }
        }
        return null;
    }

}
