package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class LargeOakFoliagePlacer extends BlobFoliagePlacer {
   public static final Codec<LargeOakFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return createCodec(instance).apply(instance, (Function3)(LargeOakFoliagePlacer::new));
   });

   public LargeOakFoliagePlacer(UniformIntDistribution uniformIntDistribution, UniformIntDistribution uniformIntDistribution2, int i) {
      super(uniformIntDistribution, uniformIntDistribution2, i);
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
   }

   protected void generate(ModifiableTestableWorld world, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, Set<BlockPos> leaves, int offset, BlockBox box) {
      for(int i = offset; i >= offset - foliageHeight; --i) {
         int j = radius + (i != offset && i != offset - foliageHeight ? 1 : 0);
         this.generateSquare(world, random, config, treeNode.getCenter(), j, leaves, i, treeNode.isGiantTrunk(), box);
      }

   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      return MathHelper.square((float)dx + 0.5F) + MathHelper.square((float)dz + 0.5F) > (float)(radius * radius);
   }
}
