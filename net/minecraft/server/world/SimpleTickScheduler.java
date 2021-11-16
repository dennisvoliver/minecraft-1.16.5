package net.minecraft.server.world;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;
import net.minecraft.world.TickScheduler;

public class SimpleTickScheduler<T> implements TickScheduler<T> {
   private final List<SimpleTickScheduler.Tick<T>> scheduledTicks;
   private final Function<T, Identifier> identifierProvider;

   public SimpleTickScheduler(Function<T, Identifier> identifierProvider, List<ScheduledTick<T>> scheduledTicks, long startTime) {
      this(identifierProvider, (List)scheduledTicks.stream().map((scheduledTick) -> {
         return new SimpleTickScheduler.Tick(scheduledTick.getObject(), scheduledTick.pos, (int)(scheduledTick.time - startTime), scheduledTick.priority);
      }).collect(Collectors.toList()));
   }

   private SimpleTickScheduler(Function<T, Identifier> identifierProvider, List<SimpleTickScheduler.Tick<T>> scheduledTicks) {
      this.scheduledTicks = scheduledTicks;
      this.identifierProvider = identifierProvider;
   }

   public boolean isScheduled(BlockPos pos, T object) {
      return false;
   }

   public void schedule(BlockPos pos, T object, int delay, TickPriority priority) {
      this.scheduledTicks.add(new SimpleTickScheduler.Tick(object, pos, delay, priority));
   }

   public boolean isTicking(BlockPos pos, T object) {
      return false;
   }

   public NbtList toNbt() {
      NbtList nbtList = new NbtList();
      Iterator var2 = this.scheduledTicks.iterator();

      while(var2.hasNext()) {
         SimpleTickScheduler.Tick<T> tick = (SimpleTickScheduler.Tick)var2.next();
         NbtCompound nbtCompound = new NbtCompound();
         nbtCompound.putString("i", ((Identifier)this.identifierProvider.apply(tick.object)).toString());
         nbtCompound.putInt("x", tick.pos.getX());
         nbtCompound.putInt("y", tick.pos.getY());
         nbtCompound.putInt("z", tick.pos.getZ());
         nbtCompound.putInt("t", tick.delay);
         nbtCompound.putInt("p", tick.priority.getIndex());
         nbtList.add(nbtCompound);
      }

      return nbtList;
   }

   public static <T> SimpleTickScheduler<T> fromNbt(NbtList ticks, Function<T, Identifier> function, Function<Identifier, T> function2) {
      List<SimpleTickScheduler.Tick<T>> list = Lists.newArrayList();

      for(int i = 0; i < ticks.size(); ++i) {
         NbtCompound nbtCompound = ticks.getCompound(i);
         T object = function2.apply(new Identifier(nbtCompound.getString("i")));
         if (object != null) {
            BlockPos blockPos = new BlockPos(nbtCompound.getInt("x"), nbtCompound.getInt("y"), nbtCompound.getInt("z"));
            list.add(new SimpleTickScheduler.Tick(object, blockPos, nbtCompound.getInt("t"), TickPriority.byIndex(nbtCompound.getInt("p"))));
         }
      }

      return new SimpleTickScheduler(function, list);
   }

   public void scheduleTo(TickScheduler<T> scheduler) {
      this.scheduledTicks.forEach((tick) -> {
         scheduler.schedule(tick.pos, tick.object, tick.delay, tick.priority);
      });
   }

   static class Tick<T> {
      private final T object;
      public final BlockPos pos;
      public final int delay;
      public final TickPriority priority;

      private Tick(T object, BlockPos pos, int delay, TickPriority priority) {
         this.object = object;
         this.pos = pos;
         this.delay = delay;
         this.priority = priority;
      }

      public String toString() {
         return this.object + ": " + this.pos + ", " + this.delay + ", " + this.priority;
      }
   }
}
