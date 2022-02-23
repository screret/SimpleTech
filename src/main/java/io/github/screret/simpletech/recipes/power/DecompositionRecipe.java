package io.github.screret.simpletech.recipes.power;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.energy.PowergenMachineType;
import io.github.screret.simpletech.registry.ModRegistry;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Locale;

public class DecompositionRecipe implements Recipe<Container> {
    protected ResourceLocation id;
    final String group;
    final int result;
    final int usageTime;
    final Ingredient ingredient;

    public DecompositionRecipe(ResourceLocation resourceLocation, String group, int result, int usageTime, Ingredient ingredient) {
        this.id = resourceLocation;
        this.group = group;
        this.result = result;
        this.usageTime = usageTime;
        this.ingredient = ingredient;
    }

    @Override
    public boolean matches(Container cont, Level level) {
        return this.ingredient.test(cont.getItem(0));
    }

    @Override
    public ItemStack assemble(Container cont) {
        return new ItemStack(Items.AIR);
    }

    @Override
    public boolean canCraftInDimensions(int xSize, int ySize) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(Items.AIR);
    }

    public int getUsageTime(){
        return usageTime;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(this.ingredient);
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.DECOMPOSITION_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRegistry.DECOMPOSITION_RECIPE_TYPE;
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<DecompositionRecipe> {
        private static final ResourceLocation NAME = new ResourceLocation(SimpleTech.MOD_ID, PowergenMachineType.rot.toString().toLowerCase(Locale.ROOT));
        public DecompositionRecipe fromJson(ResourceLocation recipe, JsonObject json) {
            String s = GsonHelper.getAsString(json, "group", "");
            int usageTime = GsonHelper.getAsInt(json, "time", 100);
            Ingredient ingredient;
            if (GsonHelper.isArrayNode(json, "ingredient")) {
                ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(json, "ingredient"));
            } else {
                ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            }
            int result = GsonHelper.getAsInt(json, "output");
            return new DecompositionRecipe(recipe, s, result, usageTime, ingredient);
        }

        public DecompositionRecipe fromNetwork(ResourceLocation recipe, FriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            int time = buffer.readInt();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);

            int output = buffer.readInt();
            return new DecompositionRecipe(recipe, s, output, time, ingredient);
        }

        public void toNetwork(FriendlyByteBuf buffer, DecompositionRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeInt(recipe.usageTime);
            recipe.ingredient.toNetwork(buffer);

            buffer.writeInt(recipe.result);
        }
    }
}
