package com.github.vfyjxf.nee.config;

import net.minecraft.client.resources.I18n;

import java.util.Locale;

public enum ItemCombination {
    DISABLED, ENABLED, WHITELIST;

    public String getLocalName() {
        return I18n.format("gui.neenergistics.button.name" + "." + this.name().toLowerCase(Locale.ROOT));
    }
}
