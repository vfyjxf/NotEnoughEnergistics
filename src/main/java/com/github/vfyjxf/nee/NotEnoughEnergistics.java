package com.github.vfyjxf.nee;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.setup.CommonSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(NotEnoughEnergistics.MODID)
public class NotEnoughEnergistics {

    public static final String MODID = "neenergistics";
    public static final String MOD_NAME = "NotEnoughEnergistics";
    public static Logger logger = LogManager.getLogger(MOD_NAME);

    public NotEnoughEnergistics() {

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, NEEConfig.CLIENT_SPEC);

        CommonSetup commonSetup = new CommonSetup();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(commonSetup::onCommonSetup);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> NEECommands::register);
    }


}
