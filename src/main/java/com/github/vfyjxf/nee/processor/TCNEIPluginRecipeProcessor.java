package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;

import java.util.*;

/**
 * @author vfyjxf
 */
public class TCNEIPluginRecipeProcessor implements IRecipeProcessor{

    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "arcaneshapedrecipes","arcaneshapelessrecipes","aspectsRecipe",
                "cruciblerecipe","infusionCrafting"
        ));
    }

    @Override
    public String getRecipeProcessorId() {
        return "TCNEIPlugin";
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for(String ident : getAllOverlayIdentifier()){
            if(ident.equals(identifier)){
                List<PositionedStack> recipeInputs = new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
                recipeInputs.removeIf(positionedStack -> positionedStack.item.getItem() instanceof ItemAspect);
                return recipeInputs;
            }
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for(String ident : getAllOverlayIdentifier()){
            if(ident.equals(identifier)){
                List<PositionedStack> recipeOutputs = new ArrayList<>();
                recipeOutputs.add(recipe.getResultStack(recipeIndex));
                return recipeOutputs;
            }
        }
        return null;
    }
}
