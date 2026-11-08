package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 铜制品「氧化」配方：水桶 + 任意铜制品 → 前进一个氧化阶段。
 *
 * <ul>
 *   <li>未涂蜡：铜 → 斑驳 → 锈蚀 → 氧化；最终「氧化」阶段没有下一阶段；</li>
 *   <li>涂蜡物品不参与（蜡封防氧化，与 {@code getOxidizingResult} 一致）；</li>
 *   <li>整堆处理：物品数量原样保留；</li>
 *   <li>消耗水：合成后返还 1 个空铁桶。</li>
 * </ul>
 *
 * <p><b>性能</b>：{@code matches} 位于合成热路径上（每次改动合成格、每次查配方书都会调用），
 * 因此先用配置开关和 {@link CopperOxidation#anyEnabled()} 短路，再用 O(1) 的数组查表。</p>
 */
public class CopperOxidizingRecipe extends CustomRecipe {

    public CopperOxidizingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!EquipmentConfig.COPPER_OXIDIZING_CRAFTING.get()) return false;
        if (!CopperOxidation.anyEnabled()) return false;

        int bucketCount = 0;
        int copperCount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.WATER_BUCKET)) {
                bucketCount++;
            } else if (CopperOxidation.getOxidizingResult(stack.getItem()) != null) {
                copperCount++;
            } else {
                return false;
            }
            if (bucketCount > 1 || copperCount > 1) return false;
        }
        return bucketCount == 1 && copperCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || stack.is(Items.WATER_BUCKET)) continue;
            Item result = CopperOxidation.getOxidizingResult(stack.getItem());
            if (result == null) continue;
            // 每次合成原版只从输入格扣 1 个，产物也只能 1 个，否则会凭空多出物品。
            ItemStack output = new ItemStack(result);
            output.applyComponents(stack.getComponentsPatch());
            return output;
        }
        return ItemStack.EMPTY;
    }

    /** 消耗水，返还空铁桶。 */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(Items.WATER_BUCKET)) {
                remaining.set(i, new ItemStack(Items.BUCKET));
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EquipmentComplement.COPPER_OXIDIZING_SERIALIZER.get();
    }
}
