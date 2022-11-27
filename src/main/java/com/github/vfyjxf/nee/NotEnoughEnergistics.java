package com.github.vfyjxf.nee;

import com.github.vfyjxf.nee.network.NEEGuiHandler;
import com.github.vfyjxf.nee.proxy.CommonProxy;
import com.github.vfyjxf.nee.utils.Globals;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = Globals.MOD_ID,
        version = Globals.VERSION,
        name = Globals.NAME,
        dependencies = Globals.DEPENDENCIES,
        guiFactory = Globals.GUI_FACTORY,
        useMetadata = true)
public class NotEnoughEnergistics {
    public static final Logger logger = LogManager.getLogger("NotEnoughEnergistics");

    @Mod.Instance(Globals.MOD_ID)
    public static NotEnoughEnergistics instance;

    @SidedProxy(clientSide = "com.github.vfyjxf.nee.proxy.ClientProxy", serverSide = "com.github.vfyjxf.nee.proxy.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new NEEGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }


}
