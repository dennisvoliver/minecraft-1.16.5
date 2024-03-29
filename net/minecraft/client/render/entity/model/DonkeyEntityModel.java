package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;

@Environment(EnvType.CLIENT)
public class DonkeyEntityModel<T extends AbstractDonkeyEntity> extends HorseEntityModel<T> {
   private final ModelPart leftChest = new ModelPart(this, 26, 21);
   private final ModelPart rightChest;

   public DonkeyEntityModel(float f) {
      super(f);
      this.leftChest.addCuboid(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
      this.rightChest = new ModelPart(this, 26, 21);
      this.rightChest.addCuboid(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
      this.leftChest.yaw = -1.5707964F;
      this.rightChest.yaw = 1.5707964F;
      this.leftChest.setPivot(6.0F, -8.0F, 0.0F);
      this.rightChest.setPivot(-6.0F, -8.0F, 0.0F);
      this.body.addChild(this.leftChest);
      this.body.addChild(this.rightChest);
   }

   protected void method_2789(ModelPart modelPart) {
      ModelPart modelPart2 = new ModelPart(this, 0, 12);
      modelPart2.addCuboid(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
      modelPart2.setPivot(1.25F, -10.0F, 4.0F);
      ModelPart modelPart3 = new ModelPart(this, 0, 12);
      modelPart3.addCuboid(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);
      modelPart3.setPivot(-1.25F, -10.0F, 4.0F);
      modelPart2.pitch = 0.2617994F;
      modelPart2.roll = 0.2617994F;
      modelPart3.pitch = 0.2617994F;
      modelPart3.roll = -0.2617994F;
      modelPart.addChild(modelPart2);
      modelPart.addChild(modelPart3);
   }

   public void setAngles(T abstractDonkeyEntity, float f, float g, float h, float i, float j) {
      super.setAngles((HorseBaseEntity)abstractDonkeyEntity, f, g, h, i, j);
      if (abstractDonkeyEntity.hasChest()) {
         this.leftChest.visible = true;
         this.rightChest.visible = true;
      } else {
         this.leftChest.visible = false;
         this.rightChest.visible = false;
      }

   }
}
