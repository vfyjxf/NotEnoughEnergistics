package com.github.vfyjxf.nee.jei;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.helper.BlackListHelper;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeTransfer;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.github.vfyjxf.nee.utils.StackWrapper;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.vfyjxf.nee.helper.PreferenceHelper.getFromPreference;

/**
 * @author vfyjxf
 */
public class PatternTransferHandler implements IRecipeTransferHandler<ContainerPatternTerm> {

    public static final String OUTPUT_KEY = "O";
    public static final String INPUT_KEY = "#";
    public static Map<String, List<ItemStack>> ingredients = new HashMap<>();

    public PatternTransferHandler() {
    }

    @Override
    @Nonnull
    public Class<ContainerPatternTerm> getContainerClass() {
        return ContainerPatternTerm.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerPatternTerm container, IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        String recipeType = recipeLayout.getRecipeCategory().getUid();
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);
        if (doTransfer) {
            Pair<NBTTagCompound, NBTTagCompound> recipePair = packRecipe(container, recipeLayout, recipeType);
            NEENetworkHandler.getInstance().sendToServer(new PacketRecipeTransfer(recipePair.getLeft(), recipePair.getRight(), isCraftingRecipe));
            if (NEEConfig.isPrintRecipeType()) {
                NotEnoughEnergistics.logger.info(recipeType);
            }
        } else {
            //TODO:Wireless Pattern Term support?
            //TODO:Prefer item in network
            GuiScreen parent = GuiUtils.getParentScreen();
            if (parent instanceof GuiPatternTerm) {
                GuiPatternTerm patternTerm = (GuiPatternTerm) parent;
                RecipeAnalyzer analyzer = new RecipeAnalyzer(patternTerm);
                return new CraftingInfoError(analyzer.analyzeRecipe(recipeLayout), false);
            }
        }

        return null;
    }

    private Pair<NBTTagCompound, NBTTagCompound> packRecipe(ContainerPatternTerm container, IRecipeLayout recipeLayout, String recipeType) {
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);
        boolean shouldMerge = shouldMerge(recipeType);
        NBTTagCompound inputs = new NBTTagCompound();
        NBTTagCompound outputs = new NBTTagCompound();
        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        List<StackWrapper> inputsList = new ArrayList<>();
        int inputIndex = 0;
        int outputIndex = 0;
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
            final IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if (ingredient != null) {
                ItemStack stack = NEEConfig.isUseDisplayed() ? ingredient.getDisplayedIngredient() : ItemUtils.getFirstStack(ingredient);
                if (stack == null) {
                    stack = ItemStack.EMPTY;
                }

                if (ingredient.isInput()) {
                    StackWrapper current = new StackWrapper(stack.copy(), ingredient.getAllIngredients());
                    if (!isCraftingRecipe) {

                        //TODO:For some of the more unusual mod recipes, we may need these 'empties'(Such as gtceu?).
                        if (stack.isEmpty()) continue;

                        boolean merged = shouldMerge && inputsList.stream().anyMatch(wrapper -> wrapper.merge(current));

                        if (!merged) inputsList.add(current);

                    } else {
                        inputsList.add(current);
                    }
                } else {
                    if (outputIndex >= 3 || stack.isEmpty() || (container.isCraftingMode())) {
                        continue;
                    }
                    outputs.setTag(OUTPUT_KEY + outputIndex, stack.writeToNBT(new NBTTagCompound()));
                    outputIndex++;
                }
            }
        }
        for (StackWrapper preference : mapToPreference(inputsList)) {

            if (BlackListHelper.isBlacklistItem(preference.getStack())) continue;

            ItemStack finalStack = preference.getStack().copy();
            finalStack.setCount(preference.getCount());

            inputs.setTag(INPUT_KEY + inputIndex, finalStack.writeToNBT(new NBTTagCompound()));
            PatternTransferHandler.ingredients.put(INPUT_KEY + inputIndex, preference.getIngredients());
            inputIndex++;
            //TODO:Ingredient Switcher data

        }

        return Pair.of(inputs, outputs);
    }

    private boolean isCraftingRecipe(String recipeType) {
        if (!recipeType.equals(VanillaRecipeCategoryUid.INFORMATION) && !recipeType.equals(VanillaRecipeCategoryUid.FUEL)) {
            return recipeType.equals(VanillaRecipeCategoryUid.CRAFTING);
        }
        return false;
    }

    private boolean shouldMerge(String recipeType) {
        switch (NEEConfig.getMergeMode()) {
            case ENABLED:
                return NEEConfig.getMergeBlacklist().contains(recipeType);
            case DISABLED:
            default:
                return false;
        }
    }

    private List<StackWrapper> mapToPreference(List<StackWrapper> wrappers) {
        return wrappers.stream()
                .map(wrapper -> new StackWrapper(getFromPreference(wrapper.getIngredients(), wrapper.getStack()), wrapper.getIngredients()))
                .filter(wrapper -> NEEConfig.getBlacklist().stream().noneMatch(stack -> ItemUtils.matches(wrapper.getStack(), stack)))
                .collect(Collectors.toList());
    }

}
