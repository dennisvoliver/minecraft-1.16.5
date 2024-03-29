package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * A utility for combining multiple VertexConsumers into one.
 */
@Environment(EnvType.CLIENT)
public class VertexConsumers {
   public static VertexConsumer union(VertexConsumer first, VertexConsumer second) {
      return new VertexConsumers.Dual(first, second);
   }

   @Environment(EnvType.CLIENT)
   static class Dual implements VertexConsumer {
      private final VertexConsumer first;
      private final VertexConsumer second;

      public Dual(VertexConsumer first, VertexConsumer second) {
         if (first == second) {
            throw new IllegalArgumentException("Duplicate delegates");
         } else {
            this.first = first;
            this.second = second;
         }
      }

      public VertexConsumer vertex(double x, double y, double z) {
         this.first.vertex(x, y, z);
         this.second.vertex(x, y, z);
         return this;
      }

      public VertexConsumer color(int red, int green, int blue, int alpha) {
         this.first.color(red, green, blue, alpha);
         this.second.color(red, green, blue, alpha);
         return this;
      }

      public VertexConsumer texture(float u, float v) {
         this.first.texture(u, v);
         this.second.texture(u, v);
         return this;
      }

      public VertexConsumer overlay(int u, int v) {
         this.first.overlay(u, v);
         this.second.overlay(u, v);
         return this;
      }

      public VertexConsumer light(int u, int v) {
         this.first.light(u, v);
         this.second.light(u, v);
         return this;
      }

      public VertexConsumer normal(float x, float y, float z) {
         this.first.normal(x, y, z);
         this.second.normal(x, y, z);
         return this;
      }

      public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
         this.first.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
         this.second.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
      }

      public void next() {
         this.first.next();
         this.second.next();
      }
   }
}
