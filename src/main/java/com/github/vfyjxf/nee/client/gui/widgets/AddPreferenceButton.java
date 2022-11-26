package com.github.vfyjxf.nee.client.gui.widgets;

import com.github.vfyjxf.nee.client.gui.IngredientSwitcherWidget;
import com.github.vfyjxf.nee.config.PreferenceList;
import com.github.vfyjxf.nee.helper.PreferenceHelper;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AddPreferenceButton extends GuiImageButton {

    private final IngredientSwitcherWidget widget;
    private boolean ingredientExists;

    public AddPreferenceButton(IngredientSwitcherWidget switcherWidget, int x, int y) {
        super(x, y, 11, 11, false);
        this.widget = switcherWidget;
    }

    @Override
    public void drawImage(Minecraft mc) {
        mc.renderEngine.bindTexture(new ResourceLocation("neenergistics", "textures/gui/states.png"));
        this.drawTexturedModalRect(0, 0, 18, 2, 11, 11);
        if (ingredientExists) {
            this.drawTexturedModalRect(1, 1, 3, 84, 9, 9);
        } else {
            this.drawTexturedModalRect(1, 1, 3, 100, 9, 9);
        }
    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            if (widget.getSelectedWidget() != null) {
                if (ingredientExists) {
                    PreferenceList.INSTANCE.removePreference(widget.getSelectedWidget().getIngredient());
                } else {
                    PreferenceList.INSTANCE.addPreference(widget.getSelectedWidget().getIngredient(), null);
                }
                ingredientExists = !ingredientExists;
            }
            return true;
        }
        return false;
    }

    public void update() {
        this.ingredientExists = widget.getSelectedWidget() != null &&
                PreferenceHelper.isPreferItem(widget.getSelectedWidget().getIngredient());
    }

    @Override
    public void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            List<String> tooltips = new ArrayList<>();
            tooltips.add(ingredientExists ? I18n.format("gui.neenergistics.button.tooltip.preference.remove") : I18n.format("gui.neenergistics.button.tooltip.preference.add"));
            tooltips.add(I18n.format("gui.neenergistics.button.tooltip.preference.text1"));
            TooltipRenderer.drawHoveringText(mc, tooltips, mouseX, mouseY);
        }
    }

}
