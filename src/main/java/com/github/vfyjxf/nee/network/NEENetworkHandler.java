package com.github.vfyjxf.nee.network;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.network.packets.PacketCraftingRequest;
import com.github.vfyjxf.nee.network.packets.PacketRecipeTransfer;
import com.github.vfyjxf.nee.network.packets.PacketSlotStackChange;
import com.github.vfyjxf.nee.network.packets.PacketStackCountChange;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NEENetworkHandler {

    private static final String VERSION = "1.0.0";
    private static int packId;
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NotEnoughEnergistics.MODID, "network_channel"),
            () -> VERSION,
            (version) -> version.equals(VERSION),
            (version) -> version.equals(VERSION));


    private static int nextId() {
        return packId++;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(nextId(), PacketRecipeTransfer.class, PacketRecipeTransfer::encode, PacketRecipeTransfer::decode, PacketRecipeTransfer::handle);
        INSTANCE.registerMessage(nextId(), PacketStackCountChange.class, PacketStackCountChange::encode, PacketStackCountChange::decode, PacketStackCountChange::handle);
        INSTANCE.registerMessage(nextId(), PacketSlotStackChange.class, PacketSlotStackChange::encode, PacketSlotStackChange::decode, PacketSlotStackChange::handle);
        INSTANCE.registerMessage(nextId(), PacketCraftingRequest.class, PacketCraftingRequest::encode, PacketCraftingRequest::decode, PacketCraftingRequest::handle);
    }

    public static SimpleChannel getInstance() {
        return INSTANCE;
    }

}
