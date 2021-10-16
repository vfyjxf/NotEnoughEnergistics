package com.github.vfyjxf.nee.network.packet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.common.WCTGuiHandler;
import net.p455w0rd.wirelesscraftingterminal.common.container.ContainerWirelessCraftingTerminal;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;

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
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container container = player.openContainer;
        if (container instanceof AEBaseContainer) {
            AEBaseContainer baseContainer = (AEBaseContainer) container;
            Object target = baseContainer.getTarget();
            if (target instanceof IGridHost) {
                IGridHost gh = (IGridHost) target;
                IGridNode gn = gh.getGridNode(ForgeDirection.UNKNOWN);
                if (gn != null) {
                    IGrid grid = gn.getGrid();
                    if (grid != null && message.getRequireToCraftStack() != null) {
                        Future<ICraftingJob> futureJob = null;
                        try {
                            final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                            IAEItemStack result = AEItemStack.create(message.getRequireToCraftStack());
                            futureJob = cg.beginCraftingJob(player.worldObj, grid, baseContainer.getActionSource(), result, null);

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
            }
        } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
            ContainerWirelessCraftingTerminal cwt = (ContainerWirelessCraftingTerminal) container;
            Object target = cwt.getTarget();
            if (target instanceof WirelessTerminalGuiObject) {
                IGrid grid = ((WirelessTerminalGuiObject) target).getTargetGrid();
                if (grid != null && message.getRequireToCraftStack() != null) {
                    Future<ICraftingJob> futureJob = null;
                    try {
                        final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                        IAEItemStack result = AEItemStack.create(message.getRequireToCraftStack());
                        futureJob = cg.beginCraftingJob(player.worldObj, grid, cwt.getActionSource(), result, null);

                        int x = (int) player.posX;
                        int y = (int) player.posY;
                        int z = (int) player.posZ;

                        WCTGuiHandler.launchGui(Reference.GUI_CRAFT_CONFIRM, player, player.worldObj, x, y, z);

                        if (player.openContainer instanceof net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm) {
                            final net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm ccc = (net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm) player.openContainer;
                            ccc.setJob(futureJob);
                            ccc.setAutoStart(message.isNoPreview());
                        }
                    } catch (final Throwable e) {
                        if (futureJob != null) {
                            futureJob.cancel(true);
                        }
                        AELog.debug(e);
                    }
                }
            }
        }
        return null;
    }

}
