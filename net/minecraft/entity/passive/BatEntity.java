package net.minecraft.entity.passive;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BatEntity extends AmbientEntity {
   /**
    * The tracked flags of bats. Only has the {@code 1} bit for {@linkplain
    * #isRoosting() roosting}.
    */
   private static final TrackedData<Byte> BAT_FLAGS;
   private static final TargetPredicate CLOSE_PLAYER_PREDICATE;
   private BlockPos hangingPosition;

   public BatEntity(EntityType<? extends BatEntity> entityType, World world) {
      super(entityType, world);
      this.setRoosting(true);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(BAT_FLAGS, (byte)0);
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   protected float getSoundPitch() {
      return super.getSoundPitch() * 0.95F;
   }

   @Nullable
   public SoundEvent getAmbientSound() {
      return this.isRoosting() && this.random.nextInt(4) != 0 ? null : SoundEvents.ENTITY_BAT_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_BAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_BAT_DEATH;
   }

   public boolean isPushable() {
      return false;
   }

   protected void pushAway(Entity entity) {
   }

   protected void tickCramming() {
   }

   public static DefaultAttributeContainer.Builder createBatAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0D);
   }

   /**
    * Returns whether this bat is hanging upside-down under a block.
    */
   public boolean isRoosting() {
      return ((Byte)this.dataTracker.get(BAT_FLAGS) & 1) != 0;
   }

   public void setRoosting(boolean roosting) {
      byte b = (Byte)this.dataTracker.get(BAT_FLAGS);
      if (roosting) {
         this.dataTracker.set(BAT_FLAGS, (byte)(b | 1));
      } else {
         this.dataTracker.set(BAT_FLAGS, (byte)(b & -2));
      }

   }

   public void tick() {
      super.tick();
      if (this.isRoosting()) {
         this.setVelocity(Vec3d.ZERO);
         this.setPos(this.getX(), (double)MathHelper.floor(this.getY()) + 1.0D - (double)this.getHeight(), this.getZ());
      } else {
         this.setVelocity(this.getVelocity().multiply(1.0D, 0.6D, 1.0D));
      }

   }

   protected void mobTick() {
      super.mobTick();
      BlockPos blockPos = this.getBlockPos();
      BlockPos blockPos2 = blockPos.up();
      if (this.isRoosting()) {
         boolean bl = this.isSilent();
         if (this.world.getBlockState(blockPos2).isSolidBlock(this.world, blockPos)) {
            if (this.random.nextInt(200) == 0) {
               this.headYaw = (float)this.random.nextInt(360);
            }

            if (this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this) != null) {
               this.setRoosting(false);
               if (!bl) {
                  this.world.syncWorldEvent((PlayerEntity)null, 1025, blockPos, 0);
               }
            }
         } else {
            this.setRoosting(false);
            if (!bl) {
               this.world.syncWorldEvent((PlayerEntity)null, 1025, blockPos, 0);
            }
         }
      } else {
         if (this.hangingPosition != null && (!this.world.isAir(this.hangingPosition) || this.hangingPosition.getY() < 1)) {
            this.hangingPosition = null;
         }

         if (this.hangingPosition == null || this.random.nextInt(30) == 0 || this.hangingPosition.isWithinDistance(this.getPos(), 2.0D)) {
            this.hangingPosition = new BlockPos(this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7), this.getY() + (double)this.random.nextInt(6) - 2.0D, this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7));
         }

         double d = (double)this.hangingPosition.getX() + 0.5D - this.getX();
         double e = (double)this.hangingPosition.getY() + 0.1D - this.getY();
         double f = (double)this.hangingPosition.getZ() + 0.5D - this.getZ();
         Vec3d vec3d = this.getVelocity();
         Vec3d vec3d2 = vec3d.add((Math.signum(d) * 0.5D - vec3d.x) * 0.10000000149011612D, (Math.signum(e) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D, (Math.signum(f) * 0.5D - vec3d.z) * 0.10000000149011612D);
         this.setVelocity(vec3d2);
         float g = (float)(MathHelper.atan2(vec3d2.z, vec3d2.x) * 57.2957763671875D) - 90.0F;
         float h = MathHelper.wrapDegrees(g - this.yaw);
         this.forwardSpeed = 0.5F;
         this.yaw += h;
         if (this.random.nextInt(100) == 0 && this.world.getBlockState(blockPos2).isSolidBlock(this.world, blockPos2)) {
            this.setRoosting(true);
         }
      }

   }

   protected boolean canClimb() {
      return false;
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      return false;
   }

   protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
   }

   public boolean canAvoidTraps() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         if (!this.world.isClient && this.isRoosting()) {
            this.setRoosting(false);
         }

         return super.damage(source, amount);
      }
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.dataTracker.set(BAT_FLAGS, nbt.getByte("BatFlags"));
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putByte("BatFlags", (Byte)this.dataTracker.get(BAT_FLAGS));
   }

   public static boolean canSpawn(EntityType<BatEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      if (pos.getY() >= world.getSeaLevel()) {
         return false;
      } else {
         int i = world.getLightLevel(pos);
         int j = 4;
         if (isTodayAroundHalloween()) {
            j = 7;
         } else if (random.nextBoolean()) {
            return false;
         }

         return i > random.nextInt(j) ? false : canMobSpawn(type, world, spawnReason, pos, random);
      }
   }

   private static boolean isTodayAroundHalloween() {
      LocalDate localDate = LocalDate.now();
      int i = localDate.get(ChronoField.DAY_OF_MONTH);
      int j = localDate.get(ChronoField.MONTH_OF_YEAR);
      return j == 10 && i >= 20 || j == 11 && i <= 3;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height / 2.0F;
   }

   static {
      BAT_FLAGS = DataTracker.registerData(BatEntity.class, TrackedDataHandlerRegistry.BYTE);
      CLOSE_PLAYER_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(4.0D).includeTeammates();
   }
}
