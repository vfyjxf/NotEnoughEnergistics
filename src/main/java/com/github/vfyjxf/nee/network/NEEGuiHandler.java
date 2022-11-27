package com.github.vfyjxf.nee.network;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.parts.reporting.PartCraftingTerminal;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import com.github.vfyjxf.nee.client.gui.ConfirmWrapperGui;
import com.github.vfyjxf.nee.client.gui.CraftingAmountGui;
import com.github.vfyjxf.nee.client.gui.PatternInterfaceGui;
import com.github.vfyjxf.nee.client.gui.WirelessConfirmWrapperGui;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.container.ContainerCraftingConfirm;
import com.github.vfyjxf.nee.container.ContainerPatternInterface;
import com.github.vfyjxf.nee.container.WCTContainerCraftingConfirm;
import com.github.vfyjxf.nee.utils.Globals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.apache.commons.lang3.tuple.Pair;
import p455w0rd.ae2wtlib.api.ICustomWirelessTerminalItem;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.WTGuiObject;
import p455w0rd.wct.api.IWirelessCraftingTerminalItem;
import p455w0rd.wct.container.ContainerWCT;
import p455w0rd.wct.init.ModGuiHandler;
import p455w0rd.wct.util.WCTUtils;

import javax.annotation.Nullable;

import static com.github.vfyjxf.nee.NotEnoughEnergistics.instance;

public class NEEGuiHandler implements IGuiHandler {


    public static final int CRAFTING_AMOUNT_ID = 0;
    public static final int CRAFTING_CONFIRM_ID = 1;
    public static final int PATTERN_INTERFACE_ID = 2;
    public static final int CONFIRM_WRAPPER_ID = 3;

    public static final int WIRELESS_START_INDEX = 100;
    public static final int WIRELESS_CRAFTING_CONFIRM_ID = WIRELESS_START_INDEX + 1;
    public static final int WIRELESS_CRAFTING_AMOUNT_ID = WIRELESS_START_INDEX + 2;
    public static final int WIRELESS_CONFIRM_WRAPPER_ID = WIRELESS_START_INDEX + 3;

    @Nullable
    @Override
    public Object getServerGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        final int guiId = ordinal >> 8;
        if (guiId < WIRELESS_START_INDEX) {
            final AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 7);
            //There should be no crafting terminal in the center of the block, right?
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (side != AEPartLocation.INTERNAL) {
                if (tile instanceof IPartHost) {
                    IPartHost partHost = (IPartHost) tile;
                    IPart part = partHost.getPart(side);
                    switch (guiId) {
                        case CRAFTING_AMOUNT_ID:
                            if (part instanceof PartCraftingTerminal) {
                                return updateGui(new ContainerCraftingAmount(player.inventory, part), world, x, y, z, side, part);
                            }
                            return null;
                        case CRAFTING_CONFIRM_ID:
                            if (part instanceof PartCraftingTerminal) {
                                return updateGui(new ContainerCraftingConfirm(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                            }
                            return null;
                        case CONFIRM_WRAPPER_ID:
                            if (part instanceof PartCraftingTerminal) {
                                return updateGui(new ContainerCraftConfirm(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                            }
                            return null;
                        default:
                            break;
                    }

                }
            } else {
                if (tile != null) {
                    if (guiId == PATTERN_INTERFACE_ID) {
                        if (tile instanceof TilePatternInterface) {
                            return new ContainerPatternInterface(player.inventory, (TilePatternInterface) tile);
                        }
                    }
                }
            }
        } else if (Loader.isModLoaded(Globals.WCT)) {

            final ITerminalHost craftingTerminal = getCraftingTerminal(player, ModGuiHandler.isBauble(), ModGuiHandler.getSlot());
            if (craftingTerminal != null) {
                Container container = player.openContainer;
                if (guiId == WIRELESS_CRAFTING_AMOUNT_ID) {
                    if (container instanceof ContainerWCT) {
                        return new ContainerCraftingAmount(player.inventory, craftingTerminal);
                    }
                } else {
                    return new WCTContainerCraftingConfirm(player.inventory, craftingTerminal, ModGuiHandler.isBauble(), ModGuiHandler.getSlot());
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ordinal, EntityPlayer player, World world, int x, int y, int z) {
        final int guiId = ordinal >> 8;
        if (guiId < WIRELESS_START_INDEX) {
            final AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 7);
            //There should be no crafting terminal in the center of the block, right?
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (side != AEPartLocation.INTERNAL) {
                if (tile instanceof IPartHost) {
                    IPartHost partHost = (IPartHost) tile;
                    IPart part = partHost.getPart(side);

                    switch (guiId) {
                        case CRAFTING_AMOUNT_ID:
                            if (part instanceof PartCraftingTerminal) {
                                return updateGui(new CraftingAmountGui(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                            }
                            return null;
                        case CRAFTING_CONFIRM_ID:
                            if (part instanceof PartCraftingTerminal) {
                                return updateGui(new GuiCraftConfirm(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                            }
                            return null;
                        case CONFIRM_WRAPPER_ID:
                            if (part instanceof PartCraftingTerminal) {
                                return updateGui(new ConfirmWrapperGui(player.inventory, (ITerminalHost) part), world, x, y, z, side, part);
                            }
                        default:
                            break;
                    }
                }

            } else {
                if (tile != null) {
                    if (guiId == PATTERN_INTERFACE_ID) {
                        if (tile instanceof TilePatternInterface) {
                            return new PatternInterfaceGui(player.inventory, (TilePatternInterface) tile);
                        }
                    }
                }
            }
        } else if (Loader.isModLoaded(Globals.WCT)) {
            final ITerminalHost craftingTerminal = getCraftingTerminal(player, ModGuiHandler.isBauble(), ModGuiHandler.getSlot());
            if (craftingTerminal != null) {
                Container container = player.openContainer;
                if (guiId == WIRELESS_CRAFTING_AMOUNT_ID) {
                    if (container instanceof ContainerWCT) {
                        return new CraftingAmountGui(player.inventory, craftingTerminal, (ContainerWCT) container);
                    }
                } else if (guiId == WIRELESS_CONFIRM_WRAPPER_ID) {
                    return new WirelessConfirmWrapperGui(player.inventory, craftingTerminal, ModGuiHandler.isBauble(), ModGuiHandler.getSlot());
                } else {
                    return new p455w0rd.wct.client.gui.GuiCraftConfirm(player.inventory, craftingTerminal, ModGuiHandler.isBauble(), ModGuiHandler.getSlot());
                }
            }
        }
        return null;
    }

    private Object updateGui(Object newContainer, final World w, final int x, final int y, final int z, final AEPartLocation side, final IPart part) {
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

    public static void openGui(EntityPlayer player, int id, TileEntity tile, AEPartLocation side) {
        int x = tile.getPos().getX();
        int y = tile.getPos().getY();
        int z = tile.getPos().getZ();
        player.openGui(instance, id << 8 | side.ordinal(), tile.getWorld(), x, y, z);
    }

    public static void openGui(EntityPlayer player, int id, World world) {
        int x = (int) player.posX;
        int y = (int) player.posY;
        int z = (int) player.posZ;
        player.openGui(instance, (id << 8), world, x, y, z);
    }

    @SuppressWarnings("unchecked")
    @Optional.Method(modid = Globals.WCT)
    private ITerminalHost getCraftingTerminal(final EntityPlayer player, final boolean isBauble, final int slot) {
        ItemStack wirelessTerminal;
        if (slot >= 0) {
            wirelessTerminal = isBauble ? WTApi.instance().getBaublesUtility().getWTBySlot(player, slot, IWirelessCraftingTerminalItem.class) : WTApi.instance().getWTBySlot(player, slot);
        } else {
            final Pair<Boolean, Pair<Integer, ItemStack>> firstTerm = WCTUtils.getFirstWirelessCraftingTerminal(player.inventory);
            wirelessTerminal = firstTerm.getRight().getRight();
            ModGuiHandler.setSlot(firstTerm.getRight().getLeft());
            ModGuiHandler.setIsBauble(firstTerm.getLeft());
        }
        final ICustomWirelessTerminalItem wh = (ICustomWirelessTerminalItem) AEApi.instance().registries().wireless().getWirelessTerminalHandler(wirelessTerminal);
        return wh == null ? null : (WTGuiObject<IAEItemStack>) WTApi.instance().getGUIObject(wh, wirelessTerminal, player);
    }

}
