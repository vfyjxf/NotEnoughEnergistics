package com.github.vfyjxf.nee.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ItemWidget extends Gui {

    private boolean visible;
    private boolean isSelected;
    private int posX;
    private int posY;
    private int width;
    private int height;
    private final ItemStack ingredient;

    public ItemWidget(ItemStack ingredient, int posX, int posY) {
        this.ingredient = ingredient;
        this.posX = posX;
        this.posY = posY;
        this.width = 18;
        this.height = 18;
        this.visible = true;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void updatePosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public ItemStack getIngredient() {
        return ingredient;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return visible && (mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height);
    }

    public void drawWidget(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            FontRenderer fontRenderer = ingredient.getItem().getFontRenderer(ingredient);
            if (fontRenderer == null) {
                fontRenderer = mc.fontRenderer;
            }
            GlStateManager.enableDepth();
            this.zLevel += 300;
            mc.getRenderItem().zLevel += 300;
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(null, ingredient, posX, posY);
            mc.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, ingredient, mouseX, mouseY, null);
            GlStateManager.disableBlend();
            RenderHelper.disableStandardItemLighting();
            this.zLevel -= 300;
            mc.getRenderItem().zLevel -= 300;

            if (isSelected) {
                GlStateManager.disableDepth();
                RenderHelper.disableStandardItemLighting();
                drawRect(posX, posY, posX + 16, posY + 16, 0x660000ff);
                GlStateManager.enableDepth();
            }
        }
    }

    public void drawTooltips(Minecraft mc, GuiScreen screen, int mouseX, int mouseY) {
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        if (!isSelected) {
            drawRect(posX, posY, posX + 16, posY + 16, 0x80FFFFFF);
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
        screen.drawHoveringText(screen.getItemToolTip(ingredient), mouseX, mouseY);
        GlStateManager.enableDepth();
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
