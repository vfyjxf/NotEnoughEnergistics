package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author vfyjxf
 */
public interface IRecipeProcessor {


    default Set<String> getAllOverlayIdentifier(){
        return Collections.emptySet();
    }

    String getRecipeProcessorId();

    List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier);

    List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier);

}
