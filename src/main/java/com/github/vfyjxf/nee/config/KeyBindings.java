package com.github.vfyjxf.nee.config;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.input.Keyboard;

public final class KeyBindings {

    public static final KeyBinding SWITCH_INGREDIENT = new KeyBinding("key.neenergistics.ingredient.switch", KeyConflictContext.GUI, Keyboard.KEY_LSHIFT, "neenergistics.category");
    public static final KeyBinding MODIFY_COUNT = new KeyBinding("key.neenergistics.stack.count.change", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.category");
    public static final KeyBinding AUTO_CRAFT_WITH_PREVIEW = new KeyBinding("key.neenergistics.crafting.helper.preview", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.category");
    public static final KeyBinding AUTO_CRAFT_NON_PREVIEW = new KeyBinding("key.neenergistics.crafting.helper.noPreview", KeyConflictContext.GUI, Keyboard.KEY_LMENU, "neenergistics.category");

    public static boolean isSwitchIngredientKeyDown() {
        return Keyboard.isKeyDown(SWITCH_INGREDIENT.getKeyCode());
    }

    public static boolean isModifyCountKeyDown() {
        return Keyboard.isKeyDown(MODIFY_COUNT.getKeyCode());
    }

    public static boolean isPreviewKeyDown() {
        return Keyboard.isKeyDown(AUTO_CRAFT_WITH_PREVIEW.getKeyCode());
    }

    public static boolean isNonPreviewKeyDown() {
        return Keyboard.isKeyDown(AUTO_CRAFT_NON_PREVIEW.getKeyCode());
    }

}
