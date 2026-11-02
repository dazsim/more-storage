package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.block.FurnitureBlock;
import com.workshopcraft.muchmorestorage.block.GenomskadaBlockEntity;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.FurnitureModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

// pre 26.1 form of client.GenomskadaRenderer
public class GenomskadaRenderer implements BlockEntityRenderer<GenomskadaBlockEntity> {
    public GenomskadaRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public int getViewDistance() { return 64; }

    @Override
    public void render(GenomskadaBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof FurnitureBlock)) return;
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - state.getValue(FurnitureBlock.FACING).toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        FurnitureModel.genomskadaLid(new Quads(pose.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), light, OverlayTexture.NO_OVERLAY),
                CupboardModel.top(GenomskadaBlockEntity.lid(be.body())));
        int seed = (int) be.getBlockPos().asLong();
        for (int i = 0; i < be.getContainerSize(); i++) {
            ItemStack stack = be.getItem(i);
            if (stack.isEmpty()) continue;
            boolean block = Minecraft.getInstance().getItemRenderer().getModel(stack, be.getLevel(), null, seed + i).isGui3d();
            float sc = block ? 0.22F : 0.26F;
            pose.pushPose();
            pose.translate((4 + (i % 3) * 4) / 16F, 0.6F / 16F, (4 + (i / 3) * 4) / 16F);
            pose.mulPose(Axis.XP.rotationDegrees(-90F));
            pose.scale(sc, sc, sc);
            pose.translate(0, 0, block ? 0.25F : 0.03F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, be.getLevel(), seed + i);
            pose.popPose();
        }
        FurnitureModel.genomskadaWalls(new Quads(pose.last(), buffers.getBuffer(Sheets.translucentCullBlockSheet()), light, OverlayTexture.NO_OVERLAY));
        pose.popPose();
    }
}
*///?}
