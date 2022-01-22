package com.github.vfyjxf.nee.client;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiInterface;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import appeng.util.item.AEItemStack;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingRequest;
import com.github.vfyjxf.nee.network.packet.PacketSlotStackChange;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ItemUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.config.NEEConfig.draggedStackDefaultSize;
import static com.github.vfyjxf.nee.config.NEEConfig.useStackSizeFromNEI;
import static com.github.vfyjxf.nee.nei.NEECraftingHelper.*;

public class GuiHandler implements INEIGuiHandler {

    public static GuiHandler instance = new GuiHandler();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            boolean isPatternTerm = mc.currentScreen instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(mc.currentScreen);
            boolean isInterface = mc.currentScreen instanceof GuiInterface;
            if (isPatternTerm || isInterface) {
                int dWheel = Mouse.getDWheel();
                if (dWheel != 0) {
                    int x = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
                    int y = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
                    Slot currentSlot = ((GuiContainer) mc.currentScreen).getSlotAtPosition(x, y);
                    if (currentSlot instanceof SlotFake && currentSlot.getHasStack()) {
                        //try to change current itemstack to next ingredient;
                        if (Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.ingredient")) && GuiUtils.isCraftingSlot(currentSlot)) {
                            handleRecipeIngredientChange((GuiContainer) mc.currentScreen, currentSlot, dWheel);
                        } else if (Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.count"))) {
                            int changeCount = dWheel / 120;
                            NEENetworkHandler.getInstance().sendToServer(new PacketStackCountChange(currentSlot.slotNumber, changeCount));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        boolean isGuiCraftingTerm = event.gui instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(event.gui);
        boolean isGuiCraftConfirm = currentScreen instanceof GuiCraftConfirm || GuiUtils.isWirelessGuiCraftConfirm(currentScreen);
        if (isGuiCraftingTerm && isGuiCraftConfirm && tracker != null) {
            if (!tracker.getRequireStacks().isEmpty() && stackIndex < tracker.getRequireStacks().size()) {
                IAEItemStack stack = AEItemStack.create(tracker.getRequireStacks().get(stackIndex));
                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(stack, noPreview));
                stackIndex++;
            }
        } else if (!isGuiCraftingTerm && isGuiCraftConfirm && tracker != null) {
            //Prevent opening the gui during normal use
            tracker = null;
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
        if (gui instanceof AEBaseGui) {
            if (button != 2 && draggedStack != null) {
                Slot currentSlot = gui.getSlotAtPosition(mouseX, mouseY);
                if (currentSlot instanceof SlotFake && ((SlotFake) currentSlot).isEnabled()) {
                    List<Integer> slots = new ArrayList<>();
                    slots.add(currentSlot.slotNumber);
                    ItemStack copyStack = draggedStack.copy();
                    copyStack.stackSize = useStackSizeFromNEI ? draggedStack.stackSize : draggedStackDefaultSize;
                    NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackChange(copyStack, slots));
                    draggedStack.stackSize = 0;
                    return true;
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
