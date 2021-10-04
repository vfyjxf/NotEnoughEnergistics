package com.github.vfyjxf.nee.utils;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.me.ItemRepo;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import com.github.vfyjxf.nee.config.NEEConfig;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IngredientTracker {

    private List<IAEItemStack> craftbleAEItemStacks;
    private List<Ingredient> ingredients = new ArrayList<>();

    public IngredientTracker(GuiContainer gui, IRecipeHandler recipe, int recipeIndex) {
        this.craftbleAEItemStacks = getCraftableStacks(gui);
        List<PositionedStack> requiredIngredients = new ArrayList<>();
        for (PositionedStack positionedStack : recipe.getIngredientStacks(recipeIndex)) {
            boolean find = false;
            for (PositionedStack currentIngredient : requiredIngredients) {
                boolean areItemStackEquals = currentIngredient.items[0].isItemEqual(positionedStack.items[0]) && ItemStack.areItemStackTagsEqual(currentIngredient.items[0], positionedStack.items[0]);
                if (areItemStackEquals) {
                    currentIngredient.items[0].stackSize += positionedStack.items[0].stackSize;
                    find = true;
                }
            }

            if (!find) {
                requiredIngredients.add(positionedStack);
            }

        }
        for (PositionedStack requiredIngredient : requiredIngredients) {
            Ingredient ingredient = new Ingredient(requiredIngredient);
            ingredients.add(ingredient);

            for (IAEItemStack craftableStack : NEEConfig.matchOtherItems ? this.getAEStacks(gui) : craftbleAEItemStacks) {
                if (requiredIngredient.contains(craftableStack.getItemStack())) {
                    ingredient.setCraftableIngredient(craftableStack.getItemStack());
                    ingredient.addCurrentCount(craftableStack.getItemStack().stackSize);
                }
            }
        }
    }

    public IngredientTracker(GuiContainer gui, List<PositionedStack> requiredIngredients) {
        IItemList<IAEItemStack> aeItemStacks = this.getAEStacks(gui);
        for (PositionedStack positionedStack : requiredIngredients) {
            this.ingredients.add(new Ingredient(positionedStack));
        }
        for (Ingredient ingredient : this.ingredients) {
            for (IAEItemStack stack : aeItemStacks) {
                if (ingredient.getIngredients().contains(stack.getItemStack())) {
                    if (ingredient.getCraftableIngredient() == null && stack.isCraftable()) {
                        ingredient.setCraftableIngredient(stack.getItemStack());
                    }
                    if (stack.getStackSize() > 0) {
                        ingredient.addCurrentCount(stack.getStackSize());
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

    private List<IAEItemStack> getCraftableStacks(GuiContainer gui) {
        List<IAEItemStack> craftableAEItemSafcks = new ArrayList<>();
        IItemList<IAEItemStack> list = null;
        try {
            ItemRepo repo = (ItemRepo) ReflectionHelper.findField(GuiMEMonitorable.class, "repo").get(gui);
            list = (IItemList<IAEItemStack>) ReflectionHelper.findField(ItemRepo.class, "list").get(repo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (list != null) {
            for (IAEItemStack stack : list) {
                if (stack.isCraftable()) {
                    craftableAEItemSafcks.add(stack.copy());
                }
            }
        }
        return craftableAEItemSafcks;
    }

    private IItemList<IAEItemStack> getAEStacks(GuiContainer gui) {
        IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
        try {
            ItemRepo repo = (ItemRepo) ReflectionHelper.findField(GuiMEMonitorable.class, "repo").get(gui);
            for (IAEItemStack stack : (IItemList<IAEItemStack>) ReflectionHelper.findField(ItemRepo.class, "list").get(repo)) {
                list.add(stack.copy());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getRequireToCraftStacks() {
        List<ItemStack> requireToCraftStacks = new ArrayList<>();
        for (Ingredient ingredient : this.ingredients) {
            ItemStack craftableStack = ingredient.getCraftableIngredient();
            if (craftableStack != null && ingredient.requiresToCraft()) {
                ItemStack requireStack = craftableStack.copy();
                requireStack.stackSize = (int) ingredient.getMissingCount();
                requireToCraftStacks.add(requireStack);
            }
        }
        return requireToCraftStacks;
    }

    public void addIngredientStack(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.requiresToCraft()) {
                ItemStack craftableStack = ingredient.getCraftableIngredient();
                if (craftableStack != null && craftableStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(craftableStack, stack)) {
                    ingredient.addCurrentCount(stack.stackSize);
                    break;
                }
            }
        }
    }

}
