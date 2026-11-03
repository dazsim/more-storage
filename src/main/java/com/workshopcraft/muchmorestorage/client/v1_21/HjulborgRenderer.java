package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.FurnitureModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import com.workshopcraft.muchmorestorage.entity.HjulborgEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

// pre 26.1 form of client.HjulborgRenderer
public class HjulborgRenderer extends EntityRenderer<HjulborgEntity> {
    public HjulborgRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0.45F;
    }

    @Override
    public void render(HjulborgEntity e, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float wobble = Math.max(0F, e.wobble() - partialTick);
        float lift = wobble > 0 ? (int) wobble % 2 * 0.4F : 0F;
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        if (wobble > 0) pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(wobble * 1.5F) * wobble * 0.6F));
        pose.translate(-0.5F, 0, -0.5F);
        CupboardModel.Body body = CupboardModel.Body.of(e.body());
        FurnitureModel.hjulborgBody(new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY), body);
        pose.translate(0, 10 / 16F, 1.5F / 16F);
        pose.mulPose(Axis.XP.rotationDegrees(-80F * e.lid(partialTick)));
        pose.translate(0, -10 / 16F, -1.5F / 16F);
        FurnitureModel.hjulborgLid(new Quads(pose.last(), vc, light, OverlayTexture.NO_OVERLAY), body, lift);
        pose.popPose();
        super.render(e, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public Identifier getTextureLocation(HjulborgEntity e) { return TextureAtlas.LOCATION_BLOCKS; }
}
*///?}
