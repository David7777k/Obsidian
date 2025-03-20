package org.obsidian.client.utils.other;

import lombok.experimental.UtilityClass;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

@UtilityClass
public class Console {
    static {
        AnsiConsole.systemInstall();
    }

    public Ansi getConsoleReset() {
        return Ansi.ansi().reset();
    }

    public Ansi getConsoleBackground() {
        return Ansi.ansi().bgRgb(0, 25, 20);
    }

    public Ansi getConsoleText() {
        return Ansi.ansi().fgRgb(0, 255, 200);
    }

    public Ansi getConsoleTextValue() {
        return Ansi.ansi().fgBrightGreen();
    }
}
