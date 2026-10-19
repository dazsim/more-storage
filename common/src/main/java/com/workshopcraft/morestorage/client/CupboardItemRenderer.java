package com.workshopcraft.morestorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.workshopcraft.morestorage.item.CarcassItem;
import com.workshopcraft.morestorage.item.WorktopItem;
import com.workshopcraft.morestorage.registry.Finishes;
import com.workshopcraft.morestorage.registry.ModContent;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

// carcasses, worktops and cupboards all draw their finish from the stack
public class CupboardItemRenderer implements SpecialModelRenderer<CupboardItemRenderer.Look> {
    public record Look(String body, String top) {}

    @Override
    public @Nullable Look extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof CarcassItem carcass) return new Look(carcass.body, "");
        if (stack.getItem() instanceof WorktopItem worktop) return new Look("", worktop.top);
        return new Look(stack.getOrDefault(ModContent.BODY, "oak"), stack.getOrDefault(ModContent.TOP, "oak"));
    }

    @Override
    public void submit(@Nullable Look look, PoseStack pose, SubmitNodeCollector out, int light, int overlay, boolean foil, int outline) {
        if (look == null) return;
        out.submitCustomGeometry(pose, Sheets.cutoutBlockItemSheet(), (p, vc) -> {
            Quads q = new Quads(p, vc, light, overlay);
            if (look.body().isEmpty()) {
                PoseStack ps = new PoseStack();
                ps.last().set(p);
                ps.translate(0, -7 / 16F, 0);
                CupboardModel.worktop(new Quads(ps.last(), vc, light, overlay), CupboardModel.sprite(Finishes.topSprite(look.top())));
                return;
            }
            CupboardModel.Body body = CupboardModel.Body.of(look.body());
            CupboardModel.carcass(q, body);
            CupboardModel.door(q, body, false);
            if (!look.top().isEmpty()) CupboardModel.worktop(q, CupboardModel.sprite(Finishes.topSprite(look.top())));
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Look> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Look> bake(SpecialModelRenderer.BakingContext ctx) { return new CupboardItemRenderer(); }

        @Override
        public MapCodec<Unbaked> type() { return MAP_CODEC; }
    }
}
