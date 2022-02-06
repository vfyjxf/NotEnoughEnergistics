package com.github.vfyjxf.nee.container;

import appeng.api.storage.ITerminalHost;
import appeng.util.Platform;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import p455w0rd.wct.container.ContainerCraftConfirm;

public class WCTContainerCraftingConfirm extends ContainerCraftConfirm {

    private TilePatternInterface tile;
    private int patternIndex;
    private boolean hasWorkCommitted = false;

    public WCTContainerCraftingConfirm(InventoryPlayer ip, ITerminalHost te, boolean isBauble, int wctSlot) {
        super(ip, te, isBauble, wctSlot);
    }

    @Override
    public void startJob() {
        super.startJob();
        if (Platform.isServer() && this.tile != null) {
            hasWorkCommitted = true;
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer entityPlayer) {
        super.onContainerClosed(entityPlayer);
        if (Platform.isServer() && this.tile != null && !hasWorkCommitted) {
            this.tile.getPatternInventory().setStackInSlot(patternIndex, ItemStack.EMPTY);
        }
    }

    public void setTile(TilePatternInterface tile) {
        this.tile = tile;
    }

    public void setPatternIndex(int patternIndex) {
        this.patternIndex = patternIndex;
    }

}
