package org.obsidian.client.managers.other.autobuy.item;

import lombok.Getter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class AutoBuyEnchItem extends AutoBuyItem {
    private final Set<EnchantmentLevel> enchants;

    public AutoBuyEnchItem(Item item, int price, Set<EnchantmentLevel> enchants) {
        super(item, price);
        this.enchants = new HashSet<>(enchants);
    }

    public void addEnchant(Enchantment enchantment, List<Integer> levels) {
        this.enchants.add(new EnchantmentLevel(enchantment, levels));
    }

    @Getter
    public static class EnchantmentLevel {
        private final Enchantment enchantment;
        private final List<Integer> levels;

        public EnchantmentLevel(Enchantment enchantment, List<Integer> levels) {
            this.enchantment = enchantment;
            this.levels = levels;
        }
    }
}