package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.component.impl.target.TargetComponent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.events.render.RenderNameEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.combat.AntiBot;
import org.obsidian.client.managers.module.impl.misc.Globals;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.GLUtil;
import org.obsidian.client.utils.render.draw.Project;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.common.impl.globals.ClientAPI;
import org.obsidian.common.impl.globals.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Nametags", category = Category.RENDER)
public class Nametags extends Module {
    // Constants
    private static final int DAMAGE_EFFECT_DURATION = 600;
    private static final int MOB_ATTACK_TIME_THRESHOLD = 1000;
    private static final int FALL_DAMAGE_THRESHOLD = 3;
    private static final float ITEM_STACK_SIZE = 8.0f;
    private static final float ITEM_STACK_SPACING = 2.0f;
    private static final float ARMOR_Y_OFFSET = 12.0f;
    private static final float NAME_TAG_PADDING = 1.0f;
    private static final float SNEAK_OFFSET = 0.1f;
    private static final float STANDING_OFFSET = 0.0f;
    private static final float ENTITY_HEIGHT_MODIFIER = 0.2f;
    private static final float ITEM_SCALE = 0.5f;

    // Cached colors as TextFormatting strings
    private static final Map<TextFormatting, String> COLOR_CACHE = new ConcurrentHashMap<>();

    // Settings
    private final MultiBooleanSetting entityTypes = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Предметы", false)
    );
    private final SliderSetting fontSize = new SliderSetting(this, "Размер шрифта", 8F, 6F, 12F, 0.1F);
    private final BooleanSetting optimized = new BooleanSetting(this, "Оптимизировать", true);
    private final BooleanSetting renderDonat = new BooleanSetting(this, "Отображать донат", true);
    private final BooleanSetting showArmor = new BooleanSetting(this, "Отображать броню", true);

    // Instance fields
    private final int backgroundColorWithAlpha = ColorUtil.getColor(0, 128);
    private final Font font = Fonts.SF_MEDIUM;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Nametags-Processor");
        thread.setDaemon(true);
        return thread;
    });

    // Singleton accessor
    public static Nametags getInstance() {
        return Instance.get(Nametags.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        executorService.shutdown();
    }

    /**
     * Handles rendering player nametags
     */
    @EventHandler
    public void onRenderName(RenderNameEvent event) {
        if (event.getEntity() instanceof AbstractClientPlayerEntity) {
            event.cancel();
        }
    }

    /**
     * Handles damage packets to add visual effects to player nametags
     */
    @EventHandler
    public void onPacket(PacketEvent e) {
        if (PlayerUtil.nullCheck()) return;

        IPacket<?> packet = e.getPacket();
        if (!(packet instanceof SEntityStatusPacket entityStatus)) return;

        Entity entity = entityStatus.getEntity(mc.world);

        // Process only player damage packets
        if (!(entity instanceof PlayerEntity player)) return;

        byte opCode = entityStatus.getOpCode();

        if (opCode == 2 || opCode == 33) {
            processPlayerDamagePacket(player);
        }
    }

    /**
     * Processes player damage to add visual indicators
     */
    private void processPlayerDamagePacket(PlayerEntity player) {
        // Skip if it's fall damage (fall distance reset)
        if (player.prevFallDistance > FALL_DAMAGE_THRESHOLD && player.fallDistance == 0) {
            return;
        }

        // Check if damage was caused by a mob
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof MobEntity mob) {
                if (mob.getLastAttackedEntity() == player &&
                        mob.getLastAttackedEntityTime() < MOB_ATTACK_TIME_THRESHOLD) {
                    return;
                }
            }
        }

        // Add unluck effect for visual indication of damage
        player.addPotionEffect(new EffectInstance(Effects.UNLUCK, DAMAGE_EFFECT_DURATION, -1));
    }

    /**
     * Main rendering handler
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        float currentFontSize = fontSize.getValue();

        // Render player nametags
        if (entityTypes.getValue("Игроки")) {
            TargetComponent.getTargets(128, this::isValidEntity, false)
                    .forEach(entity -> renderEntityNametag(entity, event.getMatrix(), currentFontSize));
        }

        // Render item nametags
        if (entityTypes.getValue("Предметы")) {
            mc.world.loadedItemEntityList()
                    .forEach(entity -> renderEntityNametag(entity, event.getMatrix(), currentFontSize));
        }
    }

    /**
     * Renders a nametag for the specified entity
     */
    private void renderEntityNametag(Entity entity, MatrixStack matrix, float fontHeight) {
        // Get entity position and bounding box
        Vector3d interpolatedPos = RenderUtil3D.interpolate(entity, mc.getRenderPartialTicks());
        AxisAlignedBB entityBox = calculateEntityBoundingBox(entity, interpolatedPos);

        // Project 3D coordinates to 2D screen space
        Vector4f screenBounds = calculateScreenBounds(entityBox);
        if (screenBounds == null) return;

        float x = screenBounds.x;
        float y = screenBounds.y - fontHeight;
        float width = screenBounds.z - x;
        float centerX = x + (width / 2F);

        // Create and render the appropriate nametag
        if (entityTypes.getValue("Игроки") && entity instanceof PlayerEntity) {
            renderPlayerNametag(matrix, (PlayerEntity) entity, centerX, y, fontHeight);
        } else if (entityTypes.getValue("Предметы") && entity instanceof ItemEntity) {
            renderItemEntityNametag(matrix, (ItemEntity) entity, centerX, y, fontHeight);
        }
    }

    /**
     * Renders a nametag for a player entity
     */
    private void renderPlayerNametag(MatrixStack matrix, PlayerEntity player, float centerX, float y, float fontHeight) {
        // Create player tag component with all info
        ITextComponent tagComponent = createPlayerTagComponent(player);
        float nameWidth = getTextWidth(tagComponent);

        // Draw the nametag background and text
        drawNameTagBackground(matrix, centerX, y, nameWidth, fontHeight);
        font.drawTextComponent(matrix, tagComponent, centerX - nameWidth / 2F, y, -1, false, fontHeight);

        // Render player equipment if enabled
        if (showArmor().getValue()) {
            renderPlayerEquipment(matrix, player, centerX - nameWidth / 2F, y, nameWidth, fontHeight);
        }

        // Render client icon if Globals module is enabled
        if (Globals.getInstance().isEnabled()) {
            renderClientIcon(matrix, player, centerX, y, nameWidth, fontHeight);
        }
    }

    /**
     * Renders a nametag for an item entity
     */
    private void renderItemEntityNametag(MatrixStack matrix, ItemEntity itemEntity, float centerX, float y, float fontHeight) {
        ItemStack item = itemEntity.getItem();

        // Create item tag component
        ITextComponent tagComponent = new StringTextComponent("").append(item.getDisplayName());
        if (item.getCount() > 1) {
            String grayColor = getColorString(TextFormatting.GRAY);
            tagComponent = tagComponent.deepCopy().appendString(grayColor + " " + item.getCount() + "x");
        }

        float nameWidth = getTextWidth(tagComponent);

        // Draw the nametag background and text
        drawNameTagBackground(matrix, centerX, y, nameWidth, fontHeight);
        font.drawTextComponent(matrix, tagComponent, centerX - nameWidth / 2F, y, -1, false, fontHeight);
    }

    /**
     * Creates a text component for player nametags with all relevant information
     */
    private ITextComponent createPlayerTagComponent(PlayerEntity player) {
        // Get items and NBT data
        ItemStack offHandStack = player.getHeldItemOffhand();
        CompoundNBT offHandNBT = offHandStack.getTag();

        // Cache color strings
        String redColor = getColorString(TextFormatting.RED);
        String greenColor = getColorString(TextFormatting.GREEN);
        String darkRedColor = getColorString(TextFormatting.DARK_RED);
        String goldColor = getColorString(TextFormatting.GOLD);
        String whiteColor = getColorString(TextFormatting.WHITE);

        // Build tag parts
        String sphereInfo = getSphereInfo(player, offHandStack, offHandNBT, goldColor, redColor, whiteColor);
        String damageTimerInfo = getDamageTimerInfo(player, redColor, whiteColor);
        String healthInfo = getHealthInfo(player, redColor, whiteColor);
        String friendInfo = getFriendInfo(player, greenColor, whiteColor);
        String botInfo = getBotInfo(player, darkRedColor, whiteColor);

        // Get base player name
        ITextComponent playerName = renderDonat.getValue() ? player.getDisplayName() : player.getName();

        // Build final tag component
        if (optimized().getValue()) {
            // Optimized version - single string
            return new StringTextComponent("").appendString(
                    playerName.getString() + healthInfo + sphereInfo + friendInfo + botInfo + damageTimerInfo);
        } else {
            // Standard version - preserves formatting of player name
            return playerName.deepCopy().appendString(
                    healthInfo + sphereInfo + friendInfo + botInfo + damageTimerInfo);
        }
    }

    /**
     * Gets sphere/talisman info string
     */
    private String getSphereInfo(PlayerEntity player, ItemStack offHandStack, CompoundNBT offHandNBT,
                                 String goldColor, String redColor, String whiteColor) {
        if (offHandNBT == null) return "";

        if (PlayerUtil.isFuntime()) {
            int tsLevel = offHandNBT.getInt("tslevel");
            if (tsLevel != 0) {
                return " [" + goldColor + offHandNBT.getString("don-item").replace("sphere-", "").toUpperCase()
                        + redColor + " " + tsLevel + "/3" + whiteColor + "]";
            }
        } else if (PlayerUtil.isHoly()) {
            String itemName = offHandStack.getDisplayName().getString().toLowerCase();
            if (itemName.contains("талисман") || itemName.contains("сфера")) {
                return " [" + goldColor + itemName.replace("талисман", "").replace("сфера", "")
                        .replace(" ", "").replace("-", "") + whiteColor + "]";
            }
        }

        return "";
    }

    /**
     * Gets damage timer info string
     */
    private String getDamageTimerInfo(PlayerEntity player, String redColor, String whiteColor) {
        for (EffectInstance effect : player.getActivePotionEffects()) {
            if (effect.getDuration() != 0 && effect.getPotion() == Effects.UNLUCK && effect.getAmplifier() == -1) {
                String timeDisplay = StringUtils.ticksToElapsedTime(effect.getDuration());
                if (effect.getDuration() >= 200) {
                    timeDisplay = timeDisplay.replace("0:", "");
                } else {
                    timeDisplay = timeDisplay.replace("0:0", "");
                }
                return " [" + redColor + timeDisplay + whiteColor + "]";
            }
        }
        return "";
    }

    /**
     * Gets health info string
     */
    private String getHealthInfo(PlayerEntity player, String redColor, String whiteColor) {
        if (PlayerUtil.isFuntime() && player.getHealthFixed() == 1000) {
            return "";
        }
        return " [" + redColor + player.getHealthFixed() + " HP" + whiteColor + "]";
    }

    /**
     * Gets friend status info string
     */
    private String getFriendInfo(PlayerEntity player, String greenColor, String whiteColor) {
        return Obsidian.inst().friendManager().isFriend(player.getGameProfile().getName())
                ? greenColor + " [F]" + whiteColor
                : "";
    }

    /**
     * Gets bot detection info string
     */
    private String getBotInfo(PlayerEntity player, String darkRedColor, String whiteColor) {
        return AntiBot.getInstance().isBot(player)
                ? darkRedColor + " [BOT]" + whiteColor
                : "";
    }

    /**
     * Renders the client icon for a player if Globals module is enabled
     */
    private void renderClientIcon(MatrixStack matrix, PlayerEntity player, float centerX, float y,
                                  float nameWidth, float fontHeight) {
        String clientName = ClientAPI.getClient(player.getName().getString());
        if (clientName == null) return;

        String userImageURL = "https://rockstar.moscow/api/globals/" + clientName + ".png";
        mc.getTextureManager().bindTexture(Converter.getResourceLocation(userImageURL));

        float iconSize = fontHeight + 1;
        RectUtil.drawRect(matrix, centerX - nameWidth / 2F - iconSize, y - 0.5f,
                iconSize, iconSize, -1, false, true);
    }

    /**
     * Renders player equipment (armor and held items)
     */
    private void renderPlayerEquipment(MatrixStack matrix, PlayerEntity player, float x, float y,
                                       float width, float fontHeight) {
        List<ItemStack> equipment = getPlayerEquipment(player);
        if (equipment.isEmpty()) return;

        float centerX = x + width / 2F;
        float posX = centerX - (equipment.size() * (ITEM_STACK_SIZE + ITEM_STACK_SPACING)) / 2;
        float posY = y - ARMOR_Y_OFFSET;

        for (ItemStack item : equipment) {
            if (item.isEmpty()) continue;

            GLUtil.startScale(posX + (ITEM_STACK_SIZE / 2F), posY + (ITEM_STACK_SIZE / 2F), ITEM_SCALE);
            drawItemStack(matrix, item, posX, posY, true);
            GLUtil.endScale();

            posX += (ITEM_STACK_SIZE + ITEM_STACK_SPACING);
        }
    }

    /**
     * Gets all equipment items for a player (armor + held items)
     */
    private List<ItemStack> getPlayerEquipment(PlayerEntity player) {
        List<ItemStack> items = new ArrayList<>();

        // Add offhand item
        if (!player.getHeldItemOffhand().isEmpty()) {
            items.add(player.getHeldItemOffhand());
        }

        // Add armor items (in order)
        for (ItemStack armorItem : player.getArmorInventoryList()) {
            if (!armorItem.isEmpty()) {
                items.add(armorItem.copy());
            }
        }

        // Add mainhand item
        if (!player.getHeldItemMainhand().isEmpty()) {
            items.add(player.getHeldItemMainhand());
        }

        return items;
    }

    /**
     * Draws the background rectangle for a nametag
     */
    private void drawNameTagBackground(MatrixStack matrix, float centerX, float y, float width, float height) {
        RectUtil.drawRect(matrix,
                centerX - (width / 2F) - NAME_TAG_PADDING,
                y - 0.5F,
                width + 2 * NAME_TAG_PADDING,
                height + 1,
                backgroundColorWithAlpha);
    }

    /**
     * Draws an ItemStack with background and count
     */
    public void drawItemStack(MatrixStack matrix, ItemStack stack, double x, double y, boolean drawRect) {
        matrix.push();
        RenderSystem.translated(x, y, 0);

        if (drawRect) {
            RectUtil.drawRect(matrix, 0, 0, 16, 16, backgroundColorWithAlpha);
        }

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);

        // Draw item count if more than 1
        if (stack.getCount() > 1) {
            RenderSystem.translated(0, 0, 200);
            mc.fontRenderer.drawString(
                    matrix,
                    String.valueOf(stack.getCount()),
                    (float) (16 - mc.fontRenderer.getStringWidth(String.valueOf(stack.getCount()))),
                    8,
                    0xFFFFFF
            );
        }

        RenderSystem.translated(-x, -y, 0);
        matrix.pop();
    }

    /**
     * Calculates the entity's bounding box for rendering
     */
    private AxisAlignedBB calculateEntityBoundingBox(Entity entity, Vector3d position) {
        Vector3d size = new Vector3d(
                entity.getBoundingBox().maxX - entity.getBoundingBox().minX,
                entity.getBoundingBox().maxY - entity.getBoundingBox().minY,
                entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ
        );

        float heightOffset = ENTITY_HEIGHT_MODIFIER - (entity.isSneaking() ? SNEAK_OFFSET : STANDING_OFFSET);

        return new AxisAlignedBB(
                position.x - size.x / 2f,
                position.y,
                position.z - size.z / 2f,
                position.x + size.x / 2f,
                position.y + size.y + heightOffset,
                position.z + size.z / 2f
        );
    }

    /**
     * Projects entity bounds to screen coordinates
     */
    private Vector4f calculateScreenBounds(AxisAlignedBB entityBox) {
        Vector2f min = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
        Vector2f max = new Vector2f(Float.MIN_VALUE, Float.MIN_VALUE);
        boolean valid = false;

        for (Vector3d corner : entityBox.getCorners()) {
            Vector2f screenPos = Project.project2D(corner);

            if (screenPos.x != Float.MAX_VALUE && screenPos.y != Float.MAX_VALUE) {
                min.x = Math.min(min.x, screenPos.x);
                min.y = Math.min(min.y, screenPos.y);
                max.x = Math.max(max.x, screenPos.x);
                max.y = Math.max(max.y, screenPos.y);
                valid = true;
            }
        }

        if (!valid) return null;

        return new Vector4f(min.x, min.y, max.x, max.y);
    }

    /**
     * Gets a cached color string for the specified formatting
     */
    private String getColorString(TextFormatting formatting) {
        return COLOR_CACHE.computeIfAbsent(formatting,
                format -> ColorFormatting.getColor(format.getColor()));
    }

    /**
     * Gets the width of text with the current font size
     */
    private float getTextWidth(ITextComponent text) {
        return font.getWidth(text, fontSize.getValue());
    }

    /**
     * Checks if an entity should have a nametag rendered
     */
    private boolean isValidEntity(final Entity entity) {
        if (!entity.isAlive()) return false;

        // Skip if it's the player in first person view
        if (mc.renderViewEntity != null &&
                entity == mc.renderViewEntity &&
                mc.gameSettings.getPointOfView().firstPerson()) {
            return false;
        }

        // Check if entity is in view and is a player or item
        return isEntityInView(entity) && (entity instanceof PlayerEntity || entity instanceof ItemEntity);
    }

    /**
     * Checks if an entity is within the player's view frustum
     */
    private boolean isEntityInView(Entity entity) {
        if (mc.getRenderViewEntity() == null || mc.worldRenderer.getClippinghelper() == null) {
            return false;
        }

        return mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(entity.getBoundingBox()) ||
                entity.ignoreFrustumCheck;
    }
}