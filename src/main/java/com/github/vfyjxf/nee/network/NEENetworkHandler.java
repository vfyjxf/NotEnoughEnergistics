package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.network.packet.NEERecipeTransferPacket;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NEENetworkHandler {

    private static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(NotEnoughEnergistics.MODID);

    public static SimpleNetworkWrapper getInstance() {
        return INSTANCE;
    }

    public static void init() {
        NEENetworkHandler.getInstance().registerMessage(NEERecipeTransferPacket.class, NEERecipeTransferPacket.class, 0, Side.SERVER);
        NEENetworkHandler.getInstance().registerMessage(PacketStackCountChange.class, PacketStackCountChange.class, 1, Side.SERVER);
    }

}
