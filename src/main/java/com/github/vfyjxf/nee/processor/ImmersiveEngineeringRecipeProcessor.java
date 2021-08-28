package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.*;

/**
 * @author vfyjxf
 */
public class ImmersiveEngineeringRecipeProcessor implements IRecipeProcessor {
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "ieArcFurnace", "ieBlastFurnace", "ieBlueprintCrafting",
                "ieBottlingMachine", "ieCokeOven", "ieCrusher",
                "ieFermenter", "ieHammerCrushing", "ieMetalPress",
                "ieRefinery", "ieShaderBag", "ieSqueezer"
        ));
    }

    @Override
    public String getRecipeProcessorId() {
        return "ImmersiveEngineering";
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (ident.equals(identifier)) {
                List<PositionedStack> recipeInputs = new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
                if (!("ieArcFurnace".equals(identifier) || "ieCrusher".equals(identifier) || "ieBlastFurnace".equals(identifier))) {
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
                if ("ieArcFurnace".equals(identifier) || "ieCrusher".equals(identifier)) {
                    recipeOutputs.addAll(recipe.getOtherStacks(recipeIndex));
                }
                return recipeOutputs;
            }
        }
        return null;
    }
}
