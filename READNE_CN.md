# Not Enough Energistics
NotEnoughEnergistics 是[Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee) 的逆向移植版本

它替换了AE2默认的NEI集成, 默认情况下ae只能识别玩家身上或者网络里的存储物品。

本MOD允许你直接从nei获取配方并写入样板终端(shift点击[?]按钮)
并且会自动帮你切换样板模式。

## 当前支持的Mod列表：
- [x]  Vanilla
- [x]  GregTech
- [x]  IndustrialCraft2
- [x]  Avaritia

如果你想要别的Mod的支持，请看[RecipeProcessor](./src/main/java/com/github/vfyjxf/nee/processor/RecipeProcessor.java) 或者[模组支持处理区](https://github.com/vfyjxf/NotEnoughEnergistics/issues/1)

## TODO
 - 添加转换黑名单，使在黑名单里的物品不会被转移到样板终端
 - 添加优先转换列表，在优先转换列表的物品会替换配方里的同类物品
 - 在有多个同类物品的情况下，合并同类物品

## Credits
感谢 TheRealp455w0rd 和他的 [Just Enough Energistics](https://www.curseforge.com/minecraft/mc-mods/just-enough-energistics-jee)


