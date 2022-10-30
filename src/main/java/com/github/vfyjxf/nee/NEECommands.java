package com.github.vfyjxf.nee;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class NEECommands extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "nee";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 1;
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "add", "reload","help");
        } else if (args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "blacklist", "priorityItem", "priorityMod", "itemCombinationWhitelist");
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        /*
        if (args.length > 0) {
            if("help".equalsIgnoreCase(args[0])){
                sender.sendMessage(new TextComponentString("Usage:"));
                sender.sendMessage(new TextComponentString("/nee add blacklist/priorityItem processor_id identifier"));
                sender.sendMessage(new TextComponentString("/nee add blacklist/priorityItem/itemCombination identifier"));
            }
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
                        List<String> newItemList = new ArrayList<>(Arrays.asList(itemList));
                        for (String currentJsonString : itemList) {
                            if (currentJsonString.equals(itemJsonString)) {
                                return;
                            }
                        }
                        newItemList.add(itemJsonString);
                        if ("blacklist".equalsIgnoreCase(args[1])) {
                            NEEConfig.setItemBlacklist(newItemList.toArray(new String[0]));
                        } else {
                            NEEConfig.setItemPriorityList(newItemList.toArray(new String[0]));
                        }
                    }
                } else if ("priorityMod".equalsIgnoreCase(args[1]) && args.length == 3) {
                    String modid = args[2];
                    if (!ItemUtils.hasModId(modid)) {
                        List<String> newModIDList = new ArrayList<>(Arrays.asList(NEEConfig.modPriorityList));
                        newModIDList.add(modid);
                        NEEConfig.setPriorityMods(newModIDList.toArray(new String[0]));
                    }
                } else if ("itemCombinationWhitelist".equalsIgnoreCase(args[1]) && args.length == 3) {
                    String recipeType = args[2];
                    if (!Arrays.asList(NEEConfig.itemCombinationWhitelist).contains(recipeType)) {
                        List<String> newLists = new ArrayList<>(Arrays.asList(NEEConfig.itemCombinationWhitelist));
                        newLists.add(recipeType);
                        NEEConfig.setMergeBlacklist(newLists.toArray(new String[0]));
                    }
                }
            } else if ("reload".equalsIgnoreCase(args[0])) {
                ConfigManager.sync(NotEnoughEnergistics.MOD_ID, Config.Type.INSTANCE);
            }
        }

         */
    }

}
