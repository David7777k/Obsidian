package org.obsidian.client.utils.other;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.api.interfaces.IExecutor;
import org.obsidian.client.api.interfaces.IMinecraft;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@UtilityClass
public class SoundUtil implements IMinecraft, IExecutor {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void playSound(final String location, double volume) {
        executorService.execute(() -> {
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(SoundUtil.class.getResourceAsStream("/assets/minecraft/" + Constants.NAMESPACE + "/sound/" + location)))) {
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                setVolume(clip, volume);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.fillInStackTrace();
            }
        });
    }

    public void playSound(final String location) {
        playSound(location, 0.5);
    }

    private void setVolume(Clip clip, double volume) {
        if (volume < 0) volume = 0;
        if (volume > 1) volume = 1;

        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(volume == 0 ? 0.0001D : volume) / Math.log(10.0) * 20.0);
        volumeControl.setValue(dB);
    }

    @Getter
    @Setter
    public class AudioClipPlayController {
        private final AudioClip audioClip;
        private Supplier<Boolean> playIf;
        private boolean stopIsAPause;
        private boolean started;

        private AudioClipPlayController(AudioClip audioClip, Supplier<Boolean> playIf, boolean stopIsAPause) {
            this.audioClip = audioClip;
            this.playIf = playIf;
            this.stopIsAPause = stopIsAPause;
        }

        public static AudioClipPlayController build(AudioClip audioClip, Supplier<Boolean> playIf, boolean stopIsAPause) {
            return new AudioClipPlayController(audioClip, playIf, stopIsAPause);
        }

        public void setStopIsAPauseMode(boolean stopIsAPause) {
            this.stopIsAPause = stopIsAPause;
        }

        public void updatePlayingStatus() {
            if (started && audioClip.clip == null && playIf.get()) {
                started = false;
            }
            if (!started && playIf.get()) {
                audioClip.startPlayingAudio();
                started = true;
            }
            if (stopIsAPause) {
                audioClip.setPause(!playIf.get());
                return;
            }
            if (audioClip.isPlaying() != playIf.get()) {
                if (playIf.get()) audioClip.startPlayingAudio();
                else audioClip.stopPlayingAudio();
            }
        }

        public boolean isSucessPlaying() {
            return this.audioClip.isPlaying();
        }
    }

    public class AudioClip {
        private final boolean loop;
        private boolean pause;
        private long currentPlayTime;
        @Getter
        private String soundName;
        private Clip clip;

        private AudioClip(String soundName, boolean loop) {
            this.soundName = soundName;
            this.loop = loop;
        }

        public static AudioClip build(String soundName, boolean loop) {
            return new AudioClip(soundName, loop);
        }

        public boolean isPlaying() {
            return this.clip != null && this.clip.isOpen() && this.clip.isRunning();
        }

        public void changeAudioTrack(String soundName) {
            this.soundName = soundName;
            stopPlayingAudio();
            startPlayingAudio();
        }

        public void setLoop(boolean loop) {
            if (this.clip == null) return;
            this.clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
        }

        public boolean isLoop() {
            return this.loop && clip != null && clip.isOpen();
        }

        public void setPause(boolean pause) {
            if (this.pause != pause && clip != null && clip.isOpen() && clip.getMicrosecondLength() != 0) {
                if (pause) {
                    currentPlayTime = clip.getMicrosecondPosition();
                    clip.stop();
                } else {
                    clip.setMicrosecondPosition(currentPlayTime);
                    this.setVolume(this.getVolume());
                    this.setLoop(this.isLoop());
                    clip.start();
                }
                this.pause = pause;
            }
        }

        public boolean isPaused() {
            return this.pause && clip != null && !clip.isRunning();
        }

        public void setVolume(float volume) {
            if (this.clip == null) return;
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            double dbValue = 20.0 * Math.log10(volume <= 0.0f ? 0.0001f : volume);
            dbValue = Math.max(control.getMinimum(), Math.min(control.getMaximum(), dbValue));
            if (control.getValue() != (float) dbValue) {
                control.setValue((float) dbValue);
            }
        }

        private float getVolume() {
            FloatControl control = ((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN));
            return control.getValue();
        }

        public void startPlayingAudio() {
            this.stopPlayingAudio();
            try {
                this.clip = AudioSystem.getClip();
                String resourcePath = "/assets/minecraft/" + Constants.NAMESPACE + "/sound/" + this.soundName;
                InputStream audioSrc = SoundUtil.class.getResourceAsStream(resourcePath);
                assert audioSrc != null;
                try {
                    BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
                    clip.open(inputStream);
                    this.setVolume(this.getVolume());
                    this.setLoop(this.isLoop());
                    clip.start();
                } catch (Exception exception) {
                    System.out.println(exception.getLocalizedMessage());
                }
            } catch (Exception exception) {
                System.out.println(exception.getLocalizedMessage());
            }
        }

        public void stopPlayingAudio() {
            if (this.clip == null) return;
            if (this.clip.isRunning()) this.clip.stop();
            if (this.clip.isOpen()) this.clip.close();
            this.clip = null;
        }
    }
}
