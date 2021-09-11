package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


/**
 * @author vfyjxf
 */
@Config(modid = "neenergistics", name = "NotEnoughEnergistics", category = "transfer")
public class NEEConfig {

    @Comment("Automatic switch Pattern Terminal's Mode when click [+].Default:true")
    public static boolean allowAutomaticSwitchPatternTerminalMode = true;

    @Comment("If true, print current recipe type in log.Default:false")
    public static boolean allowPrintRecipeType = false;

    @Comment("If oredict has this mod's item, use it first.")
    public static String[] modPriorityList = new String[0];

    @Comment("If item in the blacklist, it will not be transferred.\n" +
            "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}")
    public static String[] itemBlacklist = new String[0];

    @Comment("If item in tne priority list, it will be transferred first.\n" +
            "example:{\"itemName\":\"gregtech:meta_item_2\",\"meta\":\"32492\"}")
    public static String[] itemPriorityList = new String[0];

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
        if(NotEnoughEnergistics.MODID.equals(event.getModID())){
            ConfigManager.sync(NotEnoughEnergistics.MODID, Config.Type.INSTANCE);
        }
    }

}
