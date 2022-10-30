package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.utils.ItemUtils;
import net.minecraft.item.ItemStack;

public class BlackListHelper {

    private BlackListHelper(){

    }

    public static boolean isBlacklistItem(ItemStack stack){
        return NEEConfig.getBlacklist()
                .stream()
                .anyMatch(is -> ItemUtils.matches(is, stack));
    }

}
