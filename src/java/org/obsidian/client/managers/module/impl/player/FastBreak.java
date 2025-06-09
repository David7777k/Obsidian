package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.DelimiterSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;

import java.util.Random;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FastBreak", category = Category.PLAYER)
public class FastBreak extends Module {

    public static FastBreak getInstance() {
        return Instance.get(FastBreak.class);
    }

    // Разделитель для группировки настроек
    final DelimiterSetting delimiter = new DelimiterSetting(this, "Настройки FastBreak");

    // Множитель пакетов: сколько раз за тик отправляем STOP/ABORT
    // (возвращается Float, поэтому при использовании — .intValue())
    final SliderSetting multiplier = new SliderSetting(this, "Усиление", 2f, 1f, 5f, 1f);

    // Включить работу только при наличии кирки в руках
    final BooleanSetting pickaxeOnly = new BooleanSetting(this, "Только кирка", true);

    private final Random random = new Random();

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            mc.player.sendStatusMessage(
                    new StringTextComponent(
                            TextFormatting.GREEN
                                    + "[FastBreak] Включён! Усиление: "
                                    + multiplier.getValue().intValue()
                                    + ", Только кирка: "
                                    + (pickaxeOnly.getValue() ? "да" : "нет")
                    ),
                    false
            );
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.sendStatusMessage(
                    new StringTextComponent(
                            TextFormatting.RED + "[FastBreak] Выключен."
                    ),
                    false
            );
        }
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        // Проверяем, смотрим ли мы на блок
        if (!(mc.objectMouseOver instanceof BlockRayTraceResult result)) {
            return;
        }

        // Если настройка «Только кирка» включена, проверяем, что в руках кирка
        if (pickaxeOnly.getValue()) {
            var heldItem = mc.player.getHeldItem(Hand.MAIN_HAND).getItem();
            if (!(heldItem instanceof PickaxeItem)) {
                return;
            }
        }

        // Проверяем процент повреждения блока (curBlockDamageMP)
        if (mc.playerController.curBlockDamageMP > 0.5f) {
            // Получаем целое число из Float
            int times = multiplier.getValue().intValue();

            // Отправляем заданное количество пакетов STOP + ABORT
            for (int i = 0; i < times; i++) {
                mc.player.connection.sendPacket(
                        new CPlayerDiggingPacket(
                                CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK,
                                result.getPos(),
                                result.getFace()
                        )
                );
                mc.player.connection.sendPacket(
                        new CPlayerDiggingPacket(
                                CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK,
                                result.getPos(),
                                result.getFace()
                        )
                );
            }
        }
    }
}
