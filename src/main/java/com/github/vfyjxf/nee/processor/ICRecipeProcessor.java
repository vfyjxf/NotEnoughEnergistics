package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.*;

/**
 * @author vfyjxf
 */
public class ICRecipeProcessor implements IRecipeProcessor {
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "blastfurnace", "BlockCutter", "centrifuge", "compressor", "extractor", "fluidcanner", "macerator",
                "metalformer", "oreWashing", "solidcanner"
        ));
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (identifier.equals(ident)) {
                return new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
            }
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (identifier.equals(ident)) {
                List<PositionedStack> recipeOutputs = new ArrayList<>();
                recipeOutputs.add(recipe.getResultStack(recipeIndex));
                recipeOutputs.addAll(recipe.getOtherStacks(recipeIndex));
                return recipeOutputs;
            }
        }
        return null;
    }


}
