package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.vertex.IVertexBuilder;
import org.obsidian.common.impl.fastrandom.FastRandom;

public class PistonTileEntityRenderer extends TileEntityRenderer<PistonTileEntity> {
    private final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

    public PistonTileEntityRenderer(TileEntityRendererDispatcher p_i226012_1_) {
        super(p_i226012_1_);
    }

    public void render(PistonTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        World world = tileEntityIn.getWorld();

        if (world != null) {
            BlockPos blockpos = tileEntityIn.getPos().offset(tileEntityIn.getMotionDirection().getOpposite());
            BlockState blockstate = tileEntityIn.getPistonState();

            if (!blockstate.isAir()) {
                BlockModelRenderer.enableCache();
                matrixStackIn.push();
                matrixStackIn.translate(tileEntityIn.getOffsetX(partialTicks), tileEntityIn.getOffsetY(partialTicks), tileEntityIn.getOffsetZ(partialTicks));

                if (blockstate.isIn(Blocks.PISTON_HEAD) && tileEntityIn.getProgress(partialTicks) <= 4.0F) {
                    blockstate = blockstate.with(PistonHeadBlock.SHORT, Boolean.valueOf(tileEntityIn.getProgress(partialTicks) <= 0.5F));
                    this.func_228876_a_(blockpos, blockstate, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
                } else if (tileEntityIn.shouldPistonHeadBeRendered() && !tileEntityIn.isExtending()) {
                    PistonType pistontype = blockstate.isIn(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
                    BlockState blockstate1 = Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.TYPE, pistontype).with(PistonHeadBlock.FACING, blockstate.get(PistonBlock.FACING));
                    blockstate1 = blockstate1.with(PistonHeadBlock.SHORT, Boolean.valueOf(tileEntityIn.getProgress(partialTicks) >= 0.5F));
                    this.func_228876_a_(blockpos, blockstate1, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
                    BlockPos blockpos1 = blockpos.offset(tileEntityIn.getMotionDirection());
                    matrixStackIn.pop();
                    matrixStackIn.push();
                    blockstate = blockstate.with(PistonBlock.EXTENDED, Boolean.valueOf(true));
                    this.func_228876_a_(blockpos1, blockstate, matrixStackIn, bufferIn, world, true, combinedOverlayIn);
                } else {
                    this.func_228876_a_(blockpos, blockstate, matrixStackIn, bufferIn, world, false, combinedOverlayIn);
                }

                matrixStackIn.pop();
                BlockModelRenderer.disableCache();
            }
        }
    }

    private void func_228876_a_(BlockPos p_228876_1_, BlockState p_228876_2_, MatrixStack p_228876_3_, IRenderTypeBuffer p_228876_4_, World p_228876_5_, boolean p_228876_6_, int p_228876_7_) {
        RenderType rendertype = RenderTypeLookup.func_239221_b_(p_228876_2_);
        IVertexBuilder ivertexbuilder = p_228876_4_.getBuffer(rendertype);
        this.blockRenderer.getBlockModelRenderer().renderModel(p_228876_5_, this.blockRenderer.getModelForState(p_228876_2_), p_228876_2_, p_228876_1_, p_228876_3_, ivertexbuilder, p_228876_6_, new FastRandom(), p_228876_2_.getPositionRandom(p_228876_1_), p_228876_7_);
    }
}
