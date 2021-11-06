package com.github.vfyjxf.nee;

import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPatternTermEx;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.guihook.GuiContainerManager;
import com.github.vfyjxf.nee.client.NEEContainerDrawHandler;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.nei.NEECraftingHelper;
import com.github.vfyjxf.nee.processor.IRecipeProcessor;
import com.github.vfyjxf.nee.processor.RecipeProcessor;
import cpw.mods.fml.common.Loader;
import net.p455w0rd.wirelesscraftingterminal.client.gui.GuiWirelessCraftingTerminal;
import org.lwjgl.input.Keyboard;
import thaumicenergistics.client.gui.GuiKnowledgeInscriber;
import wanion.avaritiaddons.block.extremeautocrafter.GuiExtremeAutoCrafter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class NEINeeConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {

        RecipeProcessor.init();

        registerKeyBindings();

        if (NEEConfig.drawHighlight) {
            GuiContainerManager.addDrawHandler(NEEContainerDrawHandler.instance);
        }

        Set<String> defaultIdentifiers = new HashSet<>(
                Arrays.asList("crafting", "crafting2x2", "brewing", "smelting", "fuel", null)
        );
        Set<String> identifiers = new HashSet<>(defaultIdentifiers);

        for (IRecipeProcessor processor : RecipeProcessor.recipeProcessors) {
            identifiers.addAll(processor.getAllOverlayIdentifier());
        }

        for (String ident : identifiers) {
            API.registerGuiOverlay(GuiPatternTerm.class, ident);
            API.registerGuiOverlayHandler(GuiPatternTerm.class, new NEECraftingHandler(), ident);
        }

        installCraftingTermSupport();

        installWirelessCraftingTermSupport();

        installPatternTerminalExSupport(identifiers);

        installThaumicEnergisticsSupport();

        installAvaritiaddonsSupport();
    }

    @Override
    public String getName() {
        return NotEnoughEnergistics.NAME;
    }

    @Override
    public String getVersion() {
        return NotEnoughEnergistics.VERSION;
    }

    private void registerKeyBindings() {
        API.addKeyBind("nee.count", Keyboard.KEY_LCONTROL);
        API.addKeyBind("nee.ingredient", Keyboard.KEY_LSHIFT);
        API.addKeyBind("nee.preview", Keyboard.KEY_LCONTROL);
        API.addKeyBind("nee.nopreview", Keyboard.KEY_LMENU);
    }

    private void installCraftingTermSupport() {
        API.registerGuiOverlay(GuiCraftingTerm.class, "crafting");
        API.registerGuiOverlay(GuiCraftingTerm.class, "crafting2x2");
        API.registerGuiOverlayHandler(GuiCraftingTerm.class, NEECraftingHelper.INSTANCE, "crafting");
        API.registerGuiOverlayHandler(GuiCraftingTerm.class, NEECraftingHelper.INSTANCE, "crafting2x2");
    }

    private void installWirelessCraftingTermSupport() {
        if (Loader.isModLoaded("ae2wct")) {
            API.registerGuiOverlayHandler(GuiWirelessCraftingTerminal.class, NEECraftingHelper.INSTANCE, "crafting");
            API.registerGuiOverlayHandler(GuiWirelessCraftingTerminal.class, NEECraftingHelper.INSTANCE, "crafting2x2");
        }
    }

    private void installThaumicEnergisticsSupport() {
        try {
            Class.forName("thaumicenergistics.client.gui.GuiKnowledgeInscriber");
        } catch (ClassNotFoundException e) {
            return;
        }
        if (Loader.isModLoaded("thaumcraftneiplugin")) {
            NotEnoughEnergistics.logger.info("Install ThaumicEnergistics support");

            API.registerGuiOverlay(GuiKnowledgeInscriber.class, "arcaneshapedrecipes");
            API.registerGuiOverlay(GuiKnowledgeInscriber.class, "arcaneshapelessrecipes");
            API.registerGuiOverlayHandler(GuiKnowledgeInscriber.class, new NEECraftingHandler(), "arcaneshapedrecipes");
            API.registerGuiOverlayHandler(GuiKnowledgeInscriber.class, new NEECraftingHandler(), "arcaneshapelessrecipes");

        }
    }

    private void installPatternTerminalExSupport(Set<String> identifiers) {
        try {
            Class.forName("appeng.client.gui.implementations.GuiPatternTermEx");
        } catch (ClassNotFoundException e) {
            return;
        }
        identifiers.remove("crafting");
        identifiers.remove("crafting2x2");
        //PatternTermEx Support
        for (String ident : identifiers) {
            API.registerGuiOverlay(GuiPatternTermEx.class, ident);
            API.registerGuiOverlayHandler(GuiPatternTermEx.class, new NEECraftingHandler(), ident);
        }
    }

    private void installAvaritiaddonsSupport() {
        try {
            Class.forName("wanion.avaritiaddons.block.extremeautocrafter.GuiExtremeAutoCrafter");
        } catch (ClassNotFoundException e) {
            return;
        }
        if (Loader.isModLoaded("avaritiaddons")) {
            NotEnoughEnergistics.logger.info("Install Avaritiaddons support");

            API.registerGuiOverlay(GuiExtremeAutoCrafter.class, "extreme");
            API.registerGuiOverlayHandler(GuiExtremeAutoCrafter.class, new NEECraftingHandler(), "extreme");
        }
    }
}