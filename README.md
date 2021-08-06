[中文](READNE_CN.md)

# Not Enough Energistics
NotEnoughEnergistics is a port of [Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee) to Minecraft 1.7.10

This mod replaces AE2's default NEI integration for the Pattern Terminal.

By default, AE2 forces the player to either have the necessary items for a recipe in their personal inventory or in their ME network.

This mod removes that functionality and allows the player to use NEI's transfer system (shift + click [?] button) to create patterns whether the player possesses the items required by the recipe.

And it will automatically switch the mode of PatternTerminal.

## Compatible Modslist as followed：
- [x]  Vanilla
- [x]  GregTech
- [x]  IndustrialCraft2
- [x]  Avaritia


If you want other Mod support,see [RecipeProcessor](./src/main/java/com/github/vfyjxf/nee/processor/RecipeProcessor.java) and [Mod support](https://github.com/vfyjxf/NotEnoughEnergistics/issues/1)

## TODO
- Add item blackList ,if item in the blacklist, it will not be transferred.
- Add item  priority list, if item in tne priority list, it will be transferred first.
- Combine like stacks in processing patterns.

## Credits
Thanks TheRealp455w0rd and his [Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee)