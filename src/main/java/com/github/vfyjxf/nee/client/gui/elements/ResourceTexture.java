package com.github.vfyjxf.nee.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class ResourceTexture implements ITexture {

    private final ResourceLocation resourceLocation;
    private final int width;
    private final int height;

    public ResourceTexture(ResourceLocation resourceLocation, int width, int height) {
        this.resourceLocation = resourceLocation;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        draw(minecraft, xOffset, yOffset, 0, 0, 0, 0);
    }


    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
        minecraft.getTextureManager().bindTexture(this.resourceLocation);
        int width = this.width - maskRight - maskLeft;
        int height = this.height - maskBottom - maskTop;
        Gui.drawModalRectWithCustomSizedTexture(xOffset, yOffset, 0, 0, width, height, width, height);
    }
}
