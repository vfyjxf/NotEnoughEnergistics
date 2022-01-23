package com.github.vfyjxf.nee.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketJEIRecipe;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingRequest;
import com.github.vfyjxf.nee.network.packet.PacketOpenCraftAmount;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.IngredientTracker;
import com.github.vfyjxf.nee.utils.ModIds;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import org.lwjgl.input.Keyboard;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModNetworking;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.vfyjxf.nee.client.KeyBindings.craftingHelperNoPreview;
import static com.github.vfyjxf.nee.client.KeyBindings.craftingHelperPreview;

public class CraftingHelperTransferHandler<C extends AEBaseContainer> implements IRecipeTransferHandler<C> {

    public static IngredientTracker tracker = null;
    public static int stackIndex = 1;
    public static boolean noPreview = false;
    private final Class<C> containerClass;

    public CraftingHelperTransferHandler(Class<C> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    @Nonnull
    public Class<C> getContainerClass() {
        return this.containerClass;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull C container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (container instanceof IContainerCraftingPacket) {
            if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui) {
                RecipesGui recipesGui = (RecipesGui) Minecraft.getMinecraft().currentScreen;
                tracker = new IngredientTracker(container, recipeLayout, player, recipesGui);
                if (doTransfer) {
                    tracker = new IngredientTracker(container, recipeLayout, player, recipesGui);
                    boolean doCraftingHelp = Keyboard.isKeyDown(craftingHelperPreview.getKeyCode()) || Keyboard.isKeyDown(craftingHelperNoPreview.getKeyCode());
                    noPreview = Keyboard.isKeyDown(craftingHelperNoPreview.getKeyCode());
                    if (doCraftingHelp) {
                        if (noPreview) {
                            if (!tracker.getRequireStacks().isEmpty()) {
                                IAEItemStack stack = AEItemStack.fromItemStack(tracker.getRequiredStack(0));
                                NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(stack, noPreview));
                                stackIndex = 1;
                            } else {
                                moveItems(container, recipeLayout);
                            }
                        } else if (NEEConfig.enableCraftAmountSettingGui && tracker.hasCraftableIngredient()) {
                            if (!GuiUtils.isWirelessCraftingTermContainer(container)) {
                                NEENetworkHandler.getInstance().sendToServer(new PacketOpenCraftAmount(getRecipeOutput(recipeLayout)));
                            } else if (Loader.isModLoaded(ModIds.WCT)) {
                                openWirelessCraftingAmountGui(container, recipeLayout);
                            }
                        }
                    } else {
                        moveItems(container, recipeLayout);
                    }
                } else {
                    if (NEEConfig.enableCraftAmountSettingGui) {
                        return new CraftingHelperTooltipError(new IngredientTracker(recipeLayout, recipesGui), true);
                    } else {
                        return new CraftingHelperTooltipError(tracker, true);
                    }
                }
            }
        }
        return null;
    }

    private void moveItems(AEBaseContainer container, IRecipeLayout recipeLayout) {

        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

        final NBTTagCompound recipe = new NBTTagCompound();

        int slotIndex = 0;
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
            if (!ingredient.isInput()) {
                continue;
            }

            for (final Slot slot : container.inventorySlots) {
                if (slot instanceof SlotCraftingMatrix || slot instanceof SlotFakeCraftingMatrix) {
                    if (slot.getSlotIndex() == slotIndex) {
                        final NBTTagList tags = new NBTTagList();
                        final List<ItemStack> list = new ArrayList<>();
                        final ItemStack displayed = ingredient.getDisplayedIngredient();

                        // prefer currently displayed item
                        if (displayed != null && !displayed.isEmpty()) {
                            list.add(displayed);
                        }

                        // prefer pure crystals.
                        for (ItemStack stack : ingredient.getAllIngredients()) {
                            if (Platform.isRecipePrioritized(stack)) {
                                list.add(0, stack);
                            } else {
                                list.add(stack);
                            }
                        }

                        for (final ItemStack is : list) {
                            final NBTTagCompound tag = new NBTTagCompound();
                            is.writeToNBT(tag);
                            tags.appendTag(tag);
                        }

                        recipe.setTag("#" + slot.getSlotIndex(), tags);
                        break;
                    }
                }
            }

            slotIndex++;
        }

        try {
            if (container instanceof ContainerCraftingTerm) {
                NetworkHandler.instance().sendToServer(new PacketJEIRecipe(recipe));
            } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                moveItemsForWirelessTerm(recipe);
            }
        } catch (IOException e) {
            AELog.debug(e);
        }

    }

    private ItemStack getRecipeOutput(IRecipeLayout recipeLayout) {
        ItemStack recipeOutput = ItemStack.EMPTY;
        for (IGuiIngredient<ItemStack> guiIngredient : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (!guiIngredient.isInput() && guiIngredient.getDisplayedIngredient() != null && !guiIngredient.getDisplayedIngredient().isEmpty()) {
                recipeOutput = guiIngredient.getDisplayedIngredient().copy();
            }
        }
        return recipeOutput;
    }

    @Optional.Method(modid = ModIds.WCT)
    private void openWirelessCraftingAmountGui(Container container, IRecipeLayout recipeLayout) {
        if (container instanceof ContainerWCT) {
            ContainerWCT wct = (ContainerWCT) container;
            NEENetworkHandler.getInstance().sendToServer(new PacketOpenCraftAmount(getRecipeOutput(recipeLayout), wct.isWTBauble(), wct.getWTSlot()));
        }
    }

    @Optional.Method(modid = ModIds.WCT)
    private void moveItemsForWirelessTerm(NBTTagCompound recipe) {
        try {
            ModNetworking.instance().sendToServer(new p455w0rd.wct.sync.packets.PacketJEIRecipe(recipe));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
