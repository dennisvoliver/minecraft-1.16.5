package net.minecraft.world.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function7;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.dynamic.RegistryElementCodec;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class Biome {
   public static final Logger LOGGER = LogManager.getLogger();
   public static final Codec<Biome> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Biome.Weather.CODEC.forGetter((biome) -> {
         return biome.weather;
      }), Biome.Category.CODEC.fieldOf("category").forGetter((biome) -> {
         return biome.category;
      }), Codec.FLOAT.fieldOf("depth").forGetter((biome) -> {
         return biome.depth;
      }), Codec.FLOAT.fieldOf("scale").forGetter((biome) -> {
         return biome.scale;
      }), BiomeEffects.CODEC.fieldOf("effects").forGetter((biome) -> {
         return biome.effects;
      }), GenerationSettings.CODEC.forGetter((biome) -> {
         return biome.generationSettings;
      }), SpawnSettings.CODEC.forGetter((biome) -> {
         return biome.spawnSettings;
      })).apply(instance, (Function7)(Biome::new));
   });
   public static final Codec<Biome> field_26633 = RecordCodecBuilder.create((instance) -> {
      return instance.group(Biome.Weather.CODEC.forGetter((biome) -> {
         return biome.weather;
      }), Biome.Category.CODEC.fieldOf("category").forGetter((biome) -> {
         return biome.category;
      }), Codec.FLOAT.fieldOf("depth").forGetter((biome) -> {
         return biome.depth;
      }), Codec.FLOAT.fieldOf("scale").forGetter((biome) -> {
         return biome.scale;
      }), BiomeEffects.CODEC.fieldOf("effects").forGetter((biome) -> {
         return biome.effects;
      })).apply(instance, (Function5)((weather, category, float_, float2, biomeEffects) -> {
         return new Biome(weather, category, float_, float2, biomeEffects, GenerationSettings.INSTANCE, SpawnSettings.INSTANCE);
      }));
   });
   public static final Codec<Supplier<Biome>> REGISTRY_CODEC;
   public static final Codec<List<Supplier<Biome>>> field_26750;
   private final Map<Integer, List<StructureFeature<?>>> structures;
   private static final OctaveSimplexNoiseSampler TEMPERATURE_NOISE;
   private static final OctaveSimplexNoiseSampler FROZEN_OCEAN_NOISE;
   public static final OctaveSimplexNoiseSampler FOLIAGE_NOISE;
   private final Biome.Weather weather;
   private final GenerationSettings generationSettings;
   private final SpawnSettings spawnSettings;
   private final float depth;
   private final float scale;
   private final Biome.Category category;
   private final BiomeEffects effects;
   private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache;

   private Biome(Biome.Weather weather, Biome.Category category, float depth, float scale, BiomeEffects effects, GenerationSettings generationSettings, SpawnSettings spawnSettings) {
      this.structures = (Map)Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy((structureFeature) -> {
         return structureFeature.getGenerationStep().ordinal();
      }));
      this.temperatureCache = ThreadLocal.withInitial(() -> {
         return (Long2FloatLinkedOpenHashMap)Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
               protected void rehash(int i) {
               }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
         });
      });
      this.weather = weather;
      this.generationSettings = generationSettings;
      this.spawnSettings = spawnSettings;
      this.category = category;
      this.depth = depth;
      this.scale = scale;
      this.effects = effects;
   }

   @Environment(EnvType.CLIENT)
   public int getSkyColor() {
      return this.effects.getSkyColor();
   }

   public SpawnSettings getSpawnSettings() {
      return this.spawnSettings;
   }

   public Biome.Precipitation getPrecipitation() {
      return this.weather.precipitation;
   }

   public boolean hasHighHumidity() {
      return this.getDownfall() > 0.85F;
   }

   private float computeTemperature(BlockPos pos) {
      float f = this.weather.temperatureModifier.getModifiedTemperature(pos, this.getTemperature());
      if (pos.getY() > 64) {
         float g = (float)(TEMPERATURE_NOISE.sample((double)((float)pos.getX() / 8.0F), (double)((float)pos.getZ() / 8.0F), false) * 4.0D);
         return f - (g + (float)pos.getY() - 64.0F) * 0.05F / 30.0F;
      } else {
         return f;
      }
   }

   public final float getTemperature(BlockPos blockPos) {
      long l = blockPos.asLong();
      Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = (Long2FloatLinkedOpenHashMap)this.temperatureCache.get();
      float f = long2FloatLinkedOpenHashMap.get(l);
      if (!Float.isNaN(f)) {
         return f;
      } else {
         float g = this.computeTemperature(blockPos);
         if (long2FloatLinkedOpenHashMap.size() == 1024) {
            long2FloatLinkedOpenHashMap.removeFirstFloat();
         }

         long2FloatLinkedOpenHashMap.put(l, g);
         return g;
      }
   }

   public boolean canSetIce(WorldView world, BlockPos blockPos) {
      return this.canSetIce(world, blockPos, true);
   }

   public boolean canSetIce(WorldView world, BlockPos pos, boolean doWaterCheck) {
      if (this.getTemperature(pos) >= 0.15F) {
         return false;
      } else {
         if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightLevel(LightType.BLOCK, pos) < 10) {
            BlockState blockState = world.getBlockState(pos);
            FluidState fluidState = world.getFluidState(pos);
            if (fluidState.getFluid() == Fluids.WATER && blockState.getBlock() instanceof FluidBlock) {
               if (!doWaterCheck) {
                  return true;
               }

               boolean bl = world.isWater(pos.west()) && world.isWater(pos.east()) && world.isWater(pos.north()) && world.isWater(pos.south());
               if (!bl) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean canSetSnow(WorldView world, BlockPos blockPos) {
      if (this.getTemperature(blockPos) >= 0.15F) {
         return false;
      } else {
         if (blockPos.getY() >= 0 && blockPos.getY() < 256 && world.getLightLevel(LightType.BLOCK, blockPos) < 10) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isAir() && Blocks.SNOW.getDefaultState().canPlaceAt(world, blockPos)) {
               return true;
            }
         }

         return false;
      }
   }

   public GenerationSettings getGenerationSettings() {
      return this.generationSettings;
   }

   public void generateFeatureStep(StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, ChunkRegion region, long populationSeed, ChunkRandom random, BlockPos origin) {
      List<List<Supplier<ConfiguredFeature<?, ?>>>> list = this.generationSettings.getFeatures();
      int i = GenerationStep.Feature.values().length;

      for(int j = 0; j < i; ++j) {
         int k = 0;
         if (structureAccessor.shouldGenerateStructures()) {
            List<StructureFeature<?>> list2 = (List)this.structures.getOrDefault(j, Collections.emptyList());

            for(Iterator var13 = list2.iterator(); var13.hasNext(); ++k) {
               StructureFeature<?> structureFeature = (StructureFeature)var13.next();
               random.setDecoratorSeed(populationSeed, k, j);
               int l = origin.getX() >> 4;
               int m = origin.getZ() >> 4;
               int n = l << 4;
               int o = m << 4;

               try {
                  structureAccessor.getStructuresWithChildren(ChunkSectionPos.from(origin), structureFeature).forEach((structureStart) -> {
                     structureStart.generateStructure(region, structureAccessor, chunkGenerator, random, new BlockBox(n, o, n + 15, o + 15), new ChunkPos(l, m));
                  });
               } catch (Exception var21) {
                  CrashReport crashReport = CrashReport.create(var21, "Feature placement");
                  crashReport.addElement("Feature").add("Id", (Object)Registry.STRUCTURE_FEATURE.getId(structureFeature)).add("Description", () -> {
                     return structureFeature.toString();
                  });
                  throw new CrashException(crashReport);
               }
            }
         }

         if (list.size() > j) {
            for(Iterator var23 = ((List)list.get(j)).iterator(); var23.hasNext(); ++k) {
               Supplier<ConfiguredFeature<?, ?>> supplier = (Supplier)var23.next();
               ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature)supplier.get();
               random.setDecoratorSeed(populationSeed, k, j);

               try {
                  configuredFeature.generate(region, chunkGenerator, random, origin);
               } catch (Exception var22) {
                  CrashReport crashReport2 = CrashReport.create(var22, "Feature placement");
                  crashReport2.addElement("Feature").add("Id", (Object)Registry.FEATURE.getId(configuredFeature.feature)).add("Config", (Object)configuredFeature.config).add("Description", () -> {
                     return configuredFeature.feature.toString();
                  });
                  throw new CrashException(crashReport2);
               }
            }
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public int getFogColor() {
      return this.effects.getFogColor();
   }

   @Environment(EnvType.CLIENT)
   public int getGrassColorAt(double x, double z) {
      int i = (Integer)this.effects.getGrassColor().orElseGet(this::getDefaultGrassColor);
      return this.effects.getGrassColorModifier().getModifiedGrassColor(x, z, i);
   }

   @Environment(EnvType.CLIENT)
   private int getDefaultGrassColor() {
      double d = (double)MathHelper.clamp(this.weather.temperature, 0.0F, 1.0F);
      double e = (double)MathHelper.clamp(this.weather.downfall, 0.0F, 1.0F);
      return GrassColors.getColor(d, e);
   }

   @Environment(EnvType.CLIENT)
   public int getFoliageColor() {
      return (Integer)this.effects.getFoliageColor().orElseGet(this::getDefaultFoliageColor);
   }

   @Environment(EnvType.CLIENT)
   private int getDefaultFoliageColor() {
      double d = (double)MathHelper.clamp(this.weather.temperature, 0.0F, 1.0F);
      double e = (double)MathHelper.clamp(this.weather.downfall, 0.0F, 1.0F);
      return FoliageColors.getColor(d, e);
   }

   public void buildSurface(Random random, Chunk chunk, int x, int z, int worldHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed) {
      ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder = (ConfiguredSurfaceBuilder)this.generationSettings.getSurfaceBuilder().get();
      configuredSurfaceBuilder.initSeed(seed);
      configuredSurfaceBuilder.generate(random, chunk, this, x, z, worldHeight, noise, defaultBlock, defaultFluid, seaLevel, seed);
   }

   public final float getDepth() {
      return this.depth;
   }

   public final float getDownfall() {
      return this.weather.downfall;
   }

   public final float getScale() {
      return this.scale;
   }

   public final float getTemperature() {
      return this.weather.temperature;
   }

   public BiomeEffects getEffects() {
      return this.effects;
   }

   @Environment(EnvType.CLIENT)
   public final int getWaterColor() {
      return this.effects.getWaterColor();
   }

   @Environment(EnvType.CLIENT)
   public final int getWaterFogColor() {
      return this.effects.getWaterFogColor();
   }

   @Environment(EnvType.CLIENT)
   public Optional<BiomeParticleConfig> getParticleConfig() {
      return this.effects.getParticleConfig();
   }

   @Environment(EnvType.CLIENT)
   public Optional<SoundEvent> getLoopSound() {
      return this.effects.getLoopSound();
   }

   @Environment(EnvType.CLIENT)
   public Optional<BiomeMoodSound> getMoodSound() {
      return this.effects.getMoodSound();
   }

   @Environment(EnvType.CLIENT)
   public Optional<BiomeAdditionsSound> getAdditionsSound() {
      return this.effects.getAdditionsSound();
   }

   @Environment(EnvType.CLIENT)
   public Optional<MusicSound> getMusic() {
      return this.effects.getMusic();
   }

   public final Biome.Category getCategory() {
      return this.category;
   }

   public String toString() {
      Identifier identifier = BuiltinRegistries.BIOME.getId(this);
      return identifier == null ? super.toString() : identifier.toString();
   }

   static {
      REGISTRY_CODEC = RegistryElementCodec.of(Registry.BIOME_KEY, CODEC);
      field_26750 = RegistryElementCodec.method_31194(Registry.BIOME_KEY, CODEC);
      TEMPERATURE_NOISE = new OctaveSimplexNoiseSampler(new ChunkRandom(1234L), ImmutableList.of(0));
      FROZEN_OCEAN_NOISE = new OctaveSimplexNoiseSampler(new ChunkRandom(3456L), ImmutableList.of(-2, -1, 0));
      FOLIAGE_NOISE = new OctaveSimplexNoiseSampler(new ChunkRandom(2345L), ImmutableList.of(0));
   }

   static class Weather {
      public static final MapCodec<Biome.Weather> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter((weather) -> {
            return weather.precipitation;
         }), Codec.FLOAT.fieldOf("temperature").forGetter((weather) -> {
            return weather.temperature;
         }), Biome.TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE).forGetter((weather) -> {
            return weather.temperatureModifier;
         }), Codec.FLOAT.fieldOf("downfall").forGetter((weather) -> {
            return weather.downfall;
         })).apply(instance, (Function4)(Biome.Weather::new));
      });
      private final Biome.Precipitation precipitation;
      private final float temperature;
      private final Biome.TemperatureModifier temperatureModifier;
      private final float downfall;

      private Weather(Biome.Precipitation precipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
         this.precipitation = precipitation;
         this.temperature = temperature;
         this.temperatureModifier = temperatureModifier;
         this.downfall = downfall;
      }
   }

   /**
    * Represents a point in a multi-dimensional cartesian plane. Mixed-noise
    * biome generator picks the closest noise point from its selected point
    * and choose the biome associated to that closest point. Another factor,
    * rarity potential, favors larger differences in values instead, contrary
    * to other point values.
    */
   public static class MixedNoisePoint {
      public static final Codec<Biome.MixedNoisePoint> CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.floatRange(-2.0F, 2.0F).fieldOf("temperature").forGetter((mixedNoisePoint) -> {
            return mixedNoisePoint.temperature;
         }), Codec.floatRange(-2.0F, 2.0F).fieldOf("humidity").forGetter((mixedNoisePoint) -> {
            return mixedNoisePoint.humidity;
         }), Codec.floatRange(-2.0F, 2.0F).fieldOf("altitude").forGetter((mixedNoisePoint) -> {
            return mixedNoisePoint.altitude;
         }), Codec.floatRange(-2.0F, 2.0F).fieldOf("weirdness").forGetter((mixedNoisePoint) -> {
            return mixedNoisePoint.weirdness;
         }), Codec.floatRange(0.0F, 1.0F).fieldOf("offset").forGetter((mixedNoisePoint) -> {
            return mixedNoisePoint.weight;
         })).apply(instance, (Function5)(Biome.MixedNoisePoint::new));
      });
      private final float temperature;
      private final float humidity;
      private final float altitude;
      private final float weirdness;
      /**
       * This value awards another point with value farthest from this one; i.e.
       * unlike other points where closer distance is better, for this value the
       * farther the better. The result of the different values can be
       * approximately modeled by a hyperbola weight=cosh(peak-1) as used by the
       * mixed-noise generator.
       */
      private final float weight;

      public MixedNoisePoint(float temperature, float humidity, float altitude, float weirdness, float weight) {
         this.temperature = temperature;
         this.humidity = humidity;
         this.altitude = altitude;
         this.weirdness = weirdness;
         this.weight = weight;
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            Biome.MixedNoisePoint mixedNoisePoint = (Biome.MixedNoisePoint)object;
            if (Float.compare(mixedNoisePoint.temperature, this.temperature) != 0) {
               return false;
            } else if (Float.compare(mixedNoisePoint.humidity, this.humidity) != 0) {
               return false;
            } else if (Float.compare(mixedNoisePoint.altitude, this.altitude) != 0) {
               return false;
            } else {
               return Float.compare(mixedNoisePoint.weirdness, this.weirdness) == 0;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.temperature != 0.0F ? Float.floatToIntBits(this.temperature) : 0;
         i = 31 * i + (this.humidity != 0.0F ? Float.floatToIntBits(this.humidity) : 0);
         i = 31 * i + (this.altitude != 0.0F ? Float.floatToIntBits(this.altitude) : 0);
         i = 31 * i + (this.weirdness != 0.0F ? Float.floatToIntBits(this.weirdness) : 0);
         return i;
      }

      /**
       * Calculates the distance from this noise point to another one. The
       * distance is a squared distance in a multi-dimensional cartesian plane
       * from a mathematical point of view, with a special parameter that
       * reduces the calculated distance.
       * 
       * <p>For most fields except weight, smaller difference between
       * two points' fields will lead to smaller distance. For weight,
       * larger differences lead to smaller distance.
       * 
       * <p>This distance is used by the mixed-noise biome layer source. The
       * layer source calculates an arbitrary noise point, and selects the
       * biome that offers a closest point to its arbitrary point.
       * 
       * @param other the other noise point
       */
      public float calculateDistanceTo(Biome.MixedNoisePoint other) {
         return (this.temperature - other.temperature) * (this.temperature - other.temperature) + (this.humidity - other.humidity) * (this.humidity - other.humidity) + (this.altitude - other.altitude) * (this.altitude - other.altitude) + (this.weirdness - other.weirdness) * (this.weirdness - other.weirdness) + (this.weight - other.weight) * (this.weight - other.weight);
      }
   }

   public static class Builder {
      @Nullable
      private Biome.Precipitation precipitation;
      @Nullable
      private Biome.Category category;
      @Nullable
      private Float depth;
      @Nullable
      private Float scale;
      @Nullable
      private Float temperature;
      private Biome.TemperatureModifier temperatureModifier;
      @Nullable
      private Float downfall;
      @Nullable
      private BiomeEffects specialEffects;
      @Nullable
      private SpawnSettings spawnSettings;
      @Nullable
      private GenerationSettings generationSettings;

      public Builder() {
         this.temperatureModifier = Biome.TemperatureModifier.NONE;
      }

      public Biome.Builder precipitation(Biome.Precipitation precipitation) {
         this.precipitation = precipitation;
         return this;
      }

      public Biome.Builder category(Biome.Category category) {
         this.category = category;
         return this;
      }

      public Biome.Builder depth(float depth) {
         this.depth = depth;
         return this;
      }

      public Biome.Builder scale(float scale) {
         this.scale = scale;
         return this;
      }

      public Biome.Builder temperature(float temperature) {
         this.temperature = temperature;
         return this;
      }

      public Biome.Builder downfall(float downfall) {
         this.downfall = downfall;
         return this;
      }

      public Biome.Builder effects(BiomeEffects effects) {
         this.specialEffects = effects;
         return this;
      }

      public Biome.Builder spawnSettings(SpawnSettings spawnSettings) {
         this.spawnSettings = spawnSettings;
         return this;
      }

      public Biome.Builder generationSettings(GenerationSettings generationSettings) {
         this.generationSettings = generationSettings;
         return this;
      }

      public Biome.Builder temperatureModifier(Biome.TemperatureModifier temperatureModifier) {
         this.temperatureModifier = temperatureModifier;
         return this;
      }

      public Biome build() {
         if (this.precipitation != null && this.category != null && this.depth != null && this.scale != null && this.temperature != null && this.downfall != null && this.specialEffects != null && this.spawnSettings != null && this.generationSettings != null) {
            return new Biome(new Biome.Weather(this.precipitation, this.temperature, this.temperatureModifier, this.downfall), this.category, this.depth, this.scale, this.specialEffects, this.generationSettings, this.spawnSettings);
         } else {
            throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
         }
      }

      public String toString() {
         return "BiomeBuilder{\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.category + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.spawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n" + '}';
      }
   }

   public static enum TemperatureModifier implements StringIdentifiable {
      NONE("none") {
         public float getModifiedTemperature(BlockPos pos, float temperature) {
            return temperature;
         }
      },
      FROZEN("frozen") {
         public float getModifiedTemperature(BlockPos pos, float temperature) {
            double d = Biome.FROZEN_OCEAN_NOISE.sample((double)pos.getX() * 0.05D, (double)pos.getZ() * 0.05D, false) * 7.0D;
            double e = Biome.FOLIAGE_NOISE.sample((double)pos.getX() * 0.2D, (double)pos.getZ() * 0.2D, false);
            double f = d + e;
            if (f < 0.3D) {
               double g = Biome.FOLIAGE_NOISE.sample((double)pos.getX() * 0.09D, (double)pos.getZ() * 0.09D, false);
               if (g < 0.8D) {
                  return 0.2F;
               }
            }

            return temperature;
         }
      };

      private final String name;
      public static final Codec<Biome.TemperatureModifier> CODEC = StringIdentifiable.createCodec(Biome.TemperatureModifier::values, Biome.TemperatureModifier::byName);
      private static final Map<String, Biome.TemperatureModifier> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(Biome.TemperatureModifier::getName, (temperatureModifier) -> {
         return temperatureModifier;
      }));

      public abstract float getModifiedTemperature(BlockPos pos, float temperature);

      private TemperatureModifier(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public String asString() {
         return this.name;
      }

      public static Biome.TemperatureModifier byName(String name) {
         return (Biome.TemperatureModifier)BY_NAME.get(name);
      }
   }

   public static enum Precipitation implements StringIdentifiable {
      NONE("none"),
      RAIN("rain"),
      SNOW("snow");

      public static final Codec<Biome.Precipitation> CODEC = StringIdentifiable.createCodec(Biome.Precipitation::values, Biome.Precipitation::byName);
      private static final Map<String, Biome.Precipitation> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(Biome.Precipitation::getName, (precipitation) -> {
         return precipitation;
      }));
      private final String name;

      private Precipitation(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static Biome.Precipitation byName(String name) {
         return (Biome.Precipitation)BY_NAME.get(name);
      }

      public String asString() {
         return this.name;
      }
   }

   public static enum Category implements StringIdentifiable {
      NONE("none"),
      TAIGA("taiga"),
      EXTREME_HILLS("extreme_hills"),
      JUNGLE("jungle"),
      MESA("mesa"),
      PLAINS("plains"),
      SAVANNA("savanna"),
      ICY("icy"),
      THEEND("the_end"),
      BEACH("beach"),
      FOREST("forest"),
      OCEAN("ocean"),
      DESERT("desert"),
      RIVER("river"),
      SWAMP("swamp"),
      MUSHROOM("mushroom"),
      NETHER("nether");

      public static final Codec<Biome.Category> CODEC = StringIdentifiable.createCodec(Biome.Category::values, Biome.Category::byName);
      private static final Map<String, Biome.Category> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(Biome.Category::getName, (category) -> {
         return category;
      }));
      private final String name;

      private Category(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static Biome.Category byName(String name) {
         return (Biome.Category)BY_NAME.get(name);
      }

      public String asString() {
         return this.name;
      }
   }
}
