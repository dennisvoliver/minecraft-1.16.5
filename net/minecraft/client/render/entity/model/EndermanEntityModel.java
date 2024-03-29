package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class EndermanEntityModel<T extends LivingEntity> extends BipedEntityModel<T> {
   public boolean carryingBlock;
   public boolean angry;

   public EndermanEntityModel(float f) {
      super(0.0F, -14.0F, 64, 32);
      float g = -14.0F;
      this.hat = new ModelPart(this, 0, 16);
      this.hat.addCuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, f - 0.5F);
      this.hat.setPivot(0.0F, -14.0F, 0.0F);
      this.body = new ModelPart(this, 32, 16);
      this.body.addCuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f);
      this.body.setPivot(0.0F, -14.0F, 0.0F);
      this.rightArm = new ModelPart(this, 56, 0);
      this.rightArm.addCuboid(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, f);
      this.rightArm.setPivot(-3.0F, -12.0F, 0.0F);
      this.leftArm = new ModelPart(this, 56, 0);
      this.leftArm.mirror = true;
      this.leftArm.addCuboid(-1.0F, -2.0F, -1.0F, 2.0F, 30.0F, 2.0F, f);
      this.leftArm.setPivot(5.0F, -12.0F, 0.0F);
      this.rightLeg = new ModelPart(this, 56, 0);
      this.rightLeg.addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, f);
      this.rightLeg.setPivot(-2.0F, -2.0F, 0.0F);
      this.leftLeg = new ModelPart(this, 56, 0);
      this.leftLeg.mirror = true;
      this.leftLeg.addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 30.0F, 2.0F, f);
      this.leftLeg.setPivot(2.0F, -2.0F, 0.0F);
   }

   public void setAngles(T livingEntity, float f, float g, float h, float i, float j) {
      super.setAngles(livingEntity, f, g, h, i, j);
      this.head.visible = true;
      float k = -14.0F;
      this.body.pitch = 0.0F;
      this.body.pivotY = -14.0F;
      this.body.pivotZ = -0.0F;
      ModelPart var10000 = this.rightLeg;
      var10000.pitch -= 0.0F;
      var10000 = this.leftLeg;
      var10000.pitch -= 0.0F;
      var10000 = this.rightArm;
      var10000.pitch = (float)((double)var10000.pitch * 0.5D);
      var10000 = this.leftArm;
      var10000.pitch = (float)((double)var10000.pitch * 0.5D);
      var10000 = this.rightLeg;
      var10000.pitch = (float)((double)var10000.pitch * 0.5D);
      var10000 = this.leftLeg;
      var10000.pitch = (float)((double)var10000.pitch * 0.5D);
      float l = 0.4F;
      if (this.rightArm.pitch > 0.4F) {
         this.rightArm.pitch = 0.4F;
      }

      if (this.leftArm.pitch > 0.4F) {
         this.leftArm.pitch = 0.4F;
      }

      if (this.rightArm.pitch < -0.4F) {
         this.rightArm.pitch = -0.4F;
      }

      if (this.leftArm.pitch < -0.4F) {
         this.leftArm.pitch = -0.4F;
      }

      if (this.rightLeg.pitch > 0.4F) {
         this.rightLeg.pitch = 0.4F;
      }

      if (this.leftLeg.pitch > 0.4F) {
         this.leftLeg.pitch = 0.4F;
      }

      if (this.rightLeg.pitch < -0.4F) {
         this.rightLeg.pitch = -0.4F;
      }

      if (this.leftLeg.pitch < -0.4F) {
         this.leftLeg.pitch = -0.4F;
      }

      if (this.carryingBlock) {
         this.rightArm.pitch = -0.5F;
         this.leftArm.pitch = -0.5F;
         this.rightArm.roll = 0.05F;
         this.leftArm.roll = -0.05F;
      }

      this.rightArm.pivotZ = 0.0F;
      this.leftArm.pivotZ = 0.0F;
      this.rightLeg.pivotZ = 0.0F;
      this.leftLeg.pivotZ = 0.0F;
      this.rightLeg.pivotY = -5.0F;
      this.leftLeg.pivotY = -5.0F;
      this.head.pivotZ = -0.0F;
      this.head.pivotY = -13.0F;
      this.hat.pivotX = this.head.pivotX;
      this.hat.pivotY = this.head.pivotY;
      this.hat.pivotZ = this.head.pivotZ;
      this.hat.pitch = this.head.pitch;
      this.hat.yaw = this.head.yaw;
      this.hat.roll = this.head.roll;
      float n;
      if (this.angry) {
         n = 1.0F;
         var10000 = this.head;
         var10000.pivotY -= 5.0F;
      }

      n = -14.0F;
      this.rightArm.setPivot(-5.0F, -12.0F, 0.0F);
      this.leftArm.setPivot(5.0F, -12.0F, 0.0F);
   }
}
