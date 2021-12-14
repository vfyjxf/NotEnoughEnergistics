package com.github.vfyjxf.nee.utils;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.ItemRepo;
import appeng.client.gui.me.items.PatternTermScreen;
import appeng.container.me.common.GridInventoryEntry;
import com.google.common.collect.BiMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.config.NEEConfig.CLIENT_CONFIG;

public class IngredientTracker {

    private final List<Ingredient> ingredients = new ArrayList<>();

    public IngredientTracker(IRecipeLayout recipeLayout) {
        for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (guiIngredient.isInput() && !guiIngredient.getAllIngredients().isEmpty()) {
                ingredients.add(new Ingredient(guiIngredient));
            }
        }

        for (Ingredient ingredient : this.ingredients) {
            List<IAEItemStack> stacks = CLIENT_CONFIG.getMatchOtherItems() ? getStorageStacks() : getCraftableStacks();
            for (IAEItemStack stack : stacks) {
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

    private List<IAEItemStack> getStorageStacks() {
        List<IAEItemStack> list = new ArrayList<>();
        if (Minecraft.getInstance().screen instanceof RecipesGui) {
            RecipesGui recipesGui = (RecipesGui) Minecraft.getInstance().screen;
            Screen parentScreen = ObfuscationReflectionHelper.getPrivateValue(RecipesGui.class, recipesGui, "parentScreen");
            ItemRepo repo = null;
            if (parentScreen instanceof CraftingTermScreen || parentScreen instanceof PatternTermScreen) {
                repo = ObfuscationReflectionHelper.getPrivateValue(MEMonitorableScreen.class, (MEMonitorableScreen) parentScreen, "repo");
            }
            if (repo != null) {
                BiMap<Long, GridInventoryEntry<IAEItemStack>> entries = ObfuscationReflectionHelper.getPrivateValue(Repo.class, repo, "entries");
                if (entries != null) {
                    for (GridInventoryEntry<IAEItemStack> entry : entries.values()) {
                        list.add(entry.getStack().copy());
                    }
                }
            }
        }
        return list;
    }

    public List<IAEItemStack> getCraftableStacks() {
        List<IAEItemStack> craftableStacks = new ArrayList<>();
        if (Minecraft.getInstance().screen instanceof RecipesGui) {
            RecipesGui recipesGui = (RecipesGui) Minecraft.getInstance().screen;
            Screen parentScreen = ObfuscationReflectionHelper.getPrivateValue(RecipesGui.class, recipesGui, "parentScreen");
            ItemRepo repo = null;
            if (parentScreen instanceof CraftingTermScreen || parentScreen instanceof PatternTermScreen) {
                repo = ObfuscationReflectionHelper.getPrivateValue(MEMonitorableScreen.class, (MEMonitorableScreen) parentScreen, "repo");
            }
            if (repo != null) {
                BiMap<Long, GridInventoryEntry<IAEItemStack>> entries = ObfuscationReflectionHelper.getPrivateValue(Repo.class, repo, "entries");
                if (entries != null) {
                    for (GridInventoryEntry<IAEItemStack> entry : entries.values()) {
                        if (entry.isCraftable()) {
                            craftableStacks.add(entry.getStack().copy());
                        }
                    }
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
                    boolean areStackEqual = stack.sameItem(ingredient.getCraftableIngredient()) && ItemStack.isSame(stack, ingredient.getCraftableIngredient());
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

    public void addAvailableStack(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.requiresToCraft()) {
                if (CLIENT_CONFIG.getMatchOtherItems()) {
                    boolean canUse = stack.getCount() > 0 && ItemUtils.getIngredientIndex(stack, ingredient.getIngredient().getAllIngredients()) >= 0;
                    if (canUse) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCount(stack.getCount());
                        if (ingredient.requiresToCraft()) {
                            stack.setCount(0);
                        } else {
                            stack.setCount(stack.getCount() - missingCount);
                        }
                        break;
                    }
                } else {
                    ItemStack craftableStack = ingredient.getCraftableIngredient();
                    if (!craftableStack.isEmpty() && craftableStack.sameItem(stack) && ItemStack.isSame(craftableStack, stack)) {
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

}
