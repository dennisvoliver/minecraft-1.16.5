package net.minecraft.block.entity;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class EnchantingTableBlockEntity extends BlockEntity implements Nameable, Tickable {
   public int ticks;
   public float nextPageAngle;
   public float pageAngle;
   public float field_11969;
   public float field_11967;
   public float nextPageTurningSpeed;
   public float pageTurningSpeed;
   public float field_11964;
   public float field_11963;
   public float field_11962;
   private static final Random RANDOM = new Random();
   private Text customName;

   public EnchantingTableBlockEntity() {
      super(BlockEntityType.ENCHANTING_TABLE);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.hasCustomName()) {
         nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

      return nbt;
   }

   public void fromTag(BlockState state, NbtCompound tag) {
      super.fromTag(state, tag);
      if (tag.contains("CustomName", 8)) {
         this.customName = Text.Serializer.fromJson(tag.getString("CustomName"));
      }

   }

   public void tick() {
      this.pageTurningSpeed = this.nextPageTurningSpeed;
      this.field_11963 = this.field_11964;
      PlayerEntity playerEntity = this.world.getClosestPlayer((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D, 3.0D, false);
      if (playerEntity != null) {
         double d = playerEntity.getX() - ((double)this.pos.getX() + 0.5D);
         double e = playerEntity.getZ() - ((double)this.pos.getZ() + 0.5D);
         this.field_11962 = (float)MathHelper.atan2(e, d);
         this.nextPageTurningSpeed += 0.1F;
         if (this.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
            float f = this.field_11969;

            do {
               this.field_11969 += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
            } while(f == this.field_11969);
         }
      } else {
         this.field_11962 += 0.02F;
         this.nextPageTurningSpeed -= 0.1F;
      }

      while(this.field_11964 >= 3.1415927F) {
         this.field_11964 -= 6.2831855F;
      }

      while(this.field_11964 < -3.1415927F) {
         this.field_11964 += 6.2831855F;
      }

      while(this.field_11962 >= 3.1415927F) {
         this.field_11962 -= 6.2831855F;
      }

      while(this.field_11962 < -3.1415927F) {
         this.field_11962 += 6.2831855F;
      }

      float g;
      for(g = this.field_11962 - this.field_11964; g >= 3.1415927F; g -= 6.2831855F) {
      }

      while(g < -3.1415927F) {
         g += 6.2831855F;
      }

      this.field_11964 += g * 0.4F;
      this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
      ++this.ticks;
      this.pageAngle = this.nextPageAngle;
      float h = (this.field_11969 - this.nextPageAngle) * 0.4F;
      float i = 0.2F;
      h = MathHelper.clamp(h, -0.2F, 0.2F);
      this.field_11967 += (h - this.field_11967) * 0.9F;
      this.nextPageAngle += this.field_11967;
   }

   public Text getName() {
      return (Text)(this.customName != null ? this.customName : new TranslatableText("container.enchant"));
   }

   public void setCustomName(@Nullable Text value) {
      this.customName = value;
   }

   @Nullable
   public Text getCustomName() {
      return this.customName;
   }
}
