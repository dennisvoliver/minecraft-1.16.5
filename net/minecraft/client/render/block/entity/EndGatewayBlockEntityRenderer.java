package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class EndGatewayBlockEntityRenderer extends EndPortalBlockEntityRenderer<EndGatewayBlockEntity> {
   private static final Identifier BEAM_TEXTURE = new Identifier("textures/entity/end_gateway_beam.png");

   public EndGatewayBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
   }

   public void render(EndGatewayBlockEntity endGatewayBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      if (endGatewayBlockEntity.isRecentlyGenerated() || endGatewayBlockEntity.needsCooldownBeforeTeleporting()) {
         float g = endGatewayBlockEntity.isRecentlyGenerated() ? endGatewayBlockEntity.getRecentlyGeneratedBeamHeight(f) : endGatewayBlockEntity.getCooldownBeamHeight(f);
         double d = endGatewayBlockEntity.isRecentlyGenerated() ? 256.0D : 50.0D;
         g = MathHelper.sin(g * 3.1415927F);
         int k = MathHelper.floor((double)g * d);
         float[] fs = endGatewayBlockEntity.isRecentlyGenerated() ? DyeColor.MAGENTA.getColorComponents() : DyeColor.PURPLE.getColorComponents();
         long l = endGatewayBlockEntity.getWorld().getTime();
         BeaconBlockEntityRenderer.renderBeam(matrixStack, vertexConsumerProvider, BEAM_TEXTURE, f, g, l, 0, k, fs, 0.15F, 0.175F);
         BeaconBlockEntityRenderer.renderBeam(matrixStack, vertexConsumerProvider, BEAM_TEXTURE, f, g, l, 0, -k, fs, 0.15F, 0.175F);
      }

      super.render((EndPortalBlockEntity)endGatewayBlockEntity, f, matrixStack, vertexConsumerProvider, i, j);
   }

   protected int method_3592(double d) {
      return super.method_3592(d) + 1;
   }

   protected float method_3594() {
      return 1.0F;
   }
}
