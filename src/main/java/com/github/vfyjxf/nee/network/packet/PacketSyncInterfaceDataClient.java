package com.github.vfyjxf.nee.network.packet;

import com.github.vfyjxf.nee.network.SyncAction;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Objects;

public class PacketSyncInterfaceDataClient implements IMessage {


    private int actionId;
    private NBTTagCompound data;

    public PacketSyncInterfaceDataClient() {
    }

    public PacketSyncInterfaceDataClient(SyncAction action, NBTTagCompound data) {
        this.actionId = action.ordinal();
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.actionId = byteBuf.readInt();
        this.data = ByteBufUtils.readTag(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.actionId);
        ByteBufUtils.writeTag(byteBuf, this.data);
    }

    public static class Handler implements IMessageHandler<PacketSyncInterfaceDataClient, IMessage> {

        @Override
        public IMessage onMessage(PacketSyncInterfaceDataClient packet, MessageContext ctx) {
            SyncAction action = SyncAction.values()[packet.actionId];
            if (Objects.requireNonNull(action) == SyncAction.SYNC_INTERFACE_DATA) {
                handleSyncInterfaceData(packet, ctx);
            } else {
                return null;
            }
            return null;
        }

        private void handleSyncInterfaceData(PacketSyncInterfaceDataClient packet, MessageContext ctx) {
//            QuickPusherWidget pusher = GuiEventHandler.getInstance().getPusherWidget();
//            if (pusher != null) {
//                Minecraft.getMinecraft().addScheduledTask(() -> pusher.syncFromServer(packet.data));
//            }
        }

    }
}
