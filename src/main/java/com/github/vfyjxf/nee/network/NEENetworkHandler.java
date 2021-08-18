package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NEENetworkHandler {

    private static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(NotEnoughEnergistics.MODID);

    public static SimpleNetworkWrapper getInstance(){
        return INSTANCE;
    }

    public static void init(){
    }

}
