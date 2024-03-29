package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShulkerEntity extends GolemEntity implements Monster {
   private static final UUID COVERED_ARMOR_BONUS_ID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
   private static final EntityAttributeModifier COVERED_ARMOR_BONUS;
   protected static final TrackedData<Direction> ATTACHED_FACE;
   protected static final TrackedData<Optional<BlockPos>> ATTACHED_BLOCK;
   protected static final TrackedData<Byte> PEEK_AMOUNT;
   protected static final TrackedData<Byte> COLOR;
   private float prevOpenProgress;
   private float openProgress;
   private BlockPos prevAttachedBlock = null;
   private int teleportLerpTimer;

   public ShulkerEntity(EntityType<? extends ShulkerEntity> entityType, World world) {
      super(entityType, world);
      this.experiencePoints = 5;
   }

   protected void initGoals() {
      this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(4, new ShulkerEntity.ShootBulletGoal());
      this.goalSelector.add(7, new ShulkerEntity.PeekGoal());
      this.goalSelector.add(8, new LookAroundGoal(this));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
      this.targetSelector.add(2, new ShulkerEntity.SearchForPlayerGoal(this));
      this.targetSelector.add(3, new ShulkerEntity.SearchForTargetGoal(this));
   }

   protected boolean canClimb() {
      return false;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SHULKER_AMBIENT;
   }

   public void playAmbientSound() {
      if (!this.isClosed()) {
         super.playAmbientSound();
      }

   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SHULKER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isClosed() ? SoundEvents.ENTITY_SHULKER_HURT_CLOSED : SoundEvents.ENTITY_SHULKER_HURT;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(ATTACHED_FACE, Direction.DOWN);
      this.dataTracker.startTracking(ATTACHED_BLOCK, Optional.empty());
      this.dataTracker.startTracking(PEEK_AMOUNT, (byte)0);
      this.dataTracker.startTracking(COLOR, (byte)16);
   }

   public static DefaultAttributeContainer.Builder createShulkerAttributes() {
      return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0D);
   }

   protected BodyControl createBodyControl() {
      return new ShulkerEntity.ShulkerBodyControl(this);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.dataTracker.set(ATTACHED_FACE, Direction.byId(nbt.getByte("AttachFace")));
      this.dataTracker.set(PEEK_AMOUNT, nbt.getByte("Peek"));
      this.dataTracker.set(COLOR, nbt.getByte("Color"));
      if (nbt.contains("APX")) {
         int i = nbt.getInt("APX");
         int j = nbt.getInt("APY");
         int k = nbt.getInt("APZ");
         this.dataTracker.set(ATTACHED_BLOCK, Optional.of(new BlockPos(i, j, k)));
      } else {
         this.dataTracker.set(ATTACHED_BLOCK, Optional.empty());
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putByte("AttachFace", (byte)((Direction)this.dataTracker.get(ATTACHED_FACE)).getId());
      nbt.putByte("Peek", (Byte)this.dataTracker.get(PEEK_AMOUNT));
      nbt.putByte("Color", (Byte)this.dataTracker.get(COLOR));
      BlockPos blockPos = this.getAttachedBlock();
      if (blockPos != null) {
         nbt.putInt("APX", blockPos.getX());
         nbt.putInt("APY", blockPos.getY());
         nbt.putInt("APZ", blockPos.getZ());
      }

   }

   public void tick() {
      super.tick();
      BlockPos blockPos = (BlockPos)((Optional)this.dataTracker.get(ATTACHED_BLOCK)).orElse((Object)null);
      if (blockPos == null && !this.world.isClient) {
         blockPos = this.getBlockPos();
         this.dataTracker.set(ATTACHED_BLOCK, Optional.of(blockPos));
      }

      float g;
      if (this.hasVehicle()) {
         blockPos = null;
         g = this.getVehicle().yaw;
         this.yaw = g;
         this.bodyYaw = g;
         this.prevBodyYaw = g;
         this.teleportLerpTimer = 0;
      } else if (!this.world.isClient) {
         BlockState blockState = this.world.getBlockState(blockPos);
         Direction direction2;
         if (!blockState.isAir()) {
            if (blockState.isOf(Blocks.MOVING_PISTON)) {
               direction2 = (Direction)blockState.get(PistonBlock.FACING);
               if (this.world.isAir(blockPos.offset(direction2))) {
                  blockPos = blockPos.offset(direction2);
                  this.dataTracker.set(ATTACHED_BLOCK, Optional.of(blockPos));
               } else {
                  this.tryTeleport();
               }
            } else if (blockState.isOf(Blocks.PISTON_HEAD)) {
               direction2 = (Direction)blockState.get(PistonHeadBlock.FACING);
               if (this.world.isAir(blockPos.offset(direction2))) {
                  blockPos = blockPos.offset(direction2);
                  this.dataTracker.set(ATTACHED_BLOCK, Optional.of(blockPos));
               } else {
                  this.tryTeleport();
               }
            } else {
               this.tryTeleport();
            }
         }

         direction2 = this.getAttachedFace();
         if (!this.canStay(blockPos, direction2)) {
            Direction direction4 = this.findAttachSide(blockPos);
            if (direction4 != null) {
               this.dataTracker.set(ATTACHED_FACE, direction4);
            } else {
               this.tryTeleport();
            }
         }
      }

      g = (float)this.getPeekAmount() * 0.01F;
      this.prevOpenProgress = this.openProgress;
      if (this.openProgress > g) {
         this.openProgress = MathHelper.clamp(this.openProgress - 0.05F, g, 1.0F);
      } else if (this.openProgress < g) {
         this.openProgress = MathHelper.clamp(this.openProgress + 0.05F, 0.0F, g);
      }

      if (blockPos != null) {
         if (this.world.isClient) {
            if (this.teleportLerpTimer > 0 && this.prevAttachedBlock != null) {
               --this.teleportLerpTimer;
            } else {
               this.prevAttachedBlock = blockPos;
            }
         }

         this.resetPosition((double)blockPos.getX() + 0.5D, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5D);
         double d = 0.5D - (double)MathHelper.sin((0.5F + this.openProgress) * 3.1415927F) * 0.5D;
         double e = 0.5D - (double)MathHelper.sin((0.5F + this.prevOpenProgress) * 3.1415927F) * 0.5D;
         Direction direction5 = this.getAttachedFace().getOpposite();
         this.setBoundingBox((new Box(this.getX() - 0.5D, this.getY(), this.getZ() - 0.5D, this.getX() + 0.5D, this.getY() + 1.0D, this.getZ() + 0.5D)).stretch((double)direction5.getOffsetX() * d, (double)direction5.getOffsetY() * d, (double)direction5.getOffsetZ() * d));
         double h = d - e;
         if (h > 0.0D) {
            List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox());
            if (!list.isEmpty()) {
               Iterator var11 = list.iterator();

               while(var11.hasNext()) {
                  Entity entity = (Entity)var11.next();
                  if (!(entity instanceof ShulkerEntity) && !entity.noClip) {
                     entity.move(MovementType.SHULKER, new Vec3d(h * (double)direction5.getOffsetX(), h * (double)direction5.getOffsetY(), h * (double)direction5.getOffsetZ()));
                  }
               }
            }
         }
      }

   }

   public void move(MovementType movementType, Vec3d movement) {
      if (movementType == MovementType.SHULKER_BOX) {
         this.tryTeleport();
      } else {
         super.move(movementType, movement);
      }

   }

   public void setPosition(double x, double y, double z) {
      super.setPosition(x, y, z);
      if (this.dataTracker != null && this.age != 0) {
         Optional<BlockPos> optional = (Optional)this.dataTracker.get(ATTACHED_BLOCK);
         Optional<BlockPos> optional2 = Optional.of(new BlockPos(x, y, z));
         if (!optional2.equals(optional)) {
            this.dataTracker.set(ATTACHED_BLOCK, optional2);
            this.dataTracker.set(PEEK_AMOUNT, (byte)0);
            this.velocityDirty = true;
         }

      }
   }

   @Nullable
   protected Direction findAttachSide(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction direction = var2[var4];
         if (this.canStay(pos, direction)) {
            return direction;
         }
      }

      return null;
   }

   private boolean canStay(BlockPos pos, Direction direction) {
      return this.world.isDirectionSolid(pos.offset(direction), this, direction.getOpposite()) && this.world.isSpaceEmpty(this, ShulkerLidCollisions.getLidCollisionBox(pos, direction.getOpposite()));
   }

   protected boolean tryTeleport() {
      if (!this.isAiDisabled() && this.isAlive()) {
         BlockPos blockPos = this.getBlockPos();

         for(int i = 0; i < 5; ++i) {
            BlockPos blockPos2 = blockPos.add(8 - this.random.nextInt(17), 8 - this.random.nextInt(17), 8 - this.random.nextInt(17));
            if (blockPos2.getY() > 0 && this.world.isAir(blockPos2) && this.world.getWorldBorder().contains(blockPos2) && this.world.isSpaceEmpty(this, new Box(blockPos2))) {
               Direction direction = this.findAttachSide(blockPos2);
               if (direction != null) {
                  this.dataTracker.set(ATTACHED_FACE, direction);
                  this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0F, 1.0F);
                  this.dataTracker.set(ATTACHED_BLOCK, Optional.of(blockPos2));
                  this.dataTracker.set(PEEK_AMOUNT, (byte)0);
                  this.setTarget((LivingEntity)null);
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public void tickMovement() {
      super.tickMovement();
      this.setVelocity(Vec3d.ZERO);
      if (!this.isAiDisabled()) {
         this.prevBodyYaw = 0.0F;
         this.bodyYaw = 0.0F;
      }

   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (ATTACHED_BLOCK.equals(data) && this.world.isClient && !this.hasVehicle()) {
         BlockPos blockPos = this.getAttachedBlock();
         if (blockPos != null) {
            if (this.prevAttachedBlock == null) {
               this.prevAttachedBlock = blockPos;
            } else {
               this.teleportLerpTimer = 6;
            }

            this.resetPosition((double)blockPos.getX() + 0.5D, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5D);
         }
      }

      super.onTrackedDataSet(data);
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.bodyTrackingIncrements = 0;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isClosed()) {
         Entity entity = source.getSource();
         if (entity instanceof PersistentProjectileEntity) {
            return false;
         }
      }

      if (super.damage(source, amount)) {
         if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5D && this.random.nextInt(4) == 0) {
            this.tryTeleport();
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isClosed() {
      return this.getPeekAmount() == 0;
   }

   public boolean isCollidable() {
      return this.isAlive();
   }

   public Direction getAttachedFace() {
      return (Direction)this.dataTracker.get(ATTACHED_FACE);
   }

   @Nullable
   public BlockPos getAttachedBlock() {
      return (BlockPos)((Optional)this.dataTracker.get(ATTACHED_BLOCK)).orElse((Object)null);
   }

   public void setAttachedBlock(@Nullable BlockPos pos) {
      this.dataTracker.set(ATTACHED_BLOCK, Optional.ofNullable(pos));
   }

   public int getPeekAmount() {
      return (Byte)this.dataTracker.get(PEEK_AMOUNT);
   }

   public void setPeekAmount(int peekAmount) {
      if (!this.world.isClient) {
         this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).removeModifier(COVERED_ARMOR_BONUS);
         if (peekAmount == 0) {
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).addPersistentModifier(COVERED_ARMOR_BONUS);
            this.playSound(SoundEvents.ENTITY_SHULKER_CLOSE, 1.0F, 1.0F);
         } else {
            this.playSound(SoundEvents.ENTITY_SHULKER_OPEN, 1.0F, 1.0F);
         }
      }

      this.dataTracker.set(PEEK_AMOUNT, (byte)peekAmount);
   }

   @Environment(EnvType.CLIENT)
   public float getOpenProgress(float delta) {
      return MathHelper.lerp(delta, this.prevOpenProgress, this.openProgress);
   }

   @Environment(EnvType.CLIENT)
   public int getTeleportLerpTimer() {
      return this.teleportLerpTimer;
   }

   @Environment(EnvType.CLIENT)
   public BlockPos getPrevAttachedBlock() {
      return this.prevAttachedBlock;
   }

   protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.5F;
   }

   public int getLookPitchSpeed() {
      return 180;
   }

   public int getBodyYawSpeed() {
      return 180;
   }

   public void pushAwayFrom(Entity entity) {
   }

   public float getTargetingMargin() {
      return 0.0F;
   }

   @Environment(EnvType.CLIENT)
   public boolean hasAttachedBlock() {
      return this.prevAttachedBlock != null && this.getAttachedBlock() != null;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public DyeColor getColor() {
      Byte byte_ = (Byte)this.dataTracker.get(COLOR);
      return byte_ != 16 && byte_ <= 15 ? DyeColor.byId(byte_) : null;
   }

   static {
      COVERED_ARMOR_BONUS = new EntityAttributeModifier(COVERED_ARMOR_BONUS_ID, "Covered armor bonus", 20.0D, EntityAttributeModifier.Operation.ADDITION);
      ATTACHED_FACE = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.FACING);
      ATTACHED_BLOCK = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
      PEEK_AMOUNT = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
      COLOR = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
   }

   static class SearchForTargetGoal extends FollowTargetGoal<LivingEntity> {
      public SearchForTargetGoal(ShulkerEntity shulker) {
         super(shulker, LivingEntity.class, 10, true, false, (entity) -> {
            return entity instanceof Monster;
         });
      }

      public boolean canStart() {
         return this.mob.getScoreboardTeam() == null ? false : super.canStart();
      }

      protected Box getSearchBox(double distance) {
         Direction direction = ((ShulkerEntity)this.mob).getAttachedFace();
         if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().expand(4.0D, distance, distance);
         } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().expand(distance, distance, 4.0D) : this.mob.getBoundingBox().expand(distance, 4.0D, distance);
         }
      }
   }

   class SearchForPlayerGoal extends FollowTargetGoal<PlayerEntity> {
      public SearchForPlayerGoal(ShulkerEntity shulker) {
         super(shulker, PlayerEntity.class, true);
      }

      public boolean canStart() {
         return ShulkerEntity.this.world.getDifficulty() == Difficulty.PEACEFUL ? false : super.canStart();
      }

      protected Box getSearchBox(double distance) {
         Direction direction = ((ShulkerEntity)this.mob).getAttachedFace();
         if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().expand(4.0D, distance, distance);
         } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().expand(distance, distance, 4.0D) : this.mob.getBoundingBox().expand(distance, 4.0D, distance);
         }
      }
   }

   class ShootBulletGoal extends Goal {
      private int counter;

      public ShootBulletGoal() {
         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
      }

      public boolean canStart() {
         LivingEntity livingEntity = ShulkerEntity.this.getTarget();
         if (livingEntity != null && livingEntity.isAlive()) {
            return ShulkerEntity.this.world.getDifficulty() != Difficulty.PEACEFUL;
         } else {
            return false;
         }
      }

      public void start() {
         this.counter = 20;
         ShulkerEntity.this.setPeekAmount(100);
      }

      public void stop() {
         ShulkerEntity.this.setPeekAmount(0);
      }

      public void tick() {
         if (ShulkerEntity.this.world.getDifficulty() != Difficulty.PEACEFUL) {
            --this.counter;
            LivingEntity livingEntity = ShulkerEntity.this.getTarget();
            ShulkerEntity.this.getLookControl().lookAt(livingEntity, 180.0F, 180.0F);
            double d = ShulkerEntity.this.squaredDistanceTo(livingEntity);
            if (d < 400.0D) {
               if (this.counter <= 0) {
                  this.counter = 20 + ShulkerEntity.this.random.nextInt(10) * 20 / 2;
                  ShulkerEntity.this.world.spawnEntity(new ShulkerBulletEntity(ShulkerEntity.this.world, ShulkerEntity.this, livingEntity, ShulkerEntity.this.getAttachedFace().getAxis()));
                  ShulkerEntity.this.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (ShulkerEntity.this.random.nextFloat() - ShulkerEntity.this.random.nextFloat()) * 0.2F + 1.0F);
               }
            } else {
               ShulkerEntity.this.setTarget((LivingEntity)null);
            }

            super.tick();
         }
      }
   }

   class PeekGoal extends Goal {
      private int counter;

      private PeekGoal() {
      }

      public boolean canStart() {
         return ShulkerEntity.this.getTarget() == null && ShulkerEntity.this.random.nextInt(40) == 0;
      }

      public boolean shouldContinue() {
         return ShulkerEntity.this.getTarget() == null && this.counter > 0;
      }

      public void start() {
         this.counter = 20 * (1 + ShulkerEntity.this.random.nextInt(3));
         ShulkerEntity.this.setPeekAmount(30);
      }

      public void stop() {
         if (ShulkerEntity.this.getTarget() == null) {
            ShulkerEntity.this.setPeekAmount(0);
         }

      }

      public void tick() {
         --this.counter;
      }
   }

   class ShulkerBodyControl extends BodyControl {
      public ShulkerBodyControl(MobEntity entity) {
         super(entity);
      }

      public void tick() {
      }
   }
}
