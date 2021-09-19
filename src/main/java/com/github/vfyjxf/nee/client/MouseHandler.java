package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class MouseHandler {
    public static final KeyBinding recipeIngredientChange = new KeyBinding("key.neenergistics.recipe.ingredient.change", KeyConflictContext.GUI, Keyboard.KEY_LSHIFT, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding stackCountChange = new KeyBinding("key.neenergistics.stack.count.change", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.NotEnoughEnergistics");

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
                    GuiUtils.handleRecipeIngredientChange(currentSlot, i);
                } else if (Keyboard.isKeyDown(stackCountChange.getKeyCode())) {
                    int changeCount = i / 120;
                    NEENetworkHandler.getInstance().sendToServer(new PacketStackCountChange(currentSlot.slotNumber, changeCount));
                }
            }
        }
    }

}
