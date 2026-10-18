package io.github.SaltOvOFish.equipmentcomplement;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 铜制品的涂蜡配方（蜜脾，或装了 C&amp;C 时的锡板）。
 *
 * <p><b>性能</b>：{@code matches} 位于合成热路径上——玩家每次改动合成格、每次查配方书
 * 都会让 vanilla 逐个配方调用它（最多 9 格），所以这里：</p>
 * <ul>
 *   <li>开头就用 {@link CopperOxidation#anyEnabled()} 短路（两族氧化都关时不可能有可涂蜡物品）；</li>
 *   <li>锡板物品**解析一次并缓存**，不在每次 {@code matches} 里做 {@code ResourceLocation} 解析 + 注表查询。</li>
 * </ul>
 */
public class CopperWaxingRecipe extends CustomRecipe {

    /** 锡板（C&amp;C）：解析一次后缓存；未装 C&amp;C 时得到 {@code Items.AIR}，永远不会与真实物品相等。 */
    private static Item tinplate;
    private static boolean tinplateResolved;

    public CopperWaxingRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static Item tinplate() {
        if (!tinplateResolved) {
            tinplate = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "tinplate"));
            tinplateResolved = true;
        }
        return tinplate;
    }

    // 判断是否是涂蜡材料（蜜脾或锡板）
    private static boolean isWaxMaterial(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.HONEYCOMB || item == tinplate();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        // 无需在这里判断开关：CopperOxidation.getWaxed 会按物品所属族的开关放行
        if (!CopperOxidation.anyEnabled()) return false;
        int spearCount = 0;
        int waxCount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (CopperOxidation.getWaxed(stack.getItem()) != null) {
                spearCount++;
            } else if (isWaxMaterial(stack)) {
                waxCount++;
            } else {
                return false;
            }
            if (spearCount > 1 || waxCount > 1) return false;
        }
        return spearCount == 1 && waxCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack spearStack = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && CopperOxidation.getWaxed(stack.getItem()) != null) {
                spearStack = stack;
                break;
            }
        }
        Item waxed = CopperOxidation.getWaxed(spearStack.getItem());
        ItemStack result = new ItemStack(waxed, spearStack.getCount());
        result.applyComponents(spearStack.getComponentsPatch()); // 保留附魔、耐久等
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EquipmentComplement.COPPER_WAXING_SERIALIZER.get();
    }
}
