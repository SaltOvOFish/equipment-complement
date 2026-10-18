package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 铜制品的氧化 / 涂蜡映射表与推进判定。
 *
 * <p>每条链固定 8 个物品：下标 0..3 是未涂蜡的四个阶段（铜 / 斑驳 / 锈蚀 / 氧化），
 * 4..7 是对应的涂蜡版本。共两族：</p>
 * <ul>
 *   <li>本模组注册在 {@code caverns_and_chasms} 命名空间的铜矛与铜鹦鹉螺铠（由
 *       {@code [oxidation] candc_copper_oxidation} 开关控制）；</li>
 *   <li>VanillaBackport 的铜制品（{@code minecraft} 命名空间，见 {@link CopperEquipmentRegistry}，
 *       由 {@code [copper_equipment] oxidation} 开关控制）。</li>
 * </ul>
 *
 * <p>推进参数与 Caverns &amp; Chasms / 原版风化完全一致：单次尝试概率 {@code 5.688889e-4}，
 * 「铜」基础阶段再 ×0.75，按 {@code randomTickSpeed} 次尝试，判定节奏为每 60 tick 一次。</p>
 *
 * <p>默认规则同 C&amp;C：只有穿戴 / 手持时才会氧化，铜锭是唯一例外（按玩家背包处理）。
 * 可选开关 {@link EquipmentConfig#INVENTORY_OXIDATION} 可以让两族铜类物品「放在背包里也氧化」。</p>
 *
 * <p>性能：查表用「物品注册 id → 数组下标」的 O(1) 数组访问，无哈希、无装箱、无分配；
 * 配置开关只在命中候选物品后才读取。</p>
 */
public final class CopperOxidation {

    /** 未涂蜡阶段数（铜 / 斑驳 / 锈蚀 / 氧化）。 */
    private static final int STAGES = 4;

    /** 单次尝试的氧化概率（与 C&amp;C / 原版风化一致）。 */
    private static final float CHANCE = 0.05688889F * 0.01F;

    /** 「铜」基础阶段的概率倍率（与 C&amp;C / 原版风化一致）。 */
    private static final float BASE_STAGE_MULTIPLIER = 0.75F;

    private static Stage[] byItemId;

    /** 缓存的槽位数组，避免每个实体都做一次枚举克隆。 */
    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    private CopperOxidation() {
    }

    /** 某个物品的阶段信息。 */
    private static final class Stage {
        private final Item next;
        private final Item previous;
        private final Item first;
        private final Item waxed;
        private final boolean base;
        /** 只有该类型的槽位才会推进；{@code null} 表示没有装备槽（仅玩家背包内推进，如铜锭）。 */
        private final EquipmentSlot.Type slotType;
        private final ModConfigSpec.BooleanValue gate;

        private Stage(Item next, Item previous, Item first, Item waxed, boolean base,
                      EquipmentSlot.Type slotType, ModConfigSpec.BooleanValue gate) {
            this.next = next;
            this.previous = previous;
            this.first = first;
            this.waxed = waxed;
            this.base = base;
            this.slotType = slotType;
            this.gate = gate;
        }
    }

    /** 一条氧化链。 */
    private static final class Chain {
        private final ModConfigSpec.BooleanValue gate;
        private final EquipmentSlot.Type slotType;
        private final Item[] items;

        private Chain(ModConfigSpec.BooleanValue gate, EquipmentSlot.Type slotType, Item[] items) {
            this.gate = gate;
            this.slotType = slotType;
            this.items = items;
        }
    }

    // ==================== 查询 ====================

    /** 下一氧化阶段；不可氧化（已到最终阶段 / 机制关闭 / 非铜制品）返回 null。 */
    public static Item getNext(Item item) {
        Stage stage = enabled(item);
        return stage == null ? null : stage.next;
    }

    /** 涂蜡版本；不可涂蜡返回 null。 */
    public static Item getWaxed(Item item) {
        Stage stage = enabled(item);
        return stage == null ? null : stage.waxed;
    }

    /** 是否处于未氧化的「铜」阶段（氧化概率更低，同原版）。 */
    public static boolean isBaseStage(Item item) {
        Stage stage = stageOf(item);
        return stage != null && stage.base;
    }

    /** 是否有任一族的氧化机制处于开启状态。 */
    public static boolean anyEnabled() {
        return EquipmentConfig.CANDC_COPPER_OXIDATION.get() || EquipmentConfig.VBP_COPPER_OXIDATION.get();
    }

    private static Stage enabled(Item item) {
        Stage stage = stageOf(item);
        return stage != null && stage.gate.get() ? stage : null;
    }

    private static Stage stageOf(Item item) {
        // 空气物品不参与查表（防止空槽被当成可氧化物品而“凭空”生成物品）
        if (item == null || item == Items.AIR) return null;
        Stage[] table = table();
        int id = BuiltInRegistries.ITEM.getId(item);
        return id >= 0 && id < table.length ? table[id] : null;
    }

    // ==================== 推进 ====================

    /**
     * 遍历一个实体的全部装备槽各推进一次（调用方已保证每 60 tick 只调用一次）。
     * 循环用缓存的 {@link #SLOTS}，不再每次克隆枚举数组。
     */
    public static void tickAllSlots(LivingEntity living, Level level) {
        for (EquipmentSlot slot : SLOTS) {
            tickSlot(living, slot, level);
        }
    }

    /** 随机取一个装备槽（闪电去氧化用）。 */
    public static EquipmentSlot randomSlot(RandomSource random) {
        return SLOTS[random.nextInt(SLOTS.length)];
    }

    /** 对一个装备槽里的物品做一次氧化判定。 */
    public static void tickSlot(LivingEntity living, EquipmentSlot slot, Level level) {
        ItemStack stack = living.getItemBySlot(slot);
        Stage stage = usable(stack.getItem(), slot);
        if (stage == null) return;
        ItemStack next = roll(stack, stage, level);
        if (next != null) living.setItemSlot(slot, next);
    }

    /**
     * 玩家背包一趟：铜锭（无装备槽）总是处理；开启 {@code copper.inventory_oxidation} 后，
     * 装备类铜制品（武器 / 工具 / 盔甲 / 马铠 / 鹦鹉螺铠 / 铜矛）在背包里也会氧化。
     *
     * <p>背包那一趟会跳过盔甲 / 副手槽（它们走装备槽逻辑）与当前选中的快捷栏格
     * （它等价于主手物品，已由 {@link #tickSlot} 推进），以免同一件物品每 60 tick 被掷两次骰。</p>
     */
    public static void tickPlayerInventory(Player player, Level level) {
        Inventory inventory = player.getInventory();
        // 主背包 + 快捷栏（items，36 格）；36..40 是盔甲与副手槽
        int mainSize = inventory.items.size();
        int selected = inventory.selected;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            tickContainer(inventory, i, level);
            if (i < mainSize && i != selected) {
                tickInventorySlot(inventory, i, level);
            }
        }
    }

    /** 对容器内「没有装备槽」的物品（铜锭）做一次氧化判定。 */
    public static void tickContainer(Container container, int slot, Level level) {
        ItemStack stack = container.getItem(slot);
        Stage stage = stageOf(stack.getItem());
        if (stage == null || stage.slotType != null || !stage.gate.get()) return;
        ItemStack next = roll(stack, stage, level);
        if (next != null) container.setItem(slot, next);
    }

    /**
     * 背包内氧化（可选，见 {@link EquipmentConfig#INVENTORY_OXIDATION}）：
     * 对「本来只靠装备槽推进」的铜制品做一次氧化判定，让它们放在背包里也会氧化。
     *
     * <p>调用方需要跳过玩家当前选中的快捷栏格（那格物品就是主手物品，已由
     * {@link #tickSlot} 推进）以及盔甲 / 副手槽（它们走 {@code EquipmentSlot}），
     * 以免同一件物品每 60 tick 被掷两次骰。</p>
     */
    public static void tickInventorySlot(Container container, int slot, Level level) {
        if (!EquipmentConfig.INVENTORY_OXIDATION.get()) return;
        ItemStack stack = container.getItem(slot);
        Stage stage = stageOf(stack.getItem());
        if (stage == null || stage.slotType == null || !stage.gate.get()) return;
        ItemStack next = roll(stack, stage, level);
        if (next != null) container.setItem(slot, next);
    }

    /** 闪电去氧化：把该槽位的铜制品重置回最基础的「铜」阶段（与 C&amp;C 的头部槽处理一致）。 */
    public static void resetToFirst(LivingEntity living, EquipmentSlot slot) {
        ItemStack stack = living.getItemBySlot(slot);
        Stage stage = usable(stack.getItem(), slot);
        if (stage == null || stage.first == null || stage.first == stack.getItem()) return;
        living.setItemSlot(slot, copyWith(stack, stage.first));
    }

    /** 闪电去氧化：把该槽位的铜制品退一个氧化阶段（与 C&amp;C 的随机槽位处理一致）。 */
    public static void stepBack(LivingEntity living, EquipmentSlot slot) {
        ItemStack stack = living.getItemBySlot(slot);
        Stage stage = usable(stack.getItem(), slot);
        if (stage == null || stage.previous == null) return;
        living.setItemSlot(slot, copyWith(stack, stage.previous));
    }

    /** 该物品能否在这个槽位上被推进 / 去氧化。 */
    private static Stage usable(Item item, EquipmentSlot slot) {
        Stage stage = stageOf(item);
        return stage != null && stage.slotType == slot.getType() && stage.gate.get() ? stage : null;
    }

    /** 按原版风化概率尝试推进一个阶段；成功返回新物品堆，否则 null。 */
    private static ItemStack roll(ItemStack stack, Stage stage, Level level) {
        if (stage.next == null) return null;

        RandomSource random = level.getRandom();
        float chance = CHANCE;
        if (stage.base) chance *= BASE_STAGE_MULTIPLIER;

        int randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        for (int i = 0; i < randomTickSpeed; i++) {
            if (random.nextFloat() < chance) {
                return copyWith(stack, stage.next);
            }
        }
        return null;
    }

    /** 换成同数量、保留组件（附魔 / 耐久等）的另一件物品。 */
    private static ItemStack copyWith(ItemStack stack, Item item) {
        ItemStack copy = new ItemStack(item, stack.getCount());
        copy.applyComponents(stack.getComponentsPatch());
        return copy;
    }

    // ==================== 建表 ====================

    private static Stage[] table() {
        if (byItemId == null) {
            Stage[] table = new Stage[BuiltInRegistries.ITEM.size()];
            for (Chain chain : chains()) {
                Item[] items = chain.items;
                if (items[1] == null || items[1] == Items.AIR) continue; // 变体不可用则整条链跳过
                for (int i = 0; i < STAGES; i++) {
                    Item unwaxed = items[i];
                    if (unwaxed == null || unwaxed == Items.AIR) continue;
                    int id = BuiltInRegistries.ITEM.getId(unwaxed);
                    if (id < 0 || id >= table.length) continue;
                    table[id] = new Stage(i < STAGES - 1 ? items[i + 1] : null,
                            i > 0 ? items[i - 1] : null, items[0], usable(items[i + STAGES]),
                            i == 0, chain.slotType, chain.gate);
                }
            }
            byItemId = table;
        }
        return byItemId;
    }

    private static List<Chain> chains() {
        List<Chain> chains = new ArrayList<>();

        // ===== 族一：本模组的 C&C 命名空间铜矛 / 铜鹦鹉螺铠（C&C 本体没有这两件） =====
        chains.add(chain(EquipmentConfig.CANDC_COPPER_OXIDATION, EquipmentSlot.Type.HAND,
                EquipmentComplement.CANDC_COPPER_SPEAR,
                EquipmentComplement.CANDC_EXPOSED_COPPER_SPEAR,
                EquipmentComplement.CANDC_WEATHERED_COPPER_SPEAR,
                EquipmentComplement.CANDC_OXIDIZED_COPPER_SPEAR,
                EquipmentComplement.CANDC_WAXED_COPPER_SPEAR,
                EquipmentComplement.CANDC_WAXED_EXPOSED_COPPER_SPEAR,
                EquipmentComplement.CANDC_WAXED_WEATHERED_COPPER_SPEAR,
                EquipmentComplement.CANDC_WAXED_OXIDIZED_COPPER_SPEAR));

        chains.add(chain(EquipmentConfig.CANDC_COPPER_OXIDATION, EquipmentSlot.Type.ANIMAL_ARMOR,
                NautilusArmorRegistry.CANDC_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_EXPOSED_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_WEATHERED_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_OXIDIZED_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_WAXED_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_WAXED_EXPOSED_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_WAXED_WEATHERED_COPPER_NAUTILUS_ARMOR,
                NautilusArmorRegistry.CANDC_WAXED_OXIDIZED_COPPER_NAUTILUS_ARMOR));

        // ===== 族二：VBP 铜制品（minecraft 命名空间，13 件 × 8 阶段） =====
        for (String piece : CopperEquipmentRegistry.PIECES) {
            Item[] items = new Item[STAGES * 2];
            items[0] = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("copper_" + piece));
            if (piece.equals("ingot") && CavernsAndChasmsCompat.isLoaded()) {
                // 铜锭：装了 C&C 时本模组不再注册自家铜锭变体，直接接上 C&C 的铜锭链
                // （minecraft:copper_ingot → caverns_and_chasms:*_copper_ingot，与它的数据表一致）
                items[1] = CavernsAndChasmsCompat.item("exposed_copper_ingot");
                items[2] = CavernsAndChasmsCompat.item("weathered_copper_ingot");
                items[3] = CavernsAndChasmsCompat.item("oxidized_copper_ingot");
                items[4] = CavernsAndChasmsCompat.item("waxed_copper_ingot");
                items[5] = CavernsAndChasmsCompat.item("waxed_exposed_copper_ingot");
                items[6] = CavernsAndChasmsCompat.item("waxed_weathered_copper_ingot");
                items[7] = CavernsAndChasmsCompat.item("waxed_oxidized_copper_ingot");
            } else {
                for (int i = 0; i < CopperEquipmentRegistry.VARIANTS.size(); i++) {
                    items[i + 1] = item(CopperEquipmentRegistry.get(
                            CopperEquipmentRegistry.VARIANTS.get(i) + "_copper_" + piece));
                }
            }
            chains.add(new Chain(EquipmentConfig.VBP_COPPER_OXIDATION, slotTypeFor(piece), items));
        }

        return chains;
    }

    /** 该铜制品在哪种槽位才会氧化；铜锭没有装备槽（按玩家背包处理）。 */
    private static EquipmentSlot.Type slotTypeFor(String piece) {
        return switch (piece) {
            case "helmet", "chestplate", "leggings", "boots" -> EquipmentSlot.Type.HUMANOID_ARMOR;
            case "horse_armor", "nautilus_armor" -> EquipmentSlot.Type.ANIMAL_ARMOR;
            case "ingot" -> null;
            default -> EquipmentSlot.Type.HAND; // 剑 / 工具 / 长矛
        };
    }

    @SafeVarargs
    private static Chain chain(ModConfigSpec.BooleanValue gate, EquipmentSlot.Type slotType,
                               DeferredItem<Item>... holders) {
        Item[] items = new Item[STAGES * 2];
        for (int i = 0; i < items.length && i < holders.length; i++) {
            items[i] = item(holders[i]);
        }
        return new Chain(gate, slotType, items);
    }

    private static Item item(DeferredItem<Item> holder) {
        return holder == null ? null : holder.get();
    }

    /** 把「缺失 / 空气」的物品归一成 {@code null}（用于涂蜡目标）。 */
    private static Item usable(Item item) {
        return item == null || item == Items.AIR ? null : item;
    }
}
