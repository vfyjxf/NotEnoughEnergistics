package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NEENetworkHandler {

    private static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(NotEnoughEnergistics.MODID);

    public static SimpleNetworkWrapper getInstance(){
        return INSTANCE;
    }

    public static void init(){
        INSTANCE.registerMessage(PacketNEIPatternRecipe.class, PacketNEIPatternRecipe.class,0, Side.SERVER);
    }

}
