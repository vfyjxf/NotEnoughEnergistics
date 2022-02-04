package com.github.vfyjxf.nee.network.packet;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import com.github.vfyjxf.nee.network.NEEGuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class PacketOpenGui implements IMessage, IMessageHandler<PacketOpenGui, IMessage> {

    private int guiId;

    public PacketOpenGui() {

    }

    public PacketOpenGui(int guiId) {
        this.guiId = guiId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.guiId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.guiId);
    }

    @Override
    public IMessage onMessage(PacketOpenGui message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        if (container instanceof AEBaseContainer) {
            player.getServerWorld().addScheduledTask(() -> {
                final ContainerOpenContext context = ((AEBaseContainer) container).getOpenContext();
                if (context != null) {
                    final TileEntity tile = context.getTile();
                    NEEGuiHandler.openGui(player, message.guiId, tile, context.getSide());
                }
            });
        }
        return null;
    }
}
