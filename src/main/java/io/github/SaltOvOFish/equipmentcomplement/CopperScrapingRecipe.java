package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 铜制品「除锈」配方：任意斧子 + 任意铜制品 → 退一个氧化阶段。
 *
 * <ul>
 *   <li>未涂蜡：斑驳 → 铜、锈蚀 → 斑驳；基础「铜」没有上一阶段，最终「氧化」阶段不可除锈；</li>
 *   <li>任意阶段的涂蜡物品 → 同阶段的未涂蜡版本（去蜡，不改变阶段）；</li>
 *   <li>整堆处理：物品数量原样保留；</li>
 *   <li>斧子只作为工具：合成后原样留在合成格里，不消耗、也不掉耐久。</li>
 * </ul>
 *
 * <p><b>性能</b>：{@code matches} 位于合成热路径上（每次改动合成格、每次查配方书都会调用），
 * 因此先用配置开关和 {@link CopperOxidation#anyEnabled()} 短路，再用 O(1) 的数组查表。</p>
 */
public class CopperScrapingRecipe extends CustomRecipe {

    public CopperScrapingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!EquipmentConfig.COPPER_SCRAPING_CRAFTING.get()) return false;
        if (!CopperOxidation.anyEnabled()) return false;

        int axeCount = 0;
        int copperCount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ItemTags.AXES)) {
                axeCount++;
            } else if (CopperOxidation.getScrapingResult(stack.getItem()) != null) {
                copperCount++;
            } else {
                return false;
            }
            if (axeCount > 1 || copperCount > 1) return false;
        }
        return axeCount == 1 && copperCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || stack.is(ItemTags.AXES)) continue;
            Item result = CopperOxidation.getScrapingResult(stack.getItem());
            if (result == null) continue;
            // 每次合成原版只从输入格扣 1 个，产物也必须只有 1 个，否则会凭空多出物品。
            // 整堆处理由 Shift 连续合成完成。
            ItemStack output = new ItemStack(result);
            output.applyComponents(stack.getComponentsPatch());
            return output;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 斧子留在合成格里：除锈铜装备时消耗 1 点耐久，除锈铜锭时不掉耐久；
     * 耐久耗尽的斧子会直接消失（与原版工具一致）。
     */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        Item copper = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && !stack.is(ItemTags.AXES)) {
                copper = stack.getItem();
                break;
            }
        }
        boolean ingot = copper != null && CopperOxidation.isIngot(copper);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || !stack.is(ItemTags.AXES)) continue;
            ItemStack axe = stack.copy();
            if (!ingot && axe.isDamageableItem()) {
                int damage = axe.getDamageValue() + 1;
                if (damage >= axe.getMaxDamage()) continue; // 耐久耗尽，斧子损坏
                axe.setDamageValue(damage);
            }
            remaining.set(i, axe);
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EquipmentComplement.COPPER_SCRAPING_SERIALIZER.get();
    }
}
