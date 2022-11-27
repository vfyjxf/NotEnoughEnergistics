package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.utils.Globals;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    public static boolean networkOrInventoryFirst = true;
    /**
     * Priority: blacklist > preference list > priority mods
     */
    private static List<String> priorityMods = new ArrayList<>();
    private static IngredientMergeMode mergeMode = IngredientMergeMode.ENABLED;
    private static List<String> mergeBlacklist = new ArrayList<>();

    private static int updateIntervalTime = 1500;
    private static Color craftableHighlightColor = new Color(0.0f, 0.0f, 1.0f, 0.4f);
    private static Color missingHighlightColor = new Color(1.0f, 0.0f, 0.0f, 0.4f);

    private static boolean syncIngredientSwitcher = true;

    public static void preInit(FMLPreInitializationEvent event) {
        File configDir = new File(event.getModConfigurationDirectory(), Globals.MOD_ID);
        modConfigFile = new File(configDir, Globals.MOD_ID + ".cfg");
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
                    "If true, print current recipe type in log.[Default:false]"
            );

            useDisplayed = config.getBoolean(
                    "UseDisplayed",
                    CATEGORY_PATTERN_TRANSFER,
                    useDisplayed,
                    "If true, the ingredient currently displayed by JEI will be transferred.[Default:false]"
            );

            networkOrInventoryFirst = config.getBoolean(
                    "NetworkOrInventoryFirst",
                    CATEGORY_PATTERN_TRANSFER,
                    networkOrInventoryFirst,
                    "If true, the ingredient will be transferred from network first, then from inventory,and finally from jei.[Default:true]"
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

            updateIntervalTime = config.getInt(
                    "UpdateIntervalTime",
                    CATEGORY_CRAFTING_HELPER,
                    updateIntervalTime,
                    -1,
                    60000,
                    "Time interval in milliseconds for updating recipe information in Recipe Gui.\n" +
                            " Never updated when set to -1."
            );

            craftableHighlightColor = new Color(Integer.decode(
                    config.getString(
                            "CraftableHighlightColor",
                            CATEGORY_CRAFTING_HELPER,
                            "0x660000FF",
                            "Highlight colour of craftable ingredients in Recipe Gui.\n" +
                                    " Format: 0xRRGGBBAA"
                    )), true);

            missingHighlightColor = new Color(Integer.decode(
                    config.getString(
                            "MissingHighlightColour",
                            CATEGORY_CRAFTING_HELPER,
                            "0x66FF0000",
                            "Highlight colour of missing ingredients in Recipe Gui.\n" +
                                    " Format: 0xRRGGBBAA"
                    )), true);

        }

        {
            syncIngredientSwitcher = config.getBoolean(
                    "SyncIngredientSwitcher",
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

        PreferenceList.INSTANCE.loadList();
        IngredientBlackList.INSTANCE.loadList();

    }

    public static File getPreferenceConfigFile() {
        return preferenceConfigFile;
    }

    public static File getBlacklistFile() {
        return blacklistFile;
    }

    public static boolean isPrintRecipeType() {
        return printRecipeType;
    }

    public static boolean isUseDisplayed() {
        return useDisplayed;
    }

    public static boolean isNetworkOrInventoryFirst() {
        return networkOrInventoryFirst;
    }

    public static List<String> getPriorityMods() {
        return priorityMods;
    }

    public static IngredientMergeMode getMergeMode() {
        return mergeMode;
    }

    public static List<String> getMergeBlacklist() {
        return mergeBlacklist;
    }

    public static int getUpdateIntervalTime() {
        return updateIntervalTime;
    }

    public static Color getMissingHighlightColor() {
        return missingHighlightColor;
    }

    public static Color getCraftableHighlightColor() {
        return craftableHighlightColor;
    }

    public static boolean isSyncIngredientSwitcher() {
        return syncIngredientSwitcher;
    }

    public static File getModConfigFile() {
        return modConfigFile;
    }

    public static void setPriorityMods(String[] priorityMods) {
        NEEConfig.priorityMods = Arrays.asList(priorityMods);
        config.get(CATEGORY_PATTERN_TRANSFER,
                "PriorityMods",
                priorityMods,
                "If oredict has this mod's item, use it first."
        ).set(priorityMods);
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
        config.get(CATEGORY_PATTERN_TRANSFER,
                "MergeBlackList",
                mergeBlacklist,
                "If a recipe type is in here, it will not be merged."
        ).set(mergeBlacklist);
        config.save();
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Globals.MOD_ID)) {
            if (config.hasChanged()) {
                config.save();
            }
            loadConfig();
        }
    }
}
