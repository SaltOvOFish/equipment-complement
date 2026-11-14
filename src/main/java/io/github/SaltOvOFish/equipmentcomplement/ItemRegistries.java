package io.github.SaltOvOFish.equipmentcomplement;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 跨模组命名空间的物品注册表汇总。
 *
 * <p>本模组向多个上游命名空间补充物品（长矛、马铠、鹦鹉螺铠、铜变体）。
 * 统一在此维护每个命名空间唯一的 {@link DeferredRegister.Items}，避免不同模块各自创建实例导致
 * 注册顺序隐式耦合或同 ID 冲突难排查。</p>
 */
public final class ItemRegistries {

    public static final DeferredRegister.Items MINECRAFT = DeferredRegister.createItems("minecraft");
    public static final DeferredRegister.Items OREGANIZED = DeferredRegister.createItems("oreganized");
    public static final DeferredRegister.Items BETTEREND = DeferredRegister.createItems("betterend");
    public static final DeferredRegister.Items CAVERNS_AND_CHASMS = DeferredRegister.createItems("caverns_and_chasms");
    public static final DeferredRegister.Items CURIOSITIES = DeferredRegister.createItems("curiosities");
    public static final DeferredRegister.Items LEGENDARY_MONSTERS = DeferredRegister.createItems("legendary_monsters");

    private ItemRegistries() {
    }

    public static void registerAll(IEventBus modEventBus) {
        MINECRAFT.register(modEventBus);
        OREGANIZED.register(modEventBus);
        BETTEREND.register(modEventBus);
        CAVERNS_AND_CHASMS.register(modEventBus);
        CURIOSITIES.register(modEventBus);
        LEGENDARY_MONSTERS.register(modEventBus);
    }
}
