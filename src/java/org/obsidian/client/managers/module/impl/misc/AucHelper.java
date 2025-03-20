package org.obsidian.client.managers.module.impl.misc;

import com.mojang.datafixers.util.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.ChatInputEvent;
import org.obsidian.client.managers.events.other.ContainerRenderEvent;
import org.obsidian.client.managers.events.other.ScoreBoardEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.DelimiterSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.autobuy.AutoBuyUtils;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AucHelper", category = Category.MISC)
public class AucHelper extends Module {
    public static AucHelper getInstance() {
        return Instance.get(AucHelper.class);
    }

    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Дешёвый за",
            BooleanSetting.of("Единицу", true),
            BooleanSetting.of("Слот", true)
    );
    private final BooleanSetting calculator = new BooleanSetting(this, "Калькулятор", false).setVisible(this::isAnyTrueCheck);
    private final BooleanSetting checkDurability = new BooleanSetting(this, "Без повреждений", false).setVisible(this::isAnyTrueCheck);
    private final BooleanSetting checkEnchants = new BooleanSetting(this, "Только с чарами", false).setVisible(this::isAnyTrueCheck);
    private final BooleanSetting checkBalance = new BooleanSetting(this, "Учитывать баланс", false).setVisible(this::isAnyTrueCheck);
    private final BooleanSetting checkFilters = new BooleanSetting(this, "Фильтровать предметы", false).setVisible(this::isAnyTrueCheck);

    private final MultiBooleanSetting armorChecks = new MultiBooleanSetting(this, "Фильтры брони",
            BooleanSetting.of("Защита", false),
            BooleanSetting.of("Прочность", false),
            BooleanSetting.of("Починка", false),
            BooleanSetting.of("Без шипов", false)
    ).setVisible(checkFilters::getValue);

    private final SliderSetting protectionLevel = new SliderSetting(this, "Уровень защиты", 5, 1, 5, 1)
            .setVisible(() -> armorChecks.getValue("Защита"));
    private final SliderSetting armorUnbreakingLevel = new SliderSetting(this, "Уровень Прочности", 5, 1, 5, 1)
            .setVisible(() -> armorChecks.getValue("Прочность"));

    private final MultiBooleanSetting swordChecks = new MultiBooleanSetting(this, "Фильтры Меча",
            BooleanSetting.of("Острота", false),
            BooleanSetting.of("Прочность", false),
            BooleanSetting.of("Починка", false)
    ).setVisible(checkFilters::getValue);

    private final SliderSetting sharpnessLevel = new SliderSetting(this, "Уровень остроты", 7, 1, 7, 1)
            .setVisible(() -> swordChecks.getValue("Острота"));
    private final SliderSetting swordUnbreakingLevel = new SliderSetting(this, "Уровень Прочности", 5, 1, 5, 1)
            .setVisible(() -> swordChecks.getValue("Прочность"));

    private final DelimiterSetting potionsDelimiter = new DelimiterSetting(this, "Фильтры зелий").setVisible(checkFilters::getValue);

    private final MultiBooleanSetting potionsChecks = new MultiBooleanSetting(this, "Содержит эффект",
            BooleanSetting.of("Сила", false), // 1-4
            BooleanSetting.of("Скорость", false), // 1-3
            BooleanSetting.of("Прилив здоровья", false), // 1-3
            BooleanSetting.of("Исцеление", false), // 1-2
            BooleanSetting.of("Регенерация", false), // 1-3
            BooleanSetting.of("Отравление", false), // 1-2
            BooleanSetting.of("Замедление", false), // 1-4
            BooleanSetting.of("Слабость", false), // 1-3
            BooleanSetting.of("Иссушение", false) // 1-5
    ).setVisible(checkFilters::getValue);

    private final SliderSetting strengthLevel = new SliderSetting(this, "Уровень Силы", 3, 1, 4, 1)
            .setVisible(() -> potionsChecks.getValue("Сила"));
    private final SliderSetting speedLevel = new SliderSetting(this, "Уровень Скорости", 3, 1, 3, 1)
            .setVisible(() -> potionsChecks.getValue("Скорость"));
    private final SliderSetting healthBoostLevel = new SliderSetting(this, "Уровень Прилива здоровья", 3, 1, 3, 1)
            .setVisible(() -> potionsChecks.getValue("Прилив здоровья"));
    private final SliderSetting instantHealthBoostLevel = new SliderSetting(this, "Уровень Исцеления", 2, 1, 2, 1)
            .setVisible(() -> potionsChecks.getValue("Исцеление"));
    private final SliderSetting regenerationLevel = new SliderSetting(this, "Уровень Регенерации", 3, 1, 3, 1)
            .setVisible(() -> potionsChecks.getValue("Регенерация"));
    private final SliderSetting poisonLevel = new SliderSetting(this, "Уровень Отравления", 2, 1, 2, 1)
            .setVisible(() -> potionsChecks.getValue("Отравление"));
    private final SliderSetting slownessLevel = new SliderSetting(this, "Уровень Замедления", 4, 1, 4, 1)
            .setVisible(() -> potionsChecks.getValue("Замедление"));
    private final SliderSetting weaknessLevel = new SliderSetting(this, "Уровень Слабости", 3, 1, 3, 1)
            .setVisible(() -> potionsChecks.getValue("Слабость"));
    private final SliderSetting witherLevel = new SliderSetting(this, "Уровень Иссушения", 5, 1, 5, 1)
            .setVisible(() -> potionsChecks.getValue("Иссушение"));

    private boolean isAnyTrueCheck() {
        return checks().isAnyTrue();
    }

    private int balance = -1;

    private void resetBalance() {
        balance = -1;
    }

    @EventHandler
    public void onEvent(ScoreBoardEvent event) {
        if (event.getList().isEmpty()) {
            resetBalance();
            return;
        }
        for (Pair<Score, ITextComponent> pair : event.getList()) {
            String component = TextFormatting.removeFormatting(pair.getSecond().getString());
            if (component.contains("Монет:")) {
                String[] splitted = component.split(":");
                if (splitted.length > 1) {
                    try {
                        this.balance = Integer.parseInt(splitted[1].trim());
                        break;
                    } catch (NumberFormatException ignored) {
                        resetBalance();
                    }
                } else {
                    resetBalance();
                }
            }
        }
    }

    private boolean isValid(ItemStack stack) {
        if (!checkDurability.getValue() && !checkEnchants.getValue() && !checkBalance.getValue() && !checkFilters.getValue()) {
            return true;
        }

        boolean valid = true;

        if (checkDurability.getValue()) {
            valid &= !stack.isDamageable() || stack.getDamage() == 0;
        }
        if (checkEnchants.getValue()) {
            valid &= !stack.isEnchantable() || stack.isEnchanted();
        }
        if (checkBalance.getValue()) {
            int price = AutoBuyUtils.getPrice(stack);
            valid &= this.balance != -1 && price != -1 && price < this.balance;
        }
        if (checkFilters.getValue()) {
            valid &= checkFilters(stack);
        }

        return valid;
    }

    private boolean checkFilters(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        List<ITextComponent> tooltips = AutoBuyUtils.getTooltip(stack);

        boolean valid = true;

        if (stack.isEnchanted()) {
            if (stack.getItem() instanceof ArmorItem armorItem && (armorItem.getArmorMaterial().equals(ArmorMaterial.NETHERITE) || armorItem.getArmorMaterial().equals(ArmorMaterial.DIAMOND)) && armorChecks.isAnyTrue()) {
                if (armorChecks.getValue("Защита")) {
                    valid &= validEnchant(enchantments, Enchantments.PROTECTION, protectionLevel().getValue().intValue());
                }
                if (armorChecks.getValue("Прочность")) {
                    valid &= validEnchant(enchantments, Enchantments.UNBREAKING, armorUnbreakingLevel().getValue().intValue());
                }
                if (armorChecks.getValue("Починка")) {
                    valid &= validEnchant(enchantments, Enchantments.MENDING, 1);
                }
                if (armorChecks.getValue("Без шипов")) {
                    valid &= !enchantments.containsKey(Enchantments.THORNS);
                }
                return valid;
            } else if (stack.getItem() instanceof SwordItem swordItem && (swordItem.getTier().equals(ItemTier.NETHERITE) || swordItem.getTier().equals(ItemTier.DIAMOND)) && swordChecks.isAnyTrue()) {
                if (swordChecks.getValue("Острота")) {
                    valid &= validEnchant(enchantments, Enchantments.SHARPNESS, sharpnessLevel().getValue().intValue());
                }
                if (swordChecks.getValue("Прочность")) {
                    valid &= validEnchant(enchantments, Enchantments.UNBREAKING, swordUnbreakingLevel().getValue().intValue());
                }
                if (swordChecks.getValue("Починка")) {
                    valid &= validEnchant(enchantments, Enchantments.MENDING, 1);
                }
                return valid;
            }
            return false;
        }

        if (stack.getItem() instanceof PotionItem && potionsChecks.isAnyTrue()) {
            if (potionsChecks.getValue("Сила")) {
                valid &= validPotion(tooltips, Effects.STRENGTH, strengthLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Скорость")) {
                valid &= validPotion(tooltips, Effects.SPEED, speedLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Прилив здоровья")) {
                valid &= validPotion(tooltips, Effects.HEALTH_BOOST, healthBoostLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Исцеление")) {
                valid &= validPotion(tooltips, Effects.INSTANT_HEALTH, instantHealthBoostLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Регенерация")) {
                valid &= validPotion(tooltips, Effects.REGENERATION, regenerationLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Отравление")) {
                valid &= validPotion(tooltips, Effects.POISON, poisonLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Замедление")) {
                valid &= validPotion(tooltips, Effects.SLOWNESS, slownessLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Слабость")) {
                valid &= validPotion(tooltips, Effects.WEAKNESS, weaknessLevel().getValue().intValue());
            }
            if (potionsChecks.getValue("Иссушение")) {
                valid &= validPotion(tooltips, Effects.WITHER, witherLevel().getValue().intValue());
            }
            return valid;
        }
        return false;
    }

    private boolean validEnchant(Map<Enchantment, Integer> enchantments, Enchantment enchantment, int minLevel) {
        return enchantments.containsKey(enchantment) && enchantments.getOrDefault(enchantment, 0) >= minLevel;
    }

    private boolean validPotion(List<ITextComponent> tooltips, Effect effect, int minLevel) {
        return hasPotion(tooltips, effect) && getPotionLevel(tooltips, effect) >= minLevel;
    }

    private boolean hasPotion(List<ITextComponent> tooltips, Effect effect) {
        return tooltips.stream().anyMatch(component -> TextFormatting.removeFormatting(component.getString()).contains(TextFormatting.removeFormatting(effect.getDisplayName().getString())));
    }

    private int getPotionLevel(List<ITextComponent> tooltips, Effect effect) {
        Pattern pattern = Pattern.compile(TextFormatting.removeFormatting(effect.getDisplayName().getString()) + "(?:\\s([IVX]+))?");

        for (ITextComponent tooltip : tooltips) {
            Matcher matcher = pattern.matcher(TextFormatting.removeFormatting(tooltip.getString()));
            if (matcher.find()) {
                if (matcher.group(1) != null) {
                    return RomanConverter.romanToInteger(matcher.group(1));
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }

    @EventHandler
    public void onEvent(ContainerRenderEvent event) {
        final List<PriceSlot> priceSlots = new ArrayList<>();
        final List<PriceSlot> pricesWithCountSlot = new ArrayList<>();

        final MatrixStack matrix = event.getMatrix();
        final Container container = event.getContainer();
        final ITextComponent title = event.getTitle();

        for (int i = 0; i < container.inventorySlots.size() - 36; ++i) {
            Slot slot = container.inventorySlots.get(i);

            ItemStack stack = slot.getStack();
            if (!isValid(stack)) continue;
            if (!stack.isEmpty() && container instanceof ChestContainer && AutoBuyUtils.isSearchScreen(title.getString())) {
                if (AutoBuyUtils.getPrice(stack) != -1) {
                    int price = AutoBuyUtils.getPrice(stack);
                    priceSlots.add(new PriceSlot(i, price));
                    pricesWithCountSlot.add(new PriceSlot(i, price / stack.getCount()));
                }
            }
        }

        priceSlots.sort(Comparator.comparingInt(o -> o.price));
        pricesWithCountSlot.sort(Comparator.comparingInt(o -> o.price));

        float stackSize = 16;

        if (!priceSlots.isEmpty() && AutoBuyUtils.isSearchScreen(title.getString())) {
            Slot minSlot = container.inventorySlots.get(priceSlots.get(0).slotIndex);
            Slot minSlotWithCount = container.inventorySlots.get(pricesWithCountSlot.get(0).slotIndex);
            for (Slot slot : container.inventorySlots) {
                if (!slot.getHasStack()
                        || slot.equals(minSlot)
                        || slot.equals(minSlotWithCount)
                        || slot.slotNumber >= 45
                        || (!checkBalance.getValue() || AutoBuyUtils.getPrice(slot.getStack()) > this.balance))
                    continue;
                RectUtil.drawRect(matrix, slot.xPos, slot.yPos, stackSize, stackSize, ColorUtil.multAlpha(ColorUtil.RED, 0.5F));
            }
            if (checks.getValue("Слот")) {
                if (minSlotWithCount != minSlot) {
                    RectUtil.drawRect(matrix, minSlot.xPos, minSlot.yPos, stackSize, stackSize, ColorUtil.multAlpha(ColorUtil.GREEN, 0.5F));
                }
            }
            if (checks.getValue("Единицу")) {
                RectUtil.drawRect(matrix, minSlotWithCount.xPos, minSlotWithCount.yPos, stackSize, stackSize, ColorUtil.multAlpha(ColorUtil.BLUE, 0.5f));
            }
        }
    }

    @EventHandler
    public void onEvent(ChatInputEvent event) {
        final String message = event.getMessage().toLowerCase();
        final String sellCommand = "/ah sell";
        if (calculator.getValue() && message.startsWith(sellCommand)) {
            String math = message.substring(sellCommand.length()).trim();

            if (!math.isEmpty()) {
                int result = calculate(math);
                if (result != -1) {
                    event.setMessage(sellCommand + " " + result);
                } else {
                    ChatUtil.addTextWithError("Ошибка при вычислении стоимости предмета.");
                    event.cancel();
                }
            } else {
                ChatUtil.addTextWithError("Не указано математическое выражение.");
                event.cancel();
            }
        }

    }

    private int calculate(String math) {
        try {
            Expression expression = new ExpressionBuilder(math).build();
            return (int) expression.evaluate();
        } catch (Exception ignored) {
        }
        return -1;
    }

    private record PriceSlot(int slotIndex, int price) {
    }

    private static class RomanConverter {
        private static final Map<Integer, String> ROMAN_VALUES = Map.ofEntries(
                Map.entry(10, "X"), Map.entry(9, "IX"), Map.entry(5, "V"), Map.entry(4, "IV"), Map.entry(1, "I")
        );

        public static String toRoman(int number) {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<Integer, String> entry : ROMAN_VALUES.entrySet()) {
                int repeat = number / entry.getKey();
                if (repeat > 0) {
                    result.append(entry.getValue().repeat(repeat));
                    number %= entry.getKey();
                }
            }
            return result.toString();
        }

        public static int romanToInteger(String roman) {
            int result = 0;
            int previousValue = 0;

            for (int i = roman.length() - 1; i >= 0; i--) {
                int finalValue = i;
                int currentValue = ROMAN_VALUES.entrySet()
                        .stream()
                        .filter(e -> e.getValue().equals(String.valueOf(roman.charAt(finalValue))))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Roman numeral: " + roman.charAt(finalValue)));

                result += (currentValue < previousValue) ? -currentValue : currentValue;
                previousValue = currentValue;
            }
            return result;
        }
    }

}