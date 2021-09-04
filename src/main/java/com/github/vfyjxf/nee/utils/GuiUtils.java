package com.github.vfyjxf.nee.utils;

import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFakeCraftingMatrix;
import com.github.vfyjxf.nee.jei.NEERecipeTransferHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketRecipeItemChange;
import mezz.jei.api.gui.IGuiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author vfyjxf
 */
public class GuiUtils {
    public static Slot getSlotUnderMouse(GuiContainer container, int mouseX, int mouseY) {
        Method getSlotMethod = ObfuscationReflectionHelper.findMethod(AEBaseGui.class, "getSlot", Slot.class, int.class, int.class);
        try {
            return (Slot) getSlotMethod.invoke(container, mouseX, mouseY);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isCraftingSlot(Slot slot) {
        Container container = Minecraft.getMinecraft().player.openContainer;
        if (container instanceof ContainerPatternTerm) {
            SlotFakeCraftingMatrix[] craftingSlots = null;
            try {
                Field craftingSlotsField = ContainerPatternTerm.class.getDeclaredField("craftingSlots");
                craftingSlotsField.setAccessible(true);
                craftingSlots = (SlotFakeCraftingMatrix[]) craftingSlotsField.get(container);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (craftingSlots != null) {
                for (Slot currentSlot : craftingSlots) {
                    if (currentSlot.equals(slot)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void handleRecipeIngredientChange(Slot currentSlot, int dWheel) {
        IGuiIngredient<ItemStack> ingredient = NEERecipeTransferHandler.ingredients.get("input" + currentSlot.getSlotIndex());
        if (ingredient != null && ingredient.getDisplayedIngredient() != null && ingredient.getAllIngredients().size() > 1) {
            List<ItemStack> currentIngredients = ingredient.getAllIngredients();
            int currentStackIndex = ItemUtils.getIngredientIndex(currentSlot.getStack(), currentIngredients);
            if (currentStackIndex >= 0) {
                int nextStackIndex = dWheel / 120;
                for (int i = 0; i < Math.abs(nextStackIndex); i++) {
                    currentStackIndex = nextStackIndex > 0 ? currentStackIndex + 1 : currentStackIndex - 1;
                    if (currentStackIndex >= currentIngredients.size()) {
                        currentStackIndex = 0;
                    } else if (currentStackIndex < 0) {
                        currentStackIndex = currentIngredients.size() - 1;
                    }
                    ItemStack currentIngredientStack = currentIngredients.get(currentStackIndex).copy();
                    currentIngredientStack.setCount(currentSlot.getStack().getCount());
                    NEENetworkHandler.getInstance().sendToServer(new PacketRecipeItemChange(currentIngredientStack.writeToNBT(new NBTTagCompound()), currentSlot.slotNumber));
                }
            }
        }
    }

}
