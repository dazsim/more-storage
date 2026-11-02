package com.workshopcraft.muchmorestorage.block;

//? if >=1.20.5 && <26.3
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
//? if >=1.20.5 && <26.3
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

// the BOKWURM bookcase, two shelves of six, books go in and come out where you click
public class BokwurmBlock extends FurnitureBlock {
    //? if >=1.20.5 && <26.3
    public static final MapCodec<BokwurmBlock> CODEC = simpleCodec(BokwurmBlock::new);
    // the inside runs from 1 to 15 pixels, the shelf sits at 7.5 to 8.5
    public static final float SHELF = 8.5F / 16F;

    public BokwurmBlock(Properties props) { super(props); }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    //?}

    @Override
    protected Map<String, ? extends Item> flatpacks() { return ModContent.BOKWURM_FLATPACKS; }

    @Override
    protected ItemStack assembledItem(String body) { return ModContent.furniture(ModContent.BOKWURM_ITEM, body); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BokwurmBlockEntity(pos, state); }

    // the slot under a click on the front, row 0 is the top shelf
    private static int slotAt(BlockState state, BlockHitResult hit) {
        Vec3 uv = frontHit(state, hit);
        if (uv == null) return -1;
        int row = uv.y >= 0.5 ? 0 : 1;
        int col = (int) Math.floor((uv.x * 16 - 1) / (14.0 / BokwurmBlockEntity.PER_SHELF));
        return row * BokwurmBlockEntity.PER_SHELF + Math.max(0, Math.min(BokwurmBlockEntity.PER_SHELF - 1, col));
    }

    // the clicked slot if it matches, else the nearest one on the same shelf that does
    private static int nearest(BokwurmBlockEntity be, int slot, boolean empty) {
        int row = slot / BokwurmBlockEntity.PER_SHELF * BokwurmBlockEntity.PER_SHELF;
        int col = slot % BokwurmBlockEntity.PER_SHELF;
        for (int d = 0; d < BokwurmBlockEntity.PER_SHELF; d++) {
            for (int c : new int[]{col - d, col + d}) {
                if (c < 0 || c >= BokwurmBlockEntity.PER_SHELF) continue;
                if (be.getItem(row + c).isEmpty() == empty) return row + c;
            }
        }
        return -1;
    }

    @Override
    protected boolean useHeld(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!held.is(ModContent.BOKWURM_BOOKS) || !(level.getBlockEntity(pos) instanceof BokwurmBlockEntity be)) return false;
        int at = slotAt(state, hit);
        int slot = at < 0 ? -1 : nearest(be, at, true);
        if (slot < 0) return false;
        if (!level.isClientSide()) {
            be.setItem(slot, held.copyWithCount(1));
            if (!player.getAbilities().instabuild) held.shrink(1);
            level.playSound(null, pos, held.is(Items.ENCHANTED_BOOK) || be.getItem(slot).is(Items.ENCHANTED_BOOK)
                    ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    protected boolean useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BokwurmBlockEntity be)) return false;
        int at = slotAt(state, hit);
        int slot = at < 0 ? -1 : nearest(be, at, false);
        if (slot < 0) return false;
        if (!level.isClientSide()) {
            ItemStack out = be.removeItem(slot, 1);
            level.playSound(null, pos, out.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED
                    : SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(out)) net.minecraft.world.level.block.Block.popResource(level, pos.above(), out);
        }
        return true;
    }

    // every 3 books counts as one bookshelf, an empty one gives nothing
    public static int power(net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BokwurmBlockEntity be ? be.books() / 3 : 0;
    }

    // vanilla counts each tagged block as 1, so Fabric adds BOKWURM power here instead
    public static boolean reaches(Level level, BlockPos table, BlockPos offset) {
        return power(level, table.offset(offset)) > 0 && level.getBlockState(table.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2))
                .is(net.minecraft.tags.BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }

    public static int tablePower(Level level, BlockPos table) {
        int n = 0;
        for (BlockPos offset : SHELF_OFFSETS) if (reaches(level, table, offset)) n += power(level, table.offset(offset));
        return n;
    }

    //? if >=1.20.5 {
    public static final java.util.List<BlockPos> SHELF_OFFSETS = net.minecraft.world.level.block.EnchantingTableBlock.BOOKSHELF_OFFSETS;
    //?} else {
    /*public static final java.util.List<BlockPos> SHELF_OFFSETS = net.minecraft.world.level.block.EnchantmentTableBlock.BOOKSHELF_OFFSETS;
    *///?}
    // set while the enchanting menu works out its costs
    public static final ThreadLocal<Integer> TABLE_EXTRA = new ThreadLocal<>();

    //? if forge || (neoforge && <26.1) {
    /*@Override
    public float getEnchantPowerBonus(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) { return power(level, pos); }
    *///?} else if neoforge {
    /*@Override
    public float getEnchantPowerBonus(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) { return power(level, pos); }
    *///?}

    @Override
    protected int signal(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BokwurmBlockEntity be ? be.books() : 0;
    }
}
