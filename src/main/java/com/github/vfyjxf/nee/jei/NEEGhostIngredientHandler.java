package com.github.vfyjxf.nee.jei;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotFake;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.util.AEFluidStack;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketSlotStackSwitch;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NEEGhostIngredientHandler implements IGhostIngredientHandler<AEBaseGui> {

    @Nonnull
    @Override
    public <I> List<Target<I>> getTargets(@Nonnull AEBaseGui gui, @Nonnull I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        if (ingredient instanceof ItemStack) {
            if (gui instanceof GuiPatternTerm) {
                if (GuiContainer.isShiftKeyDown()) {
                    addItemStackTargets(gui, targets);
                }
            } else {
                addItemStackTargets(gui, targets);
            }
        }
        if (ingredient instanceof FluidStack) {
            addFluidStackTargets(gui, targets);
        }
        return targets;
    }

    @Override
    public void onComplete() {

    }

    @SuppressWarnings("unchecked")
    private static <I> void addItemStackTargets(AEBaseGui gui, List<Target<I>> targets) {
        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (slot instanceof AppEngSlot) {
                AppEngSlot aeSlot = (AppEngSlot) slot;
                if (aeSlot.isSlotEnabled()) {
                    if (aeSlot instanceof SlotFake) {
                        targets.add((Target<I>) new ItemSlotTarget(gui, aeSlot));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <I> void addFluidStackTargets(AEBaseGui gui, List<Target<I>> targets) {
        for (GuiCustomSlot slot : getGuiSlots(gui)) {
            if (slot.isSlotEnabled() && slot instanceof GuiFluidSlot) {
                targets.add((Target<I>) new FluidSlotTarget(gui, (GuiFluidSlot) slot));
            }
        }
    }

    private static class ItemSlotTarget implements Target<ItemStack> {
        private final AppEngSlot slot;
        private final Rectangle rectangle;

        public ItemSlotTarget(AEBaseGui gui, AppEngSlot slot) {
            this.slot = slot;
            this.rectangle = new Rectangle(gui.getGuiLeft() + slot.xPos, gui.getGuiTop() + slot.yPos, 16, 16);
        }

        @Nonnull
        @Override
        public Rectangle getArea() {
            return this.rectangle;
        }

        @Override
        public void accept(@Nonnull ItemStack ingredient) {
            List<Integer> slots = new ArrayList<>();
            slots.add(this.slot.slotNumber);
            NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackSwitch(ingredient, slots));
        }
    }

    private static class FluidSlotTarget implements Target<FluidStack> {

        private final GuiFluidSlot slot;
        private final Rectangle rectangle;

        private FluidSlotTarget(AEBaseGui gui, GuiFluidSlot slot) {
            this.slot = slot;
            this.rectangle = new Rectangle(slot.xPos() + gui.getGuiLeft(), slot.yPos() + gui.getGuiTop(), slot.getWidth(), slot.getHeight());
        }

        @Nonnull
        @Override
        public Rectangle getArea() {
            return this.rectangle;
        }

        @Override
        public void accept(@Nonnull FluidStack ingredient) {
            slot.setFluidStack(AEFluidStack.fromFluidStack(ingredient));
        }

    }

    private static List<GuiCustomSlot> getGuiSlots(AEBaseGui gui) {
        return new ArrayList<>(ObfuscationReflectionHelper.getPrivateValue(AEBaseGui.class, gui, "guiSlots"));
    }

}
