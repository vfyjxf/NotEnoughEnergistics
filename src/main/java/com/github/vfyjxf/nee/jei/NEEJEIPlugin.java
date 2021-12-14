package com.github.vfyjxf.nee.jei;

import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.PatternTermContainer;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.utils.WrappedTable;
import com.google.common.collect.Table.Cell;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.collect.Table;
import mezz.jei.config.Constants;
import mezz.jei.load.registration.RecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;


@JeiPlugin
public class NEEJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(NotEnoughEnergistics.MODID, "appeng");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        RecipeTransferRegistration recipeTransferRegistration = (RecipeTransferRegistration) registration;
        Table<Class<?>, ResourceLocation, IRecipeTransferHandler<?>> newRegistry = Table.hashBasedTable();
        boolean patternHandlerFound = false;
        boolean craftingHandlerFound = false;
        for (final Cell<Class<?>, ResourceLocation, IRecipeTransferHandler<?>> currentCell : recipeTransferRegistration.getRecipeTransferHandlers().cellSet()) {
            if (PatternTermContainer.class.equals(currentCell.getRowKey())) {
                patternHandlerFound = true;
                continue;
            }

            if (CraftingTermContainer.class.equals(currentCell.getRowKey())) {
                craftingHandlerFound = true;
                continue;
            }
            newRegistry.put(currentCell.getRowKey(), currentCell.getColumnKey(), currentCell.getValue());
        }

        newRegistry.put(PatternTermContainer.class, Constants.UNIVERSAL_RECIPE_TRANSFER_UID, new PatternRecipeTransferHandler());
        newRegistry.put(CraftingTermContainer.class, VanillaRecipeCategoryUid.CRAFTING, new CraftingHelperTransferHandler(registration.getTransferHelper()));
        if (patternHandlerFound && craftingHandlerFound) {
            NotEnoughEnergistics.logger.info("AE2 RecipeTransferHandler Replaced Successfully (Registered prior)");
        } else {
            newRegistry = new WrappedTable<>(newRegistry, registration.getTransferHelper());
        }

        ObfuscationReflectionHelper.setPrivateValue(RecipeTransferRegistration.class, recipeTransferRegistration, newRegistry, "recipeTransferHandlers");
    }

}
