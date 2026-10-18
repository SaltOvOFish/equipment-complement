package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/**
 * 坐骑铠（马铠 / 鹦鹉螺铠）护甲材质的注册工具。
 *
 * <p>两个注册类（{@link HorseArmorRegistry} / {@link NautilusArmorRegistry}）原本各写了一遍
 * 「只有 BODY 有防御值 + 单一贴图层 + 惰性修复材料」，这里统一收口，
 * 约定：<b>贴图层命名空间 = 材质注册表命名空间</b>、<b>贴图层名 = 材质注册名</b>
 * （鹦鹉螺铠的实体贴图就是按材质注册名推导路径的）。</p>
 */
public final class ArmorMaterials {

    private ArmorMaterials() {
    }

    /** 坐骑铠只有 BODY（坐骑）有防御值，其余部位一律 0。 */
    public static EnumMap<ArmorItem.Type, Integer> bodyDefense(int body) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 0);
            map.put(ArmorItem.Type.LEGGINGS, 0);
            map.put(ArmorItem.Type.CHESTPLATE, 0);
            map.put(ArmorItem.Type.HELMET, 0);
            map.put(ArmorItem.Type.BODY, body);
        });
    }

    /** 穿戴贴图层：{@code <layerNamespace>:textures/models/armor/<name>_layer_*.png}。 */
    public static List<ArmorMaterial.Layer> layer(String layerNamespace, String name) {
        return List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(layerNamespace, name)));
    }

    /**
     * 注册一个坐骑铠护甲材质；贴图层命名空间取材质注册表自身的命名空间。
     *
     * @param body                 BODY 槽（坐骑）防御值
     * @param repairNamespace      修复材料所在命名空间
     * @param repairItem           修复材料物品名
     */
    public static DeferredHolder<ArmorMaterial, ArmorMaterial> register(
            DeferredRegister<ArmorMaterial> registry, String name, int body,
            int enchantmentValue, Holder<SoundEvent> equipSound,
            String repairNamespace, String repairItem,
            float toughness, float knockbackResistance) {
        String layerNamespace = registry.getNamespace();
        return registry.register(name, () -> new ArmorMaterial(
                bodyDefense(body), enchantmentValue, equipSound,
                () -> Ingredient.of(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(repairNamespace, repairItem))),
                layer(layerNamespace, name), toughness, knockbackResistance));
    }
}
