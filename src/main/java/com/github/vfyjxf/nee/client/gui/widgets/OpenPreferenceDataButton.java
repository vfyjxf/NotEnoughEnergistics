package com.github.vfyjxf.nee.client.gui.widgets;

import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class OpenPreferenceDataButton extends GuiImageButton {
    public OpenPreferenceDataButton(int x, int y) {
        super(x, y, 9, 9, false);
    }

    @Override
    public void drawImage(Minecraft mc) {
        mc.renderEngine.bindTexture(new ResourceLocation("neenergistics", "textures/gui/states.png"));
        this.drawTexturedModalRect(0, 0, 18, 2, 11, 11);
        this.drawTexturedModalRect(1, 1, 3, 116, 9, 9);
    }

    @Override
    public void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            String tooltip = I18n.format("gui.neenergistics.button.tooltip.preference.open");
            TooltipRenderer.drawHoveringText(mc, tooltip, mouseX, mouseY);
        }
    }
}
