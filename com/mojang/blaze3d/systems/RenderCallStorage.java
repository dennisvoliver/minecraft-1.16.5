package com.mojang.blaze3d.systems;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A storage of render calls with recording. It exposes
 * one queue: the recording queue.
 * 
 * <p>This storage appears to be a work in progress,
 * as more queues are exposed in future versions.
 */
@Environment(EnvType.CLIENT)
public class RenderCallStorage {
   private final List<ConcurrentLinkedQueue<RenderCall>> recordingQueues = ImmutableList.of(new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue());
   private volatile int field_20454;
   private volatile int field_20455;
   private volatile int field_20456;

   public RenderCallStorage() {
      this.field_20454 = this.field_20455 = this.field_20456 + 1;
   }
}
