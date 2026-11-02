package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.Insert;
import com.workshopcraft.muchmorestorage.block.KubhalaBlock;
import com.workshopcraft.muchmorestorage.block.KubhalaBlockEntity;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.KubhalaModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import com.workshopcraft.muchmorestorage.item.SakksakkItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

// pre 26.1 form of client.KubhalaRenderer
public class KubhalaRenderer implements BlockEntityRenderer<KubhalaBlockEntity> {
    private static final float DOOR_ANGLE = 100F;

    public KubhalaRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public void render(KubhalaBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof KubhalaBlock)) return;
        Direction facing = state.getValue(KubhalaBlock.FACING);
        boolean assembled = state.getValue(KubhalaBlock.ASSEMBLED);
        Insert insert = state.getValue(KubhalaBlock.INSERT);
        Level level = be.getLevel();
        int frontLight = level == null ? light : LevelRenderer.getLightColor(level, be.getBlockPos().relative(facing));
        float o = 1F - be.getOpenNess(partialTick);
        float open = 1F - o * o * o;
        KubhalaModel.Join join = level == null || !assembled ? KubhalaModel.Join.NONE : KubhalaModel.Join.of(level, be.getBlockPos(), state);
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        CupboardModel.Body body = CupboardModel.Body.of(be.body());
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        if (!assembled) {
            KubhalaModel.flatpack(quads(pose, vc, light), body);
            pose.popPose();
            return;
        }
        KubhalaModel.frame(quads(pose, vc, light), body, join);
        if (insert == Insert.NONE || insert == Insert.DOOR && open > 0.05F) items(be, join, pose, buffers, light, false);
        if (insert == Insert.BOX) {
            pose.pushPose();
            pose.translate(0, 0, -3 / 16F * open);
            KubhalaModel.sakksakk(quads(pose, vc, frontLight), CupboardModel.sprite(SakksakkItem.texture(be.box())), join);
            pose.popPose();
        }
        if (insert.closes()) {
            pose.pushPose();
            if (!state.getValue(KubhalaBlock.FITTED)) {
                pose.translate(0.5F, 0.5F, -1.5F / 16);
                pose.mulPose(Axis.YP.rotationDegrees(5F));
                pose.mulPose(Axis.ZP.rotationDegrees(-2F));
                pose.translate(-0.5F, -0.5F, 0);
            }
            if (insert == Insert.DRAWER) {
                pose.translate(0, 0, -9 / 16F * open);
                KubhalaModel.drawer(quads(pose, vc, frontLight), body, join);
                if (open > 0.05F) items(be, join, pose, buffers, frontLight, true);
            } else {
                float hx = KubhalaModel.doorHinge(join);
                pose.translate(hx, 0, 0.1F / 16);
                pose.mulPose(Axis.YP.rotationDegrees(-DOOR_ANGLE * open));
                pose.translate(-hx, 0, -0.1F / 16);
                KubhalaModel.door(quads(pose, vc, frontLight), body, join);
            }
            pose.popPose();
        }
        pose.popPose();
    }

    private static Quads quads(PoseStack pose, VertexConsumer vc, int light) {
        return new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY);
    }

    // up to three stacks stood on the floor of the cube facing out, or lying in the drawer
    private static void items(KubhalaBlockEntity be, KubhalaModel.Join join, PoseStack pose, MultiBufferSource buffers, int light, boolean drawer) {
        List<ItemStack> shown = new ArrayList<>();
        for (int i = 0; i < be.getContainerSize() && shown.size() < 3; i++) {
            if (!be.getItem(i).isEmpty()) shown.add(be.getItem(i));
        }
        int n = shown.size();
        if (n == 0) return;
        float[] xs = n == 1 ? new float[]{8} : n == 2 ? new float[]{5.25F, 10.75F} : new float[]{4, 8, 12};
        float scale = n == 1 ? 0.6F : n == 2 ? 0.42F : 0.34F;
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; i < n; i++) {
            ItemStack stack = shown.get(i);
            boolean block = Minecraft.getInstance().getItemRenderer().getModel(stack, be.getLevel(), null, seed + i).isGui3d();
            float sc = block ? scale * 1.5F : scale;
            pose.pushPose();
            if (drawer) {
                sc *= 0.7F;
                float x = n == 1 ? 8 : n == 2 ? 2 + 4 * (i + 1) : 2 + 3 * (i + 1);
                pose.translate(x / 16F, (join.y0() + KubhalaModel.GAP + 0.76F) / 16F, (4.5F + (i % 2) * 0.75F) / 16F);
                pose.mulPose(Axis.YP.rotationDegrees(180F + (i - 1) * 9F));
                pose.mulPose(Axis.XP.rotationDegrees(-90F));
                pose.scale(sc, sc, sc);
                pose.translate(0, 0, block ? 0.25F : 1 / 32F);
            } else {
                pose.translate(xs[i] / 16F, join.y0() / 16F + (block ? 0.25F : 0.5F) * sc, 0.5F);
                pose.mulPose(Axis.YP.rotationDegrees(180F));
                pose.scale(sc, sc, sc);
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), seed + i);
            pose.popPose();
        }
    }
}
*///?}
