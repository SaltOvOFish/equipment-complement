package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 本模组物品（长矛 + 马铠 + 鹦鹉螺铠）的属性修饰符派发。
 *
 * <p>包含两部分：</p>
 * <ul>
 *     <li><b>附加效果</b>：在基础属性之上叠加跨模组或特殊属性
 *         （Oreganized 无敌帧、Caverns &amp; Chasms 魔法保护 / 缓慢回馈、Curiosities 交互距离 / 护甲穿透 / 伤害减免、旅行者/琥珀金移速与跳跃）。</li>
 *     <li><b>精确覆盖</b>：融晶铠甲的护甲是小数、C&amp;C 死灵合金 / 下界合金马铠需要重写面板，
 *         这些用 {@code clearModifiers()} 清空后重写，避免叠加。</li>
 * </ul>
 *
 * <p><b>性能</b>：本事件在每次生成带属性修饰符的物品 tooltip 时都会触发（悬停装备时每帧一次）：</p>
 * <ol>
 *   <li>物品派发只做一次 {@link IdentityHashMap} 查表；</li>
 *   <li>所有 {@link AttributeModifier}（含其 id）预先构造成 {@code static final} 常量，事件内零分配；</li>
 *   <li>跨模组属性只在首次建表时解析一次，之后由 lambda 捕获。</li>
 * </ol>
 */
@EventBusSubscriber(modid = EquipmentComplement.MOD_ID)
public class ItemAttributeEvents {

    private static final EquipmentSlotGroup BODY = EquipmentSlotGroup.BODY;
    private static final EquipmentSlotGroup MAINHAND = EquipmentSlotGroup.MAINHAND;

    // ==================== 预构造的属性修饰符（含 id 字符串，全部只构造一次） ====================
    private static final AttributeModifier SILVER_HORSE_ARMOR_INVINCIBILITY =
            add("silver_horse_armor_invincibility", 0.2D);
    private static final AttributeModifier ELECTRUM_HORSE_ARMOR_SPEED =
            multipliedBase("electrum_horse_armor_speed", 0.05D);
    private static final AttributeModifier CRYSTALITE_HORSE_ARMOR_ARMOR =
            add("crystalite_horse_armor_armor", 9.4D);
    private static final AttributeModifier CRYSTALITE_HORSE_ARMOR_TOUGHNESS =
            add("crystalite_horse_armor_toughness", 0.96D);
    private static final AttributeModifier NECROMIUM_HORSE_ARMOR_ARMOR =
            add("necromium_horse_armor_armor", 19.0D);
    private static final AttributeModifier NECROMIUM_HORSE_ARMOR_TOUGHNESS =
            add("necromium_horse_armor_toughness", 2.0D);
    private static final AttributeModifier NECROMIUM_HORSE_ARMOR_SLOWNESS =
            add("necromium_horse_armor_slowness_retribution", 1.0D);
    /** C&C 下界合金马铠：对齐 VanillaBackport 版本（护甲 19 / 韧性 3 / 抗击退 0.1）。 */
    private static final AttributeModifier NETHERITE_HORSE_ARMOR_ARMOR =
            add("netherite_horse_armor_armor", 19.0D);
    private static final AttributeModifier NETHERITE_HORSE_ARMOR_TOUGHNESS =
            add("netherite_horse_armor_toughness", 3.0D);
    private static final AttributeModifier NETHERITE_HORSE_ARMOR_KNOCKBACK =
            add("netherite_horse_armor_knockback_resistance", 0.1D);

    private static final AttributeModifier SILVER_NAUTILUS_ARMOR_INVINCIBILITY =
            add("silver_nautilus_armor_invincibility", 0.2D);
    private static final AttributeModifier SILVER_NAUTILUS_ARMOR_MAGIC_PROTECTION =
            add("silver_nautilus_armor_magic_protection", 0.4D);
    private static final AttributeModifier ELECTRUM_NAUTILUS_ARMOR_SPEED =
            multipliedBase("electrum_nautilus_armor_speed", 0.05D);
    private static final AttributeModifier NECROMIUM_NAUTILUS_ARMOR_SLOWNESS =
            add("necromium_nautilus_armor_slowness_retribution", 1.0D);
    private static final AttributeModifier CRYSTALITE_NAUTILUS_ARMOR_ARMOR =
            add("crystalite_nautilus_armor_armor", 9.4D);
    private static final AttributeModifier CRYSTALITE_NAUTILUS_ARMOR_TOUGHNESS =
            add("crystalite_nautilus_armor_toughness", 0.96D);
    /** C&C 铜鹦鹉螺铠：移动速度 -20%（与 C&C 铜马铠的修饰符完全一致）。 */
    private static final AttributeModifier COPPER_NAUTILUS_ARMOR_SLOWNESS =
            multipliedBase("copper_nautilus_armor_slowness", -0.2D);

    // ==================== Curiosities 兼容效果 ====================
    /** 铝矛：+2 实体交互距离（近战突刺距离另见 {@link SpearRegistry#onModifyComponents}）。 */
    private static final AttributeModifier ALUMINUM_SPEAR_ENTITY_RANGE =
            add("aluminum_spear_entity_range", 2.0D);
    /** 殷钢矛：+15% 护甲穿透（curiosities 属性，基准 1.0）。 */
    private static final AttributeModifier INVAR_SPEAR_ARMOR_PIERCING =
            multipliedBase("invar_spear_armor_piercing", 0.15D);
    /** 旅行者坐骑铠（铝）：+15% 移动速度、+15% 跳跃力度（鹦鹉螺骑乘冲刺同步按跳跃力度加成，见 AbstractNautilusMixin）。 */
    private static final AttributeModifier TRAVELER_MOUNT_ARMOR_SPEED =
            multipliedBase("traveler_mount_armor_speed", 0.15D);
    private static final AttributeModifier TRAVELER_MOUNT_ARMOR_JUMP =
            multipliedBase("traveler_mount_armor_jump", 0.15D);
    /** 殷钢坐骑铠：0.25 点伤害减免（curiosities 属性，直接减免）。 */
    private static final AttributeModifier INVAR_MOUNT_ARMOR_DAMAGE_REDUCTION =
            add("invar_mount_armor_damage_reduction", 0.25D);

    // ==================== 物品 → 处理函数 ====================
    @FunctionalInterface
    private interface Handler {
        void apply(ItemAttributeModifierEvent event);
    }

    /**
     * 懒构建 + {@code volatile} 发布：客户端渲染线程（tooltip）与服务端线程（装备变更）
     * 都可能第一个触发本事件，这样做既线程安全，又保证事件里只读一个字段。
     */
    private static volatile Map<Item, Handler> handlers;

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        Map<Item, Handler> map = handlers;
        if (map == null) map = buildHandlers();
        Handler handler = map.get(event.getItemStack().getItem());
        if (handler != null) handler.apply(event);
    }

    private static synchronized Map<Item, Handler> buildHandlers() {
        if (handlers != null) return handlers;

        final Holder<Attribute> invincibility = attribute("oreganized", "invincibility_frames");
        final Holder<Attribute> magicProtection = attribute("caverns_and_chasms", "magic_protection");
        final Holder<Attribute> slownessRetribution = attribute("caverns_and_chasms", "slowness_retribution");
        final Holder<Attribute> armorPiercing = attribute("curiosities", "armor_piercing");
        final Holder<Attribute> damageReduction = attribute("curiosities", "damage_reduction");

        Map<Item, Handler> map = new IdentityHashMap<>();

        // ==================== 长矛 ====================
        put(map, SpearRegistry.CURIOSITIES_ALUMINUM_SPEAR,
                event -> event.addModifier(Attributes.ENTITY_INTERACTION_RANGE, ALUMINUM_SPEAR_ENTITY_RANGE, MAINHAND));
        put(map, SpearRegistry.CURIOSITIES_INVAR_SPEAR, event -> {
            if (armorPiercing != null) event.addModifier(armorPiercing, INVAR_SPEAR_ARMOR_PIERCING, MAINHAND);
        });

        // ==================== 马铠 ====================
        put(map, HorseArmorRegistry.SILVER_HORSE_ARMOR,
                event -> addAttribute(event, invincibility, SILVER_HORSE_ARMOR_INVINCIBILITY));
        put(map, HorseArmorRegistry.ELECTRUM_HORSE_ARMOR,
                event -> event.addModifier(Attributes.MOVEMENT_SPEED, ELECTRUM_HORSE_ARMOR_SPEED, BODY));
        put(map, HorseArmorRegistry.ALUMINUM_HORSE_ARMOR, event -> {
            event.addModifier(Attributes.MOVEMENT_SPEED, TRAVELER_MOUNT_ARMOR_SPEED, BODY);
            event.addModifier(Attributes.JUMP_STRENGTH, TRAVELER_MOUNT_ARMOR_JUMP, BODY);
        });
        put(map, HorseArmorRegistry.INVAR_HORSE_ARMOR, event -> {
            if (damageReduction != null) event.addModifier(damageReduction, INVAR_MOUNT_ARMOR_DAMAGE_REDUCTION, BODY);
        });
        // BetterEnd 融晶马铠：护甲 9.4 / 韧性 0.96 / 抗击退 0（+1 生命恢复见 MountArmorEffects；血裔同理）
        put(map, HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR,
                event -> setExact(event, CRYSTALITE_HORSE_ARMOR_ARMOR, CRYSTALITE_HORSE_ARMOR_TOUGHNESS));
        // Caverns & Chasms 死灵合金马铠 / 下界合金马铠面板对齐
        if (CavernsAndChasmsCompat.isLoaded()) {
            put(map, CavernsAndChasmsCompat.necromiumHorseArmor(), event -> {
                setExact(event, NECROMIUM_HORSE_ARMOR_ARMOR, NECROMIUM_HORSE_ARMOR_TOUGHNESS);
                addAttribute(event, slownessRetribution, NECROMIUM_HORSE_ARMOR_SLOWNESS);
            });
            put(map, CavernsAndChasmsCompat.netheriteHorseArmor(), event -> {
                event.clearModifiers();
                event.addModifier(Attributes.ARMOR, NETHERITE_HORSE_ARMOR_ARMOR, BODY);
                event.addModifier(Attributes.ARMOR_TOUGHNESS, NETHERITE_HORSE_ARMOR_TOUGHNESS, BODY);
                event.addModifier(Attributes.KNOCKBACK_RESISTANCE, NETHERITE_HORSE_ARMOR_KNOCKBACK, BODY);
            });
        }

        // ==================== 鹦鹉螺铠 ====================
        put(map, NautilusArmorRegistry.OREGANIZED_SILVER_NAUTILUS_ARMOR,
                event -> addAttribute(event, invincibility, SILVER_NAUTILUS_ARMOR_INVINCIBILITY));
        put(map, NautilusArmorRegistry.CANDC_SILVER_NAUTILUS_ARMOR,
                event -> addAttribute(event, magicProtection, SILVER_NAUTILUS_ARMOR_MAGIC_PROTECTION));
        put(map, NautilusArmorRegistry.OREGANIZED_ELECTRUM_NAUTILUS_ARMOR,
                event -> event.addModifier(Attributes.MOVEMENT_SPEED, ELECTRUM_NAUTILUS_ARMOR_SPEED, BODY));
        put(map, NautilusArmorRegistry.ALUMINUM_NAUTILUS_ARMOR, event -> {
            event.addModifier(Attributes.MOVEMENT_SPEED, TRAVELER_MOUNT_ARMOR_SPEED, BODY);
            event.addModifier(Attributes.JUMP_STRENGTH, TRAVELER_MOUNT_ARMOR_JUMP, BODY);
        });
        put(map, NautilusArmorRegistry.INVAR_NAUTILUS_ARMOR, event -> {
            if (damageReduction != null) event.addModifier(damageReduction, INVAR_MOUNT_ARMOR_DAMAGE_REDUCTION, BODY);
        });
        put(map, NautilusArmorRegistry.CANDC_NECROMIUM_NAUTILUS_ARMOR,
                event -> addAttribute(event, slownessRetribution, NECROMIUM_NAUTILUS_ARMOR_SLOWNESS));
        putCopperNautilusArmor(map);
        put(map, NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR,
                event -> setExact(event, CRYSTALITE_NAUTILUS_ARMOR_ARMOR, CRYSTALITE_NAUTILUS_ARMOR_TOUGHNESS));

        handlers = map;
        return map;
    }

    /** C&C 铜鹦鹉螺铠全部 8 件（铜 / 斑驳 / 锈蚀 / 氧化 + 涂蜡×4）统一 -20% 移动速度。 */
    private static void putCopperNautilusArmor(Map<Item, Handler> map) {
        Handler handler = event -> event.addModifier(Attributes.MOVEMENT_SPEED, COPPER_NAUTILUS_ARMOR_SLOWNESS, BODY);
        put(map, NautilusArmorRegistry.CANDC_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_EXPOSED_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_WEATHERED_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_OXIDIZED_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_WAXED_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_WAXED_EXPOSED_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_WAXED_WEATHERED_COPPER_NAUTILUS_ARMOR, handler);
        put(map, NautilusArmorRegistry.CANDC_WAXED_OXIDIZED_COPPER_NAUTILUS_ARMOR, handler);
    }

    // ==================== 工具方法 ====================

    private static void put(Map<Item, Handler> map, DeferredItem<Item> holder, Handler handler) {
        if (holder != null) map.put(holder.get(), handler);
    }

    private static void put(Map<Item, Handler> map, Item item, Handler handler) {
        if (item != null && item != Items.AIR) map.put(item, handler);
    }

    private static void addAttribute(ItemAttributeModifierEvent event, Holder<Attribute> attribute,
            AttributeModifier modifier) {
        if (attribute == null) return;
        event.addModifier(attribute, modifier, BODY);
    }

    private static void setExact(ItemAttributeModifierEvent event, AttributeModifier armor,
            AttributeModifier toughness) {
        event.clearModifiers();
        event.addModifier(Attributes.ARMOR, armor, BODY);
        event.addModifier(Attributes.ARMOR_TOUGHNESS, toughness, BODY);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(EquipmentComplement.MOD_ID, path);
    }

    private static AttributeModifier add(String path, double amount) {
        return new AttributeModifier(id(path), amount, AttributeModifier.Operation.ADD_VALUE);
    }

    private static AttributeModifier multipliedBase(String path, double amount) {
        return new AttributeModifier(id(path), amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static Holder<Attribute> attribute(String namespace, String path) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(
                ResourceKey.create(Registries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath(namespace, path)))
                .orElse(null);
    }
}
