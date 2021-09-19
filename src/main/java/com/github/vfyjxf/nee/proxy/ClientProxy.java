package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.NEECommands;
import com.github.vfyjxf.nee.client.MouseHandler;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;


public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(MouseHandler.recipeIngredientChange);
        ClientRegistry.registerKeyBinding(MouseHandler.stackCountChange);
        ClientCommandHandler.instance.registerCommand(new NEECommands());
        MinecraftForge.EVENT_BUS.register(new MouseHandler());
    }
}
