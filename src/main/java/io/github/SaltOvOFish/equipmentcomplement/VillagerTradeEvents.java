package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/**
 * 坐骑铠的村民交易。
 *
 * <p>原版皮革马铠在皮匠专家级（4 级）以 6 个绿宝石出售随机染色版本，
 * 这里给皮革鹦鹉螺铠补上完全相同的交易。</p>
 */
public final class VillagerTradeEvents {

    private VillagerTradeEvents() {
    }

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LEATHERWORKER) {
            return;
        }
        if (NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR == null) {
            return;
        }
        var expertTrades = event.getTrades().get(4);
        if (expertTrades == null) {
            return;
        }
        expertTrades.add(new VillagerTrades.DyedArmorForEmeralds(
                NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR.get(), 6, 12, 15));
    }
}
