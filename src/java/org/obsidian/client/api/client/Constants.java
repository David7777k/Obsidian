package org.obsidian.client.api.client;

import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class Constants {
    public final String NAME = "Obsidian";
    public final String NAMESPACE = NAME.toLowerCase();
    public final String RELEASE = "Alpha";
    public final String VERSION = "1337";
    public final String WEBSITE_SHORT = "Obsidian";
    public final String WEBSITE = "Ты лучший! By:" + WEBSITE_SHORT;
    public final String TITLE = String.format("%s %s (%s) -> %s", NAME, VERSION, RELEASE, WEBSITE);
    public final Path MAIN_DIR = Paths.get("C:/ObsidianClient/game");
    public final String FILE_FORMAT = ".exc";
    public final String DEVELOPER = "sheluvparis";
}
