package io.github.SaltOvOFish.equipmentcomplement.client;

import com.blackgear.vanillabackport.client.level.model.entity.nautilus.NautilusModel;
import com.blackgear.vanillabackport.client.registries.ModModelLayers;
import com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus;
import io.github.SaltOvOFish.equipmentcomplement.NautilusArmorRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 皮革鹦鹉螺铠的染色渲染层。
 *
 * <p>VanillaBackport 的 {@code SimpleEquipmentLayer} 只会按材质贴图绘制，不处理
 * {@code dyed_color} 组件，所以皮革鹦鹉螺铠需要在同一位置再画一次带颜色的模型。
 * 本层被插在「盔甲层」之后、「鞍具层」之前，保证鞍具仍然盖在盔甲上面。</p>
 *
 * <p>基础皮革贴图本身是灰度的（与用户提供的 {@code leather.png} 一致），
 * 未染色时使用原版皮革默认色 {@link DyedItemColor#LEATHER_COLOR}。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class LeatherNautilusArmorLayer extends RenderLayer<AbstractNautilus, NautilusModel<AbstractNautilus>> {

    /** 与材质名 {@code minecraft:leather} 推导出的实体贴图路径一致。 */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/nautilus/armor/leather.png");

    private final NautilusModel<AbstractNautilus> armorModel;

    public LeatherNautilusArmorLayer(RenderLayerParent<AbstractNautilus, NautilusModel<AbstractNautilus>> parent,
            EntityModelSet models) {
        super(parent);
        this.armorModel = new NautilusModel<>(models.bakeLayer(ModModelLayers.NAUTILUS_ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractNautilus nautilus,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (nautilus.isBaby() || NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR == null) return;

        ItemStack stack = nautilus.getItemBySlot(EquipmentSlot.BODY);
        if (!stack.is(NautilusArmorRegistry.LEATHER_NAUTILUS_ARMOR.get())) return;

        this.armorModel.setupAnim(nautilus, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.armorModel.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR));
    }
}
