[中文](READNE_CN.md)

# Not Enough Energistics
NotEnoughEnergistics is a port of [Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee) to Minecraft 1.7.10

This mod replaces AE2's default NEI integration for the Pattern Terminal.

By default, AE2 forces the player to either have the necessary items for a recipe in their personal inventory or in their ME network.

This mod removes that functionality and allows the player to use NEI's transfer system (shift + click [?] button) to create patterns whether the player possesses the items required by the recipe.


## Features

- Automatically switch the mode of PatternTerminal
- If an item is a probability output, then nee will not transfer it.
- Combine like stacks in processing patterns

## Compatible Modslist as followed：

- [ ]  AppliedEnergistics2(Won't support it because AE2 doesn't register OverlayHandler)
- [x]  Vanilla
- [x]  GregTech
- [x]  IndustrialCraft2
- [x]  Avaritia
- [x]  EnderIO 
- [x]  Forestry(Doesn't support Fermenter and Still,because they don't have an item output)

If you want other Mod support,see [RecipeProcessor](./src/main/java/com/github/vfyjxf/nee/processor/RecipeProcessor.java) and [Mod support](https://github.com/vfyjxf/NotEnoughEnergistics/issues/1)

## TODO

- Add item blackList ,if item in the blacklist, it will not be transferred.
- Add item  priority list, if item in tne priority list, it will be transferred first

~~Combine like stacks in processing patterns.~~

## Credits
Thanks TheRealp455w0rd and his [Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee)
