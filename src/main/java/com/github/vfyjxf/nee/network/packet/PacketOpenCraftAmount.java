package com.github.vfyjxf.nee.network.packet;

import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftingTerm;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.network.NEEGuiHandler;
import com.github.vfyjxf.nee.utils.GuiUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static com.github.vfyjxf.nee.network.NEEGuiHandler.CRAFTING_AMOUNT_ID;
import static com.github.vfyjxf.nee.network.NEEGuiHandler.CRAFTING_AMOUNT_WIRELESS_ID;

public class PacketOpenCraftAmount implements IMessage, IMessageHandler<PacketOpenCraftAmount, IMessage> {

    private ItemStack resultStack;
    private boolean isWirelessTerm;
    private boolean isBauble;
    private int wctSlot = -1;

    public PacketOpenCraftAmount() {

    }

    public PacketOpenCraftAmount(ItemStack resultStack) {
        this.resultStack = resultStack;
    }

    public PacketOpenCraftAmount(ItemStack resultStack, boolean isBauble, int wctSlot) {
        this.resultStack = resultStack;
        this.isBauble = isBauble;
        this.wctSlot = wctSlot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.resultStack = ByteBufUtils.readItemStack(buf);
        this.isWirelessTerm = buf.readBoolean();
        if (isWirelessTerm) {
            this.isBauble = buf.readBoolean();
            this.wctSlot = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.resultStack);
        if (this.wctSlot >= 0) {
            buf.writeBoolean(true);
            buf.writeBoolean(this.isBauble);
            buf.writeInt(this.wctSlot);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public IMessage onMessage(PacketOpenCraftAmount message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        player.getServerWorld().addScheduledTask(() -> {
            if (container instanceof ContainerCraftingTerm) {
                final ContainerOpenContext context = ((ContainerCraftingTerm) container).getOpenContext();
                if (context != null) {
                    final TileEntity tile = context.getTile();

                    NEEGuiHandler.openGui(player, CRAFTING_AMOUNT_ID, tile, context.getSide());

                    if (player.openContainer instanceof ContainerCraftingAmount) {
                        ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                        if (message.resultStack != null && !message.resultStack.isEmpty()) {
                            cca.setResultStack(message.resultStack);
                            cca.getResultSlot().putStack(message.resultStack);
                        }
                        cca.detectAndSendChanges();
                    }

                }
            } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                NEEGuiHandler.openGui(player, CRAFTING_AMOUNT_WIRELESS_ID, player.world);

                if (player.openContainer instanceof ContainerCraftingAmount) {
                    ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                    if (message.resultStack != null) {
                        cca.setResultStack(message.resultStack);
                        cca.getResultSlot().putStack(message.resultStack);
                    }
                    if (message.isWirelessTerm) {
                        cca.setBauble(message.isBauble);
                        cca.setWctSlot(message.wctSlot);
                    }
                    cca.detectAndSendChanges();
                }
            }
        });
        return null;
    }
}
