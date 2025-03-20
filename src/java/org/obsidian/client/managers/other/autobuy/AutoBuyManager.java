package org.obsidian.client.managers.other.autobuy;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyEnchItem;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyItem;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyToolTipItem;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.file.FileType;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
@Getter
public class AutoBuyManager extends CopyOnWriteArrayList<AutoBuyItem> {
    public static File AUTOBUY_DIRECTORY;

    public AutoBuyManager() {
        init();
    }

    public void init() {
        AUTOBUY_DIRECTORY = new File(FileManager.DIRECTORY, FileType.AUTOBUY.getName());
        if (!AUTOBUY_DIRECTORY.exists()) {
            if (!AUTOBUY_DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", FileType.AUTOBUY.getName());
                System.exit(0);
            }
        }

        registerAllItems();
    }

    private void registerAllItems() {

        List<AutoBuyItem> items = Lists.newArrayList();

        items.add(new AutoBuyItem(Items.DIAMOND, 5000));
        items.add(new AutoBuyItem(Items.GOLDEN_APPLE, 10000));
        items.add(new AutoBuyItem(Items.EXPERIENCE_BOTTLE, 1500));
        items.add(new AutoBuyItem(Items.FIREWORK_ROCKET, 1500));

        items.add(new AutoBuyItem(Items.ZOMBIE_VILLAGER_SPAWN_EGG, 180000));
        items.add(new AutoBuyItem(Items.VILLAGER_SPAWN_EGG, 400000));
        items.add(new AutoBuyItem(Items.ENCHANTED_GOLDEN_APPLE, 100000));
        items.add(new AutoBuyItem(Items.ELYTRA, 300000));
        items.add(new AutoBuyItem(Items.DRAGON_HEAD, 250000));
        items.add(new AutoBuyItem(Items.WITHER_SKELETON_SKULL, 250000));
        items.add(new AutoBuyItem(Items.TOTEM_OF_UNDYING, 90000));
        items.add(new AutoBuyItem(Items.ANCIENT_DEBRIS, 250000));
        items.add(new AutoBuyItem(Items.NETHERITE_INGOT, 1000000));

        items.add(new AutoBuyToolTipItem(Items.IRON_NUGGET, 30000, Set.of("§b[★]§3 Серебро")));
        items.add(new AutoBuyToolTipItem(Items.TRIPWIRE_HOOK, 400000, Set.of("§9С Броней")));
        items.add(new AutoBuyToolTipItem(Items.TRIPWIRE_HOOK, 200000, Set.of("§9С Инструментами")));
        items.add(new AutoBuyToolTipItem(Items.TRIPWIRE_HOOK, 1750000, Set.of("§9С Ресурсами")));
        items.add(new AutoBuyToolTipItem(Items.TRIPWIRE_HOOK, 1350000, Set.of("§9С Оружием")));
        items.add(new AutoBuyToolTipItem(Items.TRIPWIRE_HOOK, 8000000, Set.of("§9С Сферами")));

        items.add(new AutoBuyToolTipItem(Items.SPLASH_POTION, 1000000, Set.of("[★] Зелье агента")));

        AutoBuyEnchItem.EnchantmentLevel protection = new AutoBuyEnchItem.EnchantmentLevel(Enchantments.PROTECTION, List.of(4, 5));
        AutoBuyEnchItem.EnchantmentLevel mending = new AutoBuyEnchItem.EnchantmentLevel(Enchantments.MENDING, List.of(1));

        Set<AutoBuyEnchItem.EnchantmentLevel> enchants = new HashSet<>();
        enchants.add(protection);
        enchants.add(mending);

        items.add(new AutoBuyEnchItem(Items.NETHERITE_HELMET, 250000, enchants));
        items.add(new AutoBuyEnchItem(Items.NETHERITE_CHESTPLATE, 250000, enchants));
        items.add(new AutoBuyEnchItem(Items.NETHERITE_LEGGINGS, 250000, enchants));
        items.add(new AutoBuyEnchItem(Items.NETHERITE_BOOTS, 250000, enchants));

        items.add(new AutoBuyItem(Items.SHULKER_BOX, 10000));

        this.addAll(items);
    }

    public AutoBuyFile get() {
        final File file = new File(AUTOBUY_DIRECTORY, FileType.AUTOBUY.getName() + Constants.FILE_FORMAT);
        return new AutoBuyFile(file);
    }

    public void set() {
        final File file = new File(AUTOBUY_DIRECTORY, FileType.AUTOBUY.getName() + Constants.FILE_FORMAT);
        AutoBuyFile autoBuyFile = get();
        if (autoBuyFile == null) {
            autoBuyFile = new AutoBuyFile(file);
        }
        autoBuyFile.write();
    }
}