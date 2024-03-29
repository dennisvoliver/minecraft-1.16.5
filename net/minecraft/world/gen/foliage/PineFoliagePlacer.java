package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableTestableWorld;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class PineFoliagePlacer extends FoliagePlacer {
   public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and((App)UniformIntDistribution.createValidatedCodec(0, 16, 8).fieldOf("height").forGetter((pineFoliagePlacer) -> {
         return pineFoliagePlacer.height;
      })).apply(instance, (Function3)(PineFoliagePlacer::new));
   });
   private final UniformIntDistribution height;

   public PineFoliagePlacer(UniformIntDistribution radius, UniformIntDistribution offset, UniformIntDistribution height) {
      super(radius, offset);
      this.height = height;
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.PINE_FOLIAGE_PLACER;
   }

   protected void generate(ModifiableTestableWorld world, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, Set<BlockPos> leaves, int offset, BlockBox box) {
      int i = 0;

      for(int j = offset; j >= offset - foliageHeight; --j) {
         this.generateSquare(world, random, config, treeNode.getCenter(), i, leaves, j, treeNode.isGiantTrunk(), box);
         if (i >= 1 && j == offset - foliageHeight + 1) {
            --i;
         } else if (i < radius + treeNode.getFoliageRadius()) {
            ++i;
         }
      }

   }

   public int getRandomRadius(Random random, int baseHeight) {
      return super.getRandomRadius(random, baseHeight) + random.nextInt(baseHeight + 1);
   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height.getValue(random);
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      return dx == radius && dz == radius && radius > 0;
   }
}
