package org.obsidian.client.managers.events.player;

/**
 * Событие, возникающее при нажатии клавиши.
 * key хранит код клавиши, которую нажали.
 */
public class KeyEvent {
    private final int key;

    public KeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
