package com.github.vfyjxf.nee.asm;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.wct.client.gui.GuiWCT;

import java.util.List;

@SuppressWarnings("unused")
public class AppengHooks {

    /**
     * Before {@link appeng.core.sync.packets.PacketMEInventoryUpdate#clientPacketData(INetworkInfo, AppEngPacket, EntityPlayer)} Return
     */
    public static void updateMeInventory(GuiScreen screen, final List<IAEItemStack> list) {
        if (screen instanceof RecipesGui) {
            GuiScreen parent = ((RecipesGui) screen).getParentScreen();
            if (parent instanceof GuiMEMonitorable) {
                ((GuiMEMonitorable) parent).postUpdate(list);
            }
        }
    }

    /**
     * Before {@link appeng.client.gui.implementations.GuiMEMonitorable#postUpdate(List)} Return
     */
    public static void updateWirelessInventory(GuiScreen screen, final List<IAEItemStack> list){
        if (screen instanceof RecipesGui) {
            GuiScreen parent = ((RecipesGui) screen).getParentScreen();
            if (parent instanceof GuiWCT) {
                ((GuiWCT) parent).postUpdate(list);
            }
        }
    }

}
