package com.github.vfyjxf.nee.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;
import com.github.vfyjxf.nee.helper.RecipeHelper;
import com.github.vfyjxf.nee.jei.PatternTransferHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.PlayerAction;
import com.github.vfyjxf.nee.network.packet.PacketPlayerAction;
import com.github.vfyjxf.nee.utils.ItemUtils;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;
import java.util.function.Supplier;

import static com.github.vfyjxf.nee.utils.Globals.INPUT_KEY_HEAD;

public class QuickPusherWidget extends Gui implements IInnerWidget {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final GuiContainer screen;
    private final MEGuiTextField searchField;
    private final Map<String, PatternGroup> allGroups = new HashMap<>();
    private final Map<String, PatternGroup> recommend = new HashMap<>();
    private final Supplier<Boolean> craftingMode;
    private PatternGroup focusedGroup;
    private int scrollOffset;
    private int maxScroll;

    public QuickPusherWidget(int x, int y, int width, int height, GuiContainer screen, Supplier<Boolean> craftingMode) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.screen = screen;
        this.craftingMode = craftingMode;
        searchField = null;
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void drawTooltips(Minecraft mc, int mouseX, int mouseY) {

    }

    @Override
    public boolean handleKeyPressed(char typedChar, int eventKey) {
        return false;
    }

    @Override
    public boolean handleMouseClicked(int eventButton, int mouseX, int mouseY) {
        return false;
    }

    public void syncFromServer(NBTTagCompound tag) {
        allGroups.clear();
        recommend.clear();
        for (String uid : tag.getKeySet()) {
            NBTTagCompound faceTag = tag.getCompoundTag(uid);
            String name = faceTag.getString("name");
            PatternGroup group = allGroups.computeIfAbsent(name, PatternGroup::new);
            NBTTagCompound patternTag = faceTag.getCompoundTag("pattern");
            ItemStack[] patterns = new ItemStack[patternTag.getInteger("size")];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = ItemUtils.fromTagOrEmpty(patternTag.getCompoundTag(INPUT_KEY_HEAD + i));
            }
            ItemStack identity = ItemUtils.fromTagOrEmpty(faceTag.getCompoundTag("identity"));
            ItemStack target = ItemUtils.fromTagOrEmpty(faceTag.getCompoundTag("target"));
            ClientPatternProvider provider = new ClientPatternProvider(uid, name, patterns, identity, target);
            group.getProviders().add(provider);
            if (isRecommend(provider)) {
                recommend.computeIfAbsent(name, PatternGroup::new).getProviders().add(provider);
            }
        }
    }

    private boolean isRecommend(ClientPatternProvider provider) {
        if (craftingMode.get()) {
            return RecipeHelper.checkCatalysts(provider.getTarget(), VanillaRecipeCategoryUid.CRAFTING);
        } else {
            return RecipeHelper.checkCatalysts(provider.getTarget(), PatternTransferHandler.getLastRecipeType());
        }
    }

    public static class PatternGroup extends Gui implements IInnerWidget {
        /**
         * The name of the interfaces.
         */
        private final String groupName;

        private final List<ClientPatternProvider> providers;
        private boolean focused;

        public PatternGroup(String groupName) {
            this.groupName = groupName;
            this.providers = new ArrayList<>();
        }


        public String getGroupName() {
            return groupName;
        }

        public List<ClientPatternProvider> getProviders() {
            return providers;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        }

        @Override
        public void drawTooltips(Minecraft mc, int mouseX, int mouseY) {

        }

        @Override
        public boolean handleKeyPressed(char typedChar, int eventKey) {
            return false;
        }

        @Override
        public boolean handleMouseClicked(int eventButton, int mouseX, int mouseY) {
            return false;
        }
    }

    public static class ClientPatternProvider {
        /**
         * Used to control pattern from client to server.
         */
        private final String uid;
        private final String name;
        private final ItemStack identifier;
        private final ItemStack target;
        private final ItemStack[] patterns;

        public ClientPatternProvider(String uid, String name, ItemStack[] patterns, ItemStack identifier, ItemStack target) {
            this.uid = uid;
            this.name = name;
            this.identifier = identifier;
            this.target = target;
            this.patterns = patterns;
        }

        public String getName() {
            return name;
        }

        public ItemStack getIdentifier() {
            return identifier;
        }

        public ItemStack getTarget() {
            return target;
        }

        public ItemStack[] getPatterns() {
            return patterns;
        }

        public void pickPattern(int slot) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("uid", uid);
            data.setInteger("slot", slot);
            NEENetworkHandler.getInstance().sendToServer(new PacketPlayerAction(PlayerAction.PICK, data));
        }

        public void putPattern(ItemStack pattern, int slot) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("uid", uid);
            data.setInteger("slot", slot);
            data.setTag("pattern", pattern.serializeNBT());
            NEENetworkHandler.getInstance().sendToServer(new PacketPlayerAction(PlayerAction.PUT, data));
        }

        public void dropPattern(int slot) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("uid", uid);
            data.setInteger("slot", slot);
            NEENetworkHandler.getInstance().sendToServer(new PacketPlayerAction(PlayerAction.DROP, data));
        }

        public boolean isFull() {
            return Arrays.stream(patterns).noneMatch(Objects::nonNull);
        }

    }

}
