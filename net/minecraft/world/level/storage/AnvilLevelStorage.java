package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilLevelStorage {
   private static final Logger LOGGER = LogManager.getLogger();

   static boolean convertLevel(LevelStorage.Session storageSession, ProgressListener progressListener) {
      progressListener.progressStagePercentage(0);
      List<File> list = Lists.newArrayList();
      List<File> list2 = Lists.newArrayList();
      List<File> list3 = Lists.newArrayList();
      File file = storageSession.getWorldDirectory(World.OVERWORLD);
      File file2 = storageSession.getWorldDirectory(World.NETHER);
      File file3 = storageSession.getWorldDirectory(World.END);
      LOGGER.info("Scanning folders...");
      addRegionFiles(file, list);
      if (file2.exists()) {
         addRegionFiles(file2, list2);
      }

      if (file3.exists()) {
         addRegionFiles(file3, list3);
      }

      int i = list.size() + list2.size() + list3.size();
      LOGGER.info((String)"Total conversion count is {}", (Object)i);
      DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
      RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, (ResourceManager)ResourceManager.Empty.INSTANCE, impl);
      SaveProperties saveProperties = storageSession.readLevelProperties(registryOps, DataPackSettings.SAFE_MODE);
      long l = saveProperties != null ? saveProperties.getGeneratorOptions().getSeed() : 0L;
      Registry<Biome> registry = impl.get(Registry.BIOME_KEY);
      Object biomeSource2;
      if (saveProperties != null && saveProperties.getGeneratorOptions().isFlatWorld()) {
         biomeSource2 = new FixedBiomeSource((Biome)registry.getOrThrow(BiomeKeys.PLAINS));
      } else {
         biomeSource2 = new VanillaLayeredBiomeSource(l, false, false, registry);
      }

      convertRegions(impl, new File(file, "region"), list, (BiomeSource)biomeSource2, 0, i, progressListener);
      convertRegions(impl, new File(file2, "region"), list2, new FixedBiomeSource((Biome)registry.getOrThrow(BiomeKeys.NETHER_WASTES)), list.size(), i, progressListener);
      convertRegions(impl, new File(file3, "region"), list3, new FixedBiomeSource((Biome)registry.getOrThrow(BiomeKeys.THE_END)), list.size() + list2.size(), i, progressListener);
      makeMcrLevelDatBackup(storageSession);
      storageSession.backupLevelDataFile(impl, saveProperties);
      return true;
   }

   private static void makeMcrLevelDatBackup(LevelStorage.Session storageSession) {
      File file = storageSession.getDirectory(WorldSavePath.LEVEL_DAT).toFile();
      if (!file.exists()) {
         LOGGER.warn("Unable to create level.dat_mcr backup");
      } else {
         File file2 = new File(file.getParent(), "level.dat_mcr");
         if (!file.renameTo(file2)) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
         }

      }
   }

   private static void convertRegions(DynamicRegistryManager.Impl registryManager, File directory, Iterable<File> files, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
      Iterator var7 = files.iterator();

      while(var7.hasNext()) {
         File file = (File)var7.next();
         convertRegion(registryManager, directory, file, biomeSource, i, j, progressListener);
         ++i;
         int k = (int)Math.round(100.0D * (double)i / (double)j);
         progressListener.progressStagePercentage(k);
      }

   }

   private static void convertRegion(DynamicRegistryManager.Impl registryManager, File directory, File file, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
      String string = file.getName();

      try {
         RegionFile regionFile = new RegionFile(file, directory, true);
         Throwable var9 = null;

         try {
            RegionFile regionFile2 = new RegionFile(new File(directory, string.substring(0, string.length() - ".mcr".length()) + ".mca"), directory, true);
            Throwable var11 = null;

            try {
               for(int k = 0; k < 32; ++k) {
                  int m;
                  for(m = 0; m < 32; ++m) {
                     ChunkPos chunkPos = new ChunkPos(k, m);
                     if (regionFile.hasChunk(chunkPos) && !regionFile2.hasChunk(chunkPos)) {
                        NbtCompound nbtCompound3;
                        try {
                           DataInputStream dataInputStream = regionFile.getChunkInputStream(chunkPos);
                           Throwable var17 = null;

                           try {
                              if (dataInputStream == null) {
                                 LOGGER.warn((String)"Failed to fetch input stream for chunk {}", (Object)chunkPos);
                                 continue;
                              }

                              nbtCompound3 = NbtIo.read((DataInput)dataInputStream);
                           } catch (Throwable var105) {
                              var17 = var105;
                              throw var105;
                           } finally {
                              if (dataInputStream != null) {
                                 if (var17 != null) {
                                    try {
                                       dataInputStream.close();
                                    } catch (Throwable var102) {
                                       var17.addSuppressed(var102);
                                    }
                                 } else {
                                    dataInputStream.close();
                                 }
                              }

                           }
                        } catch (IOException var107) {
                           LOGGER.warn((String)"Failed to read data for chunk {}", (Object)chunkPos, (Object)var107);
                           continue;
                        }

                        NbtCompound nbtCompound4 = nbtCompound3.getCompound("Level");
                        AlphaChunkIo.AlphaChunk alphaChunk = AlphaChunkIo.readAlphaChunk(nbtCompound4);
                        NbtCompound nbtCompound5 = new NbtCompound();
                        NbtCompound nbtCompound6 = new NbtCompound();
                        nbtCompound5.put("Level", nbtCompound6);
                        AlphaChunkIo.convertAlphaChunk(registryManager, alphaChunk, nbtCompound6, biomeSource);
                        DataOutputStream dataOutputStream = regionFile2.getChunkOutputStream(chunkPos);
                        Throwable var21 = null;

                        try {
                           NbtIo.write((NbtCompound)nbtCompound5, (DataOutput)dataOutputStream);
                        } catch (Throwable var103) {
                           var21 = var103;
                           throw var103;
                        } finally {
                           if (dataOutputStream != null) {
                              if (var21 != null) {
                                 try {
                                    dataOutputStream.close();
                                 } catch (Throwable var101) {
                                    var21.addSuppressed(var101);
                                 }
                              } else {
                                 dataOutputStream.close();
                              }
                           }

                        }
                     }
                  }

                  m = (int)Math.round(100.0D * (double)(i * 1024) / (double)(j * 1024));
                  int n = (int)Math.round(100.0D * (double)((k + 1) * 32 + i * 1024) / (double)(j * 1024));
                  if (n > m) {
                     progressListener.progressStagePercentage(n);
                  }
               }
            } catch (Throwable var108) {
               var11 = var108;
               throw var108;
            } finally {
               if (regionFile2 != null) {
                  if (var11 != null) {
                     try {
                        regionFile2.close();
                     } catch (Throwable var100) {
                        var11.addSuppressed(var100);
                     }
                  } else {
                     regionFile2.close();
                  }
               }

            }
         } catch (Throwable var110) {
            var9 = var110;
            throw var110;
         } finally {
            if (regionFile != null) {
               if (var9 != null) {
                  try {
                     regionFile.close();
                  } catch (Throwable var99) {
                     var9.addSuppressed(var99);
                  }
               } else {
                  regionFile.close();
               }
            }

         }
      } catch (IOException var112) {
         LOGGER.error((String)"Failed to upgrade region file {}", (Object)file, (Object)var112);
      }

   }

   private static void addRegionFiles(File worldDirectory, Collection<File> files) {
      File file = new File(worldDirectory, "region");
      File[] files2 = file.listFiles((filex, string) -> {
         return string.endsWith(".mcr");
      });
      if (files2 != null) {
         Collections.addAll(files, files2);
      }

   }
}
