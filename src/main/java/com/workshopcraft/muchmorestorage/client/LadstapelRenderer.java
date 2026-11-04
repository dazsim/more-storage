package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.LadstapelBlockEntity;
//? if >=26.2 {
/*import net.minecraft.util.LightCoordsUtil;
*///?} else
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LadstapelRenderer implements BlockEntityRenderer<LadstapelBlockEntity, LadstapelRenderer.State> {
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "oak";
        boolean assembled;
        int frontLight;
        final float[] open = new float[LadstapelBlockEntity.DRAWERS];
        final List<List<ItemStackRenderState>> shown = new ArrayList<>();
        final List<List<Boolean>> blocks = new ArrayList<>();
    }

    public LadstapelRenderer(BlockEntityRendererProvider.Context ctx) {
        items = ctx.itemModelResolver();
    }

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public State createRenderState() {
        State s = new State();
        for (int d = 0; d < LadstapelBlockEntity.DRAWERS; d++) {
            s.shown.add(new ArrayList<>());
            s.blocks.add(new ArrayList<>());
        }
        return s;
    }

    @Override
    public void extractRenderState(LadstapelBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        var state = be.getBlockState();
        s.facing = state.getValue(FurnitureBlock.FACING);
        s.body = be.body();
        s.assembled = state.getValue(FurnitureBlock.ASSEMBLED);
        //? if >=26.2 {
        /*s.frontLight = be.getLevel() == null ? s.lightCoords : LightCoordsUtil.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        *///?} else
        s.frontLight = be.getLevel() == null ? s.lightCoords : LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        int seed = (int) be.getBlockPos().asLong();
        for (int d = 0; d < LadstapelBlockEntity.DRAWERS; d++) {
            float o = 1F - be.drawerOpenness(d, partialTick);
            s.open[d] = 1F - o * o * o;
            List<ItemStackRenderState> shown = s.shown.get(d);
            List<Boolean> blocks = s.blocks.get(d);
            shown.clear();
            blocks.clear();
            var drawer = be.drawer(d);
            for (int i = 0; s.assembled && s.open[d] > 0.05F && i < drawer.getContainerSize() && shown.size() < 3; i++) {
                ItemStack stack = drawer.getItem(i);
                if (stack.isEmpty()) continue;
                ItemStackRenderState r = new ItemStackRenderState();
                items.updateForTopItem(r, stack, ItemDisplayContext.FIXED, be.getLevel(), null, seed + d * 9 + i);
                shown.add(r);
                blocks.add(r.usesBlockLight());
            }
        }
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
            pose.popPose();
            return;
        }
        KubhalaRenderer.submit(s, pose, out, s.lightCoords, q -> FurnitureModel.ladstapel(q, body));
        for (int d = 0; d < LadstapelBlockEntity.DRAWERS; d++) {
            int drawer = d;
            pose.pushPose();
            pose.translate(0, 0, -FurnitureModel.DRAWER_OUT / 16F * s.open[d]);
            KubhalaRenderer.submit(s, pose, out, s.frontLight, q -> FurnitureModel.drawer(q, body, drawer));
            submitItems(s, d, pose, out);
            pose.popPose();
        }
        pose.popPose();
    }

    // up to three stacks lying back in the open drawer so they show from a standing view
    private void submitItems(State s, int d, PoseStack pose, SubmitNodeCollector out) {
        List<ItemStackRenderState> shown = s.shown.get(d);
        int n = shown.size();
        if (n == 0) return;
        float[] xs = n == 1 ? new float[]{8} : n == 2 ? new float[]{5.75F, 10.25F} : new float[]{4.5F, 8, 11.5F};
        float scale = n == 1 ? 0.4F : 0.32F;
        float floor = FurnitureModel.drawerFloor(d) / 16F;
        for (int i = 0; i < n; i++) {
            ItemStackRenderState r = shown.get(i);
            float sc = s.blocks.get(d).get(i) ? scale * 1.2F : scale;
            AABB box = r.getModelBoundingBox();
            pose.pushPose();
            pose.translate(xs[i] / 16F, floor + 0.01F / 16, (5.5F + (i % 2)) / 16F);
            KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees(180F + (i - 1) * 6F));
            KubhalaRenderer.rotate(pose, Axis.XP.rotationDegrees(-60F));
            pose.scale(sc, sc, sc);
            pose.translate(0, -box.minY, 0);
            r.submit(pose, out, s.frontLight, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
//?}
