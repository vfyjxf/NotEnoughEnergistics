package com.github.vfyjxf.nee.client;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingRequest;
import com.github.vfyjxf.nee.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.*;

public class GuiHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        boolean isCraftingGui = event.getGui() instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(event.getGui());
        boolean isCraftConfirmGui = currentScreen instanceof GuiCraftConfirm || GuiUtils.isWirelessGuiCraftConfirm(currentScreen);
        if (isCraftingGui && isCraftConfirmGui && tracker != null) {
            if (tracker.getRequireStacks().size() > 1 && stackIndex < tracker.getRequireStacks().size()) {
                IAEItemStack stack = AEItemStack.fromItemStack(tracker.getRequiredStack(stackIndex));
                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(stack, noPreview));
                stackIndex++;
            }
        } else if (!isCraftingGui && isCraftConfirmGui && tracker != null) {
            //Prevent opening the gui during normal use
            tracker = null;
        }
    }

    //TODO:add some buttons to change settings
    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {

    }

}
