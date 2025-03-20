package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.VanillaPack;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.optifine.Config;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.util.PropertiesOrdered;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.font.Fonts;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public class ResourceLoadProgressGui extends LoadingGui {
    private static final ResourceLocation MOJANG_LOGO_TEXTURE = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int field_238627_b_ = ColorHelper.PackedColor.packColor(255, 239, 50, 61);
    private static final int field_238628_c_ = field_238627_b_ & 16777215;
    private final Minecraft mc;
    private final IAsyncReloader asyncReloader;
    private final Consumer<Optional<Throwable>> completedCallback;
    private final boolean reloading;
    private float progress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;
    private int colorBackground = field_238628_c_;
    private int colorBar = field_238628_c_;
    private int colorOutline = 16777215;
    private int colorProgress = 16777215;
    private GlBlendState blendState = null;
    private boolean fadeOut = false;

    // Расширенный список советов
    private static final List<String> TIPS = Arrays.asList(
            "Нажмите F3+B чтобы видеть хитбоксы сущностей",
            "Используйте команду .bind для настройки горячих клавиш",
            "Включите модуль AutoTotem для автоматической экипировки тотема",
            "Нажмите CTRL+Средняя кнопка мыши для дюпа предметов в креативе",
            "Модуль KillAura автоматически атакует ближайших врагов",
            "Используйте модуль ESP для подсветки сущностей через стены",
            "Настройте тему клиента в меню Theme",
            "Настройте цвета через меню Colors",
            "Используйте модуль Scaffold для автоматической установки блоков",
            "Модуль FastPlace ускоряет размещение блоков",
            "Используйте команду .modules для просмотра всех доступных модулей",
            "Настройте HUD в меню HUD для отображения нужной информации",
            "Модуль Flight позволяет летать в выживании",
            "Используйте команду .friend для управления списком друзей",
            "Модуль NoRender убирает визуальные эффекты для лучшей производительности",
            "Нажмите F3+T для перезагрузки текстур",
            "Используйте модуль XRay для видения через блоки",
            "Модуль AutoFish автоматически ловит рыбу",
            "Настройте скорость анимации в настройках",
            "Используйте команду .config для сохранения и загрузки конфигураций",
            "Модуль ChestESP подсвечивает сундуки через стены",
            "Используйте .search для поиска блоков или предметов",
            "Модуль AntiAFK предотвращает отключение за бездействие",
            "Нажмите F3+Q для списка всех F3-комбинаций",
            "Модуль Speed увеличивает скорость передвижения"
    );

    // Текущий совет и анимация
    private String currentTip;
    private float tipAlpha = 1.0f;
    private long lastTipChange = Util.milliTime();
    private static final long TIP_CHANGE_INTERVAL = 5000; // 5 секунд на совет

    // Этапы загрузки
    private String currentLoadingStage = "Инициализация...";
    private float stageProgress = 0.0f;
    private static final Map<Float, String> LOADING_STAGES = new LinkedHashMap<Float, String>() {{
        put(0.1f, "Загрузка текстур...");
        put(0.25f, "Инициализация модулей...");
        put(0.4f, "Загрузка настроек...");
        put(0.6f, "Применение шейдеров...");
        put(0.8f, "Финальная подготовка...");
        put(0.95f, "Запуск...");
    }};

    public ResourceLoadProgressGui(Minecraft p_i225928_1_, IAsyncReloader p_i225928_2_, Consumer<Optional<Throwable>> p_i225928_3_, boolean p_i225928_4_) {
        this.mc = p_i225928_1_;
        this.asyncReloader = p_i225928_2_;
        this.completedCallback = p_i225928_3_;
        this.reloading = false;

        // Инициализация первого совета
        this.currentTip = getRandomTip();
        this.lastTipChange = Util.milliTime();
    }

    // Получение случайного совета
    private String getRandomTip() {
        int index = new Random().nextInt(TIPS.size());
        return TIPS.get(index);
    }

    // Обновление анимации совета
    private void updateTipAnimation(long currentTime) {
        if (currentTime - lastTipChange > TIP_CHANGE_INTERVAL) {
            currentTip = getRandomTip();
            lastTipChange = currentTime;
            tipAlpha = 0.0f;
        } else {
            tipAlpha = Math.min(tipAlpha + 0.05f, 1.0f);
        }
    }

    // Обновление текущего этапа загрузки
    private void updateLoadingStage() {
        for (Map.Entry<Float, String> entry : LOADING_STAGES.entrySet()) {
            if (this.progress <= entry.getKey()) {
                currentLoadingStage = entry.getValue();
                stageProgress = this.progress / entry.getKey();
                break;
            }
        }
    }

    public static void loadLogoTexture(Minecraft mc) {
        mc.getTextureManager().loadTexture(MOJANG_LOGO_TEXTURE, new ResourceLoadProgressGui.MojangLogoTexture());
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int width = this.mc.getMainWindow().getScaledWidth();
        int height = this.mc.getMainWindow().getScaledHeight();
        long currentTime = Util.milliTime();

        if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = currentTime;
        }

        float fadeOutProgress = this.fadeOutStart > -1L ? (float) (currentTime - this.fadeOutStart) / 1000.0F : -1.0F;
        float fadeInProgress = this.fadeInStart > -1L ? (float) (currentTime - this.fadeInStart) / 500.0F : -1.0F;
        float alpha;

        if (fadeOutProgress >= 1.0F) {
            this.fadeOut = true;

            if (this.mc.currentScreen != null) {
                this.mc.currentScreen.render(matrixStack, 0, 0, partialTicks);
            }
            float l = (1.0F - MathHelper.clamp(fadeOutProgress - 1.0F, 0.0F, 1.0F));
            RenderUtil.drawShaderBackground(matrixStack, l);
            alpha = 1.0F - MathHelper.clamp(fadeOutProgress - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            if (this.mc.currentScreen != null && fadeInProgress < 1.0F) {
                this.mc.currentScreen.render(matrixStack, mouseX, mouseY, partialTicks);
            }

            float i2 = MathHelper.clamp(fadeInProgress, 0.15F, 1.0F);
            RenderUtil.drawShaderBackground(matrixStack, i2);
            alpha = MathHelper.clamp(fadeInProgress, 0.0F, 1.0F);
        } else {
            float i2 = MathHelper.clamp(fadeInProgress, 0.15F, 1.0F);
            RenderUtil.drawShaderBackground(matrixStack, 1F);
            alpha = 1.0F;
        }

        int centerX = width / 2;
        int centerY = height / 2;

        Theme theme = Theme.getInstance();
        int textColor = ColorUtil.getColor(200, 200, 230, alpha);
        int accentColor = ColorUtil.multAlpha(theme.clientColor(), alpha);
        int secondaryColor = ColorUtil.multAlpha(theme.secondaryColor(), alpha);
        int tertiaryColor = ColorUtil.multAlpha(theme.tertiaryColor(), alpha);
        float darker = 0.5F;

        // Отображение основного текста
        String namespace = ColorFormatting.getColor(accentColor) + Constants.NAMESPACE + ColorFormatting.reset() + " - выбор лучших.";
        Fonts.SF_REGULAR.drawCenter(matrixStack, namespace, centerX, centerY - 12, ColorUtil.multDark(textColor, darker), 8);

        float textWidth = Fonts.SF_REGULAR.getWidth(namespace, 8);

        int progressY = centerY + 12;
        float f3 = this.asyncReloader.estimateExecutionSpeed();
        this.progress = MathHelper.clamp(this.progress * 0.95F + f3 * 0.050000012F, 0.0F, 1.0F);

        // Обновление этапа загрузки
        updateLoadingStage();

        // Обновление анимации совета
        updateTipAnimation(currentTime);

        int k2 = (int) (textWidth / 2F);
        if (fadeOutProgress < 1.0F) {
            this.drawProgressBar(matrixStack, centerX - k2, progressY - 1, centerX + k2, progressY + 1, 1.0F - MathHelper.clamp(fadeOutProgress, 0.0F, 1.0F));

            // Отображение текущего этапа загрузки
            Fonts.SF_REGULAR.drawCenter(matrixStack, currentLoadingStage, centerX, progressY + 8,
                    ColorUtil.multAlpha(secondaryColor, alpha), 7);

            // Отображение совета с анимацией
            String tipText = "Совет: " + currentTip;
            Fonts.SF_REGULAR.drawCenter(matrixStack, tipText, centerX, progressY + 20,
                    ColorUtil.multAlpha(tertiaryColor, alpha * tipAlpha), 6);

            // Отображение процента загрузки
            String percentText = String.format("%.1f%%", this.progress * 100);
            Fonts.SF_REGULAR.drawCenter(matrixStack, percentText, centerX, progressY - 10,
                    ColorUtil.multAlpha(secondaryColor, alpha), 7);
        }

        if (fadeOutProgress >= 2.0F) {
            this.mc.setLoadingGui(null);
        }

        if (this.fadeOutStart == -1L && this.asyncReloader.fullyDone() && (!this.reloading || fadeInProgress >= 2.0F)) {
            this.fadeOutStart = Util.milliTime();

            try {
                this.asyncReloader.join();
                this.completedCallback.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.completedCallback.accept(Optional.of(throwable));
            }

            if (this.mc.currentScreen != null) {
                this.mc.currentScreen.init(this.mc, this.mc.getMainWindow().getScaledWidth(), this.mc.getMainWindow().getScaledHeight());
            }
        }
    }

    private void drawProgressBar(MatrixStack matrix, int minX, int minY, int maxX, int maxY, float alphaPC) {
        int width = maxX - minX;
        int height = maxY - minY;
        int progressWidth = MathHelper.ceil((float) (width) * this.progress);
        int alpha = Math.round(alphaPC * 255.0F);

        // Использование цветов темы
        Theme theme = Theme.getInstance();
        int color = ColorUtil.multDark(ColorUtil.multAlpha(theme.clientColor(), alpha), 0.5F);
        int colorGlow = ColorUtil.multAlpha(ColorUtil.multAlpha(theme.clientColor(), alpha), 0.25F);
        int colorBackground = ColorUtil.getColor(20, 20, 30, alpha);
        int colorOutline = ColorUtil.multAlpha(theme.tertiaryColor(), alpha * 0.7f);

        // Рисуем фон прогресс-бара
        RectUtil.drawRoundedRectShadowed(matrix, minX, minY, width, height, height / 4F, 1,
                colorBackground, colorBackground, colorBackground, colorBackground,
                false, false, true, true);

        // Рисуем свечение прогресс-бара
        RectUtil.drawRoundedRectShadowed(matrix, minX, minY + height / 2F, progressWidth, 0, 0, 8,
                colorGlow, colorGlow, colorGlow, colorGlow,
                true, true, true, true);

        // Рисуем сам прогресс-бар
        RectUtil.drawRoundedRectShadowed(matrix, minX, minY, progressWidth, height, height / 4F, 1,
                color, color, color, color,
                true, false, true, true);

        // Добавляем обводку для прогресс-бара
        RectUtil.drawRoundedRect(matrix, minX, minY, width, height, height / 4F,
                colorOutline);
    }

    public void update() {
        this.colorBackground = field_238628_c_;
        this.colorBar = field_238628_c_;
        this.colorOutline = 16777215;
        this.colorProgress = 16777215;

        if (Config.isCustomColors()) {
            try {
                String s = "optifine/color.properties";
                ResourceLocation resourcelocation = new ResourceLocation(s);

                if (!Config.hasResource(resourcelocation)) {
                    return;
                }

                InputStream inputstream = Config.getResourceStream(resourcelocation);
                Config.dbg("Loading " + s);
                Properties properties = new PropertiesOrdered();
                properties.load(inputstream);
                inputstream.close();
                this.colorBackground = readColor(properties, "screen.loading", this.colorBackground);
                this.colorOutline = readColor(properties, "screen.loading.outline", this.colorOutline);
                this.colorBar = readColor(properties, "screen.loading.bar", this.colorBar);
                this.colorProgress = readColor(properties, "screen.loading.progress", this.colorProgress);
                this.blendState = ShaderPackParser.parseBlendState(properties.getProperty("screen.loading.blend"));
            } catch (Exception exception) {
                Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
            }
        }
    }

    private static int readColor(Properties p_readColor_0_, String p_readColor_1_, int p_readColor_2_) {
        String s = p_readColor_0_.getProperty(p_readColor_1_);

        if (s == null) {
            return p_readColor_2_;
        } else {
            s = s.trim();
            int i = parseColor(s, p_readColor_2_);

            if (i < 0) {
                Config.warn("Invalid color: " + p_readColor_1_ + " = " + s);
                return i;
            } else {
                Config.dbg(p_readColor_1_ + " = " + s);
                return i;
            }
        }
    }

    private static int parseColor(String p_parseColor_0_, int p_parseColor_1_) {
        if (p_parseColor_0_ == null) {
            return p_parseColor_1_;
        } else {
            p_parseColor_0_ = p_parseColor_0_.trim();

            try {
                return Integer.parseInt(p_parseColor_0_, 16) & 16777215;
            } catch (NumberFormatException numberformatexception) {
                return p_parseColor_1_;
            }
        }
    }

    public boolean isFadeOut() {
        return this.fadeOut;
    }

    static class MojangLogoTexture extends SimpleTexture {
        public MojangLogoTexture() {
            super(ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE);
        }

        protected SimpleTexture.TextureData getTextureData(IResourceManager resourceManager) {
            Minecraft minecraft = Minecraft.getInstance();
            VanillaPack vanillapack = minecraft.getPackFinder().getVanillaPack();

            try (InputStream inputstream = getLogoInputStream(resourceManager, vanillapack)) {
                return new SimpleTexture.TextureData(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
            } catch (IOException ioexception1) {
                return new SimpleTexture.TextureData(ioexception1);
            }
        }

        private static InputStream getLogoInputStream(IResourceManager p_getLogoInputStream_0_, VanillaPack p_getLogoInputStream_1_) throws IOException {
            return p_getLogoInputStream_0_.hasResource(ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE) ? p_getLogoInputStream_0_.getResource(ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE).getInputStream() : p_getLogoInputStream_1_.getResourceStream(ResourcePackType.CLIENT_RESOURCES, ResourceLoadProgressGui.MOJANG_LOGO_TEXTURE);
        }
    }
}