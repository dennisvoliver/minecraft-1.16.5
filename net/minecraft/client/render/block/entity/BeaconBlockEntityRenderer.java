package net.minecraft.client.render.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class BeaconBlockEntityRenderer extends BlockEntityRenderer<BeaconBlockEntity> {
   public static final Identifier BEAM_TEXTURE = new Identifier("textures/entity/beacon_beam.png");

   public BeaconBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
   }

   public void render(BeaconBlockEntity beaconBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      long l = beaconBlockEntity.getWorld().getTime();
      List<BeaconBlockEntity.BeamSegment> list = beaconBlockEntity.getBeamSegments();
      int k = 0;

      for(int m = 0; m < list.size(); ++m) {
         BeaconBlockEntity.BeamSegment beamSegment = (BeaconBlockEntity.BeamSegment)list.get(m);
         renderBeam(matrixStack, vertexConsumerProvider, f, l, k, m == list.size() - 1 ? 1024 : beamSegment.getHeight(), beamSegment.getColor());
         k += beamSegment.getHeight();
      }

   }

   private static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, float[] color) {
      renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0F, worldTime, yOffset, maxY, color, 0.2F, 0.25F);
   }

   public static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, float[] color, float innerRadius, float outerRadius) {
      int i = yOffset + maxY;
      matrices.push();
      matrices.translate(0.5D, 0.0D, 0.5D);
      float f = (float)Math.floorMod(worldTime, 40L) + tickDelta;
      float g = maxY < 0 ? f : -f;
      float h = MathHelper.fractionalPart(g * 0.2F - (float)MathHelper.floor(g * 0.1F));
      float j = color[0];
      float k = color[1];
      float l = color[2];
      matrices.push();
      matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(f * 2.25F - 45.0F));
      float y = 0.0F;
      float ab = 0.0F;
      float ac = -innerRadius;
      float r = 0.0F;
      float s = 0.0F;
      float t = -innerRadius;
      float ag = 0.0F;
      float ah = 1.0F;
      float ai = -1.0F + h;
      float aj = (float)maxY * heightScale * (0.5F / innerRadius) + ai;
      method_22741(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, false)), j, k, l, 1.0F, yOffset, i, 0.0F, innerRadius, innerRadius, 0.0F, ac, 0.0F, 0.0F, t, 0.0F, 1.0F, aj, ai);
      matrices.pop();
      y = -outerRadius;
      float z = -outerRadius;
      ab = -outerRadius;
      ac = -outerRadius;
      ag = 0.0F;
      ah = 1.0F;
      ai = -1.0F + h;
      aj = (float)maxY * heightScale + ai;
      method_22741(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, true)), j, k, l, 0.125F, yOffset, i, y, z, outerRadius, ab, ac, outerRadius, outerRadius, outerRadius, 0.0F, 1.0F, aj, ai);
      matrices.pop();
   }

   private static void method_22741(MatrixStack matrixStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u, float v, float w) {
      MatrixStack.Entry entry = matrixStack.peek();
      Matrix4f matrix4f = entry.getModel();
      Matrix3f matrix3f = entry.getNormal();
      method_22740(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, l, m, n, o, t, u, v, w);
      method_22740(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, r, s, p, q, t, u, v, w);
      method_22740(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, n, o, r, s, t, u, v, w);
      method_22740(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, p, q, l, m, t, u, v, w);
   }

   private static void method_22740(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s) {
      method_23076(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, l, m, q, r);
      method_23076(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, l, m, q, s);
      method_23076(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, n, o, p, s);
      method_23076(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, n, o, p, r);
   }

   private static void method_23076(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, float k, float l, float m, float n) {
      vertexConsumer.vertex(matrix4f, k, (float)j, l).color(f, g, h, i).texture(m, n).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
   }

   public boolean rendersOutsideBoundingBox(BeaconBlockEntity beaconBlockEntity) {
      return true;
   }
}
