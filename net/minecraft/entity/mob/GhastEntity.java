package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class GhastEntity extends FlyingEntity implements Monster {
   private static final TrackedData<Boolean> SHOOTING;
   private int fireballStrength = 1;

   public GhastEntity(EntityType<? extends GhastEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 5;
      this.moveControl = new GhastEntity.GhastMoveControl(this);
   }

   protected void initGoals() {
      this.goalSelector.add(5, new GhastEntity.FlyRandomlyGoal(this));
      this.goalSelector.add(7, new GhastEntity.LookAtTargetGoal(this));
      this.goalSelector.add(7, new GhastEntity.ShootFireballGoal(this));
      this.targetSelector.add(1, new FollowTargetGoal(this, PlayerEntity.class, 10, true, false, (livingEntity) -> {
         return Math.abs(livingEntity.getY() - this.getY()) <= 4.0D;
      }));
   }

   @Environment(EnvType.CLIENT)
   public boolean isShooting() {
      return (Boolean)this.dataTracker.get(SHOOTING);
   }

   public void setShooting(boolean shooting) {
      this.dataTracker.set(SHOOTING, shooting);
   }

   public int getFireballStrength() {
      return this.fireballStrength;
   }

   protected boolean isDisallowedInPeaceful() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (source.getSource() instanceof FireballEntity && source.getAttacker() instanceof PlayerEntity) {
         super.damage(source, 1000.0F);
         return true;
      } else {
         return super.damage(source, amount);
      }
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(SHOOTING, false);
   }

   public static DefaultAttributeContainer.Builder createGhastAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100.0D);
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_GHAST_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_GHAST_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_GHAST_DEATH;
   }

   protected float getSoundVolume() {
      return 5.0F;
   }

   public static boolean canSpawn(EntityType<GhastEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(20) == 0 && canMobSpawn(type, world, spawnReason, pos, random);
   }

   public int getLimitPerChunk() {
      return 1;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("ExplosionPower", this.fireballStrength);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("ExplosionPower", 99)) {
         this.fireballStrength = nbt.getInt("ExplosionPower");
      }

   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 2.6F;
   }

   static {
      SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   static class ShootFireballGoal extends Goal {
      private final GhastEntity ghast;
      public int cooldown;

      public ShootFireballGoal(GhastEntity ghast) {
         this.ghast = ghast;
      }

      public boolean canStart() {
         return this.ghast.getTarget() != null;
      }

      public void start() {
         this.cooldown = 0;
      }

      public void stop() {
         this.ghast.setShooting(false);
      }

      public void tick() {
         LivingEntity livingEntity = this.ghast.getTarget();
         double d = 64.0D;
         if (livingEntity.squaredDistanceTo(this.ghast) < 4096.0D && this.ghast.canSee(livingEntity)) {
            World world = this.ghast.world;
            ++this.cooldown;
            if (this.cooldown == 10 && !this.ghast.isSilent()) {
               world.syncWorldEvent((PlayerEntity)null, 1015, this.ghast.getBlockPos(), 0);
            }

            if (this.cooldown == 20) {
               double e = 4.0D;
               Vec3d vec3d = this.ghast.getRotationVec(1.0F);
               double f = livingEntity.getX() - (this.ghast.getX() + vec3d.x * 4.0D);
               double g = livingEntity.getBodyY(0.5D) - (0.5D + this.ghast.getBodyY(0.5D));
               double h = livingEntity.getZ() - (this.ghast.getZ() + vec3d.z * 4.0D);
               if (!this.ghast.isSilent()) {
                  world.syncWorldEvent((PlayerEntity)null, 1016, this.ghast.getBlockPos(), 0);
               }

               FireballEntity fireballEntity = new FireballEntity(world, this.ghast, f, g, h);
               fireballEntity.explosionPower = this.ghast.getFireballStrength();
               fireballEntity.setPosition(this.ghast.getX() + vec3d.x * 4.0D, this.ghast.getBodyY(0.5D) + 0.5D, fireballEntity.getZ() + vec3d.z * 4.0D);
               world.spawnEntity(fireballEntity);
               this.cooldown = -40;
            }
         } else if (this.cooldown > 0) {
            --this.cooldown;
         }

         this.ghast.setShooting(this.cooldown > 10);
      }
   }

   static class LookAtTargetGoal extends Goal {
      private final GhastEntity ghast;

      public LookAtTargetGoal(GhastEntity ghast) {
         this.ghast = ghast;
         this.setControls(EnumSet.of(Goal.Control.LOOK));
      }

      public boolean canStart() {
         return true;
      }

      public void tick() {
         if (this.ghast.getTarget() == null) {
            Vec3d vec3d = this.ghast.getVelocity();
            this.ghast.yaw = -((float)MathHelper.atan2(vec3d.x, vec3d.z)) * 57.295776F;
            this.ghast.bodyYaw = this.ghast.yaw;
         } else {
            LivingEntity livingEntity = this.ghast.getTarget();
            double d = 64.0D;
            if (livingEntity.squaredDistanceTo(this.ghast) < 4096.0D) {
               double e = livingEntity.getX() - this.ghast.getX();
               double f = livingEntity.getZ() - this.ghast.getZ();
               this.ghast.yaw = -((float)MathHelper.atan2(e, f)) * 57.295776F;
               this.ghast.bodyYaw = this.ghast.yaw;
            }
         }

      }
   }

   static class FlyRandomlyGoal extends Goal {
      private final GhastEntity ghast;

      public FlyRandomlyGoal(GhastEntity ghast) {
         this.ghast = ghast;
         this.setControls(EnumSet.of(Goal.Control.MOVE));
      }

      public boolean canStart() {
         MoveControl moveControl = this.ghast.getMoveControl();
         if (!moveControl.isMoving()) {
            return true;
         } else {
            double d = moveControl.getTargetX() - this.ghast.getX();
            double e = moveControl.getTargetY() - this.ghast.getY();
            double f = moveControl.getTargetZ() - this.ghast.getZ();
            double g = d * d + e * e + f * f;
            return g < 1.0D || g > 3600.0D;
         }
      }

      public boolean shouldContinue() {
         return false;
      }

      public void start() {
         Random random = this.ghast.getRandom();
         double d = this.ghast.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double e = this.ghast.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double f = this.ghast.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.ghast.getMoveControl().moveTo(d, e, f, 1.0D);
      }
   }

   static class GhastMoveControl extends MoveControl {
      private final GhastEntity ghast;
      private int collisionCheckCooldown;

      public GhastMoveControl(GhastEntity ghast) {
         super(ghast);
         this.ghast = ghast;
      }

      public void tick() {
         if (this.state == MoveControl.State.MOVE_TO) {
            if (this.collisionCheckCooldown-- <= 0) {
               this.collisionCheckCooldown += this.ghast.getRandom().nextInt(5) + 2;
               Vec3d vec3d = new Vec3d(this.targetX - this.ghast.getX(), this.targetY - this.ghast.getY(), this.targetZ - this.ghast.getZ());
               double d = vec3d.length();
               vec3d = vec3d.normalize();
               if (this.willCollide(vec3d, MathHelper.ceil(d))) {
                  this.ghast.setVelocity(this.ghast.getVelocity().add(vec3d.multiply(0.1D)));
               } else {
                  this.state = MoveControl.State.WAIT;
               }
            }

         }
      }

      private boolean willCollide(Vec3d direction, int steps) {
         Box box = this.ghast.getBoundingBox();

         for(int i = 1; i < steps; ++i) {
            box = box.offset(direction);
            if (!this.ghast.world.isSpaceEmpty(this.ghast, box)) {
               return false;
            }
         }

         return true;
      }
   }
}
