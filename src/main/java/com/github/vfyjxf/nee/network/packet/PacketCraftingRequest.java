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
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ModIds;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import p455w0rd.ae2wtlib.api.container.IWTContainer;
import p455w0rd.ae2wtlib.api.networking.security.WTIActionHost;
import p455w0rd.wct.init.ModGuiHandler;

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

    public IAEItemStack getRequireToCraftStack() {
        return requireToCraftStack;
    }

    public boolean isNoPreview() {
        return noPreview;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            this.requireToCraftStack = AEItemStack.fromPacket(buf);
        }
        this.noPreview = buf.readBoolean();
        this.craftAmount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (this.requireToCraftStack != null) {
            buf.writeBoolean(true);
            try {
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
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        player.getServerWorld().addScheduledTask(() -> {
            AEBaseContainer baseContainer = (AEBaseContainer) container;
            Object target = baseContainer.getTarget();
            if (container instanceof ContainerCraftingTerm) {
                handlerCraftingTermRequest((ContainerCraftingTerm) baseContainer, message, target, player);
            }
            if (container instanceof ContainerCraftingAmount) {
                handlerCraftingAmountRequest((ContainerCraftingAmount) baseContainer, message, target, player);
            }
            if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                handlerWirelessCraftingRequest(baseContainer, message, target, player);
            }
        });
        return null;
    }


    private void handlerCraftingTermRequest(ContainerCraftingTerm container, PacketCraftingRequest message, Object target, EntityPlayerMP player) {
        if (target instanceof IActionHost) {
            IActionHost ah = (IActionHost) target;
            IGridNode gn = ah.getActionableNode();
            IGrid grid = gn.getGrid();
            if (message.getRequireToCraftStack() != null) {
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(player.world, grid, container.getActionSource(), message.getRequireToCraftStack(), null);

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

    private void handlerCraftingAmountRequest(ContainerCraftingAmount container, PacketCraftingRequest message, Object target, EntityPlayerMP player) {
        if (target instanceof IActionHost) {
            IActionHost ah = (IActionHost) target;
            IGridNode gn = ah.getActionableNode();
            IGrid grid = gn.getGrid();
            ItemStack resultStack = container.getResultStack();
            IAEItemStack result = message.getRequireToCraftStack();
            if (resultStack != null && !resultStack.isEmpty() && result != null) {
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(player.world, grid, container.getActionSource(), result, null);

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
                    } else if (Loader.isModLoaded(ModIds.WCT) && container.isWirelessTerm()) {

                        int x = (int) player.posX;
                        int y = (int) player.posY;
                        int z = (int) player.posZ;

                        ModGuiHandler.open(ModGuiHandler.GUI_CRAFT_CONFIRM, player, player.getEntityWorld(), new BlockPos(x, y, z), false, container.isBauble(), container.getWctSlot());
                        if (player.openContainer instanceof p455w0rd.wct.container.ContainerCraftConfirm) {
                            final p455w0rd.wct.container.ContainerCraftConfirm ccc = (p455w0rd.wct.container.ContainerCraftConfirm) player.openContainer;
                            ccc.setAutoStart(message.isNoPreview());
                            ccc.setJob(futureJob);
                            ccc.detectAndSendChanges();
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

    private void handlerWirelessCraftingRequest(AEBaseContainer container, PacketCraftingRequest message, Object target, EntityPlayerMP player) {
        if (target instanceof WTIActionHost) {
            IWTContainer iwtContainer = (IWTContainer) container;
            final WTIActionHost ah = (WTIActionHost) target;
            final IGridNode gn = ah.getActionableNode(true);
            if (gn != null) {
                final IGrid grid = gn.getGrid();
                if (message.getRequireToCraftStack() != null) {
                    Future<ICraftingJob> futureJob = null;
                    try {
                        final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                        futureJob = cg.beginCraftingJob(player.world, grid, container.getActionSource(), message.getRequireToCraftStack(), null);

                        int x = (int) player.posX;
                        int y = (int) player.posY;
                        int z = (int) player.posZ;

                        ModGuiHandler.open(ModGuiHandler.GUI_CRAFT_CONFIRM, player, player.getEntityWorld(), new BlockPos(x, y, z), false, iwtContainer.isWTBauble(), iwtContainer.getWTSlot());

                        if (player.openContainer instanceof p455w0rd.wct.container.ContainerCraftConfirm) {
                            final p455w0rd.wct.container.ContainerCraftConfirm ccc = (p455w0rd.wct.container.ContainerCraftConfirm) player.openContainer;
                            ccc.setAutoStart(message.isNoPreview());
                            ccc.setJob(futureJob);
                            ccc.detectAndSendChanges();
                        }
                    } catch (Throwable e) {
                        if (futureJob != null) {
                            futureJob.cancel(true);
                        }
                    }
                }
            }
        }
    }

}
