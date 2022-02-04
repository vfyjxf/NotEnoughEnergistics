package com.github.vfyjxf.nee.proxy;

import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import com.github.vfyjxf.nee.block.BlockPatternInterface;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.github.vfyjxf.nee.NotEnoughEnergistics.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class CommonProxy {

    public void init(FMLInitializationEvent event) {
        AEBaseTile.registerTileItem(BlockPatternInterface.BLOCK_INSTANCE.getTileEntityClass(),
                new BlockStackSrc(BlockPatternInterface.BLOCK_INSTANCE, 0, ActivityState.Enabled));
    }

    public void preInit(FMLPreInitializationEvent event) {

    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(BlockPatternInterface.ITEM_INSTANCE);
    }

    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(BlockPatternInterface.BLOCK_INSTANCE);
        GameRegistry.registerTileEntity(TilePatternInterface.class, new ResourceLocation(MODID, "patterninterface"));
    }

}
