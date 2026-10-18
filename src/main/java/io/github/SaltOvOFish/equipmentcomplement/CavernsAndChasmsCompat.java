package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caverns &amp; Chasms 兼容处理。
 *
 * <p>C&amp;C 自带一件 {@code caverns_and_chasms:netherite_horse_armor}，与 VanillaBackport 移植的
 * {@code minecraft:netherite_horse_armor} 重复。本模组：</p>
 * <ul>
 *     <li>用 {@code neoforge:false} 条件覆盖其锻造配方（{@code data/caverns_and_chasms/recipe/netherite_horse_armor_smithing.json}），
 *         使其无法再通过锻造台获得；</li>
 *     <li>在 {@code BuildCreativeModeTabContentsEvent} 中把它从所有创造模式标签页里移除（见 {@link CreativeTabEvents}）。</li>
 * </ul>
 *
 * <p>物品本体由 C&amp;C 注册，无法安全地"反注册"（C&amp;C 内部持有引用，硬删会炸），
 * 因此这里只能做到"不可合成 + 不在物品栏"，{@code /give} 依然可用。</p>
 *
 * <p><b>性能</b>：这些物品在注册之后不再变化，所以按路径缓存解析结果（含「不存在」→ {@code Items.AIR}），
 * 不再每次调用都查一遍注册表；缓存用 {@link ConcurrentHashMap}，因为创造栏（客户端线程）与
 * 属性事件（服务端线程）都会用到。</p>
 */
public final class CavernsAndChasmsCompat {

    public static final String MOD_ID = "caverns_and_chasms";

    private static final Map<String, Item> CACHE = new ConcurrentHashMap<>();

    private CavernsAndChasmsCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * C&amp;C 自带的下界合金马铠（与本模组无关，仅用于移除）。
     * <p>延迟解析：注册表在 RegisterEvent 之后才包含 C&amp;C 的物品，不能在类初始化时取。</p>
     */
    public static Item netheriteHorseArmor() {
        return item("netherite_horse_armor");
    }

    /** C&amp;C 自带的死灵合金马铠（仅用于覆盖其数值）。 */
    public static Item necromiumHorseArmor() {
        return item("necromium_horse_armor");
    }

    /** C&amp;C 自带的铜马铠（仅用于重新安排它在标签页中的位置）。 */
    public static Item copperHorseArmor() {
        return item("copper_horse_armor");
    }

    /** C&amp;C 自带的银马铠（仅用于重新安排它在标签页中的位置）。 */
    public static Item silverHorseArmor() {
        return item("silver_horse_armor");
    }

    /**
     * 解析 C&amp;C 的物品 id 并缓存；不存在时得到 {@code Items.AIR}（原版 ITEM 注册表是 defaulted 的）。
     * <p>供马铠兼容与铜锭氧化链（{@code caverns_and_chasms:*_copper_ingot}）使用。</p>
     */
    public static Item item(String path) {
        Item cached = CACHE.get(path);
        if (cached != null) return cached;
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
        CACHE.put(path, item);
        return item;
    }
}
