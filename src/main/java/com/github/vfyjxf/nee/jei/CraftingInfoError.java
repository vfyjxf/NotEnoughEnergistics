package com.github.vfyjxf.nee.jei;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.utils.IngredientStatus;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.config.KeyBindings.AUTO_CRAFT_WITH_PREVIEW;
import static com.github.vfyjxf.nee.jei.CraftingTransferHandler.isIsPatternInterfaceExists;
import static mezz.jei.api.recipe.transfer.IRecipeTransferError.Type.USER_FACING;

public class CraftingInfoError implements IRecipeTransferError {

    private final boolean crafting;
    private final RecipeAnalyzer analyzer;


    public CraftingInfoError(RecipeAnalyzer analyzer, boolean isCrafting) {
        this.analyzer = analyzer;
        this.crafting = isCrafting;
    }

    /**
     * Since the 1.12.2 jei does not have a third type, we use asm to set the button enable.
     */
    @Nonnull
    @Override
    public Type getType() {
        return USER_FACING;
    }

    @Override
    public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
        analyzer.update();
        List<RecipeAnalyzer.RecipeIngredient> ingredients = analyzer.analyzeRecipe(recipeLayout);
        for (RecipeAnalyzer.RecipeIngredient ingredient : ingredients) {
            if (ingredient.getStatus() == IngredientStatus.CRAFTABLE) {
                ingredient.drawHighlight(minecraft, NEEConfig.getCraftableHighlightColor(), recipeX, recipeY);
            } else if (ingredient.getStatus() == IngredientStatus.MISSING) {
                ingredient.drawHighlight(minecraft, NEEConfig.getMissingHighlightColor(), recipeX, recipeY);
            }
        }

        boolean autoCraftingAvailable = false;
        boolean patternExists = false;
        boolean missingItems = false;
        List<String> tooltips = new ArrayList<>();
        if (crafting) {
            if (!isIsPatternInterfaceExists()) {
                autoCraftingAvailable = ingredients.stream().anyMatch(ingredient -> ingredient.getStatus() == IngredientStatus.CRAFTABLE);
                missingItems = ingredients.stream().anyMatch(ingredient -> ingredient.getStatus() == IngredientStatus.MISSING);
            } else {
                //TODO:Pattern Interface support
            }
        } else {
            patternExists = ingredients.stream().anyMatch(ingredient -> ingredient.getStatus() == IngredientStatus.CRAFTABLE);
        }
        if (autoCraftingAvailable) {
            tooltips.add(String.format("%s" + TextFormatting.GRAY + " + " +
                            TextFormatting.BLUE + I18n.format("jei.tooltip.nee.helper.crafting.text1"),
                    TextFormatting.YELLOW + Keyboard.getKeyName(AUTO_CRAFT_WITH_PREVIEW.getKeyCode())));
        }
        if (patternExists) {
            tooltips.add(TextFormatting.BLUE + I18n.format("jei.tooltip.nee.helper.pattern"));
        }
        if (missingItems) {
            tooltips.add(TextFormatting.RED + I18n.format("jei.tooltip.error.recipe.transfer.missing"));
        }
        if (isIsPatternInterfaceExists()) {
            tooltips.add(String.format("%s" + TextFormatting.GRAY + " + " +
                            TextFormatting.BLUE + I18n.format("jei.tooltip.nee.helper.crafting.text2"),
                    TextFormatting.YELLOW + Keyboard.getKeyName(AUTO_CRAFT_WITH_PREVIEW.getKeyCode())));
        }

        TooltipRenderer.drawHoveringText(minecraft, tooltips, mouseX, mouseY);
    }

}
