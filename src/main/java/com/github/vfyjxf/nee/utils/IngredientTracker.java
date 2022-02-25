package com.github.vfyjxf.nee.utils;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.me.ItemRepo;
import appeng.container.AEBaseContainer;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingRequest;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import p455w0rd.wct.client.gui.GuiWCT;

import java.util.ArrayList;
import java.util.List;

import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.noPreview;

public class IngredientTracker {

    private final List<Ingredient> ingredients = new ArrayList<>();
    private AEBaseContainer termContainer;
    private final GuiScreen parentScreen;
    private EntityPlayer player;
    private List<ItemStack> requireStacks;
    private int currentIndex = 0;


    /**
     * For PatternRecipeTransfer
     */
    public IngredientTracker(IRecipeLayout recipeLayout, RecipesGui recipesGui) {
        this.parentScreen = recipesGui.getParentScreen();
        for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (guiIngredient.isInput() && !guiIngredient.getAllIngredients().isEmpty()) {
                if (guiIngredient.getAllIngredients().stream().anyMatch(stack -> stack != null && !stack.isEmpty())) {
                    ingredients.add(new Ingredient(guiIngredient));
                }
            }
        }

        for (Ingredient ingredient : this.ingredients) {
            for (IAEItemStack stack : getCraftableStacks()) {
                if (ItemUtils.getIngredientIndex(stack.asItemStackRepresentation(), ingredient.getIngredient().getAllIngredients()) >= 0) {
                    if (ingredient.getCraftableIngredient().isEmpty()) {
                        ingredient.setCraftableIngredient(stack.asItemStackRepresentation());
                    }
                }
            }
        }

    }

    /**
     * For CraftingHelperTransferHandler
     */
    public IngredientTracker(AEBaseContainer termContainer, IRecipeLayout recipeLayout, EntityPlayer player, RecipesGui recipesGui) {
        this.termContainer = termContainer;
        this.player = player;
        this.parentScreen = recipesGui.getParentScreen();

        for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (guiIngredient.isInput() && !guiIngredient.getAllIngredients().isEmpty()) {
                if (guiIngredient.getAllIngredients().stream().anyMatch(stack -> stack != null && !stack.isEmpty())) {
                    ingredients.add(new Ingredient(guiIngredient));
                }
            }
        }

        for (Ingredient ingredient : this.ingredients) {
            for (IAEItemStack stack : getCraftableStacks()) {
                if (ItemUtils.getIngredientIndex(stack.asItemStackRepresentation(), ingredient.getIngredient().getAllIngredients()) >= 0) {
                    if (ingredient.getCraftableIngredient().isEmpty() && stack.isCraftable()) {
                        ingredient.setCraftableIngredient(stack.asItemStackRepresentation());
                    }
                }
            }
        }

        this.calculateIngredients();

    }

    @SuppressWarnings("unchecked")
    private List<IAEItemStack> getStorageStacks() {
        List<IAEItemStack> list = new ArrayList<>();
        if (this.parentScreen != null) {
            ItemRepo repo = null;
            if (parentScreen instanceof GuiCraftingTerm || parentScreen instanceof GuiPatternTerm) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiMEMonitorable.class, (GuiMEMonitorable) parentScreen, "repo");
            } else if (GuiUtils.isGuiWirelessCrafting(parentScreen)) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiWCT.class, (GuiWCT) parentScreen, "repo");
            }
            if (repo != null) {
                for (IAEItemStack stack : (IItemList<IAEItemStack>) ObfuscationReflectionHelper.getPrivateValue(ItemRepo.class, repo, "list")) {
                    list.add(stack.copy());
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<IAEItemStack> getCraftableStacks() {
        List<IAEItemStack> craftableStacks = new ArrayList<>();
        if (parentScreen != null) {
            ItemRepo repo = null;
            if (parentScreen instanceof GuiCraftingTerm || parentScreen instanceof GuiPatternTerm) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiMEMonitorable.class, (GuiMEMonitorable) parentScreen, "repo");
            } else if (GuiUtils.isGuiWirelessCrafting(parentScreen)) {
                repo = ObfuscationReflectionHelper.getPrivateValue(GuiWCT.class, (GuiWCT) parentScreen, "repo");
            }
            if (repo != null) {
                for (IAEItemStack stack : (IItemList<IAEItemStack>) ObfuscationReflectionHelper.getPrivateValue(ItemRepo.class, repo, "list")) {
                    craftableStacks.add(stack.copy());
                }
            }
        }
        return craftableStacks;
    }

    public List<ItemStack> getRequireToCraftStacks() {
        List<ItemStack> requireToCraftStacks = new ArrayList<>();
        for (Ingredient ingredient : this.getIngredients()) {
            boolean find = false;
            if (ingredient.isCraftable() && ingredient.requiresToCraft()) {
                for (ItemStack stack : requireToCraftStacks) {
                    boolean areStackEqual = stack.isItemEqual(ingredient.getCraftableIngredient()) && ItemStack.areItemStackTagsEqual(stack, ingredient.getCraftableIngredient());
                    if (areStackEqual) {
                        stack.setCount((int) (stack.getCount() + ingredient.getMissingCount()));
                        find = true;
                        break;
                    }

                }

                if (!find) {
                    ItemStack requireStack = ingredient.getCraftableIngredient().copy();
                    requireStack.setCount((int) ingredient.getMissingCount());
                    requireToCraftStacks.add(requireStack);
                }
            }
        }
        return requireToCraftStacks;
    }

    public List<ItemStack> getRequireStacks() {
        return requireStacks;
    }

    public ItemStack getRequiredStack(int index) {
        return this.getRequireStacks().get(index);
    }

    public boolean hasCraftableIngredient() {
        for (Ingredient ingredient : this.getIngredients()) {
            if (ingredient.isCraftable()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNext() {
        return currentIndex < getRequireStacks().size();
    }

    public void requestNextIngredient() {
        IAEItemStack stack = AEItemStack.fromItemStack(this.getRequiredStack(currentIndex));
        if (stack != null) {
            NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(stack, noPreview));
        }
        currentIndex++;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void addAvailableStack(ItemStack stack) {
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.requiresToCraft()) {
                if (NEEConfig.matchOtherItems) {
                    boolean canUse = stack.getCount() > 0 && ItemUtils.getIngredientIndex(stack, ingredient.getIngredient().getAllIngredients()) >= 0;
                    if (canUse) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCount(stack.getCount());
                        if (ingredient.requiresToCraft()) {
                            stack.setCount(0);
                        } else {
                            stack.setCount(stack.getCount() - missingCount);
                        }
                        break;
                    }
                } else {
                    ItemStack craftableStack = ingredient.getCraftableIngredient();
                    if (!craftableStack.isEmpty() && craftableStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(craftableStack, stack)) {
                        int missingCount = (int) ingredient.getMissingCount();
                        ingredient.addCount(stack.getCount());
                        if (ingredient.requiresToCraft()) {
                            stack.setCount(0);
                        } else {
                            stack.setCount(stack.getCount() - missingCount);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void calculateIngredients() {

        List<IAEItemStack> stacks = NEEConfig.matchOtherItems ? getStorageStacks() : getCraftableStacks();
        for (Ingredient ingredient : this.ingredients) {
            for (IAEItemStack stack : stacks) {
                if (ItemUtils.getIngredientIndex(stack.asItemStackRepresentation(), ingredient.getIngredient().getAllIngredients()) >= 0) {
                    if (stack.getStackSize() > 0) {
                        ingredient.addCount(stack.getStackSize());
                        if (ingredient.requiresToCraft()) {
                            stack.setStackSize(0);
                        } else {
                            stack.setStackSize(stack.getStackSize() - ingredient.getRequireCount());
                        }
                    }
                }
            }
        }

        List<ItemStack> inventoryStacks = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < player.inventory.getSizeInventory(); slotIndex++) {
            if (!player.inventory.getStackInSlot(slotIndex).isEmpty()) {
                inventoryStacks.add(player.inventory.getStackInSlot(slotIndex).copy());
            }
        }

        if (this.termContainer instanceof IContainerCraftingPacket) {
            //check stacks in crafting grid
            IContainerCraftingPacket ccp = (IContainerCraftingPacket) this.termContainer;
            final IItemHandler craftMatrix = ccp.getInventoryByName("crafting");
            for (int slotIndex = 0; slotIndex < craftMatrix.getSlots(); slotIndex++) {
                if (!craftMatrix.getStackInSlot(slotIndex).isEmpty()) {
                    inventoryStacks.add(craftMatrix.getStackInSlot(slotIndex).copy());
                }
            }
        }

        for (int i = 0; i < getIngredients().size(); i++) {
            for (ItemStack stack : inventoryStacks) {
                addAvailableStack(stack);
            }
        }

        this.requireStacks = this.getRequireToCraftStacks();
    }

}
