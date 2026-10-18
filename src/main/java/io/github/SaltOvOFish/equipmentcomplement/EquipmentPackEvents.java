package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * 内置资源包：「重铜命名」。
 *
 * <p>把 Caverns &amp; Chasms 的铜制装备显示名改成「重铜XX」/「Heavy Copper XX」，
 * 避免与 VanillaBackport 的铜制装备重名（两边都叫「铜剑 / 铜矛」…）。
 * 语言覆盖写在 mod 内置的 {@code resourcepacks/heavy_copper_names} 里
 * （{@code assets/caverns_and_chasms/lang/{zh_cn,en_us}.json}，共 96 条：
 * C&amp;C 本体的铜武器 / 工具 / 盔甲 / 铜马铠共 80 条 + 本模组注册在 C&amp;C 命名空间的铜矛 / 铜鹦鹉螺铠共 16 条）。</p>
 *
 * <p>只有 {@code [general] heavy_copper_names} 开启时才把该资源包注册进仓库。
 * 位置用 {@link Pack.Position#TOP} 保证优先级高于所有模组自带资源（包括 C&amp;C 自己的语言文件），
 * {@code alwaysActive} 为 {@code true}，所以它在资源包界面里是强制启用、不可取消的。</p>
 *
 * <p>注意：这是 <b>startup</b> 配置项，改动后需要重启游戏（资源包仓库只在启动时构建一次）。</p>
 */
public final class EquipmentPackEvents {

    private static final ResourceLocation HEAVY_COPPER_PACK =
            ResourceLocation.fromNamespaceAndPath(EquipmentComplement.MOD_ID, "resourcepacks/heavy_copper_names");

    private EquipmentPackEvents() {
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        if (!EquipmentConfig.HEAVY_COPPER_NAMES.get()) return;
        // 没装 C&C 时这些物品根本不存在（本模组的铜矛 / 铜鹦鹉螺铠也注册在 C&C 命名空间），不必挂一个空资源包
        if (!CavernsAndChasmsCompat.isLoaded()) return;

        event.addPackFinders(HEAVY_COPPER_PACK, PackType.CLIENT_RESOURCES,
                Component.translatable("resourcepack.equipment_complement.heavy_copper_names"),
                PackSource.BUILT_IN, true, Pack.Position.TOP);
    }
}
