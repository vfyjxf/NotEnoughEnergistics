package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.helper.PreferenceHelper;
import com.github.vfyjxf.nee.integration.IToolHelper;
import com.github.vfyjxf.nee.integration.RecipeToolManager;
import com.github.vfyjxf.nee.utils.PreferenceIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PreferenceList {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final PreferenceList INSTANCE = new PreferenceList();

    private final List<PreferenceIngredient> preferenceList = new ArrayList<>();

    private PreferenceList() {

    }

    public List<PreferenceIngredient> getPreferenceList() {
        return preferenceList;
    }

    public void addPreference(ItemStack stack, String acceptedType) {
        IToolHelper toolHelper = RecipeToolManager.INSTANCE.getToolHelper(stack.getItem());
        boolean isTool = toolHelper != null;
        PreferenceIngredient ingredient = new PreferenceIngredient(stack.copy(), isTool, acceptedType);
        if (!preferenceList.contains(ingredient)) {
            preferenceList.add(ingredient);
            saveList();
        }
    }

    public void removePreference(ItemStack stack) {
        PreferenceIngredient ingredient = PreferenceHelper.getPreferIngredient(stack, null);
        if (ingredient != null) {
            preferenceList.remove(ingredient);
            saveList();
        }
    }

    public void loadList() {

        File file = NEEConfig.getPreferenceConfigFile();
        if (file == null || !file.exists()) {
            return;
        }

        List<String> strings;
        try (FileReader reader = new FileReader(file)) {
            strings = IOUtils.readLines(reader);
        } catch (IOException e) {
            LOGGER.error("Failed to load preference list from file {}", file, e);
            return;
        }

        List<PreferenceIngredient> list = strings.stream()
                .map(jsonString -> {
                    try {
                        return JsonToNBT.getTagFromJson(jsonString);
                    } catch (NBTException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(tagCompound -> {
                    ItemStack identifier = new ItemStack(tagCompound.getCompoundTag("identifier"));
                    boolean isTool = tagCompound.getBoolean("isTool");
                    String acceptedType = tagCompound.hasKey("acceptedType") ? tagCompound.getString("acceptedType") : null;
                    return new PreferenceIngredient(identifier, isTool, acceptedType);
                })
                .collect(Collectors.toList());

        this.preferenceList.clear();
        this.preferenceList.addAll(list);

    }

    public void saveList() {
        File file = NEEConfig.getPreferenceConfigFile();
        if (file != null) {
            List<String> strings = preferenceList.stream()
                    .map(preferenceIngredient -> preferenceIngredient.toTag().toString())
                    .collect(Collectors.toList());
            try (FileWriter writer = new FileWriter(file)) {
                IOUtils.writeLines(strings, "\n", writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save preference list to file {}", file, e);
            }
        }
    }

}
