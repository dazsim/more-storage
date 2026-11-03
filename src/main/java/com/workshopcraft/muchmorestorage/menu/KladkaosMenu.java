package com.workshopcraft.muchmorestorage.menu;

import com.workshopcraft.muchmorestorage.block.KladkaosBlockEntity;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// the 54 slots of a chest plus the four rail hooks down the left side
public class KladkaosMenu extends ChestMenu {
    public static final int RAIL_X = -20, RAIL_Y = 18;
    private static final Identifier[] ICONS = {InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS};
    private final int railStart;

    public KladkaosMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(KladkaosBlockEntity.SIZE), new SimpleContainer(KladkaosBlockEntity.RAIL.length));
    }

    public KladkaosMenu(int id, Inventory inv, Container box, Container rail) {
        super(ModContent.KLADKAOS_MENU, id, inv, box, 6);
        railStart = slots.size();
        Player player = inv.player;
        for (int i = 0; i < KladkaosBlockEntity.RAIL.length; i++) {
            final int hook = i;
            addSlot(new Slot(rail, i, RAIL_X, RAIL_Y + i * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) { return KladkaosBlockEntity.railSlot(player, stack) == hook; }

                @Override
                public int getMaxStackSize() { return 1; }

                //? if >=26.1 {
                @Override
                public Identifier getNoItemIcon() { return ICONS[hook]; }
                //?} else {
                /*@Override
                public com.mojang.datafixers.util.Pair<Identifier, Identifier> getNoItemIcon() {
                    return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, ICONS[hook]);
                }
                *///?}
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= railStart) {
            Slot slot = slots.get(index);
            if (!slot.hasItem()) return ItemStack.EMPTY;
            ItemStack stack = slot.getItem();
            ItemStack copy = stack.copy();
            if (!moveItemStackTo(stack, 54, railStart, true)) return ItemStack.EMPTY;
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            return copy;
        }
        if (index >= 54) {
            Slot slot = slots.get(index);
            ItemStack stack = slot.getItem();
            int hook = KladkaosBlockEntity.railSlot(player, stack);
            if (hook >= 0 && !slots.get(railStart + hook).hasItem()) {
                ItemStack copy = stack.copy();
                if (moveItemStackTo(stack, railStart + hook, railStart + hook + 1, false)) {
                    if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
                    else slot.setChanged();
                    return copy;
                }
            }
        }
        return super.quickMoveStack(player, index);
    }
}
