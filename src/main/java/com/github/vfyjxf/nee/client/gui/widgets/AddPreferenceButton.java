package com.github.vfyjxf.nee.client.gui.widgets;

import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AddPreferenceButton extends GuiImageButton {

    private boolean ingredientExists;

    public AddPreferenceButton(int x, int y) {
        super(x, y, 9, 9, false);
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
        boolean pressable = super.mousePressed(mc, mouseX, mouseY);
        if (pressable) {

        }
        return pressable;
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

    public interface OnSlotPress {
        void onSlotPress();
    }

}
