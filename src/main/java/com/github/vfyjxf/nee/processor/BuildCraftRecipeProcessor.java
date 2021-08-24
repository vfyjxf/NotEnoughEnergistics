package com.github.vfyjxf.nee.processor;

import buildcraft.compat.nei.RecipeHandlerAssemblyTable;
import buildcraft.compat.nei.RecipeHandlerIntegrationTable;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;

import java.util.Collections;
import java.util.List;

/**
 * @author vfyjxf
 */
public class BuildCraftRecipeProcessor implements IRecipeProcessor {

    @Override
    public String getRecipeProcessorId() {
        return "BuildCraft";
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        if (recipe instanceof RecipeHandlerAssemblyTable) {
            return handlerAssemblyTableRecipe((RecipeHandlerAssemblyTable) recipe, recipeIndex, true);
        } else if (recipe instanceof RecipeHandlerIntegrationTable) {
            return handlerIntegrationTableRecipe((RecipeHandlerIntegrationTable) recipe, recipeIndex, true);
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        if (recipe instanceof RecipeHandlerAssemblyTable) {
            return handlerAssemblyTableRecipe((RecipeHandlerAssemblyTable) recipe, recipeIndex, false);
        } else if (recipe instanceof RecipeHandlerIntegrationTable) {
            return handlerIntegrationTableRecipe((RecipeHandlerIntegrationTable) recipe, recipeIndex, false);
        }
        return null;
    }

    private List<PositionedStack> handlerAssemblyTableRecipe(RecipeHandlerAssemblyTable recipe, int recipeIndex, boolean getInput) {
        return getInput ? recipe.getIngredientStacks(recipeIndex) : Collections.singletonList(recipe.getResultStack(recipeIndex));
    }

    private List<PositionedStack> handlerIntegrationTableRecipe(RecipeHandlerIntegrationTable recipe, int recipeIndex, boolean getInput) {
        return getInput ? recipe.getIngredientStacks(recipeIndex) : Collections.singletonList(recipe.getResultStack(recipeIndex));
    }

}
