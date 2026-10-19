package com.workshopcraft.morestorage.block;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morestorage.item.ScrewdriverItem;
import com.workshopcraft.morestorage.registry.Finish;
import com.workshopcraft.morestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class CupboardBlock extends BaseEntityBlock {
    public static final MapCodec<CupboardBlock> CODEC = simpleCodec(p -> new CupboardBlock(Finish.OAK, p));
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty LOOSE = BooleanProperty.create("loose");
    public final Finish finish;

    public CupboardBlock(Finish finish, Properties props) {
        super(props);
        this.finish = finish;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HINGE, DoorHingeSide.LEFT).setValue(LOOSE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, HINGE, LOOSE); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(LOOSE, true);
    }

    @Override
    protected BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }

    @Override
    protected BlockState mirror(BlockState s, Mirror m) {
        BlockState r = s.rotate(m.getRotation(s.getValue(FACING)));
        return m == Mirror.NONE ? r : r.cycle(HINGE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CupboardBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModContent.CUPBOARD_BE, CupboardBlockEntity::clientTick) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        return held.getItem() instanceof ScrewdriverItem ? InteractionResult.PASS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof CupboardBlockEntity cupboard) {
            player.openMenu(cupboard);
            player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            PiglinAi.angerNearbyPiglins(server, player, true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CupboardBlockEntity cupboard)) return;
        cupboard.recheckOpen();
        if (state.getValue(LOOSE) && cupboard.settleEnd > 0) {
            long left = cupboard.settleEnd - level.getGameTime();
            if (left > 0) {
                level.scheduleTick(pos, this, (int) left);
                return;
            }
            cupboard.settleEnd = 0;
            level.setBlock(pos, state.setValue(LOOSE, false), 3);
            SoundType sound = finish.stoneTop ? SoundType.STONE : SoundType.WOOD;
            level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, 0.8F, 1.1F);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
