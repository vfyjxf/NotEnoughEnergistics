package com.github.vfyjxf.nee.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class HighResolutionTexture implements ITexture {
    private final ITexture texture;
    private final int scale;

    public HighResolutionTexture(ITexture texture, int scale) {
        this.texture = texture;
        this.scale = scale;
    }


    @Override
    public int getWidth() {
        return texture.getWidth() / scale;
    }

    @Override
    public int getHeight() {
        return texture.getHeight() / scale;
    }

    @Override
    public void draw(Minecraft mc, int xOffset, int yOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(xOffset, yOffset, 0.0F);
        GlStateManager.scale(1.0f / scale, 1.0f / scale, 1.0f / scale);
        texture.draw(mc, 0, 0);
        GlStateManager.popMatrix();
    }

    @Override
    public void draw(Minecraft mc, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
        draw(mc, xOffset, yOffset);
    }
}
