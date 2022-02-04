package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.NEECommands;
import com.github.vfyjxf.nee.client.KeyBindings;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.event.GuiEventHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;


public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(KeyBindings.recipeIngredientChange);
        ClientRegistry.registerKeyBinding(KeyBindings.stackCountChange);
        ClientRegistry.registerKeyBinding(KeyBindings.craftingHelperPreview);
        ClientRegistry.registerKeyBinding(KeyBindings.craftingHelperNoPreview);
        ClientCommandHandler.instance.registerCommand(new NEECommands());
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        NEEConfig.initConfig(new File(event.getModConfigurationDirectory().getPath(), "NotEnoughEnergistics.cfg"));
        MinecraftForge.EVENT_BUS.register(NEEConfig.INSTANCE);
        NEENetworkHandler.init();
    }
}
