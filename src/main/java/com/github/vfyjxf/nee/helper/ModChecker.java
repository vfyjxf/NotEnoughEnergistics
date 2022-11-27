package com.github.vfyjxf.nee.helper;

/**
 * Used to check the information of other mods to determine what mod they are.
 */
public class ModChecker {

    public static boolean isUnofficialAppeng = false;

    static {
        checkUnofficial();
    }

    public static void checkUnofficial() {
        try {
            Class.forName("appeng.container.slot.IJEITargetSlot");
            isUnofficialAppeng = true;
        } catch (ClassNotFoundException e) {
            isUnofficialAppeng = false;
        }
    }

}
