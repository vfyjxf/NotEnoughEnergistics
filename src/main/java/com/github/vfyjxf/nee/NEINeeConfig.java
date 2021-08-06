package com.github.vfyjxf.nee;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPatternTermEx;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.github.vfyjxf.nee.nei.NEECraftingHandler;
import com.github.vfyjxf.nee.processor.IRecipeProcessor;
import com.github.vfyjxf.nee.processor.RecipeProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NEINeeConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {

        RecipeProcessor.init();

        List<String> defaultIdentifiers= Arrays.asList("crafting", "crafting2x2","brewing","smelting","fuel");
        List<String> identifiers = new ArrayList<>(defaultIdentifiers);

        for(IRecipeProcessor processor : RecipeProcessor.recipeProcessors){
            identifiers.addAll(processor.getAllOverlayIdentifier());
        }

        for(String ident : identifiers){
        API.registerGuiOverlay(GuiPatternTerm.class, ident);
        API.registerGuiOverlayHandler(GuiPatternTerm.class, new NEECraftingHandler(), ident);
        }

        try {
           Class.forName("appeng.client.gui.implementations.GuiPatternTermEx");
        } catch (ClassNotFoundException e) {
            return;
        }

        identifiers.removeAll(defaultIdentifiers);
        //PatternTermEx Support
        for(String ident : identifiers){
            API.registerGuiOverlay(GuiPatternTermEx.class, ident);
            API.registerGuiOverlayHandler(GuiPatternTermEx.class, new NEECraftingHandler(), ident);
        }
    }

    @Override
    public String getName() {
        return NotEnoughEnergistics.NAME;
    }

    @Override
    public String getVersion() {
        return NotEnoughEnergistics.VERSION;
    }
}
