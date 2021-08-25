package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import gregtech.api.util.GTPP_Recipe;

import java.util.*;

/**
 * @author vfyjxf
 */
public class GTPPRecipeProcessor implements IRecipeProcessor {
    @Override
    public Set<String> getAllOverlayIdentifier() {
        HashSet<String> identifiers = new HashSet<>(Collections.singletonList("GTPP_Decayables"));

        for (GTPP_Recipe.GTPP_Recipe_Map_Internal gtppMap : GTPP_Recipe.GTPP_Recipe_Map_Internal.sMappingsEx) {
            if (gtppMap.mNEIAllowed) {
                identifiers.add(gtppMap.mNEIName);
            }
        }

        return identifiers;
    }

    @Override
    public String getRecipeProcessorId() {
        return "GT++";
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (ident.equals(identifier)) {
                List<PositionedStack> recipeInputs = new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
                recipeInputs.removeIf(positionedStack -> GregTech5RecipeProcessor.getFluidFromDisplayStack(positionedStack.items[0]) != null || positionedStack.item.stackSize == 0);
                return recipeInputs;
            }
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for (String ident : getAllOverlayIdentifier()) {
            if (ident.equals(identifier)) {
                List<PositionedStack> recipeOutputs = new ArrayList<>(recipe.getOtherStacks(recipeIndex));
                recipeOutputs.removeIf(positionedStack -> GregTech5RecipeProcessor.getFluidFromDisplayStack(positionedStack.items[0]) != null);
                return recipeOutputs;
            }
        }
        return null;
    }
}
