package org.obsidian.client.managers.other.autobuy.item;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;


@Getter
@Setter
public class AutoBuyItem {
    private final Item item;
    private int price;
    private boolean enabled = true;

    public AutoBuyItem(Item item, int price) {
        this.item = item;
        this.price = price;
    }
}