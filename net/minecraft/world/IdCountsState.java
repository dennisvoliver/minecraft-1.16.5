package net.minecraft.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Iterator;
import net.minecraft.nbt.NbtCompound;

public class IdCountsState extends PersistentState {
   private final Object2IntMap<String> idCounts = new Object2IntOpenHashMap();

   public IdCountsState() {
      super("idcounts");
      this.idCounts.defaultReturnValue(-1);
   }

   public void fromTag(NbtCompound tag) {
      this.idCounts.clear();
      Iterator var2 = tag.getKeys().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         if (tag.contains(string, 99)) {
            this.idCounts.put(string, tag.getInt(string));
         }
      }

   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      ObjectIterator var2 = this.idCounts.object2IntEntrySet().iterator();

      while(var2.hasNext()) {
         Entry<String> entry = (Entry)var2.next();
         nbt.putInt((String)entry.getKey(), entry.getIntValue());
      }

      return nbt;
   }

   public int getNextMapId() {
      int i = this.idCounts.getInt("map") + 1;
      this.idCounts.put("map", i);
      this.markDirty();
      return i;
   }
}
