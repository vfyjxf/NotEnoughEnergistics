package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.NEECommands;
import com.github.vfyjxf.nee.client.GuiEventHandler;
import com.github.vfyjxf.nee.config.KeyBindings;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.utils.Gobals;
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

import static com.github.vfyjxf.nee.block.BlockPatternInterface.ITEM_INSTANCE;

@Mod.EventBusSubscriber(modid = Gobals.MOD_ID)
public class ClientProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(KeyBindings.SWITCH_INGREDIENT);
        ClientRegistry.registerKeyBinding(KeyBindings.MODIFY_COUNT);
        ClientRegistry.registerKeyBinding(KeyBindings.AUTO_CRAFT_NON_PREVIEW);
        ClientRegistry.registerKeyBinding(KeyBindings.AUTO_CRAFT_NON_PREVIEW);
        ClientCommandHandler.instance.registerCommand(new NEECommands());
        MinecraftForge.EVENT_BUS.register(GuiEventHandler.getInstance());
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        NEEConfig.preInit(event);
        NEENetworkHandler.init();
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, new ModelResourceLocation(ITEM_INSTANCE.getRegistryName(), "inventory"));
    }

}
