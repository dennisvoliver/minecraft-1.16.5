package net.minecraft.world.biome.source;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeLayerSampler {
   private static final Logger LOGGER = LogManager.getLogger();
   private final CachingLayerSampler sampler;

   public BiomeLayerSampler(LayerFactory<CachingLayerSampler> layerFactory) {
      this.sampler = (CachingLayerSampler)layerFactory.make();
   }

   public Biome sample(Registry<Biome> biomeRegistry, int x, int z) {
      int i = this.sampler.sample(x, z);
      RegistryKey<Biome> registryKey = BuiltinBiomes.fromRawId(i);
      if (registryKey == null) {
         throw new IllegalStateException("Unknown biome id emitted by layers: " + i);
      } else {
         Biome biome = (Biome)biomeRegistry.get(registryKey);
         if (biome == null) {
            if (SharedConstants.isDevelopment) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException("Unknown biome id: " + i));
            } else {
               LOGGER.warn((String)"Unknown biome id: ", (Object)i);
               return (Biome)biomeRegistry.get(BuiltinBiomes.fromRawId(0));
            }
         } else {
            return biome;
         }
      }
   }
}
