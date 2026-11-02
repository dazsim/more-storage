package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.CupboardBlock;
import com.workshopcraft.muchmorestorage.block.CupboardBlockEntity;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;

// pre 26.1 form of client.CupboardRenderer, drawing straight into the buffer
public class CupboardRenderer implements BlockEntityRenderer<CupboardBlockEntity> {
    private static final float MAX_ANGLE = 100F;

    public CupboardRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public void render(CupboardBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof CupboardBlock)) return;
        Direction facing = state.getValue(CupboardBlock.FACING);
        boolean rightHinge = state.getValue(CupboardBlock.HINGE) == DoorHingeSide.RIGHT;
        Level level = be.getLevel();
        int frontLight = level == null ? light : LevelRenderer.getLightColor(level, be.getBlockPos().relative(facing));
        int topLight = level == null ? light : LevelRenderer.getLightColor(level, be.getBlockPos().above());
        float o = 1F - be.getOpenNess(partialTick);
        float open = 1F - o * o * o;
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        CupboardModel.Body body = CupboardModel.Body.of(be.body());
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        CupboardModel.carcass(quads(pose, vc, light), body);
        if (be.hasTop()) worktop(be, state, partialTick, pose, vc, topLight);
        float hingeX = rightHinge ? 0F : 1F;
        pose.translate(hingeX, 0, 1 / 16F);
        pose.mulPose(Axis.YP.rotationDegrees((rightHinge ? MAX_ANGLE : -MAX_ANGLE) * open));
        pose.translate(-hingeX, 0, -1 / 16F);
        CupboardModel.door(quads(pose, vc, frontLight), body, rightHinge);
        pose.popPose();
    }

    private static Quads quads(PoseStack pose, VertexConsumer vc, int light) {
        return new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY);
    }

    // a loose worktop sits a little high and askew, then drops flat while it is screwed down
    private static void worktop(CupboardBlockEntity be, BlockState state, float partialTick, PoseStack pose, VertexConsumer vc, int light) {
        TextureAtlasSprite top = CupboardModel.top(be.top());
        float settle = be.settleProgress(partialTick);
        float k = state.getValue(CupboardBlock.ASSEMBLED) ? 0F : 1F - settle * settle;
        pose.pushPose();
        pose.translate(0.5F + 0.6F / 16 * k, 15 / 16F + 1.5F / 16 * k, 0.5F + 0.4F / 16 * k);
        pose.mulPose(Axis.YP.rotationDegrees(4F * k));
        pose.mulPose(Axis.ZP.rotationDegrees(-2F * k));
        pose.mulPose(Axis.XP.rotationDegrees(1F * k));
        pose.translate(-0.5F, -15 / 16F, -0.5F);
        CupboardModel.worktop(quads(pose, vc, light), top);
        pose.popPose();
    }
}
*///?}
