/*
 * Copied from Just Enough Energistics(https://github.com/p455w0rd/JustEnoughEnergistics/blob/master/src/main/java/p455w0rd/jee/integration/JEI.java)
 */
package com.github.vfyjxf.nee.jei;

import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.utils.WrappedTable;
import com.google.common.collect.Table.Cell;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.collect.Table;
import mezz.jei.config.Constants;
import mezz.jei.recipes.RecipeTransferRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * @author vfyjxf
 */
@JEIPlugin
public class NEEJEIPlugin implements IModPlugin {

    public static IModRegistry registry;

    @Override
    public void register(IModRegistry registry) {
        NEEJEIPlugin.registry = registry;
        Table<Class<?>, String, IRecipeTransferHandler> newRegistry = Table.hashBasedTable();
        boolean patternHandlerFound = false;
        boolean craftingHandlerFound = false;
        for (final Cell<Class, String, IRecipeTransferHandler> currentCell : ((RecipeTransferRegistry) registry.getRecipeTransferRegistry()).getRecipeTransferHandlers().cellSet()) {
            if (currentCell.getRowKey().equals(ContainerPatternTerm.class)) {
                patternHandlerFound = true;
                continue;
            }
            if (currentCell.getRowKey().equals(ContainerCraftingTerm.class)) {
                craftingHandlerFound = true;
                continue;
            }
            newRegistry.put(currentCell.getRowKey(), currentCell.getColumnKey(), currentCell.getValue());
        }
        newRegistry.put(ContainerPatternTerm.class, Constants.UNIVERSAL_RECIPE_TRANSFER_UID, new PatternRecipeTransferHandler());
        newRegistry.put(ContainerCraftingTerm.class, VanillaRecipeCategoryUid.CRAFTING, new CraftingHelperTransferHandler());
        if (patternHandlerFound && craftingHandlerFound) {
            NotEnoughEnergistics.logger.info("AE2 RecipeTransferHandler Replaced Successfully (Registered prior)");
        } else {
            newRegistry = new WrappedTable<>(newRegistry);
        }
        ObfuscationReflectionHelper.setPrivateValue(RecipeTransferRegistry.class, (RecipeTransferRegistry) registry.getRecipeTransferRegistry(), newRegistry, "recipeTransferHandlers");
    }
}
