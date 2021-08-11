package com.github.vfyjxf.nee;

import com.github.vfyjxf.nee.processor.IRecipeProcessor;
import com.github.vfyjxf.nee.processor.RecipeProcessor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import java.util.List;

import static com.github.vfyjxf.nee.NotEnoughEnergistics.logger;

public class NEECommands extends CommandBase {
    @Override
    public String getCommandName() {
        return "nee";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "use /nee RecipeProcessor to get RecipeProcessor and identifier,see them in log";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 0) {
            if ("RecipeProcessor".equals(args[0])) {
                for (IRecipeProcessor processor : RecipeProcessor.recipeProcessors) {
                    logger.info("RecipeProcessor:" + processor.getRecipeProcessorId() + "  identifier:");
                    for (String ident : processor.getAllOverlayIdentifier()) {
                        logger.info(ident);
                    }
                }
            }
        }else {
            throw new WrongUsageException("use /nee RecipeProcessor to get RecipeProcessor and identifier,see them in log");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return CommandBase.getListOfStringsMatchingLastWord(args, "RecipeProcessor");
    }
}
