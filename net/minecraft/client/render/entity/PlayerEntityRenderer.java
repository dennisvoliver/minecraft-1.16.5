package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.Deadmau5FeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckStingersFeatureRenderer;
import net.minecraft.client.render.entity.feature.TridentRiptideFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class PlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
   public PlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      this(entityRenderDispatcher, false);
   }

   public PlayerEntityRenderer(EntityRenderDispatcher dispatcher, boolean bl) {
      super(dispatcher, new PlayerEntityModel(0.0F, bl), 0.5F);
      this.addFeature(new ArmorFeatureRenderer(this, new BipedEntityModel(0.5F), new BipedEntityModel(1.0F)));
      this.addFeature(new HeldItemFeatureRenderer(this));
      this.addFeature(new StuckArrowsFeatureRenderer(this));
      this.addFeature(new Deadmau5FeatureRenderer(this));
      this.addFeature(new CapeFeatureRenderer(this));
      this.addFeature(new HeadFeatureRenderer(this));
      this.addFeature(new ElytraFeatureRenderer(this));
      this.addFeature(new ShoulderParrotFeatureRenderer(this));
      this.addFeature(new TridentRiptideFeatureRenderer(this));
      this.addFeature(new StuckStingersFeatureRenderer(this));
   }

   public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      this.setModelPose(abstractClientPlayerEntity);
      super.render((LivingEntity)abstractClientPlayerEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   public Vec3d getPositionOffset(AbstractClientPlayerEntity abstractClientPlayerEntity, float f) {
      return abstractClientPlayerEntity.isInSneakingPose() ? new Vec3d(0.0D, -0.125D, 0.0D) : super.getPositionOffset(abstractClientPlayerEntity, f);
   }

   private void setModelPose(AbstractClientPlayerEntity player) {
      PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = (PlayerEntityModel)this.getModel();
      if (player.isSpectator()) {
         playerEntityModel.setVisible(false);
         playerEntityModel.head.visible = true;
         playerEntityModel.hat.visible = true;
      } else {
         playerEntityModel.setVisible(true);
         playerEntityModel.hat.visible = player.isPartVisible(PlayerModelPart.HAT);
         playerEntityModel.jacket.visible = player.isPartVisible(PlayerModelPart.JACKET);
         playerEntityModel.leftPants.visible = player.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
         playerEntityModel.rightPants.visible = player.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
         playerEntityModel.leftSleeve.visible = player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
         playerEntityModel.rightSleeve.visible = player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
         playerEntityModel.sneaking = player.isInSneakingPose();
         BipedEntityModel.ArmPose armPose = getArmPose(player, Hand.MAIN_HAND);
         BipedEntityModel.ArmPose armPose2 = getArmPose(player, Hand.OFF_HAND);
         if (armPose.method_30156()) {
            armPose2 = player.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
         }

         if (player.getMainArm() == Arm.RIGHT) {
            playerEntityModel.rightArmPose = armPose;
            playerEntityModel.leftArmPose = armPose2;
         } else {
            playerEntityModel.rightArmPose = armPose2;
            playerEntityModel.leftArmPose = armPose;
         }
      }

   }

   private static BipedEntityModel.ArmPose getArmPose(AbstractClientPlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.isEmpty()) {
         return BipedEntityModel.ArmPose.EMPTY;
      } else {
         if (player.getActiveHand() == hand && player.getItemUseTimeLeft() > 0) {
            UseAction useAction = itemStack.getUseAction();
            if (useAction == UseAction.BLOCK) {
               return BipedEntityModel.ArmPose.BLOCK;
            }

            if (useAction == UseAction.BOW) {
               return BipedEntityModel.ArmPose.BOW_AND_ARROW;
            }

            if (useAction == UseAction.SPEAR) {
               return BipedEntityModel.ArmPose.THROW_SPEAR;
            }

            if (useAction == UseAction.CROSSBOW && hand == player.getActiveHand()) {
               return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
            }
         } else if (!player.handSwinging && itemStack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemStack)) {
            return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
         }

         return BipedEntityModel.ArmPose.ITEM;
      }
   }

   public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
      return abstractClientPlayerEntity.getSkinTexture();
   }

   protected void scale(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f) {
      float g = 0.9375F;
      matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
   }

   protected void renderLabelIfPresent(AbstractClientPlayerEntity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      double d = this.dispatcher.getSquaredDistanceToCamera(abstractClientPlayerEntity);
      matrixStack.push();
      if (d < 100.0D) {
         Scoreboard scoreboard = abstractClientPlayerEntity.getScoreboard();
         ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(2);
         if (scoreboardObjective != null) {
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(abstractClientPlayerEntity.getEntityName(), scoreboardObjective);
            super.renderLabelIfPresent(abstractClientPlayerEntity, (new LiteralText(Integer.toString(scoreboardPlayerScore.getScore()))).append(" ").append(scoreboardObjective.getDisplayName()), matrixStack, vertexConsumerProvider, i);
            this.getFontRenderer().getClass();
            matrixStack.translate(0.0D, (double)(9.0F * 1.15F * 0.025F), 0.0D);
         }
      }

      super.renderLabelIfPresent(abstractClientPlayerEntity, text, matrixStack, vertexConsumerProvider, i);
      matrixStack.pop();
   }

   public void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
      this.renderArm(matrices, vertexConsumers, light, player, ((PlayerEntityModel)this.model).rightArm, ((PlayerEntityModel)this.model).rightSleeve);
   }

   public void renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
      this.renderArm(matrices, vertexConsumers, light, player, ((PlayerEntityModel)this.model).leftArm, ((PlayerEntityModel)this.model).leftSleeve);
   }

   private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve) {
      PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = (PlayerEntityModel)this.getModel();
      this.setModelPose(player);
      playerEntityModel.handSwingProgress = 0.0F;
      playerEntityModel.sneaking = false;
      playerEntityModel.leaningPitch = 0.0F;
      playerEntityModel.setAngles((LivingEntity)player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      arm.pitch = 0.0F;
      arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
      sleeve.pitch = 0.0F;
      sleeve.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
   }

   protected void setupTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h) {
      float i = abstractClientPlayerEntity.getLeaningPitch(h);
      float n;
      float k;
      if (abstractClientPlayerEntity.isFallFlying()) {
         super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
         n = (float)abstractClientPlayerEntity.getRoll() + h;
         k = MathHelper.clamp(n * n / 100.0F, 0.0F, 1.0F);
         if (!abstractClientPlayerEntity.isUsingRiptide()) {
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k * (-90.0F - abstractClientPlayerEntity.pitch)));
         }

         Vec3d vec3d = abstractClientPlayerEntity.getRotationVec(h);
         Vec3d vec3d2 = abstractClientPlayerEntity.getVelocity();
         double d = Entity.squaredHorizontalLength(vec3d2);
         double e = Entity.squaredHorizontalLength(vec3d);
         if (d > 0.0D && e > 0.0D) {
            double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
            double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
            matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float)(Math.signum(m) * Math.acos(l))));
         }
      } else if (i > 0.0F) {
         super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
         n = abstractClientPlayerEntity.isTouchingWater() ? -90.0F - abstractClientPlayerEntity.pitch : -90.0F;
         k = MathHelper.lerp(i, 0.0F, n);
         matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k));
         if (abstractClientPlayerEntity.isInSwimmingPose()) {
            matrixStack.translate(0.0D, -1.0D, 0.30000001192092896D);
         }
      } else {
         super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
      }

   }
}
