package net.minecraft.client;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.util.math.MathHelper;
import net.optifine.Config;
import org.lwjgl.glfw.*;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.events.input.MouseReleaseEvent;
import org.obsidian.client.managers.events.input.ScrollEvent;
import org.obsidian.client.managers.events.player.LookEvent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Getter
public class MouseHelper implements IMinecraft {
    private final Minecraft minecraft;

    private boolean leftDown;
    private boolean middleDown;

    private boolean rightDown;

    private double mouseX;

    private double mouseY;
    private int simulatedRightClicks;
    private int activeButton = -1;
    private boolean ignoreFirstMove = true;
    private int touchScreenCounter;
    private double eventTime;
    private final MouseSmoother xSmoother = new MouseSmoother();
    private final MouseSmoother ySmoother = new MouseSmoother();
    private double xVelocity;
    private double yVelocity;
    private double accumulatedScrollDelta;
    private double lastLookTime = Double.MIN_VALUE;


    @Setter
    private boolean mouseGrabbed;

    @Setter
    private double wheel = 0;

    public MouseHelper(Minecraft minecraftIn) {
        this.minecraft = minecraftIn;
    }

    /**
     * Will be called when a mouse button is pressed or released.
     *
     * @see GLFWMouseButtonCallbackI
     */
    private void mouseButtonCallback(long handle, int button, int action, int mods) {
        if (handle == this.minecraft.getMainWindow().getHandle()) {
            boolean flag = action == 1;

            if (Minecraft.IS_RUNNING_ON_MAC && button == 0) {
                if (flag) {
                    if ((mods & 2) == 2) {
                        button = 1;
                        ++this.simulatedRightClicks;
                    }
                } else if (this.simulatedRightClicks > 0) {
                    button = 1;
                    --this.simulatedRightClicks;
                }
            }

            int i = button;

            double d0 = this.mouseX * (double) this.minecraft.getMainWindow().getScaledWidth() / (double) this.minecraft.getMainWindow().getWidth();
            double d1 = this.mouseY * (double) this.minecraft.getMainWindow().getScaledHeight() / (double) this.minecraft.getMainWindow().getHeight();

            if (minecraft.player != null && minecraft.world != null) {
                if (GLFW.GLFW_PRESS == action) {
                    MousePressEvent event = MousePressEvent.getInstance();
                    event.set(button, minecraft.currentScreen, d0, d1);
                    event.hook();
                }
                if (GLFW.GLFW_RELEASE == action) {

                    MouseReleaseEvent event = MouseReleaseEvent.getInstance();
                    event.set(button, minecraft.currentScreen, d0, d1);
                    event.hook();
                }
            }

            if (flag) {
                if (this.minecraft.gameSettings.touchscreen && this.touchScreenCounter++ > 0) {
                    return;
                }

                this.activeButton = i;
                this.eventTime = NativeUtil.getTime();
            } else if (this.activeButton != -1) {
                if (this.minecraft.gameSettings.touchscreen && --this.touchScreenCounter > 0) {
                    return;
                }

                this.activeButton = -1;
            }

            boolean[] aboolean = new boolean[]{false};

            if (this.minecraft.loadingGui == null) {
                if (this.minecraft.currentScreen == null) {
                    if (!this.mouseGrabbed && flag) {
                        this.grabMouse();
                    }
                } else {
                    if (flag) {
                        Screen.wrapScreenError(() ->
                        {
                            aboolean[0] = this.minecraft.currentScreen.mouseClicked(d0, d1, i);
                        }, "mouseClicked event handler", this.minecraft.currentScreen.getClass().getCanonicalName());
                    } else {
                        Screen.wrapScreenError(() ->
                        {
                            aboolean[0] = this.minecraft.currentScreen.mouseReleased(d0, d1, i);
                        }, "mouseReleased event handler", this.minecraft.currentScreen.getClass().getCanonicalName());
                    }
                }
            }

            if (!aboolean[0] && (this.minecraft.currentScreen == null || this.minecraft.currentScreen.passEvents) && this.minecraft.loadingGui == null) {
                if (i == 0) {
                    this.leftDown = flag;
                } else if (i == 2) {
                    this.middleDown = flag;
                } else if (i == 1) {
                    this.rightDown = flag;
                }

                KeyBinding.setKeyBindState(InputMappings.Type.MOUSE.getOrMakeInput(i), flag);

                if (flag) {
                    if (this.minecraft.player.isSpectator() && i == 2) {
                        this.minecraft.ingameGUI.getSpectatorGui().onMiddleClick();
                    } else {
                        KeyBinding.onTick(InputMappings.Type.MOUSE.getOrMakeInput(i));
                    }
                }
            }
        }
    }

    /**
     * Will be called when a scrolling device is used, such as a mouse wheel or scrolling area of a touchpad.
     *
     * @see GLFWScrollCallbackI
     */
    private void scrollCallback(long handle, double xoffset, double yoffset) {
        if (handle == Minecraft.getInstance().getMainWindow().getHandle()) {
            double d0 = (this.minecraft.gameSettings.discreteMouseScroll ? Math.signum(yoffset) : yoffset) * this.minecraft.gameSettings.mouseWheelSensitivity;
            double d1 = this.mouseX * (double) this.minecraft.getMainWindow().getScaledWidth() / (double) this.minecraft.getMainWindow().getWidth();
            double d2 = this.mouseY * (double) this.minecraft.getMainWindow().getScaledHeight() / (double) this.minecraft.getMainWindow().getHeight();

            ScrollEvent scrollEvent = ScrollEvent.getInstance();
            scrollEvent.set(d0, d1, d2);
            scrollEvent.hook();
            if (scrollEvent.isCancelled()) return;
            if (this.minecraft.loadingGui == null) {
                if (this.minecraft.currentScreen != null) {
                    this.minecraft.currentScreen.mouseScrolled(d1, d2, d0);
                    wheel = d0;
                } else if (this.minecraft.player != null) {
                    if (this.accumulatedScrollDelta != 0.0D && Math.signum(d0) != Math.signum(this.accumulatedScrollDelta)) {
                        this.accumulatedScrollDelta = 0.0D;
                    }

                    this.accumulatedScrollDelta += d0;
                    float direction = (float) ((int) this.accumulatedScrollDelta);

                    if (direction == 0.0F) {
                        return;
                    }

                    this.accumulatedScrollDelta -= direction;

                    if (!minecraft.gameSettings.getPointOfView().equals(PointOfView.FIRST_PERSON) && Screen.hasShiftDown()) {
                        minecraft.gameRenderer.getActiveRenderInfo().zoomWheel -= direction;
                        minecraft.gameRenderer.getActiveRenderInfo().zoomWheel /= Math.pow(1.1D, Math.signum(direction));
                    } else if (Config.zoomMode) {
                        minecraft.gameRenderer.zoomWheel += direction;
                        minecraft.gameRenderer.zoomWheel *= Math.pow(1.1D, Math.signum(direction));
                    } else if (this.minecraft.player.isSpectator()) {
                        if (this.minecraft.ingameGUI.getSpectatorGui().isMenuActive()) {
                            this.minecraft.ingameGUI.getSpectatorGui().onMouseScroll(-direction);
                        } else {
                            float f = MathHelper.clamp(this.minecraft.player.abilities.getFlySpeed() + direction * 0.005F, 0.0F, 0.2F);
                            this.minecraft.player.abilities.setFlySpeed(f);
                        }
                    } else {
                        this.minecraft.player.inventory.changeCurrentItem(direction);
                    }
                }
            }
        }
    }

    private void addPacksToScreen(long window, List<Path> paths) {
        if (this.minecraft.currentScreen != null) {
            this.minecraft.currentScreen.addPacks(paths);
        }
    }

    public void registerCallbacks(long handle) {
        InputMappings.setMouseCallbacks(handle, (handle1, xPos, yPos) ->
        {
            this.minecraft.execute(() -> {
                this.cursorPosCallback(handle1, xPos, yPos);
            });
        }, (handle1, button, action, modifiers) ->
        {
            this.minecraft.execute(() -> {
                this.mouseButtonCallback(handle1, button, action, modifiers);
            });
        }, (handle1, xOffset, yOffset) ->
        {
            this.minecraft.execute(() -> {
                this.scrollCallback(handle1, xOffset, yOffset);
            });
        }, (window, callbackCount, names) ->
        {
            Path[] apath = new Path[callbackCount];

            for (int i = 0; i < callbackCount; ++i) {
                apath[i] = Paths.get(GLFWDropCallback.getName(names, i));
            }

            this.minecraft.execute(() -> {
                this.addPacksToScreen(window, Arrays.asList(apath));
            });
        });
    }

    /**
     * Will be called when the cursor is moved.
     *
     * <p>The callback function receives the cursor position, measured in screen coordinates but relative to the top-
     * left corner of the window client area. On platforms that provide it, the full sub-pixel cursor position is passed
     * on.</p>
     *
     * @see GLFWCursorPosCallbackI
     */
    private void cursorPosCallback(long handle, double xpos, double ypos) {
        if (handle == Minecraft.getInstance().getMainWindow().getHandle()) {
            if (this.ignoreFirstMove) {
                this.mouseX = xpos;
                this.mouseY = ypos;
                this.ignoreFirstMove = false;
            }

            IGuiEventListener iguieventlistener = this.minecraft.currentScreen;

            if (iguieventlistener != null && this.minecraft.loadingGui == null) {
                double d0 = xpos * (double) this.minecraft.getMainWindow().getScaledWidth() / (double) this.minecraft.getMainWindow().getWidth();
                double d1 = ypos * (double) this.minecraft.getMainWindow().getScaledHeight() / (double) this.minecraft.getMainWindow().getHeight();
                Screen.wrapScreenError(() ->
                {
                    iguieventlistener.mouseMoved(d0, d1);
                }, "mouseMoved event handler", iguieventlistener.getClass().getCanonicalName());

                if (this.activeButton != -1 && this.eventTime > 0.0D) {
                    double d2 = (xpos - this.mouseX) * (double) this.minecraft.getMainWindow().getScaledWidth() / (double) this.minecraft.getMainWindow().getWidth();
                    double d3 = (ypos - this.mouseY) * (double) this.minecraft.getMainWindow().getScaledHeight() / (double) this.minecraft.getMainWindow().getHeight();
                    Screen.wrapScreenError(() ->
                    {
                        iguieventlistener.mouseDragged(d0, d1, this.activeButton, d2, d3);
                    }, "mouseDragged event handler", iguieventlistener.getClass().getCanonicalName());
                }
            }

            if (this.isMouseGrabbed() && this.minecraft.isGameFocused()) {
                this.xVelocity += xpos - this.mouseX;
                this.yVelocity += ypos - this.mouseY;
            }

            this.updatePlayerLook();
            this.mouseX = xpos;
            this.mouseY = ypos;
        }
    }

    public void updatePlayerLook() {
        double d0 = NativeUtil.getTime();
        double d1 = d0 - this.lastLookTime;
        this.lastLookTime = d0;

        if (this.isMouseGrabbed() && this.minecraft.isGameFocused()) {
            double d4 = this.minecraft.gameSettings.mouseSensitivity * (double) 0.6F + (double) 0.2F;
            double d5 = d4 * d4 * d4 * 8.0D;
            double d2;
            double d3;

            if (this.minecraft.gameSettings.smoothCamera) {
                double d6 = this.xSmoother.smooth(this.xVelocity * d5, d1 * d5);
                double d7 = this.ySmoother.smooth(this.yVelocity * d5, d1 * d5);
                d2 = d6;
                d3 = d7;
            } else {
                this.xSmoother.reset();
                this.ySmoother.reset();
                d2 = this.xVelocity * d5;
                d3 = this.yVelocity * d5;
            }

            this.xVelocity = 0.0D;
            this.yVelocity = 0.0D;
            int i = 1;

            if (this.minecraft.gameSettings.invertMouse) {
                i = -1;
            }

            if (this.minecraft.player != null) {
                LookEvent event = new LookEvent(d2, d3 * (double) i);
                event.hook();
                if (!event.isCancelled()) this.minecraft.player.rotateTowards(event.getYaw(), event.getPitch());
            }
        } else {
            this.xVelocity = 0.0D;
            this.yVelocity = 0.0D;
        }
    }

    public void setIgnoreFirstMove() {
        this.ignoreFirstMove = true;
    }

    /**
     * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen
     * currently displayed
     */
    public void grabMouse() {
        forceGrabMouse(true);
    }

    public void forceGrabMouse(boolean screen) {
        if (this.minecraft.isGameFocused()) {
            if (!this.mouseGrabbed) {
                if (!Minecraft.IS_RUNNING_ON_MAC && screen) {
                    KeyBinding.updateKeyBindState();
                }
                this.mouseGrabbed = true;
                this.mouseX = this.minecraft.getMainWindow().getWidth() / 2D;
                this.mouseY = this.minecraft.getMainWindow().getHeight() / 2D;
                InputMappings.setCursorPosAndMode(this.minecraft.getMainWindow().getHandle(), 212995, this.mouseX, this.mouseY);
                if (screen) {
                    this.minecraft.displayScreen(null);
                }
                this.minecraft.leftClickCounter = 10000;
                this.ignoreFirstMove = true;
            }
        }
    }

    /**
     * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
     */
    public void ungrabMouse() {
        if (this.mouseGrabbed) {
            this.mouseGrabbed = false;
            this.mouseX = this.minecraft.getMainWindow().getWidth() / 2D;
            this.mouseY = this.minecraft.getMainWindow().getHeight() / 2D;
            InputMappings.setCursorPosAndMode(this.minecraft.getMainWindow().getHandle(), 212993, this.mouseX, this.mouseY);
        }
    }

    public void ignoreFirstMove() {
        this.ignoreFirstMove = true;
    }
}
