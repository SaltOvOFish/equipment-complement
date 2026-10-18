package io.github.SaltOvOFish.equipmentcomplement.client;

import com.blackgear.platform.client.GameRendering;
import com.blackgear.vanillabackport.client.level.layer.SimpleEquipmentLayer;
import com.blackgear.vanillabackport.client.level.renderer.entity.mob.NautilusRenderer;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus;
import io.github.SaltOvOFish.equipmentcomplement.NautilusArmorRegistry;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 客户端初始化。
 *
 * <ul>
 *   <li>把本模组长矛注册到 Platform 的物品渲染器系统；</li>
 *   <li>注册皮革鹦鹉螺铠的**物品栏染色**（{@code dyed_color} 组件 → 图标颜色）；</li>
 *   <li>给普通鹦鹉螺的渲染器补上**实体染色层**（鹦鹉螺身上的皮革铠）。</li>
 * </ul>
 *
 * <p>仅应在物理客户端调用（由主类在 {@code FMLEnvironment.dist.isClient()} 分支中触发）。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientSetupEvents {

    private ClientSetupEvents() {
    }

    public static void setup(IEventBus modEventBus) {
        GameRendering.registerItemLikeRenderers(
                event -> event.simple(AddonSpearRenderer.INSTANCE, AddonSpearRenderer.spears()));

        modEventBus.addListener(ClientSetupEvents::registerItemColors);
        modEventBus.addListener(ClientSetupEvents::addLayers);
    }

    /**
     * 皮革鹦鹉螺铠的物品栏图标染色。与原版皮革盔甲一致：{@code layer0}（tint index 0）整体染色，
     * 未染色的默认色为 {@link DyedItemColor#LEATHER_COLOR}。
     */
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        if (NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR == null) return;
        event.register(
                (stack, tintIndex) -> tintIndex > 0
                        ? -1
                        : DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR),
                NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR.get());
    }

    /** 给所有普通鹦鹉螺渲染器（不含僵尸鹦鹉螺）补一层皮革鹦鹉螺铠的染色渲染。 */
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        if (NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR == null) return;
        EntityModelSet models = event.getEntityModels();
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer instanceof NautilusRenderer) {
                addLeatherLayer(models, renderer);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLeatherLayer(EntityModelSet models, EntityRenderer<?> renderer) {
        NautilusRenderer<AbstractNautilus> nautilusRenderer = (NautilusRenderer<AbstractNautilus>) renderer;
        LeatherNautilusArmorLayer layer = new LeatherNautilusArmorLayer(nautilusRenderer, models);

        // 鹦鹉螺渲染器的层顺序是「盔甲层 → 鞍具层」，两者都是 SimpleEquipmentLayer。
        // 把皮革层插到最后一个装备层（鞍具）之前：既排在盔甲层之后（覆盖未染色的灰度底图），
        // 又排在鞍具层之前（鞍具仍然压在盔甲上）。
        try {
            Field field = LivingEntityRenderer.class.getDeclaredField("layers");
            field.setAccessible(true);
            List<RenderLayer<?, ?>> layers = (List<RenderLayer<?, ?>>) field.get(nautilusRenderer);
            int equipmentLayers = 0;
            int lastEquipment = -1;
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) instanceof SimpleEquipmentLayer) {
                    equipmentLayers++;
                    lastEquipment = i;
                }
            }
            if (equipmentLayers >= 2) {
                layers.add(lastEquipment, layer);
                return;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // 反射不可用时退化为追加；只影响「同时装备鞍具」时的绘制顺序。
        }
        nautilusRenderer.addLayer(layer);
    }
}
