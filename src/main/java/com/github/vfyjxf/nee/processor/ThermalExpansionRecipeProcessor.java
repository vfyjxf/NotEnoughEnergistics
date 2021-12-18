package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import cofh.thermalexpansion.plugins.nei.handlers.RecipeHandlerBase;
import cpw.mods.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;


public class ThermalExpansionRecipeProcessor implements IRecipeProcessor {

    @Nonnull
    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList(
                "thermalexpansion.charger", "thermalexpansion.crucible", "thermalexpansion.furnace", "thermalexpansion.insolator",
                "thermalexpansion.pulverizer", "thermalexpansion.sawmill", "thermalexpansion.smelter", "thermalexpansion.transposer"
        ));
    }

    @Nonnull
    @Override
    public String getRecipeProcessorId() {
        return "ThermalExpansion";
    }

    @Nonnull
    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        List<PositionedStack> recipeInputs = new ArrayList<>();
        if (this.getAllOverlayIdentifier().contains(identifier)) {
            recipeInputs.addAll(recipe.getIngredientStacks(recipeIndex));
            if (recipe instanceof RecipeHandlerBase) {
                Class<?> NEIRecipeBase = null;
                try {
                    NEIRecipeBase = Class.forName("cofh.thermalexpansion.plugins.nei.handlers.RecipeHandlerBase$NEIRecipeBase");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (NEIRecipeBase != null) {
                    Field secondaryInputField = ReflectionHelper.findField(NEIRecipeBase, "secondaryInput");
                    PositionedStack secondaryInput = null;
                    try {
                        secondaryInput = (PositionedStack) secondaryInputField.get(((RecipeHandlerBase) recipe).arecipes.get(recipeIndex));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (secondaryInput != null) {
                        recipeInputs.add(secondaryInput);
                    }
                }
            }
            return recipeInputs;
        }

        return recipeInputs;
    }

    @Nonnull
    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        List<PositionedStack> recipeOutputs = new ArrayList<>();
        if (this.getAllOverlayIdentifier().contains(identifier)) {
            recipeOutputs.add(recipe.getResultStack(recipeIndex));
            if (recipe instanceof RecipeHandlerBase) {
                Class<?> NEIRecipeBase = null;
                try {
                    NEIRecipeBase = Class.forName("cofh.thermalexpansion.plugins.nei.handlers.RecipeHandlerBase$NEIRecipeBase");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (NEIRecipeBase != null) {
                    Field secondaryOutputField = ReflectionHelper.findField(NEIRecipeBase, "secondaryOutput");
                    Field secondaryOutputChanceField = ReflectionHelper.findField(NEIRecipeBase, "secondaryOutputChance");
                    PositionedStack secondaryOutput = null;
                    int secondaryOutputChance = 0;
                    try {
                        secondaryOutput = (PositionedStack) secondaryOutputField.get(((RecipeHandlerBase) recipe).arecipes.get(recipeIndex));
                        secondaryOutputChance = (int) secondaryOutputChanceField.get(((RecipeHandlerBase) recipe).arecipes.get(recipeIndex));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (secondaryOutput != null && secondaryOutputChance >= 100) {
                        recipeOutputs.add(secondaryOutput);
                    }
                }
            }
            return recipeOutputs;
        }
        return recipeOutputs;
    }
}
