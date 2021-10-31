package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeItemChange;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ItemUtils;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class MouseHandler {
    public static final KeyBinding recipeIngredientChange = new KeyBinding("key.neenergistics.recipe.ingredient.change", KeyConflictContext.GUI, Keyboard.KEY_LSHIFT, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding stackCountChange = new KeyBinding("key.neenergistics.stack.count.change", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding craftingHelperPreview = new KeyBinding("key.neenergistics.crafting.helper.preview", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding craftingHelperNoPreview = new KeyBinding("key.neenergistics.crafting.helper.noPreview", KeyConflictContext.GUI, Keyboard.KEY_LMENU, "neenergistics.NotEnoughEnergistics");

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        int i = Mouse.getEventDWheel();
        if (i != 0 && mc.currentScreen instanceof GuiPatternTerm) {
            GuiPatternTerm guiPatternTerm = (GuiPatternTerm) mc.currentScreen;
            int x = Mouse.getEventX() * guiPatternTerm.width / mc.displayWidth;
            int y = guiPatternTerm.height - Mouse.getEventY() * guiPatternTerm.height / mc.displayHeight - 1;
            Slot currentSlot = GuiUtils.getSlotUnderMouse(guiPatternTerm, x, y);
            if (currentSlot instanceof SlotFake && currentSlot.getHasStack()) {
                if (Keyboard.isKeyDown(recipeIngredientChange.getKeyCode()) && GuiUtils.isCraftingSlot(currentSlot)) {
                    handleRecipeIngredientChange((GuiContainer) mc.currentScreen, currentSlot, i);
                } else if (Keyboard.isKeyDown(stackCountChange.getKeyCode())) {
                    int changeCount = i / 120;
                    NEENetworkHandler.getInstance().sendToServer(new PacketStackCountChange(currentSlot.slotNumber, changeCount));
                }
            }
        }
    }

    private void handleRecipeIngredientChange(GuiContainer gui, Slot currentSlot, int dWheel) {
        IGuiIngredient<ItemStack> ingredient = PatternRecipeTransferHandler.ingredients.get("input" + currentSlot.getSlotIndex());
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
                            IGuiIngredient<ItemStack> slotIngredients = PatternRecipeTransferHandler.ingredients.get("input" + slot.getSlotIndex());
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

                    NEENetworkHandler.getInstance().sendToServer(new PacketRecipeItemChange(currentIngredientStack, craftingSlots));
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

}
