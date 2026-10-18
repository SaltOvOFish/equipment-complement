package io.github.SaltOvOFish.equipmentcomplement.client;

import com.blackgear.platform.client.v2.render.ItemRendererRegistry;
import com.blackgear.platform.core.util.event.ResultHolder;
import io.github.SaltOvOFish.equipmentcomplement.CopperEquipmentRegistry;
import io.github.SaltOvOFish.equipmentcomplement.EquipmentComplement;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 本模组长矛的手持（in-hand）模型渲染器。
 *
 * VanillaBackport 的长矛手持 3D 效果不是通过物品模型 override 谓词实现的，
 * 而是通过 Platform 库的 {@link ItemRendererRegistry} 自定义物品渲染器完成。
 * 这里把本模组注册的全部长矛交给该渲染器，第一人称/第三人称改用各自命名空间下的
 * {@code *_spear_in_hand} 模型。
 *
 * <p><b>性能</b>：{@code renderFirstPerson} 在持矛时**每帧**都会调用，所以这里把
 * 「物品 → {@link ModelResourceLocation}」缓存在 {@link IdentityHashMap} 里，
 * 避免每帧重新拼字符串、构造 {@code ResourceLocation} 与 {@code ModelResourceLocation}（都是分配）。</p>
 */
@OnlyIn(Dist.CLIENT)
public class AddonSpearRenderer implements ItemRendererRegistry.Renderer {

    public static final AddonSpearRenderer INSTANCE = new AddonSpearRenderer();

    /** 物品 → 手持模型 id（物品是单例，用 IdentityHashMap）。只在渲染线程读写。 */
    private static final Map<Item, ModelResourceLocation> MODEL_CACHE = new IdentityHashMap<>();

    /** 长矛集合：注册完成后不再变化，第一次构建后缓存。 */
    private static Set<ItemLike> spears;

    private AddonSpearRenderer() {
    }

    /** 在事件触发时（物品已注册）动态收集所有实际注册的长矛。 */
    public static Set<ItemLike> spears() {
        // 缓存只在收集到内容后才生效（防止万一在注册完成前被调到而缓存成空集合）
        if (spears == null || spears.isEmpty()) spears = collectSpears();
        return spears;
    }

    private static Set<ItemLike> collectSpears() {
        Set<ItemLike> set = new LinkedHashSet<>();
        // VBP 铜矛的 7 个氧化 / 涂蜡变体
        set.addAll(CopperEquipmentRegistry.spears());
        add(set, EquipmentComplement.OREGANIZED_SILVER_SPEAR);
        add(set, EquipmentComplement.OREGANIZED_ELECTRUM_SPEAR);
        add(set, EquipmentComplement.THALLASIUM_SPEAR);
        add(set, EquipmentComplement.TERMINITE_SPEAR);
        add(set, EquipmentComplement.AETERNIUM_SPEAR);
        add(set, EquipmentComplement.CANDC_SILVER_SPEAR);
        add(set, EquipmentComplement.CANDC_NECROMIUM_SPEAR);
        add(set, EquipmentComplement.CANDC_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_EXPOSED_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_WEATHERED_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_OXIDIZED_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_WAXED_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_WAXED_EXPOSED_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_WAXED_WEATHERED_COPPER_SPEAR);
        add(set, EquipmentComplement.CANDC_WAXED_OXIDIZED_COPPER_SPEAR);
        return set;
    }

    private static void add(Set<ItemLike> set, DeferredItem<Item> item) {
        if (item != null) set.add(item.get());
    }

    private static ModelResourceLocation modelFor(Item item) {
        ModelResourceLocation cached = MODEL_CACHE.get(item);
        if (cached != null) return cached;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        ModelResourceLocation model = new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath() + "_in_hand"), "inventory");
        MODEL_CACHE.put(item, model);
        return model;
    }

    @Override
    public ResultHolder<BakedModel> renderFirstPerson(ItemStack stack, ItemDisplayContext context, ItemModelShaper shaper) {
        return context == ItemDisplayContext.GUI || context == ItemDisplayContext.FIXED || context == ItemDisplayContext.GROUND
                ? ResultHolder.pass()
                : ResultHolder.submit(shaper.getModelManager().getModel(modelFor(stack.getItem())));
    }

    @Override
    public ResultHolder<BakedModel> renderThirdPerson(ItemStack stack, ItemModelShaper shaper) {
        return ResultHolder.pass();
    }

    @Override
    public Set<ModelResourceLocation> registerModels() {
        Set<ModelResourceLocation> models = new LinkedHashSet<>();
        for (ItemLike item : spears()) models.add(modelFor(item.asItem()));
        return models;
    }
}
