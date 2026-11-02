package com.workshopcraft.muchmorestorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
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

// a plain item with grey tooltip lines
public class TipItem extends Item {
    private final String[] keys;

    public TipItem(Properties props, String... keys) {
        super(props);
        this.keys = keys;
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        tips(lines);
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        tips(lines::add);
    }
    *///?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        tips(lines::add);
    }
    *///?}

    private void tips(Consumer<Component> lines) {
        for (String k : keys) lines.accept(Component.translatable("tooltip.muchmorestorage." + k).withStyle(ChatFormatting.GRAY));
    }
}
