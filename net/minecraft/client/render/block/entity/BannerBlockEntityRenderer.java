package net.minecraft.client.render.block.entity;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class BannerBlockEntityRenderer extends BlockEntityRenderer<BannerBlockEntity> {
   private final ModelPart banner = createBanner();
   private final ModelPart pillar = new ModelPart(64, 64, 44, 0);
   private final ModelPart crossbar;

   public BannerBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
      this.pillar.addCuboid(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
      this.crossbar = new ModelPart(64, 64, 0, 42);
      this.crossbar.addCuboid(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
   }

   public static ModelPart createBanner() {
      ModelPart modelPart = new ModelPart(64, 64, 0, 0);
      modelPart.addCuboid(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
      return modelPart;
   }

   public void render(BannerBlockEntity bannerBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      List<Pair<BannerPattern, DyeColor>> list = bannerBlockEntity.getPatterns();
      if (list != null) {
         float g = 0.6666667F;
         boolean bl = bannerBlockEntity.getWorld() == null;
         matrixStack.push();
         long m;
         if (bl) {
            m = 0L;
            matrixStack.translate(0.5D, 0.5D, 0.5D);
            this.pillar.visible = true;
         } else {
            m = bannerBlockEntity.getWorld().getTime();
            BlockState blockState = bannerBlockEntity.getCachedState();
            float h;
            if (blockState.getBlock() instanceof BannerBlock) {
               matrixStack.translate(0.5D, 0.5D, 0.5D);
               h = (float)(-(Integer)blockState.get(BannerBlock.ROTATION) * 360) / 16.0F;
               matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(h));
               this.pillar.visible = true;
            } else {
               matrixStack.translate(0.5D, -0.1666666716337204D, 0.5D);
               h = -((Direction)blockState.get(WallBannerBlock.FACING)).asRotation();
               matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(h));
               matrixStack.translate(0.0D, -0.3125D, -0.4375D);
               this.pillar.visible = false;
            }
         }

         matrixStack.push();
         matrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
         VertexConsumer vertexConsumer = ModelLoader.BANNER_BASE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
         this.pillar.render(matrixStack, vertexConsumer, i, j);
         this.crossbar.render(matrixStack, vertexConsumer, i, j);
         BlockPos blockPos = bannerBlockEntity.getPos();
         float n = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + m, 100L) + f) / 100.0F;
         this.banner.pitch = (-0.0125F + 0.01F * MathHelper.cos(6.2831855F * n)) * 3.1415927F;
         this.banner.pivotY = -32.0F;
         method_29999(matrixStack, vertexConsumerProvider, i, j, this.banner, ModelLoader.BANNER_BASE, true, list);
         matrixStack.pop();
         matrixStack.pop();
      }
   }

   public static void method_29999(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, ModelPart modelPart, SpriteIdentifier spriteIdentifier, boolean bl, List<Pair<BannerPattern, DyeColor>> list) {
      renderCanvas(matrixStack, vertexConsumerProvider, i, j, modelPart, spriteIdentifier, bl, list, false);
   }

   public static void renderCanvas(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, List<Pair<BannerPattern, DyeColor>> patterns, boolean glint) {
      canvas.render(matrices, baseSprite.method_30001(vertexConsumers, RenderLayer::getEntitySolid, glint), light, overlay);

      for(int i = 0; i < 17 && i < patterns.size(); ++i) {
         Pair<BannerPattern, DyeColor> pair = (Pair)patterns.get(i);
         float[] fs = ((DyeColor)pair.getSecond()).getColorComponents();
         SpriteIdentifier spriteIdentifier = new SpriteIdentifier(isBanner ? TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE : TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, ((BannerPattern)pair.getFirst()).getSpriteId(isBanner));
         canvas.render(matrices, spriteIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityNoOutline), light, overlay, fs[0], fs[1], fs[2], 1.0F);
      }

   }
}
