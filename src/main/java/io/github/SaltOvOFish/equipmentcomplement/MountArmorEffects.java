package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 融晶 / 血裔铠甲的「生命恢复 I」效果。
 *
 * <p>BetterEnd 的融晶盔甲套装增益是生命恢复 I，这里给融晶马铠 / 融晶鹦鹉螺铠补上同样效果；
 * Caverns &amp; Chasms 的血裔马铠 / 血裔鹦鹉螺铠也自带生命恢复 I。</p>
 *
 * <p>本类不再是事件监听器：调用点统一在 {@link CopperOxidationEvents#onServerTick} 里
 * （服务端 tick、每 20 tick 一次、随实体遍历一起做），避免再为一个每秒一次的效果多挂一个
 * 「每实体每 tick」的监听器。</p>
 */
public final class MountArmorEffects {

    private MountArmorEffects() {
    }

    /** 是否有任何带常驻效果的坐骑铠处于启用状态（全部未注册时每秒 tick 可直接跳过全维度实体遍历）。 */
    public static boolean anyEnabled() {
        return HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR != null
                || NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR != null
                || HorseArmorRegistry.SANGUINE_HORSE_ARMOR != null
                || NautilusArmorRegistry.SANGUINE_NAUTILUS_ARMOR != null;
    }

    /**
     * 若实体的 BODY 槽是融晶或血裔铠甲，刷新 3 秒的再生 I（调用方保证每 20 tick 调用一次、仅服务端）。
     */
    public static void refreshMountArmorEffects(LivingEntity living) {
        ItemStack armor = living.getItemBySlot(EquipmentSlot.BODY);
        if (armor.isEmpty()) return;
        if (!isCrystalite(armor.getItem()) && !isSanguine(armor.getItem())) return;

        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
    }

    private static boolean isCrystalite(Item item) {
        if (HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR != null
                && item == HorseArmorRegistry.CRYSTALITE_HORSE_ARMOR.get()) {
            return true;
        }
        return NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR != null
                && item == NautilusArmorRegistry.CRYSTALITE_NAUTILUS_ARMOR.get();
    }

    private static boolean isSanguine(Item item) {
        if (HorseArmorRegistry.SANGUINE_HORSE_ARMOR != null
                && item == HorseArmorRegistry.SANGUINE_HORSE_ARMOR.get()) {
            return true;
        }
        return NautilusArmorRegistry.SANGUINE_NAUTILUS_ARMOR != null
                && item == NautilusArmorRegistry.SANGUINE_NAUTILUS_ARMOR.get();
    }
}
