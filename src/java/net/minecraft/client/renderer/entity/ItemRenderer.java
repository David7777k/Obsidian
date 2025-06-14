package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.common.impl.fastrandom.FastRandom;

import java.util.Random;

public class ItemRenderer extends EntityRenderer<ItemEntity> {
    private final net.minecraft.client.renderer.ItemRenderer itemRenderer;
    private final Random random = new FastRandom();

    public ItemRenderer(EntityRendererManager renderManagerIn, net.minecraft.client.renderer.ItemRenderer itemRendererIn) {
        super(renderManagerIn);
        this.itemRenderer = itemRendererIn;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    private int getModelCount(ItemStack stack) {
        int i = 1;

        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        ItemStack itemstack = entityIn.getItem();
        int i = itemstack.isEmpty() ? 187 : Item.getIdFromItem(itemstack.getItem()) + itemstack.getDamage();
        this.random.setSeed(i);
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entityIn.world, null);
        boolean flag = ibakedmodel.isGui3d();
        int j = this.getModelCount(itemstack);
        float f = 0.25F;
        float f1 = MathHelper.sin(((float) entityIn.getAge() + partialTicks) / 10.0F + entityIn.hoverStart) * 0.1F + 0.1F;

        if (!this.shouldBob()) {
            f1 = 0.0F;
        }

        float f2 = ibakedmodel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.getY();
        matrixStackIn.translate(0.0D, f1 + 0.25F * f2, 0.0D);
        float f3 = entityIn.getItemHover(partialTicks);
        matrixStackIn.rotate(Vector3f.YP.rotation(f3));
        float f4 = ibakedmodel.getItemCameraTransforms().ground.scale.getX();
        float f5 = ibakedmodel.getItemCameraTransforms().ground.scale.getY();
        float f6 = ibakedmodel.getItemCameraTransforms().ground.scale.getZ();

        if (!flag) {
            float f7 = -0.0F * (float) (j - 1) * 0.5F * f4;
            float f8 = -0.0F * (float) (j - 1) * 0.5F * f5;
            float f9 = -0.09375F * (float) (j - 1) * 0.5F * f6;
            matrixStackIn.translate(f7, f8, f9);
        }

        for (int k = 0; k < j; ++k) {
            matrixStackIn.push();

            if (k > 0) {
                if (flag) {
                    float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;

                    if (!this.shouldSpreadItems()) {
                        f11 = 0.0F;
                        f13 = 0.0F;
                    }

                    matrixStackIn.translate(f11, f13, f10);
                } else {
                    float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrixStackIn.translate(f12, f14, 0.0D);
                }
            }

            this.itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);
            matrixStackIn.pop();

            if (!flag) {
                matrixStackIn.translate(0.0F * f4, 0.0F * f5, 0.09375F * f6);
            }
        }

        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getEntityTexture(ItemEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    public boolean shouldSpreadItems() {
        return true;
    }

    public boolean shouldBob() {
        return true;
    }
}
