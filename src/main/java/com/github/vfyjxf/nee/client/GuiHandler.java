package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import codechicken.nei.NEIClientConfig;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingHelper;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static com.github.vfyjxf.nee.nei.NEECraftingHelper.*;

public class GuiHandler {

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            int dWheel = Mouse.getDWheel();
            boolean isPatternTerm = mc.currentScreen instanceof GuiPatternTerm || GuiUtils.isPatternTermExGui(mc.currentScreen);
            if (dWheel != 0 && isPatternTerm) {
                int x = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
                int y = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
                Slot currentSlot = GuiUtils.getSlotUnderMouse((GuiContainer) mc.currentScreen, x, y);
                if (currentSlot instanceof SlotFake && currentSlot.getHasStack()) {
                    //try to change current itemstack to next ingredient;
                    if (Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.ingredient")) && GuiUtils.isCraftingSlot(currentSlot)) {
                        GuiUtils.handleRecipeIngredientChange(currentSlot, dWheel);
                    } else if (Keyboard.isKeyDown(NEIClientConfig.getKeyBinding("nee.count"))) {
                        int changeCount = dWheel / 120;
                        NEENetworkHandler.getInstance().sendToServer(new PacketStackCountChange(currentSlot.slotNumber, changeCount));
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
            if (tracker.getRequireToCraftStacks().size() > 1 && stackIndex < tracker.getRequireToCraftStacks().size()) {
                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingHelper(tracker.getRequireToCraftStacks().get(stackIndex), noPreview));
                stackIndex++;
            }
        }
    }

}
