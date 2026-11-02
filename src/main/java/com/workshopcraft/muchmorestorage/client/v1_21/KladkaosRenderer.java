package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.KladkaosBlock;
import com.workshopcraft.muchmorestorage.block.KladkaosBlockEntity;
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

// pre 26.1 form of client.KladkaosRenderer
public class KladkaosRenderer implements BlockEntityRenderer<KladkaosBlockEntity> {
    private static final float[] RAIL_Y = {25, 20, 14.5F, 9.5F};

    public KladkaosRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public boolean shouldRenderOffScreen(KladkaosBlockEntity be) { return true; }

    @Override
    public void render(KladkaosBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof KladkaosBlock) || KladkaosBlock.upper(state)) return;
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
        FurnitureModel.kladkaos(quads(pose, vc, light), body);
        float o = 1F - be.getOpenNess(partialTick);
        float open = 1F - o * o * o;
        for (boolean left : new boolean[]{false, true}) {
            float hinge = FurnitureModel.kladkaosHinge(left) / 16F;
            pose.pushPose();
            pose.translate(hinge, 0, 0);
            pose.mulPose(Axis.YP.rotationDegrees((left ? -100F : 100F) * open));
            pose.translate(-hinge, 0, 0);
            FurnitureModel.kladkaosDoor(quads(pose, vc, frontLight), body, left);
            pose.popPose();
        }
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; open > 0.05F && i < RAIL_Y.length; i++) {
            ItemStack stack = be.armour(i);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5F, RAIL_Y[i] / 16F, 0.5F);
            pose.mulPose(Axis.YP.rotationDegrees(180F));
            pose.scale(0.4F, 0.4F, 0.4F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, frontLight, OverlayTexture.NO_OVERLAY,
                    pose, buffers, level, seed + i);
            pose.popPose();
        }
        pose.popPose();
    }

    private static Quads quads(PoseStack pose, VertexConsumer vc, int light) {
        return new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY);
    }
}
*///?}
