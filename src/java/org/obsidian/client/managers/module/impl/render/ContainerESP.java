package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.draw.RenderUtil3D;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ContainerESP", category = Category.RENDER)
public class ContainerESP extends Module {
    public static ContainerESP getInstance() {
        return Instance.get(ContainerESP.class);
    }

    // Настройки для отображения различных контейнеров и спавнеров
    private final MultiBooleanSetting blocks = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Сундук", true),
            BooleanSetting.of("Эндер-Сундук", false),
            BooleanSetting.of("Шалкер", false),
            BooleanSetting.of("Бочка", false),
            BooleanSetting.of("Воронка", false),
            BooleanSetting.of("Печка", false),
            BooleanSetting.of("Спавнер", false) // Поддержка спавнеров
    );

    // Карта для сопоставления типов TileEntity с цветами
    private final Map<Class<? extends TileEntity>, Color> colorMap = new HashMap<>();
    // Карта для сопоставления типов TileEntity с именами настроек
    private final Map<Class<? extends TileEntity>, String> settingNameMap = new HashMap<>();

    public ContainerESP() {
        // Инициализация цветовой карты
        colorMap.put(ChestTileEntity.class, new Color(0xFFB327));       // Сундук
        colorMap.put(EnderChestTileEntity.class, new Color(0x9456FF));  // Эндер-сундук
        colorMap.put(BarrelTileEntity.class, new Color(0xDC8024));      // Бочка
        colorMap.put(HopperTileEntity.class, new Color(0x47484D));      // Воронка
        colorMap.put(FurnaceTileEntity.class, new Color(0x181818));     // Печка
        colorMap.put(ShulkerBoxTileEntity.class, new Color(0xF12289));  // Шалкер
        colorMap.put(MobSpawnerTileEntity.class, new Color(0xFF0000));  // Спавнер

        // Инициализация карты имен настроек
        settingNameMap.put(ChestTileEntity.class, "Сундук");
        settingNameMap.put(EnderChestTileEntity.class, "Эндер-Сундук");
        settingNameMap.put(BarrelTileEntity.class, "Бочка");
        settingNameMap.put(HopperTileEntity.class, "Воронка");
        settingNameMap.put(FurnaceTileEntity.class, "Печка");
        settingNameMap.put(ShulkerBoxTileEntity.class, "Шалкер");
        settingNameMap.put(MobSpawnerTileEntity.class, "Спавнер");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DLastEvent e) {
        // Перебор всех загруженных TileEntity в мире
        for (TileEntity entity : mc.world.loadedTileEntityList) {
            Class<? extends TileEntity> clazz = entity.getClass();

            // Пропускаем, если тип TileEntity не поддерживается
            if (!colorMap.containsKey(clazz)) {
                continue;
            }

            // Проверяем, включена ли настройка для этого типа
            if (!blocks.getValue(settingNameMap.get(clazz))) {
                continue;
            }

            // Получаем цвет из карты
            Color color = colorMap.get(clazz);

            // Получаем форму блока
            VoxelShape shape = entity.getBlockState().getShape(mc.world, entity.getPos());

            // Если форма пустая – пропускаем этот тайл-ентити
            if (shape.isEmpty()) {
                continue;
            }

            // Вычисляем bounding box и смещаем его в мировые координаты
            AxisAlignedBB box = shape.getBoundingBox().offset(entity.getPos());

            // Пропускаем, если bounding box не в поле зрения
            if (!mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(box)) {
                continue;
            }

            // Отрисовываем bounding box
            RenderUtil3D.drawBoundingBox(e.getMatrix(), box, color.getRGB(), 1, false, false);
        }
    }

    @Override
    public boolean isStarred() {
        return true; // Звёздочка всегда отображается
    }
}
