package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.NEECommands;
import com.github.vfyjxf.nee.client.KeyBindings;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.event.GuiEventHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

import static com.github.vfyjxf.nee.NotEnoughEnergistics.MODID;
import static com.github.vfyjxf.nee.block.BlockPatternInterface.ITEM_INSTANCE;

@Mod.EventBusSubscriber(modid = MODID)
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

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, new ModelResourceLocation(ITEM_INSTANCE.getRegistryName(), "inventory"));
    }

}
