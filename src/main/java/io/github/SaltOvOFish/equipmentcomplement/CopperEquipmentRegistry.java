package io.github.SaltOvOFish.equipmentcomplement;

import com.blackgear.vanillabackport.common.level.items.NautilusArmorItem;
import com.blackgear.vanillabackport.common.level.items.spear.SpearItem;
import com.blackgear.vanillabackport.common.registries.items.ModArmorMaterials;
import com.blackgear.vanillabackport.common.registries.items.ModToolMaterials;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * VanillaBackport 铜制品的氧化 / 涂蜡变体物品（命名空间 {@code minecraft}）。
 *
 * <p>VBP 本身没有任何物品氧化机制（它只有方块风化），所以这里按 Caverns &amp; Chasms 的铜套具设计
 * 自己实现一整套：每件基础物品再加 7 个变体——3 个氧化阶段（斑驳 / 锈蚀 / 氧化）+
 * 4 个涂蜡版本（涂蜡 / 涂蜡斑驳 / 涂蜡锈蚀 / 涂蜡氧化），共 8 个阶段。</p>
 *
 * <p><b>本类只负责物品注册</b>（氧化推进与涂蜡配方见后续步骤）。变体全部使用与 VBP 铜制品完全相同的
 * 数值：工具用 VBP 的 {@link ModToolMaterials#COPPER}，盔甲用铜护甲材质的面板值，
 * 铜矛参数与 {@code minecraft:copper_spear} 一致，铜锭为普通物品。</p>
 *
 * <p>盔甲的穿戴贴图与坐骑铠的实体贴图取自护甲材质的注册名，所以三个氧化阶段各有独立材质
 * （{@code minecraft:exposed_copper} / {@code weathered_copper} / {@code oxidized_copper}，
 * 面板数值与铜相同，仅贴图层不同）；涂蜡物品复用所属阶段的材质与贴图。</p>
 */
public final class CopperEquipmentRegistry {

    /** 13 件基础铜制品（基础 id = {@code minecraft:copper_<piece>}）。 */
    public static final List<String> PIECES = List.of(
            "sword", "pickaxe", "axe", "shovel", "hoe",
            "helmet", "chestplate", "leggings", "boots",
            "horse_armor", "nautilus_armor", "spear", "ingot");

    /** 7 个变体前缀（不含基础阶段）。 */
    public static final List<String> VARIANTS = List.of(
            "exposed", "weathered", "oxidized",
            "waxed", "waxed_exposed", "waxed_weathered", "waxed_oxidized");

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("minecraft");
    private static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, "minecraft");

    /** 氧化阶段的护甲材质（基础阶段 {@code minecraft:copper} 由 VBP 提供）。 */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EXPOSED_COPPER_MATERIAL = copperMaterial("exposed_copper");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WEATHERED_COPPER_MATERIAL = copperMaterial("weathered_copper");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> OXIDIZED_COPPER_MATERIAL = copperMaterial("oxidized_copper");

    /** 变体 id（如 {@code exposed_copper_sword}）→ 物品。 */
    private static final Map<String, DeferredItem<Item>> REGISTERED = new LinkedHashMap<>();

    private CopperEquipmentRegistry() {
    }

    public static void init(IEventBus modEventBus) {
        if (!EquipmentConfig.VBP_COPPER_OXIDATION.get()) return;
        // 装了 C&C 时不注册我们自己的七种钢银变种：C&C 本体已经有同概念的变种物品
        // （caverns_and_chasms:(waxed_)(exposed|weathered|oxidized)_copper_ingot），合成配方也优先用它们的。
        boolean cavernsAndChasms = ModList.get().isLoaded("caverns_and_chasms");
        for (String piece : PIECES) {
            if (cavernsAndChasms && piece.equals("ingot")) continue;
            for (String variant : VARIANTS) {
                String id = variant + "_copper_" + piece;
                REGISTERED.put(id, ITEMS.register(id, () -> create(piece, variant)));
            }
        }
        MATERIALS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    // ==================== 查询 ====================

    public static DeferredItem<Item> get(String id) {
        return REGISTERED.get(id);
    }

    public static Map<String, DeferredItem<Item>> all() {
        return Collections.unmodifiableMap(REGISTERED);
    }

    /** 铜矛的 7 个变体，供客户端手持渲染器注册。 */
    public static Set<ItemLike> spears() {
        Set<ItemLike> set = new LinkedHashSet<>();
        for (String variant : VARIANTS) {
            DeferredItem<Item> item = REGISTERED.get(variant + "_copper_spear");
            if (item != null) set.add(item.get());
        }
        return set;
    }

    // ==================== 物品构造 ====================

    private static Item create(String piece, String variant) {
        Item.Properties props = new Item.Properties();
        Holder<ArmorMaterial> material = materialFor(variant);
        return switch (piece) {
            case "sword" -> new SwordItem(ModToolMaterials.COPPER,
                    props.attributes(SwordItem.createAttributes(ModToolMaterials.COPPER, 3.0F, -2.4F)));
            case "shovel" -> new ShovelItem(ModToolMaterials.COPPER,
                    props.attributes(ShovelItem.createAttributes(ModToolMaterials.COPPER, 1.5F, -3.0F)));
            case "pickaxe" -> new PickaxeItem(ModToolMaterials.COPPER,
                    props.attributes(PickaxeItem.createAttributes(ModToolMaterials.COPPER, 1.0F, -2.8F)));
            case "axe" -> new AxeItem(ModToolMaterials.COPPER,
                    props.attributes(AxeItem.createAttributes(ModToolMaterials.COPPER, 7.0F, -3.2F)));
            case "hoe" -> new HoeItem(ModToolMaterials.COPPER,
                    props.attributes(HoeItem.createAttributes(ModToolMaterials.COPPER, -1.0F, -2.0F)));
            case "helmet" -> new ArmorItem(material, ArmorItem.Type.HELMET, props);
            case "chestplate" -> new ArmorItem(material, ArmorItem.Type.CHESTPLATE, props);
            case "leggings" -> new ArmorItem(material, ArmorItem.Type.LEGGINGS, props);
            case "boots" -> new ArmorItem(material, ArmorItem.Type.BOOTS, props);
            case "horse_armor" -> new CopperHorseArmorItem(material, textureStageOf(variant), props.stacksTo(1));
            case "nautilus_armor" -> new NautilusArmorItem(material, props.stacksTo(1));
            // 参数与 minecraft:copper_spear 完全一致（第一位是攻击间隔）
            case "spear" -> new SpearItem(ModToolMaterials.COPPER, 0.85F, 0.82F, 0.65F, 4.0F, 9.0F, 8.25F, 5.1F, 12.5F, 4.6F, props);
            case "ingot" -> new Item(props);
            default -> throw new IllegalArgumentException("未知铜制品：" + piece);
        };
    }

    /** 变体所属的氧化阶段材质；涂蜡版本复用其所处阶段的材质（与 C&amp;C 一致）。 */
    private static Holder<ArmorMaterial> materialFor(String variant) {
        return switch (stageOf(variant)) {
            case "exposed" -> EXPOSED_COPPER_MATERIAL;
            case "weathered" -> WEATHERED_COPPER_MATERIAL;
            case "oxidized" -> OXIDIZED_COPPER_MATERIAL;
            default -> ModArmorMaterials.COPPER;
        };
    }

    /** 去掉涂蜡前缀后的阶段名：{@code exposed} / {@code weathered} / {@code oxidized}，基础阶段为 {@code copper}。 */
    private static String stageOf(String variant) {
        String stage = variant.startsWith("waxed_") ? variant.substring("waxed_".length()) : variant;
        return stage.equals("waxed") ? "copper" : stage;
    }

    /** 坐骑铠实体贴图用的阶段名：铜 → {@code copper}，其余 → {@code <阶段>_copper}。 */
    private static String textureStageOf(String variant) {
        String stage = stageOf(variant);
        return stage.equals("copper") ? "copper" : stage + "_copper";
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> copperMaterial(String name) {
        return MATERIALS.register(name, () -> new ArmorMaterial(
                defense(1, 3, 4, 2, 4), 8, EquipmentSounds.vanillabackportCopper(),
                () -> Ingredient.of(Items.COPPER_INGOT),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace(name))),
                0.0F, 0.0F));
    }

    private static EnumMap<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet, int body) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, boots);
            map.put(ArmorItem.Type.LEGGINGS, leggings);
            map.put(ArmorItem.Type.CHESTPLATE, chestplate);
            map.put(ArmorItem.Type.HELMET, helmet);
            map.put(ArmorItem.Type.BODY, body);
        });
    }

    /** 铜马铠的变体：贴图按阶段取 {@code textures/entity/horse/armor/horse_armor_<阶段>.png}。 */
    private static final class CopperHorseArmorItem extends AnimalArmorItem {

        private final ResourceLocation texture;

        private CopperHorseArmorItem(Holder<ArmorMaterial> material, String stage, Item.Properties properties) {
            super(material, AnimalArmorItem.BodyType.EQUESTRIAN, false, properties);
            this.texture = ResourceLocation.fromNamespaceAndPath("minecraft",
                    "textures/entity/horse/armor/horse_armor_" + stage + ".png");
        }

        @Override
        public ResourceLocation getTexture() {
            return this.texture;
        }
    }
}
