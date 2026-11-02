package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.Insert;
import com.workshopcraft.muchmorestorage.block.KubhalaBlock;
import com.workshopcraft.muchmorestorage.block.KubhalaBlockEntity;
import com.workshopcraft.muchmorestorage.item.SakksakkItem;
//? if >=26.3 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.LightCoordsUtil;
*///?} else {
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
//?}
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KubhalaRenderer implements BlockEntityRenderer<KubhalaBlockEntity, KubhalaRenderer.State> {
    private static final float DOOR_ANGLE = 100F;
    //? if >=26.3
    /*private static final RenderType CUTOUT_BLOCK_SHEET = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);*/
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "oak";
        String box = "white";
        boolean assembled;
        Insert insert = Insert.NONE;
        boolean fitted;
        float open;
        int frontLight;
        KubhalaModel.Join join = KubhalaModel.Join.NONE;
        final List<ItemStackRenderState> shown = new ArrayList<>();
        final List<Boolean> blocks = new ArrayList<>();
    }

    public KubhalaRenderer(BlockEntityRendererProvider.Context ctx) {
        items = ctx.itemModelResolver();
    }

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(KubhalaBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        var state = be.getBlockState();
        s.facing = state.getValue(KubhalaBlock.FACING);
        s.body = be.body();
        s.box = be.box();
        s.assembled = state.getValue(KubhalaBlock.ASSEMBLED);
        s.insert = state.getValue(KubhalaBlock.INSERT);
        s.fitted = state.getValue(KubhalaBlock.FITTED);
        float o = 1F - be.getOpenNess(partialTick);
        s.open = 1F - o * o * o;
        s.join = be.getLevel() == null || !s.assembled ? KubhalaModel.Join.NONE : KubhalaModel.Join.of(be.getLevel(), be.getBlockPos(), state);
        //? if >=26.3 {
        /*s.frontLight = be.getLevel() == null ? s.lightCoords : LightCoordsUtil.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        *///?} else
        s.frontLight = be.getLevel() == null ? s.lightCoords : LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        s.shown.clear();
        s.blocks.clear();
        boolean visible = s.assembled && (s.insert == Insert.NONE || s.insert.closes() && s.open > 0.05F);
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; visible && i < be.getContainerSize() && s.shown.size() < 3; i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) continue;
            ItemStackRenderState r = new ItemStackRenderState();
            items.updateForTopItem(r, stack, ItemDisplayContext.FIXED, be.getLevel(), null, seed + i);
            s.shown.add(r);
            s.blocks.add(r.usesBlockLight());
        }
    }

    private static void submit(State s, PoseStack pose, SubmitNodeCollector out, int light, Consumer<Quads> draw) {
        //? if >=26.3 {
        /*out.submitCustomGeometry(pose, CUTOUT_BLOCK_SHEET, (p, vc) -> draw.accept(new Quads(p, vc, light, OverlayTexture.NO_OVERLAY)));
        *///?} else
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
        rotate(pose, Axis.YP.rotationDegrees(180F - s.facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        if (!s.assembled) {
            submit(s, pose, out, s.lightCoords, q -> KubhalaModel.flatpack(q, body));
            pose.popPose();
            return;
        }
        submit(s, pose, out, s.lightCoords, q -> KubhalaModel.frame(q, body, s.join));
        if (s.insert != Insert.DRAWER) submitItems(s, pose, out);
        if (s.insert == Insert.BOX) {
            pose.pushPose();
            pose.translate(0, 0, -3 / 16F * s.open);
            var fabric = CupboardModel.sprite(SakksakkItem.texture(s.box));
            submit(s, pose, out, s.frontLight, q -> KubhalaModel.sakksakk(q, fabric, s.join));
            pose.popPose();
        }
        if (s.insert.closes()) {
            pose.pushPose();
            if (!s.fitted) loose(pose);
            if (s.insert == Insert.DRAWER) {
                pose.translate(0, 0, -9 / 16F * s.open);
                submit(s, pose, out, s.frontLight, q -> KubhalaModel.drawer(q, body, s.join));
                submitDrawerItems(s, pose, out);
            } else {
                float hx = KubhalaModel.doorHinge(s.join);
                pose.translate(hx, 0, 0.1F / 16);
                rotate(pose, Axis.YP.rotationDegrees(-DOOR_ANGLE * s.open));
                pose.translate(-hx, 0, -0.1F / 16);
                submit(s, pose, out, s.frontLight, q -> KubhalaModel.door(q, body, s.join));
            }
            pose.popPose();
        }
        pose.popPose();
    }

    // an insert not yet screwed in sticks out a little, askew
    private static void loose(PoseStack pose) {
        pose.translate(0.5F, 0.5F, -1.5F / 16);
        rotate(pose, Axis.YP.rotationDegrees(5F));
        rotate(pose, Axis.ZP.rotationDegrees(-2F));
        pose.translate(-0.5F, -0.5F, 0);
    }

    // up to three stacks stood on the floor of the cube, facing out
    private void submitItems(State s, PoseStack pose, SubmitNodeCollector out) {
        int n = s.shown.size();
        if (n == 0) return;
        float[] xs = n == 1 ? new float[]{8} : n == 2 ? new float[]{5.25F, 10.75F} : new float[]{4, 8, 12};
        float scale = n == 1 ? 0.6F : n == 2 ? 0.42F : 0.34F;
        float floor = s.join.y0() / 16F;
        for (int i = 0; i < n; i++) {
            ItemStackRenderState r = s.shown.get(i);
            float sc = s.blocks.get(i) ? scale * 1.5F : scale;
            AABB box = r.getModelBoundingBox();
            pose.pushPose();
            pose.translate(xs[i] / 16F, floor, 0.5F);
            rotate(pose, Axis.YP.rotationDegrees(180F));
            pose.scale(sc, sc, sc);
            pose.translate(0, -box.minY, 0);
            r.submit(pose, out, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    // up to three stacks lying in the front of the drawer box
    private void submitDrawerItems(State s, PoseStack pose, SubmitNodeCollector out) {
        int n = s.shown.size();
        if (n == 0) return;
        float[] xs = n == 1 ? new float[]{8} : n == 2 ? new float[]{6, 10} : new float[]{5, 8, 11};
        float scale = n == 1 ? 0.42F : n == 2 ? 0.32F : 0.26F;
        float floor = (s.join.y0() + KubhalaModel.GAP + 0.75F) / 16F;
        for (int i = 0; i < n; i++) {
            ItemStackRenderState r = s.shown.get(i);
            float sc = s.blocks.get(i) ? scale * 1.4F : scale;
            AABB box = r.getModelBoundingBox();
            pose.pushPose();
            pose.translate(xs[i] / 16F, floor + 0.01F / 16, (4.5F + (i % 2) * 0.75F) / 16F);
            rotate(pose, Axis.YP.rotationDegrees(180F + (i - 1) * 9F));
            rotate(pose, Axis.XP.rotationDegrees(-90F));
            pose.scale(sc, sc, sc);
            pose.translate(0, 0, -box.minZ);
            r.submit(pose, out, s.frontLight, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    private static void rotate(PoseStack pose, Quaternionf q) {
        //? if >=26.3 {
        /*pose.rotate(q);
        *///?} else
        pose.mulPose(q);
    }
}
//?}
