package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.utils.ItemUtils;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * @author vfyjxf
 */
public class NEEConfig {

    public static Configuration config;

    public static String[] transformBlacklist = new String[0];
    public static String[] transformPriorityList = new String[0];
    public static String[] transformPriorityModList = new String[0];

    public static boolean noShift = true;

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        config.load();

        transformBlacklist = config.get("client", "transformItemBlacklist", new String[0],
                "If item in the blacklist, it will not be transferred.\n" +
                        "the format is \" {modid:modid,name:name,meta:meta,recipeProcessor:recipeProcessorID,identifier:identifier}\"\n" +
                        "example: \"{modid:minecraft,name:iron_ingot,recipeProcessor:EnderIO,identifier:EnderIOAlloySmelter}\"").getStringList();
        transformPriorityList = config.get("client", "transformItemPriorityList", new String[0],
                "If item in the priority list, it will be transferred first.").getStringList();

        transformPriorityModList = config.get("client", "transformPriorityModList", new String[0],
                "if oredict has this mod's item, use it first").getStringList();

        if (config.hasChanged()) config.save();
    }

    public static void reload() {
        config.load();
        transformBlacklist = config.get("client", "transformItemBlacklist", new String[0],
                "If item in the blacklist, it will not be transferred.\n" +
                        "the format is \" {modid:modid,name:name,meta:meta,recipeProcessor:recipeProcessorID,identifier:identifier}\"\n" +
                        "example: \"{modid:minecraft,name:iron_ingot,recipeProcessor:EnderIO,identifier:EnderIOAlloySmelter}\"").getStringList();
        transformPriorityList = config.get("client", "transformItemPriorityList", new String[0],
                "If item in the priority list, it will be transferred first.").getStringList();

        transformPriorityModList = config.get("client", "transformPriorityModList", new String[0],
                "if oredict has this mod's item, use it first").getStringList();
        ItemUtils.reloadConfig();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (NotEnoughEnergistics.MODID.equals(event.modID)) {
            config.save();
            NEEConfig.reload();
        }
    }

}
