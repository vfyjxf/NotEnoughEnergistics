package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.client.gui.IngredientSwitcherWidget;
import com.github.vfyjxf.nee.client.gui.widgets.MergeConfigButton;
import com.github.vfyjxf.nee.config.IngredientMergeMode;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.helper.CraftingHelper;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.jei.PatternTransferHandler;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.MouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

import static com.github.vfyjxf.nee.jei.PatternTransferHandler.INPUT_KEY;

public class GuiEventHandler {

    private static final GuiEventHandler INSTANCE = new GuiEventHandler();

    public static GuiEventHandler getInstance() {
        return INSTANCE;
    }

    private GuiEventHandler() {

    }

    private MergeConfigButton buttonCombination;
    private IngredientSwitcherWidget switcherWidget;

    @SubscribeEvent
    public void beforeScreenInit(GuiScreenEvent.InitGuiEvent.Pre event) {
        GuiScreen screen = event.getGui();
        if (screen instanceof RecipesGui) {
            RecipesGui recipesGui = (RecipesGui) screen;
            GuiScreen parent = recipesGui.getParentScreen();
            if (CraftingHelper.isSupportedGui(parent)) {
                RecipeAnalyzer.setCleanCache(true);
            }
        } else {
            RecipeAnalyzer.setCleanCache(false);
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (handleMouseInput(mc.currentScreen)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        char typedChar = Keyboard.getEventCharacter();
        int eventKey = Keyboard.getEventKey();
        event.setCanceled(((eventKey == 0 && typedChar >= 32) || Keyboard.getEventKeyState())
                && handleKeyInput(typedChar, eventKey, event.getGui()));
    }

    private boolean handleMouseInput(GuiScreen screen) {
        if (handleSwitcherMouseInput(screen)) {
            return true;
        }
        return false;
    }

    private boolean handleKeyInput(char typedChar, int eventKey, GuiScreen screen) {
        if (handleSwitcherKeyInput(typedChar, eventKey)) {
            return true;
        }
        return false;
    }

    private boolean handleSwitcherMouseInput(GuiScreen screen) {
        if (screen instanceof GuiPatternTerm) {
            GuiPatternTerm patternTerm = (GuiPatternTerm) screen;
            if (GuiScreen.isShiftKeyDown() && Mouse.getEventButton() == 2) {
                Slot slot = patternTerm.getSlotUnderMouse();
                if (slot instanceof SlotFake && slot.getHasStack()) {
                    List<ItemStack> ingredients = PatternTransferHandler.ingredients.get(INPUT_KEY + slot.getSlotIndex());
                    if (ingredients != null) {
                        this.switcherWidget = new IngredientSwitcherWidget(
                                slot.xPos + patternTerm.guiLeft +18 ,
                                slot.yPos,
                                94,
                                97,
                                ingredients,
                                patternTerm,
                                slot,
                                () -> this.switcherWidget = null);
                        return true;
                    }
                }
            }
        }
        Minecraft minecraft = screen.mc;
        if (minecraft != null) {
            int mouseX = MouseHelper.getX();
            int mouseY = MouseHelper.getY();
            int eventButton = Mouse.getEventButton();
            if (eventButton > -1 && Mouse.getEventButtonState()) {
                if (switcherWidget != null) {
                    return switcherWidget.handleMouseClicked(eventButton, mouseX, mouseY);
                }
            } else if (Mouse.getEventDWheel() != 0) {

            }
        }
        return false;
    }

    private boolean handleSwitcherKeyInput(char typedChar, int eventKey) {
        return switcherWidget != null && switcherWidget.handleKeyPressed(typedChar, eventKey);
    }


    /**
     * Cancel other tooltip rendering when Switcher renders Tooltip.
     */
    @SubscribeEvent
    public void onTooltipRender(RenderTooltipEvent.Pre event) {
        if (switcherWidget != null) {
            event.setCanceled(!switcherWidget.isRenderingTooltip());
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiPatternTerm) {
            GuiPatternTerm gui = (GuiPatternTerm) event.getGui();
            event.getButtonList().add(buttonCombination = new MergeConfigButton(gui.guiLeft + 84, gui.guiTop + gui.ySize - 163, NEEConfig.getMergeMode()));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.getButton() instanceof MergeConfigButton) {
            MergeConfigButton button = (MergeConfigButton) event.getButton();
            int ordinal = Mouse.getEventButton() != 2 ? button.getMergeMode().ordinal() + 1 : button.getMergeMode().ordinal() - 1;

            if (ordinal >= IngredientMergeMode.values().length) {
                ordinal = 0;
            }
            if (ordinal < 0) {
                ordinal = IngredientMergeMode.values().length - 1;
            }
            button.setMode(IngredientMergeMode.values()[ordinal]);
            NEEConfig.setMergeMode(IngredientMergeMode.values()[ordinal]);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiPatternTerm) {
            ContainerPatternTerm container = (ContainerPatternTerm) ((GuiPatternTerm) event.getGui()).inventorySlots;
            if (container.isCraftingMode()) {
                buttonCombination.enabled = false;
                buttonCombination.visible = false;
            } else {
                buttonCombination.enabled = true;
                buttonCombination.visible = true;
            }
            if (switcherWidget != null) {
                int mouseX = event.getMouseX();
                int mouseY = event.getMouseY();
                Minecraft mc = event.getGui().mc;
                if (switcherWidget.isMouseOverSlot(mouseX, mouseY) || switcherWidget.isMouseOver(mouseX, mouseY)) {
                    switcherWidget.draw(mc, event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
                } else {
                    switcherWidget = null;
                }
            }
        } else {
            switcherWidget = null;
        }

    }

}
