package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.utils.BlackIngredient;
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

public class IngredientBlackList {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final IngredientBlackList INSTANCE = new IngredientBlackList();

    private final List<BlackIngredient> blackList = new ArrayList<>();

    private IngredientBlackList() {

    }

    public void addPreference(ItemStack stack, String acceptedType) {
        BlackIngredient ingredient = new BlackIngredient(stack, acceptedType);
        if (!blackList.contains(ingredient)) {
            blackList.add(ingredient);
            saveList();
        }
    }

    public List<BlackIngredient> getBlackList() {
        return blackList;
    }

    public void loadList() {

        File file = NEEConfig.getBlacklistFile();
        if (file == null || !file.exists()) {
            return;
        }

        List<String> strings;
        try (FileReader reader = new FileReader(file)) {
            strings = IOUtils.readLines(reader);
        } catch (IOException e) {
            LOGGER.error("Failed to load blacklist from file {}", file, e);
            return;
        }

        List<BlackIngredient> list = strings.stream()
                .map(jsonString -> {
                    try {
                        return JsonToNBT.getTagFromJson(jsonString);
                    } catch (NBTException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(tagCompound -> {
                    ItemStack identifier = new ItemStack(tagCompound.getCompoundTag("identifier"));
                    String acceptedType = tagCompound.hasKey("acceptedType") ? tagCompound.getString("acceptedType") : null;
                    return new BlackIngredient(identifier, acceptedType);
                })
                .collect(Collectors.toList());

        this.blackList.clear();
        this.blackList.addAll(list);
    }

    public void saveList() {
        File file = NEEConfig.getBlacklistFile();
        if (file != null) {
            List<String> strings = blackList.stream()
                    .map(blackIngredient -> blackIngredient.toTag().toString())
                    .collect(Collectors.toList());
            try (FileWriter writer = new FileWriter(file)) {
                IOUtils.writeLines(strings, "\n", writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save blacklist to file {}", file, e);
            }
        }
    }

}
