package com.github.vfyjxf.nee.network.packet;

import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftingTerm;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.network.NEEGuiHandler;
import com.github.vfyjxf.nee.utils.GuiUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static com.github.vfyjxf.nee.jei.PatternTransferHandler.OUTPUT_KEY;
import static com.github.vfyjxf.nee.network.NEEGuiHandler.CRAFTING_AMOUNT_ID;
import static com.github.vfyjxf.nee.network.NEEGuiHandler.WIRELESS_CRAFTING_AMOUNT_ID;

public class PacketOpenCraftAmount implements IMessage {

    private NBTTagCompound recipe;
    private boolean isWirelessTerm;
    private boolean isBauble;
    private int wctSlot = -1;

    public PacketOpenCraftAmount() {

    }

    public PacketOpenCraftAmount(NBTTagCompound recipe) {
        this.recipe = recipe;
    }

    public PacketOpenCraftAmount(NBTTagCompound recipe, boolean isBauble, int wctSlot) {
        this.recipe = recipe;
        this.isBauble = isBauble;
        this.wctSlot = wctSlot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.recipe = ByteBufUtils.readTag(buf);
        this.isWirelessTerm = buf.readBoolean();
        if (isWirelessTerm) {
            this.isBauble = buf.readBoolean();
            this.wctSlot = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.recipe);
        if (this.wctSlot >= 0) {
            buf.writeBoolean(true);
            buf.writeBoolean(this.isBauble);
            buf.writeInt(this.wctSlot);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static class Handler implements IMessageHandler<PacketOpenCraftAmount, IMessage> {

        @Override
        public IMessage onMessage(PacketOpenCraftAmount message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            Container container = player.openContainer;
            player.getServerWorld().addScheduledTask(() -> {
                if (container instanceof ContainerCraftingTerm) {
                    final ContainerOpenContext context = ((ContainerCraftingTerm) container).getOpenContext();
                    if (context != null) {
                        final TileEntity tile = context.getTile();

                        NEEGuiHandler.openGui(player, CRAFTING_AMOUNT_ID, tile, context.getSide());

                        if (player.openContainer instanceof ContainerCraftingAmount) {
                            ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                            if (message.recipe != null && !message.recipe.isEmpty()) {
                                NBTTagCompound resultTag = message.recipe.getCompoundTag(OUTPUT_KEY);
                                ItemStack result = resultTag.isEmpty() ? ItemStack.EMPTY : new ItemStack(resultTag);
                                cca.setResultStack(result);
                                cca.getResultSlot().putStack(result);
                                cca.setRecipe(message.recipe);
                            }
                            cca.detectAndSendChanges();
                        }

                    }
                } else if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                    NEEGuiHandler.openGui(player, WIRELESS_CRAFTING_AMOUNT_ID, player.world);

                    if (player.openContainer instanceof ContainerCraftingAmount) {
                        ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                        if (message.recipe != null) {
                            NBTTagCompound resultTag = message.recipe.getCompoundTag(OUTPUT_KEY);
                            ItemStack result = resultTag.isEmpty() ? ItemStack.EMPTY : new ItemStack(resultTag);
                            cca.setResultStack(result);
                            cca.getResultSlot().putStack(result);
                            cca.setRecipe(message.recipe);
                        }
                        if (message.isWirelessTerm) {
                            cca.setBauble(message.isBauble);
                            cca.setWctSlot(message.wctSlot);
                        }
                        cca.detectAndSendChanges();
                    }
                }
            });
            return null;
        }

    }

}
