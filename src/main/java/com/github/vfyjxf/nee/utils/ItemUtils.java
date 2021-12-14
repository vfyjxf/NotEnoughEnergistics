package com.github.vfyjxf.nee.utils;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.config.NEEConfig.CLIENT_CONFIG;

/**
 * @author vfyjxf
 */
public final class ItemUtils {


    private static ItemStack makeItemStack(String itemName, int stackSize, String nbtString) {

        if (itemName == null || itemName.isEmpty()) {
            throw new IllegalArgumentException("The itemName cannot be null");
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            NotEnoughEnergistics.logger.error("Unable to find item with name {}", itemName);
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = new ItemStack(item, stackSize);
        if (!Strings.isNullOrEmpty(nbtString)) {
            try {
                itemStack.setTag(JsonToNBT.parseTag(nbtString));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException("Encountered an exception parsing ItemStack NBT string " + nbtString, e);
            }
        }
        return itemStack;
    }

    private static ItemStack makeItemStack(String itemName, String nbtString) {
        return makeItemStack(itemName, 1, nbtString);
    }

    private static List<StackProcessor> getTransformItemBlacklist() {
        List<StackProcessor> transformItemBlacklist = new ArrayList<>();
        CLIENT_CONFIG.getItemBlacklist().forEach(itemJsonString -> {
            JsonObject jsonObject = null;
            try {
                jsonObject = new JsonParser().parse(itemJsonString).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                NotEnoughEnergistics.logger.error("Found a error item json in transform blacklist : " + itemJsonString);
            }
            if (jsonObject != null) {
                String itemName = jsonObject.get("itemName").getAsString();
                if (itemName == null || itemName.isEmpty()) {
                    return;
                }
                String nbtJsonString = itemJsonString.contains("nbt") ? jsonObject.get("nbt").getAsString() : "";
                ItemStack currentStack = makeItemStack(itemName, nbtJsonString);
                String recipeType = itemJsonString.contains("recipeType") ? jsonObject.get("recipeType").getAsString() : "";
                transformItemBlacklist.add(new StackProcessor(currentStack, recipeType));
            }
        });
        return transformItemBlacklist;
    }

    private static List<StackProcessor> getTransformItemPreferenceList() {
        List<StackProcessor> transformItemPriorityList = new ArrayList<>();
        CLIENT_CONFIG.getListItemPreference().forEach(itemJsonString -> {
            JsonObject jsonObject = null;
            try {
                jsonObject = new JsonParser().parse(itemJsonString).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                NotEnoughEnergistics.logger.error("Found a error item json in transform item preference list : " + itemJsonString);
            }
            if (jsonObject != null) {
                String itemName = jsonObject.get("itemName").getAsString();
                if (itemName == null || itemName.isEmpty()) {
                    return;
                }
                String nbtJsonString = itemJsonString.contains("nbt") ? jsonObject.get("nbt").getAsString() : "";
                ItemStack currentStack = makeItemStack(itemName, nbtJsonString);
                String recipeType = itemJsonString.contains("recipeType") ? jsonObject.get("recipeType").getAsString() : "";
                transformItemPriorityList.add(new StackProcessor(currentStack, recipeType));
            }
        });
        return transformItemPriorityList;
    }

    public static boolean isPreferItems(ItemStack itemStack, ResourceLocation recipeType) {
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        for (StackProcessor stackProcessor : getTransformItemPreferenceList()) {
            if (ItemStack.matches(stack, stackProcessor.getCurrentStack())) {
                ResourceLocation currentRecipeType = new ResourceLocation(stackProcessor.getRecipeType());
                if (stackProcessor.getRecipeType() == null || stackProcessor.getRecipeType().isEmpty()) {
                    return true;
                } else if (recipeType.equals(currentRecipeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInBlackList(ItemStack itemStack, ResourceLocation recipeType) {
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        for (StackProcessor stackProcessor : getTransformItemBlacklist()) {
            if (ItemStack.isSame(stack, stackProcessor.getCurrentStack())) {
                ResourceLocation currentRecipeType = new ResourceLocation(stackProcessor.getRecipeType());
                if (stackProcessor.getRecipeType() == null || stackProcessor.getRecipeType().isEmpty()) {
                    return true;
                } else if (recipeType.equals(currentRecipeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack getPreferModItem(IGuiIngredient<ItemStack> ingredient) {
        for (String currentId : CLIENT_CONFIG.getListModPreference()) {
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
        for (String currentId : CLIENT_CONFIG.getListModPreference()) {
            String itemModid = stack.getItem().getRegistryName().getNamespace();
            if (itemModid.equals(currentId)) {
                return true;
            }
        }
        return false;
    }

    public static int getIngredientIndex(ItemStack stack, List<ItemStack> currentIngredients) {
        ItemStack stackInput = stack.copy();
        stackInput.setCount(1);
        for (int i = 0; i < currentIngredients.size(); i++) {
            ItemStack currentStack = currentIngredients.get(i).copy();
            currentStack.setCount(1);
            if (ItemStack.isSame(stackInput, currentStack)) {
                return i;
            }
        }
        return -1;
    }

    public static String toItemJsonString(ItemStack currentStack) {
        String nbtString = currentStack.hasTag() ? ",\"nbt\":" + "\"" + currentStack.getTag().toString() + "\"" : "";
        ResourceLocation registryName = currentStack.getItem().getRegistryName();
        return "{" + "\"itemName\":" + "\"" + registryName.toString() + "\"" + nbtString + "}";
    }

    public static boolean hasModId(String modid) {
        for (String currentId : CLIENT_CONFIG.getListModPreference()) {
            if (currentId.equals(modid)) {
                return true;
            }
        }
        return false;
    }

}
