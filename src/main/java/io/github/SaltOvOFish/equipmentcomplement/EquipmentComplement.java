package io.github.SaltOvOFish.equipmentcomplement;

import com.blackgear.vanillabackport.common.level.items.spear.SpearItem;
import io.github.SaltOvOFish.equipmentcomplement.client.ClientSetupEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 装备增补（Equipment Complement）
 *
 * <p>以 VanillaBackport（modId: vanillabackport）为前置，为其长矛系统补充多个模组材质的长矛，
 * 并补充马铠 / 鹦鹉螺铠 / 铜制品氧化等装备内容。长矛核心由 VanillaBackport 的
 * {@link SpearItem} 提供。</p>
 *
 * <p>构造流程按模块拆分：{@link #initSpears()}（长矛）、
 * {@link HorseArmorRegistry#init}（马铠）、{@link NautilusArmorRegistry#init}（鹦鹉螺铠）、
 * {@link CopperEquipmentRegistry#init}（VBP 铜制品变体）、{@link #registerEquipmentTab()}（自有标签页）。</p>
 */
@Mod(EquipmentComplement.MOD_ID)
public class EquipmentComplement {

    public static final String MOD_ID = "equipment_complement";

    private static final DeferredRegister.Items OREGANIZED_ITEMS = DeferredRegister.createItems("oreganized");
    private static final DeferredRegister.Items BETTEREND_ITEMS = DeferredRegister.createItems("betterend");
    private static final DeferredRegister.Items CAVERNS_AND_CHASMS_ITEMS = DeferredRegister.createItems("caverns_and_chasms");
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    /** 数据包条件（equipment_complement:config），供按配置启停配方使用。 */
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CopperWaxingRecipe>> COPPER_WAXING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crafting_special_copper_waxing", () -> new SimpleCraftingRecipeSerializer<>(CopperWaxingRecipe::new));

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

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT_TAB;

    public EquipmentComplement(ModContainer container, IEventBus modEventBus) {
        container.registerConfig(ModConfig.Type.STARTUP, EquipmentConfig.SPEC);

        // ===== 各模块的物品 / 材质注册 =====
        initSpears();                                 // 长矛（含 C&C 铜矛的氧化 / 涂蜡变体）
        HorseArmorRegistry.init(modEventBus);         // 马铠
        NautilusArmorRegistry.init(modEventBus);      // 鹦鹉螺铠
        CopperEquipmentRegistry.init(modEventBus);    // VBP 铜制品的氧化 / 涂蜡变体（minecraft 命名空间）
        registerEquipmentTab();                       // 自有创造模式标签页

        OREGANIZED_ITEMS.register(modEventBus);
        BETTEREND_ITEMS.register(modEventBus);
        CAVERNS_AND_CHASMS_ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CONDITIONS.register("config", () -> ConfigCondition.CODEC);
        CONDITIONS.register(modEventBus);
        TABS.register(modEventBus);

        modEventBus.addListener(CreativeTabEvents::onCreativeTab);
        modEventBus.addListener(EquipmentPackEvents::onAddPackFinders);
        NeoForge.EVENT_BUS.addListener(VillagerTradeEvents::onVillagerTrades);

        if (FMLEnvironment.dist.isClient()) {
            ClientSetupEvents.setup(modEventBus);
        }

        modEventBus.addListener(this::onModifyComponents);
    }

    // ==================== 长矛 ====================

    /** 各材质长矛的注册（未安装对应前置模组时跳过）。 */
    private static void initSpears() {
        if (ModList.get().isLoaded("oreganized")) {
            if (EquipmentConfig.ENABLE_OREGANIZED_SILVER_SPEAR.get()) {
                OREGANIZED_SILVER_SPEAR = OREGANIZED_ITEMS.register("silver_spear",
                        () -> makeSpear(oreganizedSilverTier(), 1.05F,
                                0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F));
            }
            if (EquipmentConfig.ENABLE_OREGANIZED_ELECTRUM_SPEAR.get()) {
                OREGANIZED_ELECTRUM_SPEAR = OREGANIZED_ITEMS.register("electrum_spear",
                        () -> makeSpear(oreganizedElectrumTier(), 1.05F,
                                1.075F, 0.5F, 3.0F, 7.5F, 4.0F, 5.1F, 10.0F, 4.6F));
            }
        }
        if (ModList.get().isLoaded("betterend")) {
            if (EquipmentConfig.ENABLE_THALLASIUM_SPEAR.get()) {
                THALLASIUM_SPEAR = BETTEREND_ITEMS.register("thallasium_spear",
                        () -> makeSpear(thallasiumTier(), 1.15F,
                                0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F));
            }
            if (EquipmentConfig.ENABLE_THALLASIUM_SPEAR_HEAD.get()) {
                THALLASIUM_SPEAR_HEAD = BETTEREND_ITEMS.register("thallasium_spear_head",
                        () -> new Item(new Item.Properties()));
            }
            if (EquipmentConfig.ENABLE_TERMINITE_SPEAR.get()) {
                TERMINITE_SPEAR = BETTEREND_ITEMS.register("terminite_spear",
                        () -> makeSpear(terminiteTier(), 1.0F,
                                1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F));
            }
            if (EquipmentConfig.ENABLE_TERMINITE_SPEAR_HEAD.get()) {
                TERMINITE_SPEAR_HEAD = BETTEREND_ITEMS.register("terminite_spear_head",
                        () -> new Item(new Item.Properties()));
            }
            if (EquipmentConfig.ENABLE_AETERNIUM_SPEAR.get()) {
                AETERNIUM_SPEAR = BETTEREND_ITEMS.register("aeternium_spear",
                        () -> makeSpear(aeterniumTier(), 0.95F,
                                1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F));
            }
            if (EquipmentConfig.ENABLE_AETERNIUM_SPEAR_HEAD.get()) {
                AETERNIUM_SPEAR_HEAD = BETTEREND_ITEMS.register("aeternium_spear_head",
                        () -> new Item(new Item.Properties()));
            }
        }
        if (ModList.get().isLoaded("caverns_and_chasms")) {
            if (EquipmentConfig.ENABLE_CANDC_SILVER_SPEAR.get()) {
                CANDC_SILVER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("silver_spear",
                        () -> makeSpear(candcSilverTier(), 1.05F,
                                0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F));
            }
            if (EquipmentConfig.ENABLE_CANDC_NECROMIUM_SPEAR.get()) {
                CANDC_NECROMIUM_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("necromium_spear",
                        () -> makeSpear(necromiumTier(), 0.87F,
                                1.2F, 0.4F, 2.5F, 7.0F, 3.5F, 5.1F, 8.75F, 4.6F));
            }
            if (EquipmentConfig.ENABLE_CANDC_COPPER_SPEAR.get()) {
                CANDC_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("copper_spear", EquipmentComplement::makeCopperSpear);
                if (EquipmentConfig.CANDC_COPPER_OXIDATION.get()) {
                    CANDC_EXPOSED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("exposed_copper_spear", EquipmentComplement::makeCopperSpear);
                    CANDC_WEATHERED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("weathered_copper_spear", EquipmentComplement::makeCopperSpear);
                    CANDC_OXIDIZED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("oxidized_copper_spear", EquipmentComplement::makeCopperSpear);
                    CANDC_WAXED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("waxed_copper_spear", EquipmentComplement::makeCopperSpear);
                    CANDC_WAXED_EXPOSED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("waxed_exposed_copper_spear", EquipmentComplement::makeCopperSpear);
                    CANDC_WAXED_WEATHERED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("waxed_weathered_copper_spear", EquipmentComplement::makeCopperSpear);
                    CANDC_WAXED_OXIDIZED_COPPER_SPEAR = CAVERNS_AND_CHASMS_ITEMS.register("waxed_oxidized_copper_spear", EquipmentComplement::makeCopperSpear);
                }
            }
        }
    }

    // ==================== 标签页 ====================

    /** 自有创造模式标签页「装备增补」。 */
    private static void registerEquipmentTab() {
        if (!EquipmentConfig.EQUIPMENT_TAB.get()) return;
        EQUIPMENT_TAB = TABS.register("equipment_tab", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.equipment_complement.equipment_tab"))
                .icon(() -> {
                    if (OREGANIZED_SILVER_SPEAR != null) return new ItemStack(OREGANIZED_SILVER_SPEAR.get());
                    if (CANDC_COPPER_SPEAR != null) return new ItemStack(CANDC_COPPER_SPEAR.get());
                    return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("iron_spear")));
                })
                .displayItems((params, output) -> addAllItems(output))
                .build());
    }

    /**
     * 「装备增补」标签页的内容，固定按下列顺序分组：
     * <ol>
     *   <li>长矛（不含更好的末地）—— 材质顺序同战斗用品栏</li>
     *   <li>马铠（不含更好的末地）</li>
     *   <li>鹦鹉螺铠（不含更好的末地）</li>
     *   <li>VanillaBackport 铜制品的氧化 / 涂蜡变体（仅 {@code copper_variants_in_tab} 开启时，按物品顺序排列）</li>
     *   <li>更好的末地：矛（塔拉斯金 → 终末合金 → 太古合金）</li>
     *   <li>更好的末地：矛头</li>
     *   <li>更好的末地：马铠（塔拉斯金 → 终末合金 → 融晶 → 太古合金）</li>
     *   <li>更好的末地：鹦鹉螺铠（同上）</li>
     * </ol>
     */
    private static void addAllItems(CreativeModeTab.Output output) {
        // ---------- 1. 长矛（不含更好的末地）----------
        // 铜矛的氧化 / 涂蜡变体受 general.copper_variants_in_tab 控制；关闭时只显示基础铜矛。
        addVariant(output, CANDC_COPPER_SPEAR);
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            addVariant(output, CANDC_EXPOSED_COPPER_SPEAR);
            addVariant(output, CANDC_WEATHERED_COPPER_SPEAR);
            addVariant(output, CANDC_OXIDIZED_COPPER_SPEAR);
            addVariant(output, CANDC_WAXED_COPPER_SPEAR);
            addVariant(output, CANDC_WAXED_EXPOSED_COPPER_SPEAR);
            addVariant(output, CANDC_WAXED_WEATHERED_COPPER_SPEAR);
            addVariant(output, CANDC_WAXED_OXIDIZED_COPPER_SPEAR);
        }
        addVariant(output, OREGANIZED_SILVER_SPEAR);
        addVariant(output, CANDC_SILVER_SPEAR);
        addVariant(output, OREGANIZED_ELECTRUM_SPEAR);
        addVariant(output, CANDC_NECROMIUM_SPEAR);

        // ---------- 2. 马铠（不含更好的末地）----------
        addVariant(output, HorseArmorRegistry.CHAINMAIL_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.SILVER_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.ELECTRUM_HORSE_ARMOR);

        // ---------- 3. 鹦鹉螺铠（不含更好的末地）----------
        addVariant(output, NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.CANDC_COPPER_NAUTILUS_ARMOR);
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            addVariant(output, NautilusArmorRegistry.CANDC_EXPOSED_COPPER_NAUTILUS_ARMOR);
            addVariant(output, NautilusArmorRegistry.CANDC_WEATHERED_COPPER_NAUTILUS_ARMOR);
            addVariant(output, NautilusArmorRegistry.CANDC_OXIDIZED_COPPER_NAUTILUS_ARMOR);
            addVariant(output, NautilusArmorRegistry.CANDC_WAXED_COPPER_NAUTILUS_ARMOR);
            addVariant(output, NautilusArmorRegistry.CANDC_WAXED_EXPOSED_COPPER_NAUTILUS_ARMOR);
            addVariant(output, NautilusArmorRegistry.CANDC_WAXED_WEATHERED_COPPER_NAUTILUS_ARMOR);
            addVariant(output, NautilusArmorRegistry.CANDC_WAXED_OXIDIZED_COPPER_NAUTILUS_ARMOR);
        }
        addVariant(output, NautilusArmorRegistry.CHAINMAIL_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.OREGANIZED_SILVER_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.CANDC_SILVER_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.OREGANIZED_ELECTRUM_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.CANDC_NECROMIUM_NAUTILUS_ARMOR);

        // ---------- 4. VanillaBackport 铜制品的氧化 / 涂蜡变体 ----------
        // 仅在 copper_variants_in_tab 开启时显示；注册顺序：剑 → 镐 → 斧 → 锹 → 锄 →
        // 头盔/胸甲/护腿/靴子 → 马铠 → 鹦鹉螺铠 → 矛 → 锭，每件内部按「斑驳 → 锈蚀 → 氧化 → 涂蜡×4」排列。
        // 未注册的变体（例如装 C&C 时的铜锭变体，或关闭 vbp_copper_oxidation 时）不会出现在这里。
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            for (DeferredItem<Item> variant : CopperEquipmentRegistry.all().values()) {
                addVariant(output, variant);
            }
        }

        // ---------- 5. 更好的末地：矛 ----------
        addVariant(output, THALLASIUM_SPEAR);
        addVariant(output, TERMINITE_SPEAR);
        addVariant(output, AETERNIUM_SPEAR);

        // ---------- 6. 更好的末地：矛头 ----------
        addVariant(output, THALLASIUM_SPEAR_HEAD);
        addVariant(output, TERMINITE_SPEAR_HEAD);
        addVariant(output, AETERNIUM_SPEAR_HEAD);

        // ---------- 7. 更好的末地：马铠 ----------
        addVariant(output, HorseArmorRegistry.THALLASIUM_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.TERMINITE_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.AETERNIUM_HORSE_ARMOR);

        // ---------- 8. 更好的末地：鹦鹉螺铠 ----------
        addVariant(output, NautilusArmorRegistry.THALLASIUM_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.TERMINITE_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.AETERNIUM_NAUTILUS_ARMOR);
    }

    private static void addVariant(CreativeModeTab.Output output, DeferredItem<Item> item) {
        if (item != null) output.accept(item.get());
    }

    // ==================== 长矛构造 ====================

    private static Item makeCopperSpear() {
        return makeSpear(copperTier(), 0.87F,
                0.95F, 0.6F, 2.5F, 8.0F, 4.5F, 5.1F, 11.25F, 4.6F);
    }

    private static Item makeSpear(Tier material, float attackSpeed,
                                  float chargeDamageMultiplier, float chargeDelay,
                                  float maxDismount, float minDismount,
                                  float maxKnockback, float minKnockback,
                                  float maxChargeDamage, float minChargeDamage) {
        return new SpearItem(material, 1.0F / attackSpeed,
                chargeDamageMultiplier, chargeDelay,
                maxDismount, minDismount,
                maxKnockback, minKnockback,
                maxChargeDamage, minChargeDamage,
                new Item.Properties());
    }

    private static Tier oreganizedSilverTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_IRON_TOOL,
                191, 6.0F, 2.0F, 14,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("oreganized", "silver_ingot")))
        );
    }

    private static Tier oreganizedElectrumTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_IRON_TOOL,
                1561, 8.0F, 3.0F, 14,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("oreganized", "electrum_ingot")))
        );
    }

    private static Tier terminiteTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                1230, 9.0F, 3.0F, 15,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("betterend", "terminite_ingot")))
        );
    }

    private static Tier aeterniumTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                2196, 9.0F, 5.5F, 15,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("betterend", "aeternium_ingot")))
        );
    }

    private static Tier thallasiumTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_IRON_TOOL,
                320, 6.0F, 1.5F, 14,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("betterend", "thallasium_ingot")))
        );
    }

    private static Tier candcSilverTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_IRON_TOOL,
                157, 6.0F, 1.0F, 25,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "silver_ingot")))
        );
    }

    private static Tier necromiumTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                2031, 9.0F, 3.0F, 15,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "necromium_ingot")))
        );
    }

    private static Tier copperTier() {
        return new SimpleTier(
                BlockTags.INCORRECT_FOR_STONE_TOOL,
                3191, 5.0F, 3.0F, 14,
                () -> Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/all_copper")))
        );
    }

    // ==================== 默认组件 ====================

    private void onModifyComponents(ModifyDefaultComponentsEvent event) {
        if (AETERNIUM_SPEAR != null) {
            event.modify(AETERNIUM_SPEAR.get(),
                    builder -> builder.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
        }
        if (AETERNIUM_SPEAR_HEAD != null) {
            event.modify(AETERNIUM_SPEAR_HEAD.get(),
                    builder -> builder.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE));
        }
    }
}
