package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.LadstapelBlockEntity;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.FurnitureModel;
import com.workshopcraft.muchmorestorage.client.KubhalaModel;
import com.workshopcraft.muchmorestorage.client.Quads;
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

// pre 26.1 form of client.LadstapelRenderer
public class LadstapelRenderer implements BlockEntityRenderer<LadstapelBlockEntity> {
    public LadstapelRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public void render(LadstapelBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof FurnitureBlock)) return;
        Direction facing = state.getValue(FurnitureBlock.FACING);
        Level level = be.getLevel();
        int frontLight = level == null ? light : LevelRenderer.getLightColor(level, be.getBlockPos().relative(facing));
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        CupboardModel.Body body = CupboardModel.Body.of(be.body());
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        if (!state.getValue(FurnitureBlock.ASSEMBLED)) {
            KubhalaModel.flatpack(quads(pose, vc, light), body);
            pose.popPose();
            return;
        }
        FurnitureModel.ladstapel(quads(pose, vc, light), body);
        for (int d = 0; d < LadstapelBlockEntity.DRAWERS; d++) {
            float o = 1F - be.drawerOpenness(d, partialTick);
            float open = 1F - o * o * o;
            pose.pushPose();
            pose.translate(0, 0, -FurnitureModel.DRAWER_OUT / 16F * open);
            FurnitureModel.drawer(quads(pose, vc, frontLight), body, d);
            if (open > 0.05F) items(be, d, pose, buffers, frontLight);
            pose.popPose();
        }
        pose.popPose();
    }

    private static Quads quads(PoseStack pose, VertexConsumer vc, int light) {
        return new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY);
    }

    // up to three stacks lying back in the open drawer so they show from a standing view
    private static void items(LadstapelBlockEntity be, int d, PoseStack pose, MultiBufferSource buffers, int light) {
        var drawer = be.drawer(d);
        List<ItemStack> shown = new ArrayList<>();
        for (int i = 0; i < drawer.getContainerSize() && shown.size() < 3; i++) {
            if (!drawer.getItem(i).isEmpty()) shown.add(drawer.getItem(i));
        }
        int n = shown.size();
        if (n == 0) return;
        float[] xs = n == 1 ? new float[]{8} : n == 2 ? new float[]{5.75F, 10.25F} : new float[]{4.5F, 8, 11.5F};
        float scale = n == 1 ? 0.4F : 0.32F;
        int seed = (int) be.getBlockPos().asLong() + d * 9;
        for (int i = 0; i < n; i++) {
            ItemStack stack = shown.get(i);
            boolean block = Minecraft.getInstance().getItemRenderer().getModel(stack, be.getLevel(), null, seed + i).isGui3d();
            float sc = block ? scale * 1.2F : scale;
            pose.pushPose();
            pose.translate(xs[i] / 16F, FurnitureModel.drawerFloor(d) / 16F, (5.5F + (i % 2)) / 16F);
            pose.mulPose(Axis.YP.rotationDegrees(180F + (i - 1) * 6F));
            pose.mulPose(Axis.XP.rotationDegrees(-60F));
            pose.scale(sc, sc, sc);
            pose.translate(0, block ? 0.25F : 0.5F, 0);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), seed + i);
            pose.popPose();
        }
    }
}
*///?}
