package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.utils.ItemUtils;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;


public final class NEEConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT_CONFIG;


    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT_CONFIG = specPair.getLeft();
    }

    public final static class ClientConfig {

        private static ForgeConfigSpec.BooleanValue allowPrintRecipeType;
        private static ForgeConfigSpec.BooleanValue allowSynchronousSwitchIngredient;
        private static ForgeConfigSpec.BooleanValue matchOtherItems;
        private static ForgeConfigSpec.BooleanValue useDisplayedIngredient;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> listModPreference;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> itemBlacklist;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> listItemPreference;


        public ClientConfig(ForgeConfigSpec.Builder builder) {

            builder.push("transfer");
            {
                allowPrintRecipeType = builder.comment("If true, print current recipe type in log.Default:false")
                        .define("PrintRecipeType", false);

                useDisplayedIngredient = builder.comment("If true, the ingredient currently displayed by JEI will be transferred")
                        .define("UseDisplayedIngredient", true);

                listModPreference = builder.comment("If tag has this mod's item, use it first.")
                        .defineList("ModPreference",
                                Collections.emptyList(),
                                obj -> obj instanceof String);

                itemBlacklist = builder.comment("If item in the blacklist, it will not be transferred.")
                        .comment("example:{\\\"itemName\\\" : \\\"minecraft:apple\\\", \\\"nbt\\\": \\\"{}\\\"}")
                        .defineList("ItemBlacklist",
                                Collections.emptyList(),
                                obj -> obj instanceof String && isCorrectItemJson((String) obj));

                listItemPreference = builder.comment("If item in the blacklist, it will not be transferred.")
                        .comment("example:{\\\"itemName\\\" : \\\"minecraft:apple\\\", \\\"nbt\\\": \\\"{}\\\"}")
                        .defineList("ListItemPreference",
                                Collections.emptyList(),
                                obj -> obj instanceof String && isCorrectItemJson((String) obj));
            }
            builder.pop();

            builder.push("crafting-help");
            {
                matchOtherItems = builder.comment("If true, Crafting Helper will match other items even they can't auto-crafting")
                        .define("MatchOtherItems", true);

                allowSynchronousSwitchIngredient = builder.comment("If true, it will make all similar ingredient switch at the same time ")
                        .define("AllowSynchronousSwitchIngredient", true);
            }
            builder.pop();

        }

        public boolean allowPrintRecipeType() {
            return allowPrintRecipeType.get();
        }

        public boolean allowSynchronousSwitchIngredient() {
            return allowSynchronousSwitchIngredient.get();
        }

        public boolean getMatchOtherItems() {
            return matchOtherItems.get();
        }

        public boolean useDisplayedIngredient() {
            return useDisplayedIngredient.get();
        }

        public List<? extends String> getListModPreference() {
            return listModPreference.get();
        }

        public List<? extends String> getItemBlacklist() {
            return itemBlacklist.get();
        }

        public List<? extends String> getListItemPreference() {
            return listItemPreference.get();
        }

        public void setListModPreference(List<? extends String> list) {
            listModPreference.set(list);
        }

        public void setItemBlacklist(List<? extends String> blacklist) {
            itemBlacklist.set(blacklist);
        }

        public void setListItemPreference(List<? extends String> list) {
            listItemPreference.set(list);
        }

        private boolean isCorrectItemJson(String itemJsonString) {
            JsonObject jsonObject;
            try {
                jsonObject = new JsonParser().parse(itemJsonString).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                NotEnoughEnergistics.logger.error("Found a error nbt json in item json: " + itemJsonString);
                return false;
            }
            if (jsonObject != null) {
                String itemName = jsonObject.get("itemName").getAsString();
                if (Strings.isNullOrEmpty(itemName)) {
                    return false;
                }
                String nbtJsonString = itemJsonString.contains("nbt") ? jsonObject.get("nbt").getAsString() : "";
                if (!nbtJsonString.isEmpty()) {
                    try {
                        new JsonParser().parse(nbtJsonString);
                    } catch (JsonSyntaxException e) {
                        NotEnoughEnergistics.logger.error("Found a error nbt json in item json: " + itemJsonString);
                        return false;
                    }
                    String recipeType = itemJsonString.contains("recipeType") ? jsonObject.get("recipeType").getAsString() : "";
                    if (!recipeType.isEmpty() && !ResourceLocation.isValidResourceLocation(recipeType)) {
                        NotEnoughEnergistics.logger.error("Can't find recipeType : " + recipeType);
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading configEvent) {
        if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            ItemUtils.reloadList();
        }
    }

}

