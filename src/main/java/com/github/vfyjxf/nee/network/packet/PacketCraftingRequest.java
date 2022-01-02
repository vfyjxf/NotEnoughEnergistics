package com.github.vfyjxf.nee.network.packet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ModIDs;
import cpw.mods.fml.common.Loader;
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

import java.io.IOException;
import java.util.concurrent.Future;

public class PacketCraftingRequest implements IMessage, IMessageHandler<PacketCraftingRequest, IMessage> {


    private IAEItemStack requireToCraftStack;
    private boolean noPreview;
    private int craftAmount;

    public PacketCraftingRequest() {

    }

    public PacketCraftingRequest(IAEItemStack requireToCraftStack, boolean noPreview) {
        this.requireToCraftStack = requireToCraftStack;
        this.noPreview = noPreview;
    }

    public PacketCraftingRequest(int craftAmount, boolean noPreview) {
        this.craftAmount = craftAmount;
        this.noPreview = noPreview;
    }

    public IAEItemStack getRequireToCraftStack() {
        return requireToCraftStack;
    }

    public boolean isNoPreview() {
        return noPreview;
    }


    public int getCraftAmount() {
        return craftAmount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            try {
                this.requireToCraftStack = AEItemStack.loadItemStackFromPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.noPreview = buf.readBoolean();
        this.craftAmount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (this.requireToCraftStack != null) {
            try {
                buf.writeBoolean(true);
                this.requireToCraftStack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(this.noPreview);
        buf.writeInt(this.craftAmount);
    }

    @Override
    public IMessage onMessage(PacketCraftingRequest message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        Container container = player.openContainer;

        if (container instanceof AEBaseContainer) {
            AEBaseContainer baseContainer = (AEBaseContainer) container;
            Object target = baseContainer.getTarget();
            if (container instanceof ContainerCraftingTerm) {
                handlerCraftingTermRequest((ContainerCraftingTerm) container, message, target, player);
            }

            if (container instanceof ContainerCraftingAmount) {
                handlerCraftingAmountRequest((ContainerCraftingAmount) container, message, target, player);
            }
        } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
            handlerWirelessCraftingRequest((ContainerWirelessCraftingTerminal) container, message, player);
        }

        return null;
    }

    private void handlerCraftingTermRequest(ContainerCraftingTerm container, PacketCraftingRequest message, Object target, EntityPlayerMP player) {
        if (target instanceof IGridHost) {
            IGridHost gh = (IGridHost) target;
            IGridNode gn = gh.getGridNode(ForgeDirection.UNKNOWN);
            if (gn != null) {
                IGrid grid = gn.getGrid();
                if (grid != null && message.getRequireToCraftStack() != null) {
                    Future<ICraftingJob> futureJob = null;
                    try {
                        final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                        futureJob = cg.beginCraftingJob(player.worldObj, grid, container.getActionSource(), message.getRequireToCraftStack(), null);

                        final ContainerOpenContext context = container.getOpenContext();
                        if (context != null) {
                            final TileEntity te = context.getTile();
                            Platform.openGUI(player, te, container.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_CONFIRM);
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
    }

    private void handlerCraftingAmountRequest(ContainerCraftingAmount container, PacketCraftingRequest message, Object target, EntityPlayerMP player) {
        if (target instanceof IActionHost) {
            IActionHost ah = (IActionHost) target;
            IGridNode gn = ah.getActionableNode();
            IGrid grid = gn.getGrid();
            ItemStack resultStack = container.getResultStack();
            IAEItemStack result = message.getRequireToCraftStack();
            if (resultStack != null) {
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(player.worldObj, grid, container.getActionSource(), result, null);

                    final ContainerOpenContext context = container.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        Platform.openGUI(player, te, context.getSide(), GuiBridge.GUI_CRAFTING_CONFIRM);
                        if (player.openContainer instanceof ContainerCraftConfirm) {
                            final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
                            ccc.setAutoStart(message.isNoPreview());
                            ccc.setJob(futureJob);
                            ccc.detectAndSendChanges();
                        }
                    } else if (Loader.isModLoaded(ModIDs.WCT)) {
                        int x = (int) player.posX;
                        int y = (int) player.posY;
                        int z = (int) player.posZ;

                        WCTGuiHandler.launchGui(Reference.GUI_CRAFT_CONFIRM, player, player.worldObj, x, y, z);

                        if (player.openContainer instanceof net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm) {
                            final net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm ccc = (net.p455w0rd.wirelesscraftingterminal.common.container.ContainerCraftConfirm) player.openContainer;
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

    private void handlerWirelessCraftingRequest(ContainerWirelessCraftingTerminal container, PacketCraftingRequest message, EntityPlayerMP player) {
        Object target = container.getTarget();
        if (target instanceof WirelessTerminalGuiObject) {
            IGrid grid = ((WirelessTerminalGuiObject) target).getTargetGrid();
            if (grid != null && message.getRequireToCraftStack() != null) {
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(player.worldObj, grid, container.getActionSource(), message.getRequireToCraftStack(), null);

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

}
