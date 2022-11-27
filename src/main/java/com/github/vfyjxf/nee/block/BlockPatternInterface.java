package com.github.vfyjxf.nee.block;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import com.github.vfyjxf.nee.network.NEEGuiHandler;
import com.github.vfyjxf.nee.utils.Globals;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author vfyjxf
 */
public class BlockPatternInterface extends AEBaseTileBlock {

    public static BlockPatternInterface BLOCK_INSTANCE = new BlockPatternInterface();
    public static Item ITEM_INSTANCE = new AEBaseItemBlock(BLOCK_INSTANCE).setRegistryName(Globals.MOD_ID, "pattern_interface").setTranslationKey(Globals.MOD_ID + "." + "pattern_interface");

    public BlockPatternInterface() {
        super(Material.IRON);
        setRegistryName(Globals.MOD_ID, "pattern_interface");
        setTranslationKey(Globals.MOD_ID + "." + "pattern_interface");
        setTileEntity(TilePatternInterface.class);

    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.isSneaking()) {
            return false;
        }

        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile != null) {
                NEEGuiHandler.openGui(playerIn, NEEGuiHandler.PATTERN_INTERFACE_ID, tile, AEPartLocation.INTERNAL);
                return true;
            }
        }

        return true;
    }
}
