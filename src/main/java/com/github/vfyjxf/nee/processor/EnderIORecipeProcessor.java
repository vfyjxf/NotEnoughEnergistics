package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import crazypants.enderio.nei.SagMillRecipeHandler;

import java.util.*;

public class EnderIORecipeProcessor implements IRecipeProcessor {
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "EnderIOAlloySmelter", "EIOEnchanter", "EnderIOSagMill",
                "EnderIOSliceAndSplice", "EnderIOSoulBinder", "EnderIOVat"
        ));
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
            for (String ident : getAllOverlayIdentifier()) {
                if (ident.equals(identifier)) {
                    return new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
                }
            }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        if(identifier!=null) {
            for (String ident : getAllOverlayIdentifier()) {
                if (ident.equals(identifier)) {
                    List<PositionedStack> recipeOutputs = new ArrayList<>();
                    recipeOutputs.add(recipe.getResultStack(recipeIndex));
                    recipeOutputs.addAll(recipe.getOtherStacks(recipeIndex));
                    //remove output if it's chance != 1
                    if(recipe instanceof SagMillRecipeHandler){
                        SagMillRecipeHandler.MillRecipe millRecipe = (SagMillRecipeHandler.MillRecipe) ((SagMillRecipeHandler) recipe).arecipes.get(recipeIndex);
                        recipeOutputs.removeIf(positionedStack -> millRecipe.getChanceForOutput(positionedStack.item) != 1.0F);
                    }
                    return recipeOutputs;
                }
            }
        }
        return null;
    }
}
