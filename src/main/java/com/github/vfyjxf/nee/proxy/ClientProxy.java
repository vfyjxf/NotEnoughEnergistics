package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.NEECommands;
import com.github.vfyjxf.nee.client.GuiHandler;
import com.github.vfyjxf.nee.client.NEEContainerDrawHandler;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.nei.NEECraftingHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;


public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new NEECommands());
        FMLCommonHandler.instance().bus().register(new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new GuiHandler());
        if (NEEConfig.noShift) {
            MinecraftForge.EVENT_BUS.register(new NEECraftingHelper());
        }
        if(NEEConfig.drawHighlight){
            MinecraftForge.EVENT_BUS.register(NEEContainerDrawHandler.instance);
        }
    }
}
