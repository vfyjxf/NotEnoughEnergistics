package com.github.vfyjxf.nee;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPatternTermEx;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.processor.IRecipeProcessor;
import com.github.vfyjxf.nee.processor.RecipeProcessor;
import cpw.mods.fml.common.Loader;
import thaumicenergistics.client.gui.GuiKnowledgeInscriber;

import java.util.*;


public class NEINeeConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {

        RecipeProcessor.init();

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

        installPatternTerminalExSupport(identifiers);

        installThaumicEnergisticsSupport();

    }

    @Override
    public String getName() {
        return NotEnoughEnergistics.NAME;
    }

    @Override
    public String getVersion() {
        return NotEnoughEnergistics.VERSION;
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
}
