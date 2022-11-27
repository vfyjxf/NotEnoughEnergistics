package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.network.packet.*;
import com.github.vfyjxf.nee.utils.Globals;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NEENetworkHandler {

    private static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Globals.MOD_ID);
    private static int packId = 0;

    public static SimpleNetworkWrapper getInstance() {
        return INSTANCE;
    }

    private static int nextId() {
        return packId++;
    }

    public static void init() {
        INSTANCE.registerMessage(PacketRecipeTransfer.Handler.class, PacketRecipeTransfer.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketStackSizeChange.Handler.class, PacketStackSizeChange.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketSlotStackSwitch.Handler.class, PacketSlotStackSwitch.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketCraftingRequest.Handler.class, PacketCraftingRequest.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketOpenGui.Handler.class, PacketOpenGui.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketOpenCraftAmount.Handler.class, PacketOpenCraftAmount.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketValueConfigServer.Handler.class, PacketValueConfigServer.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketValueConfigClient.Handler.class, PacketValueConfigClient.class, nextId(), Side.CLIENT);
        INSTANCE.registerMessage(PacketSetRecipe.Handler.class, PacketSetRecipe.class, nextId(), Side.SERVER);
    }

}
