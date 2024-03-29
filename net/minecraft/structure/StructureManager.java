package net.minecraft.structure;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StructureManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<Identifier, Structure> structures = Maps.newHashMap();
   private final DataFixer dataFixer;
   private ResourceManager field_25189;
   private final Path generatedPath;

   public StructureManager(ResourceManager resourceManager, LevelStorage.Session session, DataFixer dataFixer) {
      this.field_25189 = resourceManager;
      this.dataFixer = dataFixer;
      this.generatedPath = session.getDirectory(WorldSavePath.GENERATED).normalize();
   }

   public Structure getStructureOrBlank(Identifier id) {
      Structure structure = this.getStructure(id);
      if (structure == null) {
         structure = new Structure();
         this.structures.put(id, structure);
      }

      return structure;
   }

   @Nullable
   public Structure getStructure(Identifier identifier) {
      return (Structure)this.structures.computeIfAbsent(identifier, (identifierx) -> {
         Structure structure = this.loadStructureFromFile(identifierx);
         return structure != null ? structure : this.loadStructureFromResource(identifierx);
      });
   }

   public void method_29300(ResourceManager resourceManager) {
      this.field_25189 = resourceManager;
      this.structures.clear();
   }

   @Nullable
   private Structure loadStructureFromResource(Identifier id) {
      Identifier identifier = new Identifier(id.getNamespace(), "structures/" + id.getPath() + ".nbt");

      try {
         Resource resource = this.field_25189.getResource(identifier);
         Throwable var4 = null;

         Structure var5;
         try {
            var5 = this.readStructure(resource.getInputStream());
         } catch (Throwable var16) {
            var4 = var16;
            throw var16;
         } finally {
            if (resource != null) {
               if (var4 != null) {
                  try {
                     resource.close();
                  } catch (Throwable var15) {
                     var4.addSuppressed(var15);
                  }
               } else {
                  resource.close();
               }
            }

         }

         return var5;
      } catch (FileNotFoundException var18) {
         return null;
      } catch (Throwable var19) {
         LOGGER.error((String)"Couldn't load structure {}: {}", (Object)id, (Object)var19.toString());
         return null;
      }
   }

   @Nullable
   private Structure loadStructureFromFile(Identifier id) {
      if (!this.generatedPath.toFile().isDirectory()) {
         return null;
      } else {
         Path path = this.getAndCheckStructurePath(id, ".nbt");

         try {
            InputStream inputStream = new FileInputStream(path.toFile());
            Throwable var4 = null;

            Structure var5;
            try {
               var5 = this.readStructure(inputStream);
            } catch (Throwable var16) {
               var4 = var16;
               throw var16;
            } finally {
               if (inputStream != null) {
                  if (var4 != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var15) {
                        var4.addSuppressed(var15);
                     }
                  } else {
                     inputStream.close();
                  }
               }

            }

            return var5;
         } catch (FileNotFoundException var18) {
            return null;
         } catch (IOException var19) {
            LOGGER.error((String)"Couldn't load structure from {}", (Object)path, (Object)var19);
            return null;
         }
      }
   }

   private Structure readStructure(InputStream structureInputStream) throws IOException {
      NbtCompound nbtCompound = NbtIo.readCompressed(structureInputStream);
      return this.createStructure(nbtCompound);
   }

   public Structure createStructure(NbtCompound nbt) {
      if (!nbt.contains("DataVersion", 99)) {
         nbt.putInt("DataVersion", 500);
      }

      Structure structure = new Structure();
      structure.readNbt(NbtHelper.update(this.dataFixer, DataFixTypes.STRUCTURE, nbt, nbt.getInt("DataVersion")));
      return structure;
   }

   public boolean saveStructure(Identifier id) {
      Structure structure = (Structure)this.structures.get(id);
      if (structure == null) {
         return false;
      } else {
         Path path = this.getAndCheckStructurePath(id, ".nbt");
         Path path2 = path.getParent();
         if (path2 == null) {
            return false;
         } else {
            try {
               Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath() : path2);
            } catch (IOException var19) {
               LOGGER.error((String)"Failed to create parent directory: {}", (Object)path2);
               return false;
            }

            NbtCompound nbtCompound = structure.writeNbt(new NbtCompound());

            try {
               OutputStream outputStream = new FileOutputStream(path.toFile());
               Throwable var7 = null;

               try {
                  NbtIo.writeCompressed(nbtCompound, (OutputStream)outputStream);
               } catch (Throwable var18) {
                  var7 = var18;
                  throw var18;
               } finally {
                  if (outputStream != null) {
                     if (var7 != null) {
                        try {
                           outputStream.close();
                        } catch (Throwable var17) {
                           var7.addSuppressed(var17);
                        }
                     } else {
                        outputStream.close();
                     }
                  }

               }

               return true;
            } catch (Throwable var21) {
               return false;
            }
         }
      }
   }

   public Path getStructurePath(Identifier id, String extension) {
      try {
         Path path = this.generatedPath.resolve(id.getNamespace());
         Path path2 = path.resolve("structures");
         return FileNameUtil.getResourcePath(path2, id.getPath(), extension);
      } catch (InvalidPathException var5) {
         throw new InvalidIdentifierException("Invalid resource path: " + id, var5);
      }
   }

   private Path getAndCheckStructurePath(Identifier id, String extension) {
      if (id.getPath().contains("//")) {
         throw new InvalidIdentifierException("Invalid resource path: " + id);
      } else {
         Path path = this.getStructurePath(id, extension);
         if (path.startsWith(this.generatedPath) && FileNameUtil.isNormal(path) && FileNameUtil.isAllowedName(path)) {
            return path;
         } else {
            throw new InvalidIdentifierException("Invalid resource path: " + path);
         }
      }
   }

   public void unloadStructure(Identifier id) {
      this.structures.remove(id);
   }
}
