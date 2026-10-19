package com.workshopcraft.muchmorestorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.CupboardBlock;
import com.workshopcraft.muchmorestorage.block.CupboardBlockEntity;
import com.workshopcraft.muchmorestorage.registry.Finishes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;


// the whole cupboard is drawn here since the finishes come from the block entity
public class CupboardRenderer implements BlockEntityRenderer<CupboardBlockEntity, CupboardRenderer.State> {
    private static final float MAX_ANGLE = 100F;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "oak";
        String top = "";
        boolean rightHinge;
        boolean assembled;
        float settle;
        int topLight;
        float open;
        int frontLight;
    }

    public CupboardRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(CupboardBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        s.facing = be.getBlockState().getValue(CupboardBlock.FACING);
        s.body = be.body();
        s.top = be.top();
        s.rightHinge = be.getBlockState().getValue(CupboardBlock.HINGE) == DoorHingeSide.RIGHT;
        s.assembled = be.getBlockState().getValue(CupboardBlock.ASSEMBLED);
        s.settle = be.settleProgress(partialTick);
        float o = 1F - be.getOpenNess(partialTick);
        s.open = 1F - o * o * o;
        s.frontLight = be.getLevel() == null ? s.lightCoords : LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        s.topLight = be.getLevel() == null ? s.lightCoords : LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().above());
    }

    private static void submit(State s, PoseStack pose, SubmitNodeCollector out,
                               int light, Consumer<Quads> draw) {
        out.submitCustomGeometry(pose, Sheets.cutoutBlockSheet(), (p, vc) -> draw.accept(new Quads(p, vc, light, OverlayTexture.NO_OVERLAY)));
        if (s.breakProgress != null) {
            ModelFeatureRenderer.CrumblingOverlay c = s.breakProgress;
            out.submitCustomGeometry(pose, ModelBakery.DESTROY_TYPES.get(c.progress()), (p, vc) ->
                    draw.accept(new Quads(p, new SheetedDecalTextureGenerator(vc, c.cameraPose(), 1F), light, OverlayTexture.NO_OVERLAY)));
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        CupboardModel.Body body = CupboardModel.Body.of(s.body);
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - s.facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        submit(s, pose, out, s.lightCoords, q -> CupboardModel.carcass(q, body));
        if (!s.top.isEmpty()) submitWorktop(s, pose, out);
        float hingeX = s.rightHinge ? 0F : 1F;
        pose.translate(hingeX, 0, 1 / 16F);
        pose.mulPose(Axis.YP.rotationDegrees((s.rightHinge ? MAX_ANGLE : -MAX_ANGLE) * s.open));
        pose.translate(-hingeX, 0, -1 / 16F);
        submit(s, pose, out, s.frontLight, q -> CupboardModel.door(q, body, s.rightHinge));
        pose.popPose();
    }

    // a loose worktop sits a little high and askew, then drops flat while it is screwed down
    private void submitWorktop(State s, PoseStack pose, SubmitNodeCollector out) {
        TextureAtlasSprite top = CupboardModel.sprite(Finishes.topSprite(s.top));
        float k = s.assembled ? 0F : 1F - s.settle * s.settle;
        pose.pushPose();
        pose.translate(0.5F + 0.6F / 16 * k, 15 / 16F + 1.5F / 16 * k, 0.5F + 0.4F / 16 * k);
        pose.mulPose(Axis.YP.rotationDegrees(4F * k));
        pose.mulPose(Axis.ZP.rotationDegrees(-2F * k));
        pose.mulPose(Axis.XP.rotationDegrees(1F * k));
        pose.translate(-0.5F, -15 / 16F, -0.5F);
        submit(s, pose, out, s.topLight, q -> CupboardModel.worktop(q, top));
        pose.popPose();
    }
}
