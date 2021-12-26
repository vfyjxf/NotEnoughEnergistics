package com.github.vfyjxf.nee.network.packet;

import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftingTerm;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.gui.NEEGuiHandler;
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

import static com.github.vfyjxf.nee.gui.NEEGuiHandler.CRAFTING_AMOUNT_ID;
import static com.github.vfyjxf.nee.jei.CraftingHelperTransferHandler.RESULT_KEY;
import static com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler.INPUT_KEY;

public class PacketOpenCraftAmount implements IMessage, IMessageHandler<PacketOpenCraftAmount, IMessage> {

    private NBTTagCompound recipe;

    public PacketOpenCraftAmount() {

    }

    public PacketOpenCraftAmount(NBTTagCompound recipe) {
        this.recipe = recipe;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.recipe = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.recipe);
    }

    @Override
    public IMessage onMessage(PacketOpenCraftAmount message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        Container container = player.openContainer;
        if (container instanceof ContainerCraftingTerm) {
            player.getServerWorld().addScheduledTask(() -> {
                final ContainerOpenContext context = ((ContainerCraftingTerm) container).getOpenContext();
                if (context != null) {
                    final TileEntity tile = context.getTile();

                    NEEGuiHandler.openGui(player, CRAFTING_AMOUNT_ID, tile, context.getSide());

                    if (player.openContainer instanceof ContainerCraftingAmount) {
                        ContainerCraftingAmount cca = (ContainerCraftingAmount) player.openContainer;
                        if (message.recipe.hasKey(INPUT_KEY) && message.recipe.hasKey(RESULT_KEY)) {
                            cca.setFirstInputStack(new ItemStack(message.recipe.getCompoundTag(INPUT_KEY)));
                            ItemStack resultStack = new ItemStack(message.recipe.getCompoundTag(RESULT_KEY));
                            cca.setResultStack(resultStack);
                            cca.getResultSlot().putStack(resultStack);
                        }
                        cca.detectAndSendChanges();
                    }

                }
            });
        }
        return null;
    }
}
