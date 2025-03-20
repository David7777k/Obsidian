package org.obsidian.client.utils.autobuy;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.experimental.UtilityClass;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.module.impl.misc.AutoBuy;
import org.obsidian.client.managers.other.autobuy.AutoBuyManager;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyEnchItem;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyItem;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyToolTipItem;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class AutoBuyUtils implements IMinecraft {
    public final Pattern auctionPattern = Pattern.compile("\\[☃] Аукционы \\[\\d+/\\d+]");
    private final Pattern playerAuctionPattern = Pattern.compile("\\[☃] Аукционы \\((\\w+)\\) \\[\\d+/\\d+]");
    public final Pattern pricePattern = Pattern.compile("\\$(\\d+(?:\\s\\d{3})*(?:\\.\\d{2})?)");
    public final Pattern sellerPattern = Pattern.compile("\"color\":\"white\",\"text\":\"Прoдaвeц:\"},\\{\"color\":\"gold\",\"text\":\"(.*?)\"");

    public boolean matchesPattern(String title, Pattern pattern) {
        Matcher matcher = pattern.matcher(title);
        return matcher.find();
    }

    public boolean isAuction(String title) {
        title = TextFormatting.removeFormatting(title);
        if (PlayerUtil.isFuntime()) {
            return matchesPattern(title, auctionPattern);
        }
        return false;
    }

    public boolean isPlayerAuction(String title) {
        title = TextFormatting.removeFormatting(title);
        if (PlayerUtil.isFuntime()) {
            return matchesPattern(title, playerAuctionPattern);
        }
        return false;
    }

    public boolean isSearchScreen(String title) {
        title = TextFormatting.removeFormatting(title);
        if (PlayerUtil.isFuntime()) {
            return (title.startsWith("Поиск: ") || title.contains("Аукционы ")) && title.contains("[") && title.contains("/") && title.contains("]");
        }
        return false;
    }

    public boolean isAcceptScreen(String title) {
        title = TextFormatting.removeFormatting(title);
        if (PlayerUtil.isFuntime()) {
            return title.contains("[☃]") && title.contains("Подтверждение покупки");
        }
        return false;
    }

    public boolean isSuspectPriceScreen(String title) {
        title = TextFormatting.removeFormatting(title);
        if (PlayerUtil.isFuntime()) {
            return title.contains("[☃]") && title.contains("Подозрительная цена");
        }
        return false;
    }

    public boolean isContainerScreen(String title) {
        title = TextFormatting.removeFormatting(title);
        if (PlayerUtil.isFuntime()) {
            return title.contains("[☃]") && title.contains("Хранилище") && title.contains("[") && title.contains("/") && title.contains("]");
        }
        return false;
    }

    public String getSeller(ItemStack stack) {
        if (stack.getTag() != null) {
            String lore = stack.getTag().getString().replaceAll(" ", "").trim();
            Matcher matcher = sellerPattern.matcher(lore);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    public static int getPrice(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();
        if (tag == null) return -1;
        String price = StringUtils.substringBetween(tag.toString(), "\"text\":\" $", "\"}]");
        if (price == null || price.isEmpty()) return -1;
        price = price.replaceAll(" ", "").replaceAll(",", "");
        return Integer.parseInt(price);
    }

    public boolean isValid(Slot slot) {
        ItemStack stack = slot.getStack();
        Item item = stack.getItem();
        int price = AutoBuyUtils.getPrice(stack);
        int balance = AutoBuy.getInstance().balance();
        String seller = AutoBuyUtils.getSeller(stack).toLowerCase();
        if (balance != -1 && price != -1 && balance >= price && !seller.isEmpty() && !PlayerUtil.isInvalidName(seller)) {

            Map<String, Item> blackList = new HashMap<>();

            // black list items
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"Э\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A40000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B30000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C20000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D20000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E10000\",\"text\":\"ы \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F00000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#FF0000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F00000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E10000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D20000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C20000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B30000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A40000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.ELYTRA);
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"Ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A70000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B80000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#CA0000\",\"text\":\"м \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#DC0000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#ED0000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#FF0000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#ED0000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#DC0000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#CA0000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B80000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A70000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.NETHERITE_HELMET);
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"Н\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A10000\",\"text\":\"а\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#AE0000\",\"text\":\"г\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#BA0000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C70000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D30000\",\"text\":\"д\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E00000\",\"text\":\"н\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#EC0000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F90000\",\"text\":\"к \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F90000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#EC0000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E00000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D30000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C70000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#BA0000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#AE0000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A10000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.NETHERITE_CHESTPLATE);
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"П\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A40000\",\"text\":\"о\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B30000\",\"text\":\"н\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C20000\",\"text\":\"о\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D20000\",\"text\":\"ж\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E10000\",\"text\":\"и \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F00000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#FF0000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F00000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E10000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D20000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C20000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B30000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A40000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.NETHERITE_LEGGINGS);
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"Б\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A30000\",\"text\":\"о\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B10000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#BF0000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#CE0000\",\"text\":\"н\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#DC0000\",\"text\":\"к\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#EA0000\",\"text\":\"и \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F80000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F80000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#EA0000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#DC0000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#CE0000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#BF0000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B10000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A30000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.NETHERITE_BOOTS);
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A50000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B60000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C60000\",\"text\":\"к\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D60000\",\"text\":\"а \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E70000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F70000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F70000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E70000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#D60000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#C60000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#B60000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A50000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.NETHERITE_PICKAXE);
            blackList.put("Name:'{\"extra\":[{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"text\":\" \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"М\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A80000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#BC0000\",\"text\":\"ч \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#CF0000\",\"text\":\"К\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E20000\",\"text\":\"р\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F50000\",\"text\":\"у\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#F50000\",\"text\":\"ш\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#E20000\",\"text\":\"и\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#CF0000\",\"text\":\"т\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#BC0000\",\"text\":\"е\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#A80000\",\"text\":\"л\"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"#950000\",\"text\":\"я \"},{\"bold\":true,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":true,\"color\":\"#950000\",\"text\":\"xxx\"},{\"bold\":false,\"italic\":false,\"obfuscated\":false,\"text\":\"\"}],\"text\":\"\"}'}}", Items.NETHERITE_SWORD);
            blackList.put("{lvl:1s,id:\"minecraft:mending\"}", Items.ENCHANTED_BOOK);
            blackList.put(Items.NETHERITE_AXE.getTranslationKey(), Items.NETHERITE_AXE);
            blackList.put(Items.ZOMBIE_VILLAGER_SPAWN_EGG.getTranslationKey(), Items.ZOMBIE_VILLAGER_SPAWN_EGG);
            blackList.put(Items.ENCHANTED_GOLDEN_APPLE.getTranslationKey(), Items.ENCHANTED_GOLDEN_APPLE);
            blackList.put(Items.WITHER_SKELETON_SKULL.getTranslationKey(), Items.WITHER_SKELETON_SKULL);
            blackList.put(Items.VILLAGER_SPAWN_EGG.getTranslationKey(), Items.VILLAGER_SPAWN_EGG);
            blackList.put(Items.EXPERIENCE_BOTTLE.getTranslationKey(), Items.EXPERIENCE_BOTTLE);
            blackList.put(Items.NETHERITE_SCRAP.getTranslationKey(), Items.NETHERITE_SCRAP);
            blackList.put(Items.BLAZE_SPAWN_EGG.getTranslationKey(), Items.BLAZE_SPAWN_EGG);
            blackList.put(Items.SKELETON_SKULL.getTranslationKey(), Items.SKELETON_SKULL);
            blackList.put(Items.ANCIENT_DEBRIS.getTranslationKey(), Items.ANCIENT_DEBRIS);
            blackList.put(Items.CREEPER_HEAD.getTranslationKey(), Items.CREEPER_HEAD);
            blackList.put(Items.GOLDEN_APPLE.getTranslationKey(), Items.GOLDEN_APPLE);
            blackList.put(Items.NETHER_STAR.getTranslationKey(), Items.NETHER_STAR);
            blackList.put(Items.ZOMBIE_HEAD.getTranslationKey(), Items.ZOMBIE_HEAD);
            blackList.put(Items.DRAGON_HEAD.getTranslationKey(), Items.DRAGON_HEAD);
            blackList.put(Items.PLAYER_HEAD.getTranslationKey(), Items.PLAYER_HEAD);
            blackList.put(Items.GUNPOWDER.getTranslationKey(), Items.GUNPOWDER);
            blackList.put(Items.SPAWNER.getTranslationKey(), Items.SPAWNER);
            blackList.put(Items.BEACON.getTranslationKey(), Items.BEACON);
            blackList.put(Items.TNT.getTranslationKey(), Items.TNT);
            blackList.put(Items.EMERALD_ORE.getTranslationKey(), Items.EMERALD_ORE);
            blackList.put(Items.ENDER_PEARL.getTranslationKey(), Items.ENDER_PEARL);
            blackList.put(Items.VEX_SPAWN_EGG.getTranslationKey(), Items.VEX_SPAWN_EGG);
            blackList.put(Items.BAMBOO.getTranslationKey(), Items.BAMBOO);
            blackList.put(Items.DIAMOND.getTranslationKey(), Items.DIAMOND);
            blackList.put(Items.NETHERITE_INGOT.getTranslationKey(), Items.NETHERITE_INGOT);

            blackList.put(Items.NETHERITE_HELMET.getTranslationKey(), Items.NETHERITE_HELMET);
            blackList.put(Items.NETHERITE_CHESTPLATE.getTranslationKey(), Items.NETHERITE_CHESTPLATE);
            blackList.put(Items.NETHERITE_LEGGINGS.getTranslationKey(), Items.NETHERITE_LEGGINGS);
            blackList.put(Items.NETHERITE_BOOTS.getTranslationKey(), Items.NETHERITE_BOOTS);

            IntSet blackListPrice = new IntArraySet();

            blackListPrice.add(10);
            blackListPrice.add(100);
            blackListPrice.add(1000);
            blackListPrice.add(10000);
            blackListPrice.add(25000);
            blackListPrice.add(70000);
            blackListPrice.add(100000);
            blackListPrice.add(200000);
            blackListPrice.add(230000);
            blackListPrice.add(250000);
            blackListPrice.add(300000);
            blackListPrice.add(350000);
            blackListPrice.add(1000000);

            boolean containsValue = false;
            for (Map.Entry<String, Item> val : blackList.entrySet()) {
                if (stack.getOrCreateTag().getString().contains(val.getKey()) || val.getKey().equals(val.getValue().getTranslationKey())) {
                    containsValue = true;
                    break;
                }
            }
            if (blackListPrice.contains(price) && ((blackList.containsValue(item) && containsValue) || stack.getOrCreateTag().getString().contains("★"))) {
                return false;
            }
            return checkValidItem(stack, price);
        }
        return false;
    }

    private boolean checkValidItem(ItemStack stack, int price) {
        int oneItemPrice = price / stack.getCount();

        AutoBuyManager manager = Obsidian.inst().autoBuyManager();
        for (AutoBuyItem entry : manager) {
            if (!entry.isEnabled()) continue;
            if (entry instanceof AutoBuyToolTipItem nameItem) {
                List<ITextComponent> tooltip = getTooltip(stack);

                boolean find = tooltip.stream().anyMatch(textComponent -> {
                    for (String abItemToolTip : nameItem.getToolTips()) {
                        if (TextFormatting.removeFormatting(textComponent.getString()).equals(TextFormatting.removeFormatting(abItemToolTip))) {
                            return true;
                        }
                    }
                    return false;
                });

                if (!find) return false;
            }

            if (entry instanceof AutoBuyEnchItem enchItem) {
                for (Map.Entry<Enchantment, Integer> stackEnchant : EnchantmentHelper.getEnchantments(stack).entrySet()) {
                    for (AutoBuyEnchItem.EnchantmentLevel enchant : enchItem.getEnchants()) {
                        if (enchant.getEnchantment() != stackEnchant.getKey()) {
                            continue;
                        }
                        if (enchant.getLevels().stream().noneMatch(level -> Objects.equals(level, stackEnchant.getValue()))) {
                            return false;
                        }
                    }
                }
            }

            if (stack.getItem().equals(entry.getItem())) {

                if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    for (ItemStack shulkerBoxItem : getShulkerBoxItems(stack)) {
                        if (!shulkerBoxItem.isEmpty()) {
                            for (AutoBuyItem shulk : manager) {
                                if (shulkerBoxItem.getItem().equals(shulk.getItem())) {
                                    int shulkItemPrice = price / shulkerBoxItem.getCount();
                                    return AutoBuy.getInstance().balance() != -1 && price != -1 && shulkItemPrice <= shulk.getPrice();
                                }
                            }
                        }
                    }
                }

                return AutoBuy.getInstance().balance() != -1 && price != -1 && oneItemPrice <= entry.getPrice();
            }
        }

        return false;
    }

    public List<ItemStack> getShulkerBoxItems(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof ShulkerBoxBlock) || !stack.hasTag()) {
            return items;
        }

        CompoundNBT blockEntityTag = stack.getTag().getCompound("BlockEntityTag");

        if (blockEntityTag.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(blockEntityTag, items);
        }

        return items;
    }

    public List<ITextComponent> getTooltip(ItemStack stack) {
        return stack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
    }
}