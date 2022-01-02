package com.github.vfyjxf.nee.gui;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.parts.reporting.PartCraftingTerminal;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.utils.ModIDs;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTermHandler;
import net.p455w0rd.wirelesscraftingterminal.common.utils.RandomUtils;
import net.p455w0rd.wirelesscraftingterminal.helpers.WirelessTerminalGuiObject;

import javax.annotation.Nullable;

import static com.github.vfyjxf.nee.NotEnoughEnergistics.instance;

public class NEEGuiHandler implements IGuiHandler {

    public static final int CRAFTING_AMOUNT_ID = 0;
    public static final int CRAFTING_AMOUNT_WIRELESS_ID = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        final int guiId = ordinal >> 4;
        if (guiId != CRAFTING_AMOUNT_WIRELESS_ID) {
            final ForgeDirection side = ForgeDirection.getOrientation(ordinal & 7);
            if (side != ForgeDirection.UNKNOWN) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (tile instanceof IPartHost) {
                    IPartHost partHost = (IPartHost) tile;
                    IPart part = partHost.getPart(side);
                    if (guiId == CRAFTING_AMOUNT_ID) {
                        if (part instanceof PartCraftingTerminal) {
                            return updateGui(new ContainerCraftingAmount(player.inventory, part), world, x, y, z, side, part);
                        }
                    }
                }
            }
        } else if (Loader.isModLoaded(ModIDs.WCT)) {
            final IPortableCell wirelessTerminal = getWirelessTerminalGui(player, world, x, y, z);
            if (wirelessTerminal != null) {
                return new ContainerCraftingAmount(player.inventory, wirelessTerminal);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        final int guiId = ordinal >> 4;
        if (guiId != CRAFTING_AMOUNT_WIRELESS_ID) {
            final ForgeDirection side = ForgeDirection.getOrientation(ordinal & 7);
            if (side != ForgeDirection.UNKNOWN) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (tile instanceof IPartHost) {
                    IPartHost partHost = (IPartHost) tile;
                    IPart part = partHost.getPart(side);
                    if (guiId == CRAFTING_AMOUNT_ID) {
                        if (part instanceof PartCraftingTerminal) {
                            return updateGui(new GuiCraftingAmount(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                        }
                    }
                }

            }
        } else if (Loader.isModLoaded(ModIDs.WCT)) {
            final IPortableCell wirelessTerminal = getWirelessTerminalGui(player, world, x, y, z);
            if (wirelessTerminal != null) {
                return new GuiCraftingAmount(player.inventory, wirelessTerminal);
            }
        }
        return null;
    }

    private Object updateGui(Object newContainer, final World w, final int x, final int y, final int z, final ForgeDirection side, final IPart part) {
        if (newContainer instanceof AEBaseContainer) {
            final AEBaseContainer bc = (AEBaseContainer) newContainer;
            bc.setOpenContext(new ContainerOpenContext(part));
            bc.getOpenContext().setWorld(w);
            bc.getOpenContext().setX(x);
            bc.getOpenContext().setY(y);
            bc.getOpenContext().setZ(z);
            bc.getOpenContext().setSide(side);
        }
        return newContainer;
    }

    public static void openGui(EntityPlayer player, int ID, TileEntity tile, ForgeDirection side) {
        int x = tile.xCoord;
        int y = tile.yCoord;
        int z = tile.zCoord;
        player.openGui(instance, ID << 4 | (side.ordinal()), tile.getWorldObj(), x, y, z);
    }

    public static void openGui(EntityPlayer player, int ID, World world) {
        int x = (int) player.posX;
        int y = (int) player.posY;
        int z = (int) player.posZ;
        player.openGui(instance, (ID << 4), world, x, y, z);
    }

    @Optional.Method(modid = "ae2wct")
    private WirelessTerminalGuiObject getWirelessTerminalGui(EntityPlayer player, World world, int x, int y, int z) {
        final IWirelessCraftingTermHandler wh = (IWirelessCraftingTermHandler) AEApi.instance().registries().wireless().getWirelessTerminalHandler(RandomUtils.getWirelessTerm(player.inventory));
        if (wh != null) {
            return new WirelessTerminalGuiObject(wh, RandomUtils.getWirelessTerm(player.inventory), player, world, x, y, z);
        }
        return null;
    }

}
