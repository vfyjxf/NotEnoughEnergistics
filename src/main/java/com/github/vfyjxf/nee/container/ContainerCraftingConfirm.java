package com.github.vfyjxf.nee.container;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.util.Platform;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerCraftingConfirm extends ContainerCraftConfirm {

    private TilePatternInterface tile;
    private int patternIndex;
    private boolean hasWorkCommitted = false;

    public ContainerCraftingConfirm(InventoryPlayer ip, ITerminalHost te) {
        super(ip, te);
    }

    @Override
    public void startJob() {
        if (Platform.isServer() && tile != null) {
            hasWorkCommitted = true;
        }
        super.startJob();
    }

    @Override
    public void onContainerClosed(EntityPlayer entityPlayer) {
        super.onContainerClosed(entityPlayer);
        if (Platform.isServer() && tile != null && !hasWorkCommitted) {
            tile.getPatternInventory().setStackInSlot(patternIndex, ItemStack.EMPTY);
            tile.updateCraftingList();
        }
    }

    public void setTile(TilePatternInterface tile) {
        this.tile = tile;
    }

    public void setPatternIndex(int patternIndex) {
        this.patternIndex = patternIndex;
    }
}
