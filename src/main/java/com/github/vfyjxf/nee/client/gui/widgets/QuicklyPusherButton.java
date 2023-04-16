package com.github.vfyjxf.nee.client.gui.widgets;

import com.github.vfyjxf.nee.client.gui.QuickPusherWidget;
import com.github.vfyjxf.nee.client.gui.elements.HighResolutionTexture;
import com.github.vfyjxf.nee.client.gui.elements.ITexture;
import com.github.vfyjxf.nee.client.gui.elements.ResourceTexture;
import com.github.vfyjxf.nee.utils.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QuicklyPusherButton extends GuiImageButton {

    private static final ITexture QUICKLY_PUSHER_ICON = new HighResolutionTexture(new ResourceTexture(new ResourceLocation(Globals.MOD_ID, "textures/gui/quickly_pusher_icon.png"), 16, 16), 1);
    private static final ITexture SUPER_PUSHER_ICON = new HighResolutionTexture(new ResourceTexture(new ResourceLocation(Globals.MOD_ID, "textures/gui/super_pusher_icon.png"), 16, 16), 1);

    private boolean isSuper;
    @Nullable
    private QuickPusherWidget widget;

    public QuicklyPusherButton(int x, int y) {
        super(x, y, 16, 16, false);
    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            if (widget == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void drawImage(Minecraft mc) {
        mc.renderEngine.bindTexture(STATES);
        this.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
        if (GuiContainer.isShiftKeyDown()) {
            this.isSuper = true;
            SUPER_PUSHER_ICON.draw(mc, x, y);
        } else {
            this.isSuper = false;
            QUICKLY_PUSHER_ICON.draw(mc, x, y);
        }
    }

    @Override
    public void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {

        }
    }

    public boolean isSuper() {
        return isSuper;
    }

    public void setWidget(@Nullable QuickPusherWidget widget) {
        this.widget = widget;
    }
}
