package com.github.vfyjxf.nee.jei;

import com.github.vfyjxf.nee.utils.Ingredient;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.client.KeyBindings.craftingHelperPreview;
import static mezz.jei.api.recipe.transfer.IRecipeTransferError.Type.USER_FACING;

public class CraftingHelperTooltipError implements IRecipeTransferError {

    private static final Color craftableColor = new Color(0.0f, 0.0f, 1.0f, 0.4f);
    private static final Color missingColor = new Color(1.0f, 0.0f, 0.0f, 0.4f);

    private IngredientTracker tracker;
    private boolean isCraftingTerm;
    private boolean hasPatternCrafter;

    public CraftingHelperTooltipError() {
    }

    public CraftingHelperTooltipError(IngredientTracker tracker, boolean isCraftingTerm) {
        this.tracker = tracker;
        this.isCraftingTerm = isCraftingTerm;
    }

    @Override
    public Type getType() {
        return USER_FACING;
    }

    @Override
    public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {

        boolean drawTooltipInCraftingTerm = false;
        boolean drawTooltipInPattenTerm = false;
        boolean drawMissingItemTooltip = false;
        List<String> tooltips = new ArrayList<>();
        for (Ingredient ingredient : tracker.getIngredients()) {
            if (isCraftingTerm) {
                if (ingredient.requiresToCraft() && ingredient.isCraftable()) {
                    drawTooltipInCraftingTerm = true;
                    ingredient.getIngredient().drawHighlight(minecraft, craftableColor, recipeX, recipeY);
                } else if (ingredient.requiresToCraft()) {
                    drawMissingItemTooltip = true;
                    ingredient.getIngredient().drawHighlight(minecraft, missingColor, recipeX, recipeY);
                }
            } else {
                if (ingredient.isCraftable()) {
                    drawTooltipInPattenTerm = true;
                    ingredient.getIngredient().drawHighlight(minecraft, craftableColor, recipeX, recipeY);
                }
            }
        }
        if (drawTooltipInCraftingTerm) {
            tooltips.add(String.format("%s" + TextFormatting.GRAY + " + " +
                            TextFormatting.BLUE + I18n.format("jei.tooltip.nee.helper.crafting"),
                    TextFormatting.YELLOW + Keyboard.getKeyName(craftingHelperPreview.getKeyCode())));
        }
        if (drawTooltipInPattenTerm) {
            tooltips.add(TextFormatting.BLUE + I18n.format("jei.tooltip.nee.helper.pattern"));
        }
        if (drawMissingItemTooltip) {
            tooltips.add(TextFormatting.RED + I18n.format("jei.tooltip.error.recipe.transfer.missing"));
        }
        TooltipRenderer.drawHoveringText(minecraft, tooltips, mouseX, mouseY);
    }

    public void setHasPatternCrafter(boolean hasPatternCrafter) {
        this.hasPatternCrafter = hasPatternCrafter;
    }
}
