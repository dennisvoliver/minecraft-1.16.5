package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class OcelotEntityModel<T extends Entity> extends AnimalModel<T> {
   protected final ModelPart leftBackLeg;
   protected final ModelPart rightBackLeg;
   protected final ModelPart leftFrontLeg;
   protected final ModelPart rightFrontLeg;
   protected final ModelPart upperTail;
   protected final ModelPart lowerTail;
   protected final ModelPart head = new ModelPart(this);
   protected final ModelPart body;
   protected int animationState = 1;

   public OcelotEntityModel(float scale) {
      super(true, 10.0F, 4.0F);
      this.head.addCuboid("main", -2.5F, -2.0F, -3.0F, 5, 4, 5, scale, 0, 0);
      this.head.addCuboid("nose", -1.5F, 0.0F, -4.0F, 3, 2, 2, scale, 0, 24);
      this.head.addCuboid("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, scale, 0, 10);
      this.head.addCuboid("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, scale, 6, 10);
      this.head.setPivot(0.0F, 15.0F, -9.0F);
      this.body = new ModelPart(this, 20, 0);
      this.body.addCuboid(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, scale);
      this.body.setPivot(0.0F, 12.0F, -10.0F);
      this.upperTail = new ModelPart(this, 0, 15);
      this.upperTail.addCuboid(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, scale);
      this.upperTail.pitch = 0.9F;
      this.upperTail.setPivot(0.0F, 15.0F, 8.0F);
      this.lowerTail = new ModelPart(this, 4, 15);
      this.lowerTail.addCuboid(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, scale);
      this.lowerTail.setPivot(0.0F, 20.0F, 14.0F);
      this.leftBackLeg = new ModelPart(this, 8, 13);
      this.leftBackLeg.addCuboid(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, scale);
      this.leftBackLeg.setPivot(1.1F, 18.0F, 5.0F);
      this.rightBackLeg = new ModelPart(this, 8, 13);
      this.rightBackLeg.addCuboid(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, scale);
      this.rightBackLeg.setPivot(-1.1F, 18.0F, 5.0F);
      this.leftFrontLeg = new ModelPart(this, 40, 0);
      this.leftFrontLeg.addCuboid(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, scale);
      this.leftFrontLeg.setPivot(1.2F, 14.1F, -5.0F);
      this.rightFrontLeg = new ModelPart(this, 40, 0);
      this.rightFrontLeg.addCuboid(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, scale);
      this.rightFrontLeg.setPivot(-1.2F, 14.1F, -5.0F);
   }

   protected Iterable<ModelPart> getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> getBodyParts() {
      return ImmutableList.of(this.body, this.leftBackLeg, this.rightBackLeg, this.leftFrontLeg, this.rightFrontLeg, this.upperTail, this.lowerTail);
   }

   public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.pitch = headPitch * 0.017453292F;
      this.head.yaw = headYaw * 0.017453292F;
      if (this.animationState != 3) {
         this.body.pitch = 1.5707964F;
         if (this.animationState == 2) {
            this.leftBackLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * limbDistance;
            this.rightBackLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 0.3F) * limbDistance;
            this.leftFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F + 0.3F) * limbDistance;
            this.rightFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * limbDistance;
            this.lowerTail.pitch = 1.7278761F + 0.31415927F * MathHelper.cos(limbAngle) * limbDistance;
         } else {
            this.leftBackLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * limbDistance;
            this.rightBackLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * limbDistance;
            this.leftFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * limbDistance;
            this.rightFrontLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * limbDistance;
            if (this.animationState == 1) {
               this.lowerTail.pitch = 1.7278761F + 0.7853982F * MathHelper.cos(limbAngle) * limbDistance;
            } else {
               this.lowerTail.pitch = 1.7278761F + 0.47123894F * MathHelper.cos(limbAngle) * limbDistance;
            }
         }
      }

   }

   public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
      this.body.pivotY = 12.0F;
      this.body.pivotZ = -10.0F;
      this.head.pivotY = 15.0F;
      this.head.pivotZ = -9.0F;
      this.upperTail.pivotY = 15.0F;
      this.upperTail.pivotZ = 8.0F;
      this.lowerTail.pivotY = 20.0F;
      this.lowerTail.pivotZ = 14.0F;
      this.leftFrontLeg.pivotY = 14.1F;
      this.leftFrontLeg.pivotZ = -5.0F;
      this.rightFrontLeg.pivotY = 14.1F;
      this.rightFrontLeg.pivotZ = -5.0F;
      this.leftBackLeg.pivotY = 18.0F;
      this.leftBackLeg.pivotZ = 5.0F;
      this.rightBackLeg.pivotY = 18.0F;
      this.rightBackLeg.pivotZ = 5.0F;
      this.upperTail.pitch = 0.9F;
      ModelPart var10000;
      if (entity.isInSneakingPose()) {
         ++this.body.pivotY;
         var10000 = this.head;
         var10000.pivotY += 2.0F;
         ++this.upperTail.pivotY;
         var10000 = this.lowerTail;
         var10000.pivotY += -4.0F;
         var10000 = this.lowerTail;
         var10000.pivotZ += 2.0F;
         this.upperTail.pitch = 1.5707964F;
         this.lowerTail.pitch = 1.5707964F;
         this.animationState = 0;
      } else if (entity.isSprinting()) {
         this.lowerTail.pivotY = this.upperTail.pivotY;
         var10000 = this.lowerTail;
         var10000.pivotZ += 2.0F;
         this.upperTail.pitch = 1.5707964F;
         this.lowerTail.pitch = 1.5707964F;
         this.animationState = 2;
      } else {
         this.animationState = 1;
      }

   }
}
