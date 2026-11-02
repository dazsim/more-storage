package com.workshopcraft.muchmorestorage.crafting.v1_20;

//? if <1.20.5 {
/*import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

// 1.20.1 form of the 26.x KeepToolShapelessRecipe, reads the result "nbt" itself so it works without loader help
public class KeepToolShapelessRecipe implements CraftingRecipe {
    public static final RecipeSerializer<KeepToolShapelessRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public KeepToolShapelessRecipe fromJson(Identifier id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = category(GsonHelper.getAsString(json, "category", "misc"));
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (JsonElement e : GsonHelper.getAsJsonArray(json, "ingredients")) {
                Ingredient in = Ingredient.fromJson(e);
                if (!in.isEmpty()) ingredients.add(in);
            }
            if (ingredients.isEmpty()) throw new JsonParseException("No ingredients for shapeless recipe");
            Ingredient keep = Ingredient.fromJson(json.get("keep"));
            return new KeepToolShapelessRecipe(id, group, category, result(GsonHelper.getAsJsonObject(json, "result")), ingredients, keep);
        }

        @Override
        public KeepToolShapelessRecipe fromNetwork(Identifier id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            int n = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < n; i++) ingredients.add(Ingredient.fromNetwork(buf));
            Ingredient keep = Ingredient.fromNetwork(buf);
            return new KeepToolShapelessRecipe(id, group, category, buf.readItem(), ingredients, keep);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, KeepToolShapelessRecipe r) {
            buf.writeUtf(r.group);
            buf.writeEnum(r.category);
            buf.writeVarInt(r.ingredients.size());
            for (Ingredient in : r.ingredients) in.toNetwork(buf);
            r.keep.toNetwork(buf);
            buf.writeItem(r.result);
        }
    };

    private static CraftingBookCategory category(String name) {
        for (CraftingBookCategory c : CraftingBookCategory.values()) if (c.getSerializedName().equals(name)) return c;
        return CraftingBookCategory.MISC;
    }

    private static ItemStack result(JsonObject json) {
        String id = GsonHelper.getAsString(json, "item");
        Item item = BuiltInRegistries.ITEM.getOptional(new Identifier(id))
                .orElseThrow(() -> new JsonParseException("Unknown item '" + id + "'"));
        ItemStack stack = new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
        JsonElement nbt = json.get("nbt");
        if (nbt != null) {
            try {
                CompoundTag tag = nbt.isJsonObject()
                        ? (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, nbt)
                        : TagParser.parseTag(GsonHelper.convertToString(nbt, "nbt"));
                stack.setTag(tag);
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid nbt: " + e.getMessage());
            }
        }
        return stack;
    }

    private final Identifier id;
    private final String group;
    private final CraftingBookCategory category;
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final Ingredient keep;

    public KeepToolShapelessRecipe(Identifier id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, Ingredient keep) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        this.keep = keep;
    }

    @Override
    public Identifier getId() { return id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }

    @Override
    public String getGroup() { return group; }

    @Override
    public CraftingBookCategory category() { return category; }

    @Override
    public NonNullList<Ingredient> getIngredients() { return ingredients; }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        StackedContents contents = new StackedContents();
        int count = 0;
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!stack.isEmpty()) {
                count++;
                contents.accountStack(stack, 1);
            }
        }
        return count == ingredients.size() && contents.canCraft(this, null);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) { return result.copy(); }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) { return result; }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
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
