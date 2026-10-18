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
 * 坐骑铠（马铠 + 鹦鹉螺铠）的属性修正。
 *
 * <p>包含两部分：</p>
 * <ul>
 *     <li><b>附加效果</b>：在此基础上叠加其它模组的属性
 *         （Oreganized 无敌帧、Caverns &amp; Chasms 魔法保护 / 缓慢回馈）。</li>
 *     <li><b>精确覆盖</b>：融晶铠甲的护甲是小数、C&amp;C 死灵合金马铠需要被本模组改写数值，
 *         这些用 {@code clearModifiers()} 清空后重写，避免叠加。</li>
 * </ul>
 *
 * <p><b>性能</b>：本事件在**每次生成带属性修饰符的物品 tooltip 时都会触发**——
 * 链路是 {@code AbstractContainerScreen.render → getTooltipFromContainerItem →
 * ItemStack.getTooltipLines → AttributeUtil.addAttributeTooltips → forEachModifier}，
 * 也就是说悬停盔甲 / 工具 / 武器时**每帧一次**。因此这里：</p>
 * <ol>
 *   <li>物品派发只做**一次 {@link IdentityHashMap} 查表**，不再是十几次 {@code stack.is(...)}；</li>
 *   <li>所有 {@link AttributeModifier}（含其 id）预先构造成 {@code static final} 常量，
 *       事件里只做 {@code addModifier}，**不产生任何对象分配**；</li>
 *   <li>跨模组属性只在构建派发表时解析一次（不存在就是 {@code null}，直接跳过），
 *       之后由 lambda 捕获，事件里不再查注册表。</li>
 * </ol>
 */
@EventBusSubscriber(modid = EquipmentComplement.MOD_ID)
public class ArmorAttributeEvents {

    private static final EquipmentSlotGroup BODY = EquipmentSlotGroup.BODY;

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

        Map<Item, Handler> map = new IdentityHashMap<>();

        // ==================== 马铠 ====================
        // Oreganized 银马铠：+0.2 无敌帧
        put(map, HorseArmorRegistry.SILVER_HORSE_ARMOR,
                event -> addAttribute(event, invincibility, SILVER_HORSE_ARMOR_INVINCIBILITY));
        // Oreganized 琥珀金马铠：+5% 移动速度
        put(map, HorseArmorRegistry.ELECTRUM_HORSE_ARMOR,
                event -> event.addModifier(Attributes.MOVEMENT_SPEED, ELECTRUM_HORSE_ARMOR_SPEED, BODY));
        // BetterEnd 融晶马铠：护甲 9.4 / 韧性 0.96 / 抗击退 0（+1 生命恢复见 MountArmorEffects）
        put(map, HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR,
                event -> setExact(event, CRYSTALITE_HORSE_ARMOR_ARMOR, CRYSTALITE_HORSE_ARMOR_TOUGHNESS));
        // Caverns & Chasms 死灵合金马铠：护甲 19 / 韧性 2 / 抗击退 0 / 缓慢回馈 +1
        // Caverns & Chasms 下界合金马铠：数值提升到与 VanillaBackport 版本一致（19 / 3.0 / 0.1）
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
        // Oreganized 银鹦鹉螺铠：+0.2 无敌帧
        put(map, NautilusArmorRegistry.OREGANIZED_SILVER_NAUTILUS_ARMOR,
                event -> addAttribute(event, invincibility, SILVER_NAUTILUS_ARMOR_INVINCIBILITY));
        // Caverns & Chasms 银鹦鹉螺铠：魔法保护 +0.4（与 C&C 银马铠一致）
        put(map, NautilusArmorRegistry.CANDC_SILVER_NAUTILUS_ARMOR,
                event -> addAttribute(event, magicProtection, SILVER_NAUTILUS_ARMOR_MAGIC_PROTECTION));
        // Oreganized 琥珀金鹦鹉螺铠：+5% 移动速度
        put(map, NautilusArmorRegistry.OREGANIZED_ELECTRUM_NAUTILUS_ARMOR,
                event -> event.addModifier(Attributes.MOVEMENT_SPEED, ELECTRUM_NAUTILUS_ARMOR_SPEED, BODY));
        // Caverns & Chasms 死灵合金鹦鹉螺铠：缓慢回馈 +1
        put(map, NautilusArmorRegistry.CANDC_NECROMIUM_NAUTILUS_ARMOR,
                event -> addAttribute(event, slownessRetribution, NECROMIUM_NAUTILUS_ARMOR_SLOWNESS));
        // Caverns & Chasms 铜鹦鹉螺铠（含氧化 / 涂蜡变体）：移动速度 -20%
        putCopperNautilusArmor(map);
        // BetterEnd 融晶鹦鹉螺铠：护甲 9.4 / 韧性 0.96 / 抗击退 0
        put(map, NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR,
                event -> setExact(event, CRYSTALITE_NAUTILUS_ARMOR_ARMOR, CRYSTALITE_NAUTILUS_ARMOR_TOUGHNESS));

        handlers = map;
        return map;
    }

    /**
     * C&C 铜鹦鹉螺铠全部 8 件（铜 / 斑驳 / 锈蚀 / 氧化 + 涂蜡×4）统一 -20% 移动速度。
     * 与 C&C 铜马铠一样使用 {@code ADD_MULTIPLIED_BASE}，这样骑乘时与 C&C 铜马铠的手感一致。
     */
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

    /** 注册一个物品的处理函数（未注册的物品传 null，直接跳过）。 */
    private static void put(Map<Item, Handler> map, DeferredItem<Item> holder, Handler handler) {
        if (holder != null) map.put(holder.get(), handler);
    }

    /** 原生物品（例如 C&C 自带的死灵合金马铠）：解析不到或为空气时跳过。 */
    private static void put(Map<Item, Handler> map, Item item, Handler handler) {
        if (item != null && item != Items.AIR) map.put(item, handler);
    }

    /** 追加一条修饰符（属性解析不到时跳过）。 */
    private static void addAttribute(ItemAttributeModifierEvent event, Holder<Attribute> attribute,
            AttributeModifier modifier) {
        if (attribute == null) return;
        event.addModifier(attribute, modifier, BODY);
    }

    /** 清空后精确写入护甲 / 韧性（抗击退是 0，等于不写入，避免出现「+0」）。 */
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

    /** 按 id 解析属性；解析不到返回 {@code null}（只在建表时调用，调用方需容忍 null）。 */
    private static Holder<Attribute> attribute(String namespace, String path) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(
                ResourceKey.create(Registries.ATTRIBUTE, ResourceLocation.fromNamespaceAndPath(namespace, path)))
                .orElse(null);
    }
}
