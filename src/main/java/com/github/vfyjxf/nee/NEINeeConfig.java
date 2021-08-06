package com.github.vfyjxf.nee;

import appeng.client.gui.implementations.GuiPatternTerm;
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

        List<String> identifiers = new ArrayList<>(Arrays.asList("crafting", "crafting2x2","brewing","smelting","fuel"));
        for(IRecipeProcessor processor : RecipeProcessor.recipeProcessors){
            identifiers.addAll(processor.getAllOverlayIdentifier());
        }

        for(String ident : identifiers){
        API.registerGuiOverlay(GuiPatternTerm.class,ident);
        API.registerGuiOverlayHandler(GuiPatternTerm.class, new NEECraftingHandler(), ident);
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
