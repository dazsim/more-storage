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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

// a KUBHÅLA still in its flat pack, one item per finish
public class FlatpackItem extends BlockItem {
    public final String body;

    public FlatpackItem(Block block, String body, Properties props) {
        super(block, props);
        this.body = body;
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

    private static void tips(ItemStack stack, Consumer<Component> lines) {
        lines.accept(Component.translatable("tooltip.muchmorestorage.flatpack").withStyle(ChatFormatting.GRAY));
    }
    //? if <1.21.2 {

    /*// BlockItem would take the block's name
    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }
    *///?}
    //? if forge {

    /*@Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.workshopcraft.muchmorestorage.forge.MuchMoreStorageForgeClient.itemRenderer(consumer);
    }
    *///?}
}
