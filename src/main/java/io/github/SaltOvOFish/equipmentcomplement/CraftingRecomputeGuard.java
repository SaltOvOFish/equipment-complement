package io.github.SaltOvOFish.equipmentcomplement;

/**
 * 「取料期间暂缓重算合成结果」的共享标记。
 *
 * <p>原版 {@code ResultSlot.onTake} 会先把每个格子扣 1 个，再放回余项；而
 * {@code TransientCraftingContainer} 的每次增删都会同步触发
 * {@code CraftingMenu.slotChangedCraftingGrid} 重算结果并给客户端发包。
 * 工具（斧子 / 水桶）在「已扣掉、未放回」的这一瞬间不在合成格里，格子临时只剩铜锭，
 * 可能命中别的模组的「1 铜锭 → 9 铜粒」配方，导致结果槽在铜粒与铜锭之间闪烁。</p>
 *
 * <p>标记按线程隔离：整合服务端与客户端各用各的，互不影响。</p>
 */
public final class CraftingRecomputeGuard {

    private static final ThreadLocal<Boolean> SUPPRESSING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private CraftingRecomputeGuard() {
    }

    public static void begin() {
        SUPPRESSING.set(Boolean.TRUE);
    }

    public static void end() {
        SUPPRESSING.set(Boolean.FALSE);
    }

    /**
     * 强制清除标记。
     *
     * <p>取料过程若抛出异常，{@code ResultSlot.onTake} 的 RETURN 注入不会执行，标记会残留，
     * 之后该线程的合成结果将不再刷新。服务端每个 tick 末尾调用一次即可兜底。</p>
     */
    public static void reset() {
        SUPPRESSING.set(Boolean.FALSE);
    }

    public static boolean isSuppressing() {
        return SUPPRESSING.get();
    }
}
