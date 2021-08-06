package com.github.vfyjxf.nee.processor;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author vfyjxf
 */
public class GregTechRecipeProcessor implements IRecipeProcessor {

    private static final Class<?> gtDefaultClz, gtAssLineClz;

    static {
        Class<?> gtDH = null;
        Class<?> gtAL = null;
        try {
            gtDH = Class.forName("gregtech.nei.GT_NEI_DefaultHandler");
            gtAL = Class.forName("gregtech.nei.GT_NEI_AssLineHandler");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        gtDefaultClz = gtDH;
        gtAssLineClz = gtAL;
    }

    @Override
    public Set<String> getAllOverlayIdentifier() {

        Set<String> identifiers = new HashSet<>();
        for (GT_Recipe.GT_Recipe_Map tMap : GT_Recipe.GT_Recipe_Map.sMappings) {
            if (tMap.mNEIAllowed) {
                identifiers.add(tMap.mNEIName);
            }
        }
        identifiers.add("gt.recipe.fakeAssemblylineProcess");
        return identifiers;
    }

    @Override
    public List<PositionedStack> getRecipeInput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        if (gtDefaultClz.isInstance(recipe) || gtAssLineClz.isInstance(recipe)) {
            List<PositionedStack> recipeInputs = new ArrayList<>(recipe.getIngredientStacks(recipeIndex));
            recipeInputs.removeIf(positionedStack -> GT_Utility.getFluidFromDisplayStack(positionedStack.items[0]) != null || positionedStack.item.stackSize == 0);
            return recipeInputs;
        }
        return null;
    }

    @Override
    public List<PositionedStack> getRecipeOutput(IRecipeHandler recipe, int recipeIndex, String identifier) {
        if (gtDefaultClz.isInstance(recipe) || gtAssLineClz.isInstance(recipe)) {
            List<PositionedStack> recipeOutputs = new ArrayList<>(recipe.getOtherStacks(recipeIndex));
            recipeOutputs.removeIf(positionedStack -> GT_Utility.getFluidFromDisplayStack(positionedStack.items[0]) != null);
            return recipeOutputs;
        }
        return null;
    }

}
