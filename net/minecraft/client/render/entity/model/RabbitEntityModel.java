package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class RabbitEntityModel<T extends RabbitEntity> extends EntityModel<T> {
   private final ModelPart leftFoot = new ModelPart(this, 26, 24);
   private final ModelPart rightFoot;
   private final ModelPart leftBackLeg;
   private final ModelPart rightBackLeg;
   private final ModelPart body;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart head;
   private final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelPart tail;
   private final ModelPart nose;
   private float field_3531;

   public RabbitEntityModel() {
      this.leftFoot.addCuboid(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F);
      this.leftFoot.setPivot(3.0F, 17.5F, 3.7F);
      this.leftFoot.mirror = true;
      this.setAngle(this.leftFoot, 0.0F, 0.0F, 0.0F);
      this.rightFoot = new ModelPart(this, 8, 24);
      this.rightFoot.addCuboid(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F);
      this.rightFoot.setPivot(-3.0F, 17.5F, 3.7F);
      this.rightFoot.mirror = true;
      this.setAngle(this.rightFoot, 0.0F, 0.0F, 0.0F);
      this.leftBackLeg = new ModelPart(this, 30, 15);
      this.leftBackLeg.addCuboid(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F);
      this.leftBackLeg.setPivot(3.0F, 17.5F, 3.7F);
      this.leftBackLeg.mirror = true;
      this.setAngle(this.leftBackLeg, -0.34906584F, 0.0F, 0.0F);
      this.rightBackLeg = new ModelPart(this, 16, 15);
      this.rightBackLeg.addCuboid(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F);
      this.rightBackLeg.setPivot(-3.0F, 17.5F, 3.7F);
      this.rightBackLeg.mirror = true;
      this.setAngle(this.rightBackLeg, -0.34906584F, 0.0F, 0.0F);
      this.body = new ModelPart(this, 0, 0);
      this.body.addCuboid(-3.0F, -2.0F, -10.0F, 6.0F, 5.0F, 10.0F);
      this.body.setPivot(0.0F, 19.0F, 8.0F);
      this.body.mirror = true;
      this.setAngle(this.body, -0.34906584F, 0.0F, 0.0F);
      this.leftFrontLeg = new ModelPart(this, 8, 15);
      this.leftFrontLeg.addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F);
      this.leftFrontLeg.setPivot(3.0F, 17.0F, -1.0F);
      this.leftFrontLeg.mirror = true;
      this.setAngle(this.leftFrontLeg, -0.17453292F, 0.0F, 0.0F);
      this.rightFrontLeg = new ModelPart(this, 0, 15);
      this.rightFrontLeg.addCuboid(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F);
      this.rightFrontLeg.setPivot(-3.0F, 17.0F, -1.0F);
      this.rightFrontLeg.mirror = true;
      this.setAngle(this.rightFrontLeg, -0.17453292F, 0.0F, 0.0F);
      this.head = new ModelPart(this, 32, 0);
      this.head.addCuboid(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F);
      this.head.setPivot(0.0F, 16.0F, -1.0F);
      this.head.mirror = true;
      this.setAngle(this.head, 0.0F, 0.0F, 0.0F);
      this.rightEar = new ModelPart(this, 52, 0);
      this.rightEar.addCuboid(-2.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F);
      this.rightEar.setPivot(0.0F, 16.0F, -1.0F);
      this.rightEar.mirror = true;
      this.setAngle(this.rightEar, 0.0F, -0.2617994F, 0.0F);
      this.leftEar = new ModelPart(this, 58, 0);
      this.leftEar.addCuboid(0.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F);
      this.leftEar.setPivot(0.0F, 16.0F, -1.0F);
      this.leftEar.mirror = true;
      this.setAngle(this.leftEar, 0.0F, 0.2617994F, 0.0F);
      this.tail = new ModelPart(this, 52, 6);
      this.tail.addCuboid(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F);
      this.tail.setPivot(0.0F, 20.0F, 7.0F);
      this.tail.mirror = true;
      this.setAngle(this.tail, -0.3490659F, 0.0F, 0.0F);
      this.nose = new ModelPart(this, 32, 9);
      this.nose.addCuboid(-0.5F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F);
      this.nose.setPivot(0.0F, 16.0F, -1.0F);
      this.nose.mirror = true;
      this.setAngle(this.nose, 0.0F, 0.0F, 0.0F);
   }

   private void setAngle(ModelPart modelPart, float pitch, float yaw, float roll) {
      modelPart.pitch = pitch;
      modelPart.yaw = yaw;
      modelPart.roll = roll;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      if (this.child) {
         float f = 1.5F;
         matrices.push();
         matrices.scale(0.56666666F, 0.56666666F, 0.56666666F);
         matrices.translate(0.0D, 1.375D, 0.125D);
         ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose).forEach((modelPart) -> {
            modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         matrices.pop();
         matrices.push();
         matrices.scale(0.4F, 0.4F, 0.4F);
         matrices.translate(0.0D, 2.25D, 0.0D);
         ImmutableList.of(this.leftFoot, this.rightFoot, this.leftBackLeg, this.rightBackLeg, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail).forEach((modelPart) -> {
            modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         matrices.pop();
      } else {
         matrices.push();
         matrices.scale(0.6F, 0.6F, 0.6F);
         matrices.translate(0.0D, 1.0D, 0.0D);
         ImmutableList.of(this.leftFoot, this.rightFoot, this.leftBackLeg, this.rightBackLeg, this.body, this.leftFrontLeg, this.rightFrontLeg, this.head, this.rightEar, this.leftEar, this.tail, this.nose).forEach((modelPart) -> {
            modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
         });
         matrices.pop();
      }

   }

   public void setAngles(T rabbitEntity, float f, float g, float h, float i, float j) {
      float k = h - (float)rabbitEntity.age;
      this.nose.pitch = j * 0.017453292F;
      this.head.pitch = j * 0.017453292F;
      this.rightEar.pitch = j * 0.017453292F;
      this.leftEar.pitch = j * 0.017453292F;
      this.nose.yaw = i * 0.017453292F;
      this.head.yaw = i * 0.017453292F;
      this.rightEar.yaw = this.nose.yaw - 0.2617994F;
      this.leftEar.yaw = this.nose.yaw + 0.2617994F;
      this.field_3531 = MathHelper.sin(rabbitEntity.getJumpProgress(k) * 3.1415927F);
      this.leftBackLeg.pitch = (this.field_3531 * 50.0F - 21.0F) * 0.017453292F;
      this.rightBackLeg.pitch = (this.field_3531 * 50.0F - 21.0F) * 0.017453292F;
      this.leftFoot.pitch = this.field_3531 * 50.0F * 0.017453292F;
      this.rightFoot.pitch = this.field_3531 * 50.0F * 0.017453292F;
      this.leftFrontLeg.pitch = (this.field_3531 * -40.0F - 11.0F) * 0.017453292F;
      this.rightFrontLeg.pitch = (this.field_3531 * -40.0F - 11.0F) * 0.017453292F;
   }

   public void animateModel(T rabbitEntity, float f, float g, float h) {
      super.animateModel(rabbitEntity, f, g, h);
      this.field_3531 = MathHelper.sin(rabbitEntity.getJumpProgress(h) * 3.1415927F);
   }
}
