package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.network.packet.*;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NEENetworkHandler {

    private static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(NotEnoughEnergistics.MODID);
    private static int packId = 0;

    private static int nextId() {
        return packId++;
    }

    public static SimpleNetworkWrapper getInstance() {
        return INSTANCE;
    }

    public static void init() {
        INSTANCE.registerMessage(PacketNEIPatternRecipe.class, PacketNEIPatternRecipe.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketArcaneRecipe.class, PacketArcaneRecipe.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketExtremeRecipe.class, PacketExtremeRecipe.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketStackCountChange.class, PacketStackCountChange.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketSlotStackChange.class, PacketSlotStackChange.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketCraftingRequest.class, PacketCraftingRequest.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketOpenCraftAmount.class, PacketOpenCraftAmount.class, nextId(), Side.SERVER);
        INSTANCE.registerMessage(PacketOpenGui.class, PacketOpenGui.class, nextId(), Side.SERVER);
    }

}