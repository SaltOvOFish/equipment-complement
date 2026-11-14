package io.github.SaltOvOFish.equipmentcomplement.mixin;

import io.github.SaltOvOFish.equipmentcomplement.CraftingRecomputeGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取料期间跳过合成结果重算。
 *
 * <p>原版合成台在 {@code ResultSlot.onTake} 中逐格扣料并放回余项，每次容器变动都会同步调用
 * 本方法重算结果并发包。工具被临时扣掉的瞬间，合成格只剩铜锭，可能命中其它模组的
 * 「1 铜锭 → 9 铜粒」配方，导致结果槽闪烁。这里在取料期间直接取消重算，
 * 由 {@link ResultSlotMixin} 在取料结束后统一补算一次。</p>
 */
@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void ec$skipRecomputeDuringTake(AbstractContainerMenu menu, Level level, Player player,
            CraftingContainer craftSlots, ResultContainer resultSlots, RecipeHolder<CraftingRecipe> recipe,
            CallbackInfo ci) {
        if (CraftingRecomputeGuard.isSuppressing()) {
            ci.cancel();
        }
    }
}
