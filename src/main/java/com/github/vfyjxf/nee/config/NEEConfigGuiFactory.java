package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.utils.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NEEConfigGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new GuiConfig(parentScreen, getConfigElements(), Globals.MOD_ID, false, false, Globals.NAME);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    private static List<IConfigElement> getConfigElements() {
        Configuration config = NEEConfig.getConfig();
        return config.getCategoryNames()
                .stream()
                .map(name -> new ConfigElement(config.getCategory(name)))
                .collect(Collectors.toList());
    }
}
