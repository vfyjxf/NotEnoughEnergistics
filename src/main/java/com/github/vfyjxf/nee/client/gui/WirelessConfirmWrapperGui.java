package com.github.vfyjxf.nee.client.gui;

import appeng.api.storage.ITerminalHost;
import com.github.vfyjxf.nee.helper.IngredientRequester;
import net.minecraft.entity.player.InventoryPlayer;
import p455w0rd.wct.client.gui.GuiCraftConfirm;

public class WirelessConfirmWrapperGui extends GuiCraftConfirm {

    public WirelessConfirmWrapperGui(InventoryPlayer inventoryPlayer, ITerminalHost te, boolean isBauble, int wctSlot) {
        super(inventoryPlayer, te, isBauble, wctSlot);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        IngredientRequester.getInstance().requestNext();
    }
}
