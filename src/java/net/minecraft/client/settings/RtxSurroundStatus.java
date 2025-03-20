package net.minecraft.client.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Comparator;

@Getter
@RequiredArgsConstructor
public enum RtxSurroundStatus {
    OFF(0, "Off"),
    PERFOMANCE(1, "Perfomance"),
    FULL(2, "Full");
    private final int id;
    private final String resourceKey;

    private static final RtxSurroundStatus[] VALUES = Arrays.stream(values()).sorted(Comparator.comparingInt(RtxSurroundStatus::getId)).toArray(RtxSurroundStatus[]::new);

    public static RtxSurroundStatus getValue(int valueIn) {
        return VALUES[MathHelper.normalizeAngle(valueIn, VALUES.length)];
    }
}
