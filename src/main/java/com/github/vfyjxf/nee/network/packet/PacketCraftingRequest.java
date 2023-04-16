package com.github.vfyjxf.nee.network.packet;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.core.AELog;
import appeng.util.item.AEItemStack;
import com.github.vfyjxf.nee.block.tile.TilePatternInterface;
import com.github.vfyjxf.nee.container.ContainerCraftingAmount;
import com.github.vfyjxf.nee.container.ContainerCraftingConfirm;
import com.github.vfyjxf.nee.container.WCTContainerCraftingConfirm;
import com.github.vfyjxf.nee.network.NEEGuiHandler;
import com.github.vfyjxf.nee.utils.Globals;
import com.github.vfyjxf.nee.utils.GuiUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.Pair;
import p455w0rd.wct.api.IWCTContainer;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;

import static com.github.vfyjxf.nee.jei.CraftingTransferHandler.RECIPE_LENGTH;
import static com.github.vfyjxf.nee.utils.Globals.OUTPUT_KEY_HEAD;

public class PacketCraftingRequest implements IMessage {

    private IAEItemStack requireToCraftStack;
    private boolean isAutoStart;
    private int craftAmount;


    public PacketCraftingRequest() {

    }

    public PacketCraftingRequest(IAEItemStack requireToCraftStack, boolean isAutoStart) {
        this.requireToCraftStack = requireToCraftStack;
        this.isAutoStart = isAutoStart;
    }

    public PacketCraftingRequest(int craftAmount, boolean isAutoStart) {
        this.craftAmount = craftAmount;
        this.isAutoStart = isAutoStart;
    }

    public IAEItemStack getRequireToCraftStack() {
        return requireToCraftStack;
    }

    public boolean isAutoStart() {
        return isAutoStart;
    }

    public int getCraftAmount() {
        return craftAmount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            this.requireToCraftStack = AEItemStack.fromPacket(buf);
        }
        this.isAutoStart = buf.readBoolean();
        this.craftAmount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (this.requireToCraftStack != null) {
            buf.writeBoolean(true);
            try {
                this.requireToCraftStack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(this.isAutoStart);
        buf.writeInt(this.craftAmount);
    }

    public static class Handler implements IMessageHandler<PacketCraftingRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketCraftingRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            Container container = player.openContainer;
            if(!(container instanceof AEBaseContainer)) return null;
            player.getServerWorld().addScheduledTask(() -> {
                AEBaseContainer baseContainer = (AEBaseContainer) container;
                Object target = baseContainer.getTarget();

                if (target instanceof IActionHost) {
                    final IActionHost ah = (IActionHost) target;
                    final IGridNode gn = ah.getActionableNode();
                    final IGrid grid = gn.getGrid();
                    final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
                    if (security.hasPermission(player, SecurityPermissions.CRAFT)) {
                        if (container instanceof ContainerCraftingTerm) {
                            handlerCraftingTermRequest((ContainerCraftingTerm) baseContainer, message, grid, player);
                        }
                        if (container instanceof ContainerCraftingAmount) {
                            handlerCraftingAmountRequest((ContainerCraftingAmount) baseContainer, message, grid, player);
                        }
                        if (GuiUtils.isWirelessCraftingTermContainer(container)) {
                            handlerWirelessCraftingRequest(baseContainer, message, grid, player);
                        }
                    }
                }

            });
            return null;
        }

        private void handlerCraftingTermRequest(ContainerCraftingTerm container, PacketCraftingRequest message, IGrid grid, EntityPlayerMP player) {
            if (message.getRequireToCraftStack() != null) {
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(player.world, grid, container.getActionSource(), message.getRequireToCraftStack(), null);

                    final ContainerOpenContext context = container.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        NEEGuiHandler.openGui(player, NEEGuiHandler.CONFIRM_WRAPPER_ID, te, context.getSide());
                        if (player.openContainer instanceof ContainerCraftConfirm) {
                            final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
                            ccc.setAutoStart(message.isAutoStart());
                            ccc.setJob(futureJob);
                            ccc.detectAndSendChanges();
                        }
                    }
                } catch (final Throwable e) {
                    if (futureJob != null) {
                        futureJob.cancel(true);
                    }
                    AELog.debug(e);
                }
            }
        }

        private void handlerCraftingAmountRequest(ContainerCraftingAmount container, PacketCraftingRequest message, IGrid grid, EntityPlayerMP player) {
            if (!container.getResultStack().isEmpty()) {

                Pair<TilePatternInterface, Integer> pair = setRecipe(grid, container.getRecipe(), player);

                if (pair != null) {

                    final IStorageGrid inv = grid.getCache(IStorageGrid.class);
                    final IMEMonitor<IAEItemStack> storage = inv.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                    final IItemList<IAEItemStack> all = storage.getStorageList();
                    ItemStack resultStack = container.getResultStack().copy();
                    IAEItemStack result = null;
                    /*
                     *For some reason,the output from jei is different from the actual one,
                     *so apply ItemStack.isItemEqual to match.
                     */
                    for (IAEItemStack aeStack : all) {
                        if (resultStack.isItemEqual(aeStack.asItemStackRepresentation()) && aeStack.isCraftable()) {
                            result = aeStack.copy();
                            break;
                        }
                    }
                    if (result != null) {
                        result.setStackSize(message.craftAmount);
                        Future<ICraftingJob> futureJob = null;
                        try {
                            final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                            futureJob = cg.beginCraftingJob(player.world, grid, container.getActionSource(), result, null);

                            final ContainerOpenContext context = container.getOpenContext();
                            if (context != null) {
                                final TileEntity te = context.getTile();
                                NEEGuiHandler.openGui(player, NEEGuiHandler.CRAFTING_CONFIRM_ID, te, context.getSide());
                                if (player.openContainer instanceof ContainerCraftingConfirm) {
                                    final ContainerCraftingConfirm ccc = (ContainerCraftingConfirm) player.openContainer;
                                    ccc.setAutoStart(message.isAutoStart());
                                    ccc.setJob(futureJob);
                                    ccc.setTile(pair.getLeft());
                                    ccc.setPatternIndex(pair.getRight());
                                    ccc.detectAndSendChanges();
                                }
                            } else if (Loader.isModLoaded(Globals.WCT) && container.isWirelessTerm()) {

                                NEEGuiHandler.openGui(player, NEEGuiHandler.WIRELESS_CRAFTING_CONFIRM_ID, player.world);

                                if (player.openContainer instanceof WCTContainerCraftingConfirm) {
                                    final WCTContainerCraftingConfirm ccc = (WCTContainerCraftingConfirm) player.openContainer;
                                    ccc.setAutoStart(message.isAutoStart());
                                    ccc.setJob(futureJob);
                                    ccc.setTile(pair.getLeft());
                                    ccc.setPatternIndex(pair.getRight());
                                    ccc.detectAndSendChanges();
                                }
                            }
                        } catch (final Throwable e) {
                            if (futureJob != null) {
                                futureJob.cancel(true);
                            }
                            AELog.debug(e);
                        }
                    }


                }

            }
        }

        private void handlerWirelessCraftingRequest(AEBaseContainer container, PacketCraftingRequest message, IGrid grid, EntityPlayerMP player) {
            if (message.getRequireToCraftStack() != null) {
                IWCTContainer iwtContainer = (IWCTContainer) container;
                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = grid.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(player.world, grid, container.getActionSource(), message.getRequireToCraftStack(), null);

                    int x = (int) player.posX;
                    int y = (int) player.posY;
                    int z = (int) player.posZ;

                    NEEGuiHandler.openGui(player, NEEGuiHandler.WIRELESS_CONFIRM_WRAPPER_ID, player.world);

                    if (player.openContainer instanceof p455w0rd.wct.container.ContainerCraftConfirm) {
                        final p455w0rd.wct.container.ContainerCraftConfirm ccc = (p455w0rd.wct.container.ContainerCraftConfirm) player.openContainer;
                        ccc.setAutoStart(message.isAutoStart());
                        ccc.setJob(futureJob);
                        ccc.detectAndSendChanges();
                    }
                } catch (Throwable e) {
                    if (futureJob != null) {
                        futureJob.cancel(true);
                    }
                }
            }
        }

        private Pair<TilePatternInterface, Integer> setRecipe(IGrid grid, NBTTagCompound recipe, EntityPlayer player) {
            for (IGridNode gridNode : grid.getMachines(TilePatternInterface.class)) {

                if (gridNode.getMachine() instanceof TilePatternInterface) {

                    TilePatternInterface tpi = (TilePatternInterface) gridNode.getMachine();
                    NBTTagCompound currentTag = recipe.getCompoundTag(OUTPUT_KEY_HEAD);
                    ItemStack result = currentTag.isEmpty() ? ItemStack.EMPTY : new ItemStack(currentTag);

                    if (tpi.getProxy().isActive() && tpi.canPutPattern(result)) {

                        ItemStack patternStack = getPatternStack(player, recipe);

                        if (!patternStack.isEmpty()) {

                            int patternIndex = tpi.putPattern(patternStack);
                            if (patternIndex >= 0) {
                                return Pair.of(tpi, patternIndex);
                            }
                        }

                    }

                }
            }
            return null;
        }

        private ItemStack getPatternStack(EntityPlayer player, NBTTagCompound recipe) {
            ItemStack[] recipeInputs = new ItemStack[RECIPE_LENGTH];
            NBTTagCompound currentStack;
            for (int i = 0; i < recipeInputs.length; i++) {
                currentStack = recipe.getCompoundTag(Globals.INPUT_KEY_HEAD + i);
                recipeInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : new ItemStack(currentStack);
            }
            InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
            for (int i = 0; i < RECIPE_LENGTH; i++) {
                ic.setInventorySlotContents(i, recipeInputs[i]);
            }

            IRecipe iRecipe = CraftingManager.findMatchingRecipe(ic, player.world);
            if (iRecipe != null) {
                ItemStack outputStack = iRecipe.getCraftingResult(ic);
                if (!outputStack.isEmpty()) {
                    ItemStack patternStack = null;
                    Optional<ItemStack> maybePattern = AEApi.instance().definitions().items().encodedPattern().maybeStack(1);
                    if (maybePattern.isPresent()) {
                        patternStack = maybePattern.get();
                    }
                    if (patternStack != null && !patternStack.isEmpty()) {
                        final NBTTagCompound patternValue = new NBTTagCompound();
                        final NBTTagList tagIn = new NBTTagList();
                        for (ItemStack stack : recipeInputs) {
                            tagIn.appendTag(crateItemTag(stack));
                        }
                        patternValue.setTag("in", tagIn);
                        patternValue.setTag("out", outputStack.writeToNBT(new NBTTagCompound()));
                        patternValue.setBoolean("crafting", true);
                        patternValue.setBoolean("substitute", false);

                        patternStack.setTagCompound(patternValue);
                        return patternStack;
                    }
                }
            }

            return ItemStack.EMPTY;
        }

        private NBTTagCompound crateItemTag(ItemStack itemStack) {
            NBTTagCompound tag = new NBTTagCompound();
            if (!itemStack.isEmpty()) {
                itemStack.writeToNBT(tag);
            }
            return tag;
        }

    }


}
