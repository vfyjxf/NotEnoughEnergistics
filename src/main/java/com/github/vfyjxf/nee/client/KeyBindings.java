package com.github.vfyjxf.nee.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.input.Keyboard;

public final class KeyBindings {

    public static final KeyBinding recipeIngredientChange = new KeyBinding("key.neenergistics.recipe.ingredient.change", KeyConflictContext.GUI, Keyboard.KEY_LSHIFT, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding stackCountChange = new KeyBinding("key.neenergistics.stack.count.change", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding craftingHelperPreview = new KeyBinding("key.neenergistics.crafting.helper.preview", KeyConflictContext.GUI, Keyboard.KEY_LCONTROL, "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding craftingHelperNoPreview = new KeyBinding("key.neenergistics.crafting.helper.noPreview", KeyConflictContext.GUI, Keyboard.KEY_LMENU, "neenergistics.NotEnoughEnergistics");
}
