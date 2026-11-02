package com.workshopcraft.muchmorestorage.menu;

import com.workshopcraft.muchmorestorage.item.SakksakkItem;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
//? if >=26.1 {
import net.minecraft.world.inventory.ContainerInput;
//?} else
/*import net.minecraft.world.inventory.ClickType;*/
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

// a SAKKSÄKK opened from the hand or from a KUBHÅLA cube, the client sees a plain one row chest
public class SakksakkMenu extends AbstractContainerMenu {
    public final Container box;
    private final @Nullable ItemStack held;
    private final @Nullable InteractionHand hand;
    private final int locked;

    public SakksakkMenu(int id, Inventory inv, Container box) {
        this(id, inv, box, null, null, -1);
    }

    public static SakksakkMenu held(int id, Inventory inv, ItemStack stack, InteractionHand hand) {
        int slot = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i) == stack) slot = i;
        }
        return new SakksakkMenu(id, inv, new Held(stack), stack, hand, slot);
    }

    private SakksakkMenu(int id, Inventory inv, Container box, @Nullable ItemStack held, @Nullable InteractionHand hand, int locked) {
        super(MenuType.GENERIC_9x1, id);
        checkContainerSize(box, SakksakkItem.SIZE);
        this.box = box;
        this.held = held;
        this.hand = hand;
        this.locked = locked;
        box.startOpen(inv.player);
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(box, x, 8 + x * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) { return SakksakkItem.fits(stack); }
            });
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) addSlot(slot(inv, x + (y + 1) * 9, 8 + x * 18, 49 + y * 18));
        }
        for (int x = 0; x < 9; x++) addSlot(slot(inv, x, 8 + x * 18, 107));
    }

    // the slot holding the open box cannot be touched
    private Slot slot(Inventory inv, int index, int x, int y) {
        if (index != locked) return new Slot(inv, index, x, y);
        return new Slot(inv, index, x, y) {
            @Override
            public boolean mayPickup(Player player) { return false; }

            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        };
    }

    @Override
    //? if >=26.1 {
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (input == ContainerInput.SWAP && button == locked) return;
    //?} else {
    /*public void clicked(int slot, int button, ClickType input, Player player) {
        if (input == ClickType.SWAP && button == locked) return;
    *///?}
        super.clicked(slot, button, input, player);
    }

    @Override
    public boolean stillValid(Player player) {
        if (held != null) return !held.isEmpty() && player.getItemInHand(hand) == held;
        return box.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!SakksakkItem.fits(stack) || !moveItemStackTo(stack, 0, 9, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        box.stopOpen(player);
    }

    // the nine slots of a box in the hand, written back into the stack on every change
    private static class Held extends SimpleContainer {
        private final ItemStack stack;

        Held(ItemStack stack) {
            super(SakksakkItem.SIZE);
            this.stack = stack;
            var items = SakksakkItem.contents(stack);
            for (int i = 0; i < items.size(); i++) super.setItem(i, items.get(i));
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack s) { return SakksakkItem.fits(s); }

        @Override
        public void setChanged() {
            super.setChanged();
            if (stack.isEmpty()) return;
            java.util.List<ItemStack> list = new java.util.ArrayList<>();
            for (int i = 0; i < getContainerSize(); i++) list.add(getItem(i));
            SakksakkItem.setContents(stack, list);
        }
    }
}
