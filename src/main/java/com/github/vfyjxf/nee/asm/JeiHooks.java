package com.github.vfyjxf.nee.asm;

import com.github.vfyjxf.nee.jei.CraftingInfoError;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.GuiButton;

@SuppressWarnings("unused")
public class JeiHooks {

    public static void setButtonEnable(GuiButton button, IRecipeTransferError error) {
        if (error instanceof CraftingInfoError) {
            button.enabled = true;
        }
    }


}
