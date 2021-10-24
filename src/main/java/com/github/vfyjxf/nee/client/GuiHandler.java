package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingTerm;
import com.github.vfyjxf.nee.jei.CraftingHelperTooltipError;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingHelper;
import com.github.vfyjxf.nee.utils.GuiUtils;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.*;

public class GuiHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        boolean isCraftingGui = event.getGui() instanceof GuiCraftingTerm || GuiUtils.isGuiWirelessCrafting(event.getGui());
        boolean isCraftConfirmGui = currentScreen instanceof GuiCraftConfirm || GuiUtils.isWirelessGuiCraftConfirm(currentScreen);
        if (isCraftingGui && isCraftConfirmGui && tracker != null) {
            if (tracker.getRequireToCraftStacks().size() > 1 && stackIndex < tracker.getRequireToCraftStacks().size()) {
                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingHelper(tracker.getRequireToCraftStacks().get(stackIndex), noPreview));
                stackIndex++;
            }
        }

    }

    //TODO:add some buttons to change settings
    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof RecipesGui) {
            for (GuiButton button : event.getButtonList()) {
                if (!button.enabled && button instanceof RecipeTransferButton) {
                    IRecipeTransferError recipeTransferError = ObfuscationReflectionHelper.getPrivateValue(RecipeTransferButton.class, (RecipeTransferButton) button, "recipeTransferError");
                    if (recipeTransferError instanceof CraftingHelperTooltipError) {
                        button.enabled = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Post event) {
        if (event.getGui() instanceof RecipesGui) {
            RecipesGui recipesGui = (RecipesGui) event.getGui();
            List<RecipeLayout> recipeLayouts = ObfuscationReflectionHelper.getPrivateValue(RecipesGui.class, recipesGui, "recipeLayouts");
            if (recipeLayouts != null) {
                for (RecipeLayout recipeLayout : recipeLayouts) {
                    RecipeTransferButton recipeTransferButton = recipeLayout.getRecipeTransferButton();
                    if (recipeTransferButton != null && !recipeTransferButton.enabled) {
                        IRecipeTransferError recipeTransferError;
                        recipeTransferError = ObfuscationReflectionHelper.getPrivateValue(RecipeTransferButton.class, recipeTransferButton, "recipeTransferError");
                        if (recipeTransferError instanceof CraftingHelperTooltipError) {
                            recipeTransferButton.enabled = true;
                        }
                    }
                }
            }
        }
    }

}
