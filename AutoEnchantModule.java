package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.utils.other.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ReflectiveOperationException;
import java.util.HashSet;
import java.util.Set;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoEnchant", category = Category.PLAYER)
public class AutoEnchant extends Module {

    /* ---------- Статичні та константні поля ---------- */

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoEnchant.class);

    private static final String ENCHANTMENT_LEVELS_FIELD = "enchantmentLevels";
    /**
     * Кешоване поле з EnchantmentMenu, отримане через рефлексію один раз
     */
    private static final Field LEVELS_FIELD;

    static {
        Field tmp;
        try {
            tmp = EnchantmentMenu.class.getDeclaredField(ENCHANTMENT_LEVELS_FIELD);
            tmp.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
        LEVELS_FIELD = tmp;
    }

    /* ---------- Налаштування ---------- */

    final BooleanSetting automaticEnchantingEnabled = new BooleanSetting(this, "AutoOnOpen", true);

    /* ---------- Змінні стану ---------- */

    int lastContainerId = -1;                      // id відкритого меню
    final Set<Integer> alreadyEnchantedSlots = new HashSet<>();

    /* ---------- Синглтон ---------- */

    public static AutoEnchant getInstance() {
        return Instance.get(AutoEnchant.class);
    }

    /* ---------- Події ---------- */

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!automaticEnchantingEnabled.getValue()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof EnchantmentScreen enchantmentScreen)) {
            return;
        }

        processEnchanting(enchantmentScreen);
    }

    /* ---------- Логіка модуля ---------- */

    private void processEnchanting(EnchantmentScreen screen) {
        EnchantmentMenu menu = screen.getMenu();

        // Нова “ковадла” → скид кешу
        if (menu.containerId != lastContainerId) {
            lastContainerId = menu.containerId;
            alreadyEnchantedSlots.clear();
        }

        int[] levels;
        try {
            levels = getEnchantmentLevels(menu);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Не вдалося отримати рівні зачарувань:", e);
            return;
        }

        sendEnchantmentPackets(menu.containerId, levels);
    }

    private int[] getEnchantmentLevels(EnchantmentMenu menu) throws ReflectiveOperationException {
        return (int[]) LEVELS_FIELD.get(menu);
    }

    private void sendEnchantmentPackets(int windowId, int[] levels) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return;
        }

        for (int i = 0; i < levels.length; i++) {
            // Пропускаємо, якщо рівень 0 або вже пробували
            if (levels[i] <= 0 || !alreadyEnchantedSlots.add(i)) {
                continue;
            }
            connection.send(new ServerboundContainerButtonClickPacket(windowId, i));
        }
    }
}