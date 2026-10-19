package com.workshopcraft.morestorage.registry;

import com.mojang.serialization.Codec;
import com.workshopcraft.morestorage.MoreStorage;
import com.workshopcraft.morestorage.block.CupboardBlock;
import com.workshopcraft.morestorage.block.CupboardBlockEntity;
import com.workshopcraft.morestorage.item.CarcassItem;
import com.workshopcraft.morestorage.item.CupboardItem;
import com.workshopcraft.morestorage.item.ScrewdriverItem;
import com.workshopcraft.morestorage.item.WorktopItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ModContent {
    public static final Map<String, CarcassItem> CARCASSES = new LinkedHashMap<>();
    public static final Map<String, WorktopItem> WORKTOPS = new LinkedHashMap<>();
    public static CupboardBlock CUPBOARD;
    public static CupboardItem CUPBOARD_ITEM;
    public static ScrewdriverItem SCREWDRIVER;
    public static BlockEntityType<CupboardBlockEntity> CUPBOARD_BE;
    public static DataComponentType<String> BODY;
    public static DataComponentType<String> TOP;
    public static CreativeModeTab TAB;

    private static final String[][] EXAMPLES = {
            {"oak", "oak"}, {"white_concrete", "granite"}, {"spruce", "smooth_stone"}, {"dark_oak", "polished_andesite"},
            {"light_blue_concrete", "polished_diorite"}, {"black_concrete", "white_concrete"}, {"cherry", "cherry"},
            {"green_concrete", "oak"}, {"birch", "polished_granite"}};

    private ModContent() {}

    public static void registerDataComponents(BiConsumer<Identifier, DataComponentType<?>> r) {
        BODY = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
        TOP = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
        r.accept(MoreStorage.id("body"), BODY);
        r.accept(MoreStorage.id("top"), TOP);
    }

    public static void registerBlocks(BiConsumer<Identifier, Block> r) {
        Identifier id = MoreStorage.id("cupboard");
        CUPBOARD = new CupboardBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .mapColor(s -> s.getValue(CupboardBlock.CONCRETE) ? MapColor.STONE : MapColor.WOOD)
                .strength(2.5F)
                .noOcclusion());
        r.accept(id, CUPBOARD);
    }

    private static Item.Properties item(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, MoreStorage.id(name)));
    }

    public static void registerItems(BiConsumer<Identifier, Item> r) {
        for (String body : Finishes.BODIES) {
            CarcassItem item = new CarcassItem(CUPBOARD, body, item(body + "_carcass"));
            CARCASSES.put(body, item);
            r.accept(MoreStorage.id(body + "_carcass"), item);
        }
        for (String top : Finishes.TOPS) {
            WorktopItem item = new WorktopItem(top, item(top + "_worktop"));
            WORKTOPS.put(top, item);
            r.accept(MoreStorage.id(top + "_worktop"), item);
        }
        SCREWDRIVER = new ScrewdriverItem(item("screwdriver").stacksTo(1));
        r.accept(MoreStorage.id("screwdriver"), SCREWDRIVER);
        // registered last so the block maps back to it
        CUPBOARD_ITEM = new CupboardItem(CUPBOARD, item("cupboard").component(BODY, "oak").component(TOP, "oak"));
        r.accept(MoreStorage.id("cupboard"), CUPBOARD_ITEM);
    }

    public static ItemStack cupboard(String body, String top) {
        ItemStack stack = new ItemStack(CUPBOARD_ITEM);
        stack.set(BODY, body);
        stack.set(TOP, top);
        return stack;
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> r) {
        CUPBOARD_BE = new BlockEntityType<>(CupboardBlockEntity::new, Set.of(CUPBOARD));
        r.accept(MoreStorage.id("cupboard"), CUPBOARD_BE);
    }

    public static void registerCreativeTabs(BiConsumer<Identifier, CreativeModeTab> r) {
        TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup." + MoreStorage.MOD_ID))
                .icon(() -> cupboard("white_concrete", "granite"))
                .displayItems((params, out) -> {
                    out.accept(SCREWDRIVER);
                    for (String[] e : EXAMPLES) out.accept(cupboard(e[0], e[1]));
                    CARCASSES.values().forEach(out::accept);
                    WORKTOPS.values().forEach(out::accept);
                })
                .build();
        r.accept(MoreStorage.id("main"), TAB);
    }

    public static <T> BiConsumer<Identifier, T> direct(Registry<? super T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }

    public static void registerAllDirect() {
        registerDataComponents(direct(BuiltInRegistries.DATA_COMPONENT_TYPE));
        registerBlocks(direct(BuiltInRegistries.BLOCK));
        registerItems(direct(BuiltInRegistries.ITEM));
        registerBlockEntities(direct(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        registerCreativeTabs(direct(BuiltInRegistries.CREATIVE_MODE_TAB));
    }
}
