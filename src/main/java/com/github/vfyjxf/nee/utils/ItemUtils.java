package com.github.vfyjxf.nee.utils;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.config.NEEConfig.*;

/**
 * @author vfyjxf
 */
public final class ItemUtils {

    private static List<StackProcessor> getTransformItemBlacklist() {
        List<StackProcessor> transformItemBlacklist = new ArrayList<>();
        for (String itemJsonString : itemBlacklist) {
            JsonObject jsonObject;
            try {
                jsonObject = new JsonParser().parse(itemJsonString).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                NotEnoughEnergistics.logger.error("Found a error item json in transform blacklist : " + itemJsonString);
                continue;
            }
            if (jsonObject != null) {
                String itemName = jsonObject.get("itemName").getAsString();
                if (itemName == null || itemName.isEmpty()) {
                    continue;
                }
                int meta = itemJsonString.contains("meta") ? Integer.parseInt(jsonObject.get("meta").getAsString()) : 0;
                String nbtJsonString = itemJsonString.contains("nbt") ? jsonObject.get("nbt").getAsString() : "";
                ItemStack currentStack = GameRegistry.makeItemStack(itemName, meta, 1, nbtJsonString);
                String recipeType = itemJsonString.contains("recipeType") ? jsonObject.get("name").getAsString() : "";
                transformItemBlacklist.add(new StackProcessor(currentStack, recipeType));
            }
        }
        return transformItemBlacklist;
    }

    private static List<StackProcessor> getTransformItemPriorityList() {
        List<StackProcessor> transformItemPriorityList = new ArrayList<>();
        for (String itemJsonString : itemPriorityList) {
            JsonObject jsonObject;
            try {
                jsonObject = new JsonParser().parse(itemJsonString).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                NotEnoughEnergistics.logger.error("Found a error item json in item priority list: " + itemJsonString);
                continue;
            }
            if (jsonObject != null) {
                String itemName = jsonObject.get("itemName").getAsString();
                if (itemName == null || itemName.isEmpty()) {
                    continue;
                }
                int meta = itemJsonString.contains("meta") ? Integer.parseInt(jsonObject.get("meta").getAsString()) : 0;
                String nbtJsonString = itemJsonString.contains("nbt") ? jsonObject.get("nbt").getAsString() : "";
                ItemStack currentStack = GameRegistry.makeItemStack(itemName, meta, 1, nbtJsonString);
                String recipeType = itemJsonString.contains("recipeType") ? jsonObject.get("name").getAsString() : "";
                transformItemPriorityList.add(new StackProcessor(currentStack, recipeType));
            }
        }
        return transformItemPriorityList;
    }

    public static boolean isPreferItems(ItemStack itemStack, String recipeType) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        for (StackProcessor stackProcessor : getTransformItemPriorityList()) {
            if (ItemStack.areItemStacksEqual(stack, stackProcessor.getCurrentStack())) {
                String currentRecipeType = stackProcessor.getRecipeType();
                if (currentRecipeType == null || currentRecipeType.isEmpty()) {
                    return true;
                } else if (recipeType.equals(currentRecipeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPreferItems(ItemStack itemStack) {
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        for (StackProcessor stackProcessor : getTransformItemPriorityList()) {
            if (ItemStack.areItemStacksEqual(stack, stackProcessor.getCurrentStack())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInBlackList(ItemStack itemStack, String recipeType) {
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        for (StackProcessor stackProcessor : getTransformItemBlacklist()) {
            if (ItemStack.areItemStacksEqual(stack, stackProcessor.getCurrentStack())) {
                String currentRecipeType = stackProcessor.getRecipeType();
                if (currentRecipeType == null || currentRecipeType.isEmpty()) {
                    return true;
                } else if (recipeType.equals(currentRecipeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack getPreferModItem(IGuiIngredient<ItemStack> ingredient) {
        for (String currentId : modPriorityList) {
            for (ItemStack currentIngredient : ingredient.getAllIngredients()) {
                String itemModid = currentIngredient.getItem().getRegistryName().getNamespace();
                if (itemModid.equals(currentId)) {
                    return currentIngredient;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isPreferModItem(ItemStack stack) {
        for (String currentId : modPriorityList) {
            String itemModid = stack.getItem().getRegistryName().getNamespace();
            if (itemModid.equals(currentId)) {
                return true;
            }
        }
        return false;
    }

    public static int getIngredientIndex(ItemStack stack, List<ItemStack> currentIngredients) {
        for (int i = 0; i < currentIngredients.size(); i++) {

            if (currentIngredients.get(i) == null) {
                continue;
            }

            if (ItemUtils.areItemStacksEqual(stack, currentIngredients.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2) {

        if (stack1 == null && stack2 == null) {
            return true;
        }

        if (stack1 != null && stack2 != null) {
            ItemStack copyStack1 = stack1.copy();
            ItemStack copyStack2 = stack2.copy();
            copyStack1.setCount(1);
            copyStack2.setCount(1);
            return ItemStack.areItemStacksEqual(copyStack1, copyStack2);
        }

        return false;
    }


    public static String toItemJsonString(ItemStack currentStack) {
        String nbtString = currentStack.hasTagCompound() ? ",\"nbt\":" + "\"" + currentStack.getTagCompound().toString() + "\"" : "";
        ResourceLocation registryName = currentStack.getItem().getRegistryName();
        int meta = currentStack.getItemDamage();
        return "{" + "\"itemName\":" + "\"" + registryName.toString() + "\"" + "," + "\"meta\":" + "\"" + meta + "\"" + nbtString + "}";
    }

    public static boolean hasModId(String modid) {
        for (String currentId : modPriorityList) {
            if (currentId.equals(modid)) {
                return true;
            }
        }
        return false;
    }

}
