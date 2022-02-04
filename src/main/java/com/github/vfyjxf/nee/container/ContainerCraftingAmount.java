package com.github.vfyjxf.nee.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotInaccessible;
import appeng.me.helpers.PlayerSource;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ContainerCraftingAmount extends AEBaseContainer {

    private final Slot resultSlot;
    private ItemStack resultStack;
    private boolean isBauble;
    private int wctSlot = -1;
    private NBTTagCompound recipe;

    public ContainerCraftingAmount(InventoryPlayer ip, Object anchor) {
        super(ip, anchor);
        this.resultSlot = new SlotInaccessible(new AppEngInternalInventory(null, 1), 0, 34, 53);
        this.addSlotToContainer(resultSlot);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public IGrid getGrid() {
        final IActionHost h = ((IActionHost) this.getTarget());
        return h.getActionableNode().getGrid();
    }

    public World getWorld() {
        return this.getPlayerInv().player.world;
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInv().player, (IActionHost) this.getTarget());
    }

    public Slot getResultSlot() {
        return resultSlot;
    }

    public ItemStack getResultStack() {
        if (resultStack == null) {
            return ItemStack.EMPTY;
        }
        return resultStack;
    }

    public NBTTagCompound getRecipe() {
        return recipe;
    }

    public boolean isBauble() {
        return isBauble;
    }

    public int getWctSlot() {
        return wctSlot;
    }

    public void setBauble(boolean bauble) {
        isBauble = bauble;
    }

    public void setWctSlot(int wctSlot) {
        this.wctSlot = wctSlot;
    }

    public boolean isWirelessTerm() {
        return this.wctSlot >= 0;
    }

    public void setRecipe(NBTTagCompound recipe) {
        this.recipe = recipe;
    }

    public void setResultStack(ItemStack resultStack) {
        this.resultStack = resultStack;
    }
}
