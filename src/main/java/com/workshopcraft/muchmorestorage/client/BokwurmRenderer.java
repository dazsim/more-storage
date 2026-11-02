package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.BokwurmBlockEntity;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BokwurmRenderer implements BlockEntityRenderer<BokwurmBlockEntity, BokwurmRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "oak";
        boolean assembled;
        final int[] colours = new int[BokwurmBlockEntity.SLOTS];
        final float[] heights = new float[BokwurmBlockEntity.SLOTS];
    }

    public BokwurmRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BokwurmBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        var state = be.getBlockState();
        s.facing = state.getValue(FurnitureBlock.FACING);
        s.body = be.body();
        s.assembled = state.getValue(FurnitureBlock.ASSEMBLED);
        FurnitureModel.fillBooks(be, s.colours, s.heights);
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        CupboardModel.Body body = CupboardModel.Body.of(s.body);
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees(180F - s.facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        if (!s.assembled) {
            KubhalaRenderer.submit(s, pose, out, s.lightCoords, q -> KubhalaModel.flatpack(q, body));
        } else {
            var sp = FurnitureModel.book();
            KubhalaRenderer.submit(s, pose, out, s.lightCoords, q -> {
                FurnitureModel.bokwurm(q, body);
                for (int i = 0; i < s.heights.length; i++) {
                    if (s.heights[i] > 0) FurnitureModel.book(q, sp, i, s.colours[i], s.heights[i]);
                }
            });
        }
        pose.popPose();
    }
}
//?}
