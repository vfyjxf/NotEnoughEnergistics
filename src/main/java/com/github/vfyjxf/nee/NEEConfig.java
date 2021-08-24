package com.github.vfyjxf.nee;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * @author vfyjxf
 */
public class NEEConfig {


    public static String[] transformBlacklist = new String[0];
    public static String[] transformPriorityList = new String[0];
    public static String[] transformPriorityModList = new String[0];

    public static void loadConfig(File configFile) {
        Configuration config = new Configuration(configFile);
        config.load();

        transformBlacklist = config.get("client", "transformItemBlacklist", new String[0],
                "If item in the blacklist, it will not be transferred.\n" +
                        "the format is \" {modid:modid,name:name,meta:meta,recipeProcessor:recipeProcessorID,identifier:identifier}\"\n"+
                        "example: \"{\"modid\":\"minecraft\",\"name\":\"iron_ingot\",\"recipeProcessor\":\"EnderIO\",\"identifier\":\"EnderIOAlloySmelter\"}\"").getStringList();
        transformPriorityList = config.get("client", "transformItemPriorityList", new String[0],
                "If item in tne priority list, it will be transferred first.").getStringList();

        transformPriorityModList = config.get("client","transformPriorityModList",new String[0],
                "if oredict has this mod's item, use it first").getStringList();

        if (config.hasChanged()) config.save();
    }

}
