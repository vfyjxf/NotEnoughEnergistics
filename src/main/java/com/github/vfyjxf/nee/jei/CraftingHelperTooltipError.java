package com.github.vfyjxf.nee.jei;

import com.github.vfyjxf.nee.utils.Ingredient;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.client.KeyBindings.craftingHelperPreview;


public class CraftingHelperTooltipError implements IRecipeTransferError {

    private static final Color craftableColor = new Color(0.0f, 0.0f, 1.0f, 0.4f);
    private static final Color missingColor = new Color(1.0f, 0.0f, 0.0f, 0.4f);

    private final IngredientTracker tracker;
    private final boolean isCraftingTerm;

    public CraftingHelperTooltipError(IngredientTracker tracker, boolean isCraftingTerm) {
        this.tracker = tracker;
        this.isCraftingTerm = isCraftingTerm;
    }

    @Override
    public Type getType() {
        return Type.COSMETIC;
    }

    @Override
    public void showError(MatrixStack matrixStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {

        boolean drawTooltipInCraftingTerm = false;
        boolean drawTooltipInPattenTerm = false;
        boolean drawMissingItemTooltip = false;
        List<ITextComponent> messages = new ArrayList<>();

        for (Ingredient ingredient : tracker.getIngredients()) {
            if (isCraftingTerm) {
                if (ingredient.requiresToCraft() && ingredient.isCraftable()) {
                    drawTooltipInCraftingTerm = true;
                    ingredient.getIngredient().drawHighlight(matrixStack, craftableColor.getRGB(), recipeX, recipeY);
                } else if (ingredient.requiresToCraft()) {
                    drawMissingItemTooltip = true;
                    ingredient.getIngredient().drawHighlight(matrixStack, missingColor.getRGB(), recipeX, recipeY);
                }
            } else {
                if (ingredient.isCraftable()) {
                    drawTooltipInPattenTerm = true;
                    ingredient.getIngredient().drawHighlight(matrixStack, craftableColor.getRGB(), recipeX, recipeY);
                }
            }
        }

        if (drawTooltipInCraftingTerm) {
            messages.add(new TranslationTextComponent(craftingHelperPreview.getKey().getName()).withStyle(TextFormatting.YELLOW)
                    .append(new StringTextComponent(" + ").withStyle(TextFormatting.GRAY))
                    .append(new TranslationTextComponent("jei.tooltip.nee.helper.crafting").withStyle(TextFormatting.BLUE)));
        }
        if (drawTooltipInPattenTerm) {
            messages.add(new TranslationTextComponent("jei.tooltip.nee.helper.pattern").withStyle(TextFormatting.BLUE));
        }
        if (drawMissingItemTooltip) {
            messages.add(new TranslationTextComponent("jei.appliedenergistics2.missing_items").withStyle(TextFormatting.RED));
        }
        TooltipRenderer.drawHoveringText(messages, mouseX, mouseY, matrixStack);
    }
}
