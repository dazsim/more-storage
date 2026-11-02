package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.BokwurmBlockEntity;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.FurnitureModel;
import com.workshopcraft.muchmorestorage.client.KubhalaModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

// pre 26.1 form of client.BokwurmRenderer
public class BokwurmRenderer implements BlockEntityRenderer<BokwurmBlockEntity> {
    private final int[] colours = new int[BokwurmBlockEntity.SLOTS];
    private final float[] heights = new float[BokwurmBlockEntity.SLOTS];

    public BokwurmRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 96; }

    @Override
    public void render(BokwurmBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof FurnitureBlock)) return;
        Direction facing = state.getValue(FurnitureBlock.FACING);
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        CupboardModel.Body body = CupboardModel.Body.of(be.body());
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        Quads q = new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY);
        if (!state.getValue(FurnitureBlock.ASSEMBLED)) {
            KubhalaModel.flatpack(q, body);
        } else {
            FurnitureModel.bokwurm(q, body);
            FurnitureModel.fillBooks(be, colours, heights);
            var sp = FurnitureModel.book();
            for (int i = 0; i < heights.length; i++) {
                if (heights[i] > 0) FurnitureModel.book(q, sp, i, colours[i], heights[i]);
            }
        }
        pose.popPose();
    }
}
*///?}
