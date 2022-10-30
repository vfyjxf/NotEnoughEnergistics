package com.github.vfyjxf.nee.helper;

import com.github.vfyjxf.nee.network.NEENetworkHandler;
import com.github.vfyjxf.nee.network.packet.PacketCraftingRequest;
import com.github.vfyjxf.nee.utils.IngredientStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class IngredientRequester {

    private final static Logger LOGGER = LogManager.getLogger();
    private final static IngredientRequester INSTANCE = new IngredientRequester();

    private boolean isWireless;
    private boolean nonPreview;
    private List<RecipeAnalyzer.RecipeIngredient> requested;
    private int currentIndex;
    private boolean finished;

    private IngredientRequester() {

    }

    public static IngredientRequester getInstance() {
        return INSTANCE;
    }

    public boolean requestNext() {
        if (finished) return false;
        RecipeAnalyzer.RecipeIngredient ingredient = requested.get(currentIndex);
        try {
            NEENetworkHandler.getInstance().sendToServer(new PacketCraftingRequest(ingredient.createAeStack(), nonPreview));
            currentIndex++;
        } catch (Exception e) {
            LOGGER.error("Fail to request ingredient: {} ,try to request next ingredient.", ingredient.getIdentifier().getDisplayName());
            currentIndex++;
            return requestNext();
        }
        finished = currentIndex >= requested.size();
        return true;
    }

    public void setRequested(boolean isWireless, boolean nonPreview, List<RecipeAnalyzer.RecipeIngredient> ingredients) {
        this.isWireless = isWireless;
        this.nonPreview = nonPreview;
        this.requested = ingredients.stream()
                .filter(ingredient -> ingredient.getStatus() == IngredientStatus.CRAFTABLE)
                .collect(Collectors.toList());
        this.currentIndex = 0;
        this.finished = false;
    }

}
