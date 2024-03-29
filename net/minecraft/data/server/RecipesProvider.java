package net.minecraft.data.server;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.server.recipe.ComplexRecipeJsonFactory;
import net.minecraft.data.server.recipe.CookingRecipeJsonFactory;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonFactory;
import net.minecraft.data.server.recipe.SingleItemRecipeJsonFactory;
import net.minecraft.data.server.recipe.SmithingRecipeJsonFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipesProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator root;

   public RecipesProvider(DataGenerator dataGenerator) {
      this.root = dataGenerator;
   }

   public void run(DataCache cache) throws IOException {
      Path path = this.root.getOutput();
      Set<Identifier> set = Sets.newHashSet();
      generate((recipeJsonProvider) -> {
         if (!set.add(recipeJsonProvider.getRecipeId())) {
            throw new IllegalStateException("Duplicate recipe " + recipeJsonProvider.getRecipeId());
         } else {
            saveRecipe(cache, recipeJsonProvider.toJson(), path.resolve("data/" + recipeJsonProvider.getRecipeId().getNamespace() + "/recipes/" + recipeJsonProvider.getRecipeId().getPath() + ".json"));
            JsonObject jsonObject = recipeJsonProvider.toAdvancementJson();
            if (jsonObject != null) {
               saveRecipeAdvancement(cache, jsonObject, path.resolve("data/" + recipeJsonProvider.getRecipeId().getNamespace() + "/advancements/" + recipeJsonProvider.getAdvancementId().getPath() + ".json"));
            }

         }
      });
      saveRecipeAdvancement(cache, Advancement.Task.create().criterion("impossible", (CriterionConditions)(new ImpossibleCriterion.Conditions())).toJson(), path.resolve("data/minecraft/advancements/recipes/root.json"));
   }

   private static void saveRecipe(DataCache cache, JsonObject json, Path path) {
      try {
         String string = GSON.toJson((JsonElement)json);
         String string2 = SHA1.hashUnencodedChars(string).toString();
         if (!Objects.equals(cache.getOldSha1(path), string2) || !Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path.getParent());
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
            Throwable var6 = null;

            try {
               bufferedWriter.write(string);
            } catch (Throwable var16) {
               var6 = var16;
               throw var16;
            } finally {
               if (bufferedWriter != null) {
                  if (var6 != null) {
                     try {
                        bufferedWriter.close();
                     } catch (Throwable var15) {
                        var6.addSuppressed(var15);
                     }
                  } else {
                     bufferedWriter.close();
                  }
               }

            }
         }

         cache.updateSha1(path, string2);
      } catch (IOException var18) {
         LOGGER.error((String)"Couldn't save recipe {}", (Object)path, (Object)var18);
      }

   }

   private static void saveRecipeAdvancement(DataCache cache, JsonObject json, Path path) {
      try {
         String string = GSON.toJson((JsonElement)json);
         String string2 = SHA1.hashUnencodedChars(string).toString();
         if (!Objects.equals(cache.getOldSha1(path), string2) || !Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path.getParent());
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
            Throwable var6 = null;

            try {
               bufferedWriter.write(string);
            } catch (Throwable var16) {
               var6 = var16;
               throw var16;
            } finally {
               if (bufferedWriter != null) {
                  if (var6 != null) {
                     try {
                        bufferedWriter.close();
                     } catch (Throwable var15) {
                        var6.addSuppressed(var15);
                     }
                  } else {
                     bufferedWriter.close();
                  }
               }

            }
         }

         cache.updateSha1(path, string2);
      } catch (IOException var18) {
         LOGGER.error((String)"Couldn't save recipe advancement {}", (Object)path, (Object)var18);
      }

   }

   private static void generate(Consumer<RecipeJsonProvider> exporter) {
      method_24475(exporter, Blocks.ACACIA_PLANKS, ItemTags.ACACIA_LOGS);
      method_24477(exporter, Blocks.BIRCH_PLANKS, ItemTags.BIRCH_LOGS);
      method_24477(exporter, Blocks.CRIMSON_PLANKS, ItemTags.CRIMSON_STEMS);
      method_24475(exporter, Blocks.DARK_OAK_PLANKS, ItemTags.DARK_OAK_LOGS);
      method_24477(exporter, Blocks.JUNGLE_PLANKS, ItemTags.JUNGLE_LOGS);
      method_24477(exporter, Blocks.OAK_PLANKS, ItemTags.OAK_LOGS);
      method_24477(exporter, Blocks.SPRUCE_PLANKS, ItemTags.SPRUCE_LOGS);
      method_24477(exporter, Blocks.WARPED_PLANKS, ItemTags.WARPED_STEMS);
      method_24476(exporter, Blocks.ACACIA_WOOD, Blocks.ACACIA_LOG);
      method_24476(exporter, Blocks.BIRCH_WOOD, Blocks.BIRCH_LOG);
      method_24476(exporter, Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_LOG);
      method_24476(exporter, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LOG);
      method_24476(exporter, Blocks.OAK_WOOD, Blocks.OAK_LOG);
      method_24476(exporter, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LOG);
      method_24476(exporter, Blocks.CRIMSON_HYPHAE, Blocks.CRIMSON_STEM);
      method_24476(exporter, Blocks.WARPED_HYPHAE, Blocks.WARPED_STEM);
      method_24476(exporter, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG);
      method_24476(exporter, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG);
      method_24476(exporter, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG);
      method_24476(exporter, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG);
      method_24476(exporter, Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_OAK_LOG);
      method_24476(exporter, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG);
      method_24476(exporter, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_STEM);
      method_24476(exporter, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.STRIPPED_WARPED_STEM);
      method_24478(exporter, Items.ACACIA_BOAT, Blocks.ACACIA_PLANKS);
      method_24478(exporter, Items.BIRCH_BOAT, Blocks.BIRCH_PLANKS);
      method_24478(exporter, Items.DARK_OAK_BOAT, Blocks.DARK_OAK_PLANKS);
      method_24478(exporter, Items.JUNGLE_BOAT, Blocks.JUNGLE_PLANKS);
      method_24478(exporter, Items.OAK_BOAT, Blocks.OAK_PLANKS);
      method_24478(exporter, Items.SPRUCE_BOAT, Blocks.SPRUCE_PLANKS);
      method_24479(exporter, Blocks.ACACIA_BUTTON, Blocks.ACACIA_PLANKS);
      method_24480(exporter, Blocks.ACACIA_DOOR, Blocks.ACACIA_PLANKS);
      method_24481(exporter, Blocks.ACACIA_FENCE, Blocks.ACACIA_PLANKS);
      method_24482(exporter, Blocks.ACACIA_FENCE_GATE, Blocks.ACACIA_PLANKS);
      method_24483(exporter, Blocks.ACACIA_PRESSURE_PLATE, Blocks.ACACIA_PLANKS);
      method_24484(exporter, Blocks.ACACIA_SLAB, Blocks.ACACIA_PLANKS);
      method_24485(exporter, Blocks.ACACIA_STAIRS, Blocks.ACACIA_PLANKS);
      method_24486(exporter, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_PLANKS);
      method_24883(exporter, Blocks.ACACIA_SIGN, Blocks.ACACIA_PLANKS);
      method_24479(exporter, Blocks.BIRCH_BUTTON, Blocks.BIRCH_PLANKS);
      method_24480(exporter, Blocks.BIRCH_DOOR, Blocks.BIRCH_PLANKS);
      method_24481(exporter, Blocks.BIRCH_FENCE, Blocks.BIRCH_PLANKS);
      method_24482(exporter, Blocks.BIRCH_FENCE_GATE, Blocks.BIRCH_PLANKS);
      method_24483(exporter, Blocks.BIRCH_PRESSURE_PLATE, Blocks.BIRCH_PLANKS);
      method_24484(exporter, Blocks.BIRCH_SLAB, Blocks.BIRCH_PLANKS);
      method_24485(exporter, Blocks.BIRCH_STAIRS, Blocks.BIRCH_PLANKS);
      method_24486(exporter, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_PLANKS);
      method_24883(exporter, Blocks.BIRCH_SIGN, Blocks.BIRCH_PLANKS);
      method_24479(exporter, Blocks.CRIMSON_BUTTON, Blocks.CRIMSON_PLANKS);
      method_24480(exporter, Blocks.CRIMSON_DOOR, Blocks.CRIMSON_PLANKS);
      method_24481(exporter, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_PLANKS);
      method_24482(exporter, Blocks.CRIMSON_FENCE_GATE, Blocks.CRIMSON_PLANKS);
      method_24483(exporter, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.CRIMSON_PLANKS);
      method_24484(exporter, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_PLANKS);
      method_24485(exporter, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_PLANKS);
      method_24486(exporter, Blocks.CRIMSON_TRAPDOOR, Blocks.CRIMSON_PLANKS);
      method_24883(exporter, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_PLANKS);
      method_24479(exporter, Blocks.DARK_OAK_BUTTON, Blocks.DARK_OAK_PLANKS);
      method_24480(exporter, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_PLANKS);
      method_24481(exporter, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_PLANKS);
      method_24482(exporter, Blocks.DARK_OAK_FENCE_GATE, Blocks.DARK_OAK_PLANKS);
      method_24483(exporter, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.DARK_OAK_PLANKS);
      method_24484(exporter, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_PLANKS);
      method_24485(exporter, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS);
      method_24486(exporter, Blocks.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_PLANKS);
      method_24883(exporter, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_PLANKS);
      method_24479(exporter, Blocks.JUNGLE_BUTTON, Blocks.JUNGLE_PLANKS);
      method_24480(exporter, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_PLANKS);
      method_24481(exporter, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_PLANKS);
      method_24482(exporter, Blocks.JUNGLE_FENCE_GATE, Blocks.JUNGLE_PLANKS);
      method_24483(exporter, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.JUNGLE_PLANKS);
      method_24484(exporter, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_PLANKS);
      method_24485(exporter, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_PLANKS);
      method_24486(exporter, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_PLANKS);
      method_24883(exporter, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_PLANKS);
      method_24479(exporter, Blocks.OAK_BUTTON, Blocks.OAK_PLANKS);
      method_24480(exporter, Blocks.OAK_DOOR, Blocks.OAK_PLANKS);
      method_24481(exporter, Blocks.OAK_FENCE, Blocks.OAK_PLANKS);
      method_24482(exporter, Blocks.OAK_FENCE_GATE, Blocks.OAK_PLANKS);
      method_24483(exporter, Blocks.OAK_PRESSURE_PLATE, Blocks.OAK_PLANKS);
      method_24484(exporter, Blocks.OAK_SLAB, Blocks.OAK_PLANKS);
      method_24485(exporter, Blocks.OAK_STAIRS, Blocks.OAK_PLANKS);
      method_24486(exporter, Blocks.OAK_TRAPDOOR, Blocks.OAK_PLANKS);
      method_24883(exporter, Blocks.OAK_SIGN, Blocks.OAK_PLANKS);
      method_24479(exporter, Blocks.SPRUCE_BUTTON, Blocks.SPRUCE_PLANKS);
      method_24480(exporter, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_PLANKS);
      method_24481(exporter, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_PLANKS);
      method_24482(exporter, Blocks.SPRUCE_FENCE_GATE, Blocks.SPRUCE_PLANKS);
      method_24483(exporter, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.SPRUCE_PLANKS);
      method_24484(exporter, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS);
      method_24485(exporter, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS);
      method_24486(exporter, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_PLANKS);
      method_24883(exporter, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_PLANKS);
      method_24479(exporter, Blocks.WARPED_BUTTON, Blocks.WARPED_PLANKS);
      method_24480(exporter, Blocks.WARPED_DOOR, Blocks.WARPED_PLANKS);
      method_24481(exporter, Blocks.WARPED_FENCE, Blocks.WARPED_PLANKS);
      method_24482(exporter, Blocks.WARPED_FENCE_GATE, Blocks.WARPED_PLANKS);
      method_24483(exporter, Blocks.WARPED_PRESSURE_PLATE, Blocks.WARPED_PLANKS);
      method_24484(exporter, Blocks.WARPED_SLAB, Blocks.WARPED_PLANKS);
      method_24485(exporter, Blocks.WARPED_STAIRS, Blocks.WARPED_PLANKS);
      method_24486(exporter, Blocks.WARPED_TRAPDOOR, Blocks.WARPED_PLANKS);
      method_24883(exporter, Blocks.WARPED_SIGN, Blocks.WARPED_PLANKS);
      method_24884(exporter, Blocks.BLACK_WOOL, Items.BLACK_DYE);
      method_24885(exporter, Blocks.BLACK_CARPET, Blocks.BLACK_WOOL);
      method_24886(exporter, Blocks.BLACK_CARPET, Items.BLACK_DYE);
      method_24887(exporter, Items.BLACK_BED, Blocks.BLACK_WOOL);
      method_24888(exporter, Items.BLACK_BED, Items.BLACK_DYE);
      method_24889(exporter, Items.BLACK_BANNER, Blocks.BLACK_WOOL);
      method_24884(exporter, Blocks.BLUE_WOOL, Items.BLUE_DYE);
      method_24885(exporter, Blocks.BLUE_CARPET, Blocks.BLUE_WOOL);
      method_24886(exporter, Blocks.BLUE_CARPET, Items.BLUE_DYE);
      method_24887(exporter, Items.BLUE_BED, Blocks.BLUE_WOOL);
      method_24888(exporter, Items.BLUE_BED, Items.BLUE_DYE);
      method_24889(exporter, Items.BLUE_BANNER, Blocks.BLUE_WOOL);
      method_24884(exporter, Blocks.BROWN_WOOL, Items.BROWN_DYE);
      method_24885(exporter, Blocks.BROWN_CARPET, Blocks.BROWN_WOOL);
      method_24886(exporter, Blocks.BROWN_CARPET, Items.BROWN_DYE);
      method_24887(exporter, Items.BROWN_BED, Blocks.BROWN_WOOL);
      method_24888(exporter, Items.BROWN_BED, Items.BROWN_DYE);
      method_24889(exporter, Items.BROWN_BANNER, Blocks.BROWN_WOOL);
      method_24884(exporter, Blocks.CYAN_WOOL, Items.CYAN_DYE);
      method_24885(exporter, Blocks.CYAN_CARPET, Blocks.CYAN_WOOL);
      method_24886(exporter, Blocks.CYAN_CARPET, Items.CYAN_DYE);
      method_24887(exporter, Items.CYAN_BED, Blocks.CYAN_WOOL);
      method_24888(exporter, Items.CYAN_BED, Items.CYAN_DYE);
      method_24889(exporter, Items.CYAN_BANNER, Blocks.CYAN_WOOL);
      method_24884(exporter, Blocks.GRAY_WOOL, Items.GRAY_DYE);
      method_24885(exporter, Blocks.GRAY_CARPET, Blocks.GRAY_WOOL);
      method_24886(exporter, Blocks.GRAY_CARPET, Items.GRAY_DYE);
      method_24887(exporter, Items.GRAY_BED, Blocks.GRAY_WOOL);
      method_24888(exporter, Items.GRAY_BED, Items.GRAY_DYE);
      method_24889(exporter, Items.GRAY_BANNER, Blocks.GRAY_WOOL);
      method_24884(exporter, Blocks.GREEN_WOOL, Items.GREEN_DYE);
      method_24885(exporter, Blocks.GREEN_CARPET, Blocks.GREEN_WOOL);
      method_24886(exporter, Blocks.GREEN_CARPET, Items.GREEN_DYE);
      method_24887(exporter, Items.GREEN_BED, Blocks.GREEN_WOOL);
      method_24888(exporter, Items.GREEN_BED, Items.GREEN_DYE);
      method_24889(exporter, Items.GREEN_BANNER, Blocks.GREEN_WOOL);
      method_24884(exporter, Blocks.LIGHT_BLUE_WOOL, Items.LIGHT_BLUE_DYE);
      method_24885(exporter, Blocks.LIGHT_BLUE_CARPET, Blocks.LIGHT_BLUE_WOOL);
      method_24886(exporter, Blocks.LIGHT_BLUE_CARPET, Items.LIGHT_BLUE_DYE);
      method_24887(exporter, Items.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
      method_24888(exporter, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_DYE);
      method_24889(exporter, Items.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WOOL);
      method_24884(exporter, Blocks.LIGHT_GRAY_WOOL, Items.LIGHT_GRAY_DYE);
      method_24885(exporter, Blocks.LIGHT_GRAY_CARPET, Blocks.LIGHT_GRAY_WOOL);
      method_24886(exporter, Blocks.LIGHT_GRAY_CARPET, Items.LIGHT_GRAY_DYE);
      method_24887(exporter, Items.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
      method_24888(exporter, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_DYE);
      method_24889(exporter, Items.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WOOL);
      method_24884(exporter, Blocks.LIME_WOOL, Items.LIME_DYE);
      method_24885(exporter, Blocks.LIME_CARPET, Blocks.LIME_WOOL);
      method_24886(exporter, Blocks.LIME_CARPET, Items.LIME_DYE);
      method_24887(exporter, Items.LIME_BED, Blocks.LIME_WOOL);
      method_24888(exporter, Items.LIME_BED, Items.LIME_DYE);
      method_24889(exporter, Items.LIME_BANNER, Blocks.LIME_WOOL);
      method_24884(exporter, Blocks.MAGENTA_WOOL, Items.MAGENTA_DYE);
      method_24885(exporter, Blocks.MAGENTA_CARPET, Blocks.MAGENTA_WOOL);
      method_24886(exporter, Blocks.MAGENTA_CARPET, Items.MAGENTA_DYE);
      method_24887(exporter, Items.MAGENTA_BED, Blocks.MAGENTA_WOOL);
      method_24888(exporter, Items.MAGENTA_BED, Items.MAGENTA_DYE);
      method_24889(exporter, Items.MAGENTA_BANNER, Blocks.MAGENTA_WOOL);
      method_24884(exporter, Blocks.ORANGE_WOOL, Items.ORANGE_DYE);
      method_24885(exporter, Blocks.ORANGE_CARPET, Blocks.ORANGE_WOOL);
      method_24886(exporter, Blocks.ORANGE_CARPET, Items.ORANGE_DYE);
      method_24887(exporter, Items.ORANGE_BED, Blocks.ORANGE_WOOL);
      method_24888(exporter, Items.ORANGE_BED, Items.ORANGE_DYE);
      method_24889(exporter, Items.ORANGE_BANNER, Blocks.ORANGE_WOOL);
      method_24884(exporter, Blocks.PINK_WOOL, Items.PINK_DYE);
      method_24885(exporter, Blocks.PINK_CARPET, Blocks.PINK_WOOL);
      method_24886(exporter, Blocks.PINK_CARPET, Items.PINK_DYE);
      method_24887(exporter, Items.PINK_BED, Blocks.PINK_WOOL);
      method_24888(exporter, Items.PINK_BED, Items.PINK_DYE);
      method_24889(exporter, Items.PINK_BANNER, Blocks.PINK_WOOL);
      method_24884(exporter, Blocks.PURPLE_WOOL, Items.PURPLE_DYE);
      method_24885(exporter, Blocks.PURPLE_CARPET, Blocks.PURPLE_WOOL);
      method_24886(exporter, Blocks.PURPLE_CARPET, Items.PURPLE_DYE);
      method_24887(exporter, Items.PURPLE_BED, Blocks.PURPLE_WOOL);
      method_24888(exporter, Items.PURPLE_BED, Items.PURPLE_DYE);
      method_24889(exporter, Items.PURPLE_BANNER, Blocks.PURPLE_WOOL);
      method_24884(exporter, Blocks.RED_WOOL, Items.RED_DYE);
      method_24885(exporter, Blocks.RED_CARPET, Blocks.RED_WOOL);
      method_24886(exporter, Blocks.RED_CARPET, Items.RED_DYE);
      method_24887(exporter, Items.RED_BED, Blocks.RED_WOOL);
      method_24888(exporter, Items.RED_BED, Items.RED_DYE);
      method_24889(exporter, Items.RED_BANNER, Blocks.RED_WOOL);
      method_24885(exporter, Blocks.WHITE_CARPET, Blocks.WHITE_WOOL);
      method_24887(exporter, Items.WHITE_BED, Blocks.WHITE_WOOL);
      method_24889(exporter, Items.WHITE_BANNER, Blocks.WHITE_WOOL);
      method_24884(exporter, Blocks.YELLOW_WOOL, Items.YELLOW_DYE);
      method_24885(exporter, Blocks.YELLOW_CARPET, Blocks.YELLOW_WOOL);
      method_24886(exporter, Blocks.YELLOW_CARPET, Items.YELLOW_DYE);
      method_24887(exporter, Items.YELLOW_BED, Blocks.YELLOW_WOOL);
      method_24888(exporter, Items.YELLOW_BED, Items.YELLOW_DYE);
      method_24889(exporter, Items.YELLOW_BANNER, Blocks.YELLOW_WOOL);
      method_24890(exporter, Blocks.BLACK_STAINED_GLASS, Items.BLACK_DYE);
      method_24891(exporter, Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS);
      method_24892(exporter, Blocks.BLACK_STAINED_GLASS_PANE, Items.BLACK_DYE);
      method_24890(exporter, Blocks.BLUE_STAINED_GLASS, Items.BLUE_DYE);
      method_24891(exporter, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS);
      method_24892(exporter, Blocks.BLUE_STAINED_GLASS_PANE, Items.BLUE_DYE);
      method_24890(exporter, Blocks.BROWN_STAINED_GLASS, Items.BROWN_DYE);
      method_24891(exporter, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS);
      method_24892(exporter, Blocks.BROWN_STAINED_GLASS_PANE, Items.BROWN_DYE);
      method_24890(exporter, Blocks.CYAN_STAINED_GLASS, Items.CYAN_DYE);
      method_24891(exporter, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS);
      method_24892(exporter, Blocks.CYAN_STAINED_GLASS_PANE, Items.CYAN_DYE);
      method_24890(exporter, Blocks.GRAY_STAINED_GLASS, Items.GRAY_DYE);
      method_24891(exporter, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS);
      method_24892(exporter, Blocks.GRAY_STAINED_GLASS_PANE, Items.GRAY_DYE);
      method_24890(exporter, Blocks.GREEN_STAINED_GLASS, Items.GREEN_DYE);
      method_24891(exporter, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS);
      method_24892(exporter, Blocks.GREEN_STAINED_GLASS_PANE, Items.GREEN_DYE);
      method_24890(exporter, Blocks.LIGHT_BLUE_STAINED_GLASS, Items.LIGHT_BLUE_DYE);
      method_24891(exporter, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS);
      method_24892(exporter, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Items.LIGHT_BLUE_DYE);
      method_24890(exporter, Blocks.LIGHT_GRAY_STAINED_GLASS, Items.LIGHT_GRAY_DYE);
      method_24891(exporter, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS);
      method_24892(exporter, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Items.LIGHT_GRAY_DYE);
      method_24890(exporter, Blocks.LIME_STAINED_GLASS, Items.LIME_DYE);
      method_24891(exporter, Blocks.LIME_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS);
      method_24892(exporter, Blocks.LIME_STAINED_GLASS_PANE, Items.LIME_DYE);
      method_24890(exporter, Blocks.MAGENTA_STAINED_GLASS, Items.MAGENTA_DYE);
      method_24891(exporter, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS);
      method_24892(exporter, Blocks.MAGENTA_STAINED_GLASS_PANE, Items.MAGENTA_DYE);
      method_24890(exporter, Blocks.ORANGE_STAINED_GLASS, Items.ORANGE_DYE);
      method_24891(exporter, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS);
      method_24892(exporter, Blocks.ORANGE_STAINED_GLASS_PANE, Items.ORANGE_DYE);
      method_24890(exporter, Blocks.PINK_STAINED_GLASS, Items.PINK_DYE);
      method_24891(exporter, Blocks.PINK_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS);
      method_24892(exporter, Blocks.PINK_STAINED_GLASS_PANE, Items.PINK_DYE);
      method_24890(exporter, Blocks.PURPLE_STAINED_GLASS, Items.PURPLE_DYE);
      method_24891(exporter, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS);
      method_24892(exporter, Blocks.PURPLE_STAINED_GLASS_PANE, Items.PURPLE_DYE);
      method_24890(exporter, Blocks.RED_STAINED_GLASS, Items.RED_DYE);
      method_24891(exporter, Blocks.RED_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS);
      method_24892(exporter, Blocks.RED_STAINED_GLASS_PANE, Items.RED_DYE);
      method_24890(exporter, Blocks.WHITE_STAINED_GLASS, Items.WHITE_DYE);
      method_24891(exporter, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.WHITE_STAINED_GLASS);
      method_24892(exporter, Blocks.WHITE_STAINED_GLASS_PANE, Items.WHITE_DYE);
      method_24890(exporter, Blocks.YELLOW_STAINED_GLASS, Items.YELLOW_DYE);
      method_24891(exporter, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS);
      method_24892(exporter, Blocks.YELLOW_STAINED_GLASS_PANE, Items.YELLOW_DYE);
      method_24893(exporter, Blocks.BLACK_TERRACOTTA, Items.BLACK_DYE);
      method_24893(exporter, Blocks.BLUE_TERRACOTTA, Items.BLUE_DYE);
      method_24893(exporter, Blocks.BROWN_TERRACOTTA, Items.BROWN_DYE);
      method_24893(exporter, Blocks.CYAN_TERRACOTTA, Items.CYAN_DYE);
      method_24893(exporter, Blocks.GRAY_TERRACOTTA, Items.GRAY_DYE);
      method_24893(exporter, Blocks.GREEN_TERRACOTTA, Items.GREEN_DYE);
      method_24893(exporter, Blocks.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_DYE);
      method_24893(exporter, Blocks.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_DYE);
      method_24893(exporter, Blocks.LIME_TERRACOTTA, Items.LIME_DYE);
      method_24893(exporter, Blocks.MAGENTA_TERRACOTTA, Items.MAGENTA_DYE);
      method_24893(exporter, Blocks.ORANGE_TERRACOTTA, Items.ORANGE_DYE);
      method_24893(exporter, Blocks.PINK_TERRACOTTA, Items.PINK_DYE);
      method_24893(exporter, Blocks.PURPLE_TERRACOTTA, Items.PURPLE_DYE);
      method_24893(exporter, Blocks.RED_TERRACOTTA, Items.RED_DYE);
      method_24893(exporter, Blocks.WHITE_TERRACOTTA, Items.WHITE_DYE);
      method_24893(exporter, Blocks.YELLOW_TERRACOTTA, Items.YELLOW_DYE);
      method_24894(exporter, Blocks.BLACK_CONCRETE_POWDER, Items.BLACK_DYE);
      method_24894(exporter, Blocks.BLUE_CONCRETE_POWDER, Items.BLUE_DYE);
      method_24894(exporter, Blocks.BROWN_CONCRETE_POWDER, Items.BROWN_DYE);
      method_24894(exporter, Blocks.CYAN_CONCRETE_POWDER, Items.CYAN_DYE);
      method_24894(exporter, Blocks.GRAY_CONCRETE_POWDER, Items.GRAY_DYE);
      method_24894(exporter, Blocks.GREEN_CONCRETE_POWDER, Items.GREEN_DYE);
      method_24894(exporter, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_DYE);
      method_24894(exporter, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_DYE);
      method_24894(exporter, Blocks.LIME_CONCRETE_POWDER, Items.LIME_DYE);
      method_24894(exporter, Blocks.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_DYE);
      method_24894(exporter, Blocks.ORANGE_CONCRETE_POWDER, Items.ORANGE_DYE);
      method_24894(exporter, Blocks.PINK_CONCRETE_POWDER, Items.PINK_DYE);
      method_24894(exporter, Blocks.PURPLE_CONCRETE_POWDER, Items.PURPLE_DYE);
      method_24894(exporter, Blocks.RED_CONCRETE_POWDER, Items.RED_DYE);
      method_24894(exporter, Blocks.WHITE_CONCRETE_POWDER, Items.WHITE_DYE);
      method_24894(exporter, Blocks.YELLOW_CONCRETE_POWDER, Items.YELLOW_DYE);
      ShapedRecipeJsonFactory.create(Blocks.ACTIVATOR_RAIL, 6).input('#', (ItemConvertible)Blocks.REDSTONE_TORCH).input('S', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("XSX").pattern("X#X").pattern("XSX").criterion("has_rail", conditionsFromItem(Blocks.RAIL)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.ANDESITE, 2).input((ItemConvertible)Blocks.DIORITE).input((ItemConvertible)Blocks.COBBLESTONE).criterion("has_stone", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.ANVIL).input('I', (ItemConvertible)Blocks.IRON_BLOCK).input('i', (ItemConvertible)Items.IRON_INGOT).pattern("III").pattern(" i ").pattern("iii").criterion("has_iron_block", conditionsFromItem(Blocks.IRON_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.ARMOR_STAND).input('/', (ItemConvertible)Items.STICK).input('_', (ItemConvertible)Blocks.SMOOTH_STONE_SLAB).pattern("///").pattern(" / ").pattern("/_/").criterion("has_stone_slab", conditionsFromItem(Blocks.SMOOTH_STONE_SLAB)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.ARROW, 4).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.FLINT).input('Y', (ItemConvertible)Items.FEATHER).pattern("X").pattern("#").pattern("Y").criterion("has_feather", conditionsFromItem(Items.FEATHER)).criterion("has_flint", conditionsFromItem(Items.FLINT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BARREL, 1).input('P', (Tag)ItemTags.PLANKS).input('S', (Tag)ItemTags.WOODEN_SLABS).pattern("PSP").pattern("P P").pattern("PSP").criterion("has_planks", conditionsFromTag(ItemTags.PLANKS)).criterion("has_wood_slab", conditionsFromTag(ItemTags.WOODEN_SLABS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BEACON).input('S', (ItemConvertible)Items.NETHER_STAR).input('G', (ItemConvertible)Blocks.GLASS).input('O', (ItemConvertible)Blocks.OBSIDIAN).pattern("GGG").pattern("GSG").pattern("OOO").criterion("has_nether_star", conditionsFromItem(Items.NETHER_STAR)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BEEHIVE).input('P', (Tag)ItemTags.PLANKS).input('H', (ItemConvertible)Items.HONEYCOMB).pattern("PPP").pattern("HHH").pattern("PPP").criterion("has_honeycomb", conditionsFromItem(Items.HONEYCOMB)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BEETROOT_SOUP).input((ItemConvertible)Items.BOWL).input((ItemConvertible)Items.BEETROOT, 6).criterion("has_beetroot", conditionsFromItem(Items.BEETROOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BLACK_DYE).input((ItemConvertible)Items.INK_SAC).group("black_dye").criterion("has_ink_sac", conditionsFromItem(Items.INK_SAC)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BLACK_DYE).input((ItemConvertible)Blocks.WITHER_ROSE).group("black_dye").criterion("has_black_flower", conditionsFromItem(Blocks.WITHER_ROSE)).offerTo(exporter, "black_dye_from_wither_rose");
      ShapelessRecipeJsonFactory.create(Items.BLAZE_POWDER, 2).input((ItemConvertible)Items.BLAZE_ROD).criterion("has_blaze_rod", conditionsFromItem(Items.BLAZE_ROD)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BLUE_DYE).input((ItemConvertible)Items.LAPIS_LAZULI).group("blue_dye").criterion("has_lapis_lazuli", conditionsFromItem(Items.LAPIS_LAZULI)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BLUE_DYE).input((ItemConvertible)Blocks.CORNFLOWER).group("blue_dye").criterion("has_blue_flower", conditionsFromItem(Blocks.CORNFLOWER)).offerTo(exporter, "blue_dye_from_cornflower");
      ShapedRecipeJsonFactory.create(Blocks.BLUE_ICE).input('#', (ItemConvertible)Blocks.PACKED_ICE).pattern("###").pattern("###").pattern("###").criterion("has_packed_ice", conditionsFromItem(Blocks.PACKED_ICE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BONE_BLOCK).input('X', (ItemConvertible)Items.BONE_MEAL).pattern("XXX").pattern("XXX").pattern("XXX").criterion("has_bonemeal", conditionsFromItem(Items.BONE_MEAL)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BONE_MEAL, 3).input((ItemConvertible)Items.BONE).group("bonemeal").criterion("has_bone", conditionsFromItem(Items.BONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BONE_MEAL, 9).input((ItemConvertible)Blocks.BONE_BLOCK).group("bonemeal").criterion("has_bone_block", conditionsFromItem(Blocks.BONE_BLOCK)).offerTo(exporter, "bone_meal_from_bone_block");
      ShapelessRecipeJsonFactory.create(Items.BOOK).input((ItemConvertible)Items.PAPER, 3).input((ItemConvertible)Items.LEATHER).criterion("has_paper", conditionsFromItem(Items.PAPER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BOOKSHELF).input('#', (Tag)ItemTags.PLANKS).input('X', (ItemConvertible)Items.BOOK).pattern("###").pattern("XXX").pattern("###").criterion("has_book", conditionsFromItem(Items.BOOK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.BOW).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.STRING).pattern(" #X").pattern("# X").pattern(" #X").criterion("has_string", conditionsFromItem(Items.STRING)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.BOWL, 4).input('#', (Tag)ItemTags.PLANKS).pattern("# #").pattern(" # ").criterion("has_brown_mushroom", conditionsFromItem(Blocks.BROWN_MUSHROOM)).criterion("has_red_mushroom", conditionsFromItem(Blocks.RED_MUSHROOM)).criterion("has_mushroom_stew", conditionsFromItem(Items.MUSHROOM_STEW)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.BREAD).input('#', (ItemConvertible)Items.WHEAT).pattern("###").criterion("has_wheat", conditionsFromItem(Items.WHEAT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BREWING_STAND).input('B', (ItemConvertible)Items.BLAZE_ROD).input('#', (Tag)ItemTags.STONE_CRAFTING_MATERIALS).pattern(" B ").pattern("###").criterion("has_blaze_rod", conditionsFromItem(Items.BLAZE_ROD)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BRICKS).input('#', (ItemConvertible)Items.BRICK).pattern("##").pattern("##").criterion("has_brick", conditionsFromItem(Items.BRICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.BRICKS).pattern("###").criterion("has_brick_block", conditionsFromItem(Blocks.BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_brick_block", conditionsFromItem(Blocks.BRICKS)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.BROWN_DYE).input((ItemConvertible)Items.COCOA_BEANS).group("brown_dye").criterion("has_cocoa_beans", conditionsFromItem(Items.COCOA_BEANS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.BUCKET).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("# #").pattern(" # ").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CAKE).input('A', (ItemConvertible)Items.MILK_BUCKET).input('B', (ItemConvertible)Items.SUGAR).input('C', (ItemConvertible)Items.WHEAT).input('E', (ItemConvertible)Items.EGG).pattern("AAA").pattern("BEB").pattern("CCC").criterion("has_egg", conditionsFromItem(Items.EGG)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CAMPFIRE).input('L', (Tag)ItemTags.LOGS).input('S', (ItemConvertible)Items.STICK).input('C', (Tag)ItemTags.COALS).pattern(" S ").pattern("SCS").pattern("LLL").criterion("has_stick", conditionsFromItem(Items.STICK)).criterion("has_coal", conditionsFromTag(ItemTags.COALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.CARROT_ON_A_STICK).input('#', (ItemConvertible)Items.FISHING_ROD).input('X', (ItemConvertible)Items.CARROT).pattern("# ").pattern(" X").criterion("has_carrot", conditionsFromItem(Items.CARROT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.WARPED_FUNGUS_ON_A_STICK).input('#', (ItemConvertible)Items.FISHING_ROD).input('X', (ItemConvertible)Items.WARPED_FUNGUS).pattern("# ").pattern(" X").criterion("has_warped_fungus", conditionsFromItem(Items.WARPED_FUNGUS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CAULDRON).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("# #").pattern("# #").pattern("###").criterion("has_water_bucket", conditionsFromItem(Items.WATER_BUCKET)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COMPOSTER).input('#', (Tag)ItemTags.WOODEN_SLABS).pattern("# #").pattern("# #").pattern("###").criterion("has_wood_slab", conditionsFromTag(ItemTags.WOODEN_SLABS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHEST).input('#', (Tag)ItemTags.PLANKS).pattern("###").pattern("# #").pattern("###").criterion("has_lots_of_items", new InventoryChangedCriterion.Conditions(EntityPredicate.Extended.EMPTY, NumberRange.IntRange.atLeast(10), NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, new ItemPredicate[0])).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.CHEST_MINECART).input('A', (ItemConvertible)Blocks.CHEST).input('B', (ItemConvertible)Items.MINECART).pattern("A").pattern("B").criterion("has_minecart", conditionsFromItem(Items.MINECART)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHISELED_NETHER_BRICKS).input('#', (ItemConvertible)Blocks.NETHER_BRICK_SLAB).pattern("#").pattern("#").criterion("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHISELED_QUARTZ_BLOCK).input('#', (ItemConvertible)Blocks.QUARTZ_SLAB).pattern("#").pattern("#").criterion("has_chiseled_quartz_block", conditionsFromItem(Blocks.CHISELED_QUARTZ_BLOCK)).criterion("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).criterion("has_quartz_pillar", conditionsFromItem(Blocks.QUARTZ_PILLAR)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHISELED_STONE_BRICKS).input('#', (ItemConvertible)Blocks.STONE_BRICK_SLAB).pattern("#").pattern("#").criterion("has_stone_bricks", conditionsFromTag(ItemTags.STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CLAY).input('#', (ItemConvertible)Items.CLAY_BALL).pattern("##").pattern("##").criterion("has_clay_ball", conditionsFromItem(Items.CLAY_BALL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.CLOCK).input('#', (ItemConvertible)Items.GOLD_INGOT).input('X', (ItemConvertible)Items.REDSTONE).pattern(" # ").pattern("#X#").pattern(" # ").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.COAL, 9).input((ItemConvertible)Blocks.COAL_BLOCK).criterion("has_coal_block", conditionsFromItem(Blocks.COAL_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COAL_BLOCK).input('#', (ItemConvertible)Items.COAL).pattern("###").pattern("###").pattern("###").criterion("has_coal", conditionsFromItem(Items.COAL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COARSE_DIRT, 4).input('D', (ItemConvertible)Blocks.DIRT).input('G', (ItemConvertible)Blocks.GRAVEL).pattern("DG").pattern("GD").criterion("has_gravel", conditionsFromItem(Blocks.GRAVEL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COBBLESTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.COBBLESTONE).pattern("###").criterion("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COBBLESTONE_WALL, 6).input('#', (ItemConvertible)Blocks.COBBLESTONE).pattern("###").pattern("###").criterion("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COMPARATOR).input('#', (ItemConvertible)Blocks.REDSTONE_TORCH).input('X', (ItemConvertible)Items.QUARTZ).input('I', (ItemConvertible)Blocks.STONE).pattern(" # ").pattern("#X#").pattern("III").criterion("has_quartz", conditionsFromItem(Items.QUARTZ)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.COMPASS).input('#', (ItemConvertible)Items.IRON_INGOT).input('X', (ItemConvertible)Items.REDSTONE).pattern(" # ").pattern("#X#").pattern(" # ").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.COOKIE, 8).input('#', (ItemConvertible)Items.WHEAT).input('X', (ItemConvertible)Items.COCOA_BEANS).pattern("#X#").criterion("has_cocoa", conditionsFromItem(Items.COCOA_BEANS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CRAFTING_TABLE).input('#', (Tag)ItemTags.PLANKS).pattern("##").pattern("##").criterion("has_planks", conditionsFromTag(ItemTags.PLANKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.CROSSBOW).input('~', (ItemConvertible)Items.STRING).input('#', (ItemConvertible)Items.STICK).input('&', (ItemConvertible)Items.IRON_INGOT).input('$', (ItemConvertible)Blocks.TRIPWIRE_HOOK).pattern("#&#").pattern("~$~").pattern(" # ").criterion("has_string", conditionsFromItem(Items.STRING)).criterion("has_stick", conditionsFromItem(Items.STICK)).criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).criterion("has_tripwire_hook", conditionsFromItem(Blocks.TRIPWIRE_HOOK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LOOM).input('#', (Tag)ItemTags.PLANKS).input('@', (ItemConvertible)Items.STRING).pattern("@@").pattern("##").criterion("has_string", conditionsFromItem(Items.STRING)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHISELED_RED_SANDSTONE).input('#', (ItemConvertible)Blocks.RED_SANDSTONE_SLAB).pattern("#").pattern("#").criterion("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).criterion("has_chiseled_red_sandstone", conditionsFromItem(Blocks.CHISELED_RED_SANDSTONE)).criterion("has_cut_red_sandstone", conditionsFromItem(Blocks.CUT_RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHISELED_SANDSTONE).input('#', (ItemConvertible)Blocks.SANDSTONE_SLAB).pattern("#").pattern("#").criterion("has_stone_slab", conditionsFromItem(Blocks.SANDSTONE_SLAB)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.CYAN_DYE, 2).input((ItemConvertible)Items.BLUE_DYE).input((ItemConvertible)Items.GREEN_DYE).criterion("has_green_dye", conditionsFromItem(Items.GREEN_DYE)).criterion("has_blue_dye", conditionsFromItem(Items.BLUE_DYE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DARK_PRISMARINE).input('S', (ItemConvertible)Items.PRISMARINE_SHARD).input('I', (ItemConvertible)Items.BLACK_DYE).pattern("SSS").pattern("SIS").pattern("SSS").criterion("has_prismarine_shard", conditionsFromItem(Items.PRISMARINE_SHARD)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE_STAIRS, 4).input('#', (ItemConvertible)Blocks.PRISMARINE).pattern("#  ").pattern("## ").pattern("###").criterion("has_prismarine", conditionsFromItem(Blocks.PRISMARINE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.PRISMARINE_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_prismarine_bricks", conditionsFromItem(Blocks.PRISMARINE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DARK_PRISMARINE_STAIRS, 4).input('#', (ItemConvertible)Blocks.DARK_PRISMARINE).pattern("#  ").pattern("## ").pattern("###").criterion("has_dark_prismarine", conditionsFromItem(Blocks.DARK_PRISMARINE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DAYLIGHT_DETECTOR).input('Q', (ItemConvertible)Items.QUARTZ).input('G', (ItemConvertible)Blocks.GLASS).input('W', Ingredient.fromTag(ItemTags.WOODEN_SLABS)).pattern("GGG").pattern("QQQ").pattern("WWW").criterion("has_quartz", conditionsFromItem(Items.QUARTZ)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DETECTOR_RAIL, 6).input('R', (ItemConvertible)Items.REDSTONE).input('#', (ItemConvertible)Blocks.STONE_PRESSURE_PLATE).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("X X").pattern("X#X").pattern("XRX").criterion("has_rail", conditionsFromItem(Blocks.RAIL)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.DIAMOND, 9).input((ItemConvertible)Blocks.DIAMOND_BLOCK).criterion("has_diamond_block", conditionsFromItem(Blocks.DIAMOND_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_AXE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.DIAMOND).pattern("XX").pattern("X#").pattern(" #").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DIAMOND_BLOCK).input('#', (ItemConvertible)Items.DIAMOND).pattern("###").pattern("###").pattern("###").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_BOOTS).input('X', (ItemConvertible)Items.DIAMOND).pattern("X X").pattern("X X").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_CHESTPLATE).input('X', (ItemConvertible)Items.DIAMOND).pattern("X X").pattern("XXX").pattern("XXX").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_HELMET).input('X', (ItemConvertible)Items.DIAMOND).pattern("XXX").pattern("X X").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_HOE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.DIAMOND).pattern("XX").pattern(" #").pattern(" #").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_LEGGINGS).input('X', (ItemConvertible)Items.DIAMOND).pattern("XXX").pattern("X X").pattern("X X").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_PICKAXE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.DIAMOND).pattern("XXX").pattern(" # ").pattern(" # ").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_SHOVEL).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.DIAMOND).pattern("X").pattern("#").pattern("#").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.DIAMOND_SWORD).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.DIAMOND).pattern("X").pattern("X").pattern("#").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DIORITE, 2).input('Q', (ItemConvertible)Items.QUARTZ).input('C', (ItemConvertible)Blocks.COBBLESTONE).pattern("CQ").pattern("QC").criterion("has_quartz", conditionsFromItem(Items.QUARTZ)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DISPENSER).input('R', (ItemConvertible)Items.REDSTONE).input('#', (ItemConvertible)Blocks.COBBLESTONE).input('X', (ItemConvertible)Items.BOW).pattern("###").pattern("#X#").pattern("#R#").criterion("has_bow", conditionsFromItem(Items.BOW)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DROPPER).input('R', (ItemConvertible)Items.REDSTONE).input('#', (ItemConvertible)Blocks.COBBLESTONE).pattern("###").pattern("# #").pattern("#R#").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.EMERALD, 9).input((ItemConvertible)Blocks.EMERALD_BLOCK).criterion("has_emerald_block", conditionsFromItem(Blocks.EMERALD_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.EMERALD_BLOCK).input('#', (ItemConvertible)Items.EMERALD).pattern("###").pattern("###").pattern("###").criterion("has_emerald", conditionsFromItem(Items.EMERALD)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.ENCHANTING_TABLE).input('B', (ItemConvertible)Items.BOOK).input('#', (ItemConvertible)Blocks.OBSIDIAN).input('D', (ItemConvertible)Items.DIAMOND).pattern(" B ").pattern("D#D").pattern("###").criterion("has_obsidian", conditionsFromItem(Blocks.OBSIDIAN)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.ENDER_CHEST).input('#', (ItemConvertible)Blocks.OBSIDIAN).input('E', (ItemConvertible)Items.ENDER_EYE).pattern("###").pattern("#E#").pattern("###").criterion("has_ender_eye", conditionsFromItem(Items.ENDER_EYE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.ENDER_EYE).input((ItemConvertible)Items.ENDER_PEARL).input((ItemConvertible)Items.BLAZE_POWDER).criterion("has_blaze_powder", conditionsFromItem(Items.BLAZE_POWDER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.END_STONE_BRICKS, 4).input('#', (ItemConvertible)Blocks.END_STONE).pattern("##").pattern("##").criterion("has_end_stone", conditionsFromItem(Blocks.END_STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.END_CRYSTAL).input('T', (ItemConvertible)Items.GHAST_TEAR).input('E', (ItemConvertible)Items.ENDER_EYE).input('G', (ItemConvertible)Blocks.GLASS).pattern("GGG").pattern("GEG").pattern("GTG").criterion("has_ender_eye", conditionsFromItem(Items.ENDER_EYE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.END_ROD, 4).input('#', (ItemConvertible)Items.POPPED_CHORUS_FRUIT).input('/', (ItemConvertible)Items.BLAZE_ROD).pattern("/").pattern("#").criterion("has_chorus_fruit_popped", conditionsFromItem(Items.POPPED_CHORUS_FRUIT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.FERMENTED_SPIDER_EYE).input((ItemConvertible)Items.SPIDER_EYE).input((ItemConvertible)Blocks.BROWN_MUSHROOM).input((ItemConvertible)Items.SUGAR).criterion("has_spider_eye", conditionsFromItem(Items.SPIDER_EYE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.FIRE_CHARGE, 3).input((ItemConvertible)Items.GUNPOWDER).input((ItemConvertible)Items.BLAZE_POWDER).input(Ingredient.ofItems(Items.COAL, Items.CHARCOAL)).criterion("has_blaze_powder", conditionsFromItem(Items.BLAZE_POWDER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.FISHING_ROD).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.STRING).pattern("  #").pattern(" #X").pattern("# X").criterion("has_string", conditionsFromItem(Items.STRING)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.FLINT_AND_STEEL).input((ItemConvertible)Items.IRON_INGOT).input((ItemConvertible)Items.FLINT).criterion("has_flint", conditionsFromItem(Items.FLINT)).criterion("has_obsidian", conditionsFromItem(Blocks.OBSIDIAN)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.FLOWER_POT).input('#', (ItemConvertible)Items.BRICK).pattern("# #").pattern(" # ").criterion("has_brick", conditionsFromItem(Items.BRICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.FURNACE).input('#', (Tag)ItemTags.STONE_CRAFTING_MATERIALS).pattern("###").pattern("# #").pattern("###").criterion("has_cobblestone", conditionsFromTag(ItemTags.STONE_CRAFTING_MATERIALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.FURNACE_MINECART).input('A', (ItemConvertible)Blocks.FURNACE).input('B', (ItemConvertible)Items.MINECART).pattern("A").pattern("B").criterion("has_minecart", conditionsFromItem(Items.MINECART)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GLASS_BOTTLE, 3).input('#', (ItemConvertible)Blocks.GLASS).pattern("# #").pattern(" # ").criterion("has_glass", conditionsFromItem(Blocks.GLASS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GLASS_PANE, 16).input('#', (ItemConvertible)Blocks.GLASS).pattern("###").pattern("###").criterion("has_glass", conditionsFromItem(Blocks.GLASS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GLOWSTONE).input('#', (ItemConvertible)Items.GLOWSTONE_DUST).pattern("##").pattern("##").criterion("has_glowstone_dust", conditionsFromItem(Items.GLOWSTONE_DUST)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_APPLE).input('#', (ItemConvertible)Items.GOLD_INGOT).input('X', (ItemConvertible)Items.APPLE).pattern("###").pattern("#X#").pattern("###").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_AXE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("XX").pattern("X#").pattern(" #").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_BOOTS).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("X X").pattern("X X").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_CARROT).input('#', (ItemConvertible)Items.GOLD_NUGGET).input('X', (ItemConvertible)Items.CARROT).pattern("###").pattern("#X#").pattern("###").criterion("has_gold_nugget", conditionsFromItem(Items.GOLD_NUGGET)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_CHESTPLATE).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("X X").pattern("XXX").pattern("XXX").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_HELMET).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("XXX").pattern("X X").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_HOE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("XX").pattern(" #").pattern(" #").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_LEGGINGS).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("XXX").pattern("X X").pattern("X X").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_PICKAXE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("XXX").pattern(" # ").pattern(" # ").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POWERED_RAIL, 6).input('R', (ItemConvertible)Items.REDSTONE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("X X").pattern("X#X").pattern("XRX").criterion("has_rail", conditionsFromItem(Blocks.RAIL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_SHOVEL).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("X").pattern("#").pattern("#").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GOLDEN_SWORD).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.GOLD_INGOT).pattern("X").pattern("X").pattern("#").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GOLD_BLOCK).input('#', (ItemConvertible)Items.GOLD_INGOT).pattern("###").pattern("###").pattern("###").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.GOLD_INGOT, 9).input((ItemConvertible)Blocks.GOLD_BLOCK).group("gold_ingot").criterion("has_gold_block", conditionsFromItem(Blocks.GOLD_BLOCK)).offerTo(exporter, "gold_ingot_from_gold_block");
      ShapedRecipeJsonFactory.create(Items.GOLD_INGOT).input('#', (ItemConvertible)Items.GOLD_NUGGET).pattern("###").pattern("###").pattern("###").group("gold_ingot").criterion("has_gold_nugget", conditionsFromItem(Items.GOLD_NUGGET)).offerTo(exporter, "gold_ingot_from_nuggets");
      ShapelessRecipeJsonFactory.create(Items.GOLD_NUGGET, 9).input((ItemConvertible)Items.GOLD_INGOT).criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.GRANITE).input((ItemConvertible)Blocks.DIORITE).input((ItemConvertible)Items.QUARTZ).criterion("has_quartz", conditionsFromItem(Items.QUARTZ)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.GRAY_DYE, 2).input((ItemConvertible)Items.BLACK_DYE).input((ItemConvertible)Items.WHITE_DYE).criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).criterion("has_black_dye", conditionsFromItem(Items.BLACK_DYE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.HAY_BLOCK).input('#', (ItemConvertible)Items.WHEAT).pattern("###").pattern("###").pattern("###").criterion("has_wheat", conditionsFromItem(Items.WHEAT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("##").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.HONEY_BOTTLE, 4).input((ItemConvertible)Items.HONEY_BLOCK).input((ItemConvertible)Items.GLASS_BOTTLE, 4).criterion("has_honey_block", conditionsFromItem(Blocks.HONEY_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.HONEY_BLOCK, 1).input('S', (ItemConvertible)Items.HONEY_BOTTLE).pattern("SS").pattern("SS").criterion("has_honey_bottle", conditionsFromItem(Items.HONEY_BOTTLE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.HONEYCOMB_BLOCK).input('H', (ItemConvertible)Items.HONEYCOMB).pattern("HH").pattern("HH").criterion("has_honeycomb", conditionsFromItem(Items.HONEYCOMB)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.HOPPER).input('C', (ItemConvertible)Blocks.CHEST).input('I', (ItemConvertible)Items.IRON_INGOT).pattern("I I").pattern("ICI").pattern(" I ").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.HOPPER_MINECART).input('A', (ItemConvertible)Blocks.HOPPER).input('B', (ItemConvertible)Items.MINECART).pattern("A").pattern("B").criterion("has_minecart", conditionsFromItem(Items.MINECART)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_AXE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("XX").pattern("X#").pattern(" #").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.IRON_BARS, 16).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("###").pattern("###").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.IRON_BLOCK).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("###").pattern("###").pattern("###").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_BOOTS).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("X X").pattern("X X").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_CHESTPLATE).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("X X").pattern("XXX").pattern("XXX").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.IRON_DOOR, 3).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("##").pattern("##").pattern("##").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_HELMET).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("XXX").pattern("X X").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_HOE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("XX").pattern(" #").pattern(" #").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.IRON_INGOT, 9).input((ItemConvertible)Blocks.IRON_BLOCK).group("iron_ingot").criterion("has_iron_block", conditionsFromItem(Blocks.IRON_BLOCK)).offerTo(exporter, "iron_ingot_from_iron_block");
      ShapedRecipeJsonFactory.create(Items.IRON_INGOT).input('#', (ItemConvertible)Items.IRON_NUGGET).pattern("###").pattern("###").pattern("###").group("iron_ingot").criterion("has_iron_nugget", conditionsFromItem(Items.IRON_NUGGET)).offerTo(exporter, "iron_ingot_from_nuggets");
      ShapedRecipeJsonFactory.create(Items.IRON_LEGGINGS).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("XXX").pattern("X X").pattern("X X").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.IRON_NUGGET, 9).input((ItemConvertible)Items.IRON_INGOT).criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_PICKAXE).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("XXX").pattern(" # ").pattern(" # ").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_SHOVEL).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("X").pattern("#").pattern("#").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.IRON_SWORD).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("X").pattern("X").pattern("#").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.IRON_TRAPDOOR).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("##").pattern("##").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.ITEM_FRAME).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.LEATHER).pattern("###").pattern("#X#").pattern("###").criterion("has_leather", conditionsFromItem(Items.LEATHER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.JUKEBOX).input('#', (Tag)ItemTags.PLANKS).input('X', (ItemConvertible)Items.DIAMOND).pattern("###").pattern("#X#").pattern("###").criterion("has_diamond", conditionsFromItem(Items.DIAMOND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LADDER, 3).input('#', (ItemConvertible)Items.STICK).pattern("# #").pattern("###").pattern("# #").criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LAPIS_BLOCK).input('#', (ItemConvertible)Items.LAPIS_LAZULI).pattern("###").pattern("###").pattern("###").criterion("has_lapis", conditionsFromItem(Items.LAPIS_LAZULI)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.LAPIS_LAZULI, 9).input((ItemConvertible)Blocks.LAPIS_BLOCK).criterion("has_lapis_block", conditionsFromItem(Blocks.LAPIS_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEAD, 2).input('~', (ItemConvertible)Items.STRING).input('O', (ItemConvertible)Items.SLIME_BALL).pattern("~~ ").pattern("~O ").pattern("  ~").criterion("has_slime_ball", conditionsFromItem(Items.SLIME_BALL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEATHER).input('#', (ItemConvertible)Items.RABBIT_HIDE).pattern("##").pattern("##").criterion("has_rabbit_hide", conditionsFromItem(Items.RABBIT_HIDE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEATHER_BOOTS).input('X', (ItemConvertible)Items.LEATHER).pattern("X X").pattern("X X").criterion("has_leather", conditionsFromItem(Items.LEATHER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEATHER_CHESTPLATE).input('X', (ItemConvertible)Items.LEATHER).pattern("X X").pattern("XXX").pattern("XXX").criterion("has_leather", conditionsFromItem(Items.LEATHER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEATHER_HELMET).input('X', (ItemConvertible)Items.LEATHER).pattern("XXX").pattern("X X").criterion("has_leather", conditionsFromItem(Items.LEATHER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEATHER_LEGGINGS).input('X', (ItemConvertible)Items.LEATHER).pattern("XXX").pattern("X X").pattern("X X").criterion("has_leather", conditionsFromItem(Items.LEATHER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.LEATHER_HORSE_ARMOR).input('X', (ItemConvertible)Items.LEATHER).pattern("X X").pattern("XXX").pattern("X X").criterion("has_leather", conditionsFromItem(Items.LEATHER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LECTERN).input('S', (Tag)ItemTags.WOODEN_SLABS).input('B', (ItemConvertible)Blocks.BOOKSHELF).pattern("SSS").pattern(" B ").pattern(" S ").criterion("has_book", conditionsFromItem(Items.BOOK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LEVER).input('#', (ItemConvertible)Blocks.COBBLESTONE).input('X', (ItemConvertible)Items.STICK).pattern("X").pattern("#").criterion("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.LIGHT_BLUE_DYE).input((ItemConvertible)Blocks.BLUE_ORCHID).group("light_blue_dye").criterion("has_red_flower", conditionsFromItem(Blocks.BLUE_ORCHID)).offerTo(exporter, "light_blue_dye_from_blue_orchid");
      ShapelessRecipeJsonFactory.create(Items.LIGHT_BLUE_DYE, 2).input((ItemConvertible)Items.BLUE_DYE).input((ItemConvertible)Items.WHITE_DYE).group("light_blue_dye").criterion("has_blue_dye", conditionsFromItem(Items.BLUE_DYE)).criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).offerTo(exporter, "light_blue_dye_from_blue_white_dye");
      ShapelessRecipeJsonFactory.create(Items.LIGHT_GRAY_DYE).input((ItemConvertible)Blocks.AZURE_BLUET).group("light_gray_dye").criterion("has_red_flower", conditionsFromItem(Blocks.AZURE_BLUET)).offerTo(exporter, "light_gray_dye_from_azure_bluet");
      ShapelessRecipeJsonFactory.create(Items.LIGHT_GRAY_DYE, 2).input((ItemConvertible)Items.GRAY_DYE).input((ItemConvertible)Items.WHITE_DYE).group("light_gray_dye").criterion("has_gray_dye", conditionsFromItem(Items.GRAY_DYE)).criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).offerTo(exporter, "light_gray_dye_from_gray_white_dye");
      ShapelessRecipeJsonFactory.create(Items.LIGHT_GRAY_DYE, 3).input((ItemConvertible)Items.BLACK_DYE).input((ItemConvertible)Items.WHITE_DYE, 2).group("light_gray_dye").criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).criterion("has_black_dye", conditionsFromItem(Items.BLACK_DYE)).offerTo(exporter, "light_gray_dye_from_black_white_dye");
      ShapelessRecipeJsonFactory.create(Items.LIGHT_GRAY_DYE).input((ItemConvertible)Blocks.OXEYE_DAISY).group("light_gray_dye").criterion("has_red_flower", conditionsFromItem(Blocks.OXEYE_DAISY)).offerTo(exporter, "light_gray_dye_from_oxeye_daisy");
      ShapelessRecipeJsonFactory.create(Items.LIGHT_GRAY_DYE).input((ItemConvertible)Blocks.WHITE_TULIP).group("light_gray_dye").criterion("has_red_flower", conditionsFromItem(Blocks.WHITE_TULIP)).offerTo(exporter, "light_gray_dye_from_white_tulip");
      ShapedRecipeJsonFactory.create(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE).input('#', (ItemConvertible)Items.GOLD_INGOT).pattern("##").criterion("has_gold_ingot", conditionsFromItem(Items.GOLD_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.LIME_DYE, 2).input((ItemConvertible)Items.GREEN_DYE).input((ItemConvertible)Items.WHITE_DYE).criterion("has_green_dye", conditionsFromItem(Items.GREEN_DYE)).criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.JACK_O_LANTERN).input('A', (ItemConvertible)Blocks.CARVED_PUMPKIN).input('B', (ItemConvertible)Blocks.TORCH).pattern("A").pattern("B").criterion("has_carved_pumpkin", conditionsFromItem(Blocks.CARVED_PUMPKIN)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.MAGENTA_DYE).input((ItemConvertible)Blocks.ALLIUM).group("magenta_dye").criterion("has_red_flower", conditionsFromItem(Blocks.ALLIUM)).offerTo(exporter, "magenta_dye_from_allium");
      ShapelessRecipeJsonFactory.create(Items.MAGENTA_DYE, 4).input((ItemConvertible)Items.BLUE_DYE).input((ItemConvertible)Items.RED_DYE, 2).input((ItemConvertible)Items.WHITE_DYE).group("magenta_dye").criterion("has_blue_dye", conditionsFromItem(Items.BLUE_DYE)).criterion("has_rose_red", conditionsFromItem(Items.RED_DYE)).criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).offerTo(exporter, "magenta_dye_from_blue_red_white_dye");
      ShapelessRecipeJsonFactory.create(Items.MAGENTA_DYE, 3).input((ItemConvertible)Items.BLUE_DYE).input((ItemConvertible)Items.RED_DYE).input((ItemConvertible)Items.PINK_DYE).group("magenta_dye").criterion("has_pink_dye", conditionsFromItem(Items.PINK_DYE)).criterion("has_blue_dye", conditionsFromItem(Items.BLUE_DYE)).criterion("has_red_dye", conditionsFromItem(Items.RED_DYE)).offerTo(exporter, "magenta_dye_from_blue_red_pink");
      ShapelessRecipeJsonFactory.create(Items.MAGENTA_DYE, 2).input((ItemConvertible)Blocks.LILAC).group("magenta_dye").criterion("has_double_plant", conditionsFromItem(Blocks.LILAC)).offerTo(exporter, "magenta_dye_from_lilac");
      ShapelessRecipeJsonFactory.create(Items.MAGENTA_DYE, 2).input((ItemConvertible)Items.PURPLE_DYE).input((ItemConvertible)Items.PINK_DYE).group("magenta_dye").criterion("has_pink_dye", conditionsFromItem(Items.PINK_DYE)).criterion("has_purple_dye", conditionsFromItem(Items.PURPLE_DYE)).offerTo(exporter, "magenta_dye_from_purple_and_pink");
      ShapedRecipeJsonFactory.create(Blocks.MAGMA_BLOCK).input('#', (ItemConvertible)Items.MAGMA_CREAM).pattern("##").pattern("##").criterion("has_magma_cream", conditionsFromItem(Items.MAGMA_CREAM)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.MAGMA_CREAM).input((ItemConvertible)Items.BLAZE_POWDER).input((ItemConvertible)Items.SLIME_BALL).criterion("has_blaze_powder", conditionsFromItem(Items.BLAZE_POWDER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.MAP).input('#', (ItemConvertible)Items.PAPER).input('X', (ItemConvertible)Items.COMPASS).pattern("###").pattern("#X#").pattern("###").criterion("has_compass", conditionsFromItem(Items.COMPASS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MELON).input('M', (ItemConvertible)Items.MELON_SLICE).pattern("MMM").pattern("MMM").pattern("MMM").criterion("has_melon", conditionsFromItem(Items.MELON_SLICE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.MELON_SEEDS).input((ItemConvertible)Items.MELON_SLICE).criterion("has_melon", conditionsFromItem(Items.MELON_SLICE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.MINECART).input('#', (ItemConvertible)Items.IRON_INGOT).pattern("# #").pattern("###").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.MOSSY_COBBLESTONE).input((ItemConvertible)Blocks.COBBLESTONE).input((ItemConvertible)Blocks.VINE).criterion("has_vine", conditionsFromItem(Blocks.VINE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MOSSY_COBBLESTONE_WALL, 6).input('#', (ItemConvertible)Blocks.MOSSY_COBBLESTONE).pattern("###").pattern("###").criterion("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.MOSSY_STONE_BRICKS).input((ItemConvertible)Blocks.STONE_BRICKS).input((ItemConvertible)Blocks.VINE).criterion("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.MUSHROOM_STEW).input((ItemConvertible)Blocks.BROWN_MUSHROOM).input((ItemConvertible)Blocks.RED_MUSHROOM).input((ItemConvertible)Items.BOWL).criterion("has_mushroom_stew", conditionsFromItem(Items.MUSHROOM_STEW)).criterion("has_bowl", conditionsFromItem(Items.BOWL)).criterion("has_brown_mushroom", conditionsFromItem(Blocks.BROWN_MUSHROOM)).criterion("has_red_mushroom", conditionsFromItem(Blocks.RED_MUSHROOM)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHER_BRICKS).input('N', (ItemConvertible)Items.NETHER_BRICK).pattern("NN").pattern("NN").criterion("has_netherbrick", conditionsFromItem(Items.NETHER_BRICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHER_BRICK_FENCE, 6).input('#', (ItemConvertible)Blocks.NETHER_BRICKS).input('-', (ItemConvertible)Items.NETHER_BRICK).pattern("#-#").pattern("#-#").criterion("has_nether_brick", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHER_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.NETHER_BRICKS).pattern("###").criterion("has_nether_brick", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHER_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.NETHER_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_nether_brick", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHER_WART_BLOCK).input('#', (ItemConvertible)Items.NETHER_WART).pattern("###").pattern("###").pattern("###").criterion("has_nether_wart", conditionsFromItem(Items.NETHER_WART)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NOTE_BLOCK).input('#', (Tag)ItemTags.PLANKS).input('X', (ItemConvertible)Items.REDSTONE).pattern("###").pattern("#X#").pattern("###").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.OBSERVER).input('Q', (ItemConvertible)Items.QUARTZ).input('R', (ItemConvertible)Items.REDSTONE).input('#', (ItemConvertible)Blocks.COBBLESTONE).pattern("###").pattern("RRQ").pattern("###").criterion("has_quartz", conditionsFromItem(Items.QUARTZ)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.ORANGE_DYE).input((ItemConvertible)Blocks.ORANGE_TULIP).group("orange_dye").criterion("has_red_flower", conditionsFromItem(Blocks.ORANGE_TULIP)).offerTo(exporter, "orange_dye_from_orange_tulip");
      ShapelessRecipeJsonFactory.create(Items.ORANGE_DYE, 2).input((ItemConvertible)Items.RED_DYE).input((ItemConvertible)Items.YELLOW_DYE).group("orange_dye").criterion("has_red_dye", conditionsFromItem(Items.RED_DYE)).criterion("has_yellow_dye", conditionsFromItem(Items.YELLOW_DYE)).offerTo(exporter, "orange_dye_from_red_yellow");
      ShapedRecipeJsonFactory.create(Items.PAINTING).input('#', (ItemConvertible)Items.STICK).input('X', Ingredient.fromTag(ItemTags.WOOL)).pattern("###").pattern("#X#").pattern("###").criterion("has_wool", conditionsFromTag(ItemTags.WOOL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.PAPER, 3).input('#', (ItemConvertible)Blocks.SUGAR_CANE).pattern("###").criterion("has_reeds", conditionsFromItem(Blocks.SUGAR_CANE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.QUARTZ_PILLAR, 2).input('#', (ItemConvertible)Blocks.QUARTZ_BLOCK).pattern("#").pattern("#").criterion("has_chiseled_quartz_block", conditionsFromItem(Blocks.CHISELED_QUARTZ_BLOCK)).criterion("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).criterion("has_quartz_pillar", conditionsFromItem(Blocks.QUARTZ_PILLAR)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.PACKED_ICE).input((ItemConvertible)Blocks.ICE, 9).criterion("has_ice", conditionsFromItem(Blocks.ICE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.PINK_DYE, 2).input((ItemConvertible)Blocks.PEONY).group("pink_dye").criterion("has_double_plant", conditionsFromItem(Blocks.PEONY)).offerTo(exporter, "pink_dye_from_peony");
      ShapelessRecipeJsonFactory.create(Items.PINK_DYE).input((ItemConvertible)Blocks.PINK_TULIP).group("pink_dye").criterion("has_red_flower", conditionsFromItem(Blocks.PINK_TULIP)).offerTo(exporter, "pink_dye_from_pink_tulip");
      ShapelessRecipeJsonFactory.create(Items.PINK_DYE, 2).input((ItemConvertible)Items.RED_DYE).input((ItemConvertible)Items.WHITE_DYE).group("pink_dye").criterion("has_white_dye", conditionsFromItem(Items.WHITE_DYE)).criterion("has_red_dye", conditionsFromItem(Items.RED_DYE)).offerTo(exporter, "pink_dye_from_red_white_dye");
      ShapedRecipeJsonFactory.create(Blocks.PISTON).input('R', (ItemConvertible)Items.REDSTONE).input('#', (ItemConvertible)Blocks.COBBLESTONE).input('T', (Tag)ItemTags.PLANKS).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("TTT").pattern("#X#").pattern("#R#").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BASALT, 4).input('S', (ItemConvertible)Blocks.BASALT).pattern("SS").pattern("SS").criterion("has_basalt", conditionsFromItem(Blocks.BASALT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_GRANITE, 4).input('S', (ItemConvertible)Blocks.GRANITE).pattern("SS").pattern("SS").criterion("has_stone", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_DIORITE, 4).input('S', (ItemConvertible)Blocks.DIORITE).pattern("SS").pattern("SS").criterion("has_stone", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_ANDESITE, 4).input('S', (ItemConvertible)Blocks.ANDESITE).pattern("SS").pattern("SS").criterion("has_stone", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE).input('S', (ItemConvertible)Items.PRISMARINE_SHARD).pattern("SS").pattern("SS").criterion("has_prismarine_shard", conditionsFromItem(Items.PRISMARINE_SHARD)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE_BRICKS).input('S', (ItemConvertible)Items.PRISMARINE_SHARD).pattern("SSS").pattern("SSS").pattern("SSS").criterion("has_prismarine_shard", conditionsFromItem(Items.PRISMARINE_SHARD)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE_SLAB, 6).input('#', (ItemConvertible)Blocks.PRISMARINE).pattern("###").criterion("has_prismarine", conditionsFromItem(Blocks.PRISMARINE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.PRISMARINE_BRICKS).pattern("###").criterion("has_prismarine_bricks", conditionsFromItem(Blocks.PRISMARINE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DARK_PRISMARINE_SLAB, 6).input('#', (ItemConvertible)Blocks.DARK_PRISMARINE).pattern("###").criterion("has_dark_prismarine", conditionsFromItem(Blocks.DARK_PRISMARINE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.PUMPKIN_PIE).input((ItemConvertible)Blocks.PUMPKIN).input((ItemConvertible)Items.SUGAR).input((ItemConvertible)Items.EGG).criterion("has_carved_pumpkin", conditionsFromItem(Blocks.CARVED_PUMPKIN)).criterion("has_pumpkin", conditionsFromItem(Blocks.PUMPKIN)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.PUMPKIN_SEEDS, 4).input((ItemConvertible)Blocks.PUMPKIN).criterion("has_pumpkin", conditionsFromItem(Blocks.PUMPKIN)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.PURPLE_DYE, 2).input((ItemConvertible)Items.BLUE_DYE).input((ItemConvertible)Items.RED_DYE).criterion("has_blue_dye", conditionsFromItem(Items.BLUE_DYE)).criterion("has_red_dye", conditionsFromItem(Items.RED_DYE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SHULKER_BOX).input('#', (ItemConvertible)Blocks.CHEST).input('-', (ItemConvertible)Items.SHULKER_SHELL).pattern("-").pattern("#").pattern("-").criterion("has_shulker_shell", conditionsFromItem(Items.SHULKER_SHELL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PURPUR_BLOCK, 4).input('F', (ItemConvertible)Items.POPPED_CHORUS_FRUIT).pattern("FF").pattern("FF").criterion("has_chorus_fruit_popped", conditionsFromItem(Items.POPPED_CHORUS_FRUIT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PURPUR_PILLAR).input('#', (ItemConvertible)Blocks.PURPUR_SLAB).pattern("#").pattern("#").criterion("has_purpur_block", conditionsFromItem(Blocks.PURPUR_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PURPUR_SLAB, 6).input('#', Ingredient.ofItems(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR)).pattern("###").criterion("has_purpur_block", conditionsFromItem(Blocks.PURPUR_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PURPUR_STAIRS, 4).input('#', Ingredient.ofItems(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR)).pattern("#  ").pattern("## ").pattern("###").criterion("has_purpur_block", conditionsFromItem(Blocks.PURPUR_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.QUARTZ_BLOCK).input('#', (ItemConvertible)Items.QUARTZ).pattern("##").pattern("##").criterion("has_quartz", conditionsFromItem(Items.QUARTZ)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.QUARTZ_BRICKS, 4).input('#', (ItemConvertible)Blocks.QUARTZ_BLOCK).pattern("##").pattern("##").criterion("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.QUARTZ_SLAB, 6).input('#', Ingredient.ofItems(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR)).pattern("###").criterion("has_chiseled_quartz_block", conditionsFromItem(Blocks.CHISELED_QUARTZ_BLOCK)).criterion("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).criterion("has_quartz_pillar", conditionsFromItem(Blocks.QUARTZ_PILLAR)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.QUARTZ_STAIRS, 4).input('#', Ingredient.ofItems(Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR)).pattern("#  ").pattern("## ").pattern("###").criterion("has_chiseled_quartz_block", conditionsFromItem(Blocks.CHISELED_QUARTZ_BLOCK)).criterion("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).criterion("has_quartz_pillar", conditionsFromItem(Blocks.QUARTZ_PILLAR)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.RABBIT_STEW).input((ItemConvertible)Items.BAKED_POTATO).input((ItemConvertible)Items.COOKED_RABBIT).input((ItemConvertible)Items.BOWL).input((ItemConvertible)Items.CARROT).input((ItemConvertible)Blocks.BROWN_MUSHROOM).group("rabbit_stew").criterion("has_cooked_rabbit", conditionsFromItem(Items.COOKED_RABBIT)).offerTo(exporter, "rabbit_stew_from_brown_mushroom");
      ShapelessRecipeJsonFactory.create(Items.RABBIT_STEW).input((ItemConvertible)Items.BAKED_POTATO).input((ItemConvertible)Items.COOKED_RABBIT).input((ItemConvertible)Items.BOWL).input((ItemConvertible)Items.CARROT).input((ItemConvertible)Blocks.RED_MUSHROOM).group("rabbit_stew").criterion("has_cooked_rabbit", conditionsFromItem(Items.COOKED_RABBIT)).offerTo(exporter, "rabbit_stew_from_red_mushroom");
      ShapedRecipeJsonFactory.create(Blocks.RAIL, 16).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.IRON_INGOT).pattern("X X").pattern("X#X").pattern("X X").criterion("has_minecart", conditionsFromItem(Items.MINECART)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.REDSTONE, 9).input((ItemConvertible)Blocks.REDSTONE_BLOCK).criterion("has_redstone_block", conditionsFromItem(Blocks.REDSTONE_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.REDSTONE_BLOCK).input('#', (ItemConvertible)Items.REDSTONE).pattern("###").pattern("###").pattern("###").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.REDSTONE_LAMP).input('R', (ItemConvertible)Items.REDSTONE).input('G', (ItemConvertible)Blocks.GLOWSTONE).pattern(" R ").pattern("RGR").pattern(" R ").criterion("has_glowstone", conditionsFromItem(Blocks.GLOWSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.REDSTONE_TORCH).input('#', (ItemConvertible)Items.STICK).input('X', (ItemConvertible)Items.REDSTONE).pattern("X").pattern("#").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.RED_DYE).input((ItemConvertible)Items.BEETROOT).group("red_dye").criterion("has_beetroot", conditionsFromItem(Items.BEETROOT)).offerTo(exporter, "red_dye_from_beetroot");
      ShapelessRecipeJsonFactory.create(Items.RED_DYE).input((ItemConvertible)Blocks.POPPY).group("red_dye").criterion("has_red_flower", conditionsFromItem(Blocks.POPPY)).offerTo(exporter, "red_dye_from_poppy");
      ShapelessRecipeJsonFactory.create(Items.RED_DYE, 2).input((ItemConvertible)Blocks.ROSE_BUSH).group("red_dye").criterion("has_double_plant", conditionsFromItem(Blocks.ROSE_BUSH)).offerTo(exporter, "red_dye_from_rose_bush");
      ShapelessRecipeJsonFactory.create(Items.RED_DYE).input((ItemConvertible)Blocks.RED_TULIP).group("red_dye").criterion("has_red_flower", conditionsFromItem(Blocks.RED_TULIP)).offerTo(exporter, "red_dye_from_tulip");
      ShapedRecipeJsonFactory.create(Blocks.RED_NETHER_BRICKS).input('W', (ItemConvertible)Items.NETHER_WART).input('N', (ItemConvertible)Items.NETHER_BRICK).pattern("NW").pattern("WN").criterion("has_nether_wart", conditionsFromItem(Items.NETHER_WART)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_SANDSTONE).input('#', (ItemConvertible)Blocks.RED_SAND).pattern("##").pattern("##").criterion("has_sand", conditionsFromItem(Blocks.RED_SAND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_SANDSTONE_SLAB, 6).input('#', Ingredient.ofItems(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE)).pattern("###").criterion("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).criterion("has_chiseled_red_sandstone", conditionsFromItem(Blocks.CHISELED_RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CUT_RED_SANDSTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.CUT_RED_SANDSTONE).pattern("###").criterion("has_cut_red_sandstone", conditionsFromItem(Blocks.CUT_RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_SANDSTONE_STAIRS, 4).input('#', Ingredient.ofItems(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE)).pattern("#  ").pattern("## ").pattern("###").criterion("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).criterion("has_chiseled_red_sandstone", conditionsFromItem(Blocks.CHISELED_RED_SANDSTONE)).criterion("has_cut_red_sandstone", conditionsFromItem(Blocks.CUT_RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.REPEATER).input('#', (ItemConvertible)Blocks.REDSTONE_TORCH).input('X', (ItemConvertible)Items.REDSTONE).input('I', (ItemConvertible)Blocks.STONE).pattern("#X#").pattern("III").criterion("has_redstone_torch", conditionsFromItem(Blocks.REDSTONE_TORCH)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SANDSTONE).input('#', (ItemConvertible)Blocks.SAND).pattern("##").pattern("##").criterion("has_sand", conditionsFromItem(Blocks.SAND)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SANDSTONE_SLAB, 6).input('#', Ingredient.ofItems(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE)).pattern("###").criterion("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).criterion("has_chiseled_sandstone", conditionsFromItem(Blocks.CHISELED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CUT_SANDSTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.CUT_SANDSTONE).pattern("###").criterion("has_cut_sandstone", conditionsFromItem(Blocks.CUT_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SANDSTONE_STAIRS, 4).input('#', Ingredient.ofItems(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE)).pattern("#  ").pattern("## ").pattern("###").criterion("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).criterion("has_chiseled_sandstone", conditionsFromItem(Blocks.CHISELED_SANDSTONE)).criterion("has_cut_sandstone", conditionsFromItem(Blocks.CUT_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SEA_LANTERN).input('S', (ItemConvertible)Items.PRISMARINE_SHARD).input('C', (ItemConvertible)Items.PRISMARINE_CRYSTALS).pattern("SCS").pattern("CCC").pattern("SCS").criterion("has_prismarine_crystals", conditionsFromItem(Items.PRISMARINE_CRYSTALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.SHEARS).input('#', (ItemConvertible)Items.IRON_INGOT).pattern(" #").pattern("# ").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.SHIELD).input('W', (Tag)ItemTags.PLANKS).input('o', (ItemConvertible)Items.IRON_INGOT).pattern("WoW").pattern("WWW").pattern(" W ").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SLIME_BLOCK).input('#', (ItemConvertible)Items.SLIME_BALL).pattern("###").pattern("###").pattern("###").criterion("has_slime_ball", conditionsFromItem(Items.SLIME_BALL)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.SLIME_BALL, 9).input((ItemConvertible)Blocks.SLIME_BLOCK).criterion("has_slime", conditionsFromItem(Blocks.SLIME_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CUT_RED_SANDSTONE, 4).input('#', (ItemConvertible)Blocks.RED_SANDSTONE).pattern("##").pattern("##").criterion("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CUT_SANDSTONE, 4).input('#', (ItemConvertible)Blocks.SANDSTONE).pattern("##").pattern("##").criterion("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SNOW_BLOCK).input('#', (ItemConvertible)Items.SNOWBALL).pattern("##").pattern("##").criterion("has_snowball", conditionsFromItem(Items.SNOWBALL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SNOW, 6).input('#', (ItemConvertible)Blocks.SNOW_BLOCK).pattern("###").criterion("has_snowball", conditionsFromItem(Items.SNOWBALL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SOUL_CAMPFIRE).input('L', (Tag)ItemTags.LOGS).input('S', (ItemConvertible)Items.STICK).input('#', (Tag)ItemTags.SOUL_FIRE_BASE_BLOCKS).pattern(" S ").pattern("S#S").pattern("LLL").criterion("has_stick", conditionsFromItem(Items.STICK)).criterion("has_soul_sand", conditionsFromTag(ItemTags.SOUL_FIRE_BASE_BLOCKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.GLISTERING_MELON_SLICE).input('#', (ItemConvertible)Items.GOLD_NUGGET).input('X', (ItemConvertible)Items.MELON_SLICE).pattern("###").pattern("#X#").pattern("###").criterion("has_melon", conditionsFromItem(Items.MELON_SLICE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.SPECTRAL_ARROW, 2).input('#', (ItemConvertible)Items.GLOWSTONE_DUST).input('X', (ItemConvertible)Items.ARROW).pattern(" # ").pattern("#X#").pattern(" # ").criterion("has_glowstone_dust", conditionsFromItem(Items.GLOWSTONE_DUST)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STICK, 4).input('#', (Tag)ItemTags.PLANKS).pattern("#").pattern("#").group("sticks").criterion("has_planks", conditionsFromTag(ItemTags.PLANKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STICK, 1).input('#', (ItemConvertible)Blocks.BAMBOO).pattern("#").pattern("#").group("sticks").criterion("has_bamboo", conditionsFromItem(Blocks.BAMBOO)).offerTo(exporter, "stick_from_bamboo_item");
      ShapedRecipeJsonFactory.create(Blocks.STICKY_PISTON).input('P', (ItemConvertible)Blocks.PISTON).input('S', (ItemConvertible)Items.SLIME_BALL).pattern("S").pattern("P").criterion("has_slime_ball", conditionsFromItem(Items.SLIME_BALL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_BRICKS, 4).input('#', (ItemConvertible)Blocks.STONE).pattern("##").pattern("##").criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STONE_AXE).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.STONE_TOOL_MATERIALS).pattern("XX").pattern("X#").pattern(" #").criterion("has_cobblestone", conditionsFromTag(ItemTags.STONE_TOOL_MATERIALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.STONE_BRICKS).pattern("###").criterion("has_stone_bricks", conditionsFromTag(ItemTags.STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.STONE_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_stone_bricks", conditionsFromTag(ItemTags.STONE_BRICKS)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.STONE_BUTTON).input((ItemConvertible)Blocks.STONE).criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STONE_HOE).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.STONE_TOOL_MATERIALS).pattern("XX").pattern(" #").pattern(" #").criterion("has_cobblestone", conditionsFromTag(ItemTags.STONE_TOOL_MATERIALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STONE_PICKAXE).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.STONE_TOOL_MATERIALS).pattern("XXX").pattern(" # ").pattern(" # ").criterion("has_cobblestone", conditionsFromTag(ItemTags.STONE_TOOL_MATERIALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_PRESSURE_PLATE).input('#', (ItemConvertible)Blocks.STONE).pattern("##").criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STONE_SHOVEL).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.STONE_TOOL_MATERIALS).pattern("X").pattern("#").pattern("#").criterion("has_cobblestone", conditionsFromTag(ItemTags.STONE_TOOL_MATERIALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_SLAB, 6).input('#', (ItemConvertible)Blocks.STONE).pattern("###").criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_STONE_SLAB, 6).input('#', (ItemConvertible)Blocks.SMOOTH_STONE).pattern("###").criterion("has_smooth_stone", conditionsFromItem(Blocks.SMOOTH_STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.COBBLESTONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.COBBLESTONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.STONE_SWORD).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.STONE_TOOL_MATERIALS).pattern("X").pattern("X").pattern("#").criterion("has_cobblestone", conditionsFromTag(ItemTags.STONE_TOOL_MATERIALS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.WHITE_WOOL).input('#', (ItemConvertible)Items.STRING).pattern("##").pattern("##").criterion("has_string", conditionsFromItem(Items.STRING)).offerTo(exporter, "white_wool_from_string");
      ShapelessRecipeJsonFactory.create(Items.SUGAR).input((ItemConvertible)Blocks.SUGAR_CANE).group("sugar").criterion("has_reeds", conditionsFromItem(Blocks.SUGAR_CANE)).offerTo(exporter, "sugar_from_sugar_cane");
      ShapelessRecipeJsonFactory.create(Items.SUGAR, 3).input((ItemConvertible)Items.HONEY_BOTTLE).group("sugar").criterion("has_honey_bottle", conditionsFromItem(Items.HONEY_BOTTLE)).offerTo(exporter, "sugar_from_honey_bottle");
      ShapedRecipeJsonFactory.create(Blocks.TARGET).input('H', (ItemConvertible)Items.HAY_BLOCK).input('R', (ItemConvertible)Items.REDSTONE).pattern(" R ").pattern("RHR").pattern(" R ").criterion("has_redstone", conditionsFromItem(Items.REDSTONE)).criterion("has_hay_block", conditionsFromItem(Blocks.HAY_BLOCK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.TNT).input('#', Ingredient.ofItems(Blocks.SAND, Blocks.RED_SAND)).input('X', (ItemConvertible)Items.GUNPOWDER).pattern("X#X").pattern("#X#").pattern("X#X").criterion("has_gunpowder", conditionsFromItem(Items.GUNPOWDER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.TNT_MINECART).input('A', (ItemConvertible)Blocks.TNT).input('B', (ItemConvertible)Items.MINECART).pattern("A").pattern("B").criterion("has_minecart", conditionsFromItem(Items.MINECART)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.TORCH, 4).input('#', (ItemConvertible)Items.STICK).input('X', Ingredient.ofItems(Items.COAL, Items.CHARCOAL)).pattern("X").pattern("#").criterion("has_stone_pickaxe", conditionsFromItem(Items.STONE_PICKAXE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SOUL_TORCH, 4).input('X', Ingredient.ofItems(Items.COAL, Items.CHARCOAL)).input('#', (ItemConvertible)Items.STICK).input('S', (Tag)ItemTags.SOUL_FIRE_BASE_BLOCKS).pattern("X").pattern("#").pattern("S").criterion("has_soul_sand", conditionsFromTag(ItemTags.SOUL_FIRE_BASE_BLOCKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LANTERN).input('#', (ItemConvertible)Items.TORCH).input('X', (ItemConvertible)Items.IRON_NUGGET).pattern("XXX").pattern("X#X").pattern("XXX").criterion("has_iron_nugget", conditionsFromItem(Items.IRON_NUGGET)).criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SOUL_LANTERN).input('#', (ItemConvertible)Items.SOUL_TORCH).input('X', (ItemConvertible)Items.IRON_NUGGET).pattern("XXX").pattern("X#X").pattern("XXX").criterion("has_soul_torch", conditionsFromItem(Items.SOUL_TORCH)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.TRAPPED_CHEST).input((ItemConvertible)Blocks.CHEST).input((ItemConvertible)Blocks.TRIPWIRE_HOOK).criterion("has_tripwire_hook", conditionsFromItem(Blocks.TRIPWIRE_HOOK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.TRIPWIRE_HOOK, 2).input('#', (Tag)ItemTags.PLANKS).input('S', (ItemConvertible)Items.STICK).input('I', (ItemConvertible)Items.IRON_INGOT).pattern("I").pattern("S").pattern("#").criterion("has_string", conditionsFromItem(Items.STRING)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.TURTLE_HELMET).input('X', (ItemConvertible)Items.SCUTE).pattern("XXX").pattern("X X").criterion("has_scute", conditionsFromItem(Items.SCUTE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.WHEAT, 9).input((ItemConvertible)Blocks.HAY_BLOCK).criterion("has_hay_block", conditionsFromItem(Blocks.HAY_BLOCK)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.WHITE_DYE).input((ItemConvertible)Items.BONE_MEAL).group("white_dye").criterion("has_bone_meal", conditionsFromItem(Items.BONE_MEAL)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.WHITE_DYE).input((ItemConvertible)Blocks.LILY_OF_THE_VALLEY).group("white_dye").criterion("has_white_flower", conditionsFromItem(Blocks.LILY_OF_THE_VALLEY)).offerTo(exporter, "white_dye_from_lily_of_the_valley");
      ShapedRecipeJsonFactory.create(Items.WOODEN_AXE).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.PLANKS).pattern("XX").pattern("X#").pattern(" #").criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.WOODEN_HOE).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.PLANKS).pattern("XX").pattern(" #").pattern(" #").criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.WOODEN_PICKAXE).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.PLANKS).pattern("XXX").pattern(" # ").pattern(" # ").criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.WOODEN_SHOVEL).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.PLANKS).pattern("X").pattern("#").pattern("#").criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Items.WOODEN_SWORD).input('#', (ItemConvertible)Items.STICK).input('X', (Tag)ItemTags.PLANKS).pattern("X").pattern("X").pattern("#").criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.WRITABLE_BOOK).input((ItemConvertible)Items.BOOK).input((ItemConvertible)Items.INK_SAC).input((ItemConvertible)Items.FEATHER).criterion("has_book", conditionsFromItem(Items.BOOK)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.YELLOW_DYE).input((ItemConvertible)Blocks.DANDELION).group("yellow_dye").criterion("has_yellow_flower", conditionsFromItem(Blocks.DANDELION)).offerTo(exporter, "yellow_dye_from_dandelion");
      ShapelessRecipeJsonFactory.create(Items.YELLOW_DYE, 2).input((ItemConvertible)Blocks.SUNFLOWER).group("yellow_dye").criterion("has_double_plant", conditionsFromItem(Blocks.SUNFLOWER)).offerTo(exporter, "yellow_dye_from_sunflower");
      ShapelessRecipeJsonFactory.create(Items.DRIED_KELP, 9).input((ItemConvertible)Blocks.DRIED_KELP_BLOCK).criterion("has_dried_kelp_block", conditionsFromItem(Blocks.DRIED_KELP_BLOCK)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.DRIED_KELP_BLOCK).input((ItemConvertible)Items.DRIED_KELP, 9).criterion("has_dried_kelp", conditionsFromItem(Items.DRIED_KELP)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CONDUIT).input('#', (ItemConvertible)Items.NAUTILUS_SHELL).input('X', (ItemConvertible)Items.HEART_OF_THE_SEA).pattern("###").pattern("#X#").pattern("###").criterion("has_nautilus_core", conditionsFromItem(Items.HEART_OF_THE_SEA)).criterion("has_nautilus_shell", conditionsFromItem(Items.NAUTILUS_SHELL)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_GRANITE_STAIRS, 4).input('#', (ItemConvertible)Blocks.POLISHED_GRANITE).pattern("#  ").pattern("## ").pattern("###").criterion("has_polished_granite", conditionsFromItem(Blocks.POLISHED_GRANITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_RED_SANDSTONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.SMOOTH_RED_SANDSTONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_smooth_red_sandstone", conditionsFromItem(Blocks.SMOOTH_RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MOSSY_STONE_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.MOSSY_STONE_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_mossy_stone_bricks", conditionsFromItem(Blocks.MOSSY_STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_DIORITE_STAIRS, 4).input('#', (ItemConvertible)Blocks.POLISHED_DIORITE).pattern("#  ").pattern("## ").pattern("###").criterion("has_polished_diorite", conditionsFromItem(Blocks.POLISHED_DIORITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MOSSY_COBBLESTONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.MOSSY_COBBLESTONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.END_STONE_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.END_STONE_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_end_stone_bricks", conditionsFromItem(Blocks.END_STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.STONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_SANDSTONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.SMOOTH_SANDSTONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_smooth_sandstone", conditionsFromItem(Blocks.SMOOTH_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_QUARTZ_STAIRS, 4).input('#', (ItemConvertible)Blocks.SMOOTH_QUARTZ).pattern("#  ").pattern("## ").pattern("###").criterion("has_smooth_quartz", conditionsFromItem(Blocks.SMOOTH_QUARTZ)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GRANITE_STAIRS, 4).input('#', (ItemConvertible)Blocks.GRANITE).pattern("#  ").pattern("## ").pattern("###").criterion("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.ANDESITE_STAIRS, 4).input('#', (ItemConvertible)Blocks.ANDESITE).pattern("#  ").pattern("## ").pattern("###").criterion("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_NETHER_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.RED_NETHER_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_red_nether_bricks", conditionsFromItem(Blocks.RED_NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_ANDESITE_STAIRS, 4).input('#', (ItemConvertible)Blocks.POLISHED_ANDESITE).pattern("#  ").pattern("## ").pattern("###").criterion("has_polished_andesite", conditionsFromItem(Blocks.POLISHED_ANDESITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DIORITE_STAIRS, 4).input('#', (ItemConvertible)Blocks.DIORITE).pattern("#  ").pattern("## ").pattern("###").criterion("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_GRANITE_SLAB, 6).input('#', (ItemConvertible)Blocks.POLISHED_GRANITE).pattern("###").criterion("has_polished_granite", conditionsFromItem(Blocks.POLISHED_GRANITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_RED_SANDSTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.SMOOTH_RED_SANDSTONE).pattern("###").criterion("has_smooth_red_sandstone", conditionsFromItem(Blocks.SMOOTH_RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MOSSY_STONE_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.MOSSY_STONE_BRICKS).pattern("###").criterion("has_mossy_stone_bricks", conditionsFromItem(Blocks.MOSSY_STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_DIORITE_SLAB, 6).input('#', (ItemConvertible)Blocks.POLISHED_DIORITE).pattern("###").criterion("has_polished_diorite", conditionsFromItem(Blocks.POLISHED_DIORITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MOSSY_COBBLESTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.MOSSY_COBBLESTONE).pattern("###").criterion("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.END_STONE_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.END_STONE_BRICKS).pattern("###").criterion("has_end_stone_bricks", conditionsFromItem(Blocks.END_STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_SANDSTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.SMOOTH_SANDSTONE).pattern("###").criterion("has_smooth_sandstone", conditionsFromItem(Blocks.SMOOTH_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOOTH_QUARTZ_SLAB, 6).input('#', (ItemConvertible)Blocks.SMOOTH_QUARTZ).pattern("###").criterion("has_smooth_quartz", conditionsFromItem(Blocks.SMOOTH_QUARTZ)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GRANITE_SLAB, 6).input('#', (ItemConvertible)Blocks.GRANITE).pattern("###").criterion("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.ANDESITE_SLAB, 6).input('#', (ItemConvertible)Blocks.ANDESITE).pattern("###").criterion("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_NETHER_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.RED_NETHER_BRICKS).pattern("###").criterion("has_red_nether_bricks", conditionsFromItem(Blocks.RED_NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_ANDESITE_SLAB, 6).input('#', (ItemConvertible)Blocks.POLISHED_ANDESITE).pattern("###").criterion("has_polished_andesite", conditionsFromItem(Blocks.POLISHED_ANDESITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DIORITE_SLAB, 6).input('#', (ItemConvertible)Blocks.DIORITE).pattern("###").criterion("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.BRICKS).pattern("###").pattern("###").criterion("has_bricks", conditionsFromItem(Blocks.BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.PRISMARINE_WALL, 6).input('#', (ItemConvertible)Blocks.PRISMARINE).pattern("###").pattern("###").criterion("has_prismarine", conditionsFromItem(Blocks.PRISMARINE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_SANDSTONE_WALL, 6).input('#', (ItemConvertible)Blocks.RED_SANDSTONE).pattern("###").pattern("###").criterion("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.MOSSY_STONE_BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.MOSSY_STONE_BRICKS).pattern("###").pattern("###").criterion("has_mossy_stone_bricks", conditionsFromItem(Blocks.MOSSY_STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GRANITE_WALL, 6).input('#', (ItemConvertible)Blocks.GRANITE).pattern("###").pattern("###").criterion("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONE_BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.STONE_BRICKS).pattern("###").pattern("###").criterion("has_stone_bricks", conditionsFromItem(Blocks.STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHER_BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.NETHER_BRICKS).pattern("###").pattern("###").criterion("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.ANDESITE_WALL, 6).input('#', (ItemConvertible)Blocks.ANDESITE).pattern("###").pattern("###").criterion("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RED_NETHER_BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.RED_NETHER_BRICKS).pattern("###").pattern("###").criterion("has_red_nether_bricks", conditionsFromItem(Blocks.RED_NETHER_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SANDSTONE_WALL, 6).input('#', (ItemConvertible)Blocks.SANDSTONE).pattern("###").pattern("###").criterion("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.END_STONE_BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.END_STONE_BRICKS).pattern("###").pattern("###").criterion("has_end_stone_bricks", conditionsFromItem(Blocks.END_STONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.DIORITE_WALL, 6).input('#', (ItemConvertible)Blocks.DIORITE).pattern("###").pattern("###").criterion("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.CREEPER_BANNER_PATTERN).input((ItemConvertible)Items.PAPER).input((ItemConvertible)Items.CREEPER_HEAD).criterion("has_creeper_head", conditionsFromItem(Items.CREEPER_HEAD)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.SKULL_BANNER_PATTERN).input((ItemConvertible)Items.PAPER).input((ItemConvertible)Items.WITHER_SKELETON_SKULL).criterion("has_wither_skeleton_skull", conditionsFromItem(Items.WITHER_SKELETON_SKULL)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.FLOWER_BANNER_PATTERN).input((ItemConvertible)Items.PAPER).input((ItemConvertible)Blocks.OXEYE_DAISY).criterion("has_oxeye_daisy", conditionsFromItem(Blocks.OXEYE_DAISY)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.MOJANG_BANNER_PATTERN).input((ItemConvertible)Items.PAPER).input((ItemConvertible)Items.ENCHANTED_GOLDEN_APPLE).criterion("has_enchanted_golden_apple", conditionsFromItem(Items.ENCHANTED_GOLDEN_APPLE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SCAFFOLDING, 6).input('~', (ItemConvertible)Items.STRING).input('I', (ItemConvertible)Blocks.BAMBOO).pattern("I~I").pattern("I I").pattern("I I").criterion("has_bamboo", conditionsFromItem(Blocks.BAMBOO)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.GRINDSTONE).input('I', (ItemConvertible)Items.STICK).input('-', (ItemConvertible)Blocks.STONE_SLAB).input('#', (Tag)ItemTags.PLANKS).pattern("I-I").pattern("# #").criterion("has_stone_slab", conditionsFromItem(Blocks.STONE_SLAB)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BLAST_FURNACE).input('#', (ItemConvertible)Blocks.SMOOTH_STONE).input('X', (ItemConvertible)Blocks.FURNACE).input('I', (ItemConvertible)Items.IRON_INGOT).pattern("III").pattern("IXI").pattern("###").criterion("has_smooth_stone", conditionsFromItem(Blocks.SMOOTH_STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMOKER).input('#', (Tag)ItemTags.LOGS).input('X', (ItemConvertible)Blocks.FURNACE).pattern(" # ").pattern("#X#").pattern(" # ").criterion("has_furnace", conditionsFromItem(Blocks.FURNACE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CARTOGRAPHY_TABLE).input('#', (Tag)ItemTags.PLANKS).input('@', (ItemConvertible)Items.PAPER).pattern("@@").pattern("##").pattern("##").criterion("has_paper", conditionsFromItem(Items.PAPER)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.SMITHING_TABLE).input('#', (Tag)ItemTags.PLANKS).input('@', (ItemConvertible)Items.IRON_INGOT).pattern("@@").pattern("##").pattern("##").criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.FLETCHING_TABLE).input('#', (Tag)ItemTags.PLANKS).input('@', (ItemConvertible)Items.FLINT).pattern("@@").pattern("##").pattern("##").criterion("has_flint", conditionsFromItem(Items.FLINT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.STONECUTTER).input('I', (ItemConvertible)Items.IRON_INGOT).input('#', (ItemConvertible)Blocks.STONE).pattern(" I ").pattern("###").criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.LODESTONE).input('S', (ItemConvertible)Items.CHISELED_STONE_BRICKS).input('#', (ItemConvertible)Items.NETHERITE_INGOT).pattern("SSS").pattern("S#S").pattern("SSS").criterion("has_netherite_ingot", conditionsFromItem(Items.NETHERITE_INGOT)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.NETHERITE_BLOCK).input('#', (ItemConvertible)Items.NETHERITE_INGOT).pattern("###").pattern("###").pattern("###").criterion("has_netherite_ingot", conditionsFromItem(Items.NETHERITE_INGOT)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Items.NETHERITE_INGOT, 9).input((ItemConvertible)Blocks.NETHERITE_BLOCK).group("netherite_ingot").criterion("has_netherite_block", conditionsFromItem(Blocks.NETHERITE_BLOCK)).offerTo(exporter, "netherite_ingot_from_netherite_block");
      ShapelessRecipeJsonFactory.create(Items.NETHERITE_INGOT).input((ItemConvertible)Items.NETHERITE_SCRAP, 4).input((ItemConvertible)Items.GOLD_INGOT, 4).group("netherite_ingot").criterion("has_netherite_scrap", conditionsFromItem(Items.NETHERITE_SCRAP)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.RESPAWN_ANCHOR).input('O', (ItemConvertible)Blocks.CRYING_OBSIDIAN).input('G', (ItemConvertible)Blocks.GLOWSTONE).pattern("OOO").pattern("GGG").pattern("OOO").criterion("has_obsidian", conditionsFromItem(Blocks.CRYING_OBSIDIAN)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BLACKSTONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.BLACKSTONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_STAIRS, 4).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE).pattern("#  ").pattern("## ").pattern("###").criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, 4).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE_BRICKS).pattern("#  ").pattern("## ").pattern("###").criterion("has_polished_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BLACKSTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.BLACKSTONE).pattern("###").criterion("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_SLAB, 6).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE).pattern("###").criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 6).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE_BRICKS).pattern("###").criterion("has_polished_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE, 4).input('S', (ItemConvertible)Blocks.BLACKSTONE).pattern("SS").pattern("SS").criterion("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_BRICKS, 4).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE).pattern("##").pattern("##").criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHISELED_POLISHED_BLACKSTONE).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE_SLAB).pattern("#").pattern("#").criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.BLACKSTONE_WALL, 6).input('#', (ItemConvertible)Blocks.BLACKSTONE).pattern("###").pattern("###").criterion("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_WALL, 6).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE).pattern("###").pattern("###").criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_BRICK_WALL, 6).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE_BRICKS).pattern("###").pattern("###").criterion("has_polished_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter);
      ShapelessRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_BUTTON).input((ItemConvertible)Blocks.POLISHED_BLACKSTONE).criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE).input('#', (ItemConvertible)Blocks.POLISHED_BLACKSTONE).pattern("##").criterion("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter);
      ShapedRecipeJsonFactory.create(Blocks.CHAIN).input('I', (ItemConvertible)Items.IRON_INGOT).input('N', (ItemConvertible)Items.IRON_NUGGET).pattern("N").pattern("I").pattern("N").criterion("has_iron_nugget", conditionsFromItem(Items.IRON_NUGGET)).criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)).offerTo(exporter);
      ComplexRecipeJsonFactory.create(RecipeSerializer.ARMOR_DYE).offerTo(exporter, "armor_dye");
      ComplexRecipeJsonFactory.create(RecipeSerializer.BANNER_DUPLICATE).offerTo(exporter, "banner_duplicate");
      ComplexRecipeJsonFactory.create(RecipeSerializer.BOOK_CLONING).offerTo(exporter, "book_cloning");
      ComplexRecipeJsonFactory.create(RecipeSerializer.FIREWORK_ROCKET).offerTo(exporter, "firework_rocket");
      ComplexRecipeJsonFactory.create(RecipeSerializer.FIREWORK_STAR).offerTo(exporter, "firework_star");
      ComplexRecipeJsonFactory.create(RecipeSerializer.FIREWORK_STAR_FADE).offerTo(exporter, "firework_star_fade");
      ComplexRecipeJsonFactory.create(RecipeSerializer.MAP_CLONING).offerTo(exporter, "map_cloning");
      ComplexRecipeJsonFactory.create(RecipeSerializer.MAP_EXTENDING).offerTo(exporter, "map_extending");
      ComplexRecipeJsonFactory.create(RecipeSerializer.REPAIR_ITEM).offerTo(exporter, "repair_item");
      ComplexRecipeJsonFactory.create(RecipeSerializer.SHIELD_DECORATION).offerTo(exporter, "shield_decoration");
      ComplexRecipeJsonFactory.create(RecipeSerializer.SHULKER_BOX).offerTo(exporter, "shulker_box_coloring");
      ComplexRecipeJsonFactory.create(RecipeSerializer.TIPPED_ARROW).offerTo(exporter, "tipped_arrow");
      ComplexRecipeJsonFactory.create(RecipeSerializer.SUSPICIOUS_STEW).offerTo(exporter, "suspicious_stew");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.POTATO), Items.BAKED_POTATO, 0.35F, 200).criterion("has_potato", conditionsFromItem(Items.POTATO)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.CLAY_BALL), Items.BRICK, 0.3F, 200).criterion("has_clay_ball", conditionsFromItem(Items.CLAY_BALL)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.fromTag(ItemTags.LOGS_THAT_BURN), Items.CHARCOAL, 0.15F, 200).criterion("has_log", conditionsFromTag(ItemTags.LOGS_THAT_BURN)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.CHORUS_FRUIT), Items.POPPED_CHORUS_FRUIT, 0.1F, 200).criterion("has_chorus_fruit", conditionsFromItem(Items.CHORUS_FRUIT)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.COAL_ORE.asItem()), Items.COAL, 0.1F, 200).criterion("has_coal_ore", conditionsFromItem(Blocks.COAL_ORE)).offerTo(exporter, "coal_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.BEEF), Items.COOKED_BEEF, 0.35F, 200).criterion("has_beef", conditionsFromItem(Items.BEEF)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35F, 200).criterion("has_chicken", conditionsFromItem(Items.CHICKEN)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.COD), Items.COOKED_COD, 0.35F, 200).criterion("has_cod", conditionsFromItem(Items.COD)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.KELP), Items.DRIED_KELP, 0.1F, 200).criterion("has_kelp", conditionsFromItem(Blocks.KELP)).offerTo(exporter, "dried_kelp_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.SALMON), Items.COOKED_SALMON, 0.35F, 200).criterion("has_salmon", conditionsFromItem(Items.SALMON)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.MUTTON), Items.COOKED_MUTTON, 0.35F, 200).criterion("has_mutton", conditionsFromItem(Items.MUTTON)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35F, 200).criterion("has_porkchop", conditionsFromItem(Items.PORKCHOP)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.RABBIT), Items.COOKED_RABBIT, 0.35F, 200).criterion("has_rabbit", conditionsFromItem(Items.RABBIT)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.DIAMOND_ORE.asItem()), Items.DIAMOND, 1.0F, 200).criterion("has_diamond_ore", conditionsFromItem(Blocks.DIAMOND_ORE)).offerTo(exporter, "diamond_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.LAPIS_ORE.asItem()), Items.LAPIS_LAZULI, 0.2F, 200).criterion("has_lapis_ore", conditionsFromItem(Blocks.LAPIS_ORE)).offerTo(exporter, "lapis_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.EMERALD_ORE.asItem()), Items.EMERALD, 1.0F, 200).criterion("has_emerald_ore", conditionsFromItem(Blocks.EMERALD_ORE)).offerTo(exporter, "emerald_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.fromTag(ItemTags.SAND), Blocks.GLASS.asItem(), 0.1F, 200).criterion("has_sand", conditionsFromTag(ItemTags.SAND)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.fromTag(ItemTags.GOLD_ORES), Items.GOLD_INGOT, 1.0F, 200).criterion("has_gold_ore", conditionsFromTag(ItemTags.GOLD_ORES)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.SEA_PICKLE.asItem()), Items.LIME_DYE, 0.1F, 200).criterion("has_sea_pickle", conditionsFromItem(Blocks.SEA_PICKLE)).offerTo(exporter, "lime_dye_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.CACTUS.asItem()), Items.GREEN_DYE, 1.0F, 200).criterion("has_cactus", conditionsFromItem(Blocks.CACTUS)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_SWORD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GOLDEN_HORSE_ARMOR), Items.GOLD_NUGGET, 0.1F, 200).criterion("has_golden_pickaxe", conditionsFromItem(Items.GOLDEN_PICKAXE)).criterion("has_golden_shovel", conditionsFromItem(Items.GOLDEN_SHOVEL)).criterion("has_golden_axe", conditionsFromItem(Items.GOLDEN_AXE)).criterion("has_golden_hoe", conditionsFromItem(Items.GOLDEN_HOE)).criterion("has_golden_sword", conditionsFromItem(Items.GOLDEN_SWORD)).criterion("has_golden_helmet", conditionsFromItem(Items.GOLDEN_HELMET)).criterion("has_golden_chestplate", conditionsFromItem(Items.GOLDEN_CHESTPLATE)).criterion("has_golden_leggings", conditionsFromItem(Items.GOLDEN_LEGGINGS)).criterion("has_golden_boots", conditionsFromItem(Items.GOLDEN_BOOTS)).criterion("has_golden_horse_armor", conditionsFromItem(Items.GOLDEN_HORSE_ARMOR)).offerTo(exporter, "gold_nugget_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_HORSE_ARMOR, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), Items.IRON_NUGGET, 0.1F, 200).criterion("has_iron_pickaxe", conditionsFromItem(Items.IRON_PICKAXE)).criterion("has_iron_shovel", conditionsFromItem(Items.IRON_SHOVEL)).criterion("has_iron_axe", conditionsFromItem(Items.IRON_AXE)).criterion("has_iron_hoe", conditionsFromItem(Items.IRON_HOE)).criterion("has_iron_sword", conditionsFromItem(Items.IRON_SWORD)).criterion("has_iron_helmet", conditionsFromItem(Items.IRON_HELMET)).criterion("has_iron_chestplate", conditionsFromItem(Items.IRON_CHESTPLATE)).criterion("has_iron_leggings", conditionsFromItem(Items.IRON_LEGGINGS)).criterion("has_iron_boots", conditionsFromItem(Items.IRON_BOOTS)).criterion("has_iron_horse_armor", conditionsFromItem(Items.IRON_HORSE_ARMOR)).criterion("has_chainmail_helmet", conditionsFromItem(Items.CHAINMAIL_HELMET)).criterion("has_chainmail_chestplate", conditionsFromItem(Items.CHAINMAIL_CHESTPLATE)).criterion("has_chainmail_leggings", conditionsFromItem(Items.CHAINMAIL_LEGGINGS)).criterion("has_chainmail_boots", conditionsFromItem(Items.CHAINMAIL_BOOTS)).offerTo(exporter, "iron_nugget_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.IRON_ORE.asItem()), Items.IRON_INGOT, 0.7F, 200).criterion("has_iron_ore", conditionsFromItem(Blocks.IRON_ORE.asItem())).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.CLAY), Blocks.TERRACOTTA.asItem(), 0.35F, 200).criterion("has_clay_block", conditionsFromItem(Blocks.CLAY)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.NETHERRACK), Items.NETHER_BRICK, 0.1F, 200).criterion("has_netherrack", conditionsFromItem(Blocks.NETHERRACK)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2F, 200).criterion("has_nether_quartz_ore", conditionsFromItem(Blocks.NETHER_QUARTZ_ORE)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.REDSTONE_ORE), Items.REDSTONE, 0.7F, 200).criterion("has_redstone_ore", conditionsFromItem(Blocks.REDSTONE_ORE)).offerTo(exporter, "redstone_from_smelting");
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.WET_SPONGE), Blocks.SPONGE.asItem(), 0.15F, 200).criterion("has_wet_sponge", conditionsFromItem(Blocks.WET_SPONGE)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.COBBLESTONE), Blocks.STONE.asItem(), 0.1F, 200).criterion("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.STONE), Blocks.SMOOTH_STONE.asItem(), 0.1F, 200).criterion("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.SMOOTH_SANDSTONE.asItem(), 0.1F, 200).criterion("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE.asItem(), 0.1F, 200).criterion("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.QUARTZ_BLOCK), Blocks.SMOOTH_QUARTZ.asItem(), 0.1F, 200).criterion("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.STONE_BRICKS), Blocks.CRACKED_STONE_BRICKS.asItem(), 0.1F, 200).criterion("has_stone_bricks", conditionsFromItem(Blocks.STONE_BRICKS)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.BLACK_TERRACOTTA), Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_black_terracotta", conditionsFromItem(Blocks.BLACK_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.BLUE_TERRACOTTA), Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_blue_terracotta", conditionsFromItem(Blocks.BLUE_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.BROWN_TERRACOTTA), Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_brown_terracotta", conditionsFromItem(Blocks.BROWN_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.CYAN_TERRACOTTA), Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_cyan_terracotta", conditionsFromItem(Blocks.CYAN_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.GRAY_TERRACOTTA), Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_gray_terracotta", conditionsFromItem(Blocks.GRAY_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.GREEN_TERRACOTTA), Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_green_terracotta", conditionsFromItem(Blocks.GREEN_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.LIGHT_BLUE_TERRACOTTA), Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_light_blue_terracotta", conditionsFromItem(Blocks.LIGHT_BLUE_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.LIGHT_GRAY_TERRACOTTA), Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_light_gray_terracotta", conditionsFromItem(Blocks.LIGHT_GRAY_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.LIME_TERRACOTTA), Blocks.LIME_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_lime_terracotta", conditionsFromItem(Blocks.LIME_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.MAGENTA_TERRACOTTA), Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_magenta_terracotta", conditionsFromItem(Blocks.MAGENTA_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.ORANGE_TERRACOTTA), Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_orange_terracotta", conditionsFromItem(Blocks.ORANGE_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.PINK_TERRACOTTA), Blocks.PINK_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_pink_terracotta", conditionsFromItem(Blocks.PINK_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.PURPLE_TERRACOTTA), Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_purple_terracotta", conditionsFromItem(Blocks.PURPLE_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.RED_TERRACOTTA), Blocks.RED_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_red_terracotta", conditionsFromItem(Blocks.RED_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.WHITE_TERRACOTTA), Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_white_terracotta", conditionsFromItem(Blocks.WHITE_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.YELLOW_TERRACOTTA), Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), 0.1F, 200).criterion("has_yellow_terracotta", conditionsFromItem(Blocks.YELLOW_TERRACOTTA)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.ANCIENT_DEBRIS), Items.NETHERITE_SCRAP, 2.0F, 200).criterion("has_ancient_debris", conditionsFromItem(Blocks.ANCIENT_DEBRIS)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.asItem(), 0.1F, 200).criterion("has_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter);
      CookingRecipeJsonFactory.createSmelting(Ingredient.ofItems(Blocks.NETHER_BRICKS), Blocks.CRACKED_NETHER_BRICKS.asItem(), 0.1F, 200).criterion("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter);
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.IRON_ORE.asItem()), Items.IRON_INGOT, 0.7F, 100).criterion("has_iron_ore", conditionsFromItem(Blocks.IRON_ORE.asItem())).offerTo(exporter, "iron_ingot_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.fromTag(ItemTags.GOLD_ORES), Items.GOLD_INGOT, 1.0F, 100).criterion("has_gold_ore", conditionsFromTag(ItemTags.GOLD_ORES)).offerTo(exporter, "gold_ingot_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.DIAMOND_ORE.asItem()), Items.DIAMOND, 1.0F, 100).criterion("has_diamond_ore", conditionsFromItem(Blocks.DIAMOND_ORE)).offerTo(exporter, "diamond_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.LAPIS_ORE.asItem()), Items.LAPIS_LAZULI, 0.2F, 100).criterion("has_lapis_ore", conditionsFromItem(Blocks.LAPIS_ORE)).offerTo(exporter, "lapis_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.REDSTONE_ORE), Items.REDSTONE, 0.7F, 100).criterion("has_redstone_ore", conditionsFromItem(Blocks.REDSTONE_ORE)).offerTo(exporter, "redstone_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.COAL_ORE.asItem()), Items.COAL, 0.1F, 100).criterion("has_coal_ore", conditionsFromItem(Blocks.COAL_ORE)).offerTo(exporter, "coal_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.EMERALD_ORE.asItem()), Items.EMERALD, 1.0F, 100).criterion("has_emerald_ore", conditionsFromItem(Blocks.EMERALD_ORE)).offerTo(exporter, "emerald_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.NETHER_QUARTZ_ORE), Items.QUARTZ, 0.2F, 100).criterion("has_nether_quartz_ore", conditionsFromItem(Blocks.NETHER_QUARTZ_ORE)).offerTo(exporter, "quartz_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_SWORD, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, Items.GOLDEN_HORSE_ARMOR), Items.GOLD_NUGGET, 0.1F, 100).criterion("has_golden_pickaxe", conditionsFromItem(Items.GOLDEN_PICKAXE)).criterion("has_golden_shovel", conditionsFromItem(Items.GOLDEN_SHOVEL)).criterion("has_golden_axe", conditionsFromItem(Items.GOLDEN_AXE)).criterion("has_golden_hoe", conditionsFromItem(Items.GOLDEN_HOE)).criterion("has_golden_sword", conditionsFromItem(Items.GOLDEN_SWORD)).criterion("has_golden_helmet", conditionsFromItem(Items.GOLDEN_HELMET)).criterion("has_golden_chestplate", conditionsFromItem(Items.GOLDEN_CHESTPLATE)).criterion("has_golden_leggings", conditionsFromItem(Items.GOLDEN_LEGGINGS)).criterion("has_golden_boots", conditionsFromItem(Items.GOLDEN_BOOTS)).criterion("has_golden_horse_armor", conditionsFromItem(Items.GOLDEN_HORSE_ARMOR)).offerTo(exporter, "gold_nugget_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SWORD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_HORSE_ARMOR, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), Items.IRON_NUGGET, 0.1F, 100).criterion("has_iron_pickaxe", conditionsFromItem(Items.IRON_PICKAXE)).criterion("has_iron_shovel", conditionsFromItem(Items.IRON_SHOVEL)).criterion("has_iron_axe", conditionsFromItem(Items.IRON_AXE)).criterion("has_iron_hoe", conditionsFromItem(Items.IRON_HOE)).criterion("has_iron_sword", conditionsFromItem(Items.IRON_SWORD)).criterion("has_iron_helmet", conditionsFromItem(Items.IRON_HELMET)).criterion("has_iron_chestplate", conditionsFromItem(Items.IRON_CHESTPLATE)).criterion("has_iron_leggings", conditionsFromItem(Items.IRON_LEGGINGS)).criterion("has_iron_boots", conditionsFromItem(Items.IRON_BOOTS)).criterion("has_iron_horse_armor", conditionsFromItem(Items.IRON_HORSE_ARMOR)).criterion("has_chainmail_helmet", conditionsFromItem(Items.CHAINMAIL_HELMET)).criterion("has_chainmail_chestplate", conditionsFromItem(Items.CHAINMAIL_CHESTPLATE)).criterion("has_chainmail_leggings", conditionsFromItem(Items.CHAINMAIL_LEGGINGS)).criterion("has_chainmail_boots", conditionsFromItem(Items.CHAINMAIL_BOOTS)).offerTo(exporter, "iron_nugget_from_blasting");
      CookingRecipeJsonFactory.createBlasting(Ingredient.ofItems(Blocks.ANCIENT_DEBRIS), Items.NETHERITE_SCRAP, 2.0F, 100).criterion("has_ancient_debris", conditionsFromItem(Blocks.ANCIENT_DEBRIS)).offerTo(exporter, "netherite_scrap_from_blasting");
      generateCookingRecipes(exporter, "smoking", RecipeSerializer.SMOKING, 100);
      generateCookingRecipes(exporter, "campfire_cooking", RecipeSerializer.CAMPFIRE_COOKING, 600);
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.STONE_SLAB, 2).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "stone_slab_from_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.STONE_STAIRS).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "stone_stairs_from_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.STONE_BRICKS).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "stone_bricks_from_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.STONE_BRICK_SLAB, 2).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "stone_brick_slab_from_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.STONE_BRICK_STAIRS).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "stone_brick_stairs_from_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.CHISELED_STONE_BRICKS).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "chiseled_stone_bricks_stone_from_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE), Blocks.STONE_BRICK_WALL).create("has_stone", conditionsFromItem(Blocks.STONE)).offerTo(exporter, "stone_brick_walls_from_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.CUT_SANDSTONE).create("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "cut_sandstone_from_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.SANDSTONE_SLAB, 2).create("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "sandstone_slab_from_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.CUT_SANDSTONE_SLAB, 2).create("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "cut_sandstone_slab_from_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.CUT_SANDSTONE), Blocks.CUT_SANDSTONE_SLAB, 2).create("has_cut_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "cut_sandstone_slab_from_cut_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.SANDSTONE_STAIRS).create("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "sandstone_stairs_from_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.SANDSTONE_WALL).create("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "sandstone_wall_from_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SANDSTONE), Blocks.CHISELED_SANDSTONE).create("has_sandstone", conditionsFromItem(Blocks.SANDSTONE)).offerTo(exporter, "chiseled_sandstone_from_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE).create("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "cut_red_sandstone_from_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_SLAB, 2).create("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "red_sandstone_slab_from_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE_SLAB, 2).create("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "cut_red_sandstone_slab_from_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.CUT_RED_SANDSTONE), Blocks.CUT_RED_SANDSTONE_SLAB, 2).create("has_cut_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "cut_red_sandstone_slab_from_cut_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_STAIRS).create("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "red_sandstone_stairs_from_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.RED_SANDSTONE_WALL).create("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "red_sandstone_wall_from_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_SANDSTONE), Blocks.CHISELED_RED_SANDSTONE).create("has_red_sandstone", conditionsFromItem(Blocks.RED_SANDSTONE)).offerTo(exporter, "chiseled_red_sandstone_from_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_SLAB, 2).create("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter, "quartz_slab_from_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_STAIRS).create("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter, "quartz_stairs_from_quartz_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_PILLAR).create("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter, "quartz_pillar_from_quartz_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.QUARTZ_BLOCK), Blocks.CHISELED_QUARTZ_BLOCK).create("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter, "chiseled_quartz_block_from_quartz_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.QUARTZ_BLOCK), Blocks.QUARTZ_BRICKS).create("has_quartz_block", conditionsFromItem(Blocks.QUARTZ_BLOCK)).offerTo(exporter, "quartz_bricks_from_quartz_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.COBBLESTONE), Blocks.COBBLESTONE_STAIRS).create("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter, "cobblestone_stairs_from_cobblestone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.COBBLESTONE), Blocks.COBBLESTONE_SLAB, 2).create("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter, "cobblestone_slab_from_cobblestone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.COBBLESTONE), Blocks.COBBLESTONE_WALL).create("has_cobblestone", conditionsFromItem(Blocks.COBBLESTONE)).offerTo(exporter, "cobblestone_wall_from_cobblestone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_SLAB, 2).create("has_stone_bricks", conditionsFromItem(Blocks.STONE_BRICKS)).offerTo(exporter, "stone_brick_slab_from_stone_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_STAIRS).create("has_stone_bricks", conditionsFromItem(Blocks.STONE_BRICKS)).offerTo(exporter, "stone_brick_stairs_from_stone_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE_BRICKS), Blocks.STONE_BRICK_WALL).create("has_stone_bricks", conditionsFromItem(Blocks.STONE_BRICKS)).offerTo(exporter, "stone_brick_wall_from_stone_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.STONE_BRICKS), Blocks.CHISELED_STONE_BRICKS).create("has_stone_bricks", conditionsFromItem(Blocks.STONE_BRICKS)).offerTo(exporter, "chiseled_stone_bricks_from_stone_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BRICKS), Blocks.BRICK_SLAB, 2).create("has_bricks", conditionsFromItem(Blocks.BRICKS)).offerTo(exporter, "brick_slab_from_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BRICKS), Blocks.BRICK_STAIRS).create("has_bricks", conditionsFromItem(Blocks.BRICKS)).offerTo(exporter, "brick_stairs_from_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BRICKS), Blocks.BRICK_WALL).create("has_bricks", conditionsFromItem(Blocks.BRICKS)).offerTo(exporter, "brick_wall_from_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_SLAB, 2).create("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter, "nether_brick_slab_from_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_STAIRS).create("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter, "nether_brick_stairs_from_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.NETHER_BRICKS), Blocks.NETHER_BRICK_WALL).create("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter, "nether_brick_wall_from_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.NETHER_BRICKS), Blocks.CHISELED_NETHER_BRICKS).create("has_nether_bricks", conditionsFromItem(Blocks.NETHER_BRICKS)).offerTo(exporter, "chiseled_nether_bricks_from_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_SLAB, 2).create("has_nether_bricks", conditionsFromItem(Blocks.RED_NETHER_BRICKS)).offerTo(exporter, "red_nether_brick_slab_from_red_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_STAIRS).create("has_nether_bricks", conditionsFromItem(Blocks.RED_NETHER_BRICKS)).offerTo(exporter, "red_nether_brick_stairs_from_red_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.RED_NETHER_BRICKS), Blocks.RED_NETHER_BRICK_WALL).create("has_nether_bricks", conditionsFromItem(Blocks.RED_NETHER_BRICKS)).offerTo(exporter, "red_nether_brick_wall_from_red_nether_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PURPUR_BLOCK), Blocks.PURPUR_SLAB, 2).create("has_purpur_block", conditionsFromItem(Blocks.PURPUR_BLOCK)).offerTo(exporter, "purpur_slab_from_purpur_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PURPUR_BLOCK), Blocks.PURPUR_STAIRS).create("has_purpur_block", conditionsFromItem(Blocks.PURPUR_BLOCK)).offerTo(exporter, "purpur_stairs_from_purpur_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PURPUR_BLOCK), Blocks.PURPUR_PILLAR).create("has_purpur_block", conditionsFromItem(Blocks.PURPUR_BLOCK)).offerTo(exporter, "purpur_pillar_from_purpur_block_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PRISMARINE), Blocks.PRISMARINE_SLAB, 2).create("has_prismarine", conditionsFromItem(Blocks.PRISMARINE)).offerTo(exporter, "prismarine_slab_from_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PRISMARINE), Blocks.PRISMARINE_STAIRS).create("has_prismarine", conditionsFromItem(Blocks.PRISMARINE)).offerTo(exporter, "prismarine_stairs_from_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PRISMARINE), Blocks.PRISMARINE_WALL).create("has_prismarine", conditionsFromItem(Blocks.PRISMARINE)).offerTo(exporter, "prismarine_wall_from_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_SLAB, 2).create("has_prismarine_brick", conditionsFromItem(Blocks.PRISMARINE_BRICKS)).offerTo(exporter, "prismarine_brick_slab_from_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.PRISMARINE_BRICKS), Blocks.PRISMARINE_BRICK_STAIRS).create("has_prismarine_brick", conditionsFromItem(Blocks.PRISMARINE_BRICKS)).offerTo(exporter, "prismarine_brick_stairs_from_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DARK_PRISMARINE), Blocks.DARK_PRISMARINE_SLAB, 2).create("has_dark_prismarine", conditionsFromItem(Blocks.DARK_PRISMARINE)).offerTo(exporter, "dark_prismarine_slab_from_dark_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DARK_PRISMARINE), Blocks.DARK_PRISMARINE_STAIRS).create("has_dark_prismarine", conditionsFromItem(Blocks.DARK_PRISMARINE)).offerTo(exporter, "dark_prismarine_stairs_from_dark_prismarine_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.ANDESITE), Blocks.ANDESITE_SLAB, 2).create("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter, "andesite_slab_from_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.ANDESITE), Blocks.ANDESITE_STAIRS).create("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter, "andesite_stairs_from_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.ANDESITE), Blocks.ANDESITE_WALL).create("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter, "andesite_wall_from_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE).create("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter, "polished_andesite_from_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE_SLAB, 2).create("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter, "polished_andesite_slab_from_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.ANDESITE), Blocks.POLISHED_ANDESITE_STAIRS).create("has_andesite", conditionsFromItem(Blocks.ANDESITE)).offerTo(exporter, "polished_andesite_stairs_from_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_ANDESITE), Blocks.POLISHED_ANDESITE_SLAB, 2).create("has_polished_andesite", conditionsFromItem(Blocks.POLISHED_ANDESITE)).offerTo(exporter, "polished_andesite_slab_from_polished_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_ANDESITE), Blocks.POLISHED_ANDESITE_STAIRS).create("has_polished_andesite", conditionsFromItem(Blocks.POLISHED_ANDESITE)).offerTo(exporter, "polished_andesite_stairs_from_polished_andesite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BASALT), Blocks.POLISHED_BASALT).create("has_basalt", conditionsFromItem(Blocks.BASALT)).offerTo(exporter, "polished_basalt_from_basalt_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.GRANITE), Blocks.GRANITE_SLAB, 2).create("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter, "granite_slab_from_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.GRANITE), Blocks.GRANITE_STAIRS).create("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter, "granite_stairs_from_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.GRANITE), Blocks.GRANITE_WALL).create("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter, "granite_wall_from_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.GRANITE), Blocks.POLISHED_GRANITE).create("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter, "polished_granite_from_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.GRANITE), Blocks.POLISHED_GRANITE_SLAB, 2).create("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter, "polished_granite_slab_from_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.GRANITE), Blocks.POLISHED_GRANITE_STAIRS).create("has_granite", conditionsFromItem(Blocks.GRANITE)).offerTo(exporter, "polished_granite_stairs_from_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_GRANITE), Blocks.POLISHED_GRANITE_SLAB, 2).create("has_polished_granite", conditionsFromItem(Blocks.POLISHED_GRANITE)).offerTo(exporter, "polished_granite_slab_from_polished_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_GRANITE), Blocks.POLISHED_GRANITE_STAIRS).create("has_polished_granite", conditionsFromItem(Blocks.POLISHED_GRANITE)).offerTo(exporter, "polished_granite_stairs_from_polished_granite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DIORITE), Blocks.DIORITE_SLAB, 2).create("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter, "diorite_slab_from_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DIORITE), Blocks.DIORITE_STAIRS).create("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter, "diorite_stairs_from_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DIORITE), Blocks.DIORITE_WALL).create("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter, "diorite_wall_from_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DIORITE), Blocks.POLISHED_DIORITE).create("has_diorite", conditionsFromItem(Blocks.DIORITE)).offerTo(exporter, "polished_diorite_from_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DIORITE), Blocks.POLISHED_DIORITE_SLAB, 2).create("has_diorite", conditionsFromItem(Blocks.POLISHED_DIORITE)).offerTo(exporter, "polished_diorite_slab_from_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.DIORITE), Blocks.POLISHED_DIORITE_STAIRS).create("has_diorite", conditionsFromItem(Blocks.POLISHED_DIORITE)).offerTo(exporter, "polished_diorite_stairs_from_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_DIORITE), Blocks.POLISHED_DIORITE_SLAB, 2).create("has_polished_diorite", conditionsFromItem(Blocks.POLISHED_DIORITE)).offerTo(exporter, "polished_diorite_slab_from_polished_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_DIORITE), Blocks.POLISHED_DIORITE_STAIRS).create("has_polished_diorite", conditionsFromItem(Blocks.POLISHED_DIORITE)).offerTo(exporter, "polished_diorite_stairs_from_polished_diorite_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_SLAB, 2).create("has_mossy_stone_bricks", conditionsFromItem(Blocks.MOSSY_STONE_BRICKS)).offerTo(exporter, "mossy_stone_brick_slab_from_mossy_stone_brick_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_STAIRS).create("has_mossy_stone_bricks", conditionsFromItem(Blocks.MOSSY_STONE_BRICKS)).offerTo(exporter, "mossy_stone_brick_stairs_from_mossy_stone_brick_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.MOSSY_STONE_BRICKS), Blocks.MOSSY_STONE_BRICK_WALL).create("has_mossy_stone_bricks", conditionsFromItem(Blocks.MOSSY_STONE_BRICKS)).offerTo(exporter, "mossy_stone_brick_wall_from_mossy_stone_brick_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_SLAB, 2).create("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter, "mossy_cobblestone_slab_from_mossy_cobblestone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_STAIRS).create("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter, "mossy_cobblestone_stairs_from_mossy_cobblestone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.MOSSY_COBBLESTONE), Blocks.MOSSY_COBBLESTONE_WALL).create("has_mossy_cobblestone", conditionsFromItem(Blocks.MOSSY_COBBLESTONE)).offerTo(exporter, "mossy_cobblestone_wall_from_mossy_cobblestone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_SANDSTONE), Blocks.SMOOTH_SANDSTONE_SLAB, 2).create("has_smooth_sandstone", conditionsFromItem(Blocks.SMOOTH_SANDSTONE)).offerTo(exporter, "smooth_sandstone_slab_from_smooth_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_SANDSTONE), Blocks.SMOOTH_SANDSTONE_STAIRS).create("has_mossy_cobblestone", conditionsFromItem(Blocks.SMOOTH_SANDSTONE)).offerTo(exporter, "smooth_sandstone_stairs_from_smooth_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE_SLAB, 2).create("has_smooth_red_sandstone", conditionsFromItem(Blocks.SMOOTH_RED_SANDSTONE)).offerTo(exporter, "smooth_red_sandstone_slab_from_smooth_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_RED_SANDSTONE), Blocks.SMOOTH_RED_SANDSTONE_STAIRS).create("has_smooth_red_sandstone", conditionsFromItem(Blocks.SMOOTH_RED_SANDSTONE)).offerTo(exporter, "smooth_red_sandstone_stairs_from_smooth_red_sandstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_QUARTZ), Blocks.SMOOTH_QUARTZ_SLAB, 2).create("has_smooth_quartz", conditionsFromItem(Blocks.SMOOTH_QUARTZ)).offerTo(exporter, "smooth_quartz_slab_from_smooth_quartz_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_QUARTZ), Blocks.SMOOTH_QUARTZ_STAIRS).create("has_smooth_quartz", conditionsFromItem(Blocks.SMOOTH_QUARTZ)).offerTo(exporter, "smooth_quartz_stairs_from_smooth_quartz_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_SLAB, 2).create("has_end_stone_brick", conditionsFromItem(Blocks.END_STONE_BRICKS)).offerTo(exporter, "end_stone_brick_slab_from_end_stone_brick_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_STAIRS).create("has_end_stone_brick", conditionsFromItem(Blocks.END_STONE_BRICKS)).offerTo(exporter, "end_stone_brick_stairs_from_end_stone_brick_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE_BRICKS), Blocks.END_STONE_BRICK_WALL).create("has_end_stone_brick", conditionsFromItem(Blocks.END_STONE_BRICKS)).offerTo(exporter, "end_stone_brick_wall_from_end_stone_brick_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE), Blocks.END_STONE_BRICKS).create("has_end_stone", conditionsFromItem(Blocks.END_STONE)).offerTo(exporter, "end_stone_bricks_from_end_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE), Blocks.END_STONE_BRICK_SLAB, 2).create("has_end_stone", conditionsFromItem(Blocks.END_STONE)).offerTo(exporter, "end_stone_brick_slab_from_end_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE), Blocks.END_STONE_BRICK_STAIRS).create("has_end_stone", conditionsFromItem(Blocks.END_STONE)).offerTo(exporter, "end_stone_brick_stairs_from_end_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.END_STONE), Blocks.END_STONE_BRICK_WALL).create("has_end_stone", conditionsFromItem(Blocks.END_STONE)).offerTo(exporter, "end_stone_brick_wall_from_end_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.SMOOTH_STONE), Blocks.SMOOTH_STONE_SLAB, 2).create("has_smooth_stone", conditionsFromItem(Blocks.SMOOTH_STONE)).offerTo(exporter, "smooth_stone_slab_from_smooth_stone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.BLACKSTONE_SLAB, 2).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "blackstone_slab_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.BLACKSTONE_STAIRS).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "blackstone_stairs_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.BLACKSTONE_WALL).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "blackstone_wall_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_WALL).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_wall_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_SLAB, 2).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_slab_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_STAIRS).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_stairs_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.CHISELED_POLISHED_BLACKSTONE).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "chiseled_polished_blackstone_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICKS).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_bricks_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 2).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_brick_slab_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_brick_stairs_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_WALL).create("has_blackstone", conditionsFromItem(Blocks.BLACKSTONE)).offerTo(exporter, "polished_blackstone_brick_wall_from_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_SLAB, 2).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_slab_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_STAIRS).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_stairs_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICKS).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_bricks_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_WALL).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_wall_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 2).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_brick_slab_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_brick_stairs_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.POLISHED_BLACKSTONE_BRICK_WALL).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "polished_blackstone_brick_wall_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE), Blocks.CHISELED_POLISHED_BLACKSTONE).create("has_polished_blackstone", conditionsFromItem(Blocks.POLISHED_BLACKSTONE)).offerTo(exporter, "chiseled_polished_blackstone_from_polished_blackstone_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, 2).create("has_polished_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter, "polished_blackstone_brick_slab_from_polished_blackstone_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS).create("has_polished_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter, "polished_blackstone_brick_stairs_from_polished_blackstone_bricks_stonecutting");
      SingleItemRecipeJsonFactory.createStonecutting(Ingredient.ofItems(Blocks.POLISHED_BLACKSTONE_BRICKS), Blocks.POLISHED_BLACKSTONE_BRICK_WALL).create("has_polished_blackstone_bricks", conditionsFromItem(Blocks.POLISHED_BLACKSTONE_BRICKS)).offerTo(exporter, "polished_blackstone_brick_wall_from_polished_blackstone_bricks_stonecutting");
      method_29728(exporter, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
      method_29728(exporter, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
      method_29728(exporter, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
      method_29728(exporter, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
      method_29728(exporter, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
      method_29728(exporter, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
      method_29728(exporter, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
      method_29728(exporter, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
      method_29728(exporter, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
   }

   private static void method_29728(Consumer<RecipeJsonProvider> consumer, Item item, Item item2) {
      SmithingRecipeJsonFactory.create(Ingredient.ofItems(item), Ingredient.ofItems(Items.NETHERITE_INGOT), item2).criterion("has_netherite_ingot", conditionsFromItem(Items.NETHERITE_INGOT)).offerTo(consumer, Registry.ITEM.getId(item2.asItem()).getPath() + "_smithing");
   }

   private static void method_24475(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, Tag<Item> tag) {
      ShapelessRecipeJsonFactory.create(itemConvertible, 4).input(tag).group("planks").criterion("has_log", conditionsFromTag(tag)).offerTo(consumer);
   }

   private static void method_24477(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, Tag<Item> tag) {
      ShapelessRecipeJsonFactory.create(itemConvertible, 4).input(tag).group("planks").criterion("has_logs", conditionsFromTag(tag)).offerTo(consumer);
   }

   private static void method_24476(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 3).input('#', itemConvertible2).pattern("##").pattern("##").group("bark").criterion("has_log", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24478(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible).input('#', itemConvertible2).pattern("# #").pattern("###").group("boat").criterion("in_water", requireEnteringFluid(Blocks.WATER)).offerTo(consumer);
   }

   private static void method_24479(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapelessRecipeJsonFactory.create(itemConvertible).input(itemConvertible2).group("wooden_button").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24480(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 3).input('#', itemConvertible2).pattern("##").pattern("##").pattern("##").group("wooden_door").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24481(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 3).input('#', (ItemConvertible)Items.STICK).input('W', itemConvertible2).pattern("W#W").pattern("W#W").group("wooden_fence").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24482(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible).input('#', (ItemConvertible)Items.STICK).input('W', itemConvertible2).pattern("#W#").pattern("#W#").group("wooden_fence_gate").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24483(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible).input('#', itemConvertible2).pattern("##").group("wooden_pressure_plate").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24484(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 6).input('#', itemConvertible2).pattern("###").group("wooden_slab").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24485(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 4).input('#', itemConvertible2).pattern("#  ").pattern("## ").pattern("###").group("wooden_stairs").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24486(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 2).input('#', itemConvertible2).pattern("###").pattern("###").group("wooden_trapdoor").criterion("has_planks", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24883(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible2.asItem()).getPath();
      ShapedRecipeJsonFactory.create(itemConvertible, 3).group("sign").input('#', itemConvertible2).input('X', (ItemConvertible)Items.STICK).pattern("###").pattern("###").pattern(" X ").criterion("has_" + string, conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24884(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapelessRecipeJsonFactory.create(itemConvertible).input(itemConvertible2).input((ItemConvertible)Blocks.WHITE_WOOL).group("wool").criterion("has_white_wool", conditionsFromItem(Blocks.WHITE_WOOL)).offerTo(consumer);
   }

   private static void method_24885(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible2.asItem()).getPath();
      ShapedRecipeJsonFactory.create(itemConvertible, 3).input('#', itemConvertible2).pattern("##").group("carpet").criterion("has_" + string, conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24886(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible.asItem()).getPath();
      String string2 = Registry.ITEM.getId(itemConvertible2.asItem()).getPath();
      ShapedRecipeJsonFactory.create(itemConvertible, 8).input('#', (ItemConvertible)Blocks.WHITE_CARPET).input('$', itemConvertible2).pattern("###").pattern("#$#").pattern("###").group("carpet").criterion("has_white_carpet", conditionsFromItem(Blocks.WHITE_CARPET)).criterion("has_" + string2, conditionsFromItem(itemConvertible2)).offerTo(consumer, string + "_from_white_carpet");
   }

   private static void method_24887(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible2.asItem()).getPath();
      ShapedRecipeJsonFactory.create(itemConvertible).input('#', itemConvertible2).input('X', (Tag)ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").criterion("has_" + string, conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24888(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible.asItem()).getPath();
      ShapelessRecipeJsonFactory.create(itemConvertible).input((ItemConvertible)Items.WHITE_BED).input(itemConvertible2).group("dyed_bed").criterion("has_bed", conditionsFromItem(Items.WHITE_BED)).offerTo(consumer, string + "_from_white_bed");
   }

   private static void method_24889(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible2.asItem()).getPath();
      ShapedRecipeJsonFactory.create(itemConvertible).input('#', itemConvertible2).input('|', (ItemConvertible)Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").criterion("has_" + string, conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24890(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 8).input('#', (ItemConvertible)Blocks.GLASS).input('X', itemConvertible2).pattern("###").pattern("#X#").pattern("###").group("stained_glass").criterion("has_glass", conditionsFromItem(Blocks.GLASS)).offerTo(consumer);
   }

   private static void method_24891(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 16).input('#', itemConvertible2).pattern("###").pattern("###").group("stained_glass_pane").criterion("has_glass", conditionsFromItem(itemConvertible2)).offerTo(consumer);
   }

   private static void method_24892(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      String string = Registry.ITEM.getId(itemConvertible.asItem()).getPath();
      String string2 = Registry.ITEM.getId(itemConvertible2.asItem()).getPath();
      ShapedRecipeJsonFactory.create(itemConvertible, 8).input('#', (ItemConvertible)Blocks.GLASS_PANE).input('$', itemConvertible2).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").criterion("has_glass_pane", conditionsFromItem(Blocks.GLASS_PANE)).criterion("has_" + string2, conditionsFromItem(itemConvertible2)).offerTo(consumer, string + "_from_glass_pane");
   }

   private static void method_24893(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapedRecipeJsonFactory.create(itemConvertible, 8).input('#', (ItemConvertible)Blocks.TERRACOTTA).input('X', itemConvertible2).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").criterion("has_terracotta", conditionsFromItem(Blocks.TERRACOTTA)).offerTo(consumer);
   }

   private static void method_24894(Consumer<RecipeJsonProvider> consumer, ItemConvertible itemConvertible, ItemConvertible itemConvertible2) {
      ShapelessRecipeJsonFactory.create(itemConvertible, 8).input(itemConvertible2).input((ItemConvertible)Blocks.SAND, 4).input((ItemConvertible)Blocks.GRAVEL, 4).group("concrete_powder").criterion("has_sand", conditionsFromItem(Blocks.SAND)).criterion("has_gravel", conditionsFromItem(Blocks.GRAVEL)).offerTo(consumer);
   }

   private static void generateCookingRecipes(Consumer<RecipeJsonProvider> exporter, String cooker, CookingRecipeSerializer<?> serializer, int cookingTime) {
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.BEEF), Items.COOKED_BEEF, 0.35F, cookingTime, serializer).criterion("has_beef", conditionsFromItem(Items.BEEF)).offerTo(exporter, "cooked_beef_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.CHICKEN), Items.COOKED_CHICKEN, 0.35F, cookingTime, serializer).criterion("has_chicken", conditionsFromItem(Items.CHICKEN)).offerTo(exporter, "cooked_chicken_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.COD), Items.COOKED_COD, 0.35F, cookingTime, serializer).criterion("has_cod", conditionsFromItem(Items.COD)).offerTo(exporter, "cooked_cod_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Blocks.KELP), Items.DRIED_KELP, 0.1F, cookingTime, serializer).criterion("has_kelp", conditionsFromItem(Blocks.KELP)).offerTo(exporter, "dried_kelp_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.SALMON), Items.COOKED_SALMON, 0.35F, cookingTime, serializer).criterion("has_salmon", conditionsFromItem(Items.SALMON)).offerTo(exporter, "cooked_salmon_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.MUTTON), Items.COOKED_MUTTON, 0.35F, cookingTime, serializer).criterion("has_mutton", conditionsFromItem(Items.MUTTON)).offerTo(exporter, "cooked_mutton_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.PORKCHOP), Items.COOKED_PORKCHOP, 0.35F, cookingTime, serializer).criterion("has_porkchop", conditionsFromItem(Items.PORKCHOP)).offerTo(exporter, "cooked_porkchop_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.POTATO), Items.BAKED_POTATO, 0.35F, cookingTime, serializer).criterion("has_potato", conditionsFromItem(Items.POTATO)).offerTo(exporter, "baked_potato_from_" + cooker);
      CookingRecipeJsonFactory.create(Ingredient.ofItems(Items.RABBIT), Items.COOKED_RABBIT, 0.35F, cookingTime, serializer).criterion("has_rabbit", conditionsFromItem(Items.RABBIT)).offerTo(exporter, "cooked_rabbit_from_" + cooker);
   }

   private static EnterBlockCriterion.Conditions requireEnteringFluid(Block block) {
      return new EnterBlockCriterion.Conditions(EntityPredicate.Extended.EMPTY, block, StatePredicate.ANY);
   }

   private static InventoryChangedCriterion.Conditions conditionsFromItem(ItemConvertible item) {
      return conditionsFromItemPredicates(ItemPredicate.Builder.create().item(item).build());
   }

   private static InventoryChangedCriterion.Conditions conditionsFromTag(Tag<Item> tag) {
      return conditionsFromItemPredicates(ItemPredicate.Builder.create().tag(tag).build());
   }

   private static InventoryChangedCriterion.Conditions conditionsFromItemPredicates(ItemPredicate... items) {
      return new InventoryChangedCriterion.Conditions(EntityPredicate.Extended.EMPTY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, items);
   }

   public String getName() {
      return "Recipes";
   }
}
