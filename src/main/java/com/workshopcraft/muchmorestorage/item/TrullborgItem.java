package com.workshopcraft.muchmorestorage.item;

import com.workshopcraft.muchmorestorage.entity.TrullborgEntity;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;
//? if <1.21.5
/*import java.util.List;*/
//? if <1.20.5
/*import org.jspecify.annotations.Nullable;*/
import java.util.function.Consumer;

// places a TRULLBORG on top of a block, the finish rides on the stack
public class TrullborgItem extends Item {
    public TrullborgItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        Level level = ctx.getLevel();
        Vec3 at = ctx.getClickLocation();
        TrullborgEntity cart = new TrullborgEntity(ModContent.TRULLBORG, level);
        cart.setPos(at.x, at.y, at.z);
        if (!cart.fits()) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        cart.setYRot(player == null ? 0F : player.getYRot() + 180F);
        ItemStack stack = ctx.getItemInHand();
        cart.setBody(ModContent.bodyOf(stack));
        level.addFreshEntity(cart);
        if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        tips(stack, lines);
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        tips(stack, lines::add);
    }
    *///?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        tips(stack, lines::add);
    }
    *///?}

    private void tips(ItemStack stack, Consumer<Component> lines) {
        lines.accept(Component.translatable("tooltip.muchmorestorage.body", CupboardItem.finishName(ModContent.bodyOf(stack))).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage.trullborg").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage.trullborg.use").withStyle(ChatFormatting.GRAY));
    }
    //? if forge {

    /*@Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.workshopcraft.muchmorestorage.forge.MuchMoreStorageForgeClient.itemRenderer(consumer);
    }
    *///?}
}
