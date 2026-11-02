package com.workshopcraft.muchmorestorage.item;

import com.workshopcraft.muchmorestorage.block.CrateBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

// the hex key, each use on a flat-pack crate is one turn
public class HexhojkItem extends TipItem {
    public HexhojkItem(Properties props) {
        super(props, "hexhojk", "hexhojk.use");
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (!(state.getBlock() instanceof CrateBlock) || !CrateBlock.turn(ctx.getLevel(), ctx.getClickedPos(), state, ctx.getPlayer())) return InteractionResult.PASS;
        //? if >=1.21.2 {
        return InteractionResult.SUCCESS;
        //?} else
        /*return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide());*/
    }
}
