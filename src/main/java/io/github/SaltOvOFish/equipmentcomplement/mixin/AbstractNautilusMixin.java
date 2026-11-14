package io.github.SaltOvOFish.equipmentcomplement.mixin;

import com.blackgear.vanillabackport.common.level.entities.mob.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 让鹦鹉螺的骑乘蓄力冲刺（{@code executeRidersJump}）额外受 {@link Attributes#JUMP_STRENGTH} 倍率影响。
 *
 * <p>VanillaBackport 的 {@code AbstractNautilus.executeRidersJump} 原本只读取 {@code MOVEMENT_SPEED}，
 * 未读取 {@code JUMP_STRENGTH}。这里按 {@code getAttributeValue / getAttributeBaseValue} 缩放冲刺力度：
 * 未装备跳跃加成时比值为 1.0（零副作用）；装备旅行者鹦鹉螺铠（+15% 跳跃力度）后，冲刺距离 / 跃出水面高度额外提升 15%。</p>
 */
@Mixin(AbstractNautilus.class)
public abstract class AbstractNautilusMixin extends TamableAnimal {

    protected AbstractNautilusMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "executeRidersJump", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float ec$applyJumpStrengthToDash(float scale) {
        double base = this.getAttributeBaseValue(Attributes.JUMP_STRENGTH);
        if (base <= 0.0D) return scale;
        return (float) (scale * (this.getAttributeValue(Attributes.JUMP_STRENGTH) / base));
    }
}
