package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.workshopcraft.muchmorestorage.entity.HjulborgEntity;
//? if >=26.3 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
*///?} else
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class HjulborgRenderer extends EntityRenderer<HjulborgEntity, HjulborgRenderer.State> {
    //? if >=26.3
    /*private static final RenderType CUTOUT_BLOCK_SHEET = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);*/

    public static class State extends EntityRenderState {
        String body = "oak";
        float yaw, wobble;
    }

    public HjulborgRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0.45F;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(HjulborgEntity e, State s, float partialTick) {
        super.extractRenderState(e, s, partialTick);
        s.body = e.body();
        s.yaw = Mth.rotLerp(partialTick, e.yRotO, e.getYRot());
        s.wobble = Math.max(0F, e.wobble() - partialTick);
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        CupboardModel.Body body = CupboardModel.Body.of(s.body);
        int light = s.lightCoords;
        float lift = s.wobble > 0 ? (int) s.wobble % 2 * 0.4F : 0F;
        pose.pushPose();
        KubhalaRenderer.rotate(pose, Axis.YP.rotationDegrees(-s.yaw));
        if (s.wobble > 0) KubhalaRenderer.rotate(pose, Axis.ZP.rotationDegrees(Mth.sin(s.wobble * 1.5F) * s.wobble * 0.6F));
        pose.translate(-0.5F, 0, -0.5F);
        //? if >=26.3 {
        /*out.submitCustomGeometry(pose, CUTOUT_BLOCK_SHEET, (p, vc) -> FurnitureModel.hjulborg(new Quads(p, vc, light, OverlayTexture.NO_OVERLAY), body, lift));
        *///?} else
        out.submitCustomGeometry(pose, Sheets.cutoutBlockSheet(), (p, vc) -> FurnitureModel.hjulborg(new Quads(p, vc, light, OverlayTexture.NO_OVERLAY), body, lift));
        pose.popPose();
        super.submit(s, pose, out, camera);
    }
}
//?}
