package com.github.vfyjxf.nee.network.packet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperInvItemHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

import static com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler.OUTPUT_KEY;

/**
 * @author vfyjxf
 */
public class PacketRecipeTransfer implements IMessage, IMessageHandler<PacketRecipeTransfer, IMessage> {

    private NBTTagCompound input;
    private NBTTagCompound output;
    private boolean craftingMode;

    public PacketRecipeTransfer() {

    }

    public PacketRecipeTransfer(@Nonnull NBTTagCompound input, NBTTagCompound output, boolean craftingMode) {
        this.input = input;
        this.output = output;
        this.craftingMode = craftingMode;
    }

    public NBTTagCompound getInput() {
        return input;
    }

    public NBTTagCompound getOutput() {
        return output;
    }

    public boolean getCraftingMode() {
        return craftingMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.input = ByteBufUtils.readTag(buf);
        this.craftingMode = buf.readBoolean();
        if (buf.readBoolean()) {
            this.output = ByteBufUtils.readTag(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.input);
        buf.writeBoolean(this.craftingMode);
        if (this.output != null) {
            buf.writeBoolean(true);
            ByteBufUtils.writeTag(buf, this.output);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public IMessage onMessage(PacketRecipeTransfer message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        player.getServerWorld().addScheduledTask(() -> {
            if (container instanceof ContainerPatternTerm) {
                ((ContainerPatternTerm) container).getPatternTerminal().setCraftingRecipe(message.getCraftingMode());
                ItemStack[] recipeInputs = new ItemStack[9];
                ItemStack[] recipeOutputs = null;
                NBTTagCompound currentStack;

                for (int i = 0; i < recipeInputs.length; i++) {
                    currentStack = message.getInput().getCompoundTag("#" + i);
                    recipeInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : new ItemStack(currentStack);
                }

                if (!message.output.isEmpty()) {
                    recipeOutputs = new ItemStack[3];
                    for (int i = 0; i < recipeOutputs.length; i++) {
                        currentStack = message.getOutput().getCompoundTag(OUTPUT_KEY + i);
                        recipeOutputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : new ItemStack(currentStack);
                    }
                }
                final IContainerCraftingPacket cct = (IContainerCraftingPacket) container;
                final IGridNode node = cct.getNetworkNode();
                if (node == null) {
                    return;
                }
                final IGrid grid = node.getGrid();
                final IStorageGrid inv = grid.getCache(IStorageGrid.class);
                final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
                final IItemHandler craftMatrix = cct.getInventoryByName("crafting");
                if (message.input != null) {
                    for (int i = 0; i < craftMatrix.getSlots(); i++) {
                        ItemStack currentItem = ItemStack.EMPTY;
                        if (recipeInputs[i] != null) {
                            currentItem = recipeInputs[i].copy();
                        }
                        ItemHandlerUtil.setStackInSlot(craftMatrix, i, currentItem);
                    }
                    if (recipeOutputs != null && !message.getCraftingMode()) {
                        final IItemHandler outputMatrix = cct.getInventoryByName("output");
                        for (int i = 0; i < outputMatrix.getSlots(); i++) {
                            ItemStack currentItem = ItemStack.EMPTY;
                            if (recipeOutputs[i] != null) {
                                currentItem = recipeOutputs[i].copy();
                            }
                            ItemHandlerUtil.setStackInSlot(outputMatrix, i, currentItem);
                        }
                    }
                    container.onCraftMatrixChanged(new WrapperInvItemHandler(craftMatrix));
                }
            }
        });
        return null;
    }

}
