package com.github.vfyjxf.nee.integration;

import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class RecipeToolManager {

    public static final RecipeToolManager INSTANCE = new RecipeToolManager();

    private final List<IToolHelper> toolHelpers = new ArrayList<>();

    private RecipeToolManager() {

    }

    public IToolHelper getToolHelper(Item tool) {
        return toolHelpers.stream()
                .filter(helper -> helper.isSupport(tool.getClass()))
                .findAny()
                .orElse(null);
    }

    public void registerToolHelper(IToolHelper toolHelper) {
        if (!toolHelpers.contains(toolHelper)) {
            toolHelpers.add(toolHelper);
        }
    }

    public List<IToolHelper> getToolHelpers() {
        return toolHelpers;
    }

}
