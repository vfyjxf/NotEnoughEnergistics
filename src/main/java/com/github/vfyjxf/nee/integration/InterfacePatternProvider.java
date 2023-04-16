package com.github.vfyjxf.nee.integration;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class InterfacePatternProvider implements IPatternProvider<TileInterface> {

    @Nonnull
    @Override
    public Class<? extends IGridHost> getHostClass() {
        return TileInterface.class;
    }

    @Override
    public IItemHandler getPatterns(TileInterface tileEntity) {
        DualityInterface dual = tileEntity.getInterfaceDuality();
        IGridNode node = tileEntity.getGridNode(AEPartLocation.INTERNAL);
        if (node != null && node.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
            return null;
        }
        return dual.getPatterns();
    }

    @Override
    public ItemStack getTarget(TileInterface tileEntity) {
        DualityInterface dual = tileEntity.getInterfaceDuality();
        IInterfaceHost iHost = (IInterfaceHost) dual.getHost();
        {
            final TileEntity hostTile = iHost.getTile();
            final World hostWorld = hostTile.getWorld();

            if (((ICustomNameObject) iHost).hasCustomInventoryName()) {
                IBlockState blockState = hostWorld.getBlockState(hostTile.getPos());
                return new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState));
            }

            final EnumSet<EnumFacing> possibleDirections = iHost.getTargets();
            for (final EnumFacing direction : possibleDirections) {
                final BlockPos target = hostTile.getPos().offset(direction);
                final TileEntity directedTile = hostWorld.getTileEntity(target);

                if (directedTile == null) {
                    continue;
                }

                if (directedTile instanceof IInterfaceHost && directedTile instanceof AENetworkInvTile) {
                    if (IPatternProvider.sameGird(dual, ((IInterfaceHost) directedTile).getInterfaceDuality())) {
                        continue;
                    }
                }

                final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(directedTile, direction.getOpposite());
                if (directedTile instanceof ICraftingMachine || adaptor != null) {
                    if (adaptor != null && !adaptor.hasSlots()) {
                        continue;
                    }

                    final IBlockState directedBlockState = hostWorld.getBlockState(target);
                    final Block directedBlock = directedBlockState.getBlock();
                    ItemStack what = new ItemStack(directedBlock, 1, directedBlock.getMetaFromState(directedBlockState));
                    Vec3d from = new Vec3d(hostTile.getPos().getX() + 0.5, hostTile.getPos().getY() + 0.5, hostTile.getPos().getZ() + 0.5);
                    from = from.add(direction.getXOffset() * 0.501, direction.getYOffset() * 0.501, direction.getZOffset() * 0.501);
                    final Vec3d to = from.add(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
                    final RayTraceResult mop = hostWorld.rayTraceBlocks(from, to, true);
                    if (mop != null) {
                        if (mop.getBlockPos().equals(directedTile.getPos())) {
                            final ItemStack g = directedBlock.getPickBlock(directedBlockState, mop, hostWorld, directedTile.getPos(), null);
                            if (!g.isEmpty()) {
                                what = g;
                            }
                        }
                    }

                    if (what.getItem() != Items.AIR) {
                        return what.copy();
                    }

                    final Item item = Item.getItemFromBlock(directedBlock);
                    if (item == Items.AIR) {
                        return new ItemStack(directedBlock, 1, directedBlock.getMetaFromState(directedBlockState));
                    }
                }
            }

            return ItemStack.EMPTY;
        }
    }

    @Override
    public String getName(TileInterface tileEntity) {
        return tileEntity.getInterfaceDuality().getTermName();
    }

    @Override
    public String uid(TileInterface tileEntity) {
        return Long.toString(tileEntity.getInterfaceDuality().getSortValue());
    }
}
