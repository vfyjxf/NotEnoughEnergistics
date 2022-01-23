package com.github.vfyjxf.nee.proxy;

import com.github.vfyjxf.nee.network.NEENetworkHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy extends CommonProxy {
    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        NEENetworkHandler.init();
    }
}
