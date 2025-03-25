package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.EntityRenderMatrixEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.other.SoundUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "EggMan", category = Category.MISC)
public class EggMan extends Module {
    public static EggMan getInstance() {
        return Instance.get(EggMan.class);
    }

    // Animation mode settings
    public final ModeSetting animationMode = new ModeSetting(this, "Режим анимации", "Яйцо", "Пружина", "Размер");

    // Wobble mode settings (Egg mode)
    public final SliderSetting wobbleIntensity = new SliderSetting(this, "Интенсивность эффекта", 1.0f, 0.5f, 2.0f, 0.1f)
            .setVisible(() -> animationMode.getValue().equals("Яйцо"));

    // Bounce mode settings
    public final SliderSetting bounceIntensity = new SliderSetting(this, "Интенсивность отскока", 1.5f, 0.5f, 3.0f, 0.1f)
            .setVisible(() -> animationMode.getValue().equals("Пружина"));
    public final SliderSetting bounceSpeed = new SliderSetting(this, "Скорость отскока", 2.0f, 0.5f, 5.0f, 0.1f)
            .setVisible(() -> animationMode.getValue().equals("Пружина"));

    // Size mode settings - now with a wider range for both small and large players
    public final SliderSetting playerScale = new SliderSetting(this, "Размер игрока", 1.0f, 0.2f, 3.0f, 0.1f)
            .setVisible(() -> animationMode.getValue().equals("Размер"));

    // Sound settings
    public final BooleanSetting soundEffect = new BooleanSetting(this, "Звуковой эффект", true);
    private final SliderSetting soundVolume = new SliderSetting(this, "Громкость звука", 25, 0, 100, 1)
            .setVisible(soundEffect::getValue);
    private final ModeSetting soundType = new ModeSetting(this, "Тип звука", "Egg", "Boing")
            .setVisible(soundEffect::getValue);

    // Sound controllers
    private final SoundUtil.AudioClipPlayController eggSoundTuner = SoundUtil.AudioClipPlayController.build(
            SoundUtil.AudioClip.build("eggman.wav", true),
            () -> soundEffect.getValue() && soundType.getValue().equals("Egg") && isEnabled(),
            true);

    private final SoundUtil.AudioClipPlayController boingSoundTuner = SoundUtil.AudioClipPlayController.build(
            SoundUtil.AudioClip.build("boing.wav", true),
            () -> soundEffect.getValue() && soundType.getValue().equals("Boing") && isEnabled(),
            true);

    private float prevVolume;

    @Override
    public void toggle() {
        super.toggle();
        prevVolume = soundVolume.getValue();

        // Safely set volume for both sound controllers
        try {
            if (eggSoundTuner != null && eggSoundTuner.getAudioClip() != null) {
                eggSoundTuner.getAudioClip().setVolume(soundVolume.getValue() / 100F);
            }

            if (boingSoundTuner != null && boingSoundTuner.getAudioClip() != null) {
                boingSoundTuner.getAudioClip().setVolume(soundVolume.getValue() / 100F);
            }
        } catch (Exception e) {
            System.out.println("Failed to set audio volume: " + e.getMessage());
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        updateSounds();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        try {
            eggSoundTuner.updatePlayingStatus();
            boingSoundTuner.updatePlayingStatus();
        } catch (Exception e) {
            System.out.println("Failed to update sound status: " + e.getMessage());
        }
    }

    @EventHandler
    public void onEvent(EntityRenderMatrixEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            String currentMode = animationMode.getValue();

            if (currentMode.equals("Яйцо")) {
                applyEggEffect(event);
            } else if (currentMode.equals("Пружина")) {
                applyBounceEffect(event);
            } else if (currentMode.equals("Размер")) {
                applySizeEffect(event);
            }
        }
    }

    private void applyEggEffect(EntityRenderMatrixEvent event) {
        float wobble = ((System.currentTimeMillis() + event.getEntity().getEntityId() * 100) % 400) / 400F;
        wobble = (wobble > 0.5F ? 1F - wobble : wobble) * 2F;
        wobble = Mathf.clamp01(wobble) * wobbleIntensity.getValue();
        event.getMatrix().scale(wobble * 2F + 1F, 1F - 0.5F * wobble, wobble * 2F + 1F);
    }

    private void applyBounceEffect(EntityRenderMatrixEvent event) {
        float bounce = (float) Math.sin((System.currentTimeMillis() + event.getEntity().getEntityId() * 120)
                % 1000 / (1000f / (bounceSpeed.getValue() * Math.PI * 2)));
        bounce = Mathf.clamp01(Math.abs(bounce)) * bounceIntensity.getValue();

        // Squash and stretch effect
        event.getMatrix().scale(1F + (0.3F * bounce), 1F + (0.8F * bounce), 1F + (0.3F * bounce));

        // Apply slight vertical offset based on bounce
        event.getMatrix().translate(0, bounce * 0.1F, 0);
    }

    private void applySizeEffect(EntityRenderMatrixEvent event) {
        // Scale player based on slider value
        float scale = playerScale.getValue();
        event.getMatrix().scale(scale, scale, scale);

        // Adjust vertical position to keep players' feet on the ground
        if (scale != 1.0f) {
            // This adjustment prevents players from sinking into ground or floating
            // The formula adjusts Y position based on how much the scale differs from normal (1.0)
            event.getMatrix().translate(0, (1.0f - scale) * 0.5f, 0);
        }
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        updateSounds();
    }

    private void updateSounds() {
        try {
            // Update both sound controllers
            eggSoundTuner.updatePlayingStatus();
            boingSoundTuner.updatePlayingStatus();

            // Update volume if changed
            if (prevVolume != soundVolume.getValue()) {
                if (eggSoundTuner.isSucessPlaying()) {
                    eggSoundTuner.getAudioClip().setVolume(soundVolume.getValue() / 100F);
                }

                if (boingSoundTuner.isSucessPlaying()) {
                    boingSoundTuner.getAudioClip().setVolume(soundVolume.getValue() / 100F);
                }

                prevVolume = soundVolume.getValue();
            }
        } catch (Exception e) {
            System.out.println("Failed to update sounds: " + e.getMessage());
        }
    }

    @Override
    public boolean isStarred() {
        return true; // Звёздочка всегда отображается
    }
}
