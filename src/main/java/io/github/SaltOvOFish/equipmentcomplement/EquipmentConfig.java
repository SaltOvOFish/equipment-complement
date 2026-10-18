package io.github.SaltOvOFish.equipmentcomplement;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置，按用途分为五个模块：
 *
 * <ol>
 *   <li>{@code [general]} —— 全局（创造模式标签页 / 下界合金马铠版本 / 铜制品命名与创造栏）</li>
 *   <li>{@code [oxidation]} —— 铜制品的氧化与涂蜡</li>
 *   <li>{@code [spears]} —— 长矛</li>
 *   <li>{@code [armor.horse_armor]} —— 马铠</li>
 *   <li>{@code [armor.nautilus_armor]} —— 鹦鹉螺铠</li>
 * </ol>
 *
 * <p>注册类只负责「是否注册该物品」，具体数值与行为在各自的注册 / 事件类里。</p>
 */
public class EquipmentConfig {

    public static final ModConfigSpec SPEC;

    // ==================== 全局（general） ====================
    /** 是否启用创造模式标签页「装备增补」。 */
    public static final ModConfigSpec.BooleanValue EQUIPMENT_TAB;
    /** 是否用 Caverns &amp; Chasms 的下界合金马铠替换 VanillaBackport 版本（只影响显示 / 配方，不删除物品）。 */
    public static final ModConfigSpec.BooleanValue CANDC_NETHERITE_HORSE_ARMOR;
    /** 是否在创造模式物品栏显示铜制品的七种氧化 / 涂蜡变体。 */
    public static final ModConfigSpec.BooleanValue COPPER_VARIANTS_IN_TAB;
    /** 是否把 C&amp;C 的铜制装备显示名改成「重铜XX」/「Heavy Copper XX」（内置资源包实现）。 */
    public static final ModConfigSpec.BooleanValue HEAVY_COPPER_NAMES;
    /** 是否启用马铠 / 鹦鹉螺铠的工作台配方（不影响锻造台与灌注配方）。 */
    public static final ModConfigSpec.BooleanValue MOUNT_ARMOR_CRAFTING;

    // ==================== 氧化（oxidation） ====================
    /** C&amp;C 铜矛 / 铜鹦鹉螺铠的氧化与涂蜡（注册变体 + 推进）。 */
    public static final ModConfigSpec.BooleanValue CANDC_COPPER_OXIDATION;
    /** VanillaBackport 铜制装备与铜锭的氧化与涂蜡（注册变体 + 推进）。 */
    public static final ModConfigSpec.BooleanValue VBP_COPPER_OXIDATION;
    /** 是否允许铜制装备放在物品栏任意格中氧化（不必穿戴 / 手持）。 */
    public static final ModConfigSpec.BooleanValue INVENTORY_OXIDATION;

    // ==================== 长矛（spears） ====================
    public static final ModConfigSpec.BooleanValue ENABLE_OREGANIZED_SILVER_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_OREGANIZED_ELECTRUM_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_CANDC_SILVER_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_CANDC_NECROMIUM_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_CANDC_COPPER_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_THALLASIUM_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_THALLASIUM_SPEAR_HEAD;
    public static final ModConfigSpec.BooleanValue ENABLE_TERMINITE_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_TERMINITE_SPEAR_HEAD;
    public static final ModConfigSpec.BooleanValue ENABLE_AETERNIUM_SPEAR;
    public static final ModConfigSpec.BooleanValue ENABLE_AETERNIUM_SPEAR_HEAD;

    // ==================== 马铠（armor.horse_armor） ====================
    public static final ModConfigSpec.BooleanValue ENABLE_CHAINMAIL_HORSE_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_SILVER_HORSE_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_ELECTRUM_HORSE_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_THALLASIUM_HORSE_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_TERMINITE_HORSE_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CRYSTALITE_HORSE_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_AETERNIUM_HORSE_ARMOR;

    // ==================== 鹦鹉螺铠（armor.nautilus_armor） ====================
    public static final ModConfigSpec.BooleanValue ENABLE_LEATHER_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CHAINMAIL_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_OREGANIZED_SILVER_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_OREGANIZED_ELECTRUM_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CANDC_COPPER_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CANDC_SILVER_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CANDC_NECROMIUM_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_THALLASIUM_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_TERMINITE_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_CRYSTALITE_NAUTILUS_ARMOR;
    public static final ModConfigSpec.BooleanValue ENABLE_AETERNIUM_NAUTILUS_ARMOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // ---------- 模块一：全局 ----------
        builder.comment("全局（General）").push("general");
        EQUIPMENT_TAB = builder
                .comment("是否启用创造模式标签页「装备增补」（收录本模组全部新增物品）",
                        " Whether to enable the \"Equipment Complement\" creative mode tab (contains every item added by this mod)",
                        "默认：禁用",
                        " Default：false")
                .define("equipment_tab", false);
        CANDC_NETHERITE_HORSE_ARMOR = builder
                .comment("是否将下界合金马铠替换为 Caverns & Chasms 版本",
                        " Whether to replace the Netherite Horse Armor with the Caverns & Chasms version",
                        "开启时将显示 Caverns & Chasms 下界合金马铠，并隐藏 VanillaBackport 下界合金马铠",
                        " When enabled, the Caverns & Chasms Netherite Horse Armor is shown and the VanillaBackport one is hidden",
                        "并不会真正删除另一版本",
                        " The other version is not actually removed",
                        "默认：禁用",
                        " Default：false")
                .define("candc_netherite_horse_armor", false);
        COPPER_VARIANTS_IN_TAB = builder
                .comment("是否在创造模式物品栏中显示铜制品的七种氧化 / 涂蜡变体",
                        " Whether to show the seven oxidized / waxed variants of copper items in the creative inventory",
                        "本模组新增的 Caverns & Chasms 与 VanillaBackport 铜制品都会按「变体跟在基础件后面」排列",
                        " Copper items from Caverns & Chasms and VanillaBackport added by this mod are all ordered with their variants right after the base item",
                        "默认：禁用",
                        " Default：false")
                .define("copper_variants_in_tab", false);
        HEAVY_COPPER_NAMES = builder
                .comment("是否更改 Caverns & Chasms 的铜制装备显示名为「重铜XX」",
                        " Whether to rename Caverns & Chasms copper equipment to \"Heavy Copper XX\"",
                        "关闭时使用 Caverns & Chasms 的原物品名",
                        " When disabled, the original Caverns & Chasms item names are used",
                        "默认：启用",
                        " Default：true")
                .define("heavy_copper_names", true);
        MOUNT_ARMOR_CRAFTING = builder
                .comment("是否启用马铠/鹦鹉螺铠的工作台配方",
                        " Whether to enable crafting table recipes for horse armor / nautilus armor",
                        "禁用会导致部分新增的马铠/鹦鹉螺铠无法获取",
                        " Disabling this makes some of the added horse armor / nautilus armor unobtainable",
                        "皮革以及锻造台配方的坐骑铠不受影响",
                        " Mount armor made from leather and from smithing table recipes is unaffected",
                        "默认：启用",
                        " Default：true")
                .define("mount_armor_crafting", true);
        builder.pop();

        // ---------- 模块二：氧化 ----------
        builder.comment("氧化（Oxidation）").push("oxidation");
        CANDC_COPPER_OXIDATION = builder
                .comment("是否启用 Caverns & Chasms 铜矛与铜鹦鹉螺铠的氧化与涂蜡机制",
                        " Whether to enable oxidation and waxing for Caverns & Chasms copper spears and copper nautilus armor",
                        "关闭时仅注册普通铜矛 / 铜鹦鹉螺铠，不注册变体",
                        " When disabled, only the plain copper spear / copper nautilus armor is registered, with no variants",
                        "默认：启用",
                        " Default：true")
                .define("candc_copper_oxidation", true);
        VBP_COPPER_OXIDATION = builder
                .comment("是否启用 VanillaBackport 铜制装备及铜锭的氧化与涂蜡机制",
                        " Whether to enable oxidation and waxing for VanillaBackport copper equipment and copper ingots",
                        "关闭时仅注册普通铜装备与铜锭，不注册铜制装备及锭的变体",
                        " When disabled, only plain copper equipment and copper ingots are registered, with no variants",
                        "铜锭的氧化变体会在安装 Caverns & Chasms 时自动禁用",
                        " The copper ingot oxidation variants are automatically disabled when Caverns & Chasms is installed",
                        "默认：启用",
                        " Default：true")
                .define("vbp_copper_oxidation", true);
        INVENTORY_OXIDATION = builder
                .comment("是否使所有铜制装备可以放在物品栏任意格中氧化",
                        " Whether all copper equipment can oxidize while sitting in any inventory slot",
                        "关闭时仅在手持或者装备时氧化",
                        " When disabled, oxidation only happens while held in hand or worn",
                        "默认：禁用",
                        " Default：false")
                .define("inventory_oxidation", false);
        builder.pop();

        // ---------- 模块三：长矛 ----------
        builder.comment("长矛（Spears）").push("spears");
        ENABLE_OREGANIZED_SILVER_SPEAR = builder
                .comment("是否注册 Oreganized 银矛",
                        " Whether to register the Oreganized silver spear",
                        "默认：启用",
                        " Default：true")
                .define("oreganized_silver_spear", true);
        ENABLE_OREGANIZED_ELECTRUM_SPEAR = builder
                .comment("是否注册 Oreganized 琥珀金矛",
                        " Whether to register the Oreganized electrum spear",
                        "默认：启用",
                        " Default：true")
                .define("oreganized_electrum_spear", true);
        ENABLE_CANDC_SILVER_SPEAR = builder
                .comment("是否注册 Caverns & Chasms 银矛",
                        " Whether to register the Caverns & Chasms silver spear",
                        "默认：启用",
                        " Default：true")
                .define("candc_silver_spear", true);
        ENABLE_CANDC_NECROMIUM_SPEAR = builder
                .comment("是否注册 Caverns & Chasms 死灵合金矛",
                        " Whether to register the Caverns & Chasms necromium spear",
                        "默认：启用",
                        " Default：true")
                .define("candc_necromium_spear", true);
        ENABLE_CANDC_COPPER_SPEAR = builder
                .comment("是否注册 Caverns & Chasms 铜矛",
                        " Whether to register the Caverns & Chasms copper spear",
                        "默认：启用",
                        " Default：true")
                .define("candc_copper_spear", true);
        ENABLE_THALLASIUM_SPEAR = builder
                .comment("是否注册 BetterEnd 塔拉斯金矛＆矛头",
                        " Whether to register the BetterEnd thallasium spear & spear head",
                        "默认：启用",
                        " Default：true")
                .define("thallasium_spear", true);
        ENABLE_THALLASIUM_SPEAR_HEAD = builder
                .define("thallasium_spear_head", true);
        ENABLE_TERMINITE_SPEAR = builder
                .comment("是否注册 BetterEnd 终末合金矛＆矛头",
                        " Whether to register the BetterEnd terminite spear & spear head",
                        "默认：启用",
                        " Default：true")
                .define("terminite_spear", true);
        ENABLE_TERMINITE_SPEAR_HEAD = builder
                .define("terminite_spear_head", true);
        ENABLE_AETERNIUM_SPEAR = builder
                .comment("是否注册 BetterEnd 太古合金矛＆矛头",
                        " Whether to register the BetterEnd aeternium spear & spear head",
                        "默认：启用",
                        " Default：true")
                .define("aeternium_spear", true);
        ENABLE_AETERNIUM_SPEAR_HEAD = builder
                .define("aeternium_spear_head", true);
        builder.pop();

        // ---------- 模块四：马铠 ----------
        builder.push("armor");

        builder.comment("马铠（Horse Armor）").push("horse_armor");
        ENABLE_CHAINMAIL_HORSE_ARMOR = builder
                .comment("是否注册锁链马铠",
                        " Whether to register chainmail horse armor",
                        "默认：启用",
                        " Default：true")
                .define("chainmail_horse_armor", true);
        ENABLE_SILVER_HORSE_ARMOR = builder
                .comment("是否注册 Oreganized 银马铠",
                        " Whether to register the Oreganized silver horse armor",
                        "默认：启用",
                        " Default：true")
                .define("silver_horse_armor", true);
        ENABLE_ELECTRUM_HORSE_ARMOR = builder
                .comment("是否注册 Oreganized 琥珀金马铠",
                        " Whether to register the Oreganized electrum horse armor",
                        "默认：启用",
                        " Default：true")
                .define("electrum_horse_armor", true);
        ENABLE_THALLASIUM_HORSE_ARMOR = builder
                .comment("是否注册 BetterEnd 塔拉斯金马铠",
                        " Whether to register the BetterEnd thallasium horse armor",
                        "默认：启用",
                        " Default：true")
                .define("thallasium_horse_armor", true);
        ENABLE_TERMINITE_HORSE_ARMOR = builder
                .comment("是否注册 BetterEnd 终末合金马铠",
                        " Whether to register the BetterEnd terminite horse armor",
                        "默认：启用",
                        " Default：true")
                .define("terminite_horse_armor", true);
        ENABLE_CRYSTALITE_HORSE_ARMOR = builder
                .comment("是否注册 BetterEnd 融晶马铠",
                        " Whether to register the BetterEnd crystalite horse armor",
                        "默认：启用",
                        " Default：true")
                .define("crystalite_horse_armor", true);
        ENABLE_AETERNIUM_HORSE_ARMOR = builder
                .comment("是否注册 BetterEnd 太古合金马铠",
                        " Whether to register the BetterEnd aeternium horse armor",
                        "默认：启用",
                        " Default：true")
                .define("aeternium_horse_armor", true);
        builder.pop();

        // ---------- 模块五：鹦鹉螺铠 ----------
        builder.comment("鹦鹉螺铠（Nautilus Armor）").push("nautilus_armor");
        ENABLE_LEATHER_NAUTILUS_ARMOR = builder
                .comment("是否注册皮革鹦鹉螺铠",
                        " Whether to register leather nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("leather_nautilus_armor", true);
        ENABLE_CHAINMAIL_NAUTILUS_ARMOR = builder
                .comment("是否注册锁链鹦鹉螺铠",
                        " Whether to register chainmail nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("chainmail_nautilus_armor", true);
        ENABLE_OREGANIZED_SILVER_NAUTILUS_ARMOR = builder
                .comment("是否注册 Oreganized 银鹦鹉螺铠",
                        " Whether to register the Oreganized silver nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("oreganized_silver_nautilus_armor", true);
        ENABLE_OREGANIZED_ELECTRUM_NAUTILUS_ARMOR = builder
                .comment("是否注册 Oreganized 琥珀金鹦鹉螺铠",
                        " Whether to register the Oreganized electrum nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("oreganized_electrum_nautilus_armor", true);
        ENABLE_CANDC_COPPER_NAUTILUS_ARMOR = builder
                .comment("是否注册 Caverns & Chasms 铜鹦鹉螺铠",
                        " Whether to register the Caverns & Chasms copper nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("candc_copper_nautilus_armor", true);
        ENABLE_CANDC_SILVER_NAUTILUS_ARMOR = builder
                .comment("是否注册 Caverns & Chasms 银鹦鹉螺铠",
                        " Whether to register the Caverns & Chasms silver nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("candc_silver_nautilus_armor", true);
        ENABLE_CANDC_NECROMIUM_NAUTILUS_ARMOR = builder
                .comment("是否注册 Caverns & Chasms 死灵合金鹦鹉螺铠",
                        " Whether to register the Caverns & Chasms necromium nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("candc_necromium_nautilus_armor", true);
        ENABLE_THALLASIUM_NAUTILUS_ARMOR = builder
                .comment("是否注册 BetterEnd 塔拉斯金鹦鹉螺铠",
                        " Whether to register the BetterEnd thallasium nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("thallasium_nautilus_armor", true);
        ENABLE_TERMINITE_NAUTILUS_ARMOR = builder
                .comment("是否注册 BetterEnd 终末合金鹦鹉螺铠",
                        " Whether to register the BetterEnd terminite nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("terminite_nautilus_armor", true);
        ENABLE_CRYSTALITE_NAUTILUS_ARMOR = builder
                .comment("是否注册 BetterEnd 融晶鹦鹉螺铠",
                        " Whether to register the BetterEnd crystalite nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("crystalite_nautilus_armor", true);
        ENABLE_AETERNIUM_NAUTILUS_ARMOR = builder
                .comment("是否注册 BetterEnd 太古合金鹦鹉螺铠",
                        " Whether to register the BetterEnd aeternium nautilus armor",
                        "默认：启用",
                        " Default：true")
                .define("aeternium_nautilus_armor", true);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

    /**
     * 按名称查找一个布尔配置项，供 {@link ConfigCondition} 在数据包条件里使用。
     * 未知名称返回 {@code null}。
     */
    public static ModConfigSpec.BooleanValue booleanOption(String option) {
        return switch (option) {
            case "mount_armor_crafting" -> MOUNT_ARMOR_CRAFTING;
            case "candc_netherite_horse_armor" -> CANDC_NETHERITE_HORSE_ARMOR;
            default -> null;
        };
    }
}
