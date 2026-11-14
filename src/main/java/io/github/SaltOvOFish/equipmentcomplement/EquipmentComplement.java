package io.github.SaltOvOFish.equipmentcomplement;

import com.mojang.serialization.MapCodec;
import io.github.SaltOvOFish.equipmentcomplement.client.ClientSetupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 装备增补（Equipment Complement）主入口。
 *
 * <p>构造流程按模块拆分：{@link SpearRegistry#init()}（长矛与矛头）、
 * {@link HorseArmorRegistry#init}（马铠）、{@link NautilusArmorRegistry#init}（鹦鹉螺铠）、
 * {@link CopperEquipmentRegistry#init}（VBP 铜制品变体）、{@link #registerEquipmentTab()}（自有标签页）；
 * 跨命名空间的物品注册表统一由 {@link ItemRegistries} 管理。</p>
 */
@Mod(EquipmentComplement.MOD_ID)
public class EquipmentComplement {

    public static final String MOD_ID = "equipment_complement";

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    /** 数据包条件（equipment_complement:config），供按配置启停配方使用。 */
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CopperWaxingRecipe>> COPPER_WAXING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crafting_special_copper_waxing", () -> new SimpleCraftingRecipeSerializer<>(CopperWaxingRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CopperScrapingRecipe>> COPPER_SCRAPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crafting_special_copper_scraping", () -> new SimpleCraftingRecipeSerializer<>(CopperScrapingRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CopperOxidizingRecipe>> COPPER_OXIDIZING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crafting_special_copper_oxidizing", () -> new SimpleCraftingRecipeSerializer<>(CopperOxidizingRecipe::new));

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT_TAB;

    public EquipmentComplement(ModContainer container, IEventBus modEventBus) {
        container.registerConfig(ModConfig.Type.STARTUP, EquipmentConfig.SPEC);

        // ===== 各模块的物品 / 材质注册 =====
        SpearRegistry.init();                         // 长矛（含 C&C 铜矛的氧化 / 涂蜡变体）
        HorseArmorRegistry.init(modEventBus);         // 马铠
        NautilusArmorRegistry.init(modEventBus);      // 鹦鹉螺铠
        CopperEquipmentRegistry.init(modEventBus);    // VBP 铜制品的氧化 / 涂蜡变体（minecraft 命名空间）
        registerEquipmentTab();                       // 自有创造模式标签页

        ItemRegistries.registerAll(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        CONDITIONS.register("config", () -> ConfigCondition.CODEC);
        CONDITIONS.register(modEventBus);
        TABS.register(modEventBus);

        modEventBus.addListener(CreativeTabEvents::onCreativeTab);
        modEventBus.addListener(EquipmentPackEvents::onAddPackFinders);
        modEventBus.addListener(SpearRegistry::onModifyComponents);
        NeoForge.EVENT_BUS.addListener(VillagerTradeEvents::onVillagerTrades);

        if (FMLEnvironment.dist.isClient()) {
            ClientSetupEvents.setup(modEventBus);
        }
    }

    // ==================== 自有创造模式标签页 ====================

    private static void registerEquipmentTab() {
        if (!EquipmentConfig.EQUIPMENT_TAB.get()) return;
        EQUIPMENT_TAB = TABS.register("equipment_tab", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.equipment_complement.equipment_tab"))
                .icon(() -> {
                    if (SpearRegistry.OREGANIZED_SILVER_SPEAR != null) return new ItemStack(SpearRegistry.OREGANIZED_SILVER_SPEAR.get());
                    if (SpearRegistry.CANDC_COPPER_SPEAR != null) return new ItemStack(SpearRegistry.CANDC_COPPER_SPEAR.get());
                    return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("iron_spear")));
                })
                .displayItems((params, output) -> addAllItems(output))
                .build());
    }

    /**
     * 「装备增补」标签页的内容，固定按下列顺序分组：
     * <ol>
     *   <li>长矛（不含更好的末地）</li>
     *   <li>马铠（不含更好的末地）</li>
     *   <li>鹦鹉螺铠（不含更好的末地）</li>
     *   <li>VanillaBackport 铜制品的氧化 / 涂蜡变体（仅 {@code copper_variants_in_tab} 开启时）</li>
     *   <li>更好的末地：矛 → 矛头 → 马铠 → 鹦鹉螺铠</li>
     * </ol>
     */
    private static void addAllItems(CreativeModeTab.Output output) {
        // ---------- 1. 长矛（不含更好的末地）----------
        addVariant(output, SpearRegistry.CANDC_COPPER_SPEAR);
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            addVariant(output, SpearRegistry.CANDC_EXPOSED_COPPER_SPEAR);
            addVariant(output, SpearRegistry.CANDC_WEATHERED_COPPER_SPEAR);
            addVariant(output, SpearRegistry.CANDC_OXIDIZED_COPPER_SPEAR);
            addVariant(output, SpearRegistry.CANDC_WAXED_COPPER_SPEAR);
            addVariant(output, SpearRegistry.CANDC_WAXED_EXPOSED_COPPER_SPEAR);
            addVariant(output, SpearRegistry.CANDC_WAXED_WEATHERED_COPPER_SPEAR);
            addVariant(output, SpearRegistry.CANDC_WAXED_OXIDIZED_COPPER_SPEAR);
        }
        addVariant(output, SpearRegistry.OREGANIZED_SILVER_SPEAR);
        addVariant(output, SpearRegistry.CANDC_SILVER_SPEAR);
        addVariant(output, SpearRegistry.OREGANIZED_ELECTRUM_SPEAR);
        addVariant(output, SpearRegistry.CANDC_NECROMIUM_SPEAR);
        addVariant(output, SpearRegistry.CURIOSITIES_ALUMINUM_SPEAR);
        addVariant(output, SpearRegistry.CURIOSITIES_INVAR_SPEAR);
        addVariant(output, SpearRegistry.LEGENDARY_MONSTERS_ENDERITIUM_SPEAR);

        // ---------- 2. 马铠（不含更好的末地）----------
        addVariant(output, HorseArmorRegistry.CHAINMAIL_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.SILVER_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.ELECTRUM_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.ALUMINUM_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.INVAR_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.SANGUINE_HORSE_ARMOR);

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
        addVariant(output, NautilusArmorRegistry.ALUMINUM_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.INVAR_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.SANGUINE_NAUTILUS_ARMOR);

        // ---------- 4. VanillaBackport 铜制品的氧化 / 涂蜡变体 ----------
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            for (DeferredItem<Item> variant : CopperEquipmentRegistry.all().values()) {
                output.accept(variant.get());
            }
        }

        // ---------- 5~8. 更好的末地 ----------
        addVariant(output, SpearRegistry.THALLASIUM_SPEAR);
        addVariant(output, SpearRegistry.TERMINITE_SPEAR);
        addVariant(output, SpearRegistry.AETERNIUM_SPEAR);

        addVariant(output, SpearRegistry.THALLASIUM_SPEAR_HEAD);
        addVariant(output, SpearRegistry.TERMINITE_SPEAR_HEAD);
        addVariant(output, SpearRegistry.AETERNIUM_SPEAR_HEAD);

        addVariant(output, HorseArmorRegistry.THALLASIUM_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.TERMINITE_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR);
        addVariant(output, HorseArmorRegistry.AETERNIUM_HORSE_ARMOR);

        addVariant(output, NautilusArmorRegistry.THALLASIUM_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.TERMINITE_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR);
        addVariant(output, NautilusArmorRegistry.AETERNIUM_NAUTILUS_ARMOR);
    }

    private static void addVariant(CreativeModeTab.Output output, DeferredItem<Item> item) {
        if (item != null) output.accept(item.get());
    }
}
