package com.github.vfyjxf.nee.jei;

import appeng.container.me.items.CraftingTermContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import com.github.vfyjxf.nee.client.KeyBindings;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packets.PacketCraftingRequest;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.vfyjxf.nee.client.KeyBindings.craftingHelperNoPreview;
import static com.github.vfyjxf.nee.client.KeyBindings.craftingHelperPreview;

public class CraftingHelperTransferHandler implements IRecipeTransferHandler<CraftingTermContainer> {

    private final IRecipeTransferHandlerHelper helper;
    private boolean canSendReference;

    public static IngredientTracker tracker = null;
    public static int stackIndex = 1;
    public static boolean autoStart = false;

    public CraftingHelperTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }


    @Override
    public Class<CraftingTermContainer> getContainerClass() {
        return CraftingTermContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(CraftingTermContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        tracker = crateTracker(container, recipeLayout, player);
        IRecipeTransferError error = checkError(recipe, player);
        if (error == null) {
            if (doTransfer) {
                tracker = crateTracker(container, recipeLayout, player);
                boolean doCraftingHelp = KeyBindings.isKeyDown(craftingHelperPreview) || KeyBindings.isKeyDown(craftingHelperNoPreview);
                autoStart = KeyBindings.isKeyDown(craftingHelperNoPreview);
                if (doCraftingHelp && !tracker.getRequireToCraftStacks().isEmpty()) {
                    for (ItemStack requireToCraftStack : tracker.getRequireToCraftStacks()) {
                        if (!requireToCraftStack.isEmpty()) {
                            NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(requireToCraftStack, autoStart));
                            stackIndex = 1;
                            break;
                        }
                    }

                } else {
                    moveItems(recipe, recipeLayout);
                }
            } else {
                return new CraftingHelperTooltipError(tracker, true);
            }
        }
        return error;
    }

    private IngredientTracker crateTracker(CraftingTermContainer container, IRecipeLayout recipeLayout, PlayerEntity player) {
        IngredientTracker tracker = new IngredientTracker(recipeLayout);

        List<ItemStack> inventoryStacks = new ArrayList<>();

        //check stacks in player's inventory
        for (int slotIndex = 0; slotIndex < player.inventory.getContainerSize(); slotIndex++) {
            if (!player.inventory.getItem(slotIndex).isEmpty()) {
                inventoryStacks.add(player.inventory.getItem(slotIndex).copy());
            }
        }

        //check stacks in crafting grid
        final IItemHandler craftMatrix = container.getInventoryByName("crafting");
        for (int slotIndex = 0; slotIndex < craftMatrix.getSlots(); slotIndex++) {
            if (!craftMatrix.getStackInSlot(slotIndex).isEmpty()) {
                inventoryStacks.add(craftMatrix.getStackInSlot(slotIndex).copy());
            }
        }

        for (int i = 0; i < tracker.getIngredients().size(); i++) {
            for (ItemStack stack : inventoryStacks) {
                tracker.addAvailableStack(stack);
            }
        }

        return tracker;
    }

    /**
     * base on RecipeTransferHandler.transferRecipe
     */
    private void moveItems(Object recipe, IRecipeLayout recipeLayout) {
        if (recipe instanceof IRecipe) {
            ResourceLocation recipeId = ((IRecipe<?>) recipe).getId();
            if (this.canSendReference) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId, true));
            } else {

                NonNullList<Ingredient> flatIngredients = NonNullList.withSize(9, Ingredient.EMPTY);
                ItemStack output = ItemStack.EMPTY;

                int firstInputSlot = recipeLayout.getItemStacks().getGuiIngredients().entrySet().stream()
                        .filter(e -> e.getValue().isInput()).mapToInt(Map.Entry::getKey).min().orElse(0);

                for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks()
                        .getGuiIngredients().entrySet()) {
                    IGuiIngredient<ItemStack> item = entry.getValue();
                    if (item.getDisplayedIngredient() == null) {
                        continue;
                    }

                    int inputIndex = entry.getKey() - firstInputSlot;
                    if (item.isInput() && inputIndex < flatIngredients.size()) {
                        ItemStack displayedIngredient = item.getDisplayedIngredient();
                        if (displayedIngredient != null) {
                            flatIngredients.set(inputIndex, Ingredient.of(displayedIngredient));
                        }
                    } else if (!item.isInput() && output.isEmpty()) {
                        output = item.getDisplayedIngredient();
                    }
                }

                ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId, "", 3, 3, flatIngredients, output);
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(fallbackRecipe, true));
            }
        }
    }

    /**
     * Get errors related to the recipe, such as missing id error,but don't get the error that NEE has provided
     * base on RecipeTransferHandler.transferRecipe
     *
     * @return Those errors related to the recipe
     */
    private IRecipeTransferError checkError(Object recipe, PlayerEntity player) {
        if (!(recipe instanceof IRecipe)) {
            return this.helper.createInternalError();
        }
        final IRecipe<?> iRecipe = (IRecipe<?>) recipe;
        final ResourceLocation recipeId = iRecipe.getId();

        if (recipeId == null) {
            return this.helper
                    .createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.missing_id"));
        }

        this.canSendReference = true;

        if (!player.getCommandSenderWorld().getRecipeManager().byKey(recipeId).isPresent()) {
            if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
                return this.helper
                        .createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.missing_id"));
            }
            this.canSendReference = false;
        }

        if (!iRecipe.canCraftInDimensions(3, 3)) {
            return this.helper.createUserErrorWithTooltip(
                    new TranslationTextComponent("jei.appliedenergistics2.recipe_too_large"));
        }
        return null;
    }

}
