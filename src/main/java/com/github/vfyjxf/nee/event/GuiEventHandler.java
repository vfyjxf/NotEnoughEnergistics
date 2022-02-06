package com.github.vfyjxf.nee.event;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiInterface;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.client.KeyBindings;
import com.github.vfyjxf.nee.client.gui.widgets.GuiImgButtonEnableCombination;
import com.github.vfyjxf.nee.config.ItemCombination;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.container.ContainerCraftingConfirm;
import com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketSlotStackChange;
import com.github.vfyjxf.nee.network.packet.PacketStackSizeChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.github.vfyjxf.nee.utils.ModIds;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.tracker;
import static com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler.INPUT_KEY;

public class GuiEventHandler {

    private GuiImgButtonEnableCombination buttonCombination;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen old = Minecraft.getMinecraft().currentScreen;
        GuiScreen next = event.getGui();
        if (old != null) {
            if (GuiUtils.isGuiCraftConfirm(old) && isContainerCraftConfirm(((GuiContainer) old).inventorySlots)) {
                if (tracker != null) {
                    if (GuiUtils.isGuiCraftingTerm(next)) {
                        if (tracker.hasNext()) {
                            tracker.requestNextIngredient();
                        } else {
                            tracker = null;
                        }
                    } else {
                        if (tracker != null) {
                            tracker = null;
                        }
                    }
                }
            }
        }

    }

    @SubscribeEvent
    public void onCraftConfirmActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {

        if (tracker != null) {
            if (event.getGui() instanceof GuiCraftConfirm) {
                if (getCancelButton((GuiCraftConfirm) event.getGui()) == event.getButton()) {
                    tracker = null;
                }
            }

            if (GuiUtils.isWirelessGuiCraftConfirm(event.getGui())) {
                if (getCancelButton((p455w0rd.wct.client.gui.GuiCraftConfirm) event.getGui()) == event.getButton()) {
                    tracker = null;
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean isSupportedGui = mc.currentScreen instanceof GuiPatternTerm || mc.currentScreen instanceof GuiInterface;
        if (isSupportedGui) {
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                AEBaseGui aeBaseGui = (AEBaseGui) mc.currentScreen;
                Slot currentSlot = aeBaseGui.getSlotUnderMouse();
                if (currentSlot != null && currentSlot.getHasStack()) {
                    if (currentSlot instanceof SlotFake) {
                        if (Keyboard.isKeyDown(KeyBindings.recipeIngredientChange.getKeyCode()) && GuiUtils.isCraftingSlot(currentSlot)) {
                            handleRecipeIngredientChange((GuiContainer) mc.currentScreen, currentSlot, dWheel);
                            event.setCanceled(true);
                        } else if (Keyboard.isKeyDown(KeyBindings.stackCountChange.getKeyCode())) {
                            int changeCount = dWheel / 120;
                            NEENetworkHandler.getInstance().sendToServer(new PacketStackSizeChange(currentSlot.slotNumber, changeCount));
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    private void handleRecipeIngredientChange(GuiContainer gui, Slot currentSlot, int dWheel) {
        IGuiIngredient<ItemStack> ingredient = PatternRecipeTransferHandler.ingredients.get(INPUT_KEY + currentSlot.getSlotIndex());
        List<Integer> craftingSlots = new ArrayList<>();
        if (ingredient != null && ingredient.getDisplayedIngredient() != null) {
            List<ItemStack> currentIngredients = ingredient.getAllIngredients();
            int currentStackIndex = ItemUtils.getIngredientIndex(currentSlot.getStack(), currentIngredients);
            if (currentStackIndex >= 0) {
                int nextStackIndex = dWheel / 120;
                for (int i = 0; i < Math.abs(nextStackIndex); i++) {
                    currentStackIndex = nextStackIndex > 0 ? currentStackIndex + 1 : currentStackIndex - 1;
                    if (currentStackIndex >= currentIngredients.size()) {
                        currentStackIndex = 0;
                    } else if (currentStackIndex < 0) {
                        currentStackIndex = currentIngredients.size() - 1;
                    }
                    ItemStack currentIngredientStack = currentIngredients.get(currentStackIndex).copy();
                    currentIngredientStack.setCount(currentSlot.getStack().getCount());

                    if (NEEConfig.allowSynchronousSwitchIngredient) {
                        for (Slot slot : getCraftingSlots(gui)) {
                            IGuiIngredient<ItemStack> slotIngredients = PatternRecipeTransferHandler.ingredients.get(INPUT_KEY + slot.getSlotIndex());
                            boolean areItemStackEqual = currentSlot.getHasStack() &&
                                    slot.getHasStack() &&
                                    currentSlot.getStack().isItemEqual(slot.getStack()) &&
                                    ItemStack.areItemStackTagsEqual(currentSlot.getStack(), slot.getStack());

                            boolean areIngredientEqual = slotIngredients != null &&
                                    slotIngredients.getAllIngredients().size() > 0 &&
                                    ItemUtils.getIngredientIndex(slotIngredients.getAllIngredients().get(0), currentIngredients) >= 0;

                            if (areItemStackEqual && areIngredientEqual) {
                                craftingSlots.add(slot.slotNumber);
                            }
                        }
                    } else {
                        craftingSlots.add(currentSlot.slotNumber);
                    }

                    NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackChange(currentIngredientStack, craftingSlots));
                }
            }
        }
    }

    private List<Slot> getCraftingSlots(GuiContainer gui) {
        List<Slot> craftingSlots = new ArrayList<>();
        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (GuiUtils.isCraftingSlot(slot)) {
                craftingSlots.add(slot);
            }
        }
        return craftingSlots;
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiPatternTerm) {
            GuiPatternTerm gui = (GuiPatternTerm) event.getGui();
            buttonCombination = new GuiImgButtonEnableCombination(gui.guiLeft + 84, gui.guiTop + gui.ySize - 163, ItemCombination.valueOf(NEEConfig.itemCombinationMode));
            event.getButtonList().add(buttonCombination);
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getButton() instanceof GuiImgButtonEnableCombination) {
            GuiImgButtonEnableCombination button = (GuiImgButtonEnableCombination) event.getButton();
            int ordinal = Mouse.getEventButton() != 2 ? button.getCurrentValue().ordinal() + 1 : button.getCurrentValue().ordinal() - 1;

            if (ordinal >= ItemCombination.values().length) {
                ordinal = 0;
            }
            if (ordinal < 0) {
                ordinal = ItemCombination.values().length - 1;
            }
            button.setValue(ItemCombination.values()[ordinal]);
            NEEConfig.setItemCombinationMode(ItemCombination.values()[ordinal].name());
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiPatternTerm) {
            ContainerPatternTerm container = (ContainerPatternTerm) ((GuiPatternTerm) event.getGui()).inventorySlots;
            if (container.isCraftingMode()) {
                buttonCombination.enabled = false;
                buttonCombination.visible = false;
            } else {
                buttonCombination.enabled = true;
                buttonCombination.visible = true;
            }
        }
    }

    private boolean isContainerCraftConfirm(Container container) {
        return (container instanceof ContainerCraftConfirm || GuiUtils.isContainerWirelessCraftingConfirm(container)) &&
                !((container instanceof ContainerCraftingConfirm) || (GuiUtils.isWCTContainerCraftingConfirm(container)));
    }

    private GuiButton getCancelButton(GuiCraftConfirm gui) {
        return ObfuscationReflectionHelper.getPrivateValue(GuiCraftConfirm.class, gui, "cancel");
    }

    @Optional.Method(modid = ModIds.WCT)
    private GuiButton getCancelButton(p455w0rd.wct.client.gui.GuiCraftConfirm gui) {
        return ObfuscationReflectionHelper.getPrivateValue(p455w0rd.wct.client.gui.GuiCraftConfirm.class, gui, "cancel");
    }


}
