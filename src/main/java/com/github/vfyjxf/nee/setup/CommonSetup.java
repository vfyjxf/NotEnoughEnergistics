package com.github.vfyjxf.nee.setup;

import com.github.vfyjxf.nee.network.NEENetworkHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonSetup {
    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NEENetworkHandler::registerMessages);
    }
}
