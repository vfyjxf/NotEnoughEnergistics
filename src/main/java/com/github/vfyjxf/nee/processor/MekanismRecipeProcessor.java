package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.*;

/**
 * @author vfyjxf
 */
public class MekanismRecipeProcessor implements IRecipeProcessor {
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "chemicalinfuser", "chemicalinjectionchamber",
                "chemicaloxidizer", "chemicalwasher", "combiner", "crusher", "electrolyticseparator",
                "chamber", "infuser", "compressor", "prc", "precisionsawmill", "purificationchamber",
                "rotarycondensentrator", "solarneutron", "thermalevaporation"
        ));
    }

    @Override
    public String getRecipeProcessorId() {
        return "Mekanism";
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (ident.equals(identifier)) {
                List<PositionedStack> recipeInputs = new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
                if (!recipe.getOtherStacks(recipeIndex).isEmpty()) {
                    recipeInputs.addAll(recipe.getOtherStacks(recipeIndex));
                }
                return recipeInputs;
            }
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (ident.equals(identifier)) {
                List<PositionedStack> recipeOutputs = new ArrayList<>();
                recipeOutputs.add(recipe.getResultStack(recipeIndex));
                return recipeOutputs;
            }
        }
        return null;
    }
}
