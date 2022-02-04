package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;


/**
 * @author vfyjxf
 */
public class NEEConfig {

    private NEEConfig() {

    }


    public static final NEEConfig INSTANCE = new NEEConfig();

    public static Configuration config;
    public static final String CATEGORY_PATTERN_TRANSFER = "transfer";
    public static final String CATEGORY_CRAFTING_HELPER = "helper";
    public static final String CATEGORY_OTHER_SETTINGS = "other";


    public static boolean allowPrintRecipeType = false;
    public static boolean useDisplayedIngredient = false;
    public static String[] modPriorityList = new String[0];
    public static String[] itemBlacklist = new String[0];
    public static String[] itemPriorityList = new String[0];
    public static String itemCombinationMode = "ENABLED";
    public static String[] itemCombinationWhitelist = new String[0];

    public static boolean matchOtherItems = true;

    public static boolean allowSynchronousSwitchIngredient = true;

    public static void initConfig(File configFile) {
        config = new Configuration(configFile);

        config.load();

        {
            allowPrintRecipeType = config.getBoolean("allowPrintRecipeType", CATEGORY_PATTERN_TRANSFER, allowPrintRecipeType, "If true, print current recipe type in log.Default:false");
            useDisplayedIngredient = config.getBoolean("useDisplayedIngredient", CATEGORY_PATTERN_TRANSFER, useDisplayedIngredient, "If true, the ingredient currently displayed by JEI will be transferred.Default:false");
            itemCombinationMode = config.getString("itemCombinationMode", CATEGORY_PATTERN_TRANSFER, itemCombinationMode, "Item Combination Mode Setting, here are all the modes. \n" + "\"ENABLED\"" + " " + "\"DISABLED\"" + " " + "WHITELIST");
            itemCombinationWhitelist = config.getStringList("itemCombinationWhitelist", CATEGORY_PATTERN_TRANSFER, itemCombinationWhitelist, "Whitelist for item combination");
            itemPriorityList = config.getStringList("itemPriorityList", CATEGORY_PATTERN_TRANSFER, itemPriorityList, "If item in tne priority list, it will be transferred first.\n" + "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}");
            itemBlacklist = config.getStringList("itemBlacklist", CATEGORY_PATTERN_TRANSFER, itemBlacklist, "If item in the blacklist, it will not be transferred.\n" + "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}");
            modPriorityList = config.getStringList("modPriorityList", CATEGORY_PATTERN_TRANSFER, modPriorityList, "If oredict has this mod's item, use it first.");
        }

        {
            matchOtherItems = config.getBoolean("matchOtherItems", CATEGORY_CRAFTING_HELPER, matchOtherItems, "If true, Crafting Helper will match other items even they can't auto-crafting");
        }

        {
            allowSynchronousSwitchIngredient = config.getBoolean("allowSynchronousSwitchIngredient", CATEGORY_OTHER_SETTINGS, allowSynchronousSwitchIngredient, "If true, it will make all similar ingredient switch at the same time.");
        }

        if (config.hasChanged()) {
            config.save();
        }

    }

    public static void setModPriorityList(String[] modPriorityList) {
        NEEConfig.modPriorityList = modPriorityList;
        config.get(CATEGORY_PATTERN_TRANSFER, "modPriorityList", modPriorityList,
                "If oredict has this mod's item, use it first.").set(modPriorityList);
        config.save();
    }

    public static void setItemBlacklist(String[] itemBlacklist) {
        NEEConfig.itemBlacklist = itemBlacklist;
        config.get(CATEGORY_PATTERN_TRANSFER, "itemBlacklist", itemBlacklist,
                "If item in the blacklist, it will not be transferred.\n"
                        + "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}").set(itemBlacklist);
        config.save();
    }

    public static void setItemPriorityList(String[] itemPriorityList) {
        NEEConfig.itemPriorityList = itemPriorityList;
        config.get(CATEGORY_PATTERN_TRANSFER, "itemPriorityList", itemPriorityList,
                "If item in tne priority list, it will be transferred first.\n"
                        + "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}").set(itemPriorityList);
        config.save();
    }


    public static void setItemCombinationMode(String itemCombinationMode) {
        NEEConfig.itemCombinationMode = itemCombinationMode;
        config.get(CATEGORY_PATTERN_TRANSFER, "itemCombinationMode", itemCombinationMode,
                "Item Combination Mode Setting, here are all the modes. \n"
                        + "\"ENABLED\"" + " " + "\"DISABLED\"" + " " + "WHITELIST").set(itemCombinationMode);
        config.save();
    }

    public static void setItemCombinationWhitelist(String[] itemCombinationWhitelist) {
        NEEConfig.itemCombinationWhitelist = itemCombinationWhitelist;
        config.get(CATEGORY_PATTERN_TRANSFER, "itemCombinationWhitelist", itemCombinationWhitelist, "Whitelist for item combination").set(itemCombinationWhitelist);
        config.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (NotEnoughEnergistics.MODID.equals(event.getModID())) {
            ConfigManager.sync(NotEnoughEnergistics.MODID, Config.Type.INSTANCE);
        }
    }

}
