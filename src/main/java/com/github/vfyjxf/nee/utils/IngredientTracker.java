package com.github.vfyjxf.nee.utils;

import appeng.api.AEApi;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.me.ItemRepo;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import p455w0rd.wct.client.gui.GuiWCT;

import java.util.ArrayList;
import java.util.List;

public class IngredientTracker {

    private final List<Ingredient> ingredients = new ArrayList<>();
    private List<IAEItemStack> craftableStacks;

    public IngredientTracker(IRecipeLayout recipeLayout) {
        for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (guiIngredient.isInput() && !guiIngredient.getAllIngredients().isEmpty()) {
                ingredients.add(new Ingredient(guiIngredient));
            }
        }
        this.craftableStacks = getCraftableStacks();

        for (Ingredient ingredient : this.ingredients) {
            for (IAEItemStack stack : getStorageStacks()) {
                if (ItemUtils.getIngredientIndex(stack.asItemStackRepresentation(), ingredient.getIngredient().getAllIngredients()) >= 0) {
                    if (ingredient.getCraftableIngredient().isEmpty() && stack.isCraftable()) {
                        ingredient.setCraftableIngredient(stack.asItemStackRepresentation());
                    }
                    if (stack.getStackSize() > 0) {
                        ingredient.addCount(stack.getStackSize());
                        if (ingredient.requiresToCraft()) {
                            stack.setStackSize(0);
                        } else {
                            stack.setStackSize(stack.getStackSize() - ingredient.getRequireCount());
                        }
                    }
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private IItemList<IAEItemStack> getStorageStacks() {
        IItemList<IAEItemStack> list = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
            RecipesGui recipesGui = (RecipesGui) Minecraft.getMinecraft().currentScreen;
            GuiScreen parentScreen = ObfuscationReflectionHelper.getPrivateValue(RecipesGui.class, recipesGui, "parentScreen");
            ItemRepo repo = null;
            if (parentScreen instanceof GuiCraftingTerm || parentScreen instanceof GuiPatternTerm) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiMEMonitorable.class, (GuiMEMonitorable) parentScreen, "repo");
            } else if (GuiUtils.isGuiWirelessCrafting(parentScreen)) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiWCT.class, (GuiWCT) parentScreen, "repo");
            }
            if (repo != null) {
                for (IAEItemStack stack : (IItemList<IAEItemStack>) ObfuscationReflectionHelper.getPrivateValue(ItemRepo.class, repo, "list")) {
                    list.add(stack.copy());
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<IAEItemStack> getCraftableStacks() {
        List<IAEItemStack> craftableStacks = new ArrayList<>();
        if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
            RecipesGui recipesGui = (RecipesGui) Minecraft.getMinecraft().currentScreen;
            GuiScreen parentScreen = ObfuscationReflectionHelper.getPrivateValue(RecipesGui.class, recipesGui, "parentScreen");
            ItemRepo repo = null;
            if (parentScreen instanceof GuiCraftingTerm || parentScreen instanceof GuiPatternTerm) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiMEMonitorable.class, (GuiMEMonitorable) parentScreen, "repo");
            } else if (GuiUtils.isGuiWirelessCrafting(parentScreen)) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiWCT.class, (GuiWCT) parentScreen, "repo");
            }
            if (repo != null) {
                for (IAEItemStack stack : (IItemList<IAEItemStack>) ObfuscationReflectionHelper.getPrivateValue(ItemRepo.class, repo, "list")) {
                    craftableStacks.add(stack.copy());
                }
            }
        }
        return craftableStacks;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getRequireToCraftStacks() {
        List<ItemStack> requireToCraftStacks = new ArrayList<>();
        for (Ingredient ingredient : this.getIngredients()) {
            boolean find = false;
            if (ingredient.isCraftable() && ingredient.requiresToCraft()) {
                for (ItemStack stack : requireToCraftStacks) {
                    boolean areStackEqual = stack.isItemEqual(ingredient.getCraftableIngredient()) && ItemStack.areItemStackTagsEqual(stack, ingredient.getCraftableIngredient());
                    if (areStackEqual) {
                        stack.setCount((int) (stack.getCount() + ingredient.getMissingCount()));
                        find = true;
                    }

                }

                if (!find) {
                    ItemStack requireStack = ingredient.getCraftableIngredient().copy();
                    requireStack.setCount((int) ingredient.getMissingCount());
                    requireToCraftStacks.add(requireStack);
                }
            }
        }
        return requireToCraftStacks;
    }

    //TODO:make other stacks can be used,such as different woods
    public void addAvailableStack(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.requiresToCraft()) {
                ItemStack craftableStack = ingredient.getCraftableIngredient();
                if (!craftableStack.isEmpty() && craftableStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(craftableStack, stack)) {
                    int missingCount = (int) ingredient.getMissingCount();
                    ingredient.addCount(stack.getCount());
                    if (ingredient.requiresToCraft()) {
                        stack.setCount(0);
                    } else {
                        stack.setCount(stack.getCount() - missingCount);
                    }
                    break;
                }
            }
        }
    }

}
