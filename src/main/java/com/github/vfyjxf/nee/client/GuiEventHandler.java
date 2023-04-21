package com.github.vfyjxf.nee.client;

import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.container.slot.SlotFake;
import com.github.vfyjxf.nee.client.gui.IngredientSwitcherWidget;
import com.github.vfyjxf.nee.client.gui.widgets.MergeConfigButton;
import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.helper.CraftingHelper;
import com.github.vfyjxf.nee.helper.RecipeAnalyzer;
import com.github.vfyjxf.nee.jei.PatternTransferHandler;
import com.github.vfyjxf.nee.utils.ItemUtils;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.MouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Method;
import java.util.List;

import static com.github.vfyjxf.nee.utils.Globals.INPUT_KEY_HEAD;

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
                    List<ItemStack> ingredients = PatternTransferHandler.getSwitcherData().get(INPUT_KEY_HEAD + slot.getSlotIndex());
                    if (ingredients != null) {
                        ItemStack slotStack = slot.getStack();
                        boolean findAny = ingredients.stream().anyMatch(itemStack -> ItemUtils.matches(slotStack, itemStack));
                        if (findAny) {
                            this.switcherWidget = new IngredientSwitcherWidget(
                                    slot.xPos + patternTerm.getGuiLeft() + 18,
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
                return handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
            }
        }
        return false;
    }

    private boolean handleSwitcherKeyInput(char typedChar, int eventKey) {
        return switcherWidget != null && switcherWidget.handleKeyPressed(typedChar, eventKey);
    }

    private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
        if (switcherWidget != null) {
            return switcherWidget.mouseScroll(dWheel, mouseX, mouseY);
        }
        return false;
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
            event.getButtonList().add(buttonCombination = new MergeConfigButton(gui.getGuiLeft() + 84, gui.getGuiTop() + gui.getYSize() - 163, NEEConfig.getMergeMode()));
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiPatternTerm) {
            try {
                //ContainerWirelessPatternTerminal;
                //ContainerPatternTerm;
                Container container = ((GuiPatternTerm) event.getGui()).inventorySlots;
                Method saveMethod = container.getClass().getMethod("isCraftingMode");
                if ((boolean) saveMethod.invoke(container)) {
                    buttonCombination.enabled = false;
                    buttonCombination.visible = false;
                } else {
                    buttonCombination.enabled = true;
                    buttonCombination.visible = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
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
