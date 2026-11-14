package io.github.SaltOvOFish.equipmentcomplement;

import com.blackgear.vanillabackport.common.level.components.AttackRange;
import com.blackgear.vanillabackport.common.level.items.spear.SpearItem;
import com.blackgear.vanillabackport.common.registries.items.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 各模组材质长矛（与 BetterEnd 矛头）的注册、手感模板、工具等级与默认组件覆写。
 */
public final class SpearRegistry {

    public static DeferredItem<Item> OREGANIZED_SILVER_SPEAR;
    public static DeferredItem<Item> OREGANIZED_ELECTRUM_SPEAR;
    public static DeferredItem<Item> TERMINITE_SPEAR;
    public static DeferredItem<Item> AETERNIUM_SPEAR;
    public static DeferredItem<Item> THALLASIUM_SPEAR;
    public static DeferredItem<Item> THALLASIUM_SPEAR_HEAD;
    public static DeferredItem<Item> TERMINITE_SPEAR_HEAD;
    public static DeferredItem<Item> AETERNIUM_SPEAR_HEAD;
    public static DeferredItem<Item> CANDC_SILVER_SPEAR;
    public static DeferredItem<Item> CANDC_NECROMIUM_SPEAR;

    public static DeferredItem<Item> CANDC_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_EXPOSED_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_WEATHERED_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_OXIDIZED_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_WAXED_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_WAXED_EXPOSED_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_WAXED_WEATHERED_COPPER_SPEAR;
    public static DeferredItem<Item> CANDC_WAXED_OXIDIZED_COPPER_SPEAR;

    public static DeferredItem<Item> CURIOSITIES_ALUMINUM_SPEAR;
    public static DeferredItem<Item> CURIOSITIES_INVAR_SPEAR;
    public static DeferredItem<Item> LEGENDARY_MONSTERS_ENDERITIUM_SPEAR;

    /**
     * 长矛动能/突刺参数组（除攻速外的 8 个数值完全按档位复用，集中定义为具名常量防止手抄错位）。
     */
    private record SpearProfile(
            float chargeDamageMultiplier, float chargeDelay,
            float maxDismount, float minDismount,
            float maxKnockback, float minKnockback,
            float maxChargeDamage, float minChargeDamage) {
    }

    /** 轻型档（银、铜、塔拉斯金、铝）。 */
    private static final SpearProfile LIGHT = new SpearProfile(0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F);
    /** 中型档（琥珀金）。 */
    private static final SpearProfile MEDIUM = new SpearProfile(1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F);
    /** 重型档（终末合金、太古合金、死灵合金、殷钢、终末金属）。 */
    private static final SpearProfile HEAVY = new SpearProfile(1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F);

    private SpearRegistry() {
    }

    /** 注册所有已启用且对应前置已加载的长矛与矛头。 */
    public static void init() {
        if (ModList.get().isLoaded("oreganized")) {
            if (EquipmentConfig.ENABLE_OREGANIZED_SILVER_SPEAR.get()) {
                OREGANIZED_SILVER_SPEAR = ItemRegistries.OREGANIZED.register("silver_spear",
                        () -> makeSpear(oreganizedSilverTier(), 1.05F, LIGHT));
            }
            if (EquipmentConfig.ENABLE_OREGANIZED_ELECTRUM_SPEAR.get()) {
                OREGANIZED_ELECTRUM_SPEAR = ItemRegistries.OREGANIZED.register("electrum_spear",
                        () -> makeSpear(oreganizedElectrumTier(), 1.05F, MEDIUM));
            }
        }
        if (ModList.get().isLoaded("betterend")) {
            if (EquipmentConfig.ENABLE_THALLASIUM_SPEAR.get()) {
                THALLASIUM_SPEAR = ItemRegistries.BETTEREND.register("thallasium_spear",
                        () -> makeSpear(thallasiumTier(), 1.15F, LIGHT));
            }
            if (EquipmentConfig.ENABLE_THALLASIUM_SPEAR_HEAD.get()) {
                THALLASIUM_SPEAR_HEAD = ItemRegistries.BETTEREND.register("thallasium_spear_head",
                        () -> new Item(new Item.Properties()));
            }
            if (EquipmentConfig.ENABLE_TERMINITE_SPEAR.get()) {
                TERMINITE_SPEAR = ItemRegistries.BETTEREND.register("terminite_spear",
                        () -> makeSpear(terminiteTier(), 1.0F, HEAVY));
            }
            if (EquipmentConfig.ENABLE_TERMINITE_SPEAR_HEAD.get()) {
                TERMINITE_SPEAR_HEAD = ItemRegistries.BETTEREND.register("terminite_spear_head",
                        () -> new Item(new Item.Properties()));
            }
            if (EquipmentConfig.ENABLE_AETERNIUM_SPEAR.get()) {
                AETERNIUM_SPEAR = ItemRegistries.BETTEREND.register("aeternium_spear",
                        () -> makeSpear(aeterniumTier(), 0.95F, HEAVY));
            }
            if (EquipmentConfig.ENABLE_AETERNIUM_SPEAR_HEAD.get()) {
                AETERNIUM_SPEAR_HEAD = ItemRegistries.BETTEREND.register("aeternium_spear_head",
                        () -> new Item(new Item.Properties()));
            }
        }
        if (ModList.get().isLoaded("caverns_and_chasms")) {
            if (EquipmentConfig.ENABLE_CANDC_SILVER_SPEAR.get()) {
                CANDC_SILVER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("silver_spear",
                        () -> makeSpear(candcSilverTier(), 1.05F, LIGHT));
            }
            if (EquipmentConfig.ENABLE_CANDC_NECROMIUM_SPEAR.get()) {
                CANDC_NECROMIUM_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("necromium_spear",
                        () -> makeSpear(necromiumTier(), 0.87F, HEAVY));
            }
            if (EquipmentConfig.ENABLE_CANDC_COPPER_SPEAR.get()) {
                CANDC_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("copper_spear", SpearRegistry::makeCopperSpear);
                if (EquipmentConfig.CANDC_COPPER_OXIDATION.get()) {
                    CANDC_EXPOSED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("exposed_copper_spear", SpearRegistry::makeCopperSpear);
                    CANDC_WEATHERED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("weathered_copper_spear", SpearRegistry::makeCopperSpear);
                    CANDC_OXIDIZED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("oxidized_copper_spear", SpearRegistry::makeCopperSpear);
                    CANDC_WAXED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("waxed_copper_spear", SpearRegistry::makeCopperSpear);
                    CANDC_WAXED_EXPOSED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("waxed_exposed_copper_spear", SpearRegistry::makeCopperSpear);
                    CANDC_WAXED_WEATHERED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("waxed_weathered_copper_spear", SpearRegistry::makeCopperSpear);
                    CANDC_WAXED_OXIDIZED_COPPER_SPEAR = ItemRegistries.CAVERNS_AND_CHASMS.register("waxed_oxidized_copper_spear", SpearRegistry::makeCopperSpear);
                }
            }
        }
        if (ModList.get().isLoaded("curiosities")) {
            if (EquipmentConfig.ENABLE_CURIOSITIES_ALUMINUM_SPEAR.get()) {
                CURIOSITIES_ALUMINUM_SPEAR = ItemRegistries.CURIOSITIES.register("aluminum_spear",
                        () -> makeSpear(aluminumTier(), 1.15F, LIGHT));
            }
            if (EquipmentConfig.ENABLE_CURIOSITIES_INVAR_SPEAR.get()) {
                CURIOSITIES_INVAR_SPEAR = ItemRegistries.CURIOSITIES.register("invar_spear",
                        () -> makeSpear(invarTier(), 0.87F, HEAVY));
            }
        }
        if (ModList.get().isLoaded("legendary_monsters")) {
            if (EquipmentConfig.ENABLE_LEGENDARY_MONSTERS_ENDERITIUM_SPEAR.get()) {
                LEGENDARY_MONSTERS_ENDERITIUM_SPEAR = ItemRegistries.LEGENDARY_MONSTERS.register("enderitium_spear",
                        () -> makeSpear(enderitiumTier(), 0.95F, HEAVY));
            }
        }
    }

    /** 默认数据组件覆写（防火、铝矛 +1 攻击距离）。 */
    public static void onModifyComponents(ModifyDefaultComponentsEvent event) {
        if (AETERNIUM_SPEAR != null) {
            event.modify(AETERNIUM_SPEAR.get(),
                    builder -> builder.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
        }
        if (AETERNIUM_SPEAR_HEAD != null) {
            event.modify(AETERNIUM_SPEAR_HEAD.get(),
                    builder -> builder.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
        }
        // 铝矛：长矛的攻击距离来自 ATTACK_RANGE 组件（优先级高于 entity_interaction_range 属性），
        // 因此要让 +2 交互距离在攻击上同步生效，必须把组件的 reach 也各 +2。
        // 原版长矛：min 2.0 / max 4.5 / 创造 min 2.0 / 创造 max 6.5 / 命中箱 0.125 / mob 0.5
        if (CURIOSITIES_ALUMINUM_SPEAR != null) {
            event.modify(CURIOSITIES_ALUMINUM_SPEAR.get(),
                    builder -> builder.set(ModDataComponents.ATTACK_RANGE.get(),
                            new AttackRange(2.0F, 6.5F, 2.0F, 8.5F, 0.125F, 0.5F)));
        }
    }

    // ==================== 构造与工具等级 ====================

    private static Item makeCopperSpear() {
        return makeSpear(copperTier(), 0.87F, LIGHT);
    }

    private static Item makeSpear(Tier material, float attackSpeed, SpearProfile profile) {
        return new SpearItem(material, 1.0F / attackSpeed,
                profile.chargeDamageMultiplier(), profile.chargeDelay(),
                profile.maxDismount(), profile.minDismount(),
                profile.maxKnockback(), profile.minKnockback(),
                profile.maxChargeDamage(), profile.minChargeDamage(),
                new Item.Properties());
    }

    private static Tier tier(TagKey<net.minecraft.world.level.block.Block> incorrectBlocks,
                             int uses, float speed, float attackDamageBonus, int enchantmentValue,
                             String repairNamespace, String repairPath) {
        return new SimpleTier(incorrectBlocks, uses, speed, attackDamageBonus, enchantmentValue,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(repairNamespace, repairPath))));
    }

    private static Tier oreganizedSilverTier() {
        return tier(BlockTags.INCORRECT_FOR_IRON_TOOL, 191, 6.0F, 2.0F, 14, "oreganized", "silver_ingot");
    }

    private static Tier oreganizedElectrumTier() {
        return tier(BlockTags.INCORRECT_FOR_IRON_TOOL, 1561, 8.0F, 3.0F, 14, "oreganized", "electrum_ingot");
    }

    private static Tier terminiteTier() {
        return tier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1230, 9.0F, 3.0F, 15, "betterend", "terminite_ingot");
    }

    private static Tier aeterniumTier() {
        return tier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2196, 9.0F, 5.5F, 15, "betterend", "aeternium_ingot");
    }

    private static Tier thallasiumTier() {
        return tier(BlockTags.INCORRECT_FOR_IRON_TOOL, 320, 6.0F, 1.5F, 14, "betterend", "thallasium_ingot");
    }

    private static Tier candcSilverTier() {
        return tier(BlockTags.INCORRECT_FOR_IRON_TOOL, 157, 6.0F, 1.0F, 25, "caverns_and_chasms", "silver_ingot");
    }

    /** 死灵合金与殷钢同为 2031 耐久 / 9.0 速度 / +3.0 伤害 / 15 附魔，仅挖掘等级标签与修复材料不同。 */
    private static Tier netheriteLikeTier(TagKey<net.minecraft.world.level.block.Block> incorrectBlocks,
                                          String repairNamespace, String repairPath) {
        return tier(incorrectBlocks, 2031, 9.0F, 3.0F, 15, repairNamespace, repairPath);
    }

    private static Tier necromiumTier() {
        return netheriteLikeTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, "caverns_and_chasms", "necromium_ingot");
    }

    private static Tier invarTier() {
        return netheriteLikeTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, "curiosities", "invar_ingot");
    }

    private static Tier aluminumTier() {
        return tier(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 8.0F, 2.0F, 14, "curiosities", "aluminum_ingot");
    }

    private static Tier enderitiumTier() {
        return tier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2450, 9.0F, 3.5F, 15, "legendary_monsters", "enderitium_ingot");
    }

    private static Tier copperTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_STONE_TOOL,
                3191, 5.0F, 3.0F, 14,
                () -> Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/all_copper")))
        );
    }
}
