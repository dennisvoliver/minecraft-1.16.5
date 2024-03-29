package net.minecraft.block.sapling;

import java.util.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

public class OakSaplingGenerator extends SaplingGenerator {
   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> createTreeFeature(Random random, boolean bees) {
      if (random.nextInt(10) == 0) {
         return bees ? ConfiguredFeatures.FANCY_OAK_BEES_005 : ConfiguredFeatures.FANCY_OAK;
      } else {
         return bees ? ConfiguredFeatures.OAK_BEES_005 : ConfiguredFeatures.OAK;
      }
   }
}
