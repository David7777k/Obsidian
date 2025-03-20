package org.obsidian.client.utils.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
    ACCOUNT("account"),
    AUTOBUY("autobuy"),
    CONFIG("config"),
    MACROS("macros"),
    FRIEND("friend"),
    STAFF("staff"),
    MUSIC("music");
    private final String name;
}
