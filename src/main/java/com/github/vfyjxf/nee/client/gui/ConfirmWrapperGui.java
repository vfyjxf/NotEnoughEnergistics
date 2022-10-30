package com.github.vfyjxf.nee.client.gui;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftConfirm;
import com.github.vfyjxf.nee.helper.IngredientRequester;
import net.minecraft.entity.player.InventoryPlayer;

/**
 * The main purpose of creating this class is to solve
 * the problem regarding the CraftingHelper not being executed correctly.
 */
public class ConfirmWrapperGui extends GuiCraftConfirm {

    public ConfirmWrapperGui(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        IngredientRequester.getInstance().requestNext();
    }
}
