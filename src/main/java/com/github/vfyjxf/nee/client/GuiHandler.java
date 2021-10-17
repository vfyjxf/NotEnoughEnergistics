package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingTerm;
import com.github.vfyjxf.nee.jei.CraftingHelperTooltipError;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.*;

public class GuiHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (event.getGui() instanceof GuiCraftingTerm && currentScreen instanceof GuiCraftConfirm && tracker != null) {
            if (tracker.getRequireToCraftStacks().size() > 1 && stackIndex < tracker.getRequireToCraftStacks().size()) {
                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingHelper(tracker.getRequireToCraftStacks().get(stackIndex), noPreview));
                stackIndex++;
            }
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof RecipesGui) {
            for (GuiButton button : event.getButtonList()) {
                if (button instanceof RecipeTransferButton) {
                    IRecipeTransferError recipeTransferError = null;
                    try {
                        recipeTransferError = (IRecipeTransferError) ObfuscationReflectionHelper.findField(RecipeTransferButton.class, "recipeTransferError").get(button);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (recipeTransferError instanceof CraftingHelperTooltipError) {
                        button.enabled = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
            for (GuiButton button : event.getButtonList()) {
                if (button instanceof RecipeTransferButton) {
                    IRecipeTransferError recipeTransferError = null;
                    try {
                        recipeTransferError = (IRecipeTransferError) ObfuscationReflectionHelper.findField(RecipeTransferButton.class, "recipeTransferError").get(button);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (recipeTransferError instanceof CraftingHelperTooltipError) {
                        button.enabled = true;
                    }
                }
            }
        }
    }

}
