package com.github.vfyjxf.nee.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    public static final KeyBinding recipeIngredientChange = new KeyBinding("key.neenergistics.recipe.ingredient.change", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_LEFT_SHIFT), "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding stackCountChange = new KeyBinding("key.neenergistics.stack.count.change", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_LEFT_CONTROL), "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding craftingHelperPreview = new KeyBinding("key.neenergistics.crafting.helper.preview", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_LEFT_CONTROL), "neenergistics.NotEnoughEnergistics");
    public static final KeyBinding craftingHelperNoPreview = new KeyBinding("key.neenergistics.crafting.helper.noPreview", KeyConflictContext.GUI, getKey(GLFW.GLFW_KEY_LEFT_ALT), "neenergistics.NotEnoughEnergistics");


    private static InputMappings.Input getKey(int key) {
        return InputMappings.Type.KEYSYM.getOrCreate(key);
    }

    public static boolean isKeyDown(KeyBinding keyBinding) {
        return InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyBinding.getKey().getValue());
    }
}
