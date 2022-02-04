package com.github.vfyjxf.nee.client.gui.widgets;

import appeng.client.gui.widgets.ITooltip;
import com.github.vfyjxf.nee.config.ItemCombination;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiImgButtonEnableCombination extends GuiButton implements ITooltip {
    private ItemCombination currentValue;

    public GuiImgButtonEnableCombination(int x, int y, ItemCombination value) {
        super(0, x, y, "");
        this.x = x;
        this.y = y;
        this.width = 8;
        this.height = 8;
        this.currentValue = value;
    }

    public void setValue(ItemCombination currentValue) {
        this.currentValue = currentValue;
    }

    public ItemCombination getCurrentValue() {
        return currentValue;
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
            if (this.currentValue == ItemCombination.ENABLED) {
                this.drawTexturedModalRect(0, 0, 16, 16, 16, 16);
            }
            if (this.currentValue == ItemCombination.DISABLED) {
                this.drawTexturedModalRect(0, 0, 16, 0, 16, 16);
            }
            if (this.currentValue == ItemCombination.WHITELIST) {
                this.drawTexturedModalRect(0, 0, 0, 16, 16, 16);
            }
            this.mouseDragged(mc, mouseX, mouseY);
            GlStateManager.popMatrix();
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public String getMessage() {
        return I18n.format("gui.neenergistics.button.title.combination") +
                "\n" +
                I18n.format("gui.neenergistics.button.tooltip.combination", currentValue.getLocalName());
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
