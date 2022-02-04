package com.github.vfyjxf.nee.client.gui.widgets;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiImgButtonRemove extends GuiButton implements ITooltip {

    public GuiImgButtonRemove(int x, int y) {
        super(0, x, y, "");
        this.width = 8;
        this.height = 8;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x, this.y, 0.0F);
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            if (this.enabled) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                GlStateManager.color(0.5f, 0.5f, 0.5f, 1.0f);
            }
            mc.renderEngine.bindTexture(new ResourceLocation("neenergistics", "textures/gui/states.png"));
            this.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
            this.drawTexturedModalRect(0, 0, 0, 32, 16, 16);
            this.mouseDragged(mc, mouseX, mouseY);
            GlStateManager.popMatrix();
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
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
