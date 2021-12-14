package com.github.vfyjxf.nee.config;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;


@Mod.EventBusSubscriber(modid = NotEnoughEnergistics.MODID, bus = Bus.FORGE)
public final class NEEConfig {

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT_CONFIG;

    static {
        Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT_CONFIG = specPair.getLeft();
    }

    public final static class ClientConfig {

        private final ForgeConfigSpec.BooleanValue allowPrintRecipeType;
        private final ForgeConfigSpec.BooleanValue allowSynchronousSwitchIngredient;
        private final ForgeConfigSpec.BooleanValue matchOtherItems;
        private final ForgeConfigSpec.BooleanValue useDisplayedIngredient;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> listModPreference;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> itemBlacklist;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> listItemPreference;

        public ClientConfig(ForgeConfigSpec.Builder builder) {

            builder.push("transfer");

            allowPrintRecipeType = builder.comment("If true, print current recipe type in log.Default:false")
                    .define("PrintRecipeType", true);

            useDisplayedIngredient = builder.comment("If true, the ingredient currently displayed by JEI will be transferred")
                    .define("UseDisplayedIngredient", true);

            listModPreference = builder.comment("If tag has this mod's item, use it first.")
                    .define("ModPreference", Collections.emptyList());

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
            builder.pop();

            builder.push("crafting-help");

            matchOtherItems = builder.comment("If true, Crafting Helper will match other items even they can't auto-crafting")
                    .define("MatchOtherItems", true);

            allowSynchronousSwitchIngredient = builder.comment("If true, it will make all similar ingredient switch at the same time ")
                    .define("AllowSynchronousSwitchIngredient", true);

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
            if (CLIENT_SPEC.isLoaded()) {
                listModPreference.set(list);
                listModPreference.save();
            }
        }

        public void setItemBlacklist(List<? extends String> blacklist) {
            if (CLIENT_SPEC.isLoaded()) {
                itemBlacklist.set(blacklist);
                listModPreference.save();
            }
        }

        public void setListItemPreference(List<? extends String> list) {
            if (CLIENT_SPEC.isLoaded()) {
                listItemPreference.set(list);
                listModPreference.save();
            }
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
                    if (!recipeType.isEmpty() && ResourceLocation.isValidResourceLocation(recipeType)) {
                        NotEnoughEnergistics.logger.error("Can't find recipeType : " + recipeType);
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

    }

}

