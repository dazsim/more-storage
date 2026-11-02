package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.GenomskadaBlockEntity;
//? if >=26.3 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
*///?} else
import net.minecraft.client.renderer.Sheets;
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

public class GenomskadaRenderer implements BlockEntityRenderer<GenomskadaBlockEntity, GenomskadaRenderer.State> {
    //? if >=26.3
    /*private static final RenderType CLEAR = RenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);*/
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "white_concrete";
        final List<ItemStackRenderState> shown = new ArrayList<>();
        final List<Integer> slots = new ArrayList<>();
    }

    public GenomskadaRenderer(BlockEntityRendererProvider.Context ctx) {
        items = ctx.itemModelResolver();
    }

    @Override
    public int getViewDistance() { return 64; }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(GenomskadaBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        s.facing = be.getBlockState().getValue(FurnitureBlock.FACING);
        s.body = GenomskadaBlockEntity.lid(be.body());
        s.shown.clear();
        s.slots.clear();
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; i < be.getContainerSize(); i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) continue;
            ItemStackRenderState r = new ItemStackRenderState();
            items.updateForTopItem(r, stack, ItemDisplayContext.FIXED, be.getLevel(), null, seed + i);
            s.shown.add(r);
            s.slots.add(i);
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        var lid = CupboardModel.top(s.body);
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees(180F - s.facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        KubhalaRenderer.submit(s, pose, out, s.lightCoords, q -> FurnitureModel.genomskadaLid(q, lid));
        // nine stacks lie flat in a 3 by 3 grid on the floor
        for (int n = 0; n < s.shown.size(); n++) {
            ItemStackRenderState r = s.shown.get(n);
            int i = s.slots.get(n);
            AABB box = r.getModelBoundingBox();
            float sc = r.usesBlockLight() ? 0.22F : 0.26F;
            pose.pushPose();
            pose.translate((4 + (i % 3) * 4) / 16F, 0.6F / 16F, (4 + (i / 3) * 4) / 16F);
            KubhalaRenderer.rotate(pose, Axis.XP.rotationDegrees(-90F));
            pose.scale(sc, sc, sc);
            pose.translate(0, 0, -(float) box.minZ);
            r.submit(pose, out, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        //? if >=26.3 {
        /*out.submitCustomGeometry(pose, CLEAR, (p, vc) -> FurnitureModel.genomskadaWalls(new Quads(p, vc, s.lightCoords, OverlayTexture.NO_OVERLAY)));
        *///?} else
        out.submitCustomGeometry(pose, Sheets.translucentBlockSheet(), (p, vc) -> FurnitureModel.genomskadaWalls(new Quads(p, vc, s.lightCoords, OverlayTexture.NO_OVERLAY)));
        pose.popPose();
    }
}
//?}
