package com.github.vfyjxf.nee.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFake;
import codechicken.nei.PositionedStack;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import com.github.vfyjxf.nee.config.ItemCombination;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.gui.widgets.GuiImgButtonEnableCombination;
import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketSlotStackChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ItemUtils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.config.NEEConfig.draggedStackDefaultSize;
import static com.github.vfyjxf.nee.config.NEEConfig.useStackSizeFromNEI;

public class GuiEventHandler implements INEIGuiHandler {

    public static GuiEventHandler instance = new GuiEventHandler();

    private GuiImgButtonEnableCombination buttonCombination;
    private boolean hasDoubleBtn = true;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiPatternTerm) {
            GuiPatternTerm gui = (GuiPatternTerm) event.gui;
            try {
                GuiPatternTerm.class.getDeclaredField("doubleBtn");
            } catch (NoSuchFieldException e) {
                hasDoubleBtn = false;
            }
            if (hasDoubleBtn) {
                buttonCombination = new GuiImgButtonEnableCombination(gui.guiLeft + 84, gui.guiTop + gui.ySize - 153, ItemCombination.valueOf(NEEConfig.itemCombinationMode));
            } else {
                buttonCombination = new GuiImgButtonEnableCombination(gui.guiLeft + 74, gui.guiTop + gui.ySize - 153, ItemCombination.valueOf(NEEConfig.itemCombinationMode));

            }
            event.buttonList.add(buttonCombination);
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.button == this.buttonCombination) {
            GuiImgButtonEnableCombination button = (GuiImgButtonEnableCombination) event.button;
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
        if (event.gui instanceof GuiPatternTerm) {
            ContainerPatternTerm container = (ContainerPatternTerm) ((GuiPatternTerm) event.gui).inventorySlots;
            if (container.isCraftingMode()) {
                buttonCombination.enabled = false;
                buttonCombination.visible = false;
            } else {
                buttonCombination.enabled = true;
                buttonCombination.visible = true;
            }
        }
    }

    private void handleRecipeIngredientChange(GuiContainer gui, Slot currentSlot, int dWheel) {
        List<Integer> craftingSlots = new ArrayList<>();
        int currentSlotIndex = currentSlot.getSlotIndex();
        PositionedStack currentIngredients = NEECraftingHandler.ingredients.get("input" + currentSlotIndex);
        if (currentIngredients != null && currentIngredients.items.length > 1) {
            int currentStackIndex = ItemUtils.getIngredientIndex(currentSlot.getStack(), currentIngredients);
            if (currentStackIndex >= 0) {
                int nextStackIndex = dWheel / 120;
                for (int j = 0; j < Math.abs(nextStackIndex); j++) {
                    currentStackIndex = nextStackIndex > 0 ? currentStackIndex + 1 : currentStackIndex - 1;
                    if (currentStackIndex >= currentIngredients.items.length) {
                        currentStackIndex = 0;
                    } else if (currentStackIndex < 0) {
                        currentStackIndex = currentIngredients.items.length - 1;
                    }

                    ItemStack currentStack = currentIngredients.items[currentStackIndex].copy();
                    currentStack.stackSize = currentSlot.getStack().stackSize;

                    if (NEEConfig.allowSynchronousSwitchIngredient) {
                        for (Slot slot : getCraftingSlots(gui)) {

                            PositionedStack slotIngredients = NEECraftingHandler.ingredients.get("input" + slot.getSlotIndex());

                            boolean areItemStackEqual = currentSlot.getHasStack() &&
                                    slot.getHasStack() &&
                                    currentSlot.getStack().isItemEqual(slot.getStack()) &&
                                    ItemStack.areItemStackTagsEqual(currentSlot.getStack(), slot.getStack());

                            boolean areIngredientEqual = slotIngredients != null && currentIngredients.contains(slotIngredients.items[0]);

                            if (areItemStackEqual && areIngredientEqual) {
                                craftingSlots.add(slot.slotNumber);
                            }

                        }
                    } else {
                        craftingSlots.add(currentSlot.slotNumber);
                    }
                    NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackChange(currentStack, craftingSlots));
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private List<Slot> getCraftingSlots(GuiContainer gui) {
        List<Slot> craftingSlots = new ArrayList<>();
        for (Slot slot : (List<Slot>) gui.inventorySlots.inventorySlots) {
            if (GuiUtils.isCraftingSlot(slot)) {
                craftingSlots.add(slot);
            }
        }
        return craftingSlots;
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return null;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return null;
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        //When NEIAddons exist, give them to NEIAddons to handle
        if(NEEConfig.enableNEIDragNDrop || !Loader.isModLoaded("NEIAddons")) {
            if (gui instanceof AEBaseGui) {
                if (button != 2 && draggedStack != null) {
                    Slot currentSlot = gui.getSlotAtPosition(mouseX, mouseY);
                    if (currentSlot instanceof SlotFake && ((SlotFake) currentSlot).isEnabled()) {
                        List<Integer> slots = new ArrayList<>();
                        slots.add(currentSlot.slotNumber);
                        ItemStack copyStack = draggedStack.copy();
                        copyStack.stackSize = useStackSizeFromNEI ? draggedStack.stackSize : draggedStackDefaultSize;
                        NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackChange(copyStack, slots));
                        if (!NEEConfig.keepGhostitems) {
                            draggedStack.stackSize = 0;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return false;
    }
}

