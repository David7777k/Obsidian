package net.minecraft.client.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatLine<T> {
    private final int updateCounterCreated;
    private T lineString;
    private final int chatLineID;
    private final boolean client;
}
