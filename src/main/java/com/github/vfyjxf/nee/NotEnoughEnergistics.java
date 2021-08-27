package com.github.vfyjxf.nee;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


@Mod(modid = NotEnoughEnergistics.MODID,
        version = NotEnoughEnergistics.VERSION,
        name = NotEnoughEnergistics.NAME,
        dependencies = NotEnoughEnergistics.DEPENDENCIES,
        useMetadata = true)
public class NotEnoughEnergistics {
    public static final String MODID = "neenergistics";
    public static final String NAME = "NotEnoughEnergistics";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "required-after:jei;required-after:appliedenergistics2";
    public static final Logger logger = LogManager.getLogger("NotEnoughEnergistics");

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        NEENetworkHandler.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

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
                int changeCount = GuiContainer.isCtrlKeyDown() ? i / 60 : i / 120;
                NEENetworkHandler.getInstance().sendToServer(new PacketStackCountChange(currentSlot.slotNumber, changeCount));
            }
        }
    }
}
