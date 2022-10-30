package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.utils.Gobals;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author vfyjxf
 */
public class NEEConfig {

    private NEEConfig() {

    }

    public static final String CATEGORY_PATTERN_TRANSFER = "transfer";
    public static final String CATEGORY_CRAFTING_HELPER = "helper";
    public static final String CATEGORY_OTHER_SETTINGS = "other";

    private static Configuration config;
    private static File modConfigFile;

    private static File preferenceConfigFile;
    private static File blacklistFile;

    private static boolean printRecipeType = false;
    private static boolean useDisplayed = false;
    /**
     * Priority: blacklist > preference list > priority mods
     */
    private static List<String> priorityMods = new ArrayList<>();
    private static List<ItemStack> preferenceList = new ArrayList<>();
    private static List<ItemStack> blacklist = new ArrayList<>();
    private static IngredientMergeMode mergeMode = IngredientMergeMode.ENABLED;
    private static List<String> mergeBlacklist = new ArrayList<>();

    private static boolean matchOtherItems = true;
    private static int updateIntervalTime = 1500;

    private static boolean syncIngredientSwitcher = true;

    public static void preInit(FMLPreInitializationEvent event) {
        File configDir = new File(event.getModConfigurationDirectory(), Gobals.MOD_ID);
        modConfigFile = new File(configDir, Gobals.MOD_ID + ".cfg");
        preferenceConfigFile = new File(configDir, "preference.ini");
        blacklistFile = new File(configDir, "blacklist.ini");
        config = new Configuration(modConfigFile);

        loadConfig();

    }

    public static Configuration getConfig() {
        return config;
    }

    private static void loadConfig() {

        if (config == null) {
            return;
        }

        config.load();

        {
            printRecipeType = config.getBoolean(
                    "PrintRecipeType",
                    CATEGORY_PATTERN_TRANSFER,
                    printRecipeType,
                    "If true, print current recipe type in log.Default:false"
            );

            useDisplayed = config.getBoolean(
                    "UseDisplayed",
                    CATEGORY_PATTERN_TRANSFER,
                    useDisplayed,
                    "If true, the ingredient currently displayed by JEI will be transferred.Default:false"
            );

            mergeMode = IngredientMergeMode.valueOf(
                    config.getString(
                            "MergeMode",
                            CATEGORY_PATTERN_TRANSFER,
                            mergeMode.name(),
                            "Ingredient Merge Mode Setting, here are all the modes. \n" + "\"ENABLED\"" + " " + "\"DISABLED\"" + " " + "WHITELIST"
                    )
            );

            mergeBlacklist = Arrays.asList(
                    config.getStringList(
                            "MergeBlackList",
                            CATEGORY_PATTERN_TRANSFER,
                            mergeBlacklist.toArray(new String[0]),
                            "If a recipe type is in here, it will not be merged."
                    )
            );

            priorityMods = Arrays.asList(config.getStringList(
                    "PriorityMods",
                    CATEGORY_PATTERN_TRANSFER,
                    priorityMods.toArray(new String[0]),
                    "If oredict has this mod's item, use it first."
            ));

            loadList();
        }

        {
            matchOtherItems = config.getBoolean(
                    "MatchOtherItems",
                    CATEGORY_CRAFTING_HELPER,
                    matchOtherItems,
                    "If true, Crafting Helper will match other items even they can't auto-crafting"
            );

            updateIntervalTime = config.getInt(
                    "UpdateIntervalTime",
                    CATEGORY_CRAFTING_HELPER,
                    updateIntervalTime,
                    -1,
                    60000,
                    "Time interval in milliseconds for updating recipe information in Recipe Gui.\n" +
                            " Never updated when set to -1."
            );

        }

        {
            syncIngredientSwitcher = config.getBoolean(
                    "AllowSynchronousSwitchIngredient",
                    CATEGORY_OTHER_SETTINGS,
                    syncIngredientSwitcher,
                    "If true, it will make all similar ingredient switch at the same time."
            );
        }

        if (config.hasChanged()) {
            config.save();
        }

    }

    private static void loadList() {

        try (FileReader reader = new FileReader(preferenceConfigFile)) {
            List<String> strings = IOUtils.readLines(reader);
            preferenceList = strings.stream()
                    .map(s -> {
                        try {
                            NBTTagCompound tag = JsonToNBT.getTagFromJson(s);
                            return new ItemStack(tag);
                        } catch (NBTException e) {
                            NotEnoughEnergistics.logger.error("Loading prefer ingredient :" + s + " failed!");
                        }
                        return ItemStack.EMPTY;
                    })
                    .filter(is -> !is.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            NotEnoughEnergistics.logger.error("Can't load preference list!");
        }

        try (FileReader reader = new FileReader(blacklistFile)) {
            List<String> strings = IOUtils.readLines(reader);
            blacklist = strings.stream()
                    .map(s -> {
                        try {
                            NBTTagCompound tag = JsonToNBT.getTagFromJson(s);
                            return new ItemStack(tag);
                        } catch (NBTException e) {
                            NotEnoughEnergistics.logger.error("Loading ingredient :" + s + " in blacklist failed!");
                        }
                        return ItemStack.EMPTY;
                    })
                    .filter(is -> !is.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            NotEnoughEnergistics.logger.error("Can't load blacklist!");
        }

    }

    public static boolean isPrintRecipeType() {
        return printRecipeType;
    }

    public static boolean isUseDisplayed() {
        return useDisplayed;
    }

    public static List<String> getPriorityMods() {
        return priorityMods;
    }

    public static List<ItemStack> getPreferenceList() {
        return preferenceList;
    }

    public static List<ItemStack> getBlacklist() {
        return blacklist;
    }

    public static IngredientMergeMode getMergeMode() {
        return mergeMode;
    }

    public static List<String> getMergeBlacklist() {
        return mergeBlacklist;
    }

    public static boolean isMatchOtherItems() {
        return matchOtherItems;
    }

    public static int getUpdateIntervalTime() {
        return updateIntervalTime;
    }

    public static boolean isSyncIngredientSwitcher() {
        return syncIngredientSwitcher;
    }

    public static File getModConfigFile() {
        return modConfigFile;
    }

    public static void setPriorityMods(String[] priorityMods) {
        NEEConfig.priorityMods = Arrays.asList(priorityMods);
        config.get(CATEGORY_PATTERN_TRANSFER, "modPriorityList", priorityMods,
                "If oredict has this mod's item, use it first.").set(priorityMods);
        config.save();
    }

    public static void setItemBlacklist(List<String> itemBlacklist) {
//        NEEConfig.itemBlacklist = itemBlacklist;
        config.get(CATEGORY_PATTERN_TRANSFER, "itemBlacklist", itemBlacklist.toArray(new String[0]),
                "If item in the blacklist, it will not be transferred.\n"
                        + "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}").set(itemBlacklist.toArray(new String[0]));
        config.save();
    }

    public static void setItemPriorityList(String[] itemPriorityList) {
//        NEEConfig.itemPriorityList = itemPriorityList;
        config.get(CATEGORY_PATTERN_TRANSFER, "itemPriorityList", itemPriorityList,
                "If item in tne priority list, it will be transferred first.\n"
                        + "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}").set(itemPriorityList);
        config.save();
    }


    public static void setMergeMode(IngredientMergeMode mergeMode) {
        NEEConfig.mergeMode = mergeMode;
        config.get(
                CATEGORY_PATTERN_TRANSFER,
                "MergeMode",
                mergeMode.name(),
                "Item Combination Mode Setting, here are all the modes. \n" + "\"ENABLED\"" + " " + "\"DISABLED\"" + " " + "WHITELIST"
        ).set(mergeMode.name());
        config.save();
    }

    public static void setMergeBlacklist(String[] mergeBlacklist) {
        NEEConfig.mergeBlacklist = Arrays.asList(mergeBlacklist);
        config.get(CATEGORY_PATTERN_TRANSFER, "itemCombinationWhitelist", mergeBlacklist, "Whitelist for item combination").set(mergeBlacklist);
        config.save();
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Gobals.MOD_ID)) {
            if (config.hasChanged()) {
                config.save();
            }
            loadConfig();
        }
    }
}
