package com.github.vfyjxf.nee.gui;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.parts.reporting.PartCraftingTerminal;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

import static com.github.vfyjxf.nee.NotEnoughEnergistics.instance;

public class NEEGuiHandler implements IGuiHandler {


    public static final int CRAFTING_AMOUNT_ID = 0;


    @Nullable
    @Override
    public Object getServerGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        final int guiId = ordinal >> 4;
        final AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 7);
        //There should be no crafting terminal in the center of the block, right?
        if (side != AEPartLocation.INTERNAL) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof IPartHost) {
                IPartHost partHost = (IPartHost) tile;
                IPart part = partHost.getPart(side);
                if (guiId == CRAFTING_AMOUNT_ID) {
                    if (part instanceof PartCraftingTerminal) {
                        return updateGui(new ContainerCraftingAmount(player.inventory, part), world, x, y, z, side, part);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        final int guiId = ordinal >> 4;
        final AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 7);
        //There should be no crafting terminal in the center of the block, right?
        if (side != AEPartLocation.INTERNAL) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof IPartHost) {
                IPartHost partHost = (IPartHost) tile;
                IPart part = partHost.getPart(side);
                if (guiId == CRAFTING_AMOUNT_ID) {
                    if (part instanceof PartCraftingTerminal) {
                        return updateGui(new GuiCraftingAmount(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                    }
                }
            }

        }
        return null;
    }

    private Object updateGui(Object newContainer, final World w, final int x, final int y, final int z, final AEPartLocation side, final IPart part) {
        if (newContainer instanceof AEBaseContainer) {
            final AEBaseContainer bc = (AEBaseContainer) newContainer;
            bc.setOpenContext(new ContainerOpenContext(part));
            bc.getOpenContext().setWorld(w);
            bc.getOpenContext().setX(x);
            bc.getOpenContext().setY(y);
            bc.getOpenContext().setZ(z);
            bc.getOpenContext().setSide(side);
        }
        return newContainer;
    }

    public static void openGui(EntityPlayer player, int ID, TileEntity tile, AEPartLocation side) {
        int x = tile.getPos().getX();
        int y = tile.getPos().getY();
        int z = tile.getPos().getZ();
        player.openGui(instance, ID << 4 | side.ordinal(), tile.getWorld(), x, y, z);
    }
}
