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
import com.workshopcraft.muchmorestorage.block.Insert;
import net.minecraft.world.item.Item;

// a drawer or door for a KUBHÅLA cube
public class InsertItem extends Item {
    public final Insert kind;

    public InsertItem(Insert kind, Properties props) {
        super(props);
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

    private static void tips(ItemStack stack, Consumer<Component> lines) {
        lines.accept(Component.translatable("tooltip.muchmorestorage.insert").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage.insert.out").withStyle(ChatFormatting.GRAY));
    }
}
