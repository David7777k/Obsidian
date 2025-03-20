package org.obsidian.client.managers.other.autobuy.item;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class AutoBuyToolTipItem extends AutoBuyItem {
    private Set<String> toolTips;

    public AutoBuyToolTipItem(Item item, int price, Set<String> toolTips) {
        super(item, price);
        this.toolTips = new HashSet<>(toolTips);
    }
}
