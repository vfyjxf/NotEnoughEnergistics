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

    private static boolean unofficial;
    public static Class<?> containerPatternEncoderClass = null;
    public static Class<?> containerPatternTermClass = ContainerPatternTerm.class;
    public static Class<?> guiWCTClass = null;

    public static Class<?> guiMEMonitorable = GuiMEMonitorable.class;
    public static Class<?> containerWirelessCraftingTermClass = null;
    public static Class<?> wirelessCraftingGuiClass = null;

    public static void init() {
        unofficial = Loader.instance()
                .getModList()
                .stream()
                .anyMatch(modContainer -> (modContainer.getModId().equals(Globals.APPENG) && modContainer.getVersion().contains("extended_life")));
        containerPatternEncoderClass = ReflectionHelper.getClassForName("appeng.container.implementations.ContainerPatternEncoder");
        guiWCTClass = ReflectionHelper.getClassForName("p455w0rd.wct.client.gui.GuiWCT");
        containerWirelessCraftingTermClass = ReflectionHelper.getClassForName("appeng.container.implementations.ContainerWirelessCraftingTerminal");
        wirelessCraftingGuiClass = ReflectionHelper.getClassForName("appeng.client.gui.implementations.GuiWirelessCraftingTerminal");
    }

    public static boolean issUnofficial() {
        return unofficial;
    }

    public static boolean isCraftingMode(Container container) {
        return Boolean.TRUE.equals(ReflectionHelper.compositeInvoker(Lists.newArrayList(containerPatternEncoderClass, containerPatternTermClass), container, false, "isCraftingMode"));
    }

    public static ItemRepo getRepo(GuiContainer gui) {
        return ReflectionHelper.possibleValueGetter(Lists.newArrayList(guiWCTClass, guiMEMonitorable), gui, "repo");
    }

    public static ItemRepo getRepo(GuiMEMonitorable term) {
        return ReflectionHelper.getFieldValue(GuiMEMonitorable.class, term, "repo");
    }

    public static boolean isWirelessContainer(Container container) {
        return containerWirelessCraftingTermClass != null && containerWirelessCraftingTermClass.isInstance(container);
    }

    public static boolean isWirelessGui(GuiScreen gui) {
        return wirelessCraftingGuiClass != null && wirelessCraftingGuiClass.isInstance(gui);
    }

    public static boolean isWctGui(GuiScreen screen) {
        return guiWCTClass != null && guiWCTClass.isInstance(screen);
    }
}
