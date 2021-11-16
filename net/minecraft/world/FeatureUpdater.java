package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public class FeatureUpdater {
   private static final Map<String, String> OLD_TO_NEW = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put("Village", "Village");
      hashMap.put("Mineshaft", "Mineshaft");
      hashMap.put("Mansion", "Mansion");
      hashMap.put("Igloo", "Temple");
      hashMap.put("Desert_Pyramid", "Temple");
      hashMap.put("Jungle_Pyramid", "Temple");
      hashMap.put("Swamp_Hut", "Temple");
      hashMap.put("Stronghold", "Stronghold");
      hashMap.put("Monument", "Monument");
      hashMap.put("Fortress", "Fortress");
      hashMap.put("EndCity", "EndCity");
   });
   private static final Map<String, String> ANCIENT_TO_OLD = (Map)Util.make(Maps.newHashMap(), (hashMap) -> {
      hashMap.put("Iglu", "Igloo");
      hashMap.put("TeDP", "Desert_Pyramid");
      hashMap.put("TeJP", "Jungle_Pyramid");
      hashMap.put("TeSH", "Swamp_Hut");
   });
   private final boolean needsUpdate;
   private final Map<String, Long2ObjectMap<NbtCompound>> featureIdToChunkNbt = Maps.newHashMap();
   private final Map<String, ChunkUpdateState> updateStates = Maps.newHashMap();
   private final List<String> field_17658;
   private final List<String> field_17659;

   public FeatureUpdater(@Nullable PersistentStateManager persistentStateManager, List<String> list, List<String> list2) {
      this.field_17658 = list;
      this.field_17659 = list2;
      this.init(persistentStateManager);
      boolean bl = false;

      String string;
      for(Iterator var5 = this.field_17659.iterator(); var5.hasNext(); bl |= this.featureIdToChunkNbt.get(string) != null) {
         string = (String)var5.next();
      }

      this.needsUpdate = bl;
   }

   public void markResolved(long l) {
      Iterator var3 = this.field_17658.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         ChunkUpdateState chunkUpdateState = (ChunkUpdateState)this.updateStates.get(string);
         if (chunkUpdateState != null && chunkUpdateState.isRemaining(l)) {
            chunkUpdateState.markResolved(l);
            chunkUpdateState.markDirty();
         }
      }

   }

   public NbtCompound getUpdatedReferences(NbtCompound nbt) {
      NbtCompound nbtCompound = nbt.getCompound("Level");
      ChunkPos chunkPos = new ChunkPos(nbtCompound.getInt("xPos"), nbtCompound.getInt("zPos"));
      if (this.needsUpdate(chunkPos.x, chunkPos.z)) {
         nbt = this.getUpdatedStarts(nbt, chunkPos);
      }

      NbtCompound nbtCompound2 = nbtCompound.getCompound("Structures");
      NbtCompound nbtCompound3 = nbtCompound2.getCompound("References");
      Iterator var6 = this.field_17659.iterator();

      while(true) {
         String string;
         StructureFeature structureFeature;
         do {
            do {
               if (!var6.hasNext()) {
                  nbtCompound2.put("References", nbtCompound3);
                  nbtCompound.put("Structures", nbtCompound2);
                  nbt.put("Level", nbtCompound);
                  return nbt;
               }

               string = (String)var6.next();
               structureFeature = (StructureFeature)StructureFeature.STRUCTURES.get(string.toLowerCase(Locale.ROOT));
            } while(nbtCompound3.contains(string, 12));
         } while(structureFeature == null);

         int i = true;
         LongList longList = new LongArrayList();

         for(int j = chunkPos.x - 8; j <= chunkPos.x + 8; ++j) {
            for(int k = chunkPos.z - 8; k <= chunkPos.z + 8; ++k) {
               if (this.needsUpdate(j, k, string)) {
                  longList.add(ChunkPos.toLong(j, k));
               }
            }
         }

         nbtCompound3.putLongArray(string, (List)longList);
      }
   }

   private boolean needsUpdate(int chunkX, int chunkZ, String id) {
      if (!this.needsUpdate) {
         return false;
      } else {
         return this.featureIdToChunkNbt.get(id) != null && ((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(id))).contains(ChunkPos.toLong(chunkX, chunkZ));
      }
   }

   private boolean needsUpdate(int chunkX, int chunkZ) {
      if (!this.needsUpdate) {
         return false;
      } else {
         Iterator var3 = this.field_17659.iterator();

         String string;
         do {
            if (!var3.hasNext()) {
               return false;
            }

            string = (String)var3.next();
         } while(this.featureIdToChunkNbt.get(string) == null || !((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(string))).isRemaining(ChunkPos.toLong(chunkX, chunkZ)));

         return true;
      }
   }

   private NbtCompound getUpdatedStarts(NbtCompound nbt, ChunkPos pos) {
      NbtCompound nbtCompound = nbt.getCompound("Level");
      NbtCompound nbtCompound2 = nbtCompound.getCompound("Structures");
      NbtCompound nbtCompound3 = nbtCompound2.getCompound("Starts");
      Iterator var6 = this.field_17659.iterator();

      while(var6.hasNext()) {
         String string = (String)var6.next();
         Long2ObjectMap<NbtCompound> long2ObjectMap = (Long2ObjectMap)this.featureIdToChunkNbt.get(string);
         if (long2ObjectMap != null) {
            long l = pos.toLong();
            if (((ChunkUpdateState)this.updateStates.get(OLD_TO_NEW.get(string))).isRemaining(l)) {
               NbtCompound nbtCompound4 = (NbtCompound)long2ObjectMap.get(l);
               if (nbtCompound4 != null) {
                  nbtCompound3.put(string, nbtCompound4);
               }
            }
         }
      }

      nbtCompound2.put("Starts", nbtCompound3);
      nbtCompound.put("Structures", nbtCompound2);
      nbt.put("Level", nbtCompound);
      return nbt;
   }

   private void init(@Nullable PersistentStateManager persistentStateManager) {
      if (persistentStateManager != null) {
         Iterator var2 = this.field_17658.iterator();

         while(var2.hasNext()) {
            String string = (String)var2.next();
            NbtCompound nbtCompound = new NbtCompound();

            try {
               nbtCompound = persistentStateManager.readNbt(string, 1493).getCompound("data").getCompound("Features");
               if (nbtCompound.isEmpty()) {
                  continue;
               }
            } catch (IOException var13) {
            }

            Iterator var5 = nbtCompound.getKeys().iterator();

            while(var5.hasNext()) {
               String string2 = (String)var5.next();
               NbtCompound nbtCompound2 = nbtCompound.getCompound(string2);
               long l = ChunkPos.toLong(nbtCompound2.getInt("ChunkX"), nbtCompound2.getInt("ChunkZ"));
               NbtList nbtList = nbtCompound2.getList("Children", 10);
               String string5;
               if (!nbtList.isEmpty()) {
                  string5 = nbtList.getCompound(0).getString("id");
                  String string4 = (String)ANCIENT_TO_OLD.get(string5);
                  if (string4 != null) {
                     nbtCompound2.putString("id", string4);
                  }
               }

               string5 = nbtCompound2.getString("id");
               ((Long2ObjectMap)this.featureIdToChunkNbt.computeIfAbsent(string5, (stringx) -> {
                  return new Long2ObjectOpenHashMap();
               })).put(l, nbtCompound2);
            }

            String string6 = string + "_index";
            ChunkUpdateState chunkUpdateState = (ChunkUpdateState)persistentStateManager.getOrCreate(() -> {
               return new ChunkUpdateState(string6);
            }, string6);
            if (!chunkUpdateState.getAll().isEmpty()) {
               this.updateStates.put(string, chunkUpdateState);
            } else {
               ChunkUpdateState chunkUpdateState2 = new ChunkUpdateState(string6);
               this.updateStates.put(string, chunkUpdateState2);
               Iterator var17 = nbtCompound.getKeys().iterator();

               while(var17.hasNext()) {
                  String string7 = (String)var17.next();
                  NbtCompound nbtCompound3 = nbtCompound.getCompound(string7);
                  chunkUpdateState2.add(ChunkPos.toLong(nbtCompound3.getInt("ChunkX"), nbtCompound3.getInt("ChunkZ")));
               }

               chunkUpdateState2.markDirty();
            }
         }

      }
   }

   public static FeatureUpdater create(RegistryKey<World> world, @Nullable PersistentStateManager persistentStateManager) {
      if (world == World.OVERWORLD) {
         return new FeatureUpdater(persistentStateManager, ImmutableList.of("Monument", "Stronghold", "Village", "Mineshaft", "Temple", "Mansion"), ImmutableList.of("Village", "Mineshaft", "Mansion", "Igloo", "Desert_Pyramid", "Jungle_Pyramid", "Swamp_Hut", "Stronghold", "Monument"));
      } else {
         ImmutableList list2;
         if (world == World.NETHER) {
            list2 = ImmutableList.of("Fortress");
            return new FeatureUpdater(persistentStateManager, list2, list2);
         } else if (world == World.END) {
            list2 = ImmutableList.of("EndCity");
            return new FeatureUpdater(persistentStateManager, list2, list2);
         } else {
            throw new RuntimeException(String.format("Unknown dimension type : %s", world));
         }
      }
   }
}
