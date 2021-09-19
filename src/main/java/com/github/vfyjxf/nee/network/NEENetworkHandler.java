package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.network.packet.PacketRecipeTransfer;
import com.github.vfyjxf.nee.network.packet.PacketRecipeItemChange;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NEENetworkHandler {

    private static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(NotEnoughEnergistics.MODID);

    public static SimpleNetworkWrapper getInstance() {
        return INSTANCE;
    }

    public static void init() {
        NEENetworkHandler.getInstance().registerMessage(PacketRecipeTransfer.class, PacketRecipeTransfer.class, 0, Side.SERVER);
        NEENetworkHandler.getInstance().registerMessage(PacketStackCountChange.class, PacketStackCountChange.class, 1, Side.SERVER);
        NEENetworkHandler.getInstance().registerMessage(PacketRecipeItemChange.class, PacketRecipeItemChange.class, 2, Side.SERVER);
    }

}
