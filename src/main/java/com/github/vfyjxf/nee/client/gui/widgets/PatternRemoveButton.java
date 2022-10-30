package com.github.vfyjxf.nee.client.gui.widgets;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class PatternRemoveButton extends GuiImageButton implements ITooltip {

    public PatternRemoveButton(int x, int y) {
        super(x, y, 8, 8, true);
    }

    @Override
    public void drawImage(Minecraft mc) {
        mc.renderEngine.bindTexture(new ResourceLocation("neenergistics", "textures/gui/states.png"));
        this.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
        this.drawTexturedModalRect(0, 0, 0, 64, 16, 16);
    }

    @Override
    public String getMessage() {
        return I18n.format("gui.neenergistics.button.title.remove") +
                "\n" +
                I18n.format("gui.neenergistics.button.tooltip.remove");
    }

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}
