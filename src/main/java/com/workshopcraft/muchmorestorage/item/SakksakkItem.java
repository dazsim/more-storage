package com.workshopcraft.muchmorestorage.item;

import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.menu.SakksakkMenu;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
/*import net.minecraft.world.InteractionResultHolder;*/
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5
import net.minecraft.world.item.component.TooltipDisplay;
//? if <1.20.5
/*import org.jspecify.annotations.Nullable;*/
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

// the SAKKSÄKK soft box, nine slots carried in the stack
public class SakksakkItem extends Item {
    public static final int SIZE = 9;
    public final String colour;

    public SakksakkItem(String colour, Properties props) {
        super(props);
        this.colour = colour;
    }

    // what a box slot takes, nothing that holds items of its own
    public static boolean fits(ItemStack stack) {
        return stack.isEmpty() || stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public boolean canFitInsideContainerItems() { return false; }

    public static Identifier texture(String colour) {
        return colour.equals("wicker") ? MuchMoreStorage.id("block/wicker_sakksakk") : MuchMoreStorage.id("minecraft", "block/" + colour + "_wool");
    }

    public static NonNullList<ItemStack> contents(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        //? if >=1.20.5 {
        ItemContainerContents c = stack.get(DataComponents.CONTAINER);
        if (c != null) c.copyInto(items);
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Items")) ContainerHelper.loadAllItems(tag, items);
        *///?}
        return items;
    }

    public static void setContents(ItemStack stack, List<ItemStack> items) {
        boolean empty = items.stream().allMatch(ItemStack::isEmpty);
        //? if >=1.20.5 {
        if (empty) stack.remove(DataComponents.CONTAINER);
        else stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (empty) {
            if (tag != null) {
                tag.remove("Items");
                if (tag.isEmpty()) stack.setTag(null);
            }
            return;
        }
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(SIZE, items.size()); i++) list.set(i, items.get(i));
        ContainerHelper.saveAllItems(stack.getOrCreateTag(), list, true);
        *///?}
    }

    public static ItemStack of(String colour, List<ItemStack> items) {
        SakksakkItem item = ModContent.SAKKSAKKS.get(colour);
        if (item == null) item = ModContent.SAKKSAKKS.get("white");
        ItemStack stack = new ItemStack(item);
        setContents(stack, items);
        return stack;
    }

    // a name given in an anvil, kept while the box sits in a cube
    public static @org.jspecify.annotations.Nullable Component customName(ItemStack stack) {
        //? if >=1.20.5 {
        return stack.get(DataComponents.CUSTOM_NAME);
        //?} else
        /*return stack.hasCustomHoverName() ? stack.getHoverName() : null;*/
    }

    public static void setCustomName(ItemStack stack, Component name) {
        //? if >=1.20.5 {
        stack.set(DataComponents.CUSTOM_NAME, name);
        //?} else
        /*stack.setHoverName(name);*/
    }

    private static void open(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (level.isClientSide()) return;
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> SakksakkMenu.held(id, inv, stack, hand), stack.getHoverName()));
        player.playSound(SoundEvents.WOOL_PLACE, 0.6F, 1.2F);
    }

    //? if >=1.21.2 {
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        open(level, player, hand, player.getItemInHand(hand));
        return InteractionResult.SUCCESS;
    }
    //?} else {
    /*@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        open(level, player, hand, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    *///?}

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
        long used = contents(stack).stream().filter(s -> !s.isEmpty()).count();
        lines.accept(Component.translatable("tooltip.muchmorestorage.sakksakk.used", used, SIZE).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage.sakksakk").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.muchmorestorage.sakksakk.cube").withStyle(ChatFormatting.GRAY));
    }
}
