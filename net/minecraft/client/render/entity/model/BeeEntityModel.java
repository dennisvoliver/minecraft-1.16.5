package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelUtil;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BeeEntityModel<T extends BeeEntity> extends AnimalModel<T> {
   private final ModelPart bone;
   private final ModelPart torso;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart frontLegs;
   private final ModelPart middleLegs;
   private final ModelPart backLegs;
   private final ModelPart stinger;
   private final ModelPart leftAntenna;
   private final ModelPart rightAntenna;
   private float bodyPitch;

   public BeeEntityModel() {
      super(false, 24.0F, 0.0F);
      this.textureWidth = 64;
      this.textureHeight = 64;
      this.bone = new ModelPart(this);
      this.bone.setPivot(0.0F, 19.0F, 0.0F);
      this.torso = new ModelPart(this, 0, 0);
      this.torso.setPivot(0.0F, 0.0F, 0.0F);
      this.bone.addChild(this.torso);
      this.torso.addCuboid(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F, 0.0F);
      this.stinger = new ModelPart(this, 26, 7);
      this.stinger.addCuboid(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F, 0.0F);
      this.torso.addChild(this.stinger);
      this.leftAntenna = new ModelPart(this, 2, 0);
      this.leftAntenna.setPivot(0.0F, -2.0F, -5.0F);
      this.leftAntenna.addCuboid(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F, 0.0F);
      this.rightAntenna = new ModelPart(this, 2, 3);
      this.rightAntenna.setPivot(0.0F, -2.0F, -5.0F);
      this.rightAntenna.addCuboid(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F, 0.0F);
      this.torso.addChild(this.leftAntenna);
      this.torso.addChild(this.rightAntenna);
      this.rightWing = new ModelPart(this, 0, 18);
      this.rightWing.setPivot(-1.5F, -4.0F, -3.0F);
      this.rightWing.pitch = 0.0F;
      this.rightWing.yaw = -0.2618F;
      this.rightWing.roll = 0.0F;
      this.bone.addChild(this.rightWing);
      this.rightWing.addCuboid(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, 0.001F);
      this.leftWing = new ModelPart(this, 0, 18);
      this.leftWing.setPivot(1.5F, -4.0F, -3.0F);
      this.leftWing.pitch = 0.0F;
      this.leftWing.yaw = 0.2618F;
      this.leftWing.roll = 0.0F;
      this.leftWing.mirror = true;
      this.bone.addChild(this.leftWing);
      this.leftWing.addCuboid(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, 0.001F);
      this.frontLegs = new ModelPart(this);
      this.frontLegs.setPivot(1.5F, 3.0F, -2.0F);
      this.bone.addChild(this.frontLegs);
      this.frontLegs.addCuboid("frontLegBox", -5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F, 26, 1);
      this.middleLegs = new ModelPart(this);
      this.middleLegs.setPivot(1.5F, 3.0F, 0.0F);
      this.bone.addChild(this.middleLegs);
      this.middleLegs.addCuboid("midLegBox", -5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F, 26, 3);
      this.backLegs = new ModelPart(this);
      this.backLegs.setPivot(1.5F, 3.0F, 2.0F);
      this.bone.addChild(this.backLegs);
      this.backLegs.addCuboid("backLegBox", -5.0F, 0.0F, 0.0F, 7, 2, 0, 0.0F, 26, 5);
   }

   public void animateModel(T beeEntity, float f, float g, float h) {
      super.animateModel(beeEntity, f, g, h);
      this.bodyPitch = beeEntity.getBodyPitch(h);
      this.stinger.visible = !beeEntity.hasStung();
   }

   public void setAngles(T beeEntity, float f, float g, float h, float i, float j) {
      this.rightWing.pitch = 0.0F;
      this.leftAntenna.pitch = 0.0F;
      this.rightAntenna.pitch = 0.0F;
      this.bone.pitch = 0.0F;
      this.bone.pivotY = 19.0F;
      boolean bl = beeEntity.isOnGround() && beeEntity.getVelocity().lengthSquared() < 1.0E-7D;
      float l;
      if (bl) {
         this.rightWing.yaw = -0.2618F;
         this.rightWing.roll = 0.0F;
         this.leftWing.pitch = 0.0F;
         this.leftWing.yaw = 0.2618F;
         this.leftWing.roll = 0.0F;
         this.frontLegs.pitch = 0.0F;
         this.middleLegs.pitch = 0.0F;
         this.backLegs.pitch = 0.0F;
      } else {
         l = h * 2.1F;
         this.rightWing.yaw = 0.0F;
         this.rightWing.roll = MathHelper.cos(l) * 3.1415927F * 0.15F;
         this.leftWing.pitch = this.rightWing.pitch;
         this.leftWing.yaw = this.rightWing.yaw;
         this.leftWing.roll = -this.rightWing.roll;
         this.frontLegs.pitch = 0.7853982F;
         this.middleLegs.pitch = 0.7853982F;
         this.backLegs.pitch = 0.7853982F;
         this.bone.pitch = 0.0F;
         this.bone.yaw = 0.0F;
         this.bone.roll = 0.0F;
      }

      if (!beeEntity.hasAngerTime()) {
         this.bone.pitch = 0.0F;
         this.bone.yaw = 0.0F;
         this.bone.roll = 0.0F;
         if (!bl) {
            l = MathHelper.cos(h * 0.18F);
            this.bone.pitch = 0.1F + l * 3.1415927F * 0.025F;
            this.leftAntenna.pitch = l * 3.1415927F * 0.03F;
            this.rightAntenna.pitch = l * 3.1415927F * 0.03F;
            this.frontLegs.pitch = -l * 3.1415927F * 0.1F + 0.3926991F;
            this.backLegs.pitch = -l * 3.1415927F * 0.05F + 0.7853982F;
            this.bone.pivotY = 19.0F - MathHelper.cos(h * 0.18F) * 0.9F;
         }
      }

      if (this.bodyPitch > 0.0F) {
         this.bone.pitch = ModelUtil.interpolateAngle(this.bone.pitch, 3.0915928F, this.bodyPitch);
      }

   }

   protected Iterable<ModelPart> getHeadParts() {
      return ImmutableList.of();
   }

   protected Iterable<ModelPart> getBodyParts() {
      return ImmutableList.of(this.bone);
   }
}
