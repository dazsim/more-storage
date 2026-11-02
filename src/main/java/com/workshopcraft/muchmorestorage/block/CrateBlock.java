package com.workshopcraft.muchmorestorage.block;

//? if >=1.20.5 && <26.3
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
//? if >=1.20.5 && <26.3
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

// a flat-pack crate, four turns of the HEXHOJK put it together
public class CrateBlock extends FurnitureBlock {
    //? if >=1.20.5 && <26.3
    public static final MapCodec<CrateBlock> CODEC = simpleCodec(CrateBlock::new);
    public static final int TURNS = 4;
    public static final IntegerProperty STEP = IntegerProperty.create("step", 0, TURNS);
    private static final VoxelShape FLAT = Block.box(1, 0, 1, 15, 6, 15);
    private static final VoxelShape BUILT = Block.box(1, 0, 1, 15, 15.5, 15);

    public CrateBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(STEP, TURNS));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        super.createBlockStateDefinition(b);
        b.add(STEP);
    }

    @Override
    protected Map<String, ? extends Item> flatpacks() { return Map.of(); }

    @Override
    protected ItemStack assembledItem(String body) { return ModContent.furniture(ModContent.CRATE_ITEM, body); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CrateBlockEntity(pos, state); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(ASSEMBLED, false).setValue(STEP, 0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(STEP) == 0 ? FLAT : BUILT;
    }

    // a crate always comes back as its flat pack
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CupboardBlockEntity be) return List.of(assembledItem(be.body()));
        return List.of();
    }

    // one turn of the key, returns false once it is already built
    public static boolean turn(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        int step = state.getValue(STEP);
        if (step >= TURNS) return false;
        if (level.isClientSide()) return true;
        step++;
        boolean done = step == TURNS;
        level.setBlock(pos, state.setValue(STEP, step).setValue(ASSEMBLED, done), 3);
        if (player != null) tell(player, done ? Component.translatable("message.muchmorestorage.flatpack_crate.assembled")
                : Component.translatable("message.muchmorestorage.flatpack_crate.turn", step, TURNS));
        if (done) {
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1F, 0.8F);
            if (level.getRandom().nextInt(4) == 0) {
                Block.popResource(level, pos.above(), new ItemStack(ModContent.LEFTOVER_SCREW));
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.6F);
            }
        } else {
            level.playSound(null, pos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 0.7F + step * 0.15F);
        }
        return true;
    }

    private static void tell(Player p, Component c) {
        //? if >=26.1 {
        p.sendOverlayMessage(c);
        //?} else
        /*p.displayClientMessage(c, true);*/
    }

    @Override
    protected boolean useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CrateBlockEntity be)) return false;
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
}
