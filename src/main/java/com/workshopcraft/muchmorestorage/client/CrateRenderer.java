package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.CrateBlock;
import com.workshopcraft.muchmorestorage.block.CrateBlockEntity;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class CrateRenderer implements BlockEntityRenderer<CrateBlockEntity, CrateRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "oak";
        int step;
        float time;
    }

    public CrateRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(CrateBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        s.facing = be.getBlockState().getValue(FurnitureBlock.FACING);
        s.step = be.getBlockState().getValue(CrateBlock.STEP);
        s.body = be.body();
        s.time = be.getLevel() == null ? 0 : be.getLevel().getGameTime() + partialTick;
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        CupboardModel.Body body = CupboardModel.Body.of(s.body);
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees(180F - s.facing.toYRot()));
        if (s.step > 0 && s.step < CrateBlock.TURNS) {
            // half built, it wobbles
            KubhalaRenderer.rotate(pose, Axis.ZP.rotationDegrees((float) Math.sin(s.time * 0.5F) * 2.5F));
            KubhalaRenderer.rotate(pose, Axis.XP.rotationDegrees((float) Math.sin(s.time * 0.37F + 1F) * 1.5F));
        }
        pose.translate(-0.5F, 0, -0.5F);
        KubhalaRenderer.submit(s, pose, out, s.lightCoords, q -> FurnitureModel.crate(q, body, s.step));
        pose.popPose();
    }
}
//?}
