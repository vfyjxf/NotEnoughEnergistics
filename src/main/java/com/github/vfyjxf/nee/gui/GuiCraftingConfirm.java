package com.github.vfyjxf.nee.gui;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingRequest;
import net.minecraft.entity.player.InventoryPlayer;

import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.*;

public class GuiCraftingConfirm extends GuiCraftConfirm {

    public GuiCraftingConfirm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    public void onGuiClosed() {
        if (tracker != null && tracker.getRequireStacks().size() > 1 && stackIndex < tracker.getRequireStacks().size()) {
            IAEItemStack stack = AEItemStack.fromItemStack(tracker.getRequiredStack(stackIndex));
            NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(stack, noPreview));
            stackIndex++;
        } else {
            tracker = null;
        }
    }
}
