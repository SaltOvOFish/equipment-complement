package io.github.SaltOvOFish.equipmentcomplement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据包条件：读取本模组启动配置里的布尔开关。
 *
 * <p>用于按配置启用 / 禁用配方（例如 {@code mount_armor_crafting}）。
 * 配置是 STARTUP 类型，在数据包加载前已经就绪，因此条件可以安全地读取。</p>
 *
 * <p>JSON 示例：</p>
 * <pre>{@code
 * {"type": "equipment_complement:config", "option": "mount_armor_crafting"}
 * {"type": "equipment_complement:config", "option": "candc_netherite_horse_armor", "expected": false}
 * }</pre>
 */
public record ConfigCondition(String option, boolean expected) implements ICondition {

    private static final Logger LOGGER = LoggerFactory.getLogger("equipment_complement");

    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option),
            Codec.BOOL.optionalFieldOf("expected", true).forGetter(ConfigCondition::expected)
    ).apply(instance, ConfigCondition::new));

    @Override
    public boolean test(IContext context) {
        // 未安装 C&C 时，「替换 C&C 下界合金马铠」这个开关没有意义：
        // 视为满足条件，让 VanillaBackport 的原配方继续生效（C&C 配方本身还有 mod_loaded 守卫）。
        if ("candc_netherite_horse_armor".equals(option) && !ModList.get().isLoaded("caverns_and_chasms")) {
            return true;
        }
        ModConfigSpec.BooleanValue value = EquipmentConfig.booleanOption(option);
        if (value == null) {
            // 拼错开关名会让配方静默失效，这里只会在数据包加载时警告一次。
            LOGGER.warn("[equipment_complement] unknown config option in recipe condition: {}", option);
            return false;
        }
        return value.get() == expected;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
