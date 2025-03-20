package net.minecraft.client.gui.screen;

import com.google.common.util.concurrent.Runnables;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.text.TextAlign;
import org.obsidian.client.utils.render.text.TextBox;
import org.obsidian.common.impl.fastrandom.FastRandom;
import org.obsidian.common.user.LoginManager;
import org.joml.Vector2f;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class MainMenuScreen extends Screen {
    private static final Logger field_238656_b_ = LogManager.getLogger();
    public static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY_TEXTURES = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURES = new ResourceLocation("textures/gui/accessibility.png");
    private final boolean showTitleWronglySpelled;
    @Nullable
    private String splashText;
    private Button buttonResetDemo;
    private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");


    private int widthCopyright;
    private int widthCopyrightRest;
    private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);
    private final boolean showFadeInAnimation;
    private long firstRenderTime;
    private TextBox textBox;

    public MainMenuScreen() {
        this(false);
    }

    public MainMenuScreen(boolean fadeIn) {
        super(new TranslationTextComponent("narrator.screen.title"));
        this.showFadeInAnimation = fadeIn;
        this.showTitleWronglySpelled = (double) (new FastRandom()).nextFloat() < 1.0E-4D;
    }

    public static CompletableFuture<Void> loadAsync(TextureManager texMngr, Executor backgroundExecutor) {
        return CompletableFuture.allOf(texMngr.loadAsync(MINECRAFT_TITLE_TEXTURES, backgroundExecutor), texMngr.loadAsync(MINECRAFT_TITLE_EDITION, backgroundExecutor), texMngr.loadAsync(PANORAMA_OVERLAY_TEXTURES, backgroundExecutor), PANORAMA_RESOURCES.loadAsync(texMngr, backgroundExecutor));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        textBox = new TextBox(new Vector2f(5, 5), Fonts.SF_SEMIBOLD, 8, -1, TextAlign.LEFT, "Введите никнейм", 0, false, false);
        if (this.splashText == null) {
            this.splashText = this.minecraft.getSplashes().getSplashText();
        }

        this.widthCopyright = this.font.getStringWidth("Copyright Mojang AB. Do not distribute!");
        this.widthCopyrightRest = this.width - this.widthCopyright - 2;
        int i = 24;
        int j = this.height / 4 + 48;
        Button button = null;

        if (this.minecraft.isDemo()) {
            this.addDemoButtons(j, 25);
        } else {
            this.addSingleplayerMultiplayerButtons(j, 25);

            if (Reflector.ModListScreen_Constructor.exists()) {
                button = ReflectorForge.makeButtonMods(this, j, 24);
                this.addButton(button);
            }
        }

        this.addButton(new ImageButton(this.width / 2 - 124, j + 25 * 2, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (p_lambda$init$0_1_) ->
        {
            this.minecraft.displayScreen(new LanguageScreen(this, this.minecraft.gameSettings, this.minecraft.getLanguageManager()));
        }, new TranslationTextComponent("narrator.button.language")));
        this.addButton(new Button(this.width / 2 - 100, j + 25 * 2, 98, 20, new TranslationTextComponent("menu.options"), (p_lambda$init$1_1_) ->
        {
            this.minecraft.displayScreen(new OptionsScreen(this, this.minecraft.gameSettings));
        }));
        this.addButton(new Button(this.width / 2 + 2, j + 25 * 2, 98, 20, new TranslationTextComponent("menu.quit"), (p_lambda$init$2_1_) ->
        {
            this.minecraft.shutdown();
        }));
        this.addButton(new ImageButton(this.width / 2 + 104, j + 25 * 2, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURES, 32, 64, (p_lambda$init$3_1_) ->
        {
            this.minecraft.displayScreen(new AccessibilityScreen(this, this.minecraft.gameSettings));
        }, new TranslationTextComponent("narrator.button.accessibility")));

        textBox.setText(minecraft.session.getProfile().getName());
        textBox.setCursor(minecraft.session.getProfile().getName().length());
    }

    /**
     * Adds Singleplayer and Multiplayer buttons on Main Menu for players who have bought the game.
     */
    private void addSingleplayerMultiplayerButtons(int yIn, int rowHeightIn) {
        this.addButton(new Button(this.width / 2 - 100, yIn, 200, 20, new TranslationTextComponent("menu.singleplayer"), (p_lambda$addSingleplayerMultiplayerButtons$4_1_) ->
        {
            this.minecraft.displayScreen(new WorldSelectionScreen(this));
        }));
        boolean flag = this.minecraft.isMultiplayerEnabled();
        Button.ITooltip button$itooltip = flag ? Button.field_238486_s_ : (p_lambda$addSingleplayerMultiplayerButtons$5_1_, p_lambda$addSingleplayerMultiplayerButtons$5_2_, p_lambda$addSingleplayerMultiplayerButtons$5_3_, p_lambda$addSingleplayerMultiplayerButtons$5_4_) ->
        {
            if (!p_lambda$addSingleplayerMultiplayerButtons$5_1_.active) {
                this.renderTooltip(p_lambda$addSingleplayerMultiplayerButtons$5_2_, this.minecraft.fontRenderer.trimStringToWidth(new TranslationTextComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), p_lambda$addSingleplayerMultiplayerButtons$5_3_, p_lambda$addSingleplayerMultiplayerButtons$5_4_);
            }
        };
        (this.addButton(new Button(this.width / 2 - 100, yIn + rowHeightIn, 200, 20, new TranslationTextComponent("menu.multiplayer"), (p_lambda$addSingleplayerMultiplayerButtons$6_1_) ->
        {
            Screen screen = this.minecraft.gameSettings.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
            this.minecraft.displayScreen(screen);
        }, button$itooltip))).active = flag;

        if (Reflector.ModListScreen_Constructor.exists() && this.buttons.size() > 0) {
            Widget widget = this.buttons.get(this.buttons.size() - 1);
            widget.x = this.width / 2 + 2;
            widget.setWidth(98);
        }
    }

    /**
     * Adds Demo buttons on Main Menu for players who are playing Demo.
     */
    private void addDemoButtons(int yIn, int rowHeightIn) {
        boolean flag = this.func_243319_k();
        this.addButton(new Button(this.width / 2 - 100, yIn, 200, 20, new TranslationTextComponent("menu.playdemo"), (p_lambda$addDemoButtons$8_2_) ->
        {
            if (flag) {
                this.minecraft.loadWorld("Demo_World");
            } else {
                DynamicRegistries.Impl dynamicregistries$impl = DynamicRegistries.func_239770_b_();
                this.minecraft.createWorld("Demo_World", MinecraftServer.DEMO_WORLD_SETTINGS, dynamicregistries$impl, DimensionGeneratorSettings.func_242752_a(dynamicregistries$impl));
            }
        }));
        this.buttonResetDemo = this.addButton(new Button(this.width / 2 - 100, yIn + rowHeightIn, 200, 20, new TranslationTextComponent("menu.resetdemo"), (p_lambda$addDemoButtons$9_1_) ->
        {
            SaveFormat saveformat = this.minecraft.getSaveLoader();

            try (SaveFormat.LevelSave saveformat$levelsave = saveformat.getLevelSave("Demo_World")) {
                WorldSummary worldsummary = saveformat$levelsave.readWorldSummary();

                if (worldsummary != null) {
                    this.minecraft.displayScreen(new ConfirmScreen(this::deleteDemoWorld, new TranslationTextComponent("selectWorld.deleteQuestion"), new TranslationTextComponent("selectWorld.deleteWarning", worldsummary.getDisplayName()), new TranslationTextComponent("selectWorld.deleteButton"), DialogTexts.GUI_CANCEL));
                }
            } catch (IOException ioexception1) {
                field_238656_b_.warn("Failed to access demo world", ioexception1);
            }
        }));
        this.buttonResetDemo.active = flag;
    }

    private boolean func_243319_k() {
        try (SaveFormat.LevelSave saveformat$levelsave = this.minecraft.getSaveLoader().getLevelSave("Demo_World")) {
            return saveformat$levelsave.readWorldSummary() != null;
        } catch (IOException ioexception1) {
            field_238656_b_.warn("Failed to read demo world data", ioexception1);
            return false;
        }
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.firstRenderTime == 0L && this.showFadeInAnimation) {
            this.firstRenderTime = Util.milliTime();
        }

        float f = this.showFadeInAnimation ? (float) (Util.milliTime() - this.firstRenderTime) / 1000.0F : 1.0F;
        GlStateManager.disableDepthTest();
        fill(matrixStack, 0, 0, this.width, this.height, -1);
        this.panorama.render(partialTicks, MathHelper.clamp(f, 0.0F, 1.0F));
        int i = 274;
        int j = this.width / 2 - 137;
        int k = 30;
        this.minecraft.getTextureManager().bindTexture(PANORAMA_OVERLAY_TEXTURES);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.showFadeInAnimation ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
        blit(matrixStack, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float f1 = this.showFadeInAnimation ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(f1 * 255.0F) << 24;

        if ((l & -67108864) != 0) {
            this.minecraft.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);

            if (this.showTitleWronglySpelled) {
                this.blitBlackOutline(j, 30, (p_lambda$render$10_2_, p_lambda$render$10_3_) ->
                {
                    this.blit(matrixStack, p_lambda$render$10_2_, p_lambda$render$10_3_, 0, 0, 99, 44);
                    this.blit(matrixStack, p_lambda$render$10_2_ + 99, p_lambda$render$10_3_, 129, 0, 27, 44);
                    this.blit(matrixStack, p_lambda$render$10_2_ + 99 + 26, p_lambda$render$10_3_, 126, 0, 3, 44);
                    this.blit(matrixStack, p_lambda$render$10_2_ + 99 + 26 + 3, p_lambda$render$10_3_, 99, 0, 26, 44);
                    this.blit(matrixStack, p_lambda$render$10_2_ + 155, p_lambda$render$10_3_, 0, 45, 155, 44);
                });
            } else {
                this.blitBlackOutline(j, 30, (p_lambda$render$11_2_, p_lambda$render$11_3_) ->
                {
                    this.blit(matrixStack, p_lambda$render$11_2_, p_lambda$render$11_3_, 0, 0, 155, 44);
                    this.blit(matrixStack, p_lambda$render$11_2_ + 155, p_lambda$render$11_3_, 0, 45, 155, 44);
                });
            }

            this.minecraft.getTextureManager().bindTexture(MINECRAFT_TITLE_EDITION);
            blit(matrixStack, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);

            if (Reflector.ForgeHooksClient_renderMainMenu.exists()) {
                Reflector.callVoid(Reflector.ForgeHooksClient_renderMainMenu, this, matrixStack, this.font, this.width, this.height, l);
            }

            if (this.splashText != null) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float) (this.width / 2 + 90), 70.0F, 0.0F);
                RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
                float f2 = 1.8F - MathHelper.abs(MathHelper.sin((float) (Util.milliTime() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
                f2 = f2 * 100.0F / (float) (this.font.getStringWidth(this.splashText) + 32);
                RenderSystem.scalef(f2, f2, f2);
                drawCenteredStringWithShadow(matrixStack, this.font, this.splashText, 0, -8, 16776960 | l);
                RenderSystem.popMatrix();
            }

            String s = "Minecraft " + SharedConstants.getVersion().getName();

            if (this.minecraft.isDemo()) {
                s = s + " Demo";
            } else {
                s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (Reflector.BrandingControl.exists()) {
                if (Reflector.BrandingControl_forEachLine.exists()) {
                    BiConsumer<Integer, String> biconsumer = (p_lambda$render$12_3_, p_lambda$render$12_4_) ->
                    {
                        drawStringWithShadow(matrixStack, this.font, p_lambda$render$12_4_, 2, this.height - (10 + p_lambda$render$12_3_ * (9 + 1)), 16777215 | l);
                    };
                    Reflector.call(Reflector.BrandingControl_forEachLine, true, true, biconsumer);
                }

                if (Reflector.BrandingControl_forEachAboveCopyrightLine.exists()) {
                    BiConsumer<Integer, String> biconsumer1 = (p_lambda$render$13_3_, p_lambda$render$13_4_) ->
                    {
                        drawStringWithShadow(matrixStack, this.font, p_lambda$render$13_4_, this.width - this.font.getStringWidth(p_lambda$render$13_4_), this.height - (10 + (p_lambda$render$13_3_ + 1) * (9 + 1)), 16777215 | l);
                    };
                    Reflector.call(Reflector.BrandingControl_forEachAboveCopyrightLine, biconsumer1);
                }
            } else {
                drawStringWithShadow(matrixStack, this.font, s, 2, this.height - 10, 16777215 | l);
            }

            drawStringWithShadow(matrixStack, this.font, "Copyright Mojang AB. Do not distribute!", this.widthCopyrightRest, this.height - 10, 16777215 | l);

            if (mouseX > this.widthCopyrightRest && mouseX < this.widthCopyrightRest + this.widthCopyright && mouseY > this.height - 10 && mouseY < this.height) {
                fill(matrixStack, this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, 16777215 | l);
            }

            for (Widget widget : this.buttons) {
                widget.setAlpha(f1);
            }

            super.render(matrixStack, mouseX, mouseY, partialTicks);

            RenderUtil.Rounded.smooth(matrixStack, 2.5F, 2.5F, 105.0F, textBox.fontSize + 5.0F, ColorUtil.getColor(32), Round.of(2));
            textBox.setColor(-1);
            textBox.getPosition().set(5, 5);
            textBox.setWidth(100);
            textBox.draw(matrixStack);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        textBox.mouse(mouseX, mouseY, button);

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            if (mouseX > (double) this.widthCopyrightRest && mouseX < (double) (this.widthCopyrightRest + this.widthCopyright) && mouseY > (double) (this.height - 10) && mouseY < (double) this.height) {
                this.minecraft.displayScreen(new WinGameScreen(false, Runnables.doNothing()));
            }

            return false;
        }
    }

    private void deleteDemoWorld(boolean p_213087_1_) {
        if (p_213087_1_) {
            try (SaveFormat.LevelSave saveformat$levelsave = this.minecraft.getSaveLoader().getLevelSave("Demo_World")) {
                saveformat$levelsave.deleteSave();
            } catch (IOException ioexception1) {
                field_238656_b_.warn("Failed to delete demo world", ioexception1);
            }
        }

        this.minecraft.displayScreen(this);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String prevText = textBox.getText();
        textBox.keyPressed(keyCode);
        if (textBox.getText().length() >= 3 && PlayerUtil.isInvalidName(textBox.getText()))
            textBox.setText(prevText);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textBox.getText().length() < 16) {
            String prevText = textBox.getText();
            textBox.charTyped(codePoint);
            if (textBox.getText().length() >= 3 && PlayerUtil.isInvalidName(textBox.getText()))
                textBox.setText(prevText);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        textBox.selected = false;
        minecraft.session = new Session(textBox.getText());
        LoginManager.saveUsername(minecraft.session.getProfile().getName());
        textBox.setText(minecraft.session.getProfile().getName());
    }
}
