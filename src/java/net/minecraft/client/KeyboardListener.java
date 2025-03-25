package net.minecraft.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.optifine.reflect.Reflector;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.KeyboardReleaseEvent;

import javax.annotation.Nullable;
import java.util.Locale;

public class KeyboardListener {
    private final Minecraft mc;
    private boolean repeatEventsEnabled;
    private final ClipboardHelper clipboardHelper = new ClipboardHelper();
    private long debugCrashKeyPressTime = -1L;
    private long lastDebugCrashWarning = -1L;
    private long debugCrashWarningsSent = -1L;
    private boolean actionKeyF3;

    public KeyboardListener(Minecraft mcIn) {
        this.mc = mcIn;
    }

    private void printDebugMessage(String message, Object... args) {
        this.mc.ingameGUI.getChatGUI().printChatMessage(
                new StringTextComponent("")
                        .append(new TranslationTextComponent("debug.prefix").mergeStyle(TextFormatting.YELLOW, TextFormatting.BOLD))
                        .appendString(" ")
                        .append(new TranslationTextComponent(message, args))
        );
    }

    private void printDebugWarning(String message, Object... args) {
        this.mc.ingameGUI.getChatGUI().printChatMessage(
                new StringTextComponent("")
                        .append(new TranslationTextComponent("debug.prefix").mergeStyle(TextFormatting.RED, TextFormatting.BOLD))
                        .appendString(" ")
                        .append(new TranslationTextComponent(message, args))
        );
    }

    private boolean processKeyF3(int key) {
        if (this.debugCrashKeyPressTime > 0L && this.debugCrashKeyPressTime < Util.milliTime() - 100L) {
            return true;
        } else {
            switch (key) {
                // Обработчик F3-клавиш – оставьте как есть
                default:
                    return false;
            }
        }
    }

    private void copyHoveredObject(boolean privileged, boolean askServer) {
        RayTraceResult raytraceresult = this.mc.objectMouseOver;
        if (raytraceresult != null) {
            switch (raytraceresult.getType()) {
                case BLOCK:
                    BlockPos blockpos = ((BlockRayTraceResult) raytraceresult).getPos();
                    BlockState blockstate = this.mc.player.world.getBlockState(blockpos);
                    if (privileged) {
                        if (askServer) {
                            this.mc.player.connection.getNBTQueryManager().queryTileEntity(blockpos, (p_lambda) -> {
                                this.setBlockClipboardString(blockstate, blockpos, p_lambda);
                                this.printDebugMessage("debug.inspect.server.block");
                            });
                        } else {
                            TileEntity tileentity = this.mc.player.world.getTileEntity(blockpos);
                            CompoundNBT compoundnbt1 = tileentity != null ? tileentity.write(new CompoundNBT()) : null;
                            this.setBlockClipboardString(blockstate, blockpos, compoundnbt1);
                            this.printDebugMessage("debug.inspect.client.block");
                        }
                    } else {
                        this.setBlockClipboardString(blockstate, blockpos, null);
                        this.printDebugMessage("debug.inspect.client.block");
                    }
                    break;
                case ENTITY:
                    Entity entity = ((EntityRayTraceResult) raytraceresult).getEntity();
                    ResourceLocation resourcelocation = Registry.ENTITY_TYPE.getKey(entity.getType());
                    if (privileged) {
                        if (askServer) {
                            this.mc.player.connection.getNBTQueryManager().queryEntity(entity.getEntityId(), (p_lambda) -> {
                                this.setEntityClipboardString(resourcelocation, entity.getPositionVec(), p_lambda);
                                this.printDebugMessage("debug.inspect.server.entity");
                            });
                        } else {
                            CompoundNBT compoundnbt = entity.writeWithoutTypeId(new CompoundNBT());
                            this.setEntityClipboardString(resourcelocation, entity.getPositionVec(), compoundnbt);
                            this.printDebugMessage("debug.inspect.client.entity");
                        }
                    } else {
                        this.setEntityClipboardString(resourcelocation, entity.getPositionVec(), null);
                        this.printDebugMessage("debug.inspect.client.entity");
                    }
            }
        }
    }

    private void setBlockClipboardString(BlockState state, BlockPos pos, @Nullable CompoundNBT compound) {
        if (compound != null) {
            compound.remove("x");
            compound.remove("y");
            compound.remove("z");
            compound.remove("id");
        }
        StringBuilder stringbuilder = new StringBuilder(BlockStateParser.toString(state));
        if (compound != null) {
            stringbuilder.append(compound);
        }
        String s = String.format(Locale.ROOT, "/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), stringbuilder);
        this.setClipboardString(s);
    }

    private void setEntityClipboardString(ResourceLocation entityIdIn, Vector3d pos, @Nullable CompoundNBT compound) {
        String s;
        if (compound != null) {
            compound.remove("UUID");
            compound.remove("Pos");
            compound.remove("Dimension");
            String s1 = compound.toFormattedComponent().getString();
            s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", entityIdIn, pos.x, pos.y, pos.z, s1);
        } else {
            s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", entityIdIn, pos.x, pos.y, pos.z);
        }
        this.setClipboardString(s);
    }

    // Обработка клавиатурных событий. Этот метод вызывается из setupCallbacks.
    public void onKeyEvent(long windowPointer, int key, int scanCode, int action, int modifiers) {
        if (windowPointer != this.mc.getMainWindow().getHandle()) {
            return;
        }
        // Публикуем события KeyboardPressEvent / KeyboardReleaseEvent
        if (mc.player != null && mc.world != null) {
            if (GLFW.GLFW_PRESS == action && key != -1) {
                KeyboardPressEvent event = KeyboardPressEvent.getInstance();
                event.set(key, mc.currentScreen);
                event.hook();
            }
            if (GLFW.GLFW_RELEASE == action && key != -1) {
                KeyboardReleaseEvent event = KeyboardReleaseEvent.getInstance();
                event.set(key, mc.currentScreen);
                event.hook();
            }
        }

        // Дополнительная обработка для GUI, полноэкранного режима и т.п.
        if (this.debugCrashKeyPressTime > 0L) {
            if (!InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 67) ||
                    !InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 292)) {
                this.debugCrashKeyPressTime = -1L;
            }
        } else if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 67) &&
                InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 292)) {
            this.actionKeyF3 = true;
            this.debugCrashKeyPressTime = Util.milliTime();
            this.lastDebugCrashWarning = Util.milliTime();
            this.debugCrashWarningsSent = 0L;
        }

        INestedGuiEventHandler inestedguieventhandler = this.mc.currentScreen;
        if (!(this.mc.currentScreen instanceof ControlsScreen) ||
                ((ControlsScreen) inestedguieventhandler).time <= Util.milliTime() - 20L) {
            if (action == 1) {
                if (this.mc.gameSettings.keyBindFullscreen.matchesKey(key, scanCode)) {
                    this.mc.getMainWindow().toggleFullscreen();
                    this.mc.gameSettings.fullscreen = this.mc.getMainWindow().isFullscreen();
                    this.mc.gameSettings.saveOptions();
                    return;
                }
                if (this.mc.gameSettings.keyBindScreenshot.matchesKey(key, scanCode)) {
                    ScreenShotHelper.saveScreenshot(this.mc.gameDir, this.mc.getMainWindow().getFramebufferWidth(), this.mc.getMainWindow().getFramebufferHeight(), this.mc.getFramebuffer(), (msg) -> {
                        this.mc.execute(() -> {
                            this.mc.ingameGUI.getChatGUI().printChatMessage(msg);
                        });
                    });
                    return;
                }
            } else if (action == 0 && this.mc.currentScreen instanceof ControlsScreen) {
                ((ControlsScreen) this.mc.currentScreen).buttonId = null;
            }
        }

        boolean flag = inestedguieventhandler == null ||
                !(inestedguieventhandler.getListener() instanceof TextFieldWidget) ||
                !((TextFieldWidget) inestedguieventhandler.getListener()).canWrite();

        if (inestedguieventhandler != null) {
            boolean[] handled = new boolean[]{false};
            Screen.wrapScreenError(() -> {
                if (action != 1 && (action != 2 || !this.repeatEventsEnabled)) {
                    if (action == 0) {
                        if (Reflector.ForgeHooksClient_onGuiKeyReleasedPre.exists()) {
                            handled[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyReleasedPre, this.mc.currentScreen, key, scanCode, modifiers);
                            if (handled[0]) return;
                        }
                        handled[0] = inestedguieventhandler.keyReleased(key, scanCode, modifiers);
                        if (Reflector.ForgeHooksClient_onGuiKeyReleasedPost.exists() && !handled[0]) {
                            handled[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyReleasedPost, this.mc.currentScreen, key, scanCode, modifiers);
                        }
                    }
                } else {
                    if (Reflector.ForgeHooksClient_onGuiKeyPressedPre.exists()) {
                        handled[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyPressedPre, this.mc.currentScreen, key, scanCode, modifiers);
                        if (handled[0]) return;
                    }
                    handled[0] = inestedguieventhandler.keyPressed(key, scanCode, modifiers);
                    if (Reflector.ForgeHooksClient_onGuiKeyPressedPost.exists() && !handled[0]) {
                        handled[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyPressedPost, this.mc.currentScreen, key, scanCode, modifiers);
                    }
                }
            }, "keyPressed event handler", inestedguieventhandler.getClass().getCanonicalName());
            if (handled[0]) return;
        }

        if (this.mc.currentScreen == null || this.mc.currentScreen.passEvents) {
            InputMappings.Input input = InputMappings.getInputByCode(key, scanCode);
            if (action == 0) {
                KeyBinding.setKeyBindState(input, false);
                if (key == 292) {
                    if (this.actionKeyF3) {
                        this.actionKeyF3 = false;
                    } else {
                        this.mc.gameSettings.showDebugInfo = !this.mc.gameSettings.showDebugInfo;
                        this.mc.gameSettings.showLagometer = this.mc.gameSettings.showDebugInfo && Screen.hasAltDown();
                        if (this.mc.gameSettings.showDebugInfo && this.mc.gameSettings.ofLagometer) {
                            this.mc.gameSettings.showLagometer = true;
                        }
                    }
                }
            } else {
                if (key == 293 && this.mc.gameRenderer != null) {
                    this.mc.gameRenderer.switchUseShader();
                }
                boolean flag1 = false;
                if (this.mc.currentScreen == null) {
                    if (key == 256) {
                        boolean flag2 = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 292);
                        this.mc.displayInGameMenu(flag2);
                    }
                    flag1 = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 292) && this.processKeyF3(key);
                    this.actionKeyF3 |= flag1;
                    if (key == 290) {
                        this.mc.gameSettings.hideGUI = !this.mc.gameSettings.hideGUI;
                    }
                }
                if (flag1) {
                    KeyBinding.setKeyBindState(input, false);
                } else {
                    KeyBinding.setKeyBindState(input, true);
                    KeyBinding.onTick(input);
                }
            }
        }
    }

    private void onCharEvent(long windowPointer, int codePoint, int modifiers) {
        if (windowPointer == this.mc.getMainWindow().getHandle()) {
            IGuiEventListener listener = this.mc.currentScreen;
            if (listener != null && this.mc.getLoadingGui() == null) {
                if (Character.charCount(codePoint) == 1) {
                    Screen.wrapScreenError(() -> {
                        if (!Reflector.ForgeHooksClient_onGuiCharTypedPre.exists() ||
                                !Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPre, this.mc.currentScreen, (char) codePoint, modifiers)) {
                            boolean flag = listener.charTyped((char) codePoint, modifiers);
                            if (Reflector.ForgeHooksClient_onGuiCharTypedPost.exists() && !flag) {
                                Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPost, this.mc.currentScreen, (char) codePoint, modifiers);
                            }
                        }
                    }, "charTyped event handler", listener.getClass().getCanonicalName());
                } else {
                    for (char c : Character.toChars(codePoint)) {
                        Screen.wrapScreenError(() -> {
                            if (!Reflector.ForgeHooksClient_onGuiCharTypedPre.exists() ||
                                    !Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPre, this.mc.currentScreen, c, modifiers)) {
                                boolean flag = listener.charTyped(c, modifiers);
                                if (Reflector.ForgeHooksClient_onGuiCharTypedPost.exists() && !flag) {
                                    Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPost, this.mc.currentScreen, c, modifiers);
                                }
                            }
                        }, "charTyped event handler", listener.getClass().getCanonicalName());
                    }
                }
            }
        }
    }

    public void enableRepeatEvents(boolean repeatEvents) {
        this.repeatEventsEnabled = repeatEvents;
    }

    public void setupCallbacks(long window) {
        InputMappings.setKeyCallbacks(window,
                (winPtr, key, scanCode, action, modifiers) ->
                        this.mc.execute(() -> this.onKeyEvent(winPtr, key, scanCode, action, modifiers)),
                (winPtr, codePoint, modifiers) ->
                        this.mc.execute(() -> this.onCharEvent(winPtr, codePoint, modifiers))
        );
    }

    public String getClipboardString() {
        return this.clipboardHelper.getClipboardString(this.mc.getMainWindow().getHandle(), (errCode, description) -> {
            if (errCode != 65545) {
                this.mc.getMainWindow().logGlError(errCode, description);
            }
        });
    }

    public void setClipboardString(String string) {
        this.clipboardHelper.setClipboardString(this.mc.getMainWindow().getHandle(), string);
    }

    public void tick() {
        if (this.debugCrashKeyPressTime > 0L) {
            long i = Util.milliTime();
            long j = 10000L - (i - this.debugCrashKeyPressTime);
            long k = i - this.lastDebugCrashWarning;
            if (j < 0L) {
                if (Screen.hasControlDown()) {
                    NativeUtil.crash();
                }
                throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
            }
            if (k >= 1000L) {
                if (this.debugCrashWarningsSent == 0L) {
                    this.printDebugMessage("debug.crash.message");
                } else {
                    this.printDebugWarning("debug.crash.warning", MathHelper.ceil((float) j / 1000.0F));
                }
                this.lastDebugCrashWarning = i;
                ++this.debugCrashWarningsSent;
            }
        }
    }
}
