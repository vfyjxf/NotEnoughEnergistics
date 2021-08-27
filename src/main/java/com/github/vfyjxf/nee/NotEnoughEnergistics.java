package com.github.vfyjxf.nee;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketStackCountChange;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

import java.io.File;


@Mod(modid = NotEnoughEnergistics.MODID,
        version = NotEnoughEnergistics.VERSION,
        name = NotEnoughEnergistics.NAME,
        dependencies = NotEnoughEnergistics.DEPENDENCIES,
        useMetadata = true)
public class NotEnoughEnergistics {
    public static final String MODID = "neenergistics";
    public static final String NAME = "NotEnoughEnergistics";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "required-after:NotEnoughItems;required-after:appliedenergistics2";
    public static final Logger logger = LogManager.getLogger("NotEnoughEnergistics");

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        NEENetworkHandler.init();
        NEEConfig.loadConfig(new File(Launch.minecraftHome, "config/NotEnoughEnergistics.cfg"));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new NEECommands());
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            int i = Mouse.getDWheel();
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

}
