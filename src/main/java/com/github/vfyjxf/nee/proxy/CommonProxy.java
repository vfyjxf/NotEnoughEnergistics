package com.github.vfyjxf.nee.proxy;

import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.block.BlockPatternInterface;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void init(FMLInitializationEvent event) {
        AEBaseTile.registerTileItem(TilePatternInterface.class,
                new BlockStackSrc(BlockPatternInterface.BLOCK_INSTANCE, 0, ActivityState.Enabled));
    }

    public void preInit(FMLPreInitializationEvent event){
        registerBlocks();
        registerItems();
        registerTileEntities();
    }

    public void registerItems() {
        GameRegistry.registerItem(BlockPatternInterface.ITEM_INSTANCE, "tile.pattern_interface");
    }

    public void registerBlocks() {
        GameRegistry.registerBlock(BlockPatternInterface.BLOCK_INSTANCE, null,"tile.pattern_interface");
    }

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TilePatternInterface.class, NotEnoughEnergistics.MODID + "." + "tile.pattern_interface");
    }

}
