package com.github.vfyjxf.nee;

import com.github.vfyjxf.nee.config.NEEConfig;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NEECommands extends CommandBase {
    @Override
    public String getName() {
        return "nee";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 1;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "add", "reload");
        } else if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "blacklist", "priorityItem", "priorityMod");
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if ("add".equalsIgnoreCase(args[0]) && args.length > 1) {
                if ("blacklist".equalsIgnoreCase(args[1]) || "priorityItem".equalsIgnoreCase(args[1])) {
                    ItemStack currentStack = Minecraft.getMinecraft().player.getHeldItemMainhand();
                    if (!currentStack.isEmpty()) {
                        String itemJsonString = ItemUtils.toItemJsonString(currentStack);
                        if (args.length == 3) {
                            JsonObject itemJsonObject = new JsonParser().parse(itemJsonString).getAsJsonObject();
                            String recipeType = args[2];
                            itemJsonObject.addProperty("recipeType", recipeType);
                            itemJsonString = new Gson().toJson(itemJsonObject);
                        }
                        String[] itemList = "blacklist".equalsIgnoreCase(args[1]) ? NEEConfig.itemBlacklist : NEEConfig.itemPriorityList;
                        String name = "blacklist".equalsIgnoreCase(args[1]) ? "itemBlacklist" : "itemPriorityList";
                        List<String> newItemList = new ArrayList<>(Arrays.asList(itemList));
                        for (String currentJsonString : itemList) {
                            if (currentJsonString.equals(itemJsonString)) {
                                return;
                            }
                        }

                        newItemList.add(itemJsonString);
                        for (IConfigElement configElement : ConfigElement.from(NEEConfig.class).getChildElements()) {
                            if (configElement.getName().equals(name)) {
                                configElement.set(newItemList.toArray());
                            }
                        }
                        Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), "config/NotEnoughEnergistics.cfg"));
                        config.save();
                        ConfigManager.sync(NotEnoughEnergistics.MODID, Config.Type.INSTANCE);
                    }
                } else if ("priorityMod".equalsIgnoreCase(args[1]) && args.length == 3) {
                    String modid = args[2];
                    if (!ItemUtils.hasModId(modid)) {
                        for (IConfigElement configElement : ConfigElement.from(NEEConfig.class).getChildElements()) {
                            if ("modPriorityList".equals(configElement.getName())) {
                                List<String> newModIDList = new ArrayList<>(Arrays.asList(NEEConfig.modPriorityList));
                                newModIDList.add(modid);
                                configElement.set(newModIDList.toArray());
                                Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), "config/NotEnoughEnergistics.cfg"));
                                config.save();
                                ConfigManager.sync(NotEnoughEnergistics.MODID, Config.Type.INSTANCE);
                            }
                        }
                    }

                }
            } else if ("reload".equalsIgnoreCase(args[0])) {
                ConfigManager.sync(NotEnoughEnergistics.MODID, Config.Type.INSTANCE);
            }
        }
    }

}
