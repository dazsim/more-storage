package com.workshopcraft.muchmorestorage.client;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.item.CarcassItem;
import com.workshopcraft.muchmorestorage.item.FlatpackItem;
import com.workshopcraft.muchmorestorage.item.FurnitureItem;
import com.workshopcraft.muchmorestorage.item.KubhalaItem;
import com.workshopcraft.muchmorestorage.item.WorktopItem;
import com.workshopcraft.muchmorestorage.registry.ModContent;
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
    public record Look(String body, String top, int kind) {
        Look(String body, String top) { this(body, top, 0); }
    }

    @Override
    public @Nullable Look extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof CarcassItem carcass) return new Look(carcass.body, "");
        if (stack.getItem() instanceof WorktopItem worktop) return new Look("", worktop.top);
        if (stack.getItem() instanceof FlatpackItem pack) return new Look(pack.body, "", 1);
        if (stack.getItem() instanceof KubhalaItem) return new Look(ModContent.bodyOf(stack), "", 2);
        if (stack.getItem() == ModContent.BOKWURM_ITEM) return new Look(ModContent.bodyOf(stack), "", 3);
        if (stack.getItem() instanceof FurnitureItem) return new Look(ModContent.bodyOf(stack), "", 4);
        return new Look(ModContent.bodyOf(stack), ModContent.cupboardTopOf(stack));
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
                CupboardModel.worktop(new Quads(ps.last(), vc, light, overlay), CupboardModel.top(look.top()));
                return;
            }
            CupboardModel.Body body = CupboardModel.Body.of(look.body());
            if (look.kind() == 1) { KubhalaModel.flatpack(q, body); return; }
            if (look.kind() == 2) { KubhalaModel.frame(q, body, KubhalaModel.Join.NONE); return; }
            if (look.kind() == 3) { FurnitureModel.bokwurmItem(q, body); return; }
            if (look.kind() == 4) { FurnitureModel.ladstapelItem(q, body); return; }
            CupboardModel.carcass(q, body);
            CupboardModel.door(q, body, false);
            if (!look.top().isEmpty()) CupboardModel.worktop(q, CupboardModel.top(look.top()));
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
//?}
