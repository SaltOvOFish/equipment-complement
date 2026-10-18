package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 创造模式物品栏排布。
 *
 * <p><b>战斗用品栏（COMBAT）</b>：长矛 / 马铠 / 鹦鹉螺铠 三家物品按材质档次交错排列，
 * 锚点用 VanillaBackport 与 C&amp;C 已放进标签页的原版物品（本模组数据包后加载，监听器也后触发，
 * 所以这些锚点在执行时一定存在）。</p>
 *
 * <p><b>更好的末地：物品（betterend:items_tab）</b>：BetterEnd 兼容物品挂到对应材质的靴子 / 锻锤 / 剑柄之后。
 * 注意 wover 生成的页 id 带 {@code _tab} 后缀（{@code betterend:items_tab}），标题 key 却是
 * {@code itemGroup.betterend.items}，两者不一致，容易写错。</p>
 *
 * <p><b>性能</b>：每次构建标签页内容只把「本页已有的物品」收集成一次 {@link HashSet}
 * （{@link TabEditor}），插入 / 移除都同时维护该集合，因此判断「物品是否已在页里」
 * 从 O(页大小) 的扫描变成 O(1) 查表（原来每次插入要扫两遍整页）。</p>
 */
public class CreativeTabEvents {

    private static final ResourceKey<CreativeModeTab> BETTEREND_ITEMS = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("betterend", "items_tab"));

    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        TabEditor tab = new TabEditor(event);

        // 下界合金马铠两版本二选一：默认隐藏 C&C 版；开启 candc_netherite_horse_armor 后反过来隐藏 VBP 版。
        // 物品本体都保留，只做创造栏显示切换。
        if (CavernsAndChasmsCompat.isLoaded()) {
            if (EquipmentConfig.CANDC_NETHERITE_HORSE_ARMOR.get()) {
                tab.remove(mc("netherite_horse_armor"));
            } else {
                tab.remove(CavernsAndChasmsCompat.netheriteHorseArmor());
            }
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            buildCombat(tab);
        } else if (isBetterEndItemsTab(event.getTabKey())) {
            buildBetterEndItems(tab);
        }

        // VBP 铜制品的氧化 / 涂蜡变体：跟在各自基础件后面（工具页 / 战斗用品页 / 材料页通用）
        insertCopperVariants(tab);
    }

    // ==================== 铜制品变体 ====================

    /** 基础铜制品 → 它的 7 个氧化 / 涂蜡变体（只含 VBP 那族；C&C 命名空间的两件仍由 {@link #buildCombat} 处理）。 */
    private static Map<Item, List<Item>> copperVariants;

    private static Map<Item, List<Item>> copperVariants() {
        if (copperVariants != null) return copperVariants;
        Map<Item, List<Item>> map = new LinkedHashMap<>();
        for (String piece : CopperEquipmentRegistry.PIECES) {
            Item base = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("copper_" + piece));
            if (base == null || base == Items.AIR) continue;
            List<Item> variants = new ArrayList<>(CopperEquipmentRegistry.VARIANTS.size());
            for (String variant : CopperEquipmentRegistry.VARIANTS) {
                DeferredItem<Item> holder = CopperEquipmentRegistry.get(variant + "_copper_" + piece);
                if (holder != null) variants.add(holder.get());
            }
            if (!variants.isEmpty()) map.put(base, variants);
        }
        copperVariants = map;
        return map;
    }

    /**
     * 把每件铜制品的氧化 / 涂蜡变体插到它的基础件之后：先找出本页里存在的基础件，再依次插入。
     * 这样无论是在工具页（铜镐等）、战斗用品页（铜剑 / 铜盔甲 / 铜矛 / 坐骑铠）还是材料页（铜锭）都自动生效。
     */
    private static void insertCopperVariants(TabEditor tab) {
        if (!EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) return;
        Map<Item, List<Item>> variants = copperVariants();
        if (variants.isEmpty()) return;

        List<Item> present = null;
        for (Item base : variants.keySet()) {
            if (tab.has(base)) {
                if (present == null) present = new ArrayList<>(4);
                present.add(base);
            }
        }
        if (present == null) return;

        for (Item base : present) {
            Item anchor = base;
            for (Item variant : variants.get(base)) {
                anchor = tab.insert(anchor, variant);
            }
        }
    }

    /**
     * BetterEnd 的「物品」页。wover 生成 id 时会统一加 {@code _tab} 后缀
     * （因此实际是 {@code betterend:items_tab}，而不是 {@code betterend:items}），
     * 这里两种写法都接受。
     */
    private static boolean isBetterEndItemsTab(ResourceKey<CreativeModeTab> key) {
        if (key.equals(BETTEREND_ITEMS)) return true;
        ResourceLocation id = key.location();
        return "betterend".equals(id.getNamespace()) && "items_tab".equals(id.getPath());
    }

    // ==================== 战斗用品栏 ====================

    private static void buildCombat(TabEditor tab) {
        // ---------- 长矛：铜(VBP) → 铜(C&C) → 铁 → 金 → 银(Oreg) → 银(C&C) → 钻 → 琥珀金 → 下界 → 死灵(C&C) ----------
        Item anchor = tab.chain(mc("copper_spear"), EquipmentComplement.CANDC_COPPER_SPEAR);
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            anchor = tab.chain(anchor, EquipmentComplement.CANDC_EXPOSED_COPPER_SPEAR);
            anchor = tab.chain(anchor, EquipmentComplement.CANDC_WEATHERED_COPPER_SPEAR);
            anchor = tab.chain(anchor, EquipmentComplement.CANDC_OXIDIZED_COPPER_SPEAR);
            anchor = tab.chain(anchor, EquipmentComplement.CANDC_WAXED_COPPER_SPEAR);
            anchor = tab.chain(anchor, EquipmentComplement.CANDC_WAXED_EXPOSED_COPPER_SPEAR);
            anchor = tab.chain(anchor, EquipmentComplement.CANDC_WAXED_WEATHERED_COPPER_SPEAR);
            tab.chain(anchor, EquipmentComplement.CANDC_WAXED_OXIDIZED_COPPER_SPEAR);
        }
        anchor = tab.chain(mc("golden_spear"), EquipmentComplement.OREGANIZED_SILVER_SPEAR);
        anchor = tab.chain(anchor, EquipmentComplement.CANDC_SILVER_SPEAR);
        anchor = tab.chain(mc("diamond_spear"), EquipmentComplement.OREGANIZED_ELECTRUM_SPEAR);
        tab.chain(mc("netherite_spear"), EquipmentComplement.CANDC_NECROMIUM_SPEAR);

        // ---------- 马铠：铜(VBP) → 铜(C&C) → 锁链 → 铁 → 金 → 银(Oreg) → 银(C&C) → 钻 → 琥珀金 → 下界 → 死灵(C&C) ----------
        Item candcCopperHorse = null;
        Item candcSilverHorse = null;
        Item candcNecromiumHorse = null;
        Item candcNetheriteHorse = null;
        if (CavernsAndChasmsCompat.isLoaded()) {
            // C&C 把这几件放在了别的位置，先移除再由本模组重新插入
            candcCopperHorse = tab.takeOut(CavernsAndChasmsCompat.copperHorseArmor());
            candcSilverHorse = tab.takeOut(CavernsAndChasmsCompat.silverHorseArmor());
            candcNecromiumHorse = tab.takeOut(CavernsAndChasmsCompat.necromiumHorseArmor());
            candcNetheriteHorse = CavernsAndChasmsCompat.netheriteHorseArmor();
        }
        anchor = tab.insert(mc("copper_horse_armor"), candcCopperHorse);
        anchor = tab.chain(anchor, HorseArmorRegistry.CHAINMAIL_HORSE_ARMOR);
        anchor = tab.chain(mc("golden_horse_armor"), HorseArmorRegistry.SILVER_HORSE_ARMOR);
        anchor = tab.insert(anchor, candcSilverHorse);
        anchor = tab.chain(mc("diamond_horse_armor"), HorseArmorRegistry.ELECTRUM_HORSE_ARMOR);
        // 开启 C&C 下界合金马铠时，死灵合金马铠的锚点要换成 C&C 版
        Item netheriteHorseAnchor = EquipmentConfig.CANDC_NETHERITE_HORSE_ARMOR.get()
                && candcNetheriteHorse != null && candcNetheriteHorse != Items.AIR
                ? candcNetheriteHorse
                : mc("netherite_horse_armor");
        tab.insert(netheriteHorseAnchor, candcNecromiumHorse);

        // ---------- 鹦鹉螺铠：皮革 → 铜(VBP) → 铜(C&C) → 锁链 → 铁 → 金 → 银(Oreg) → 银(C&C) → 钻 → 琥珀金 → 下界 → 死灵(C&C) ----------
        // 皮革款（本模组新增）放在 VBP 铜鹦鹉螺铠之前
        tab.insertBefore(mc("copper_nautilus_armor"), NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR);
        anchor = tab.chain(mc("copper_nautilus_armor"), NautilusArmorRegistry.CANDC_COPPER_NAUTILUS_ARMOR);
        if (EquipmentConfig.COPPER_VARIANTS_IN_TAB.get()) {
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_EXPOSED_COPPER_NAUTILUS_ARMOR);
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_WEATHERED_COPPER_NAUTILUS_ARMOR);
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_OXIDIZED_COPPER_NAUTILUS_ARMOR);
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_WAXED_COPPER_NAUTILUS_ARMOR);
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_WAXED_EXPOSED_COPPER_NAUTILUS_ARMOR);
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_WAXED_WEATHERED_COPPER_NAUTILUS_ARMOR);
            anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_WAXED_OXIDIZED_COPPER_NAUTILUS_ARMOR);
        }
        anchor = tab.chain(anchor, NautilusArmorRegistry.CHAINMAIL_NAUTILUS_ARMOR);
        anchor = tab.chain(mc("iron_nautilus_armor"), NautilusArmorRegistry.OREGANIZED_SILVER_NAUTILUS_ARMOR);
        anchor = tab.chain(anchor, NautilusArmorRegistry.CANDC_SILVER_NAUTILUS_ARMOR);
        anchor = tab.chain(mc("diamond_nautilus_armor"), NautilusArmorRegistry.OREGANIZED_ELECTRUM_NAUTILUS_ARMOR);
        tab.chain(mc("netherite_nautilus_armor"), NautilusArmorRegistry.CANDC_NECROMIUM_NAUTILUS_ARMOR);
    }

    // ==================== 更好的末地：物品 ====================

    private static void buildBetterEndItems(TabEditor tab) {
        // 锻锤 → 对应材质长矛
        tab.chain(be("thallasium_hammer"), EquipmentComplement.THALLASIUM_SPEAR);
        tab.chain(be("terminite_hammer"), EquipmentComplement.TERMINITE_SPEAR);
        tab.chain(be("aeternium_hammer"), EquipmentComplement.AETERNIUM_SPEAR);

        // 剑柄 → 对应材质矛头
        tab.chain(be("thallasium_sword_handle"), EquipmentComplement.THALLASIUM_SPEAR_HEAD);
        tab.chain(be("terminite_sword_handle"), EquipmentComplement.TERMINITE_SPEAR_HEAD);
        tab.chain(be("aeternium_sword_handle"), EquipmentComplement.AETERNIUM_SPEAR_HEAD);

        // 靴子 → 马铠 → 鹦鹉螺铠
        tab.chain2(be("thallasium_boots"),
                HorseArmorRegistry.THALLASIUM_HORSE_ARMOR, NautilusArmorRegistry.THALLASIUM_NAUTILUS_ARMOR);
        tab.chain2(be("terminite_boots"),
                HorseArmorRegistry.TERMINITE_HORSE_ARMOR, NautilusArmorRegistry.TERMINITE_NAUTILUS_ARMOR);
        tab.chain2(be("aeternium_boots"),
                HorseArmorRegistry.AETERNIUM_HORSE_ARMOR, NautilusArmorRegistry.AETERNIUM_NAUTILUS_ARMOR);
        tab.chain2(be("crystalite_boots"),
                HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR, NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR);
    }

    // ==================== 工具方法 ====================

    private static Item mc(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(path));
    }

    private static Item be(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("betterend", path));
    }

    /**
     * 单次标签页构建的编辑器：内部维护一份「本页已有的物品」集合，
     * 所有增删都走它，保证集合与实际内容同步，同时把「是否已存在」变成 O(1)。
     */
    private static final class TabEditor {

        private final BuildCreativeModeTabContentsEvent event;
        private final Set<Item> present = new HashSet<>();

        private TabEditor(BuildCreativeModeTabContentsEvent event) {
            this.event = event;
            for (ItemStack stack : event.getParentEntries()) {
                present.add(stack.getItem());
            }
        }

        private boolean has(Item item) {
            return item != null && item != Items.AIR && present.contains(item);
        }

        private void remove(Item item) {
            if (!has(item)) return;
            event.remove(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            present.remove(item);
        }

        /** 把物品先从页里摘出来（供稍后重新插入到别的位置）。 */
        private Item takeOut(Item item) {
            if (!has(item)) return null;
            remove(item);
            return item;
        }

        /**
         * 插到 anchor 之后；锚点不在该页里（或物品已存在）时退化为追加到页尾，
         * 保证兼容物品一定出现在目标页，而不是静默丢失。返回实际作为下一个锚点的物品。
         */
        private Item insert(Item anchor, Item item) {
            if (item == null || item == Items.AIR) return anchor;
            if (has(item)) return item;
            if (has(anchor)) {
                event.insertAfter(new ItemStack(anchor), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            present.add(item);
            return item;
        }

        /** 插到 target 之前；target 不在页里时退化为追加，物品已存在则跳过。 */
        private void insertBefore(Item target, DeferredItem<Item> item) {
            if (item != null) insertBefore(target, item.get());
        }

        private void insertBefore(Item target, Item item) {
            if (item == null || item == Items.AIR || has(item)) return;
            if (has(target)) {
                event.insertBefore(new ItemStack(target), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            present.add(item);
        }

        private Item chain(Item anchor, DeferredItem<Item> item) {
            return item == null ? anchor : insert(anchor, item.get());
        }

        private Item chain(Item anchor, Item item) {
            return insert(anchor, item);
        }

        /** 锚点之后依次插入两件（第一件作为第二件的锚点）。 */
        private void chain2(Item anchor, DeferredItem<Item> first, DeferredItem<Item> second) {
            insert(chain(anchor, first), second == null ? null : second.get());
        }
    }
}
