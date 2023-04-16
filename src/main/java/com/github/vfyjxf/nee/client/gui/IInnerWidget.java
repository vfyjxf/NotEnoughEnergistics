package com.github.vfyjxf.nee.client.gui;

import net.minecraft.client.Minecraft;

public interface IInnerWidget {

    default boolean isFocused(){
        return true;
    }
    default void  setFocused(boolean focused){

    }

    void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks);

    void drawTooltips(Minecraft mc, int mouseX, int mouseY);

    boolean handleKeyPressed(char typedChar, int eventKey);

    boolean handleMouseClicked(int eventButton, int mouseX, int mouseY);
}
