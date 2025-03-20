package org.obsidian.client.utils.render.text;

import lombok.Data;
import net.minecraft.client.util.InputMappings;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IMouse;
import org.obsidian.client.utils.math.Interpolator;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.font.Font;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;


@Data
public class TextBox implements IMinecraft, IMouse {
    public String text = "";
    public Vector2f position;
    public boolean selected;
    public int cursor;
    public double animatedCursorPosition;
    public Font font;
    public float fontSize;
    public int color;
    private double lastBackSpace;
    private TextAlign textAlign;
    private float posX;
    private String emptyText;
    private float width;
    private boolean hideCharacters;
    private boolean onlyNumbers;

    public TextBox(final Vector2f position, final Font font, final float fontSize, final int color, final TextAlign textAlign, final String emptyText, final float width, final boolean hideCharacters, final boolean onlyNumbers) {
        this.position = position;
        this.font = font;
        this.fontSize = fontSize;
        this.color = color;
        this.textAlign = textAlign;
        this.emptyText = emptyText;
        this.width = width;
        this.hideCharacters = hideCharacters;
        this.onlyNumbers = onlyNumbers;
    }


    public void draw(MatrixStack matrix) {
        cursor = Math.min(Math.max(cursor, 0), this.text.length());

        mc.keyboardListener.enableRepeatEvents(true);
        StringBuilder drawnString = new StringBuilder(this.text);

        if (this.hideCharacters && !this.isEmpty()) {
            StringBuilder string = new StringBuilder();
            string.append("*".repeat(Math.max(0, drawnString.length())));
            drawnString = new StringBuilder(string);
        }

        switch (this.textAlign) {
            case CENTER -> {
                float fontwidth = (this.font.getWidth(this.text.isEmpty() ? this.emptyText : drawnString.toString(), fontSize) / 2F);
                posX = Interpolator.lerp(posX, position.x - fontwidth, 0.5F);
            }
            case LEFT -> posX = position.x;
        }

        if (this.isEmpty()) {
            this.font.draw(matrix, this.emptyText, posX, position.y, ColorUtil.replAlpha(color, (int) (ColorUtil.alpha(color) * (this.selected ? 0.6F : 0.4F))), fontSize);
        } else {
            this.font.draw(matrix, drawnString.toString(), posX, position.y, this.color, fontSize);
        }

        cursor = Math.min(Math.max(cursor, 0), drawnString.length());

        final StringBuilder textBeforeCursor = new StringBuilder();
        for (int i = 0; i < this.cursor; ++i) {
            textBeforeCursor.append(drawnString.charAt(i));
        }

        float cursorOffset = this.font.getWidth(textBeforeCursor.toString(), fontSize);

        animatedCursorPosition = Interpolator.lerp(animatedCursorPosition, cursorOffset, 0.1D);

        if (this.selected) {
            RectUtil.drawRect(matrix, (float) (posX + animatedCursorPosition), position.y, 0.5F, fontSize, ColorUtil.multAlpha(ColorUtil.replAlpha(color, (int) Mathf.clamp(0, 255, ((Math.sin(System.currentTimeMillis() / 200D) + 1F) / 2F) * 255)), ColorUtil.alphaf(color)));
        }
    }

    public void mouse(double mouseX, double mouseY, int button) {
        final Vector2f position = getPosition();

        this.selected = isLClick(button) && isHover(mouseX, mouseY, position.x + (textAlign == TextAlign.CENTER ? -width / 2f : 0),
                position.y, width, fontSize);
    }

    public void keyPressed(int keyCode) {
        if (!this.selected) {
            return;
        }

        cursor = Math.min(Math.max(cursor, 0), this.text.length());

        if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) && keyCode == GLFW.GLFW_KEY_V) {

            String clipboard = mc.keyboardListener.getClipboardString();
            if (onlyNumbers) {
                clipboard = clipboard.replaceAll("\\D", "");
            }
            this.addText(clipboard, cursor);
            cursor += clipboard.length();

        } else if (keyCode == GLFW.GLFW_KEY_DELETE && !this.text.isEmpty()) {
            this.removeText(cursor + 1);
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !this.text.isEmpty()) {

            this.removeText(cursor);
            cursor--;

            if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                while (!this.text.isEmpty() && 0 < cursor) {
                    this.removeText(cursor);
                    cursor--;
                }
            }

        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            cursor++;

            if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                while (this.text.length() > cursor) {
                    cursor++;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            cursor--;

            if (InputMappings.isKeyDown(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                while (0 < cursor) {
                    cursor--;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            while (this.text.length() > cursor) {
                cursor++;
            }
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            while (0 < cursor) {
                cursor--;
            }
        }

        cursor = Math.min(Math.max(cursor, 0), this.text.length());
    }

    public void charTyped(char codePoint) {
        if (!this.selected) {
            return;
        }
        cursor = Math.min(Math.max(cursor, 0), this.text.length());

        if (onlyNumbers ? TextUtils.NUMBERS.contains(Character.toString(codePoint)) : TextUtils.CHARS.contains(Character.toString(codePoint))) {
            this.addText(Character.toString(codePoint), cursor);
            cursor++;
        }

        cursor = Math.min(Math.max(cursor, 0), this.text.length());
    }

    private void addText(final String text, final int position) {
        float fontwidth = font.getWidth(this.text + text, fontSize);

        if (fontwidth <= width) {
            final StringBuilder newText = new StringBuilder();

            boolean append = false;
            for (int i = 0; i < this.text.length(); i++) {
                final String character = String.valueOf(this.text.charAt(i));

                if (i == position) {
                    append = true;
                    newText.append(text);
                }

                newText.append(character);
            }

            if (!append) {
                newText.append(text);
            }

            this.text = newText.toString();
        }
    }

    private void removeText(final int position) {
        final StringBuilder newText = new StringBuilder();
        for (int i = 0; i < this.text.length(); ++i) {
            final String character = String.valueOf(this.text.charAt(i));

            if (i != position - 1) {
                newText.append(character);
            }
        }

        this.text = newText.toString();
    }

    public boolean isEmpty() {
        return this.text.isEmpty();
    }
}