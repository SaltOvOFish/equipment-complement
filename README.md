# Equipment Complement

为 [VanillaBackport](https://github.com/ItsBlackGear/VanillaBackport) 补充多材质的长矛、马铠与鹦鹉螺铠，并加入一套铜装备氧化 / 涂蜡体系。

> Equipment Complement adds multi-material spears, horse armors and nautilus armors to VanillaBackport, plus a copper oxidation & waxing system for copper equipment.

- 适用版本：Minecraft **1.21.1** + NeoForge **21.1.209**
- Mod ID：`equipment_complement`
- 许可证：**MIT License**

## 前置

| 模组 | 类型 | 最低版本 | 说明 |
|---|---|---|---|
| [VanillaBackport](https://github.com/ItsBlackGear/VanillaBackport) | 必装 | **1.2** | 长矛核心、鹦鹉螺铠、铜装备本体 |
| [Platform](https://github.com/ItsBlackGear/Platform) | 必装 | **1.4** | VanillaBackport 的库依赖 / 物品渲染器 API |
| [BetterEnd](https://modrinth.com/mod/betterend) | 可选 | 21.0.x | 塔拉斯金 / 终末合金 / 融晶 / 太古合金材质 |
| [Oreganized](https://modrinth.com/mod/oreganized) | 可选 | 5.x | 银 / 琥珀金材质 |
| [Caverns & Chasms](https://modrinth.com/mod/caverns-and-chasms) | 可选 | 3.x | 银 / 铜 / 死灵合金材质；需要 Blueprint |

未安装可选模组时，对应物品、配方与配置项不会注册，不影响其余内容。

## 内容概览

### 长矛

- 为 Oreganized、Caverns & Chasms、BetterEnd 的多种材质补充长矛。
- 包含银、琥珀金、死灵合金、塔拉斯金、终末合金、太古合金，以及 BetterEnd 的矛头。
- 所有长矛统一使用 `bettercombat:spear` 武器属性(仅做预备，当前版本VB并不完全兼容BetterCombat)。
- 铜矛包含氧化 / 涂蜡变体。

### 马铠

- 补充锁链、银（Oreganized）、琥珀金、塔拉斯金、终末合金、融晶、太古合金马铠。
- 下界合金马铠支持在 VanillaBackport 版与 Caverns & Chasms 版之间二选一：
  - 默认使用 VanillaBackport 版；
  - 开启 `candc_netherite_horse_armor` 后改用 C&C 版，并隐藏 / 禁用另一版的获取方式；
  - C&C 版数值会对齐 VanillaBackport 版。
- 锁链马铠会出现在林地府邸与盔甲匠礼物战利品中。

### 鹦鹉螺铠

- 补充皮革、锁链、银（Oreganized / C&C）、铜（C&C）、死灵合金、塔拉斯金、终末合金、融晶、太古合金鹦鹉螺铠。
- 皮革鹦鹉螺铠可像皮革盔甲一样用染料染色，支持混色。
- C&C 铜鹦鹉螺铠包含氧化 / 涂蜡变体，并带有与 C&C 铜马铠一致的移动速度惩罚。
- 锁链鹦鹉螺铠会出现在林地府邸与盔甲匠礼物战利品中。
- 皮革鹦鹉螺铠始终可以合成，不依赖战利品。
- 皮匠专家级也会以 6 个绿宝石出售随机染色的皮革鹦鹉螺铠（与皮革马铠一致）。

### 铜装备氧化与涂蜡

- 为 VanillaBackport 的铜剑、工具、盔甲、马铠、鹦鹉螺铠、长矛与铜锭生成氧化 / 涂蜡变体。
- 支持蜜脾与 C&C 锡板涂蜡；装 C&C 时铜锭链直接接入 C&C 的铜锭体系。
- 可选开启“物品栏内氧化”；默认只在角色装备槽中推进。
- 闪电会去除氧化：头部槽位重置回「铜」阶段，另随机若干槽位各退一阶段（涂蜡物品不受影响）。

### 创造模式标签页与资源包

- 「装备增补」标签页收录本模组新增物品；是否显示铜变体由 `copper_variants_in_tab` 控制。
- 内置「重铜命名」资源包，把 C&C 本体的铜装备显示为「重铜XX」；仅在安装 C&C 时挂载。

## 获取与配方（概况）

- 马铠工作台形状：`X·X / XXX / X·X`
- 鹦鹉螺铠工作台形状：`XXX / ·X· / XXX`
- 皮革马铠与皮革鹦鹉螺铠**始终可以合成**，并可在皮匠专家级交易中以 6 个绿宝石购买随机染色版本。
- 其余新增的工作台坐骑铠配方受 `mount_armor_crafting` 控制；关闭后这些配方会从工作台 / JEI 中消失。
- 锻造台配方（下界合金、终末合金、琥珀金、死灵合金）与融晶灌注配方**不受** `mount_armor_crafting` 影响。
- 完整的配方与数值请以游戏内 JEI 和物品 tooltip 为准。

## 配置

首次启动生成 `config/equipment_complement-startup.toml`，按用途分为五组：

| 分组 | 内容 |
|---|---|
| `[general]` | 装备增补标签页、C&C 下界合金马铠替换、铜变体显示、重铜命名、工作台坐骑铠配方总开关 |
| `[oxidation]` | C&C / VanillaBackport 铜装备氧化、物品栏氧化 |
| `[spears]` | 各材质长矛与矛头注册开关 |
| `[armor.horse_armor]` | 各材质马铠注册开关 |
| `[armor.nautilus_armor]` | 各材质鹦鹉螺铠注册开关 |

所有配置均为 **STARTUP** 类型，修改后需要重启游戏。

## 安装

1. 安装 Minecraft 1.21.1 + NeoForge。
2. 安装必装前置 VanillaBackport ≥ 1.2 与 Platform ≥ 1.4。
3. 按需安装 BetterEnd / Oreganized / Caverns & Chasms。
4. 将本模组 jar 放入 `mods/`。

## 构建

需要 **JDK 21**。前置模组 jar **不包含在本仓库中**，请自行从官方发布页下载：

- `VanillaBackport-neoforge-1.21.1-1.2.jar`（或更高）
- `Platform-neoforge-1.21.1-1.4.jar`（或更高）

默认构建会读取 `libs/`；也可以显式指定其它目录：

```powershell
.\gradlew.bat build
.\gradlew.bat build -Pdependency_dir="D:\path\to\dependencies"
```

产物位于 `build/libs/equipment_complement-1.0.0.jar`。

> 上传仓库时请不要提交 `libs/*.jar`、`libs/obsolete/`、`build/`、`run/` 或 `mods/`。

## 兼容性说明

- **Better Combat**：所有长矛显式使用 `bettercombat:spear`，不依赖回退正则。
- **Caverns & Chasms 下界合金马铠**：两个版本同时只保留一个可正常获取；被隐藏的版本仍注册，可用 `/give` 取得。
- **坐骑铠耐久**：与原版马铠一致，不设耐久。
- **配置文件**：STARTUP 类型，改动后需重启。

## 许可证与致谢

除非文件内另有说明，本仓库的 **代码与原创素材** 采用 [MIT License](LICENSE)。

第三方模组的名称、物品 ID、数值与素材版权归各自作者所有；本仓库**不包含**这些模组的代码或 jar。详细声明见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

特别感谢：

- **ItsBlackGear**：VanillaBackport 与 Platform。
- **paulevsGitch / Raijin**：BetterEnd。
- **TeamGalena**：Oreganized。
- **Team Abnormals**：Caverns & Chasms。
- **Unknowneth**：Backported Spears。

如果你认为本仓库中有素材归属标注遗漏，请通过仓库 Issue 联系，我们会核实并替换或移除相关内容。

## AI 辅助开发说明

本项目的部分 Java 代码、数据文件（配方 / 标签 / 模型 JSON）与文档使用 AI 工具辅助生成、批量改写或审查。所有 AI 辅助内容均经作者人工检查与编译验证，发布前会在游戏中抽检；最终设计、取舍与发布内容由作者负责。AI 辅助不改变本项目的 MIT 许可。