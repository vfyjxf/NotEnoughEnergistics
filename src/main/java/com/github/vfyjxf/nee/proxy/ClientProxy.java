package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.NEECommands;
import com.github.vfyjxf.nee.client.GuiHandler;
import com.github.vfyjxf.nee.client.MouseHandler;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import static com.github.vfyjxf.nee.client.MouseHandler.*;


public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(recipeIngredientChange);
        ClientRegistry.registerKeyBinding(stackCountChange);
        ClientRegistry.registerKeyBinding(craftingHelperPreview);
        ClientRegistry.registerKeyBinding(craftingHelperNoPreview);
        ClientCommandHandler.instance.registerCommand(new NEECommands());
        MinecraftForge.EVENT_BUS.register(new MouseHandler());
        MinecraftForge.EVENT_BUS.register(new GuiHandler());
    }
}
