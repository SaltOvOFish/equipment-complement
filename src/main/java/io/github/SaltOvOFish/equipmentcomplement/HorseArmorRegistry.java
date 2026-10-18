package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 马铠注册。
 *
 * <p>补充原版缺失的锁链马铠，并为 Oreganized / BetterEnd 提供兼容马铠。</p>
 *
 * <p>注意：融晶（crystalite）马铠的护甲是小数（9.4），而 {@link ArmorMaterial} 的
 * 防御值是 int，因此其材质防御为 0，实际护甲/韧性/击退抗性由
 * {@link ArmorAttributeEvents} 用属性修正写入。</p>
 *
 * <p>护甲材质统一由 {@link ArmorMaterials} 构造（「只有 BODY 有防御值 + 单贴图层」）。</p>
 */
public final class HorseArmorRegistry {

    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, EquipmentComplement.MOD_ID);

    private static final DeferredRegister.Items MINECRAFT_ITEMS = DeferredRegister.createItems("minecraft");
    private static final DeferredRegister.Items OREGANIZED_ITEMS = DeferredRegister.createItems("oreganized");
    private static final DeferredRegister.Items BETTEREND_ITEMS = DeferredRegister.createItems("betterend");

    // ===== 材质 =====
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CHAINMAIL_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> SILVER_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> ELECTRUM_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> THALLASIUM_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> TERMINITE_MATERIAL;
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> AETERNIUM_MATERIAL;
    /** 融晶：防御值走 {@link ArmorAttributeEvents}，材质本身不提供面板数值。 */
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> CRYSTALITE_MATERIAL;

    // ===== 马铠物品 =====
    public static DeferredItem<Item> CHAINMAIL_HORSE_ARMOR;
    public static DeferredItem<Item> SILVER_HORSE_ARMOR;
    public static DeferredItem<Item> ELECTRUM_HORSE_ARMOR;
    public static DeferredItem<Item> THALLASIUM_HORSE_ARMOR;
    public static DeferredItem<Item> TERMINITE_HORSE_ARMOR;
    public static DeferredItem<Item> AETERNIUM_HORSE_ARMOR;
    public static DeferredItem<Item> CRYSTALITE_HORSE_ARMOR;

    private HorseArmorRegistry() {
    }

    public static void init(IEventBus modEventBus) {
        if (EquipmentConfig.ENABLE_CHAINMAIL_HORSE_ARMOR.get()) {
            CHAINMAIL_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "chainmail_horse_armor",
                    5, 12, SoundEvents.ARMOR_EQUIP_CHAIN, "minecraft", "iron_ingot", 0.0F, 0.0F);
            CHAINMAIL_HORSE_ARMOR = horseArmor(MINECRAFT_ITEMS, "chainmail_horse_armor",
                    CHAINMAIL_MATERIAL, "horse_armor_chainmail.png", false);
        }

        if (ModList.get().isLoaded("oreganized")) {
            if (EquipmentConfig.ENABLE_SILVER_HORSE_ARMOR.get()) {
                SILVER_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "silver_horse_armor",
                        6, 25, SoundEvents.ARMOR_EQUIP_GOLD, "oreganized", "silver_ingot", 0.0F, 0.0F);
                SILVER_HORSE_ARMOR = horseArmor(OREGANIZED_ITEMS, "silver_horse_armor",
                        SILVER_MATERIAL, "horse_armor_silver.png", false);
            }
            if (EquipmentConfig.ENABLE_ELECTRUM_HORSE_ARMOR.get()) {
                ELECTRUM_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "electrum_horse_armor",
                        19, 22, SoundEvents.ARMOR_EQUIP_GOLD, "oreganized", "electrum_ingot", 2.0F, 0.0F);
                ELECTRUM_HORSE_ARMOR = horseArmor(OREGANIZED_ITEMS, "electrum_horse_armor",
                        ELECTRUM_MATERIAL, "horse_armor_electrum.png", false);
            }
        }

        if (ModList.get().isLoaded("betterend")) {
            if (EquipmentConfig.ENABLE_THALLASIUM_HORSE_ARMOR.get()) {
                THALLASIUM_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "thallasium_horse_armor",
                        5, 14, SoundEvents.ARMOR_EQUIP_IRON, "betterend", "thallasium_ingot", 0.0F, 0.0F);
                THALLASIUM_HORSE_ARMOR = horseArmor(BETTEREND_ITEMS, "thallasium_horse_armor",
                        THALLASIUM_MATERIAL, "horse_armor_thallasium.png", false);
            }
            if (EquipmentConfig.ENABLE_TERMINITE_HORSE_ARMOR.get()) {
                TERMINITE_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "terminite_horse_armor",
                        10, 15, SoundEvents.ARMOR_EQUIP_DIAMOND, "betterend", "terminite_ingot", 1.0F, 0.05F);
                TERMINITE_HORSE_ARMOR = horseArmor(BETTEREND_ITEMS, "terminite_horse_armor",
                        TERMINITE_MATERIAL, "horse_armor_terminite.png", false);
            }
            if (EquipmentConfig.ENABLE_AETERNIUM_HORSE_ARMOR.get()) {
                AETERNIUM_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "aeternium_horse_armor",
                        20, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, "betterend", "aeternium_ingot", 3.5F, 0.2F);
                AETERNIUM_HORSE_ARMOR = horseArmor(BETTEREND_ITEMS, "aeternium_horse_armor",
                        AETERNIUM_MATERIAL, "horse_armor_aeternium.png", true);
            }
            if (EquipmentConfig.ENABLE_CRYSTALITE_HORSE_ARMOR.get()) {
                CRYSTALITE_MATERIAL = ArmorMaterials.register(ARMOR_MATERIALS, "crystalite_horse_armor",
                        0, 15, SoundEvents.ARMOR_EQUIP_DIAMOND, "betterend", "eternal_crystal", 0.0F, 0.0F);
                CRYSTALITE_HORSE_ARMOR = horseArmor(BETTEREND_ITEMS, "crystalite_horse_armor",
                        CRYSTALITE_MATERIAL, "horse_armor_crystalite.png", false);
            }
        }

        ARMOR_MATERIALS.register(modEventBus);
        MINECRAFT_ITEMS.register(modEventBus);
        OREGANIZED_ITEMS.register(modEventBus);
        BETTEREND_ITEMS.register(modEventBus);
    }

    // ===== 辅助方法 =====
    /** 马铠：坐骑实体贴图固定为 {@code <命名空间>:textures/entity/horse/armor/<texture>}。 */
    private static DeferredItem<Item> horseArmor(DeferredRegister.Items items, String name,
            DeferredHolder<ArmorMaterial, ArmorMaterial> material, String texture, boolean fireproof) {
        Item.Properties props = new Item.Properties().stacksTo(1);
        if (fireproof) props = props.fireResistant();
        Item.Properties finalProps = props;
        String namespace = items.getNamespace();
        return items.register(name, () -> new AnimalArmorItem(material, AnimalArmorItem.BodyType.EQUESTRIAN, false, finalProps) {
            @Override
            public ResourceLocation getTexture() {
                return ResourceLocation.fromNamespaceAndPath(namespace, "textures/entity/horse/armor/" + texture);
            }
        });
    }
}
