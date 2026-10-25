package com.workshopcraft.muchmorestorage.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A shapeless crafting recipe that hands back whatever matched "keep" (a tool) instead of using it up.
 * Plain vanilla code, so it works the same on NeoForge and Fabric. To reuse it in another mod, copy this
 * file, change the package and register SERIALIZER under that mod's id. The json is a normal
 * minecraft:crafting_shapeless recipe with the type swapped and one extra field:
 * "keep": "#c:tools/screwdriver". The kept ingredient must also be listed in "ingredients".
 * On Fabric also call RecipeSynchronization.synchronizeRecipeSerializer(SERIALIZER) so recipe viewers see it.
 */
public class KeepToolShapelessRecipe extends NormalCraftingRecipe {
    public static final MapCodec<KeepToolShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
                    Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(o -> o.ingredients),
                    Ingredient.CODEC.fieldOf("keep").forGetter(o -> o.keep)
            ).apply(i, KeepToolShapelessRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KeepToolShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ItemStackTemplate.STREAM_CODEC, o -> o.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredients,
            Ingredient.CONTENTS_STREAM_CODEC, o -> o.keep,
            KeepToolShapelessRecipe::new);
    public static final RecipeSerializer<KeepToolShapelessRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final Ingredient keep;

    public KeepToolShapelessRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                   ItemStackTemplate result, List<Ingredient> ingredients, Ingredient keep) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
        this.keep = keep;
    }

    @Override
    public RecipeSerializer<KeepToolShapelessRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != ingredients.size()) return false;
        return input.size() == 1 && ingredients.size() == 1
                ? ingredients.getFirst().test(input.getItem(0))
                : input.stackedContents().canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> left = CraftingRecipe.defaultCraftingReminder(input);
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

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                ingredients.stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }
}
