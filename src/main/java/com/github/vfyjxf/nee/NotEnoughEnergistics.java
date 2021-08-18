package com.github.vfyjxf.nee;

import com.github.vfyjxf.nee.network.NEENetworkHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = NotEnoughEnergistics.MODID,
        version = NotEnoughEnergistics.VERSION,
        name = NotEnoughEnergistics.NAME,
        dependencies = NotEnoughEnergistics.DEPENDENCIES,
        useMetadata = true)
public class NotEnoughEnergistics {
    public static final String MODID = "neenergistics";
    public static final String NAME = "NotEnoughEnergistics";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "required-after:jei;required-after:appliedenergistics2";
    public static final Logger logger = LogManager.getLogger("NotEnoughEnergistics");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        NEENetworkHandler.init();
    }


}
