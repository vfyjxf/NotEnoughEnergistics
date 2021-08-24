# Not Enough Energistics
NotEnoughEnergistics 是[Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee) 的逆向移植版本

它替换了AE2默认的NEI集成, 默认情况下ae只能识别玩家身上或者网络里的存储物品。

本MOD允许你直接从nei获取配方并写入样板终端(shift点击[?]按钮)

## Features

- 根据配方自动切换样板终端的模式
- 如果一个物品输出为概率输出，那么NEE不会将它写入样板终端
- 在处理模式中合并同类物品
- 支持增广样板终端(16 -> 4模式)
- 允许你设置转换黑名单和转换优先名单，如对应物品在里面，那么他们将不会被转移/优先被转移
- 允许你设置mod优先级列表，优先使用该mod的物品

## 当前支持的Mod列表：

- [ ]  AppliedEnergistics2(我们将不会支持AE2，因为AE2没有注册对应的OverlayHandler)
- [x]  Vanilla
- [x]  GregTech5
- [x]  GregTech6
- [x]  IndustrialCraft2
- [x]  Avaritia
- [x]  EnderIO
- [x]  Forestry(不支持发酵机和蒸馏器，因为它们没有物品输出)
- [x]  Thaumcraft NEI Plugin
- [x]  Thaumic Energistics(允许你从NEI转移奥术合成台配方到知识记录仪，需要Thaumcraft NEI Plugin)
- [x]  ThermalExpansion
- [x]  ImmersiveEngineering
- [x]  Mekanism
- [x]  BloodMagic
- [x]  BuildCraft
- [x]  Avaritiaddons(允许你从nei转移终极工作台配方到梦魇工作台)

如果你想要别的Mod的支持，请看[RecipeProcessor](./src/main/java/com/github/vfyjxf/nee/processor/RecipeProcessor.java) 或者[模组支持处理区](https://github.com/vfyjxf/NotEnoughEnergistics/issues/1)

## TODO
- [x] 添加转换黑名单，使在黑名单里的物品不会被转移到样板终端
- [x] 添加优先转换列表，在优先转换列表的物品会替换配方里的同类物品
- [x] 在有多个同类物品的情况下，合并同类物品

## Credits
感谢 TheRealp455w0rd 和他的 [Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee)


