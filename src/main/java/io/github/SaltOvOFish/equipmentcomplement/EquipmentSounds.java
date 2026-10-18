package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

/**
 * 装备音效解析。
 *
 * <p>音效一律按**注册 id** 从原版注册表里取，而不是引用前置模组的类：这样既不会因为前置改包名 /
 * 改类名而在运行时抛 {@code NoClassDefFoundError}，也不受对方 Holder 绑定时机影响。取不到时统一
 * 回退到铁甲音效，最坏情况只是音色不对，不会崩。</p>
 *
 * <p>调用点必须是在**注册事件期间**（例如护甲材质的构造）—— 那时声音已经注册完毕
 * （{@code BuiltInRegistries} 里 SOUND_EVENT 早于 ARMOR_MATERIAL / ITEM）。
 * 不要在静态字段里调用：那会在模组构造期执行，注册事件还没发生，必然误回退。</p>
 */
public final class EquipmentSounds {

    private EquipmentSounds() {
    }

    /** VanillaBackport 移植的铜装备穿戴音效：{@code minecraft:item.armor.equip_copper}。 */
    public static Holder<SoundEvent> vanillabackportCopper() {
        return resolve(ResourceLocation.withDefaultNamespace("item.armor.equip_copper"));
    }

    /** Caverns &amp; Chasms 自己的铜装备穿戴音效：{@code caverns_and_chasms:item.armor.equip_copper}。 */
    public static Holder<SoundEvent> cavernsAndChasmsCopper() {
        return resolve(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "item.armor.equip_copper"));
    }

    /** 按 id 解析音效 Holder，解析不到回退铁甲音效。 */
    public static Holder<SoundEvent> resolve(ResourceLocation id) {
        Optional<Holder.Reference<SoundEvent>> sound = BuiltInRegistries.SOUND_EVENT
                .getHolder(ResourceKey.create(Registries.SOUND_EVENT, id));
        return sound.isPresent() ? sound.get() : SoundEvents.ARMOR_EQUIP_IRON;
    }
}
