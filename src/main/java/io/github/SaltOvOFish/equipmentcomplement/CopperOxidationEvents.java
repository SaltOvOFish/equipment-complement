package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 铜制品的氧化推进 + 闪电去氧化；同时兼管融晶坐骑铠的再生效果刷新。
 *
 * <p>规则与 Caverns &amp; Chasms 一致：<b>只有穿戴 / 手持时才会氧化</b>。
 * 对一个活体实体，遍历它的全部装备槽位（主手 / 副手 / 四件盔甲 / BODY），
 * 物品与槽位类型匹配且属于可氧化铜制品时才推进（马铠、鹦鹉螺铠走 BODY 槽，
 * 所以骑乘中的坐骑铠甲也会自然氧化）。判定节奏为每 60 tick 一次。</p>
 *
 * <p>默认例外是没有装备槽的铜锭：它按「放在玩家背包里」处理，同样每 60 tick 判定一次。
 * 开启 {@link EquipmentConfig#INVENTORY_OXIDATION} 后，两族铜类物品也都能「放在背包里就氧化」：
 * 背包扫描会跳过盔甲 / 副手槽（那些仍由装备槽推进）与当前选中的快捷栏格
 * （它等价于主手物品），以免同一件物品每 60 tick 被掷两次骰。</p>
 *
 * <p><b>性能</b>：本模组所有「按时间推进」的逻辑都收在这一个监听器里，挂在
 * {@link ServerTickEvent.Post}（每 tick 一次，与实体数量无关）而不是
 * {@code EntityTickEvent.Post}（<b>每实体每 tick</b> 都会派发，连掉落物 / 箭矢都要付一次派发成本）。
 * 只有真的到点（{@code %20} / {@code %60}）才遍历该维度的实体，且没有玩家的维度直接跳过。</p>
 */
@EventBusSubscriber(modid = EquipmentComplement.MOD_ID)
public class CopperOxidationEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 没有玩家的维度里实体本来就不 tick，直接跳过
            if (level.players().isEmpty()) continue;

            long time = level.getGameTime();
            boolean oxidize = time % 60L == 0L && CopperOxidation.anyEnabled();
            boolean mountArmor = time % 20L == 0L;
            if (!oxidize && !mountArmor) continue;

            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living)) continue;
                if (oxidize) {
                    CopperOxidation.tickAllSlots(living, level);
                    if (living instanceof Player player) {
                        CopperOxidation.tickPlayerInventory(player, level);
                    }
                }
                if (mountArmor) {
                    MountArmorEffects.refreshCrystaliteRegeneration(living);
                }
            }
        }
    }

    /**
     * 闪电去氧化（与 Caverns &amp; Chasms 一致）：被闪电击中的实体，头部槽的铜制品直接重置回
     * 「铜」阶段，另外随机挑 1~3 个槽位各退一个氧化阶段（已涂蜡的物品不受影响）。
     */
    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        Level level = living.level();
        if (level.isClientSide()) return;
        if (!CopperOxidation.anyEnabled()) return;

        CopperOxidation.resetToFirst(living, EquipmentSlot.HEAD);

        var random = level.getRandom();
        int times = random.nextInt(3) + 1;
        for (int i = 0; i < times; i++) {
            CopperOxidation.stepBack(living, CopperOxidation.randomSlot(random));
        }
    }
}
