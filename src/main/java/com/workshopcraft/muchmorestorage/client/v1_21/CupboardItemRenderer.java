package com.workshopcraft.muchmorestorage.client.v1_21;

//? if <26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.workshopcraft.muchmorestorage.client.CupboardModel;
import com.workshopcraft.muchmorestorage.client.Quads;
import com.workshopcraft.muchmorestorage.item.CarcassItem;
import com.workshopcraft.muchmorestorage.item.WorktopItem;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// pre 26.1 form of client.CupboardItemRenderer, for the builtin/entity item models
public class CupboardItemRenderer extends BlockEntityWithoutLevelRenderer {
    public CupboardItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static List<Item> items() {
        List<Item> out = new ArrayList<>(ModContent.CARCASSES.values());
        out.addAll(ModContent.WORKTOPS.values());
        out.add(ModContent.CUPBOARD_ITEM);
        return out;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        draw(stack, pose, buffers, light, overlay);
    }

    // carcasses, worktops and cupboards all draw their finish from the stack
    public static void draw(ItemStack stack, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer vc = buffers.getBuffer(Sheets.cutoutBlockSheet());
        if (stack.getItem() instanceof WorktopItem worktop) {
            pose.pushPose();
            pose.translate(0, -7 / 16F, 0);
            CupboardModel.worktop(new Quads(pose.last(), vc, light, overlay), CupboardModel.top(worktop.top));
            pose.popPose();
            return;
        }
        Quads q = new Quads(pose.last(), vc, light, overlay);
        boolean carcass = stack.getItem() instanceof CarcassItem;
        String body = carcass ? ((CarcassItem) stack.getItem()).body : ModContent.bodyOf(stack);
        CupboardModel.Body b = CupboardModel.Body.of(body);
        CupboardModel.carcass(q, b);
        CupboardModel.door(q, b, false);
        String top = carcass ? "" : ModContent.cupboardTopOf(stack);
        if (!top.isEmpty()) CupboardModel.worktop(q, CupboardModel.top(top));
    }
}
*///?}
