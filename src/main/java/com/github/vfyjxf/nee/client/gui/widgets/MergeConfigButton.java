package com.github.vfyjxf.nee.client.gui.widgets;

import appeng.client.gui.widgets.ITooltip;
import com.github.vfyjxf.nee.config.IngredientMergeMode;
import com.github.vfyjxf.nee.config.NEEConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

public class MergeConfigButton extends GuiImageButton implements ITooltip {
    private IngredientMergeMode mergeMode;

    public MergeConfigButton(int x, int y, IngredientMergeMode value) {
        super(x, y, 8, 8, true);
        this.x = x;
        this.y = y;
        this.mergeMode = value;
        this.setOnPress(button -> {
            int ordinal = Mouse.getEventButton() != 2 ? this.getMergeMode().ordinal() + 1 : this.getMergeMode().ordinal() - 1;

            if (ordinal >= IngredientMergeMode.values().length) {
                ordinal = 0;
            }
            if (ordinal < 0) {
                ordinal = IngredientMergeMode.values().length - 1;
            }
            this.setMode(IngredientMergeMode.values()[ordinal]);
            NEEConfig.setMergeMode(IngredientMergeMode.values()[ordinal]);
            return true;
        });
    }

    public void setMode(IngredientMergeMode mergeMode) {
        this.mergeMode = mergeMode;
    }

    public IngredientMergeMode getMergeMode() {
        return mergeMode;
    }

    @Override
    public void drawImage(Minecraft mc) {
        mc.renderEngine.bindTexture(new ResourceLocation("neenergistics", "textures/gui/states.png"));
        this.drawTexturedModalRect(0, 0, 0, 0, 16, 16);
        if (this.mergeMode == IngredientMergeMode.DISABLED) {
            this.drawTexturedModalRect(0, 0, 0, 16, 16, 16);
        }
        if (this.mergeMode == IngredientMergeMode.ENABLED) {
            this.drawTexturedModalRect(0, 0, 0, 32, 16, 16);
        }

    }

    @Override
    public String getMessage() {
        return I18n.format("gui.neenergistics.button.title.combination") +
                "\n" +
                I18n.format("gui.neenergistics.button.tooltip.combination", mergeMode.getLocalName());
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
