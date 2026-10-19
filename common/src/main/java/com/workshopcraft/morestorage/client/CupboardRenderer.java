package com.workshopcraft.morestorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.workshopcraft.morestorage.MoreStorage;
import com.workshopcraft.morestorage.block.CupboardBlock;
import com.workshopcraft.morestorage.block.CupboardBlockEntity;
import com.workshopcraft.morestorage.registry.Finish;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

// the carcass is a normal block model, only the door is drawn here so it can swing
public class CupboardRenderer implements BlockEntityRenderer<CupboardBlockEntity, CupboardRenderer.State> {
    private static final float MAX_ANGLE = 100F;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        Finish finish = Finish.OAK;
        boolean rightHinge;
        float open;
        int frontLight;
    }

    public CupboardRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(CupboardBlockEntity be, State s, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camera, breaking);
        s.facing = be.getBlockState().getValue(CupboardBlock.FACING);
        if (be.getBlockState().getBlock() instanceof CupboardBlock block) s.finish = block.finish;
        s.rightHinge = be.getBlockState().getValue(CupboardBlock.HINGE) == DoorHingeSide.RIGHT;
        float o = 1F - be.getOpenNess(partialTick);
        s.open = 1F - o * o * o;
        s.frontLight = be.getLevel() == null ? s.lightCoords : LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(s.facing));
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector out, CameraRenderState camera) {
        TextureAtlasSprite door = sprite(s.finish.panels + "_cupboard_door");
        TextureAtlasSprite side = sprite(s.finish.panels + "_cupboard_side");
        TextureAtlasSprite handle = sprite("cupboard_handle");
        float hingeX = s.rightHinge ? 0F : 1F;
        float handleX = s.rightHinge ? 14F : 1F;
        pose.pushPose();
        pose.translate(0.5F, 0, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180F - s.facing.toYRot()));
        pose.translate(-0.5F, 0, -0.5F);
        pose.translate(hingeX, 0, 1 / 16F);
        pose.mulPose(Axis.YP.rotationDegrees((s.rightHinge ? MAX_ANGLE : -MAX_ANGLE) * s.open));
        pose.translate(-hingeX, 0, -1 / 16F);
        int light = s.frontLight;
        out.submitCustomGeometry(pose, Sheets.cutoutBlockSheet(), (p, vc) -> drawDoor(new Box(p, vc, light), door, side, handle, handleX));
        if (s.breakProgress != null) {
            ModelFeatureRenderer.CrumblingOverlay crack = s.breakProgress;
            out.submitCustomGeometry(pose, ModelBakery.DESTROY_TYPES.get(crack.progress()), (p, vc) ->
                    drawDoor(new Box(p, new SheetedDecalTextureGenerator(vc, crack.cameraPose(), 1F), light), door, side, handle, handleX));
        }
        pose.popPose();
    }

    private static void drawDoor(Box d, TextureAtlasSprite door, TextureAtlasSprite side, TextureAtlasSprite handle, float handleX) {
        d.face(Direction.NORTH, 0, 2, 1, 16, 14, 2, door, 0, 2, 16, 14);
        d.face(Direction.SOUTH, 0, 2, 1, 16, 14, 2, side, 0, 2, 16, 14);
        d.face(Direction.UP, 0, 2, 1, 16, 14, 2, side, 0, 0, 16, 1);
        d.face(Direction.DOWN, 0, 2, 1, 16, 14, 2, side, 0, 15, 16, 16);
        d.face(Direction.WEST, 0, 2, 1, 16, 14, 2, side, 0, 2, 1, 14);
        d.face(Direction.EAST, 0, 2, 1, 16, 14, 2, side, 15, 2, 16, 14);
        for (Direction dir : Direction.values()) {
            if (dir != Direction.SOUTH) d.face(dir, handleX, 6, 0, handleX + 1, 11, 1, handle, 7, 5, 8, 10);
        }
    }

    private static TextureAtlasSprite sprite(String name) {
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, MoreStorage.id("block/" + name)));
    }

    // cuboid faces in pixel units, uv given as seen from outside the face
    private record Box(PoseStack.Pose pose, VertexConsumer vc, int light) {
        void face(Direction dir, float x0, float y0, float z0, float x1, float y1, float z1,
                  TextureAtlasSprite sp, float u0, float v0, float u1, float v1) {
            x0 /= 16; y0 /= 16; z0 /= 16; x1 /= 16; y1 /= 16; z1 /= 16;
            float[][] c = switch (dir) {
                case NORTH -> new float[][]{{x1, y1, z0}, {x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}};
                case SOUTH -> new float[][]{{x0, y1, z1}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}};
                case WEST -> new float[][]{{x0, y1, z0}, {x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}};
                case EAST -> new float[][]{{x1, y1, z1}, {x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}};
                case UP -> new float[][]{{x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}};
                case DOWN -> new float[][]{{x0, y0, z1}, {x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}};
            };
            float[][] uv = {{u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}};
            Vector3f n = pose.transformNormal(dir.getUnitVec3f(), new Vector3f());
            Matrix4f m = pose.pose();
            Vector3f pos = new Vector3f();
            for (int i = 0; i < 4; i++) {
                m.transformPosition(c[i][0], c[i][1], c[i][2], pos);
                vc.addVertex(pos.x(), pos.y(), pos.z(), 0xFFFFFFFF, sp.getU(uv[i][0] / 16F), sp.getV(uv[i][1] / 16F),
                        OverlayTexture.NO_OVERLAY, light, n.x(), n.y(), n.z());
            }
        }
    }
}
