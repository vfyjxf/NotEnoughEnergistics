/*
 * Copied from Just Enough Energistics(https://github.com/p455w0rd/JustEnoughEnergistics/blob/master/src/main/java/p455w0rd/jee/integration/JEI.java)
 */
package com.github.vfyjxf.nee.jei;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.google.common.collect.Table.Cell;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.collect.Table;
import mezz.jei.recipes.RecipeTransferRegistry;
import mezz.jei.util.ErrorUtil;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import p455w0rd.wct.container.ContainerWCT;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author vfyjxf
 */
@JEIPlugin
public class NEEJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        Table<Class<?>, String, IRecipeTransferHandler<?>> recipeTransferHandlers = new MyTable<>(new HashMap<>(), HashMap::new);

        for (final Cell<Class, String, IRecipeTransferHandler> currentCell : ((RecipeTransferRegistry) registry.getRecipeTransferRegistry()).getRecipeTransferHandlers().cellSet()) {

            ErrorUtil.checkNotNull(currentCell.getValue(), "recipeTransferHandler");
            ErrorUtil.checkNotNull(currentCell.getColumnKey(), "recipeCategoryUid");
            ErrorUtil.checkNotNull(currentCell.getRowKey(), "containerClass");

            recipeTransferHandlers.put(currentCell.getRowKey(), currentCell.getColumnKey(), currentCell.getValue());

        }

        ObfuscationReflectionHelper.setPrivateValue(RecipeTransferRegistry.class, (RecipeTransferRegistry) registry.getRecipeTransferRegistry(), recipeTransferHandlers, "recipeTransferHandlers");

        registry.addGhostIngredientHandler(AEBaseGui.class, new NEEGhostIngredientHandler());

    }

    private class MyTable<R, C, V> extends Table<R, C, V> {


        private final Class<?> wirelessContainerClass = getContainerClass();

        public MyTable(Map<R, Map<C, V>> table, Supplier<Map<C, V>> rowSupplier) {
            super(table, rowSupplier);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public V put(R row, C col, V val) {

            if (row == ContainerPatternTerm.class && !(val instanceof PatternRecipeTransferHandler)) {
                val = (V) new PatternRecipeTransferHandler();
                NotEnoughEnergistics.logger.info("AE2 PatternRecipeTransfeHandler Replaced Successfully (Overwrite Denied)");
            }
            if (row == ContainerCraftingTerm.class && !(val instanceof CraftingHelperTransferHandler)) {
                val = (V) new CraftingHelperTransferHandler<>(ContainerCraftingTerm.class);
                NotEnoughEnergistics.logger.info("AE2 RecipeTransfeHandler Replaced Successfully (Overwrite Denied)");
            }

            if (row == wirelessContainerClass && !(val instanceof CraftingHelperTransferHandler)) {
                val = (V) new CraftingHelperTransferHandler<>(ContainerWCT.class);
                NotEnoughEnergistics.logger.info("Wireless Crafting Terminal RecipeTransfeHandler Replaced Successfully");
            }

            return super.put(row, col, val);
        }
    }

    private Class<?> getContainerClass() {
        try {
            return Class.forName("p455w0rd.wct.container.ContainerWCT");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
