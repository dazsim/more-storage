package com.workshopcraft.muchmorestorage.block;

import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    private static final int EVENT_SETTLE = 2;
    public static final int SETTLE_TICKS = 6;
    public long settleEnd;
    private long settleStart = -1;
    private String body = "oak";
    private String top = "oak";
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

    public String body() { return body; }

    public String top() { return top; }

    public boolean hasTop() { return !top.isEmpty(); }

    public void setFinishes(String body, String top) {
        this.body = body;
        this.top = top;
        if (top.isEmpty()) settleEnd = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    private boolean assembled() {
        return getBlockState().getBlock() instanceof CupboardBlock && getBlockState().getValue(CupboardBlock.ASSEMBLED);
    }

    @Override
    public int getContainerSize() { return 27; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) { return assembled(); }

    @Override
    public boolean stillValid(Player player) { return assembled() && super.stillValid(player); }

    @Override
    protected Component getDefaultName() { return Component.translatable("container.muchmorestorage.cupboard"); }

    @Override
    protected NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        body = input.getStringOr("Body", "oak");
        top = input.getStringOr("Top", "");
        if (top.isEmpty()) settleStart = -1;
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Body", body);
        output.putString("Top", top);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder b) {
        super.collectImplicitComponents(b);
        b.set(ModContent.BODY, body);
        if (hasTop()) b.set(ModContent.TOP, top);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter in) {
        super.applyImplicitComponents(in);
        String b = in.get(ModContent.BODY);
        String t = in.get(ModContent.TOP);
        if (b != null) body = b;
        if (t != null) top = t;
    }

    @Override
    public void removeComponentsFromTag(ValueOutput out) {
        super.removeComponentsFromTag(out);
        out.discard("Body");
        out.discard("Top");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Body", body);
        tag.putString("Top", top);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id == EVENT_OPEN_COUNT) {
            door.shouldBeOpen(value > 0);
            return true;
        }
        if (id == EVENT_SETTLE) {
            if (level != null) settleStart = level.getGameTime();
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

    public boolean startSettle() {
        if (level == null || settleEnd > 0) return false;
        settleEnd = level.getGameTime() + SETTLE_TICKS;
        level.blockEvent(getBlockPos(), getBlockState().getBlock(), EVENT_SETTLE, 0);
        level.scheduleTick(getBlockPos(), getBlockState().getBlock(), SETTLE_TICKS);
        return true;
    }

    // 0 while loose, rising to 1 as the worktop drops into place
    public float settleProgress(float partialTick) {
        if (settleStart < 0 || level == null) return 0F;
        return Math.min(1F, (level.getGameTime() - settleStart + partialTick) / SETTLE_TICKS);
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
