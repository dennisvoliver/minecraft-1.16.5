package net.minecraft.util.registry;

import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.ConfiguredSurfaceBuilders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stores a few hardcoded registries with builtin values for datapack-loadable registries,
 * from which a registry tracker can create a new dynamic registry.
 */
public class BuiltinRegistries {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Map<Identifier, Supplier<?>> DEFAULT_VALUE_SUPPLIERS = Maps.newLinkedHashMap();
   private static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry(RegistryKey.ofRegistry(new Identifier("root")), Lifecycle.experimental());
   public static final Registry<? extends Registry<?>> REGISTRIES;
   public static final Registry<ConfiguredSurfaceBuilder<?>> CONFIGURED_SURFACE_BUILDER;
   public static final Registry<ConfiguredCarver<?>> CONFIGURED_CARVER;
   public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE;
   public static final Registry<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE;
   public static final Registry<StructureProcessorList> STRUCTURE_PROCESSOR_LIST;
   public static final Registry<StructurePool> STRUCTURE_POOL;
   public static final Registry<Biome> BIOME;
   public static final Registry<ChunkGeneratorSettings> CHUNK_GENERATOR_SETTINGS;

   private static <T> Registry<T> addRegistry(RegistryKey<? extends Registry<T>> registryRef, Supplier<T> defaultValueSupplier) {
      return addRegistry(registryRef, Lifecycle.stable(), defaultValueSupplier);
   }

   private static <T> Registry<T> addRegistry(RegistryKey<? extends Registry<T>> registryRef, Lifecycle lifecycle, Supplier<T> defaultValueSupplier) {
      return addRegistry(registryRef, new SimpleRegistry(registryRef, lifecycle), defaultValueSupplier, lifecycle);
   }

   private static <T, R extends MutableRegistry<T>> R addRegistry(RegistryKey<? extends Registry<T>> registryRef, R registry, Supplier<T> defaultValueSupplier, Lifecycle lifecycle) {
      Identifier identifier = registryRef.getValue();
      DEFAULT_VALUE_SUPPLIERS.put(identifier, defaultValueSupplier);
      MutableRegistry<R> mutableRegistry = ROOT;
      return (MutableRegistry)mutableRegistry.add(registryRef, registry, lifecycle);
   }

   public static <T> T add(Registry<? super T> registry, String id, T object) {
      return add(registry, new Identifier(id), object);
   }

   public static <V, T extends V> T add(Registry<V> registry, Identifier id, T object) {
      return ((MutableRegistry)registry).add(RegistryKey.of(registry.getKey(), id), object, Lifecycle.stable());
   }

   public static <V, T extends V> T set(Registry<V> registry, int rawId, RegistryKey<V> key, T object) {
      return ((MutableRegistry)registry).set(rawId, key, object, Lifecycle.stable());
   }

   public static void init() {
   }

   static {
      REGISTRIES = ROOT;
      CONFIGURED_SURFACE_BUILDER = addRegistry(Registry.CONFIGURED_SURFACE_BUILDER_WORLDGEN, () -> {
         return ConfiguredSurfaceBuilders.NOPE;
      });
      CONFIGURED_CARVER = addRegistry(Registry.CONFIGURED_CARVER_WORLDGEN, () -> {
         return ConfiguredCarvers.CAVE;
      });
      CONFIGURED_FEATURE = addRegistry(Registry.CONFIGURED_FEATURE_WORLDGEN, () -> {
         return ConfiguredFeatures.OAK;
      });
      CONFIGURED_STRUCTURE_FEATURE = addRegistry(Registry.CONFIGURED_STRUCTURE_FEATURE_WORLDGEN, () -> {
         return ConfiguredStructureFeatures.MINESHAFT;
      });
      STRUCTURE_PROCESSOR_LIST = addRegistry(Registry.PROCESSOR_LIST_WORLDGEN, () -> {
         return StructureProcessorLists.ZOMBIE_PLAINS;
      });
      STRUCTURE_POOL = addRegistry(Registry.TEMPLATE_POOL_WORLDGEN, StructurePools::initDefaultPools);
      BIOME = addRegistry(Registry.BIOME_KEY, () -> {
         return BuiltinBiomes.PLAINS;
      });
      CHUNK_GENERATOR_SETTINGS = addRegistry(Registry.NOISE_SETTINGS_WORLDGEN, ChunkGeneratorSettings::getInstance);
      DEFAULT_VALUE_SUPPLIERS.forEach((identifier, supplier) -> {
         if (supplier.get() == null) {
            LOGGER.error((String)"Unable to bootstrap registry '{}'", (Object)identifier);
         }

      });
      Registry.validate(ROOT);
   }
}
