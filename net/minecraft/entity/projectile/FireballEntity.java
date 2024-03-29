package net.minecraft.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class FireballEntity extends AbstractFireballEntity {
   public int explosionPower = 1;

   public FireballEntity(EntityType<? extends FireballEntity> entityType, World world) {
      super(entityType, world);
   }

   @Environment(EnvType.CLIENT)
   public FireballEntity(World world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      super(EntityType.FIREBALL, x, y, z, velocityX, velocityY, velocityZ, world);
   }

   public FireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ) {
      super(EntityType.FIREBALL, owner, velocityX, velocityY, velocityZ, world);
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.world.isClient) {
         boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
         this.world.createExplosion((Entity)null, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, bl, bl ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.NONE);
         this.remove();
      }

   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         Entity entity = entityHitResult.getEntity();
         Entity entity2 = this.getOwner();
         entity.damage(DamageSource.fireball(this, entity2), 6.0F);
         if (entity2 instanceof LivingEntity) {
            this.dealDamage((LivingEntity)entity2, entity);
         }

      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("ExplosionPower", this.explosionPower);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("ExplosionPower", 99)) {
         this.explosionPower = nbt.getInt("ExplosionPower");
      }

   }
}
