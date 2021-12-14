package com.github.vfyjxf.nee.event.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.PatternTermScreen;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.FakeSlot;
import appeng.helpers.IContainerCraftingPacket;
import com.github.vfyjxf.nee.client.KeyBindings;
import com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packets.PacketCraftingRequest;
import com.github.vfyjxf.nee.network.packets.PacketSlotStackChange;
import com.github.vfyjxf.nee.network.packets.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.ItemUtils;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.client.KeyBindings.*;
import static com.github.vfyjxf.nee.client.KeyBindings.recipeIngredientChange;
import static com.github.vfyjxf.nee.client.KeyBindings.stackCountChange;
import static com.github.vfyjxf.nee.config.NEEConfig.CLIENT_CONFIG;
import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.*;
import static net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.MouseScrollEvent;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

/**
 * @author vfyjxf
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = FORGE)
public class GuiEventHandler {

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof CraftConfirmScreen && tracker != null) {
            if (event.getGui() instanceof CraftingTermScreen) {
                boolean hasNext = tracker.getRequireToCraftStacks().size() > 1 && stackIndex < tracker.getRequireToCraftStacks().size();
                if (hasNext) {
                    NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(tracker.getRequireToCraftStacks().get(stackIndex), autoStart));
                    stackIndex++;
                    event.setCanceled(true);
                } else {
                    //clean the tracker when all ingredients were requested
                    tracker = null;
                    stackIndex = 1;
                }
            } else {
                //clean the track during normal use
                tracker = null;
                stackIndex = 1;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(MouseScrollEvent.Pre event) {
        Screen currentScreen = Minecraft.getInstance().screen;
        boolean isSupportedGui = currentScreen instanceof PatternTermScreen || currentScreen instanceof InterfaceScreen;
        if (isSupportedGui) {
            AEBaseScreen<?> aeScreen = (AEBaseScreen<?>) currentScreen;
            int scrollDelta = (int) event.getScrollDelta();
            Slot currentSlot = aeScreen.getSlotUnderMouse();
            if (currentSlot instanceof FakeSlot && currentSlot.hasItem()) {
                if (isCraftingSlot(currentSlot) && isKeyDown(recipeIngredientChange)) {
                    handleRecipeIngredientChange(aeScreen, currentSlot, scrollDelta);
                    event.setCanceled(true);
                } else if (isKeyDown(stackCountChange)) {
                    NEENetworkHandler.getInstance().sendToServer(new PacketStackCountChange(currentSlot.index, scrollDelta));
                    event.setCanceled(true);
                }
            }

        }
    }

    private static void handleRecipeIngredientChange(ContainerScreen<?> gui, Slot currentSlot, int scrollDelta) {
        IGuiIngredient<ItemStack> ingredient = PatternRecipeTransferHandler.ingredients.get("input" + currentSlot.getSlotIndex());
        List<Integer> craftingSlots = new ArrayList<>();
        if (ingredient != null && ingredient.getDisplayedIngredient() != null) {
            List<ItemStack> currentIngredients = ingredient.getAllIngredients();
            int currentStackIndex = ItemUtils.getIngredientIndex(currentSlot.getItem(), currentIngredients);
            for (int i = 0; i < Math.abs(scrollDelta); i++) {
                int nextStackIndex = scrollDelta > 0 ? currentStackIndex + 1 : currentStackIndex - 1;
                if (nextStackIndex >= currentIngredients.size()) {
                    nextStackIndex = 0;
                } else if (nextStackIndex < 0) {
                    nextStackIndex = currentIngredients.size() - 1;
                }
                ItemStack currentIngredientStack = currentIngredients.get(nextStackIndex).copy();
                currentIngredientStack.setCount(currentSlot.getItem().getCount());

                if (CLIENT_CONFIG.allowSynchronousSwitchIngredient()) {
                    for (Slot slot : getCraftingSlots(gui)) {
                        IGuiIngredient<ItemStack> slotIngredients = PatternRecipeTransferHandler.ingredients.get("input" + slot.getSlotIndex());
                        boolean areItemStackEqual = currentSlot.hasItem() &&
                                slot.hasItem() &&
                                currentSlot.getItem().sameItem(slot.getItem()) &&
                                ItemStack.tagMatches(currentSlot.getItem(), slot.getItem());

                        boolean areIngredientEqual = slotIngredients != null &&
                                slotIngredients.getAllIngredients().size() > 0 &&
                                ItemUtils.getIngredientIndex(slotIngredients.getAllIngredients().get(0), currentIngredients) >= 0;

                        if (areItemStackEqual && areIngredientEqual) {
                            craftingSlots.add(slot.index);
                        }
                    }
                } else {
                    craftingSlots.add(currentSlot.index);
                }
                NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackChange(currentIngredientStack, craftingSlots));
            }

        }

    }

    private static List<Slot> getCraftingSlots(ContainerScreen<?> gui) {
        List<Slot> craftingSlots = new ArrayList<>();
        for (Slot slot : gui.getMenu().slots) {
            if (isCraftingSlot(slot)) {
                craftingSlots.add(slot);
            }
        }
        return craftingSlots;
    }

    private static boolean isCraftingSlot(Slot slot) {
        if (Minecraft.getInstance().player != null) {
            Container container = Minecraft.getInstance().player.containerMenu;
            if (container instanceof IContainerCraftingPacket && slot instanceof AppEngSlot) {
                IItemHandler craftMatrix = ((IContainerCraftingPacket) container).getInventoryByName("crafting");
                return ((AppEngSlot) slot).getItemHandler().equals(craftMatrix);
            }
        }
        return false;
    }

}
