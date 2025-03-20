package org.obsidian.client.managers.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    COMBAT("Combat", "a"),
    MOVEMENT("Movement", "b"),
    PLAYER("Player", "c"),
    RENDER("Render", "d"),
    MISC("Misc", "e"),
    CLIENT("Client", "f");
    private final String name;
    private final String icon;
}