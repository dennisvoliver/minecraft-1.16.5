package net.minecraft.world.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class SerializingRegionBasedStorage<R> implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final StorageIoWorker worker;
   private final Long2ObjectMap<Optional<R>> loadedElements = new Long2ObjectOpenHashMap();
   private final LongLinkedOpenHashSet unsavedElements = new LongLinkedOpenHashSet();
   private final Function<Runnable, Codec<R>> codecFactory;
   private final Function<Runnable, R> factory;
   private final DataFixer dataFixer;
   private final DataFixTypes dataFixTypes;

   public SerializingRegionBasedStorage(File directory, Function<Runnable, Codec<R>> codecFactory, Function<Runnable, R> factory, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl) {
      this.codecFactory = codecFactory;
      this.factory = factory;
      this.dataFixer = dataFixer;
      this.dataFixTypes = dataFixTypes;
      this.worker = new StorageIoWorker(directory, bl, directory.getName());
   }

   protected void tick(BooleanSupplier shouldKeepTicking) {
      while(!this.unsavedElements.isEmpty() && shouldKeepTicking.getAsBoolean()) {
         ChunkPos chunkPos = ChunkSectionPos.from(this.unsavedElements.firstLong()).toChunkPos();
         this.save(chunkPos);
      }

   }

   @Nullable
   protected Optional<R> getIfLoaded(long pos) {
      return (Optional)this.loadedElements.get(pos);
   }

   protected Optional<R> get(long pos) {
      ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(pos);
      if (this.isPosInvalid(chunkSectionPos)) {
         return Optional.empty();
      } else {
         Optional<R> optional = this.getIfLoaded(pos);
         if (optional != null) {
            return optional;
         } else {
            this.loadDataAt(chunkSectionPos.toChunkPos());
            optional = this.getIfLoaded(pos);
            if (optional == null) {
               throw (IllegalStateException)Util.throwOrPause(new IllegalStateException());
            } else {
               return optional;
            }
         }
      }
   }

   protected boolean isPosInvalid(ChunkSectionPos pos) {
      return World.isOutOfBuildLimitVertically(ChunkSectionPos.getBlockCoord(pos.getSectionY()));
   }

   protected R getOrCreate(long pos) {
      Optional<R> optional = this.get(pos);
      if (optional.isPresent()) {
         return optional.get();
      } else {
         R object = this.factory.apply(() -> {
            this.onUpdate(pos);
         });
         this.loadedElements.put(pos, Optional.of(object));
         return object;
      }
   }

   private void loadDataAt(ChunkPos chunkPos) {
      this.update(chunkPos, NbtOps.INSTANCE, this.loadNbt(chunkPos));
   }

   @Nullable
   private NbtCompound loadNbt(ChunkPos pos) {
      try {
         return this.worker.getNbt(pos);
      } catch (IOException var3) {
         LOGGER.error((String)"Error reading chunk {} data from disk", (Object)pos, (Object)var3);
         return null;
      }
   }

   private <T> void update(ChunkPos pos, DynamicOps<T> dynamicOps, @Nullable T data) {
      if (data == null) {
         for(int i = 0; i < 16; ++i) {
            this.loadedElements.put(ChunkSectionPos.from(pos, i).asLong(), Optional.empty());
         }
      } else {
         Dynamic<T> dynamic = new Dynamic(dynamicOps, data);
         int j = getDataVersion(dynamic);
         int k = SharedConstants.getGameVersion().getWorldVersion();
         boolean bl = j != k;
         Dynamic<T> dynamic2 = this.dataFixer.update(this.dataFixTypes.getTypeReference(), dynamic, j, k);
         OptionalDynamic<T> optionalDynamic = dynamic2.get("Sections");

         for(int l = 0; l < 16; ++l) {
            long m = ChunkSectionPos.from(pos, l).asLong();
            Optional<R> optional = optionalDynamic.get(Integer.toString(l)).result().flatMap((dynamicx) -> {
               DataResult var10000 = ((Codec)this.codecFactory.apply(() -> {
                  this.onUpdate(m);
               })).parse(dynamicx);
               Logger var10001 = LOGGER;
               var10001.getClass();
               return var10000.resultOrPartial(var10001::error);
            });
            this.loadedElements.put(m, optional);
            optional.ifPresent((object) -> {
               this.onLoad(m);
               if (bl) {
                  this.onUpdate(m);
               }

            });
         }
      }

   }

   private void save(ChunkPos chunkPos) {
      Dynamic<NbtElement> dynamic = this.method_20367(chunkPos, NbtOps.INSTANCE);
      NbtElement nbtElement = (NbtElement)dynamic.getValue();
      if (nbtElement instanceof NbtCompound) {
         this.worker.setResult(chunkPos, (NbtCompound)nbtElement);
      } else {
         LOGGER.error((String)"Expected compound tag, got {}", (Object)nbtElement);
      }

   }

   private <T> Dynamic<T> method_20367(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
      Map<T, T> map = Maps.newHashMap();

      for(int i = 0; i < 16; ++i) {
         long l = ChunkSectionPos.from(chunkPos, i).asLong();
         this.unsavedElements.remove(l);
         Optional<R> optional = (Optional)this.loadedElements.get(l);
         if (optional != null && optional.isPresent()) {
            DataResult<T> dataResult = ((Codec)this.codecFactory.apply(() -> {
               this.onUpdate(l);
            })).encodeStart(dynamicOps, optional.get());
            String string = Integer.toString(i);
            Logger var10001 = LOGGER;
            var10001.getClass();
            dataResult.resultOrPartial(var10001::error).ifPresent((object) -> {
               map.put(dynamicOps.createString(string), object);
            });
         }
      }

      return new Dynamic(dynamicOps, dynamicOps.createMap((Map)ImmutableMap.of(dynamicOps.createString("Sections"), dynamicOps.createMap((Map)map), dynamicOps.createString("DataVersion"), dynamicOps.createInt(SharedConstants.getGameVersion().getWorldVersion()))));
   }

   protected void onLoad(long pos) {
   }

   protected void onUpdate(long pos) {
      Optional<R> optional = (Optional)this.loadedElements.get(pos);
      if (optional != null && optional.isPresent()) {
         this.unsavedElements.add(pos);
      } else {
         LOGGER.warn((String)"No data for position: {}", (Object)ChunkSectionPos.from(pos));
      }
   }

   private static int getDataVersion(Dynamic<?> dynamic) {
      return dynamic.get("DataVersion").asInt(1945);
   }

   public void method_20436(ChunkPos chunkPos) {
      if (!this.unsavedElements.isEmpty()) {
         for(int i = 0; i < 16; ++i) {
            long l = ChunkSectionPos.from(chunkPos, i).asLong();
            if (this.unsavedElements.contains(l)) {
               this.save(chunkPos);
               return;
            }
         }
      }

   }

   public void close() throws IOException {
      this.worker.close();
   }
}
