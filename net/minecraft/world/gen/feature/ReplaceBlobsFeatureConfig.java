package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.UniformIntDistribution;

public class ReplaceBlobsFeatureConfig implements FeatureConfig {
   public static final Codec<ReplaceBlobsFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockState.CODEC.fieldOf("target").forGetter((replaceBlobsFeatureConfig) -> {
         return replaceBlobsFeatureConfig.target;
      }), BlockState.CODEC.fieldOf("state").forGetter((replaceBlobsFeatureConfig) -> {
         return replaceBlobsFeatureConfig.state;
      }), UniformIntDistribution.CODEC.fieldOf("radius").forGetter((replaceBlobsFeatureConfig) -> {
         return replaceBlobsFeatureConfig.radius;
      })).apply(instance, (Function3)(ReplaceBlobsFeatureConfig::new));
   });
   public final BlockState target;
   public final BlockState state;
   private final UniformIntDistribution radius;

   public ReplaceBlobsFeatureConfig(BlockState target, BlockState state, UniformIntDistribution radius) {
      this.target = target;
      this.state = state;
      this.radius = radius;
   }

   public UniformIntDistribution getRadius() {
      return this.radius;
   }
}
