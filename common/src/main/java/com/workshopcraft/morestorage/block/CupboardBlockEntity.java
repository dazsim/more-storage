package com.workshopcraft.morestorage.block;

import com.workshopcraft.morestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class CupboardBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private static final int EVENT_OPEN_COUNT = 1;
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ChestLidController door = new ChestLidController();
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) { sound(SoundEvents.BARREL_OPEN); }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) { sound(SoundEvents.BARREL_CLOSE); }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            level.blockEvent(pos, state.getBlock(), EVENT_OPEN_COUNT, current);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == CupboardBlockEntity.this;
        }
    };

    public CupboardBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.CUPBOARD_BE, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CupboardBlockEntity be) {
        be.door.tickLid();
    }

    private void sound(SoundEvent event) {
        if (level == null) return;
        BlockPos p = getBlockPos();
        level.playSound(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, event, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 1.0F);
    }

    @Override
    public int getContainerSize() { return 27; }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.morestorage.cupboard"); }

    @Override
    protected NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id == EVENT_OPEN_COUNT) {
            door.shouldBeOpen(value > 0);
            return true;
        }
        return super.triggerEvent(id, value);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!remove && !user.getLivingEntity().isSpectator()) {
            openers.incrementOpeners(user.getLivingEntity(), getLevel(), getBlockPos(), getBlockState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!remove && !user.getLivingEntity().isSpectator()) {
            openers.decrementOpeners(user.getLivingEntity(), getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return openers.getEntitiesWithContainerOpen(getLevel(), getBlockPos());
    }

    public void recheckOpen() {
        if (!remove) openers.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public float getOpenNess(float partialTick) { return door.getOpenness(partialTick); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }
}
