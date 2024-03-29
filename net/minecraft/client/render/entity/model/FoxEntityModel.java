package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class FoxEntityModel<T extends FoxEntity> extends AnimalModel<T> {
   public final ModelPart head;
   private final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelPart nose;
   private final ModelPart body;
   private final ModelPart rightBackLeg;
   private final ModelPart leftBackLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;
   private float legPitchModifier;

   public FoxEntityModel() {
      super(true, 8.0F, 3.35F);
      this.textureWidth = 48;
      this.textureHeight = 32;
      this.head = new ModelPart(this, 1, 5);
      this.head.addCuboid(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F);
      this.head.setPivot(-1.0F, 16.5F, -3.0F);
      this.rightEar = new ModelPart(this, 8, 1);
      this.rightEar.addCuboid(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
      this.leftEar = new ModelPart(this, 15, 1);
      this.leftEar.addCuboid(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
      this.nose = new ModelPart(this, 6, 18);
      this.nose.addCuboid(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F);
      this.head.addChild(this.rightEar);
      this.head.addChild(this.leftEar);
      this.head.addChild(this.nose);
      this.body = new ModelPart(this, 24, 15);
      this.body.addCuboid(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F);
      this.body.setPivot(0.0F, 16.0F, -6.0F);
      float f = 0.001F;
      this.rightBackLeg = new ModelPart(this, 13, 24);
      this.rightBackLeg.addCuboid(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.rightBackLeg.setPivot(-5.0F, 17.5F, 7.0F);
      this.leftBackLeg = new ModelPart(this, 4, 24);
      this.leftBackLeg.addCuboid(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.leftBackLeg.setPivot(-1.0F, 17.5F, 7.0F);
      this.rightFrontLeg = new ModelPart(this, 13, 24);
      this.rightFrontLeg.addCuboid(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.rightFrontLeg.setPivot(-5.0F, 17.5F, 0.0F);
      this.leftFrontLeg = new ModelPart(this, 4, 24);
      this.leftFrontLeg.addCuboid(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.leftFrontLeg.setPivot(-1.0F, 17.5F, 0.0F);
      this.tail = new ModelPart(this, 30, 0);
      this.tail.addCuboid(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F);
      this.tail.setPivot(-4.0F, 15.0F, -1.0F);
      this.body.addChild(this.tail);
   }

   public void animateModel(T foxEntity, float f, float g, float h) {
      this.body.pitch = 1.5707964F;
      this.tail.pitch = -0.05235988F;
      this.rightBackLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      this.leftBackLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
      this.rightFrontLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
      this.leftFrontLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      this.head.setPivot(-1.0F, 16.5F, -3.0F);
      this.head.yaw = 0.0F;
      this.head.roll = foxEntity.getHeadRoll(h);
      this.rightBackLeg.visible = true;
      this.leftBackLeg.visible = true;
      this.rightFrontLeg.visible = true;
      this.leftFrontLeg.visible = true;
      this.body.setPivot(0.0F, 16.0F, -6.0F);
      this.body.roll = 0.0F;
      this.rightBackLeg.setPivot(-5.0F, 17.5F, 7.0F);
      this.leftBackLeg.setPivot(-1.0F, 17.5F, 7.0F);
      if (foxEntity.isInSneakingPose()) {
         this.body.pitch = 1.6755161F;
         float i = foxEntity.getBodyRotationHeightOffset(h);
         this.body.setPivot(0.0F, 16.0F + foxEntity.getBodyRotationHeightOffset(h), -6.0F);
         this.head.setPivot(-1.0F, 16.5F + i, -3.0F);
         this.head.yaw = 0.0F;
      } else if (foxEntity.isSleeping()) {
         this.body.roll = -1.5707964F;
         this.body.setPivot(0.0F, 21.0F, -6.0F);
         this.tail.pitch = -2.6179938F;
         if (this.child) {
            this.tail.pitch = -2.1816616F;
            this.body.setPivot(0.0F, 21.0F, -2.0F);
         }

         this.head.setPivot(1.0F, 19.49F, -3.0F);
         this.head.pitch = 0.0F;
         this.head.yaw = -2.0943952F;
         this.head.roll = 0.0F;
         this.rightBackLeg.visible = false;
         this.leftBackLeg.visible = false;
         this.rightFrontLeg.visible = false;
         this.leftFrontLeg.visible = false;
      } else if (foxEntity.isSitting()) {
         this.body.pitch = 0.5235988F;
         this.body.setPivot(0.0F, 9.0F, -3.0F);
         this.tail.pitch = 0.7853982F;
         this.tail.setPivot(-4.0F, 15.0F, -2.0F);
         this.head.setPivot(-1.0F, 10.0F, -0.25F);
         this.head.pitch = 0.0F;
         this.head.yaw = 0.0F;
         if (this.child) {
            this.head.setPivot(-1.0F, 13.0F, -3.75F);
         }

         this.rightBackLeg.pitch = -1.3089969F;
         this.rightBackLeg.setPivot(-5.0F, 21.5F, 6.75F);
         this.leftBackLeg.pitch = -1.3089969F;
         this.leftBackLeg.setPivot(-1.0F, 21.5F, 6.75F);
         this.rightFrontLeg.pitch = -0.2617994F;
         this.leftFrontLeg.pitch = -0.2617994F;
      }

   }

   protected Iterable<ModelPart> getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> getBodyParts() {
      return ImmutableList.of(this.body, this.rightBackLeg, this.leftBackLeg, this.rightFrontLeg, this.leftFrontLeg);
   }

   public void setAngles(T foxEntity, float f, float g, float h, float i, float j) {
      if (!foxEntity.isSleeping() && !foxEntity.isWalking() && !foxEntity.isInSneakingPose()) {
         this.head.pitch = j * 0.017453292F;
         this.head.yaw = i * 0.017453292F;
      }

      if (foxEntity.isSleeping()) {
         this.head.pitch = 0.0F;
         this.head.yaw = -2.0943952F;
         this.head.roll = MathHelper.cos(h * 0.027F) / 22.0F;
      }

      float l;
      if (foxEntity.isInSneakingPose()) {
         l = MathHelper.cos(h) * 0.01F;
         this.body.yaw = l;
         this.rightBackLeg.roll = l;
         this.leftBackLeg.roll = l;
         this.rightFrontLeg.roll = l / 2.0F;
         this.leftFrontLeg.roll = l / 2.0F;
      }

      if (foxEntity.isWalking()) {
         l = 0.1F;
         this.legPitchModifier += 0.67F;
         this.rightBackLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F) * 0.1F;
         this.leftBackLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F + 3.1415927F) * 0.1F;
         this.rightFrontLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F + 3.1415927F) * 0.1F;
         this.leftFrontLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F) * 0.1F;
      }

   }
}
