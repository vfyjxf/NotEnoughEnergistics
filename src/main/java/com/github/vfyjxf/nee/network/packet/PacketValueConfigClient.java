package com.github.vfyjxf.nee.network.packet;

import codechicken.nei.recipe.GuiRecipe;
import com.github.vfyjxf.nee.nei.NEECraftingHelper;
import com.github.vfyjxf.nee.utils.GuiUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class PacketValueConfigClient implements IMessage, IMessageHandler<PacketValueConfigClient, IMessage> {


    private String name;
    private String value;

    public PacketValueConfigClient() {

    }

    public PacketValueConfigClient(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public PacketValueConfigClient(String name) {
        this.name = name;
        this.value = "";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.name = ByteBufUtils.readUTF8String(buf);
        this.value = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.name);
        ByteBufUtils.writeUTF8String(buf, this.value);
    }

    @Override
    public IMessage onMessage(PacketValueConfigClient message, MessageContext ctx) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if ("PatternInterface.check".equals(message.name) && Boolean.parseBoolean(message.value)) {
            if (gui instanceof GuiRecipe) {
                GuiRecipe guiRecipe = (GuiRecipe) gui;
                if (GuiUtils.isGuiCraftingTerm(guiRecipe.firstGui)) {
                    NEECraftingHelper.setIsPatternInterfaceExists(true);
                }
            }
        }
        return null;
    }
}
