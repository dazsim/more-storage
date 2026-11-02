package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.CrateBlock;
import com.workshopcraft.muchmorestorage.block.CrateBlockEntity;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.FurnitureModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;

// pre 26.1 form of client.CrateRenderer
public class CrateRenderer implements BlockEntityRenderer<CrateBlockEntity> {
    public CrateRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CrateBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof CrateBlock)) return;
        int step = state.getValue(CrateBlock.STEP);
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - state.getValue(FurnitureBlock.FACING).toYRot()));
        if (step > 0 && step < CrateBlock.TURNS && be.getLevel() != null) {
            float t = be.getLevel().getGameTime() + partialTick;
            pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(t * 0.5F) * 2.5F));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.sin(t * 0.37F + 1F) * 1.5F));
        }
        pose.translate(-0.5F, 0, -0.5F);
        FurnitureModel.crate(new Quads(pose.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), light, OverlayTexture.NO_OVERLAY),
                CupboardModel.Body.of(be.body()), step);
        pose.popPose();
    }
}
*///?}
