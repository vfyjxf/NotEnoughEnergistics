package com.github.vfyjxf.nee.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerPatternTerm;
import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.helper.BlackListHelper;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeTransfer;
import com.github.vfyjxf.nee.utils.Globals;
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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.vfyjxf.nee.helper.PreferenceHelper.getFromPreference;
import static com.github.vfyjxf.nee.utils.Globals.INPUT_KEY_HEAD;

/**
 * @author vfyjxf
 */
public class PatternTransferHandler implements IRecipeTransferHandler<ContainerPatternTerm> {

    private static final Map<String, List<ItemStack>> SWITCHER_DATA = new HashMap<>();
    private static String lastRecipeType = "";

    public PatternTransferHandler() {
    }

    @Override
    @Nonnull
    public Class<ContainerPatternTerm> getContainerClass() {
        return ContainerPatternTerm.class;
    }

    public static Map<String, List<ItemStack>> getSwitcherData() {
        return SWITCHER_DATA;
    }

    public static String getLastRecipeType() {
        return lastRecipeType;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerPatternTerm container, IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        String recipeType = recipeLayout.getRecipeCategory().getUid();
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);
        GuiScreen parent = GuiUtils.getParentScreen();
        if (parent instanceof GuiPatternTerm) {
            GuiPatternTerm patternTerm = (GuiPatternTerm) parent;
            if (doTransfer) {
                Pair<NBTTagCompound, NBTTagCompound> recipePair = packRecipe(getRepo(patternTerm), container, recipeLayout, recipeType);
                NEENetworkHandler.getInstance().sendToServer(new PacketRecipeTransfer(recipePair.getLeft(), recipePair.getRight(), isCraftingRecipe));
                if (NEEConfig.isPrintRecipeType()) {
                    NotEnoughEnergistics.logger.info("RecipeType is : " + recipeType);
                }
            } else {
                //TODO:Wireless Pattern Term support?
                Supplier<ItemRepo> repoSupplier = () -> getRepo(patternTerm);
                return new CraftingInfoError(new RecipeAnalyzer(patternTerm, true, repoSupplier), recipeLayout, false);
            }
        }

        return null;
    }

    private Pair<NBTTagCompound, NBTTagCompound> packRecipe(ItemRepo repo, ContainerPatternTerm container, IRecipeLayout recipeLayout, String recipeType) {
        SWITCHER_DATA.clear();
        boolean isCraftingRecipe = isCraftingRecipe(recipeType);
        boolean shouldMerge = shouldMerge(recipeType);
        NBTTagCompound inputs = new NBTTagCompound();
        NBTTagCompound outputs = new NBTTagCompound();
        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        List<IAEItemStack> storage = ItemUtils.getStorage(repo);
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
                    if (outputIndex >= 3 || stack.isEmpty() || isCraftingRecipe) {
                        continue;
                    }
                    outputs.setTag(Globals.OUTPUT_KEY_HEAD + outputIndex, stack.writeToNBT(new NBTTagCompound()));
                    outputIndex++;
                }
            }
        }
        for (StackWrapper preference : mapToPreference(inputsList, recipeType)) {

            if (BlackListHelper.isBlacklistItem(preference.getStack(), recipeType)) continue;

            ItemStack finalStack = preference.getStack().copy();
            if (NEEConfig.isNetworkOrInventoryFirst() && !storage.isEmpty() && !finalStack.isEmpty()) {
                ItemStack origin = finalStack;
                finalStack = storage.stream()
                        .filter(stored -> ItemUtils.contains(origin, stored.getDefinition()))
                        .min((o1, o2) -> {
                            if (o1.isCraftable() && o2.isCraftable()) {
                                return 0;
                            } else if (o1.isCraftable()) {
                                return -1;
                            } else if (o2.isCraftable()) {
                                return 1;
                            } else {
                                return Long.compare(o2.getStackSize(), o1.getStackSize());
                            }
                        })
                        .map(stored -> stored.getDefinition().copy())
                        .orElse(origin);
            }
            finalStack.setCount(preference.getCount());

            inputs.setTag(INPUT_KEY_HEAD + inputIndex, finalStack.writeToNBT(new NBTTagCompound()));
            SWITCHER_DATA.put(INPUT_KEY_HEAD + inputIndex, preference.getIngredients());
            inputIndex++;

        }
        lastRecipeType = recipeType;
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
                return !NEEConfig.getMergeBlacklist().contains(recipeType);
            case DISABLED:
            default:
                return false;
        }
    }

    private List<StackWrapper> mapToPreference(List<StackWrapper> wrappers, String recipeType) {
        return wrappers.stream()
                .map(wrapper -> new StackWrapper(getFromPreference(wrapper.getIngredients(), wrapper.getStack(), recipeType), wrapper.getIngredients(), wrapper.getCount()))
                .collect(Collectors.toList());
    }

    private ItemRepo getRepo(GuiPatternTerm patternTerm) {
        return ObfuscationReflectionHelper.getPrivateValue(GuiMEMonitorable.class, patternTerm, "repo");
    }

}
