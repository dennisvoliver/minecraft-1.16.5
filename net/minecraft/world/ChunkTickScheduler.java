package net.minecraft.world;

import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;

public class ChunkTickScheduler<T> implements TickScheduler<T> {
   protected final Predicate<T> shouldExclude;
   private final ChunkPos pos;
   private final ShortList[] scheduledPositions;

   public ChunkTickScheduler(Predicate<T> shouldExclude, ChunkPos pos) {
      this(shouldExclude, pos, new NbtList());
   }

   public ChunkTickScheduler(Predicate<T> shouldExclude, ChunkPos pos, NbtList tag) {
      this.scheduledPositions = new ShortList[16];
      this.shouldExclude = shouldExclude;
      this.pos = pos;

      for(int i = 0; i < tag.size(); ++i) {
         NbtList nbtList = tag.getList(i);

         for(int j = 0; j < nbtList.size(); ++j) {
            Chunk.getList(this.scheduledPositions, i).add(nbtList.getShort(j));
         }
      }

   }

   public NbtList toNbt() {
      return ChunkSerializer.toNbt(this.scheduledPositions);
   }

   public void tick(TickScheduler<T> scheduler, Function<BlockPos, T> dataMapper) {
      for(int i = 0; i < this.scheduledPositions.length; ++i) {
         if (this.scheduledPositions[i] != null) {
            ShortListIterator var4 = this.scheduledPositions[i].iterator();

            while(var4.hasNext()) {
               Short short_ = (Short)var4.next();
               BlockPos blockPos = ProtoChunk.joinBlockPos(short_, i, this.pos);
               scheduler.schedule(blockPos, dataMapper.apply(blockPos), 0);
            }

            this.scheduledPositions[i].clear();
         }
      }

   }

   public boolean isScheduled(BlockPos pos, T object) {
      return false;
   }

   public void schedule(BlockPos pos, T object, int delay, TickPriority priority) {
      Chunk.getList(this.scheduledPositions, pos.getY() >> 4).add(ProtoChunk.getPackedSectionRelative(pos));
   }

   public boolean isTicking(BlockPos pos, T object) {
      return false;
   }
}
