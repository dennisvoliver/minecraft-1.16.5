package net.minecraft.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BlastingRecipe extends AbstractCookingRecipe {
   public BlastingRecipe(Identifier id, String group, Ingredient input, ItemStack output, float experience, int cookTime) {
      super(RecipeType.BLASTING, id, group, input, output, experience, cookTime);
   }

   @Environment(EnvType.CLIENT)
   public ItemStack createIcon() {
      return new ItemStack(Blocks.BLAST_FURNACE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BLASTING;
   }
}
