/*
 * Copied from Just Enough Energistics(https://github.com/p455w0rd/JustEnoughEnergistics/blob/master/src/main/java/p455w0rd/jee/integration/JEI.java)
 */
package com.github.vfyjxf.nee.jei;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table.Cell;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.collect.Table;
import mezz.jei.config.Constants;
import mezz.jei.recipes.RecipeTransferRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author vfyjxf
 */
@JEIPlugin
public class NEEJeiPlugin implements IModPlugin {

    private static final Class wctContainer;

    static {
        Class<?> clazz;
        try {
            clazz = Class.forName("p455w0rd.wct.container.ContainerWCT");
        } catch (ClassNotFoundException e) {
            clazz = null;
        }
        wctContainer = clazz;
    }

    @Override
    public void register(IModRegistry registry) {
        replaceTransferHandler((RecipeTransferRegistry) registry.getRecipeTransferRegistry());
        registry.addGhostIngredientHandler(AEBaseGui.class, new NEEGhostIngredientHandler());
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private void replaceTransferHandler(RecipeTransferRegistry registry) {
        Table<Class, String, IRecipeTransferHandler> handlers = Table.hashBasedTable();
        int replaceCount = 0;
        List<Class<?>> replaceKeys = new ArrayList<>(Arrays.asList(ContainerCraftingTerm.class, ContainerPatternTerm.class));
        if (wctContainer != null) replaceKeys.add(wctContainer);
        //copy a transfer table
        for (final Cell<Class, String, IRecipeTransferHandler> cell : registry.getRecipeTransferHandlers().cellSet()) {
            if (replaceKeys.contains(cell.getRowKey())) {
                replaceCount++;
                continue;
            }
            handlers.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
        if (replaceCount > 0) {
            handlers.put(ContainerPatternTerm.class, Constants.UNIVERSAL_RECIPE_TRANSFER_UID, new PatternTransferHandler());
            handlers.put(ContainerCraftingTerm.class, VanillaRecipeCategoryUid.CRAFTING, new CraftingTransferHandler<>(ContainerCraftingTerm.class));
            NotEnoughEnergistics.logger.info("Replace AE2 integration successfully!(Registered prior)");
            if (wctContainer != null) {
                handlers.put(wctContainer, VanillaRecipeCategoryUid.CRAFTING, new CraftingTransferHandler<>(wctContainer));
                NotEnoughEnergistics.logger.info("Replace Wireless Crafting Terminal integration successfully!(Registered prior");
            }
        }
        if (replaceCount < replaceKeys.size()) {
            handlers = new WrappedTable<>(handlers);
        }
        ObfuscationReflectionHelper.setPrivateValue(RecipeTransferRegistry.class, registry, handlers, "recipeTransferHandlers");

    }

    /**
     * From <a href="https://github.com/p455w0rd/JustEnoughEnergistics/blob/master/src/main/java/p455w0rd/jee/util/WrappedTable.java">...</a>
     */
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private static class WrappedTable<R, C, V> extends Table<R, C, V> {

        private static final Field f_modifiers;
        private static final Field f_table;
        private static final Field f_rowMappingFunction;
        private final Table<R, C, V> wrapped;

        static {
            try {
                f_modifiers = Field.class.getDeclaredField("modifiers");
                f_modifiers.setAccessible(true);

                f_table = Table.class.getDeclaredField("table");
                f_table.setAccessible(true);
                makeWriteable(f_table);

                f_rowMappingFunction = Table.class.getDeclaredField("rowMappingFunction");
                f_rowMappingFunction.setAccessible(true);
                makeWriteable(f_rowMappingFunction);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Unable to do reflection things.", e);
            }
        }


        public WrappedTable(Table<R, C, V> wrapped) {
            super(new HashMap<>(), HashMap::new);
            this.wrapped = wrapped;
            try {
                f_table.set(this, f_table.get(wrapped));
                f_rowMappingFunction.set(this, f_rowMappingFunction.get(wrapped));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to do reflection things.", e);
            }
        }

        @SuppressWarnings({"unchecked","rawtypes"})
        public Pair<C, V> onValueSet(R row, C col, V value) {
            String canonicalName = value.getClass().getCanonicalName();
            if (row == ContainerPatternTerm.class && "appeng.integration.modules.jei.RecipeTransferHandler".equals(canonicalName)) {
                col = (C) Constants.UNIVERSAL_RECIPE_TRANSFER_UID;
                value = (V) new PatternTransferHandler();
                NotEnoughEnergistics.logger.info("Pattern terminal transfer handler replaced successfully (Overwrite Denied)");
            }
            if (row == ContainerCraftingTerm.class && "appeng.integration.modules.jei.RecipeTransferHandler".equals(canonicalName)) {
                col = (C) VanillaRecipeCategoryUid.CRAFTING;
                value = (V) new CraftingTransferHandler<>(ContainerCraftingTerm.class);
                NotEnoughEnergistics.logger.info("Crafting terminal transfer handler replaced successfully (Overwrite Denied)");
            }
            if (wctContainer != null) {
                if (row == wctContainer && "p455w0rd.wct.integration.JEI.RecipeTransferHandler".equals(canonicalName)) {
                    col = (C) VanillaRecipeCategoryUid.CRAFTING;
                    value = (V) new CraftingTransferHandler<>(wctContainer);
                    NotEnoughEnergistics.logger.info("Wireless crafting terminal transfer handler replaced successfully (Overwrite Denied)");
                }
            }
            return Pair.of(col, value);
        }

        @Override
        public V computeIfAbsent(R row, C col, Supplier<V> valueSupplier) {
            Map<C, V> rowMap = getRow(row);
            Pair<C, V> pair = onValueSet(row, col, valueSupplier.get());
            return rowMap.computeIfAbsent(pair.getLeft(), k -> pair.getRight());
        }

        @Override
        public V put(R row, C col, V val) {
            Map<C, V> rowMap = getRow(row);
            Pair<C, V> pair = onValueSet(row, col, val);
            return rowMap.put(pair.getLeft(), pair.getRight());
        }

        @Override
        public V get(R row, C col) {
            return wrapped.get(row, col);
        }

        @Override
        public Map<C, V> getRow(R row) {
            return wrapped.getRow(row);
        }

        @Override
        public ImmutableTable<R, C, V> toImmutable() {
            return wrapped.toImmutable();
        }


        private static void makeWriteable(Field field) throws IllegalAccessException {
            if ((field.getModifiers() & Modifier.FINAL) != 0) {
                f_modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
            }
        }

    }


}
