package com.workshopcraft.morestorage.registry;

import com.workshopcraft.morestorage.MoreStorage;
import com.workshopcraft.morestorage.block.CupboardBlock;
import com.workshopcraft.morestorage.block.CupboardBlockEntity;
import com.workshopcraft.morestorage.item.ScrewdriverItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ModContent {
    public static final Map<Finish, CupboardBlock> CUPBOARDS = new EnumMap<>(Finish.class);
    public static BlockEntityType<CupboardBlockEntity> CUPBOARD_BE;
    public static CreativeModeTab TAB;
    public static ScrewdriverItem SCREWDRIVER;

    private ModContent() {}

    public static void registerBlocks(BiConsumer<Identifier, Block> r) {
        for (Finish finish : Finish.values()) {
            Identifier id = MoreStorage.id(finish.blockId());
            CupboardBlock block = new CupboardBlock(finish, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .mapColor(finish.mapColor)
                    .sound(SoundType.WOOD)
                    .strength(2.5F)
                    .ignitedByLava()
                    .noOcclusion());
            CUPBOARDS.put(finish, block);
            r.accept(id, block);
        }
    }

    public static void registerItems(BiConsumer<Identifier, Item> r) {
        CUPBOARDS.forEach((finish, block) -> {
            Identifier id = MoreStorage.id(finish.blockId());
            r.accept(id, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix()));
        });
        Identifier id = MoreStorage.id("screwdriver");
        SCREWDRIVER = new ScrewdriverItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1));
        r.accept(id, SCREWDRIVER);
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> r) {
        CUPBOARD_BE = new BlockEntityType<>(CupboardBlockEntity::new, new HashSet<>(CUPBOARDS.values()));
        r.accept(MoreStorage.id("cupboard"), CUPBOARD_BE);
    }

    public static void registerCreativeTabs(BiConsumer<Identifier, CreativeModeTab> r) {
        TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup." + MoreStorage.MOD_ID))
                .icon(() -> new ItemStack(CUPBOARDS.get(Finish.OAK)))
                .displayItems((params, out) -> {
                    CUPBOARDS.values().forEach(out::accept);
                    out.accept(SCREWDRIVER);
                })
                .build();
        r.accept(MoreStorage.id("main"), TAB);
    }

    public static <T> BiConsumer<Identifier, T> direct(Registry<? super T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }

    public static void registerAllDirect() {
        registerBlocks(direct(BuiltInRegistries.BLOCK));
        registerItems(direct(BuiltInRegistries.ITEM));
        registerBlockEntities(direct(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        registerCreativeTabs(direct(BuiltInRegistries.CREATIVE_MODE_TAB));
    }
}
