package com.workshopcraft.muchmorestorage.registry;

//? if >=1.20.5
import com.mojang.serialization.Codec;
import com.workshopcraft.muchmorestorage.MuchMoreStorage;
import com.workshopcraft.muchmorestorage.block.BokwurmBlock;
import com.workshopcraft.muchmorestorage.block.BokwurmBlockEntity;
import com.workshopcraft.muchmorestorage.block.CupboardBlock;
import com.workshopcraft.muchmorestorage.block.CupboardBlockEntity;
import com.workshopcraft.muchmorestorage.block.Insert;
import com.workshopcraft.muchmorestorage.block.KubhalaBlock;
import com.workshopcraft.muchmorestorage.block.KubhalaBlockEntity;
import com.workshopcraft.muchmorestorage.block.LadstapelBlock;
import com.workshopcraft.muchmorestorage.block.LadstapelBlockEntity;
//? if >=26.1 {
import com.workshopcraft.muchmorestorage.crafting.KeepToolShapelessRecipe;
//?} else if >=1.21 {
/*import com.workshopcraft.muchmorestorage.crafting.v1_21.KeepToolShapelessRecipe;
*///?} else
/*import com.workshopcraft.muchmorestorage.crafting.v1_20.KeepToolShapelessRecipe;*/
import com.workshopcraft.muchmorestorage.item.CarcassItem;
import com.workshopcraft.muchmorestorage.item.CupboardItem;
import com.workshopcraft.muchmorestorage.item.FlatpackItem;
import com.workshopcraft.muchmorestorage.item.FurnitureItem;
import com.workshopcraft.muchmorestorage.item.InsertItem;
import com.workshopcraft.muchmorestorage.item.KubhalaItem;
import com.workshopcraft.muchmorestorage.item.SakksakkItem;
import com.workshopcraft.muchmorestorage.item.ScrewdriverItem;
import com.workshopcraft.muchmorestorage.item.WorktopItem;
import net.minecraft.core.Registry;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
    public static final Map<String, FlatpackItem> FLATPACKS = new LinkedHashMap<>();
    public static KubhalaBlock KUBHALA;
    public static KubhalaItem KUBHALA_ITEM;
    public static InsertItem DRAWER_INSERT;
    public static InsertItem DOOR_INSERT;
    public static final Map<String, SakksakkItem> SAKKSAKKS = new LinkedHashMap<>();
    public static BlockEntityType<KubhalaBlockEntity> KUBHALA_BE;
    public static final Map<String, FlatpackItem> BOKWURM_FLATPACKS = new LinkedHashMap<>();
    public static final Map<String, FlatpackItem> LADSTAPEL_FLATPACKS = new LinkedHashMap<>();
    public static BokwurmBlock BOKWURM;
    public static FurnitureItem BOKWURM_ITEM;
    public static BlockEntityType<BokwurmBlockEntity> BOKWURM_BE;
    public static LadstapelBlock LADSTAPEL;
    public static FurnitureItem LADSTAPEL_ITEM;
    public static BlockEntityType<LadstapelBlockEntity> LADSTAPEL_BE;
    //? if >=1.20.5 {
    public static DataComponentType<String> BODY;
    public static DataComponentType<String> TOP;
    //?}
    public static CreativeModeTab TAB;

    private static final String[][] EXAMPLES = {
            {"oak", "oak"}, {"white_concrete", "granite"}, {"spruce", "smooth_stone"}, {"dark_oak", "polished_andesite"},
            {"light_blue_concrete", "polished_diorite"}, {"black_concrete", "white_concrete"}, {"cherry", "cherry"},
            {"green_concrete", "oak"}, {"birch", "polished_granite"}};

    private static final String[] KUBHALA_EXAMPLES = {"white_concrete", "black_concrete", "oak", "birch", "dark_oak"};

    public static final java.util.List<String> SAKKSAKK_COLOURS = new java.util.ArrayList<>(Finishes.COLOURS);
    static { SAKKSAKK_COLOURS.add("wicker"); }

    private ModContent() {}

    //? if >=1.20.5 {
    public static void registerDataComponents(BiConsumer<Identifier, DataComponentType<?>> r) {
        BODY = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
        TOP = DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
        r.accept(MuchMoreStorage.id("body"), BODY);
        r.accept(MuchMoreStorage.id("top"), TOP);
    }
    //?}

    public static void registerBlocks(BiConsumer<Identifier, Block> r) {
        Identifier id = MuchMoreStorage.id("cupboard");
        CUPBOARD = new CupboardBlock(BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .mapColor(s -> s.getValue(CupboardBlock.CONCRETE) ? MapColor.STONE : MapColor.WOOD)
                .strength(2.5F)
                .noOcclusion());
        r.accept(id, CUPBOARD);
        Identifier kid = MuchMoreStorage.id("kubhala");
        KUBHALA = new KubhalaBlock(BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.BLOCK, kid))
                .mapColor(s -> s.getValue(KubhalaBlock.CONCRETE) ? MapColor.STONE : MapColor.WOOD)
                .strength(2.0F)
                .noOcclusion());
        r.accept(kid, KUBHALA);
        Identifier bid = MuchMoreStorage.id("bokwurm");
        BOKWURM = new BokwurmBlock(BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.BLOCK, bid))
                .mapColor(s -> s.getValue(BokwurmBlock.CONCRETE) ? MapColor.STONE : MapColor.WOOD)
                .strength(2.0F)
                .noOcclusion());
        r.accept(bid, BOKWURM);
        Identifier lid = MuchMoreStorage.id("ladstapel");
        LADSTAPEL = new LadstapelBlock(BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(ResourceKey.create(Registries.BLOCK, lid))
                .mapColor(s -> s.getValue(LadstapelBlock.CONCRETE) ? MapColor.STONE : MapColor.WOOD)
                .strength(2.0F)
                .noOcclusion());
        r.accept(lid, LADSTAPEL);
    }

    private static Item.Properties item(String name) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, MuchMoreStorage.id(name)));
        //?} else
        /*return new Item.Properties();*/
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
        //? if >=1.20.5 {
        CUPBOARD_ITEM = new CupboardItem(CUPBOARD, item("cupboard").component(BODY, "oak").component(TOP, "oak"));
        //?} else
        /*CUPBOARD_ITEM = new CupboardItem(CUPBOARD, item("cupboard"));*/
        r.accept(MuchMoreStorage.id("cupboard"), CUPBOARD_ITEM);
        for (String body : Finishes.BODIES) {
            FlatpackItem item = new FlatpackItem(KUBHALA, body, item(body + "_kubhala_flatpack"));
            FLATPACKS.put(body, item);
            r.accept(MuchMoreStorage.id(body + "_kubhala_flatpack"), item);
        }
        DRAWER_INSERT = new InsertItem(Insert.DRAWER, item("kubhala_drawer"));
        r.accept(MuchMoreStorage.id("kubhala_drawer"), DRAWER_INSERT);
        DOOR_INSERT = new InsertItem(Insert.DOOR, item("kubhala_door"));
        r.accept(MuchMoreStorage.id("kubhala_door"), DOOR_INSERT);
        for (String c : SAKKSAKK_COLOURS) {
            SakksakkItem box = new SakksakkItem(c, item(c + "_sakksakk").stacksTo(1));
            SAKKSAKKS.put(c, box);
            r.accept(MuchMoreStorage.id(c + "_sakksakk"), box);
        }
        //? if >=1.20.5 {
        KUBHALA_ITEM = new KubhalaItem(KUBHALA, item("kubhala").component(BODY, "white_concrete"));
        //?} else
        /*KUBHALA_ITEM = new KubhalaItem(KUBHALA, item("kubhala"));*/
        r.accept(MuchMoreStorage.id("kubhala"), KUBHALA_ITEM);
        for (String body : Finishes.BODIES) {
            FlatpackItem b = new FlatpackItem(BOKWURM, body, item(body + "_bokwurm_flatpack"));
            BOKWURM_FLATPACKS.put(body, b);
            r.accept(MuchMoreStorage.id(body + "_bokwurm_flatpack"), b);
            FlatpackItem l = new FlatpackItem(LADSTAPEL, body, item(body + "_ladstapel_flatpack"));
            LADSTAPEL_FLATPACKS.put(body, l);
            r.accept(MuchMoreStorage.id(body + "_ladstapel_flatpack"), l);
        }
        //? if >=1.20.5 {
        BOKWURM_ITEM = new FurnitureItem(BOKWURM, "bokwurm", item("bokwurm").component(BODY, "white_concrete"));
        LADSTAPEL_ITEM = new FurnitureItem(LADSTAPEL, "ladstapel", item("ladstapel").component(BODY, "white_concrete"));
        //?} else {
        /*BOKWURM_ITEM = new FurnitureItem(BOKWURM, "bokwurm", item("bokwurm"));
        LADSTAPEL_ITEM = new FurnitureItem(LADSTAPEL, "ladstapel", item("ladstapel"));
        *///?}
        r.accept(MuchMoreStorage.id("bokwurm"), BOKWURM_ITEM);
        r.accept(MuchMoreStorage.id("ladstapel"), LADSTAPEL_ITEM);
    }

    public static final TagKey<Item> WORKTOP_TAG = TagKey.create(Registries.ITEM, MuchMoreStorage.id("c", "worktops"));
    public static final TagKey<Item> BOKWURM_BOOKS = TagKey.create(Registries.ITEM, MuchMoreStorage.id("bokwurm_books"));
    public static final TagKey<Item> SCREWDRIVERS = TagKey.create(Registries.ITEM, MuchMoreStorage.id("c", "tools/screwdriver"));

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
        //? if >=1.20.5 {
        stack.set(BODY, body);
        stack.set(TOP, top);
        //?} else {
        /*stack.getOrCreateTag().putString("Body", body);
        stack.getOrCreateTag().putString("Top", top);
        *///?}
        return stack;
    }

    public static ItemStack kubhala(String body) {
        return furniture(KUBHALA_ITEM, body);
    }

    public static ItemStack furniture(Item item, String body) {
        ItemStack stack = new ItemStack(item);
        //? if >=1.20.5 {
        stack.set(BODY, body);
        //?} else
        /*stack.getOrCreateTag().putString("Body", body);*/
        return stack;
    }

    public static ItemStack insert(Insert kind) {
        return switch (kind) {
            case DRAWER -> new ItemStack(DRAWER_INSERT);
            case DOOR -> new ItemStack(DOOR_INSERT);
            default -> ItemStack.EMPTY;
        };
    }

    public static String bodyOf(ItemStack stack) {
        //? if >=1.20.5 {
        String old = legacy(stack.get(DataComponents.CUSTOM_DATA), "Body");
        return old != null ? old : stack.getOrDefault(BODY, "oak");
        //?} else
        /*return legacy(stack.getTag(), "Body", "oak");*/
    }

    public static String cupboardTopOf(ItemStack stack) {
        //? if >=1.20.5 {
        String old = legacy(stack.get(DataComponents.CUSTOM_DATA), "Top");
        return old != null ? old : stack.getOrDefault(TOP, "oak");
        //?} else
        /*return legacy(stack.getTag(), "Top", "oak");*/
    }

    // 1.20.1 kept the finishes as item nbt, which becomes custom data when a world is upgraded
    //? if >=1.20.5 {
    public static @Nullable String legacy(@Nullable CustomData data, String key) {
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        //? if >=1.21.5 {
        return tag.getString(key).orElse(null);
        //?} else
        /*return tag.contains(key) ? tag.getString(key) : null;*/
    }
    //?} else {
    /*private static String legacy(CompoundTag tag, String key, String fallback) {
        return tag != null && tag.contains(key) ? tag.getString(key) : fallback;
    }
    *///?}

    // book + screwdriver + chest gives the manual and the screwdriver comes back
    public static void registerRecipeSerializers(BiConsumer<Identifier, RecipeSerializer<?>> r) {
        r.accept(MuchMoreStorage.id("crafting_shapeless_keep_tool"), KeepToolShapelessRecipe.SERIALIZER);
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> r) {
        //? if >=1.21.2 {
        CUPBOARD_BE = new BlockEntityType<>(CupboardBlockEntity::new, Set.of(CUPBOARD));
        //?} else
        /*CUPBOARD_BE = BlockEntityType.Builder.of(CupboardBlockEntity::new, CUPBOARD).build(null);*/
        r.accept(MuchMoreStorage.id("cupboard"), CUPBOARD_BE);
        //? if >=1.21.2 {
        KUBHALA_BE = new BlockEntityType<>(KubhalaBlockEntity::new, Set.of(KUBHALA));
        //?} else
        /*KUBHALA_BE = BlockEntityType.Builder.of(KubhalaBlockEntity::new, KUBHALA).build(null);*/
        r.accept(MuchMoreStorage.id("kubhala"), KUBHALA_BE);
        //? if >=1.21.2 {
        BOKWURM_BE = new BlockEntityType<>(BokwurmBlockEntity::new, Set.of(BOKWURM));
        LADSTAPEL_BE = new BlockEntityType<>(LadstapelBlockEntity::new, Set.of(LADSTAPEL));
        //?} else {
        /*BOKWURM_BE = BlockEntityType.Builder.of(BokwurmBlockEntity::new, BOKWURM).build(null);
        LADSTAPEL_BE = BlockEntityType.Builder.of(LadstapelBlockEntity::new, LADSTAPEL).build(null);
        *///?}
        r.accept(MuchMoreStorage.id("bokwurm"), BOKWURM_BE);
        r.accept(MuchMoreStorage.id("ladstapel"), LADSTAPEL_BE);
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
                    for (String body : KUBHALA_EXAMPLES) out.accept(kubhala(body));
                    out.accept(DRAWER_INSERT);
                    out.accept(DOOR_INSERT);
                    SAKKSAKKS.values().forEach(out::accept);
                    FLATPACKS.values().forEach(out::accept);
                    for (String body : KUBHALA_EXAMPLES) out.accept(furniture(BOKWURM_ITEM, body));
                    BOKWURM_FLATPACKS.values().forEach(out::accept);
                    for (String body : KUBHALA_EXAMPLES) out.accept(furniture(LADSTAPEL_ITEM, body));
                    LADSTAPEL_FLATPACKS.values().forEach(out::accept);
                })
                .build();
        r.accept(MuchMoreStorage.id("main"), TAB);
    }

    public static <T> BiConsumer<Identifier, T> direct(Registry<? super T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }

    public static void registerAllDirect() {
        //? if >=1.20.5
        registerDataComponents(direct(BuiltInRegistries.DATA_COMPONENT_TYPE));
        registerBlocks(direct(BuiltInRegistries.BLOCK));
        registerItems(direct(BuiltInRegistries.ITEM));
        registerBlockEntities(direct(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        registerRecipeSerializers(direct(BuiltInRegistries.RECIPE_SERIALIZER));
        registerCreativeTabs(direct(BuiltInRegistries.CREATIVE_MODE_TAB));
    }
}
