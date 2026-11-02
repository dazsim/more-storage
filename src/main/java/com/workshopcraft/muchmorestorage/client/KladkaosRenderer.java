package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.KladkaosBlock;
import com.workshopcraft.muchmorestorage.block.KladkaosBlockEntity;
//? if >=26.3 {
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
import net.minecraft.world.phys.Vec3;

public class KladkaosRenderer implements BlockEntityRenderer<KladkaosBlockEntity, KladkaosRenderer.State> {
    // centre height of each rail piece, a rough outline of whoever wears it
    static final float[] RAIL_Y = {25, 20, 14.5F, 9.5F};
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        String body = "oak";
        boolean assembled, lower;
        int frontLight;
        float open;
        final ItemStackRenderState[] rail = new ItemStackRenderState[KladkaosBlockEntity.RAIL.length];
        final boolean[] shown = new boolean[KladkaosBlockEntity.RAIL.length];
    }

    public KladkaosRenderer(BlockEntityRendererProvider.Context ctx) {
        items = ctx.itemModelResolver();
    }

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public State createRenderState() {
        State s = new State();
        for (int i = 0; i < s.rail.length; i++) s.rail[i] = new ItemStackRenderState();
        return s;
    }

    @Override
    public void extractRenderState(KladkaosBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        var state = be.getBlockState();
        s.lower = state.getBlock() instanceof KladkaosBlock && !KladkaosBlock.upper(state);
        if (!s.lower) return;
        s.facing = state.getValue(FurnitureBlock.FACING);
        s.body = be.body();
        s.assembled = state.getValue(FurnitureBlock.ASSEMBLED);
        //? if >=26.3 {
        /*s.frontLight = be.getLevel() == null ? s.lightCoords : LightCoordsUtil.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        *///?} else
        s.frontLight = be.getLevel() == null ? s.lightCoords : LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
        float o = 1F - be.getOpenNess(partialTick);
        s.open = 1F - o * o * o;
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; i < s.rail.length; i++) {
            ItemStack stack = be.armour(i);
            s.shown[i] = s.assembled && s.open > 0.05F && !stack.isEmpty();
            if (s.shown[i]) items.updateForTopItem(s.rail[i], stack, ItemDisplayContext.FIXED, be.getLevel(), null, seed + i);
        }
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        if (!s.lower) return;
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
        KubhalaRenderer.submit(s, pose, out, s.lightCoords, q -> FurnitureModel.kladkaos(q, body));
        for (boolean left : new boolean[]{false, true}) {
            float hinge = FurnitureModel.kladkaosHinge(left) / 16F;
            pose.pushPose();
            pose.translate(hinge, 0, 0);
            KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees((left ? -100F : 100F) * s.open));
            pose.translate(-hinge, 0, 0);
            KubhalaRenderer.submit(s, pose, out, s.frontLight, q -> FurnitureModel.kladkaosDoor(q, body, left));
            pose.popPose();
        }
        for (int i = 0; i < s.rail.length; i++) {
            if (!s.shown[i]) continue;
            pose.pushPose();
            pose.translate(0.5F, RAIL_Y[i] / 16F, 0.5F);
            KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees(180F));
            pose.scale(0.4F, 0.4F, 0.4F);
            s.rail[i].submit(pose, out, s.frontLight, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        pose.popPose();
    }
}
//?}
