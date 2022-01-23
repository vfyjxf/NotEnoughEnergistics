package com.github.vfyjxf.nee.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public abstract class CommonProxy {

    public abstract void init(FMLInitializationEvent event);

    public abstract void preInit(FMLPreInitializationEvent event);

}
