package net.minecraft.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
   public CampfireCookingRecipe(Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
      super(RecipeType.CAMPFIRE_COOKING, id, group, input, output, experience, cookTime);
   }

   @Environment(EnvType.CLIENT)
   public ItemStack createIcon() {
      return new ItemStack(Blocks.CAMPFIRE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.CAMPFIRE_COOKING;
   }
}
