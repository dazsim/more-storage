package com.workshopcraft.muchmorestorage.block;

//? if >=1.20.5 && <26.3
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
//? if >=26.1
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
//? if <26.1
/*import net.minecraft.world.item.DyeItem;*/
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
//? if >=1.20.5 && <26.3
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

// the GENOMSKADA clear box, 9 slots, stacks three high, a dye recolours the lid
public class GenomskadaBlock extends FurnitureBlock {
    //? if >=1.20.5 && <26.3
    public static final MapCodec<GenomskadaBlock> CODEC = simpleCodec(GenomskadaBlock::new);
    public static final int MAX_STACK = 3;
    public static final Set<String> LIDS = Set.of("white", "light_gray", "gray", "black", "red", "green", "pink", "purple",
            "brown", "orange", "lime", "cyan", "magenta");
    private static final VoxelShape SHAPE = Block.box(0.5, 0, 0.5, 15.5, 12.5, 15.5);

    public GenomskadaBlock(Properties props) { super(props); }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    //?}

    @Override
    protected Map<String, ? extends Item> flatpacks() { return Map.of(); }

    @Override
    protected ItemStack assembledItem(String body) { return ModContent.furniture(ModContent.GENOMSKADA_ITEM, body); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GenomskadaBlockEntity(pos, state); }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos p = ctx.getClickedPos();
        int under = 0;
        while (under < MAX_STACK && ctx.getLevel().getBlockState(p.below(under + 1)).is(this)) under++;
        if (under >= MAX_STACK) return null;
        return super.getStateForPlacement(ctx).setValue(ASSEMBLED, true).setValue(CONCRETE, true);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    public SoundType getSoundType(BlockState state) { return SoundType.STONE; }

    private static @Nullable String lidOf(ItemStack held) {
        //? if >=26.1 {
        DyeColor c = held.get(DataComponents.DYE);
        //?} else
        /*DyeColor c = held.getItem() instanceof DyeItem d ? d.getDyeColor() : null;*/
        if (c == null || !LIDS.contains(c.getSerializedName())) return null;
        return c.getSerializedName() + "_concrete";
    }

    @Override
    protected boolean useHeld(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        String lid = lidOf(held);
        if (lid == null || !(level.getBlockEntity(pos) instanceof GenomskadaBlockEntity be) || lid.equals(be.body())) return false;
        if (!level.isClientSide()) {
            be.setFinishes(lid, "");
            if (!player.isCreative()) held.shrink(1);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1F, 1F);
        }
        return true;
    }

    @Override
    protected boolean useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GenomskadaBlockEntity be)) return false;
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
