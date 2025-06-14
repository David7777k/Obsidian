package net.minecraft.client.renderer.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import org.obsidian.common.impl.modelgapfix.ModelGapFix;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ItemModelGenerator {
    public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");
    private final Map<TextureAtlasSprite, List<Span>> spanCache = new ConcurrentHashMap<>();

    public BlockModel makeItemModel(Function<RenderMaterial, TextureAtlasSprite> textureGetter, BlockModel blockModelIn) {
        Map<String, Either<RenderMaterial, String>> map = Maps.newHashMap();
        List<BlockPart> list = Lists.newArrayList();

        for (int i = 0; i < LAYERS.size(); ++i) {
            String s = LAYERS.get(i);

            if (!blockModelIn.isTexturePresent(s)) {
                break;
            }

            RenderMaterial rendermaterial = blockModelIn.resolveTextureName(s);
            map.put(s, Either.left(rendermaterial));
            TextureAtlasSprite textureatlassprite = textureGetter.apply(rendermaterial);
            list.addAll(this.getBlockParts(i, s, textureatlassprite));
        }

        map.put("particle", blockModelIn.isTexturePresent("particle") ? Either.left(blockModelIn.resolveTextureName("particle")) : map.get("layer0"));
        BlockModel blockmodel = new BlockModel(null, list, map, false, blockModelIn.getGuiLight(), blockModelIn.getAllTransforms(), blockModelIn.getOverrides());
        blockmodel.name = blockModelIn.name;
        return blockmodel;
    }


    private List<BlockPart> getBlockParts(int tintIndex, String textureIn, TextureAtlasSprite spriteIn) {
        if (Minecraft.getInstance().gameSettings.modelGapFix) {
            return ModelGapFix.createOutlineLayerElements(tintIndex, textureIn, spriteIn);
        } else {
            Map<Direction, BlockPartFace> map = Maps.newHashMap();
            map.put(Direction.SOUTH, new BlockPartFace(null, tintIndex, textureIn, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
            map.put(Direction.NORTH, new BlockPartFace(null, tintIndex, textureIn, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
            List<BlockPart> list = Lists.newArrayList();
            list.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, null, true));
            list.addAll(this.getBlockPartsFromSpans(spriteIn, textureIn, tintIndex));
            return list;
        }
    }

    private List<BlockPart> getBlockPartsFromSpans(TextureAtlasSprite spriteIn, String textureIn, int tintIndexIn) {
        List<Span> spans = spanCache.computeIfAbsent(spriteIn, this::getSpans);
        List<BlockPart> list = Lists.newArrayList();
        float f10 = 16.0F / spriteIn.getWidth();
        float f11 = 16.0F / spriteIn.getHeight();

        for (Span span : spans) {
            float f2, f3, f4, f5, f6, f7, f8, f9;
            float f12 = (float) span.getMin();
            float f13 = (float) span.getMax();
            float f14 = (float) span.getAnchor();
            SpanFacing spanFacing = span.getFacing();

            switch (spanFacing) {
                case UP -> {
                    f6 = f12;
                    f2 = f12;
                    f4 = f7 = f13 + 1.0F;
                    f8 = f14;
                    f3 = f14;
                    f5 = f14;
                    f9 = f14 + 1.0F;
                }
                case DOWN -> {
                    f8 = f14;
                    f9 = f14 + 1.0F;
                    f6 = f12;
                    f2 = f12;
                    f4 = f7 = f13 + 1.0F;
                    f3 = f14 + 1.0F;
                    f5 = f14 + 1.0F;
                }
                case LEFT -> {
                    f6 = f14;
                    f2 = f14;
                    f4 = f14;
                    f7 = f14 + 1.0F;
                    f9 = f12;
                    f3 = f12;
                    f5 = f8 = f13 + 1.0F;
                }
                case RIGHT -> {
                    f6 = f14;
                    f7 = f14 + 1.0F;
                    f2 = f14 + 1.0F;
                    f4 = f14 + 1.0F;
                    f9 = f12;
                    f3 = f12;
                    f5 = f8 = f13 + 1.0F;
                }
                default -> throw new IllegalStateException("Unexpected value: " + spanFacing);
            }

            f2 *= f10;
            f4 *= f10;
            f3 *= f11;
            f5 *= f11;
            f3 = 16.0F - f3;
            f5 = 16.0F - f5;
            f6 *= f10;
            f7 *= f10;
            f8 *= f11;
            f9 *= f11;
            Map<Direction, BlockPartFace> map = Maps.newHashMap();
            map.put(spanFacing.getFacing(), new BlockPartFace(null, tintIndexIn, textureIn, new BlockFaceUV(new float[]{f6, f8, f7, f9}, 0)));

            switch (spanFacing) {
                case UP ->
                        list.add(new BlockPart(new Vector3f(f2, f3, 7.5F), new Vector3f(f4, f3, 8.5F), map, null, true));
                case DOWN ->
                        list.add(new BlockPart(new Vector3f(f2, f5, 7.5F), new Vector3f(f4, f5, 8.5F), map, null, true));
                case LEFT ->
                        list.add(new BlockPart(new Vector3f(f2, f3, 7.5F), new Vector3f(f2, f5, 8.5F), map, null, true));
                case RIGHT ->
                        list.add(new BlockPart(new Vector3f(f4, f3, 7.5F), new Vector3f(f4, f5, 8.5F), map, null, true));
            }
        }

        return list;
    }

    private List<Span> getSpans(TextureAtlasSprite spriteIn) {
        int i = spriteIn.getWidth();
        int j = spriteIn.getHeight();
        List<Span> list = Lists.newArrayList();

        for (int k = 0; k < spriteIn.getFrameCount(); ++k) {
            for (int l = 0; l < j; ++l) {
                for (int i1 = 0; i1 < i; ++i1) {
                    boolean flag = !this.isTransparent(spriteIn, k, i1, l, i, j);
                    this.checkTransition(SpanFacing.UP, list, spriteIn, k, i1, l, i, j, flag);
                    this.checkTransition(SpanFacing.DOWN, list, spriteIn, k, i1, l, i, j, flag);
                    this.checkTransition(SpanFacing.LEFT, list, spriteIn, k, i1, l, i, j, flag);
                    this.checkTransition(SpanFacing.RIGHT, list, spriteIn, k, i1, l, i, j, flag);
                }
            }
        }

        return list;
    }

    private void checkTransition(ItemModelGenerator.SpanFacing spanFacingIn, List<ItemModelGenerator.Span> listSpansIn, TextureAtlasSprite spriteIn, int frameIndex, int pixelX, int pixelY, int spiteWidth, int spriteHeight, boolean transparent) {
        boolean flag = this.isTransparent(spriteIn, frameIndex, pixelX + spanFacingIn.getXOffset(), pixelY + spanFacingIn.getYOffset(), spiteWidth, spriteHeight) && transparent;

        if (flag) {
            this.createOrExpandSpan(listSpansIn, spanFacingIn, pixelX, pixelY);
        }
    }

    private void createOrExpandSpan(List<ItemModelGenerator.Span> listSpansIn, ItemModelGenerator.SpanFacing spanFacingIn, int pixelX, int pixelY) {
        ItemModelGenerator.Span itemmodelgenerator$span = null;

        for (ItemModelGenerator.Span itemmodelgenerator$span1 : listSpansIn) {
            if (itemmodelgenerator$span1.getFacing() == spanFacingIn) {
                int i = spanFacingIn.isHorizontal() ? pixelY : pixelX;

                if (itemmodelgenerator$span1.getAnchor() == i) {
                    itemmodelgenerator$span = itemmodelgenerator$span1;
                    break;
                }
            }
        }

        int j = spanFacingIn.isHorizontal() ? pixelY : pixelX;
        int k = spanFacingIn.isHorizontal() ? pixelX : pixelY;

        if (itemmodelgenerator$span == null) {
            listSpansIn.add(new ItemModelGenerator.Span(spanFacingIn, k, j));
        } else {
            itemmodelgenerator$span.expand(k);
        }
    }

    private boolean isTransparent(TextureAtlasSprite spriteIn, int frameIndex, int pixelX, int pixelY, int spiteWidth, int spriteHeight) {
        return pixelX < 0 || pixelY < 0 || pixelX >= spiteWidth || pixelY >= spriteHeight || spriteIn.isPixelTransparent(frameIndex, pixelX, pixelY);
    }

    static class Span {
        private final ItemModelGenerator.SpanFacing spanFacing;
        private int min;
        private int max;
        private final int anchor;

        public Span(ItemModelGenerator.SpanFacing spanFacingIn, int minIn, int maxIn) {
            this.spanFacing = spanFacingIn;
            this.min = minIn;
            this.max = minIn;
            this.anchor = maxIn;
        }

        public void expand(int posIn) {
            if (posIn < this.min) {
                this.min = posIn;
            } else if (posIn > this.max) {
                this.max = posIn;
            }
        }

        public ItemModelGenerator.SpanFacing getFacing() {
            return this.spanFacing;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public int getAnchor() {
            return this.anchor;
        }
    }

    enum SpanFacing {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction facing;
        private final int xOffset;
        private final int yOffset;

        SpanFacing(Direction facing, int xOffsetIn, int yOffsetIn) {
            this.facing = facing;
            this.xOffset = xOffsetIn;
            this.yOffset = yOffsetIn;
        }

        public Direction getFacing() {
            return this.facing;
        }

        public int getXOffset() {
            return this.xOffset;
        }

        public int getYOffset() {
            return this.yOffset;
        }

        private boolean isHorizontal() {
            return this == DOWN || this == UP;
        }
    }
}
