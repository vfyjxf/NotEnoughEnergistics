package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.*;

/**
 * @author vfyjxf
 */
public class VanillaRecipeProcessor implements IRecipeProcessor {
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList("brewing", "smelting", "fuel"));
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        List<PositionedStack> recipeInputs = new ArrayList<>();
        if ("brewing".equals(identifier) || "smelting".equals(identifier) || "fuel".equals(identifier)) {
            recipeInputs.addAll(recipe.getIngredientStacks(recipeIndex));
            recipeInputs.addAll(recipe.getOtherStacks(recipeIndex));
        }
        return recipeInputs;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        if ("brewing".equals(identifier) || "smelting".equals(identifier) || "fuel".equals(identifier)) {
            List<PositionedStack> recipeOutput = new ArrayList<>();
            recipeOutput.add(recipe.getResultStack(recipeIndex));
            return recipeOutput;
        }
        return null;
    }

}
