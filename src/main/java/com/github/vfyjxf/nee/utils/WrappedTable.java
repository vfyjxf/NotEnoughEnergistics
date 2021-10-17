/*
 * Copied from Just Enough Energistics(https://github.com/p455w0rd/JustEnoughEnergistics/blob/master/src/main/java/p455w0rd/jee/util/WrappedTable.java)
 */
package com.github.vfyjxf.nee.utils;

import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler;
import com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler;
import com.google.common.collect.ImmutableTable;
import mezz.jei.collect.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WrappedTable<R, C, V> extends Table<R, C, V> {

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

        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to do reflection things.", e);
        }
    }

    public WrappedTable(Table<R, C, V> other) {
        super(new HashMap<>(), HashMap::new);//Dummy values to constructor, will be replaced.
        wrapped = other;

        //Set underlying table stuff incase anyone else reflects it.
        try {
            f_table.set(this, f_table.get(other));
            f_rowMappingFunction.set(this, f_rowMappingFunction.get(other));
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to do reflection things.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public V onValueSet(R row, V value) {
        if (row == ContainerPatternTerm.class && value.getClass().getCanonicalName().equals("appeng.integration.modules.jei.RecipeTransferHandler")) {
            value = (V) new PatternRecipeTransferHandler();
            NotEnoughEnergistics.logger.info("AE2 PatternRecipeTransfeHandler Replaced Successfully (Overwrite Denied)");
        }
        if(row == ContainerCraftingTerm.class && value.getClass().getCanonicalName().equals("appeng.integration.modules.jei.RecipeTransferHandler")){
            value = (V) new CraftingHelperTransferHandler();
            NotEnoughEnergistics.logger.info("AE2 RecipeTransfeHandler Replaced Successfully (Overwrite Denied)");
        }
        return value;
    }

    @Override
    public V computeIfAbsent(R row, C col, Supplier<V> valueSupplier) {
        Map<C, V> rowMap = getRow(row);
        return rowMap.computeIfAbsent(col, k -> onValueSet(row, valueSupplier.get()));
    }

    @Override
    public V put(R row, C col, V val) {
        Map<C, V> rowMap = getRow(row);
        return rowMap.put(col, onValueSet(row, val));
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
