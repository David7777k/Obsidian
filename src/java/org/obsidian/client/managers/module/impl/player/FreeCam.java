package org.obsidian.client.managers.module.impl.player;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.world.GameType;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.managers.events.other.CameraClipEvent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.events.world.WorldLoadEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;

import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FreeCam", category = Category.PLAYER)
public class FreeCam extends Module implements IWindow {
    public static FreeCam getInstance() {
        return Instance.get(FreeCam.class);
    }

    private final SliderSetting speed = new SliderSetting(this, "Скорость", 1.5F, 0.1F, 5.0F, 0.1F);
    private final int TEMP_ENTITY_ID = Integer.MAX_VALUE - 1337;
    private float x, y, z;
    private GameType prev;
    private final Font font = Fonts.SF_SEMIBOLD;

    @Override
    protected void onEnable() {
        if (mc.player.getRidingEntity() != null) toggle();

        super.onEnable();
        prev = mc.playerController.getCurrentGameType();

        mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(GameType.SPECTATOR);

        x = (float) mc.player.getPosX();
        y = (float) mc.player.getPosY();
        z = (float) mc.player.getPosZ();

        final RemoteClientPlayerEntity fakePlayer = new RemoteClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), mc.getSession().getUsername()));

        fakePlayer.inventory = mc.player.inventory;
        fakePlayer.setHealth(mc.player.getHealth());
        fakePlayer.setPositionAndRotation(x, mc.player.getBoundingBox().minY, z, mc.player.rotationYaw, mc.player.rotationPitch);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        mc.world.addEntity(TEMP_ENTITY_ID, fakePlayer);
        mc.player.setGameType(GameType.ADVENTURE);
        mc.player.setSneaking(true);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).setGameType(prev);

        mc.player.setMotion(0, 0, 0);
        mc.player.setVelocity(0, 0, 0);
        mc.player.setPosition(x, y, z);
        mc.player.setSneaking(false);
        mc.world.removeEntityFromWorld(TEMP_ENTITY_ID);
    }

    @EventHandler
    public void onEvent(CameraClipEvent event) {
        event.cancel();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        toggle();
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        toggle();
    }

    @EventHandler
    public void onEvent(Render2DEvent event) {

        float xPosition = (float) (x - mc.player.getPosX());
        float yPosition = (float) (y - mc.player.getPosY());
        float zPosition = (float) (z - mc.player.getPosZ());

        String position = "x:" + Mathf.round(xPosition, 1) + " y:" + Mathf.round(yPosition, 1) + " z:" + Mathf.round(zPosition, 1);
        font.drawCenterOutline(event.getMatrix(), position, scaled().x / 2F, scaled().y / 2F - 30, -1, 6);

    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (mc.player == null) {
            toggle();
            return;
        }

        if (!mc.player.isAlive()) {
            toggle();
        }

        if (mc.world == null) return;
        final IPacket<?> packet = event.getPacket();

        if (event.isSend()) {
            if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) {
                event.cancel();
            }
        }

        if (event.isReceive()) {
            if (packet instanceof SPlayerPositionLookPacket) {
                event.cancel();
            }
        }
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motion.y = -speed.getValue() * 0.75F;
        } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motion.y = speed.getValue() * 0.75F;
        } else {
            mc.player.setMotion(0, 0, 0);
        }

        MoveUtil.setSpeed(speed.getValue());
    }
}