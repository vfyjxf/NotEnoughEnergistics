package com.github.vfyjxf.nee.block.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.settings.TickRates;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;
import com.google.common.collect.ImmutableSet;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;


/**
 * A ME interface for storing temporary patterns.
 */
public class TilePatternInterface extends AENetworkInvTile implements IGridTickable, ICraftingProvider, IInventoryDestination {

    private final AppEngInternalInventory gridInv;
    private final AppEngInternalInventory patterns;
    private final AppEngInternalInventory ejectInv;
    private final List<ICraftingPatternDetails> craftingList;
    private List<ItemStack> waitingToSend;
    private final MachineSource source;

    private final boolean[] workStarted = new boolean[9];

    public TilePatternInterface() {
        this.gridInv = new AppEngInternalInventory(this, 9 + 1, 1);
        this.patterns = new AppEngInternalInventory(this, 9);
        this.ejectInv = new AppEngInternalInventory(this, 9);
        this.craftingList = new ArrayList<>();
        this.source = new MachineSource(this);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setIdlePowerUsage(30D);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        this.notifyNeighbors();
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        this.notifyNeighbors();
    }

    private void notifyNeighbors() {
        if (this.getProxy().isActive()) {
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.getProxy().getNode()));
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } catch (GridAccessException e) {
                // :P
            }
            Platform.notifyBlocksOfNeighbors(this.getWorld(), this.getPos());
        }
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !hasItemToSend(), true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy().isActive()) {
            return TickRateModulation.SLEEP;
        }

        if (hasItemToSend()) {
            ejectItems();
        }

        if (this.hasItemsToPush()) {
            this.pushItemsOut(EnumSet.allOf(EnumFacing.class));
        }

        if (hasWorkFinished()) {
            updateWorks();
        }

        if (hasWorks()) {
            return TickRateModulation.URGENT;
        }


        return TickRateModulation.SLEEP;
    }

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return new AppEngInternalInventory(this, 0);
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (this.getProxy().isActive()) {
            if (this.craftingList.isEmpty()) {
                for (int i = 0; i < patterns.getSlots(); i++) {
                    ItemStack stack = patterns.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem) {
                        ICraftingPatternDetails pattern = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, world);
                        if (pattern != null && pattern.isCraftable()) {
                            craftingList.add(pattern);
                        }
                    }
                }
            }
            for (ICraftingPatternDetails details : this.craftingList) {
                craftingTracker.addCraftingOption(this, details);
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!isBusy() && this.getProxy().isActive() && craftingList.contains(patternDetails) && !this.hasItemsToPush()) {
            EnumSet<EnumFacing> possibleDirections = EnumSet.allOf(EnumFacing.class);
            for (final EnumFacing facing : possibleDirections) {
                TileEntity te = this.getWorld().getTileEntity(this.getPos().offset(facing));

                if (te instanceof IInterfaceHost) {
                    try {
                        if (this.sameGrid(this.getProxy().getGrid())) {
                            continue;
                        }
                    } catch (final GridAccessException e) {
                        continue;
                    }
                }

                if (te instanceof ICraftingMachine) {
                    final ICraftingMachine cm = (ICraftingMachine) te;
                    if (cm.acceptsPlans()) {
                        if (cm.pushPattern(patternDetails, table, facing.getOpposite())) {
                            setWorkStart(patternDetails);
                            return true;
                        }
                        continue;
                    }
                }

                final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, facing.getOpposite());
                if (ad != null) {
                    if (this.acceptsItems(ad, table)) {
                        for (int x = 0; x < table.getSizeInventory(); x++) {
                            final ItemStack is = table.getStackInSlot(x);
                            if (!is.isEmpty()) {
                                final ItemStack added = ad.addItems(is);
                                this.addToSendList(added);
                            }
                        }
                        this.pushItemsOut(possibleDirections);
                        setWorkStart(patternDetails);
                        return true;
                    }
                }
            }
        }

        return false;
    }


    @Override
    public boolean isBusy() {
        return hasItemsToPush();
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        if (inv == this.ejectInv || inv == patterns) {
            try {
                this.getProxy().getTick().alertDevice(this.getProxy().getNode());
            } catch (GridAccessException e) {
                // :P
            }
        }
    }

    @Override
    public void getDrops(World w, BlockPos pos, List<ItemStack> drops) {
        if (hasItemsToPush()) {
            for (ItemStack stack : waitingToSend) {
                if (!stack.isEmpty()) {
                    drops.add(stack);
                }
            }
        }
        for (int i = 0; i < ejectInv.getSlots(); i++) {
            ItemStack stack = ejectInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
    }

    public AppEngInternalInventory getGirdInventory() {
        return gridInv;
    }

    public AppEngInternalInventory getPatternInventory() {
        return patterns;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public boolean canInsert(ItemStack stack) {

        if (stack.isEmpty() || stack.getItem() == Items.AIR) {
            return false;
        }

        try {
            final IMEMonitor<IAEItemStack> inv = this.getProxy()
                    .getStorage()
                    .getInventory(
                            AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

            final IAEItemStack out = inv.injectItems(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack),
                    Actionable.SIMULATE,
                    this.source);
            if (out == null) {
                return true;
            }
            return out.getStackSize() != stack.getCount();
        } catch (GridAccessException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capabilityClass, EnumFacing facing) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) this.ejectInv;
        }
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.waitingToSend = null;
        final NBTTagList waitingList = data.getTagList("waitingToSend", 10);
        if (!waitingList.isEmpty()) {
            for (int x = 0; x < waitingList.tagCount(); x++) {
                final NBTTagCompound c = waitingList.getCompoundTagAt(x);
                if (!c.isEmpty()) {
                    final ItemStack is = new ItemStack(c);
                    this.addToSendList(is);
                }
            }
        }
        this.patterns.readFromNBT(data, "patterns");
        this.ejectInv.readFromNBT(data, "ejectInv");

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.patterns.writeToNBT(data, "patterns");
        this.ejectInv.writeToNBT(data, "ejectInv");

        final NBTTagList waitingToSend = new NBTTagList();
        if (this.waitingToSend != null) {
            for (final ItemStack is : this.waitingToSend) {
                final NBTTagCompound item = new NBTTagCompound();
                is.writeToNBT(item);
                waitingToSend.appendTag(item);
            }
        }
        data.setTag("waitingToSend", waitingToSend);
        return data;
    }

    private boolean hasItemsToPush() {
        return this.waitingToSend != null && !this.waitingToSend.isEmpty();
    }

    public boolean canPutPattern(ItemStack result) {
        if (result == null || result.isEmpty()) {
            return false;
        }

        for (int i = 0; i < this.patterns.getSlots(); i++) {
            ItemStack stack = patterns.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem) {
                ICraftingPatternDetails pattern = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, world);
                if (result.isItemEqual(pattern.getOutputs()[0].asItemStackRepresentation())) {
                    return false;
                }
            }

        }


        return IntStream.range(0, this.patterns.getSlots()).anyMatch(slot -> patterns.getStackInSlot(slot).isEmpty());
    }

    public int putPattern(ItemStack pattern) {
        for (int i = 0; i < this.patterns.getSlots(); i++) {
            ItemStack stack = patterns.getStackInSlot(i);
            if (stack.isEmpty()) {
                patterns.setStackInSlot(i, pattern);
                updateCraftingList();
                return i;
            }
        }
        return -1;
    }

    public void updateCraftingList() {

        final Boolean[] accountedFor = {false, false, false, false, false, false, false, false, false};

        assert (accountedFor.length == this.patterns.getSlots());

        if (!this.getProxy().isReady()) {
            return;
        }

        if (craftingList != null) {
            final Iterator<ICraftingPatternDetails> i = this.craftingList.iterator();
            while (i.hasNext()) {
                final ICraftingPatternDetails details = i.next();
                boolean found = false;

                for (int x = 0; x < accountedFor.length; x++) {
                    final ItemStack is = this.patterns.getStackInSlot(x);
                    if (details.getPattern() == is) {
                        accountedFor[x] = found = true;
                    }
                }

                if (!found) {
                    i.remove();
                }
            }
        }
        for (int x = 0; x < accountedFor.length; x++) {
            if (!accountedFor[x]) {
                this.addToCraftingList(this.patterns.getStackInSlot(x));
            }
        }
        try {
            this.getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.getProxy().getNode()));
        } catch (final GridAccessException e) {
            // :P
        }

    }

    public void cancelWork(int recipeIndex) {
        IAEItemStack result = getResultStack(recipeIndex);
        if (result != null) {
            workStarted[recipeIndex] = false;
            try {
                final ICraftingGrid cg = this.getProxy().getCrafting();
                final ImmutableSet<ICraftingCPU> cpuSet = cg.getCpus();

                for (ICraftingCPU cpu : cpuSet) {
                    if (cpu instanceof CraftingCPUCluster) {
                        CraftingCPUCluster cluster = (CraftingCPUCluster) cpu;
                        final IItemList<IAEItemStack> pendingList = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
                        cluster.getListOfItem(pendingList, CraftingItemList.PENDING);
                        for (IAEItemStack pendingStack : pendingList) {
                            if (pendingStack.isSameType(result)) {
                                cluster.cancel();
                                break;
                            }
                        }
                    }
                }

            } catch (GridAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToCraftingList(final ItemStack is) {
        if (is.isEmpty()) {
            return;
        }
        if (is.getItem() instanceof ICraftingPatternItem) {
            final ICraftingPatternItem cpi = (ICraftingPatternItem) is.getItem();
            final ICraftingPatternDetails details = cpi.getPatternForItem(is, this.getWorld());

            if (details != null) {
                if (craftingList != null) {
                    this.craftingList.add(details);
                }
            }
        }
    }

    private void addToSendList(final ItemStack is) {
        if (is.isEmpty()) {
            return;
        }

        if (waitingToSend == null) {
            waitingToSend = new ArrayList<>();
        }

        this.waitingToSend.add(is);

        try {
            this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    private boolean acceptsItems(final InventoryAdaptor ad, final InventoryCrafting table) {
        for (int x = 0; x < table.getSizeInventory(); x++) {
            final ItemStack is = table.getStackInSlot(x);
            if (is.isEmpty()) {
                continue;
            }

            if (!ad.simulateAdd(is.copy()).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private void pushItemsOut(final EnumSet<EnumFacing> possibleDirections) {
        if (!hasItemsToPush()) {
            return;
        }

        final Iterator<ItemStack> i = this.waitingToSend.iterator();
        while (i.hasNext()) {
            ItemStack whatToSend = i.next();

            for (final EnumFacing s : possibleDirections) {
                final TileEntity te = this.getWorld().getTileEntity(this.getPos().offset(s));
                if (te == null) {
                    continue;
                }

                final InventoryAdaptor ad = InventoryAdaptor.getAdaptor(te, s.getOpposite());
                if (ad != null) {
                    final ItemStack result = ad.addItems(whatToSend);

                    if (result.isEmpty()) {
                        whatToSend = ItemStack.EMPTY;
                    } else {
                        whatToSend.setCount(whatToSend.getCount() - (whatToSend.getCount() - result.getCount()));
                    }

                    if (whatToSend.isEmpty()) {
                        break;
                    }
                }
            }

            if (whatToSend.isEmpty()) {
                i.remove();
            }
        }

        if (this.waitingToSend.isEmpty()) {
            this.waitingToSend = null;
        }

    }

    private boolean hasItemToSend() {
        for (int i = 0; i < ejectInv.getSlots(); i++) {
            ItemStack stack = ejectInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void ejectItems() {
        for (int i = 0; i < ejectInv.getSlots(); i++) {
            if (!ejectInv.getStackInSlot(i).isEmpty()) {
                ItemStack stack = ejectInv.getStackInSlot(i);
                int itemCount = stack.getCount();
                final InventoryAdaptor adaptor = getAdaptor(i);

                final ItemStack canExtract = adaptor.simulateRemove(itemCount, stack, null);

                if (canExtract != null && canExtract.getCount() == itemCount) {

                    try {
                        final IMEMonitor<IAEItemStack> inv = this.getProxy()
                                .getStorage()
                                .getInventory(
                                        AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                        final IEnergyGrid energy = this.getProxy().getEnergy();
                        final IAEItemStack aeStack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
                        if (aeStack != null) {
                            final IAEItemStack failed = Platform.poweredInsert(energy, inv, aeStack, this.source);

                            if (failed != null) {
                                itemCount -= failed.getStackSize();
                            }

                            if (itemCount != 0) {
                                final ItemStack removed = adaptor.removeItems(itemCount, ItemStack.EMPTY, null);
                                if (removed.isEmpty()) {
                                    throw new IllegalStateException("bad attempt at managing inventory. ( removeItems )");
                                } else if (removed.getCount() != itemCount) {
                                    throw new IllegalStateException("bad attempt at managing inventory. ( removeItems )");
                                }

                            }

                        }
                    } catch (GridAccessException e) {
                        // :P
                    }

                }
            }
        }
    }

    private InventoryAdaptor getAdaptor(final int slot) {
        return new AdaptorItemHandler(new RangedWrapper(this.ejectInv, slot, slot + 1));
    }

    private boolean hasWorks() {

        for (int i = 0; i < patterns.getSlots(); i++) {
            if (!patterns.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void setWorkStart(ICraftingPatternDetails pattern) {

        for (int i = 0; i < patterns.getSlots(); i++) {
            ItemStack stack = patterns.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem) {
                ICraftingPatternDetails currentPattern = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, world);
                if (currentPattern != null && currentPattern.equals(pattern)) {
                    workStarted[i] = true;
                }
            }
        }

    }

    private boolean hasWorkFinished() {

        for (int i = 0; i < patterns.getSlots(); i++) {
            ItemStack stack = patterns.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem) {
                ICraftingPatternDetails currentPattern = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, world);

                if (currentPattern != null) {
                    IAEItemStack what = currentPattern.getOutputs().length > 0 ? currentPattern.getOutputs()[0] : null;
                    if (isNotRequesting(what) && workStarted[i]) {
                        return true;
                    }
                }
            }

        }

        return false;
    }

    private void updateWorks() {

        for (int i = 0; i < patterns.getSlots(); i++) {

            IAEItemStack what = getResultStack(i);
            if (isNotRequesting(what) && workStarted[i]) {
                patterns.setStackInSlot(i, ItemStack.EMPTY);
                workStarted[i] = false;
                updateCraftingList();
            }
        }

    }

    private boolean isNotRequesting(IAEItemStack stack) {

        if (stack == null) {
            return true;
        }

        try {
            final ICraftingGrid cg = this.getProxy().getCrafting();
            final ImmutableSet<ICraftingCPU> cpuSet = cg.getCpus();

            final IItemList<IAEItemStack> pendingList = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

            for (ICraftingCPU cpu : cpuSet) {
                if (cpu instanceof CraftingCPUCluster) {
                    CraftingCPUCluster cluster = (CraftingCPUCluster) cpu;
                    cluster.getListOfItem(pendingList, CraftingItemList.PENDING);
                }
            }

            for (IAEItemStack pendingStack : pendingList) {
                if (pendingStack.isSameType(stack)) {
                    return false;
                }
            }

        } catch (GridAccessException e) {
            e.printStackTrace();
        }

        return true;
    }

    private IAEItemStack getResultStack(int pattern) {

        ItemStack stack = patterns.getStackInSlot(pattern);
        if (!stack.isEmpty() && stack.getItem() instanceof ICraftingPatternItem) {
            ICraftingPatternDetails currentPattern = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, world);
            if (currentPattern != null) {
                return currentPattern.getOutputs().length > 0 ? currentPattern.getOutputs()[0] : null;
            }
        }

        return null;
    }

    @Override
    public void gridChanged() {
        this.notifyNeighbors();
    }

    private boolean sameGrid(final IGrid grid) throws GridAccessException {
        return grid == this.getProxy().getGrid();
    }

}
