package com.github.vfyjxf.nee.network.packet;

import com.github.vfyjxf.nee.network.PlayerAction;
import com.github.vfyjxf.nee.utils.PatternContainerControl;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPlayerAction implements IMessage {

    private int action;
    private NBTTagCompound data;

    public PacketPlayerAction() {

    }

    public PacketPlayerAction(PlayerAction action, NBTTagCompound data) {
        this.action = action.ordinal();
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = buf.readInt();
        this.data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action);
        ByteBufUtils.writeTag(buf, this.data);
    }


    public static class Handler implements IMessageHandler<PacketPlayerAction, IMessage> {
        @Override
        public IMessage onMessage(PacketPlayerAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                PlayerAction action = PlayerAction.values()[(message.action)];
                switch (action) {
                    case PICK:
                        pick(player, message.data);
                        break;
                    case PUT:
                        put(player, message.data);
                        break;
                    case DROP:
                        drop(player, message.data);
                        break;
                    default:
                        break;
                }
            });
            return null;
        }

        private static void pick(EntityPlayerMP player, NBTTagCompound data) {
            int slot = data.getInteger("slot");
            String uid = data.getString("uid");
            ItemStack extract = PatternContainerControl.extractPattern(uid, slot);
            if (!extract.isEmpty()) {
                player.inventory.setItemStack(extract);
                player.openContainer.detectAndSendChanges();
            }
        }

        private static void put(EntityPlayerMP player, NBTTagCompound data) {
            int slot = data.getInteger("slot");
            String uid = data.getString("uid");
            ItemStack stack = player.inventory.getItemStack();
            if (!stack.isEmpty()) {
                ItemStack left = PatternContainerControl.insertPattern(uid, slot, stack);
                if (left != null) {
                    player.inventory.setItemStack(left);
                    player.openContainer.detectAndSendChanges();
                }
            }
        }

        private static void drop(EntityPlayerMP player, NBTTagCompound data) {
            int slot = data.getInteger("slot");
            String uid = data.getString("uid");
            ItemStack extract = PatternContainerControl.extractPattern(uid, slot);
            if (!extract.isEmpty()) {
                player.dropItem(extract, false);
            }
        }

    }

}
