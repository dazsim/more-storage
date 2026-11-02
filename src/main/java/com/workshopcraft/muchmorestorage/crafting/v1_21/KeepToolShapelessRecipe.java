package com.workshopcraft.muchmorestorage.crafting.v1_21;

//? if >=1.21 && <26.1 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

// 1.21.1 form of the 26.x KeepToolShapelessRecipe, same json apart from the version's ingredient and result format
public class KeepToolShapelessRecipe implements CraftingRecipe {
    public static final MapCodec<KeepToolShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(o -> o.group),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(o -> o.category),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(o -> o.result),
                    Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(o -> o.ingredients),
                    Ingredient.CODEC_NONEMPTY.fieldOf("keep").forGetter(o -> o.keep)
            ).apply(i, KeepToolShapelessRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KeepToolShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, o -> o.group,
            CraftingBookCategory.STREAM_CODEC, o -> o.category,
            ItemStack.STREAM_CODEC, o -> o.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredients,
            Ingredient.CONTENTS_STREAM_CODEC, o -> o.keep,
            KeepToolShapelessRecipe::new);
    public static final RecipeSerializer<KeepToolShapelessRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<KeepToolShapelessRecipe> codec() { return MAP_CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, KeepToolShapelessRecipe> streamCodec() { return STREAM_CODEC; }
    };

    private final String group;
    private final CraftingBookCategory category;
    private final ItemStack result;
    private final List<Ingredient> ingredients;
    private final Ingredient keep;

    public KeepToolShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, List<Ingredient> ingredients, Ingredient keep) {
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        this.keep = keep;
    }

    @Override
    public RecipeSerializer<KeepToolShapelessRecipe> getSerializer() { return SERIALIZER; }

    @Override
    public String getGroup() { return group; }

    @Override
    public CraftingBookCategory category() { return category; }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != ingredients.size()) return false;
        return input.size() == 1 && ingredients.size() == 1
                ? ingredients.getFirst().test(input.getItem(0))
                : input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return result.copy(); }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> left = CraftingRecipe.super.getRemainingItems(input);
        boolean kept = false;
        for (int slot = 0; slot < left.size(); slot++) {
            ItemStack in = input.getItem(slot);
            if (!kept && !in.isEmpty() && keep.test(in)) {
                left.set(slot, in.copyWithCount(1));
                kept = true;
            }
        }
        return left;
    }
}
*///?}
