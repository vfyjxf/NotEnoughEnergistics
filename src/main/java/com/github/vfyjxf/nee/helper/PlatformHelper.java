package com.github.vfyjxf.nee.helper;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.utils.Globals;
import com.github.vfyjxf.nee.utils.ReflectionHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.Loader;

/**
 * A helper to adapt official ae2 and pae2.
 */
public class PlatformHelper {

    private PlatformHelper() {

    }

    private static final boolean IS_UNOFFICIAL;
    private static Class<?> containerPatternEncoderClass = null;
    private static Class<?> containerPatternTermClass = ContainerPatternTerm.class;
    private static Class<?> guiWCTClass = null;
    private static Class<?> guiMEMonitorable = GuiMEMonitorable.class;
    private static Class<?> wirelessCraftingTermClass = null;
    private static Class<?> wirelessCraftingGuiClass = null;


    static {
        IS_UNOFFICIAL = Loader.instance()
                .getModList()
                .stream()
                .anyMatch(modContainer -> (modContainer.getModId().equals(Globals.APPENG) && modContainer.getVersion().contains("extended_life")));
        try {
            containerPatternEncoderClass = Class.forName("appeng.container.implementations.ContainerPatternEncoder");
            guiWCTClass = Class.forName("p455w0rd.wct.client.gui.GuiWCT");
            wirelessCraftingTermClass = Class.forName("appeng.container.implementations.ContainerWirelessCraftingTerminal");
            wirelessCraftingGuiClass = Class.forName("appeng.client.gui.implementations.GuiWirelessCraftingTerminal");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static boolean issUnofficial() {
        return IS_UNOFFICIAL;
    }

    public static boolean isCraftingMode(Container container) {
        return Boolean.TRUE.equals(ReflectionHelper.compositeInvoker(Lists.newArrayList(containerPatternEncoderClass, containerPatternTermClass), container, false, "isCraftingMode"));
    }

    public static ItemRepo getRepo(GuiContainer gui) {
        return ReflectionHelper.possibleValueGetter(Lists.newArrayList(guiWCTClass, guiMEMonitorable), gui, "repo");
    }

    public static boolean isWirelessContainer(Container container) {
        return wirelessCraftingTermClass != null && wirelessCraftingTermClass.isInstance(container);
    }

    public static boolean isWirelessGui(GuiScreen gui) {
        return wirelessCraftingGuiClass != null && wirelessCraftingGuiClass.isInstance(gui);
    }

}
