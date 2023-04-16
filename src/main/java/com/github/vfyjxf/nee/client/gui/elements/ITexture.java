package com.github.vfyjxf.nee.client.gui.elements;

import net.minecraft.client.Minecraft;

public interface ITexture {
    int getWidth();

    int getHeight();

    void draw(Minecraft mc, int xOffset, int yOffset);

    void draw(Minecraft mc, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight);
}
