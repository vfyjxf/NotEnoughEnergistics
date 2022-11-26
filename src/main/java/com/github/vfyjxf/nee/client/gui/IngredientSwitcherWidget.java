package com.github.vfyjxf.nee.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;
import com.github.vfyjxf.nee.client.gui.widgets.AddPreferenceButton;
import com.github.vfyjxf.nee.client.gui.widgets.ItemWidget;
import com.github.vfyjxf.nee.client.gui.widgets.OpenPreferenceDataButton;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.jei.PatternTransferHandler;
import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketSlotStackSwitch;
import com.github.vfyjxf.nee.utils.Gobals;
import com.github.vfyjxf.nee.utils.GuiUtils;
import com.github.vfyjxf.nee.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.vfyjxf.nee.jei.PatternTransferHandler.INPUT_KEY;

/**
 * A draggable widget to switch ingredient in pattern.
 * //TODO:AE2FC support
 */
public class IngredientSwitcherWidget extends Gui {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private int scrollOffset;
    private int maxScroll;
    private boolean renderingTooltip;
    private ItemWidget selectedWidget;
    private final Slot selectedSlot;
    private final Rectangle slotRect;
    private final GuiContainer screen;
    private final MEGuiTextField searchField;
    private final List<ItemStack> allStacks;
    private final List<ItemStack> viewStacks;
    private final List<ItemWidget> widgets;
    private final AddPreferenceButton addButton;
    private final OpenPreferenceDataButton opeButton;
    private final OnResultApply action;

    public IngredientSwitcherWidget(int x, int y, int width, int height, List<ItemStack> ingredients, GuiContainer screen, Slot slot, OnResultApply action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.screen = screen;
        this.selectedSlot = slot;
        this.slotRect = new Rectangle(slot.xPos + screen.guiLeft - 2, slot.yPos + screen.guiTop - 2, 20, 20);
        this.searchField = new MEGuiTextField(Minecraft.getMinecraft().fontRenderer, x + 6, y + 5, 54, 11);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF99FF99);
        this.searchField.setVisible(true);
        this.searchField.selectAll();
        this.allStacks = ingredients.stream().filter(stack -> stack != null && !stack.isEmpty()).collect(Collectors.toList());
        this.viewStacks = new ArrayList<>(allStacks);
        this.widgets = new ArrayList<>();
        this.initWidgets(viewStacks);
        this.scrollOffset = 0;
        this.addButton = new AddPreferenceButton(this, x + width - 32, y + 5);
        this.opeButton = new OpenPreferenceDataButton(x + width - 19, y + 5);
        this.action = action;
    }

    private void initWidgets(List<ItemStack> stacks) {
        this.widgets.clear();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                if (index >= stacks.size()) {
                    break;
                }
                ItemStack stack = stacks.get(index);
                ItemWidget widget = new ItemWidget(stack, x + 7 + col * 18, y + 20 + row * 18);
                this.widgets.add(widget);
            }
        }
        this.maxScroll = (this.viewStacks.size() + 3) / 4 - 4;
    }

    public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (mc == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        TextureManager textureManager = mc.getTextureManager();
        textureManager.bindTexture(new ResourceLocation(Gobals.MOD_ID, "textures/gui/ingredient_switcher_widget.png"));
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, this.width, this.height, this.width, this.height);
        this.searchField.drawTextBox();
        this.addButton.drawButton(mc, mouseX, mouseY, partialTicks);
        this.opeButton.drawButton(mc, mouseX, mouseY, partialTicks);
        drawScrollBar(textureManager);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();

        for (ItemWidget widget : this.widgets) {
            widget.drawWidget(mc, mouseX, mouseY);
        }

        drawTooltips(mc, mouseX, mouseY);
    }

    private void drawScrollBar(TextureManager textureManager) {
        if (maxScroll <= 0) return;
        int scrollBarLeft = x + width - 13;
        int scrollBarTop = y + 20;
        textureManager.bindTexture(new ResourceLocation(Gobals.MOD_ID, "textures/gui/states.png"));
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.scrollOffset == 0) {
            drawTexturedModalRect(scrollBarLeft, scrollBarTop, 4, 51, 7, 11);
        } else {
            final int offsetY = (this.scrollOffset) * 59 / maxScroll;
            drawTexturedModalRect(scrollBarLeft, offsetY + scrollBarTop, 4, 51, 7, 11);
        }
    }

    public void drawTooltips(Minecraft mc, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        this.renderingTooltip = true;
        this.addButton.drawTooltip(mc, mouseX, mouseY);
        this.opeButton.drawTooltip(mc, mouseX, mouseY);
        for (ItemWidget slot : widgets) {
            if (slot.isMouseOver(mouseX, mouseY)) {
                slot.drawTooltips(mc, screen, mouseX, mouseY);
            }
        }
        GlStateManager.enableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        this.renderingTooltip = false;
    }

    public boolean handleKeyPressed(char typedChar, int eventKey) {

        if (this.searchField.isFocused() && eventKey == Keyboard.KEY_RETURN) {
            this.searchField.setFocused(false);
            return true;
        }

        if (this.searchField.isFocused() && searchField.textboxKeyTyped(typedChar, eventKey)) {
            updateViews();
            return true;
        }
        return false;
    }

    public boolean handleMouseClicked(int eventButton, int mouseX, int mouseY) {

        this.searchField.mouseClicked(mouseX, mouseY, eventButton);

        if (this.addButton.mousePressed(this.screen.mc, mouseX, mouseY)) {
            return true;
        }
        if (this.opeButton.mousePressed(this.screen.mc, mouseX, mouseY)) {
            return true;
        }
        boolean result = false;
        for (ItemWidget widget : this.widgets) {
            widget.setSelected(false);
            if (widget.isMouseOver(mouseX, mouseY)) {
                if (eventButton == 0) {
                    return apply(widget.getIngredient());
                } else if (eventButton == 2) {
                    widget.setSelected(true);
                    this.selectedWidget = widget;
                    result = true;
                }
            }
        }
        this.addButton.update();

        return result;
    }

    public boolean mouseScroll(int offset, int mouseX, int mouseY) {
        if (offset == 0 || !isMouseOver(mouseX, mouseY)) return false;
        this.scrollOffset += Math.max(Math.min(-offset, 1), -1);
        this.scrollOffset = Math.max(Math.min(scrollOffset, this.maxScroll), 0);
        int startIndex = this.scrollOffset * 4;
        List<ItemStack> stacks = this.allStacks.subList(startIndex, viewStacks.size());
        this.initWidgets(stacks);
        return true;
    }

    public boolean apply(ItemStack stack) {
        try {
            List<Integer> craftingSlots = new ArrayList<>();
            Map<String, List<ItemStack>> switcherData = PatternTransferHandler.getSwitcherData();
            if (NEEConfig.isSyncIngredientSwitcher()) {
                for (Slot slot : getCraftingSlots(screen)) {
                    List<ItemStack> ingredients = switcherData.get(INPUT_KEY + slot.getSlotIndex());
                    List<ItemStack> selectedIngredients = switcherData.get(INPUT_KEY + selectedSlot.getSlotIndex());
                    boolean areItemStackEqual = selectedSlot.getHasStack() &&
                            slot.getHasStack() &&
                            ItemUtils.matches(selectedSlot.getStack(), slot.getStack());

                    boolean areIngredientEqual = ingredients != null &&
                            !ingredients.isEmpty() &&
                            ingredients.size() == selectedIngredients.size() &&
                            ItemUtils.matches(ItemUtils.getFirstStack(ingredients), ItemUtils.getFirstStack(selectedIngredients));

                    if (areItemStackEqual && areIngredientEqual) {
                        craftingSlots.add(slot.slotNumber);
                    }
                }
            } else {
                craftingSlots.add(selectedSlot.slotNumber);
            }
            NEENetworkHandler.getInstance().sendToServer(new PacketSlotStackSwitch(stack, craftingSlots));
            this.action.onApply();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private List<Slot> getCraftingSlots(GuiContainer gui) {
        List<Slot> craftingSlots = new ArrayList<>();
        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (GuiUtils.isCraftingSlot(slot)) {
                craftingSlots.add(slot);
            }
        }
        return craftingSlots;
    }

    public void updateViews() {
        String text = this.searchField.getText();
        this.scrollOffset = 0;
        this.selectedWidget = null;
        this.viewStacks.clear();
        boolean searchMod = text.startsWith("@");
        String searchString = searchMod ? text.substring(1) : text;
        List<ItemStack> searched = allStacks.stream().filter(stack -> {
            if (searchMod) {
                String modid = stack.getItem().getCreatorModId(stack);
                return modid != null && modid.toLowerCase().contains(searchString.toLowerCase());
            } else {
                return stack.getDisplayName().toLowerCase().contains(text);
            }
        }).collect(Collectors.toList());
        this.viewStacks.addAll(searched);
        this.initWidgets(searched);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    public boolean isMouseOverSlot(int mouseX, int mouseY) {
        return slotRect.contains(mouseX, mouseY);
    }

    public boolean isRenderingTooltip() {
        return renderingTooltip;
    }

    public ItemWidget getSelectedWidget() {
        return selectedWidget;
    }

    public void cleanSelection() {
        this.selectedWidget.setSelected(false);
        this.selectedWidget = null;
    }

    public interface OnResultApply {
        void onApply();
    }

}
