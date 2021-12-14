package com.github.vfyjxf.nee;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.vfyjxf.nee.config.NEEConfig.CLIENT_CONFIG;
import static net.minecraft.util.text.TextFormatting.*;
import static net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class NEECommands {

    private static CommandDispatcher<ISuggestionProvider> dispatcher;
    private static LiteralArgumentBuilder<ISuggestionProvider> builder;


    public static void register() {
        builder = literal("nee")
                .requires(commandSource -> commandSource.hasPermission(0))
                .then(literal("add")
                        .then(literal("mod_preference")
                                .then(argument("modid", StringArgumentType.string())
                                        .executes(context -> addOrRemoveModPreference(StringArgumentType.getString(context, "modid"), true))))
                        .then(literal("item_preference")
                                .then(argument("recipe_type", StringArgumentType.greedyString())
                                        .executes(context -> addOrRemoveItemPreference(StringArgumentType.getString(context, "recipe_type"), true)))
                                .executes(context -> addOrRemoveItemPreference(true)))
                        .then(literal("blacklist")
                                .then(argument("recipe_type", StringArgumentType.greedyString())
                                        .executes(context -> addOrRemoveBlacklist(StringArgumentType.getString(context, "recipe_type"), true)))
                                .executes(context -> addOrRemoveBlacklist(true))))
                .then(literal("remove")
                        .then(literal("mod_preference")
                                .then(argument("modid", StringArgumentType.string())
                                        .executes(context -> addOrRemoveModPreference(StringArgumentType.getString(context, "modid"), false))))
                        .then(literal("item_preference")
                                .then(argument("recipe_type", StringArgumentType.greedyString())
                                        .executes(context -> addOrRemoveItemPreference(StringArgumentType.getString(context, "recipe_type"), false)))
                                .executes(context -> addOrRemoveItemPreference(false)))
                        .then(literal("blacklist")
                                .then(argument("recipe_type", StringArgumentType.greedyString())
                                        .executes(context -> addOrRemoveBlacklist(StringArgumentType.getString(context, "recipe_type"), false)))
                                .executes(context -> addOrRemoveBlacklist(false)))
                );
        dispatcher = new CommandDispatcher<>();
        dispatcher.register(builder);
    }

    private static LiteralArgumentBuilder<ISuggestionProvider> literal(String s) {
        return LiteralArgumentBuilder.literal(s);
    }

    private static <T> RequiredArgumentBuilder<ISuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    private static int addOrRemoveModPreference(String modid, boolean doAdd) {
        if(NEEConfig.CLIENT_SPEC.isLoaded()) {
            List<String> modPreferences = new ArrayList<>(CLIENT_CONFIG.getListModPreference());
            if (doAdd) {
                if (!modPreferences.contains(modid)) {
                    modPreferences.add(modid);
                }
            } else {
                modPreferences.remove(modid);
            }
            CLIENT_CONFIG.setListModPreference(modPreferences);
        }
        return 1;
    }

    private static int addOrRemoveItemPreference(boolean doAdd) {
        if(NEEConfig.CLIENT_SPEC.isLoaded()) {
            List<String> itemPreferences = new ArrayList<>(CLIENT_CONFIG.getListItemPreference());
            ItemStack currentStack = getPlayer().getMainHandItem();
            if (!currentStack.isEmpty()) {
                String itemJsonString = ItemUtils.toItemJsonString(currentStack);
                if (doAdd) {
                    if (!itemPreferences.contains(itemJsonString)) {
                        itemPreferences.add(itemJsonString);

                    }
                } else {
                    itemPreferences.remove(itemJsonString);
                }
                CLIENT_CONFIG.setListItemPreference(itemPreferences);
            }
        }
        return 1;
    }

    private static int addOrRemoveItemPreference(String recipeType, boolean doAdd) {
        if(NEEConfig.CLIENT_SPEC.isLoaded()) {
            List<String> itemPreferences = new ArrayList<>(CLIENT_CONFIG.getListItemPreference());
            ItemStack currentStack = getPlayer().getMainHandItem();
            if (!currentStack.isEmpty()) {
                JsonObject itemJsonObject = new JsonParser().parse(ItemUtils.toItemJsonString(currentStack)).getAsJsonObject();
                itemJsonObject.addProperty("recipeType", recipeType);
                String itemJsonString = new Gson().toJson(itemJsonObject);
                if (doAdd) {
                    if (!itemPreferences.contains(itemJsonString)) {
                        itemPreferences.add(itemJsonString);
                    }
                } else {
                    itemPreferences.remove(itemJsonString);
                }
                CLIENT_CONFIG.setListItemPreference(itemPreferences);
            }
        }
        return 1;
    }

    private static int addOrRemoveBlacklist(boolean doAdd) {
        if(NEEConfig.CLIENT_SPEC.isLoaded()) {
            List<String> blacklist = new ArrayList<>(CLIENT_CONFIG.getItemBlacklist());
            ItemStack currentStack = getPlayer().getMainHandItem();
            if (!currentStack.isEmpty()) {
                String itemJsonString = ItemUtils.toItemJsonString(currentStack);
                if (doAdd) {
                    if (!blacklist.contains(itemJsonString)) {
                        blacklist.add(ItemUtils.toItemJsonString(currentStack));
                    }
                } else {
                    blacklist.remove(itemJsonString);
                }
                CLIENT_CONFIG.setItemBlacklist(blacklist);
            }
        }
        return 1;
    }

    private static int addOrRemoveBlacklist(String recipeType, boolean doAdd) {
        if(NEEConfig.CLIENT_SPEC.isLoaded()) {
            List<String> blacklist = new ArrayList<>(CLIENT_CONFIG.getListItemPreference());
            ItemStack currentStack = getPlayer().getMainHandItem();
            if (!currentStack.isEmpty()) {
                JsonObject itemJsonObject = new JsonParser().parse(ItemUtils.toItemJsonString(currentStack)).getAsJsonObject();
                itemJsonObject.addProperty("recipeType", recipeType);
                String itemJsonString = new Gson().toJson(itemJsonObject);
                if (doAdd) {
                    if (!blacklist.contains(itemJsonString)) {
                        blacklist.add(itemJsonString);
                    }
                } else {
                    blacklist.remove(itemJsonString);
                }
                CLIENT_CONFIG.setItemBlacklist(blacklist);
            }
        }
        return 1;
    }


    /**
     * copied and modified from JustEnoughCharacters
     */
    @SubscribeEvent
    public static void onOpenGui(GuiScreenEvent.InitGuiEvent event) {
        if (event.getGui() instanceof ChatScreen) {
            RootCommandNode<ISuggestionProvider> root = getPlayer().connection.getCommands().getRoot();
            if (root.getChild("nee") == null) {
                root.addChild(builder.build());
            }
        }
    }


    /**
     * copied and modified from JustEnoughCharacters
     */
    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onCommand(ClientChatEvent event) {
        CommandSource commandSource = getPlayer().createCommandSourceStack();
        String message = event.getMessage();
        if (message.startsWith("/nee ") || "/nee".equals(message)) {
            event.setCanceled(true);
            Minecraft.getInstance().gui.getChat().addRecentChat(message);

            try {
                StringReader stringreader = new StringReader(message);
                if (stringreader.canRead() && stringreader.peek() == '/') {
                    stringreader.skip();
                }
                ParseResults<ISuggestionProvider> parse = dispatcher.parse(stringreader, commandSource);
                dispatcher.execute(parse);
            } catch (CommandSyntaxException e) {
                // copied and modified from net.minecraft.command.Commands
                commandSource.sendFailure(TextComponentUtils.fromMessage(e.getRawMessage()));
                if (e.getInput() != null && e.getCursor() >= 0) {
                    int k = Math.min(e.getInput().length(), e.getCursor());
                    StringTextComponent tc1 = new StringTextComponent("");
                    tc1.withStyle(GRAY).withStyle(i ->
                            i.withClickEvent(new ClickEvent(SUGGEST_COMMAND, event.getMessage())));
                    if (k > 10) {
                        tc1.append("...");
                    }
                    tc1.append(e.getInput().substring(Math.max(0, k - 10), k));
                    if (k < e.getInput().length()) {
                        ITextComponent tc2 = (new StringTextComponent(e.getInput().substring(k)))
                                .withStyle(RED, UNDERLINE);
                        tc1.getSiblings().add(tc2);
                    }
                    tc1.getSiblings().add((new TranslationTextComponent("command.context.here"))
                            .withStyle(RED, ITALIC));
                    commandSource.sendFailure(tc1);
                }
            }
        }
    }

    private static ClientPlayerEntity getPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

}
