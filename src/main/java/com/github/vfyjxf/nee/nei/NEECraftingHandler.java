package com.github.vfyjxf.nee.nei;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPatternTermEx;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.network.packet.PacketNEIPatternRecipe;
import com.github.vfyjxf.nee.processor.IRecipeProcessor;
import com.github.vfyjxf.nee.processor.RecipeProcessor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vfyjxf
 */
public class NEECraftingHandler implements IOverlayHandler {

    public static final String OUTPUT_KEY = "Outputs";

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        if (firstGui instanceof GuiPatternTerm || firstGui instanceof GuiPatternTermEx) {
            NEENetworkHandler.getInstance().sendToServer(packRecipe(recipe, recipeIndex));
        }

    }

    private PacketNEIPatternRecipe packRecipe(IRecipeHandler recipe, int recipeIndex) {
        if (isCraftingTableRecipe(recipe)) {
            return packCraftingTableRecipe(recipe, recipeIndex);
        } else {
            return packProcessRecipe(recipe, recipeIndex);
        }
    }

    private PacketNEIPatternRecipe packProcessRecipe(IRecipeHandler recipe, int recipeIndex) {
        final NBTTagCompound recipeInputs = new NBTTagCompound();
        NBTTagCompound recipeOutputs = new NBTTagCompound();
        String identifier = ((TemplateRecipeHandler) recipe).getOverlayIdentifier();
        int inputIndex = 0;
        int outputIndex = 0;
        //get all recipe inputs and other stacks,use first item
        for (IRecipeProcessor processor : RecipeProcessor.recipeProcessors) {
            List<PositionedStack> inputs = processor.getRecipeInput(recipe, recipeIndex, identifier);
            List<PositionedStack> outputs = processor.getRecipeOutput(recipe, recipeIndex, identifier);

            List<PositionedStack> tInputs = new ArrayList<>();

            if (inputs != null && outputs != null) {

                for (PositionedStack positionedStack : inputs) {
                    ItemStack currentStack = positionedStack.items[0];
                    boolean find = false;
                    for (PositionedStack storedStack : tInputs) {
                        ItemStack StoredStack = storedStack.items[0];
                        if (StoredStack.isItemEqual(currentStack) && (StoredStack.stackSize + currentStack.stackSize) <= StoredStack.getMaxStackSize()) {
                            find = true;
                            storedStack.items[0].stackSize = StoredStack.stackSize + currentStack.stackSize;
                        }
                    }
                    if (!find) {
                        tInputs.add(positionedStack);
                    }
                }

                for (PositionedStack positionedStack : tInputs) {
                    ItemStack currentStack = positionedStack.items[0];
                    for (ItemStack stack : positionedStack.items) {
                        if (Platform.isRecipePrioritized(stack)) {
                            currentStack = stack.copy();
                            break;
                        }
                    }
                    recipeInputs.setTag("#" + inputIndex, currentStack.writeToNBT(new NBTTagCompound()));
                    inputIndex++;
                }

                for (PositionedStack positionedStack : outputs) {
                    if (outputIndex >= 4 || positionedStack.item == null) {
                        continue;
                    }
                    recipeOutputs.setTag(OUTPUT_KEY + outputIndex, positionedStack.item.writeToNBT(new NBTTagCompound()));
                    outputIndex++;
                }
            }
        }

        return new PacketNEIPatternRecipe(recipeInputs, recipeOutputs);
    }

    private PacketNEIPatternRecipe packCraftingTableRecipe(IRecipeHandler recipe, int recipeIndex) {
        final NBTTagCompound recipeInputs = new NBTTagCompound();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        for (final PositionedStack positionedStack : ingredients) {
            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;
            int slotIndex = col + row * 3;
            if (positionedStack.items != null && positionedStack.items.length > 0) {
                final ItemStack[] currentStackList = positionedStack.items;
                ItemStack stack = positionedStack.items[0];
                for (ItemStack currentStack : currentStackList) {
                    if (Platform.isRecipePrioritized(currentStack)) {
                        stack = currentStack.copy();
                    }
                }
                recipeInputs.setTag("#" + slotIndex, stack.writeToNBT(new NBTTagCompound()));
            }
        }
        return new PacketNEIPatternRecipe(recipeInputs, null);
    }

    private boolean isCraftingTableRecipe(IRecipeHandler recipe) {
        TemplateRecipeHandler templateRecipeHandler = (TemplateRecipeHandler) recipe;
        String overlayIdentifier = templateRecipeHandler.getOverlayIdentifier();
        return "crafting".equals(overlayIdentifier) || "crafting2x2".equals(overlayIdentifier);
    }

}
