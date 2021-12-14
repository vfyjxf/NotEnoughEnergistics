package com.github.vfyjxf.nee.network.packets;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.container.me.items.PatternTermContainer;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperInvItemHandler;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

import static com.github.vfyjxf.nee.jei.PatternRecipeTransferHandler.OUTPUT_KEY;

public class PacketRecipeTransfer {

    private CompoundNBT input;
    private CompoundNBT output;
    private boolean isCraftingMode;

    public PacketRecipeTransfer() {

    }

    public PacketRecipeTransfer(CompoundNBT input, CompoundNBT output, boolean isCraftingMode) {
        this.input = input;
        this.output = output;
        this.isCraftingMode = isCraftingMode;
    }

    public CompoundNBT getInput() {
        return input;
    }

    public CompoundNBT getOutput() {
        return output;
    }

    public boolean isCraftingMode() {
        return isCraftingMode;
    }


    public void encode(PacketBuffer buffer) {
        buffer.writeNbt(this.input);
        buffer.writeBoolean(this.isCraftingMode);
        if (this.output != null) {
            buffer.writeBoolean(true);
            buffer.writeNbt(this.output);
        } else {
            buffer.writeBoolean(false);
        }

    }


    public static PacketRecipeTransfer decode(PacketBuffer buffer) {
        PacketRecipeTransfer packet = new PacketRecipeTransfer();
        packet.input = buffer.readNbt();
        packet.isCraftingMode = buffer.readBoolean();
        if (buffer.readBoolean()) {
            packet.output = buffer.readNbt();
        }
        return packet;
    }


    public static void handle(PacketRecipeTransfer packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof PatternTermContainer) {
            ctx.get().enqueueWork(() -> {
                PatternTermContainer container = (PatternTermContainer) player.containerMenu;
                container.getPatternTerminal().setCraftingRecipe(packet.isCraftingMode());
                ItemStack[] recipeInputs = new ItemStack[9];
                ItemStack[] recipeOutputs = null;
                CompoundNBT currentStack;
                for (int i = 0; i < recipeInputs.length; i++) {
                    currentStack = packet.getInput().getCompound("#" + i);
                    recipeInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : ItemStack.of(currentStack);
                }

                if (!packet.getOutput().isEmpty()) {
                    recipeOutputs = new ItemStack[3];
                    for (int i = 0; i < recipeOutputs.length; i++) {
                        currentStack = packet.getOutput().getCompound(OUTPUT_KEY + i);
                        recipeOutputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : ItemStack.of(currentStack);
                    }
                }
                final IGridNode node = ((IContainerCraftingPacket) container).getNetworkNode();

                Preconditions.checkArgument(node != null);

                final IGrid grid = node.getGrid();
                Preconditions.checkArgument(true);

                final IStorageGrid inv = grid.getCache(IStorageGrid.class);
                Preconditions.checkArgument(inv != null);

                final ISecurityGrid security = grid.getCache(ISecurityGrid.class);
                Preconditions.checkArgument(security != null);

                final IItemHandler craftMatrix = ((IContainerCraftingPacket) container).getInventoryByName("crafting");
                if (packet.getInput() != null) {
                    for (int i = 0; i < craftMatrix.getSlots(); i++) {
                        ItemStack currentItem = ItemStack.EMPTY;
                        if (recipeInputs[i] != null) {
                            currentItem = recipeInputs[i].copy();
                        }
                        ItemHandlerUtil.setStackInSlot(craftMatrix, i, currentItem);
                    }
                    if (recipeOutputs != null && !packet.isCraftingMode()) {
                        final IItemHandler outputMatrix = ((IContainerCraftingPacket) container).getInventoryByName("output");
                        for (int i = 0; i < outputMatrix.getSlots(); i++) {
                            ItemStack currentItem = ItemStack.EMPTY;
                            if (recipeOutputs[i] != null) {
                                currentItem = recipeOutputs[i].copy();
                            }
                            ItemHandlerUtil.setStackInSlot(outputMatrix, i, currentItem);
                        }
                    }
                    container.slotsChanged(new WrapperInvItemHandler(craftMatrix));
                }
            });
            ctx.get().setPacketHandled(true);

        }
    }

}
