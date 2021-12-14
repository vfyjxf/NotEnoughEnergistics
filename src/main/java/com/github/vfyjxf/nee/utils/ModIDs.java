package com.github.vfyjxf.nee.utils;

import net.minecraftforge.fml.ModList;

/**
 * @author vfyjxf
 */
public enum ModIDs {
    AppEng("appliedenergistics2", "Applied Energistics 2"),
    JEI("jei", "Just Enough Items");
    private final String modid;
    private final String modName;


    ModIDs(String modid, String modName) {
        this.modid = modid;
        this.modName = modName;
    }

    public String getModid() {
        return this.modid;
    }

    public String getModName() {
        return this.modName;
    }

    public boolean isModLoaded() {
        return ModList.get().isLoaded(this.modid);
    }

}
