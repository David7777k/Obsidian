package org.obsidian.client.managers.other.macros;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Macro {
    private String name;
    private int key;
    private String message;
}