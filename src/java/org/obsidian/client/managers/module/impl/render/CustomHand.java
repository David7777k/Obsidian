package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.render.ItemAnimationEvent;
import org.obsidian.client.managers.events.render.RenderItemEvent;
import org.obsidian.client.managers.events.render.SwingAnimationEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.combat.KillAura;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CustomHand", category = Category.RENDER)
public class CustomHand extends Module {
    public static CustomHand getInstance() {
        return Instance.get(CustomHand.class);
    }

    private final BooleanSetting translate = new BooleanSetting(this, "Позиция рук", true);
    private final SliderSetting offsetX = new SliderSetting(this, "X", 0.0F, -1F, 1F, 0.01F).setVisible(translate::getValue);
    private final SliderSetting offsetY = new SliderSetting(this, "Y", 0.0F, -1F, 1F, 0.01F).setVisible(translate::getValue);
    private final SliderSetting offsetZ = new SliderSetting(this, "Z", 0.0F, -1F, 1F, 0.01F).setVisible(translate::getValue);
    private final BooleanSetting auraOnly = new BooleanSetting(this, "Только с Aura", false);
    private final SliderSetting speed = new SliderSetting(this, "Скорость удара", 1F, 0.5F, 2.5F, 0.1F);

    public final ModeSetting swingMode = new ModeSetting(this, "Анимации",
            "Swipe",
            "Swipe Back",
            "Down",
            "Smooth",
            "Power",
            "Feast",
            "Off"
    ).set("Off");


    @EventHandler
    public void onEvent(SwingAnimationEvent event) {
        event.setAnimation((int) (event.getAnimation() * speed.getValue()));
    }

    @EventHandler
    public void onEvent(RenderItemEvent event) {
        boolean rightHand = event.getHandSide() == HandSide.RIGHT;
        MatrixStack matrix = event.getMatrix();
        if (translate.getValue()) {
            if (rightHand) {
                matrix.translate(
                        offsetX.getValue(),
                        offsetY.getValue(),
                        offsetZ.getValue()
                );
            } else {
                matrix.translate(
                        -offsetX.getValue(),
                        offsetY.getValue(),
                        offsetZ.getValue()
                );

            }
        }
    }

    @EventHandler
    public void onEvent(ItemAnimationEvent event) {
        if (auraCheck() && event.getHand().equals(Hand.MAIN_HAND) && (event.getMainHandStack().getItem() instanceof ToolItem || event.getMainHandStack().getItem() instanceof SwordItem)) {

            final String swingMode = this.swingMode.getValue();
            float swingProgress = event.getSwingProgress();
            float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);

            float amplitude = 0.5f;
            float sinSmooth = (float) (Math.sin(swingProgress * Math.PI) * amplitude);

            int i = event.getHandSide() == HandSide.RIGHT ? 1 : -1;
            MatrixStack matrix = event.getMatrix();
            switch (swingMode) {

                case "Swipe" -> {
                    matrix.translate((float) i * 0.56F, -0.32F, -0.72F);
                    matrix.rotate(Vector3f.YP.rotationDegrees(60 * i));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(-60 * i));
                    matrix.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                    matrix.rotate(Vector3f.XP.rotationDegrees((sin2 * sin1) * -120));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-60));
                }
                case "Swipe Back" -> {
                    matrix.translate((float) i * 0.56F, -0.32F, -0.72F);
                    matrix.rotate(Vector3f.YP.rotationDegrees(60 * i));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(-60 * i));
                    matrix.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                    matrix.rotate(Vector3f.XP.rotationDegrees((sin2 * sin1) * 120));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-60));
                }
                case "Down" -> {
                    matrix.translate((float) i * 0.56F, -0.32F, -0.72F);

                    matrix.rotate(Vector3f.YP.rotationDegrees(76 * i));
                    matrix.rotate(Vector3f.YP.rotationDegrees(sin2 * -5));
                    matrix.rotate(Vector3f.XN.rotationDegrees(sin2 * -100));
                    matrix.rotate(Vector3f.XP.rotationDegrees(sin2 * -155));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-100));
                }
                case "Smooth" -> {
                    matrix.translate((float) i * 0.56F, -0.52F, -0.72F);
                    matrix.translate(0, 0.1, 0);
                    matrix.rotate(Vector3f.XP.rotationDegrees(sin2 * -80));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(sin2 * (45 * i)));
                    matrix.rotate(Vector3f.YP.rotationDegrees(sin2 * (15 * i)));
                    matrix.translate(0, -0.1, 0);
                }
                case "Power" -> {
                    matrix.translate((float) i * 0.56F, -0.32F, -0.72F);
                    matrix.translate((-sinSmooth * sinSmooth * sin1) * i, 0, 0);

                    matrix.rotate(Vector3f.YP.rotationDegrees(61 * i));
                    matrix.rotate(Vector3f.ZP.rotationDegrees(sin2));
                    matrix.rotate(Vector3f.YP.rotationDegrees((sin2 * sin1) * -5));
                    matrix.rotate(Vector3f.XP.rotationDegrees((sin2 * sin1) * -30));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-60));
                    matrix.rotate(Vector3f.XP.rotationDegrees(sinSmooth * -60));
                }
                case "Feast" -> {
                    matrix.translate((float) i * 0.56F, -0.32F, -0.72F);
                    matrix.rotate(Vector3f.YP.rotationDegrees(30 * i));
                    matrix.rotate(Vector3f.YP.rotationDegrees(sin2 * 75 * i));
                    matrix.rotate(Vector3f.XP.rotationDegrees(sin2 * -45));
                    matrix.rotate(Vector3f.YP.rotationDegrees(30 * i));
                    matrix.rotate(Vector3f.XP.rotationDegrees(-80));
                    matrix.rotate(Vector3f.YP.rotationDegrees(35 * i));
                }
                default -> {
                    float f6 = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float f7 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float) Math.PI * 2F));
                    float f10 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);

                    matrix.translate((float) i * f6, f7, f10);

                    transformSideFirstPerson(matrix, event.getEquipProgress(), (float) i);
                    transformFirstPerson(matrix, swingProgress, (float) i);
                }
            }

            event.cancel();
        }
    }

    private static void transformFirstPerson(MatrixStack matrix, float swingProgress, float i) {
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrix.rotate(Vector3f.YP.rotationDegrees(i * (45.0F + f * -20.0F)));
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrix.rotate(Vector3f.ZP.rotationDegrees(i * f1 * -20.0F));
        matrix.rotate(Vector3f.XP.rotationDegrees(f1 * -80.0F));
        matrix.rotate(Vector3f.YP.rotationDegrees(i * -45.0F));
    }

    private static void transformSideFirstPerson(MatrixStack matrix, float equipProgress, float i) {
        matrix.translate(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    public boolean auraCheck() {
        KillAura aura = KillAura.getInstance();
        return !auraOnly.getValue() || (aura.isEnabled() && aura.target() != null);
    }
}