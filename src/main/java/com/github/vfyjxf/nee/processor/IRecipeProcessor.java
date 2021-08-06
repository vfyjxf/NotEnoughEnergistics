package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;

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

    List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier);

    List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier);

}
