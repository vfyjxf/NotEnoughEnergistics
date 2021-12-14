package com.github.vfyjxf.nee.network.packets;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.core.AELog;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public class PacketCraftingRequest {

    private ItemStack requireToCraftStack;
    private boolean autoStart;

    public PacketCraftingRequest() {

    }

    public PacketCraftingRequest(ItemStack requireToCraftStack, boolean noPreview) {
        this.requireToCraftStack = requireToCraftStack;
        this.autoStart = noPreview;
    }

    public ItemStack getRequireToCraftStack() {
        return requireToCraftStack;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeItemStack(this.requireToCraftStack, false);
        buffer.writeBoolean(this.autoStart);
    }

    public static PacketCraftingRequest decode(PacketBuffer buffer) {
        PacketCraftingRequest packet = new PacketCraftingRequest();
        packet.requireToCraftStack = buffer.readItem();
        packet.autoStart = buffer.readBoolean();
        return packet;
    }

    public static void handle(PacketCraftingRequest packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof AEBaseContainer) {
            ctx.get().enqueueWork(() -> {
                AEBaseContainer baseContainer = (AEBaseContainer) player.containerMenu;
                final Object target = baseContainer.getTarget();
                if (target instanceof IActionHost) {
                    final IActionHost ah = (IActionHost) target;
                    final IGridNode gn = ah.getActionableNode();
                    if (gn != null) {
                        final IGrid gird = gn.getGrid();
                        if (packet.getRequireToCraftStack() != null && !packet.getRequireToCraftStack().isEmpty()) {
                            Future<ICraftingJob> futureJob = null;
                            try {
                                final ICraftingGrid cg = gird.getCache(ICraftingGrid.class);
                                IAEItemStack result = AEItemStack.fromItemStack(packet.getRequireToCraftStack());
                                futureJob = cg.beginCraftingJob(player.level, gird, baseContainer.getActionSource(),
                                        result, null);

                                final ContainerLocator locator = baseContainer.getLocator();
                                if (locator != null) {
                                    ContainerOpener.openContainer(CraftConfirmContainer.TYPE, player, locator);

                                    if (player.containerMenu instanceof CraftConfirmContainer) {
                                        final CraftConfirmContainer ccc = (CraftConfirmContainer) player.containerMenu;
                                        ccc.setAutoStart(packet.isAutoStart());
                                        ccc.setItemToCreate(result);
                                        ccc.setJob(futureJob);
                                    }
                                }
                            } catch (final Throwable e) {
                                if (futureJob != null) {
                                    futureJob.cancel(true);
                                }
                                AELog.info(e);
                            }
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
