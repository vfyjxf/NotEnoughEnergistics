package com.github.vfyjxf.nee.processor;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import cpw.mods.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;

public class RecipeProcessor {
    public static List<IRecipeProcessor> recipeProcessors = new ArrayList<>();

    public static void init() {
        NotEnoughEnergistics.logger.info("-----Not Enough Energistics Init Start-----");
        NotEnoughEnergistics.logger.info("Install Vanilla Support");
        recipeProcessors.add(new VanillaRecipeProcessor());

        if (Loader.isModLoaded("gregtech")) {
            NotEnoughEnergistics.logger.info("Found GregTech,install GregTech support");
            recipeProcessors.add(new GregTechRecipeProcessor());
        }
        if (Loader.isModLoaded("IC2")) {
            NotEnoughEnergistics.logger.info("Found IC2,install IC2 Support");
            recipeProcessors.add(new ICRecipeProcessor());
        }
        if (Loader.isModLoaded("Avaritia")) {
            NotEnoughEnergistics.logger.info("Found Avaritia, install Avaritia support");
            recipeProcessors.add(new AvaritiaRecipeProcessor());
        }
        if(Loader.isModLoaded("EnderIO")){
            NotEnoughEnergistics.logger.info("Found EnderIO, install EnderIO support");
            recipeProcessors.add(new EnderIORecipeProcessor());
        }
        if(Loader.isModLoaded("Forestry")){
            NotEnoughEnergistics.logger.info("Found Forestry, install Forestry support");
            recipeProcessors.add(new ForestryRecipeProcessor());
        }
        if (Loader.isModLoaded("appliedenergistics2")) {
            NotEnoughEnergistics.logger.info("Applied Energistics 2 will not be supported");
        }
        NotEnoughEnergistics.logger.info("-----Not Enough Energistics Init  Finished-----");
    }

}
