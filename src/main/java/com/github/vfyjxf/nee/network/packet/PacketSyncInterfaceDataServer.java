package com.github.vfyjxf.nee.network.packet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.integration.IPatternProvider;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.SyncAction;
import com.github.vfyjxf.nee.utils.Globals;
import com.github.vfyjxf.nee.utils.PatternContainerControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.Objects;

public class PacketSyncInterfaceDataServer implements IMessage {

    private int actionId;

    public PacketSyncInterfaceDataServer() {
    }

    public PacketSyncInterfaceDataServer(SyncAction action) {
        this.actionId = action.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {

    }

    @Override
    public void toBytes(ByteBuf byteBuf) {

    }

    public static class Handler implements IMessageHandler<PacketSyncInterfaceDataServer, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncInterfaceDataServer packet, MessageContext cxt) {
            EntityPlayerMP player = cxt.getServerHandler().player;
            Container container = player.openContainer;
            SyncAction action = SyncAction.values()[packet.actionId];
            if (Objects.requireNonNull(action) == SyncAction.SYNC_INTERFACE_DATA) {
                handleInterfaceSync(container, player);
            } else {
                return null;
            }
            return null;
        }

        private void handleInterfaceSync(Container container, EntityPlayerMP player) {
            if (container instanceof ContainerPatternTerm) {
                ContainerPatternTerm patternTerm = (ContainerPatternTerm) container;
                NBTTagCompound tag = packInterfaceData(patternTerm);
                NEENetworkHandler.getInstance().sendTo(
                        new PacketSyncInterfaceDataClient(SyncAction.SYNC_INTERFACE_DATA, tag),
                        player
                );
            }

        }

        @SuppressWarnings("unchecked")
        private <T extends IGridHost> NBTTagCompound packInterfaceData(ContainerPatternTerm term) {
            NBTTagCompound interfaceTag = new NBTTagCompound();
            for (IPatternProvider<?> p : IPatternProvider.PROVIDERS) {
                IPatternProvider<T> provider = (IPatternProvider<T>) p;
                IGrid grid = term.getNetworkNode().getGrid();
                for (IGridNode node : grid.getMachines(provider.getHostClass())) {
                    if (node.isActive()) {
                        NBTTagCompound tag = new NBTTagCompound();
                        IGridHost host = node.getMachine();
                        IItemHandler patterns = provider.getPatterns((T) host);
                        tag.setString("uid", provider.uid((T) host));
                        tag.setTag("patterns", packPatterns(patterns));
                        tag.setTag("identifier", node.getGridBlock().getMachineRepresentation().writeToNBT(new NBTTagCompound()));
                        tag.setTag("target", provider.getTarget((T) host).writeToNBT(new NBTTagCompound()));
                        tag.setString("name", provider.getName((T) host));
                        interfaceTag.setTag(provider.uid((T) host), tag);
                        PatternContainerControl.putHandler(provider.uid((T) host), patterns);
                    }
                }
            }
            return interfaceTag;
        }

        private static NBTTagCompound packPatterns(IItemHandler patterns) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("size", patterns.getSlots());
            for (int i = 0; i < patterns.getSlots(); i++) {
                NBTTagCompound pattern = new NBTTagCompound();
                patterns.getStackInSlot(i).writeToNBT(pattern);
                tag.setTag(Globals.INPUT_KEY_HEAD + i, pattern);
            }
            return tag;
        }

    }


}
