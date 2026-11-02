package com.workshopcraft.muchmorestorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;
//? if <1.21.5
/*import java.util.List;*/
//? if <1.20.5 {
/*import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
*///?}
import java.util.function.Consumer;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

// an assembled BOKWURM or LADSTAPEL, the finish rides on the stack
public class FurnitureItem extends BlockItem {
    private final String kind;

    public FurnitureItem(Block block, String kind, Properties props) {
        super(block, props);
        this.kind = kind;
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
        lines.accept(Component.translatable("tooltip.muchmorestorage." + kind).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage." + kind + ".use").withStyle(ChatFormatting.GRAY));
        if (kind.equals("bokwurm")) lines.accept(Component.translatable("tooltip.muchmorestorage.bokwurm.enchanting").withStyle(ChatFormatting.GRAY));
    }
    //? if forge {

    /*@Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.workshopcraft.muchmorestorage.forge.MuchMoreStorageForgeClient.itemRenderer(consumer);
    }
    *///?}
}
