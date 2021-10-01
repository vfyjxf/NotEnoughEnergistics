package com.github.vfyjxf.nee.utils;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.me.ItemRepo;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IngredientTracker {

    private List<IAEItemStack> craftbleAEItemStacks;
    private List<ItemStack> requireToCraftStacks = new ArrayList<>();

    public IngredientTracker(GuiCraftingTerm gui) {
        this.craftbleAEItemStacks = getCraftableStacks(gui);
    }


    private List<IAEItemStack> getCraftableStacks(GuiCraftingTerm gui) {
        List<IAEItemStack> craftableAEItemSafcks = new ArrayList<>();
        ArrayList<IAEItemStack> view = null;
        try {
            ItemRepo repo = (ItemRepo) ReflectionHelper.findField(GuiMEMonitorable.class, "repo").get(gui);
            view = (ArrayList<IAEItemStack>) ReflectionHelper.findField(ItemRepo.class, "view").get(repo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (view != null) {
            for (IAEItemStack stack : view) {
                if (stack.isCraftable()) {
                    craftableAEItemSafcks.add(stack);
                }
            }
        }
        return craftableAEItemSafcks;
    }

    public boolean hasCraftableStack(ItemStack stack) {
        for (IAEItemStack aeItemStack : this.craftbleAEItemStacks) {
            if (aeItemStack.isSameType(stack)) {
                return true;
            }
        }
        return false;
    }

    public List<IAEItemStack> getCraftbleAEItemStacks() {
        return this.craftbleAEItemStacks;
    }

    public List<ItemStack> getRequireToCraftStacks() {
        return this.requireToCraftStacks;
    }


    public void addRequireToCraftStack(ItemStack requiredStack) {
        for (IAEItemStack craftableStack : this.getCraftbleAEItemStacks()) {
            if (craftableStack.isSameType(requiredStack)) {
                int requiredCount = (int) (requiredStack.stackSize - craftableStack.getStackSize());
                if (requiredCount > 0) {
                    requiredStack.stackSize = requiredCount;
                    this.requireToCraftStacks.add(requiredStack);
                }
            }
        }
    }

}
