package io.github.SaltOvOFish.equipmentcomplement;

import com.blackgear.vanillabackport.common.level.items.NautilusArmorItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 鹦鹉螺铠注册。
 *
 * <p>鹦鹉螺铠（Nautilus Armor）是 VanillaBackport 随「Mounts of Mayhem」移植的坐骑装备，
 * 由 {@link NautilusArmorItem} 实现：它继承 {@code ArmorItem}（Type.BODY），
 * 渲染用的实体贴图路径由**护甲材质的注册名**推导：
 * {@code <材质命名空间>:textures/entity/nautilus/armor/<材质路径>.png}，
 * 并在鹦鹉螺渲染器的盔甲层上绘制。因此新增材质的鹦鹉螺铠，
 * 只需要「注册同名 ArmorMaterial + 注册 NautilusArmorItem + 放好对应路径贴图」三步。</p>
 *
 * <p>护甲材质统一由 {@link ArmorMaterials} 构造（贴图层命名空间 = 材质注册表命名空间，
 * 正好与上面的贴图路径推导规则一致）。</p>
 *
 * <p>铜鹦鹉螺铠的氧化/涂蜡变体（斑驳/锈蚀/氧化/涂蜡×4）复用未氧化材质的贴图，
 * 因此上面的「涂蜡」系列物品共用未涂蜡的 ArmorMaterial；氧化与涂蜡机制见
 * {@link CopperOxidation} / {@link CopperWaxingRecipe}。</p>
 *
 * <p>融晶（crystalite）鹦鹉螺铠护甲为小数 9.4，材质防御全为 0，
 * 实际数值由 {@link ArmorAttributeEvents} 写入。</p>
 */
public final class NautilusArmorRegistry {

    private static final DeferredRegister<ArmorMaterial> MINECRAFT_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, "minecraft");
    private static final DeferredRegister<ArmorMaterial> OREGANIZED_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, "oreganized");
    private static final DeferredRegister<ArmorMaterial> CAVERNS_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, "caverns_and_chasms");
    private static final DeferredRegister<ArmorMaterial> BETTEREND_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, "betterend");

    private static final DeferredRegister.Items MINECRAFT_ITEMS = DeferredRegister.createItems("minecraft");
    private static final DeferredRegister.Items OREGANIZED_ITEMS = DeferredRegister.createItems("oreganized");
    private static final DeferredRegister.Items CAVERNS_ITEMS = DeferredRegister.createItems("caverns_and_chasms");
    private static final DeferredRegister.Items BETTEREND_ITEMS = DeferredRegister.createItems("betterend");

    // ===== Minecraft =====
    public static DeferredItem<Item> LEATHER_NAUTILUS_ARMOR;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CHAINMAIL_MATERIAL;
    public static DeferredItem<Item> CHAINMAIL_NAUTILUS_ARMOR;

    // ===== Oreganized =====
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> OREGANIZED_SILVER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> OREGANIZED_ELECTRUM_MATERIAL;
    public static DeferredItem<Item> OREGANIZED_SILVER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> OREGANIZED_ELECTRUM_NAUTILUS_ARMOR;

    // ===== Caverns & Chasms =====
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CANDC_SILVER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CANDC_COPPER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CANDC_EXPOSED_COPPER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CANDC_WEATHERED_COPPER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CANDC_OXIDIZED_COPPER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CANDC_NECROMIUM_MATERIAL;
    public static DeferredItem<Item> CANDC_SILVER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_EXPOSED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_WEATHERED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_OXIDIZED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_WAXED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_WAXED_EXPOSED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_WAXED_WEATHERED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_WAXED_OXIDIZED_COPPER_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CANDC_NECROMIUM_NAUTILUS_ARMOR;

    // ===== BetterEnd =====
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> THALLASIUM_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> TERMINITE_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> AETERNIUM_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CRYSTALITE_MATERIAL;
    public static DeferredItem<Item> THALLASIUM_NAUTILUS_ARMOR;
    public static DeferredItem<Item> TERMINITE_NAUTILUS_ARMOR;
    public static DeferredItem<Item> AETERNIUM_NAUTILUS_ARMOR;
    public static DeferredItem<Item> CRYSTALITE_NAUTILUS_ARMOR;

    private NautilusArmorRegistry() {
    }

    public static void init(IEventBus modEventBus) {
        if (EquipmentConfig.ENABLE_LEATHER_NAUTILUS_ARMOR.get()) {
            // 直接复用原版皮革护甲材质：BODY 防御正好是 3，附魔值 15、皮革修复、皮革穿戴音效也都与皮革盔甲一致；
            // 并且材质注册名是 minecraft:leather，纹理路径自然落在
            // minecraft:textures/entity/nautilus/armor/leather.png，与已提供的贴图一致。
            LEATHER_NAUTILUS_ARMOR = nautilusArmor(MINECRAFT_ITEMS, "leather_nautilus_armor",
                    net.minecraft.world.item.ArmorMaterials.LEATHER, false);
        }
        if (EquipmentConfig.ENABLE_CHAINMAIL_NAUTILUS_ARMOR.get()) {
            CHAINMAIL_MATERIAL = ArmorMaterials.register(MINECRAFT_MATERIALS, "chainmail_nautilus_armor",
                    5, 12, SoundEvents.ARMOR_EQUIP_CHAIN, "minecraft", "iron_ingot", 0.0F, 0.0F);
            CHAINMAIL_NAUTILUS_ARMOR = nautilusArmor(MINECRAFT_ITEMS, "chainmail_nautilus_armor",
                    CHAINMAIL_MATERIAL, false);
        }

        if (ModList.get().isLoaded("oreganized")) {
            if (EquipmentConfig.ENABLE_OREGANIZED_SILVER_NAUTILUS_ARMOR.get()) {
                OREGANIZED_SILVER_MATERIAL = ArmorMaterials.register(OREGANIZED_MATERIALS, "silver_nautilus_armor",
                        6, 25, SoundEvents.ARMOR_EQUIP_GOLD, "oreganized", "silver_ingot", 0.0F, 0.0F);
                OREGANIZED_SILVER_NAUTILUS_ARMOR = nautilusArmor(OREGANIZED_ITEMS, "silver_nautilus_armor",
                        OREGANIZED_SILVER_MATERIAL, false);
            }
            if (EquipmentConfig.ENABLE_OREGANIZED_ELECTRUM_NAUTILUS_ARMOR.get()) {
                OREGANIZED_ELECTRUM_MATERIAL = ArmorMaterials.register(OREGANIZED_MATERIALS, "electrum_nautilus_armor",
                        19, 22, SoundEvents.ARMOR_EQUIP_GOLD, "oreganized", "electrum_ingot", 2.0F, 0.0F);
                OREGANIZED_ELECTRUM_NAUTILUS_ARMOR = nautilusArmor(OREGANIZED_ITEMS, "electrum_nautilus_armor",
                        OREGANIZED_ELECTRUM_MATERIAL, false);
            }
        }

        if (ModList.get().isLoaded("caverns_and_chasms")) {
            if (EquipmentConfig.ENABLE_CANDC_SILVER_NAUTILUS_ARMOR.get()) {
                CANDC_SILVER_MATERIAL = ArmorMaterials.register(CAVERNS_MATERIALS, "silver_nautilus_armor",
                        6, 25, SoundEvents.ARMOR_EQUIP_GOLD, "caverns_and_chasms", "silver_ingot", 0.0F, 0.0F);
                CANDC_SILVER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "silver_nautilus_armor",
                        CANDC_SILVER_MATERIAL, false);
            }
            if (EquipmentConfig.ENABLE_CANDC_COPPER_NAUTILUS_ARMOR.get()) {
                CANDC_COPPER_MATERIAL = copperMaterial("copper_nautilus_armor");
                CANDC_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "copper_nautilus_armor",
                        CANDC_COPPER_MATERIAL, false);
                if (EquipmentConfig.CANDC_COPPER_OXIDATION.get()) {
                    CANDC_EXPOSED_COPPER_MATERIAL = copperMaterial("exposed_copper_nautilus_armor");
                    CANDC_EXPOSED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "exposed_copper_nautilus_armor",
                            CANDC_EXPOSED_COPPER_MATERIAL, false);
                    CANDC_WEATHERED_COPPER_MATERIAL = copperMaterial("weathered_copper_nautilus_armor");
                    CANDC_WEATHERED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "weathered_copper_nautilus_armor",
                            CANDC_WEATHERED_COPPER_MATERIAL, false);
                    CANDC_OXIDIZED_COPPER_MATERIAL = copperMaterial("oxidized_copper_nautilus_armor");
                    CANDC_OXIDIZED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "oxidized_copper_nautilus_armor",
                            CANDC_OXIDIZED_COPPER_MATERIAL, false);
                    // 涂蜡系列共用未涂蜡材质（贴图与属性都一致）
                    CANDC_WAXED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "waxed_copper_nautilus_armor",
                            CANDC_COPPER_MATERIAL, false);
                    CANDC_WAXED_EXPOSED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "waxed_exposed_copper_nautilus_armor",
                            CANDC_EXPOSED_COPPER_MATERIAL, false);
                    CANDC_WAXED_WEATHERED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "waxed_weathered_copper_nautilus_armor",
                            CANDC_WEATHERED_COPPER_MATERIAL, false);
                    CANDC_WAXED_OXIDIZED_COPPER_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "waxed_oxidized_copper_nautilus_armor",
                            CANDC_OXIDIZED_COPPER_MATERIAL, false);
                }
            }
            if (EquipmentConfig.ENABLE_CANDC_NECROMIUM_NAUTILUS_ARMOR.get()) {
                CANDC_NECROMIUM_MATERIAL = ArmorMaterials.register(CAVERNS_MATERIALS, "necromium_nautilus_armor",
                        19, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, "caverns_and_chasms", "necromium_ingot", 2.0F, 0.0F);
                CANDC_NECROMIUM_NAUTILUS_ARMOR = nautilusArmor(CAVERNS_ITEMS, "necromium_nautilus_armor",
                        CANDC_NECROMIUM_MATERIAL, false);
            }
        }

        if (ModList.get().isLoaded("betterend")) {
            if (EquipmentConfig.ENABLE_THALLASIUM_NAUTILUS_ARMOR.get()) {
                THALLASIUM_MATERIAL = ArmorMaterials.register(BETTEREND_MATERIALS, "thallasium_nautilus_armor",
                        5, 14, SoundEvents.ARMOR_EQUIP_IRON, "betterend", "thallasium_ingot", 0.0F, 0.0F);
                THALLASIUM_NAUTILUS_ARMOR = nautilusArmor(BETTEREND_ITEMS, "thallasium_nautilus_armor",
                        THALLASIUM_MATERIAL, false);
            }
            if (EquipmentConfig.ENABLE_TERMINITE_NAUTILUS_ARMOR.get()) {
                TERMINITE_MATERIAL = ArmorMaterials.register(BETTEREND_MATERIALS, "terminite_nautilus_armor",
                        10, 15, SoundEvents.ARMOR_EQUIP_DIAMOND, "betterend", "terminite_ingot", 1.0F, 0.05F);
                TERMINITE_NAUTILUS_ARMOR = nautilusArmor(BETTEREND_ITEMS, "terminite_nautilus_armor",
                        TERMINITE_MATERIAL, false);
            }
            if (EquipmentConfig.ENABLE_AETERNIUM_NAUTILUS_ARMOR.get()) {
                AETERNIUM_MATERIAL = ArmorMaterials.register(BETTEREND_MATERIALS, "aeternium_nautilus_armor",
                        20, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, "betterend", "aeternium_ingot", 3.5F, 0.2F);
                AETERNIUM_NAUTILUS_ARMOR = nautilusArmor(BETTEREND_ITEMS, "aeternium_nautilus_armor",
                        AETERNIUM_MATERIAL, true);
            }
            if (EquipmentConfig.ENABLE_CRYSTALITE_NAUTILUS_ARMOR.get()) {
                CRYSTALITE_MATERIAL = ArmorMaterials.register(BETTEREND_MATERIALS, "crystalite_nautilus_armor",
                        0, 15, SoundEvents.ARMOR_EQUIP_DIAMOND, "betterend", "eternal_crystal", 0.0F, 0.0F);
                CRYSTALITE_NAUTILUS_ARMOR = nautilusArmor(BETTEREND_ITEMS, "crystalite_nautilus_armor",
                        CRYSTALITE_MATERIAL, false);
            }
        }

        MINECRAFT_MATERIALS.register(modEventBus);
        OREGANIZED_MATERIALS.register(modEventBus);
        CAVERNS_MATERIALS.register(modEventBus);
        BETTEREND_MATERIALS.register(modEventBus);
        MINECRAFT_ITEMS.register(modEventBus);
        OREGANIZED_ITEMS.register(modEventBus);
        CAVERNS_ITEMS.register(modEventBus);
        BETTEREND_ITEMS.register(modEventBus);
    }

    // ===== 辅助方法 =====
    /** C&C 铜鹦鹉螺铠用的护甲材质：穿戴音效使用 C&C 自己的铜装备音效（装不上 C&C 时这些物品本来也不会注册）。 */
    private static DeferredHolder<ArmorMaterial, ArmorMaterial> copperMaterial(String name) {
        return ArmorMaterials.register(CAVERNS_MATERIALS, name,
                4, 8, EquipmentSounds.cavernsAndChasmsCopper(), "minecraft", "copper_ingot", 0.0F, 0.0F);
    }

    private static DeferredItem<Item> nautilusArmor(DeferredRegister.Items items, String name,
            Holder<ArmorMaterial> material, boolean fireproof) {
        Item.Properties properties = fireproof
                ? new Item.Properties().stacksTo(1).fireResistant()
                : new Item.Properties().stacksTo(1);
        return items.register(name, () -> new NautilusArmorItem(material, properties));
    }
}
