package com.github.vfyjxf.nee.network.packet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.concurrent.Future;

public class PacketCraftingHelper implements IMessage, IMessageHandler<PacketCraftingHelper, IMessage> {

    private ItemStack requireToCraftStack;
    private boolean noPreview;

    public PacketCraftingHelper() {

    }

    public PacketCraftingHelper(ItemStack requireToCraftStack, boolean noPreview) {
        this.requireToCraftStack = requireToCraftStack;
        this.noPreview = noPreview;
    }

    public ItemStack getRequireToCraftStack() {
        return requireToCraftStack;
    }

    public boolean isNoPreview() {
        return noPreview;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.requireToCraftStack = ByteBufUtils.readItemStack(buf);
        this.noPreview = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.requireToCraftStack);
        buf.writeBoolean(this.noPreview);
    }

    @Override
    public IMessage onMessage(PacketCraftingHelper message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        AEBaseContainer baseContainer = (AEBaseContainer) container;
        Object target = baseContainer.getTarget();
        if (target instanceof IActionHost) {
            IActionHost ah = (IActionHost) target;
            IGridNode gn = ah.getActionableNode();
            IGrid grid = gn.getGrid();
            if (message.getRequireToCraftStack() != null && !message.getRequireToCraftStack().isEmpty()) {
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    IAEItemStack result = AEItemStack.fromItemStack(message.getRequireToCraftStack());
                    futureJob = cg.beginCraftingJob(player.world, grid, baseContainer.getActionSource(), result, null);

                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        Platform.openGUI(player, te, baseContainer.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_CONFIRM);
                        if (player.openContainer instanceof ContainerCraftConfirm) {
                            final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
                            ccc.setJob(futureJob);
                            ccc.setAutoStart(message.isNoPreview());
                        }
                    }
                } catch (final Throwable e) {
                    if (futureJob != null) {
                        futureJob.cancel(true);
                    }
                    AELog.debug(e);
                }
            }
        }
        return null;
    }
}
