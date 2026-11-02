package com.workshopcraft.muchmorestorage.block;

//? if >=1.20.5 && <26.3
import com.mojang.serialization.MapCodec;
import com.workshopcraft.muchmorestorage.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
//? if >=1.20.5 && <26.3
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Map;

// the LADSTAPEL chest of drawers, a click opens just the drawer under it
public class LadstapelBlock extends FurnitureBlock {
    //? if >=1.20.5 && <26.3
    public static final MapCodec<LadstapelBlock> CODEC = simpleCodec(LadstapelBlock::new);
    public static final String[] NAMES = {"top", "middle", "bottom"};

    public LadstapelBlock(Properties props) { super(props); }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    //?}

    @Override
    protected Map<String, ? extends Item> flatpacks() { return ModContent.LADSTAPEL_FLATPACKS; }

    @Override
    protected ItemStack assembledItem(String body) { return ModContent.furniture(ModContent.LADSTAPEL_ITEM, body); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LadstapelBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? createTickerHelper(type, ModContent.LADSTAPEL_BE, LadstapelBlockEntity::clientTick) : null;
    }

    // the drawers split the inside, 1 to 15 pixels, in three, a click off the front picks by height too
    public static int drawerAt(BlockHitResult hit) {
        double y = hit.getLocation().y - hit.getBlockPos().getY();
        return y >= (1 + 28F / 3F) / 16F ? 0 : y >= (1 + 14F / 3F) / 16F ? 1 : 2;
    }

    @Override
    protected boolean useEmpty(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LadstapelBlockEntity be)) return false;
        if (level instanceof ServerLevel server) {
            int d = drawerAt(hit);
            var drawer = be.drawer(d);
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x1, id, inv, drawer, 1),
                    Component.translatable("container.muchmorestorage.ladstapel." + NAMES[d])));
            player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            //? if >=1.21.2 {
            PiglinAi.angerNearbyPiglins(server, player, true);
            //?} else
            /*PiglinAi.angerNearbyPiglins(player, true);*/
        }
        return true;
    }
}
