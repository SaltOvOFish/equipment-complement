package io.github.SaltOvOFish.equipmentcomplement.mixin;

import io.github.SaltOvOFish.equipmentcomplement.CraftingRecomputeGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取料期间挂起结果重算，取料结束后补算一次。
 *
 * <p>只作用于逻辑服务端；客户端的结果槽完全由服务端包驱动，不需要处理。</p>
 */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Shadow
    @Final
    private Player player;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void ec$beginTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!this.player.level().isClientSide()) {
            CraftingRecomputeGuard.begin();
        }
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void ec$endTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (!this.player.level().isClientSide()) {
            CraftingRecomputeGuard.end();
            // 取料期间被挂起的重算，在这里按最终格子补一次
            this.player.containerMenu.slotsChanged(this.craftSlots);
        }
    }
}
