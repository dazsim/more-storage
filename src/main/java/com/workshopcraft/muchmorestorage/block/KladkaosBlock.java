package com.workshopcraft.muchmorestorage.block;

//? if >=1.20.5 && <26.3
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
//? if >=1.20.5 && <26.3
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Map;

// the KLADKAOS wardrobe, two blocks tall, the lower half holds the block entity
public class KladkaosBlock extends FurnitureBlock implements WorldlyContainerHolder {
    //? if >=1.20.5 && <26.3
    public static final MapCodec<KladkaosBlock> CODEC = simpleCodec(KladkaosBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public KladkaosBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        super.createBlockStateDefinition(b);
        b.add(HALF);
    }

    @Override
    protected Map<String, ? extends Item> flatpacks() { return ModContent.KLADKAOS_PANELS; }

    @Override
    protected ItemStack assembledItem(String body) { return ModContent.furniture(ModContent.KLADKAOS_ITEM, body); }

    public static boolean upper(BlockState state) { return state.getValue(HALF) == DoubleBlockHalf.UPPER; }

    public static BlockPos base(BlockState state, BlockPos pos) { return upper(state) ? pos.below() : pos; }

    private static @Nullable KladkaosBlockEntity at(BlockGetter level, BlockState state, BlockPos pos) {
        return level.getBlockEntity(base(state, pos)) instanceof KladkaosBlockEntity be ? be : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return upper(state) ? null : new KladkaosBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModContent.KLADKAOS_BE, KladkaosBlockEntity::clientTick) : null;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos above = ctx.getClickedPos().above();
        Level level = ctx.getLevel();
        if (level.isOutsideBuildHeight(above) || !level.getBlockState(above).canBeReplaced(ctx)) return null;
        return super.getStateForPlacement(ctx).setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        super.setPlacedBy(level, pos, state, by, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    // breaking either half takes the other with it, only the lower one drops anything
    private void removeOther(BlockState state, Level level, BlockPos pos) {
        BlockPos other = upper(state) ? pos.below() : pos.above();
        BlockState o = level.getBlockState(other);
        if (!o.is(this) || o.getValue(HALF) == state.getValue(HALF)) return;
        if (upper(state)) level.destroyBlock(other, true);
        else level.removeBlock(other, false);
    }

    private static void creativeBreak(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide() || !player.isCreative() || !upper(state)) return;
        BlockState below = level.getBlockState(pos.below());
        if (below.is(state.getBlock()) && !upper(below)) level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 35);
    }

    //? if >=1.20.5 {
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        creativeBreak(level, pos, state, player);
        return super.playerWillDestroy(level, pos, state, player);
    }
    //?} else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        creativeBreak(level, pos, state, player);
        super.playerWillDestroy(level, pos, state, player);
    }
    *///?}

    //? if >=1.21.5 {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        removeOther(state, level, pos);
    }
    //?} else {
    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean gone = !state.is(newState.getBlock());
        if (gone && level.getBlockEntity(pos) instanceof KladkaosBlockEntity be) be.dropArmour();
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (gone) removeOther(state, level, pos);
    }
    *///?}

    @Override
    protected boolean useHeld(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int slot = KladkaosBlockEntity.railSlot(player, held);
        KladkaosBlockEntity be = at(level, state, pos);
        if (slot < 0 || be == null) return false;
        if (!level.isClientSide()) {
            InteractionHand hand = player.getMainHandItem() == held ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            be.hang(player, hand, slot);
        }
        return true;
    }

    // a click opens the 54 slots, sneaking with empty hands swaps what you wear for what hangs on the rail
    @Override
    protected boolean useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        KladkaosBlockEntity be = at(level, state, pos);
        if (be == null) return false;
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) be.swapOutfit(player);
            return true;
        }
        if (level instanceof ServerLevel server) {
            player.openMenu(be);
            player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            //? if >=1.21.2 {
            PiglinAi.angerNearbyPiglins(server, player, true);
            //?} else
            /*PiglinAi.angerNearbyPiglins(player, true);*/
        }
        return true;
    }

    @Override
    protected int signal(Level level, BlockPos pos) {
        return super.signal(level, base(level.getBlockState(pos), pos));
    }

    @Override
    public @Nullable WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        return at(level, state, pos);
    }

    //? if >=1.21.4 {
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
    //?} else if >=1.20.5 {
    /*@Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    *///?} else {
    /*@Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
    *///?}
        KladkaosBlockEntity be = at(level, state, pos);
        return assembledItem(be == null ? "white_concrete" : be.body());
    }
}
