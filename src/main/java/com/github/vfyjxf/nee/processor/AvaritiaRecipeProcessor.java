package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.*;

public class AvaritiaRecipeProcessor implements IRecipeProcessor{
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "extreme","extreme_compression"
        ));
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for(String ident : getAllOverlayIdentifier()){
            if(ident.equals(identifier)){
                return new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
            }
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        for(String ident : getAllOverlayIdentifier()){
            if(ident.equals(identifier)){
                List<PositionedStack> recipeOutput = new ArrayList<>();
                recipeOutput.add(recipe.getResultStack(recipeIndex));
                return recipeOutput;
            }
        }
        return null;
    }
}
