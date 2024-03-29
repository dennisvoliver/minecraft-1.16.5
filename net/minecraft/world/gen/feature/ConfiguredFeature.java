package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.dynamic.RegistryElementCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.ConfiguredDecorator;
import net.minecraft.world.gen.decorator.Decoratable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfig, F extends Feature<FC>> implements Decoratable<ConfiguredFeature<?, ?>> {
   public static final Codec<ConfiguredFeature<?, ?>> CODEC;
   public static final Codec<Supplier<ConfiguredFeature<?, ?>>> REGISTRY_CODEC;
   public static final Codec<List<Supplier<ConfiguredFeature<?, ?>>>> field_26756;
   public static final Logger LOGGER;
   public final F feature;
   public final FC config;

   public ConfiguredFeature(F feature, FC config) {
      this.feature = feature;
      this.config = config;
   }

   public F getFeature() {
      return this.feature;
   }

   public FC getConfig() {
      return this.config;
   }

   public ConfiguredFeature<?, ?> decorate(ConfiguredDecorator<?> configuredDecorator) {
      return Feature.DECORATED.configure(new DecoratedFeatureConfig(() -> {
         return this;
      }, configuredDecorator));
   }

   public RandomFeatureEntry withChance(float chance) {
      return new RandomFeatureEntry(this, chance);
   }

   public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos origin) {
      return this.feature.generate(world, chunkGenerator, random, origin, this.config);
   }

   public Stream<ConfiguredFeature<?, ?>> method_30648() {
      return Stream.concat(Stream.of(this), this.config.method_30649());
   }

   static {
      CODEC = Registry.FEATURE.dispatch((configuredFeature) -> {
         return configuredFeature.feature;
      }, Feature::getCodec);
      REGISTRY_CODEC = RegistryElementCodec.of(Registry.CONFIGURED_FEATURE_WORLDGEN, CODEC);
      field_26756 = RegistryElementCodec.method_31194(Registry.CONFIGURED_FEATURE_WORLDGEN, CODEC);
      LOGGER = LogManager.getLogger();
   }
}
