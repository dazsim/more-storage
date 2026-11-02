package com.workshopcraft.muchmorestorage.item;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

//? if <1.21.5
/*import java.util.List;*/
import java.util.function.Consumer;

public class CupboardItem extends BlockItem {
    public CupboardItem(Block block, Properties props) {
        super(block, props);
    }

    public static Component finishName(String finish) {
        if (finish.contains(":")) return ModContent.worktop(finish).getHoverName();
        return Component.translatable("finish." + MuchMoreStorage.MOD_ID + "." + finish);
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        finishLines(stack, lines);
    }
    //?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        finishLines(stack, lines::add);
    }
    *///?}

    private static void finishLines(ItemStack stack, Consumer<Component> lines) {
        String body = stack.getOrDefault(ModContent.BODY, "oak");
        String top = stack.getOrDefault(ModContent.TOP, "oak");
        lines.accept(Component.translatable("tooltip.muchmorestorage.body", finishName(body)).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage.top", finishName(top)).withStyle(ChatFormatting.GRAY));
    }
}
