package net.minecraft.recipe.book;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;

public final class RecipeBookOptions {
   private static final Map<RecipeBookCategory, Pair<String, String>> CATEGORY_OPTION_NAMES;
   private final Map<RecipeBookCategory, RecipeBookOptions.CategoryOption> categoryOptions;

   private RecipeBookOptions(Map<RecipeBookCategory, RecipeBookOptions.CategoryOption> categoryOptions) {
      this.categoryOptions = categoryOptions;
   }

   public RecipeBookOptions() {
      this((Map)Util.make(Maps.newEnumMap(RecipeBookCategory.class), (enumMap) -> {
         RecipeBookCategory[] var1 = RecipeBookCategory.values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            RecipeBookCategory recipeBookCategory = var1[var3];
            enumMap.put(recipeBookCategory, new RecipeBookOptions.CategoryOption(false, false));
         }

      }));
   }

   @Environment(EnvType.CLIENT)
   public boolean isGuiOpen(RecipeBookCategory category) {
      return ((RecipeBookOptions.CategoryOption)this.categoryOptions.get(category)).guiOpen;
   }

   public void setGuiOpen(RecipeBookCategory category, boolean open) {
      ((RecipeBookOptions.CategoryOption)this.categoryOptions.get(category)).guiOpen = open;
   }

   @Environment(EnvType.CLIENT)
   public boolean isFilteringCraftable(RecipeBookCategory category) {
      return ((RecipeBookOptions.CategoryOption)this.categoryOptions.get(category)).filteringCraftable;
   }

   public void setFilteringCraftable(RecipeBookCategory category, boolean filtering) {
      ((RecipeBookOptions.CategoryOption)this.categoryOptions.get(category)).filteringCraftable = filtering;
   }

   public static RecipeBookOptions fromPacket(PacketByteBuf buf) {
      Map<RecipeBookCategory, RecipeBookOptions.CategoryOption> map = Maps.newEnumMap(RecipeBookCategory.class);
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory recipeBookCategory = var2[var4];
         boolean bl = buf.readBoolean();
         boolean bl2 = buf.readBoolean();
         map.put(recipeBookCategory, new RecipeBookOptions.CategoryOption(bl, bl2));
      }

      return new RecipeBookOptions(map);
   }

   public void toPacket(PacketByteBuf buf) {
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory recipeBookCategory = var2[var4];
         RecipeBookOptions.CategoryOption categoryOption = (RecipeBookOptions.CategoryOption)this.categoryOptions.get(recipeBookCategory);
         if (categoryOption == null) {
            buf.writeBoolean(false);
            buf.writeBoolean(false);
         } else {
            buf.writeBoolean(categoryOption.guiOpen);
            buf.writeBoolean(categoryOption.filteringCraftable);
         }
      }

   }

   public static RecipeBookOptions fromNbt(NbtCompound nbt) {
      Map<RecipeBookCategory, RecipeBookOptions.CategoryOption> map = Maps.newEnumMap(RecipeBookCategory.class);
      CATEGORY_OPTION_NAMES.forEach((recipeBookCategory, pair) -> {
         boolean bl = nbt.getBoolean((String)pair.getFirst());
         boolean bl2 = nbt.getBoolean((String)pair.getSecond());
         map.put(recipeBookCategory, new RecipeBookOptions.CategoryOption(bl, bl2));
      });
      return new RecipeBookOptions(map);
   }

   public void writeNbt(NbtCompound nbt) {
      CATEGORY_OPTION_NAMES.forEach((recipeBookCategory, pair) -> {
         RecipeBookOptions.CategoryOption categoryOption = (RecipeBookOptions.CategoryOption)this.categoryOptions.get(recipeBookCategory);
         nbt.putBoolean((String)pair.getFirst(), categoryOption.guiOpen);
         nbt.putBoolean((String)pair.getSecond(), categoryOption.filteringCraftable);
      });
   }

   public RecipeBookOptions copy() {
      Map<RecipeBookCategory, RecipeBookOptions.CategoryOption> map = Maps.newEnumMap(RecipeBookCategory.class);
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory recipeBookCategory = var2[var4];
         RecipeBookOptions.CategoryOption categoryOption = (RecipeBookOptions.CategoryOption)this.categoryOptions.get(recipeBookCategory);
         map.put(recipeBookCategory, categoryOption.copy());
      }

      return new RecipeBookOptions(map);
   }

   public void copyFrom(RecipeBookOptions other) {
      this.categoryOptions.clear();
      RecipeBookCategory[] var2 = RecipeBookCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         RecipeBookCategory recipeBookCategory = var2[var4];
         RecipeBookOptions.CategoryOption categoryOption = (RecipeBookOptions.CategoryOption)other.categoryOptions.get(recipeBookCategory);
         this.categoryOptions.put(recipeBookCategory, categoryOption.copy());
      }

   }

   public boolean equals(Object object) {
      return this == object || object instanceof RecipeBookOptions && this.categoryOptions.equals(((RecipeBookOptions)object).categoryOptions);
   }

   public int hashCode() {
      return this.categoryOptions.hashCode();
   }

   static {
      CATEGORY_OPTION_NAMES = ImmutableMap.of(RecipeBookCategory.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookCategory.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookCategory.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookCategory.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"));
   }

   static final class CategoryOption {
      private boolean guiOpen;
      private boolean filteringCraftable;

      public CategoryOption(boolean guiOpen, boolean filteringCraftable) {
         this.guiOpen = guiOpen;
         this.filteringCraftable = filteringCraftable;
      }

      public RecipeBookOptions.CategoryOption copy() {
         return new RecipeBookOptions.CategoryOption(this.guiOpen, this.filteringCraftable);
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (!(object instanceof RecipeBookOptions.CategoryOption)) {
            return false;
         } else {
            RecipeBookOptions.CategoryOption categoryOption = (RecipeBookOptions.CategoryOption)object;
            return this.guiOpen == categoryOption.guiOpen && this.filteringCraftable == categoryOption.filteringCraftable;
         }
      }

      public int hashCode() {
         int i = this.guiOpen ? 1 : 0;
         i = 31 * i + (this.filteringCraftable ? 1 : 0);
         return i;
      }

      public String toString() {
         return "[open=" + this.guiOpen + ", filtering=" + this.filteringCraftable + ']';
      }
   }
}
