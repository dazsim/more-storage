package com.workshopcraft.muchmorestorage.registry;

import com.mojang.serialization.Codec;
import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.block.CupboardBlock;
import com.workshopcraft.muchmorestorage.block.CupboardBlockEntity;
import com.workshopcraft.muchmorestorage.item.CarcassItem;
import com.workshopcraft.muchmorestorage.item.CupboardItem;
import com.workshopcraft.muchmorestorage.item.ScrewdriverItem;
import com.workshopcraft.muchmorestorage.item.WorktopItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
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
        r.accept(MuchMoreStorage.id("body"), BODY);
        r.accept(MuchMoreStorage.id("top"), TOP);
    }

    public static void registerBlocks(BiConsumer<Identifier, Block> r) {
        Identifier id = MuchMoreStorage.id("cupboard");
        CUPBOARD = new CupboardBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .mapColor(s -> s.getValue(CupboardBlock.CONCRETE) ? MapColor.STONE : MapColor.WOOD)
                .strength(2.5F)
                .noOcclusion());
        r.accept(id, CUPBOARD);
    }

    private static Item.Properties item(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, MuchMoreStorage.id(name)));
    }

    public static void registerItems(BiConsumer<Identifier, Item> r) {
        for (String body : Finishes.BODIES) {
            CarcassItem item = new CarcassItem(CUPBOARD, body, item(body + "_carcass"));
            CARCASSES.put(body, item);
            r.accept(MuchMoreStorage.id(body + "_carcass"), item);
        }
        for (String top : Finishes.TOPS) {
            WorktopItem item = new WorktopItem(top, item(top + "_worktop"));
            WORKTOPS.put(top, item);
            r.accept(MuchMoreStorage.id(top + "_worktop"), item);
        }
        SCREWDRIVER = new ScrewdriverItem(item("screwdriver").stacksTo(1));
        r.accept(MuchMoreStorage.id("screwdriver"), SCREWDRIVER);
        // registered last so the block maps back to it
        CUPBOARD_ITEM = new CupboardItem(CUPBOARD, item("cupboard").component(BODY, "oak").component(TOP, "oak"));
        r.accept(MuchMoreStorage.id("cupboard"), CUPBOARD_ITEM);
    }

    public static final TagKey<Item> WORKTOP_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "worktops"));
    public static final TagKey<Item> SCREWDRIVERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/screwdriver"));

    // own tops are short names, worktops from other mods keep their full item id
    public static String topOf(ItemStack stack) {
        if (stack.getItem() instanceof WorktopItem w) return w.top;
        if (stack.is(WORKTOP_TAG)) return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return "";
    }

    public static ItemStack worktop(String top) {
        if (top.contains(":")) {
            Identifier id = Identifier.tryParse(top);
            return id == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
        }
        WorktopItem item = WORKTOPS.get(top);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static ItemStack cupboard(String body, String top) {
        ItemStack stack = new ItemStack(CUPBOARD_ITEM);
        stack.set(BODY, body);
        stack.set(TOP, top);
        return stack;
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> r) {
        CUPBOARD_BE = new BlockEntityType<>(CupboardBlockEntity::new, Set.of(CUPBOARD));
        r.accept(MuchMoreStorage.id("cupboard"), CUPBOARD_BE);
    }

    public static void registerCreativeTabs(BiConsumer<Identifier, CreativeModeTab> r) {
        TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup." + MuchMoreStorage.MOD_ID))
                .icon(() -> cupboard("white_concrete", "granite"))
                .displayItems((params, out) -> {
                    out.accept(SCREWDRIVER);
                    for (String[] e : EXAMPLES) out.accept(cupboard(e[0], e[1]));
                    CARCASSES.values().forEach(out::accept);
                    WORKTOPS.values().forEach(out::accept);
                })
                .build();
        r.accept(MuchMoreStorage.id("main"), TAB);
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
