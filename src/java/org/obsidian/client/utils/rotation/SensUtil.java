package org.obsidian.client.utils.rotation;

import lombok.experimental.UtilityClass;
import org.obsidian.client.api.interfaces.IMinecraft;

@UtilityClass
public class SensUtil implements IMinecraft {

    public float getSens(float rotation) {
        return getDeltaMouse(rotation) * getGCDValue();
    }

    public float getGCDValue() {
        return (float) (getGCD() * 0.15D);
    }

    public float getGCD() {
        double mouseSensitivity = mc.gameSettings.mouseSensitivity;
        return (float) (Math.pow(mouseSensitivity * 0.6F + 0.2F, 3.0D) * 8F);
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

}