package net.minecraft.client.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
   private int vertexBufferId;
   private final VertexFormat format;
   private int vertexCount;

   public VertexBuffer(VertexFormat format) {
      this.format = format;
      RenderSystem.glGenBuffers((integer) -> {
         this.vertexBufferId = integer;
      });
   }

   public void bind() {
      RenderSystem.glBindBuffer(34962, () -> {
         return this.vertexBufferId;
      });
   }

   public void upload(BufferBuilder buffer) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this.uploadInternal(buffer);
         });
      } else {
         this.uploadInternal(buffer);
      }

   }

   public CompletableFuture<Void> submitUpload(BufferBuilder buffer) {
      if (!RenderSystem.isOnRenderThread()) {
         return CompletableFuture.runAsync(() -> {
            this.uploadInternal(buffer);
         }, (runnable) -> {
            RenderSystem.recordRenderCall(runnable::run);
         });
      } else {
         this.uploadInternal(buffer);
         return CompletableFuture.completedFuture((Object)null);
      }
   }

   private void uploadInternal(BufferBuilder buffer) {
      Pair<BufferBuilder.DrawArrayParameters, ByteBuffer> pair = buffer.popData();
      if (this.vertexBufferId != -1) {
         ByteBuffer byteBuffer = (ByteBuffer)pair.getSecond();
         this.vertexCount = byteBuffer.remaining() / this.format.getVertexSize();
         this.bind();
         RenderSystem.glBufferData(34962, byteBuffer, 35044);
         unbind();
      }
   }

   public void draw(Matrix4f matrix, int mode) {
      RenderSystem.pushMatrix();
      RenderSystem.loadIdentity();
      RenderSystem.multMatrix(matrix);
      RenderSystem.drawArrays(mode, 0, this.vertexCount);
      RenderSystem.popMatrix();
   }

   public static void unbind() {
      RenderSystem.glBindBuffer(34962, () -> {
         return 0;
      });
   }

   public void close() {
      if (this.vertexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.vertexBufferId);
         this.vertexBufferId = -1;
      }

   }
}
