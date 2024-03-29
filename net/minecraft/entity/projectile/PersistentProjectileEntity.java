package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class PersistentProjectileEntity extends ProjectileEntity {
   private static final TrackedData<Byte> PROJECTILE_FLAGS;
   private static final TrackedData<Byte> PIERCE_LEVEL;
   @Nullable
   private BlockState inBlockState;
   protected boolean inGround;
   protected int inGroundTime;
   public PersistentProjectileEntity.PickupPermission pickupType;
   public int shake;
   private int life;
   private double damage;
   private int punch;
   private SoundEvent sound;
   private IntOpenHashSet piercedEntities;
   private List<Entity> piercingKilledEntities;

   protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
      super(entityType, world);
      this.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
      this.damage = 2.0D;
      this.sound = this.getHitSound();
   }

   protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world) {
      this(type, world);
      this.setPosition(x, y, z);
   }

   protected PersistentProjectileEntity(EntityType<? extends PersistentProjectileEntity> type, LivingEntity owner, World world) {
      this(type, owner.getX(), owner.getEyeY() - 0.10000000149011612D, owner.getZ(), world);
      this.setOwner(owner);
      if (owner instanceof PlayerEntity) {
         this.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
      }

   }

   public void setSound(SoundEvent sound) {
      this.sound = sound;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength() * 10.0D;
      if (Double.isNaN(d)) {
         d = 1.0D;
      }

      d *= 64.0D * getRenderDistanceMultiplier();
      return distance < d * d;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(PROJECTILE_FLAGS, (byte)0);
      this.dataTracker.startTracking(PIERCE_LEVEL, (byte)0);
   }

   public void setVelocity(double x, double y, double z, float speed, float divergence) {
      super.setVelocity(x, y, z, speed, divergence);
      this.life = 0;
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.setPosition(x, y, z);
      this.setRotation(yaw, pitch);
   }

   @Environment(EnvType.CLIENT)
   public void setVelocityClient(double x, double y, double z) {
      super.setVelocityClient(x, y, z);
      this.life = 0;
   }

   public void tick() {
      super.tick();
      boolean bl = this.isNoClip();
      Vec3d vec3d = this.getVelocity();
      if (this.prevPitch == 0.0F && this.prevYaw == 0.0F) {
         float f = MathHelper.sqrt(squaredHorizontalLength(vec3d));
         this.yaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875D);
         this.pitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * 57.2957763671875D);
         this.prevYaw = this.yaw;
         this.prevPitch = this.pitch;
      }

      BlockPos blockPos = this.getBlockPos();
      BlockState blockState = this.world.getBlockState(blockPos);
      Vec3d vec3d4;
      if (!blockState.isAir() && !bl) {
         VoxelShape voxelShape = blockState.getCollisionShape(this.world, blockPos);
         if (!voxelShape.isEmpty()) {
            vec3d4 = this.getPos();
            Iterator var7 = voxelShape.getBoundingBoxes().iterator();

            while(var7.hasNext()) {
               Box box = (Box)var7.next();
               if (box.offset(blockPos).contains(vec3d4)) {
                  this.inGround = true;
                  break;
               }
            }
         }
      }

      if (this.shake > 0) {
         --this.shake;
      }

      if (this.isTouchingWaterOrRain()) {
         this.extinguish();
      }

      if (this.inGround && !bl) {
         if (this.inBlockState != blockState && this.method_26351()) {
            this.method_26352();
         } else if (!this.world.isClient) {
            this.age();
         }

         ++this.inGroundTime;
      } else {
         this.inGroundTime = 0;
         Vec3d vec3d3 = this.getPos();
         vec3d4 = vec3d3.add(vec3d);
         HitResult hitResult = this.world.raycast(new RaycastContext(vec3d3, vec3d4, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
         if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
            vec3d4 = ((HitResult)hitResult).getPos();
         }

         while(!this.removed) {
            EntityHitResult entityHitResult = this.getEntityCollision(vec3d3, vec3d4);
            if (entityHitResult != null) {
               hitResult = entityHitResult;
            }

            if (hitResult != null && ((HitResult)hitResult).getType() == HitResult.Type.ENTITY) {
               Entity entity = ((EntityHitResult)hitResult).getEntity();
               Entity entity2 = this.getOwner();
               if (entity instanceof PlayerEntity && entity2 instanceof PlayerEntity && !((PlayerEntity)entity2).shouldDamagePlayer((PlayerEntity)entity)) {
                  hitResult = null;
                  entityHitResult = null;
               }
            }

            if (hitResult != null && !bl) {
               this.onCollision((HitResult)hitResult);
               this.velocityDirty = true;
            }

            if (entityHitResult == null || this.getPierceLevel() <= 0) {
               break;
            }

            hitResult = null;
         }

         vec3d = this.getVelocity();
         double d = vec3d.x;
         double e = vec3d.y;
         double g = vec3d.z;
         if (this.isCritical()) {
            for(int i = 0; i < 4; ++i) {
               this.world.addParticle(ParticleTypes.CRIT, this.getX() + d * (double)i / 4.0D, this.getY() + e * (double)i / 4.0D, this.getZ() + g * (double)i / 4.0D, -d, -e + 0.2D, -g);
            }
         }

         double h = this.getX() + d;
         double j = this.getY() + e;
         double k = this.getZ() + g;
         float l = MathHelper.sqrt(squaredHorizontalLength(vec3d));
         if (bl) {
            this.yaw = (float)(MathHelper.atan2(-d, -g) * 57.2957763671875D);
         } else {
            this.yaw = (float)(MathHelper.atan2(d, g) * 57.2957763671875D);
         }

         this.pitch = (float)(MathHelper.atan2(e, (double)l) * 57.2957763671875D);
         this.pitch = updateRotation(this.prevPitch, this.pitch);
         this.yaw = updateRotation(this.prevYaw, this.yaw);
         float m = 0.99F;
         float n = 0.05F;
         if (this.isTouchingWater()) {
            for(int o = 0; o < 4; ++o) {
               float p = 0.25F;
               this.world.addParticle(ParticleTypes.BUBBLE, h - d * 0.25D, j - e * 0.25D, k - g * 0.25D, d, e, g);
            }

            m = this.getDragInWater();
         }

         this.setVelocity(vec3d.multiply((double)m));
         if (!this.hasNoGravity() && !bl) {
            Vec3d vec3d5 = this.getVelocity();
            this.setVelocity(vec3d5.x, vec3d5.y - 0.05000000074505806D, vec3d5.z);
         }

         this.setPosition(h, j, k);
         this.checkBlockCollision();
      }
   }

   private boolean method_26351() {
      return this.inGround && this.world.isSpaceEmpty((new Box(this.getPos(), this.getPos())).expand(0.06D));
   }

   private void method_26352() {
      this.inGround = false;
      Vec3d vec3d = this.getVelocity();
      this.setVelocity(vec3d.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
      this.life = 0;
   }

   public void move(MovementType movementType, Vec3d movement) {
      super.move(movementType, movement);
      if (movementType != MovementType.SELF && this.method_26351()) {
         this.method_26352();
      }

   }

   protected void age() {
      ++this.life;
      if (this.life >= 1200) {
         this.remove();
      }

   }

   private void clearPiercingStatus() {
      if (this.piercingKilledEntities != null) {
         this.piercingKilledEntities.clear();
      }

      if (this.piercedEntities != null) {
         this.piercedEntities.clear();
      }

   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      Entity entity = entityHitResult.getEntity();
      float f = (float)this.getVelocity().length();
      int i = MathHelper.ceil(MathHelper.clamp((double)f * this.damage, 0.0D, 2.147483647E9D));
      if (this.getPierceLevel() > 0) {
         if (this.piercedEntities == null) {
            this.piercedEntities = new IntOpenHashSet(5);
         }

         if (this.piercingKilledEntities == null) {
            this.piercingKilledEntities = Lists.newArrayListWithCapacity(5);
         }

         if (this.piercedEntities.size() >= this.getPierceLevel() + 1) {
            this.remove();
            return;
         }

         this.piercedEntities.add(entity.getEntityId());
      }

      if (this.isCritical()) {
         long l = (long)this.random.nextInt(i / 2 + 2);
         i = (int)Math.min(l + (long)i, 2147483647L);
      }

      Entity entity2 = this.getOwner();
      DamageSource damageSource2;
      if (entity2 == null) {
         damageSource2 = DamageSource.arrow(this, this);
      } else {
         damageSource2 = DamageSource.arrow(this, entity2);
         if (entity2 instanceof LivingEntity) {
            ((LivingEntity)entity2).onAttacking(entity);
         }
      }

      boolean bl = entity.getType() == EntityType.ENDERMAN;
      int j = entity.getFireTicks();
      if (this.isOnFire() && !bl) {
         entity.setOnFireFor(5);
      }

      if (entity.damage(damageSource2, (float)i)) {
         if (bl) {
            return;
         }

         if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (!this.world.isClient && this.getPierceLevel() <= 0) {
               livingEntity.setStuckArrowCount(livingEntity.getStuckArrowCount() + 1);
            }

            if (this.punch > 0) {
               Vec3d vec3d = this.getVelocity().multiply(1.0D, 0.0D, 1.0D).normalize().multiply((double)this.punch * 0.6D);
               if (vec3d.lengthSquared() > 0.0D) {
                  livingEntity.addVelocity(vec3d.x, 0.1D, vec3d.z);
               }
            }

            if (!this.world.isClient && entity2 instanceof LivingEntity) {
               EnchantmentHelper.onUserDamaged(livingEntity, entity2);
               EnchantmentHelper.onTargetDamaged((LivingEntity)entity2, livingEntity);
            }

            this.onHit(livingEntity);
            if (entity2 != null && livingEntity != entity2 && livingEntity instanceof PlayerEntity && entity2 instanceof ServerPlayerEntity && !this.isSilent()) {
               ((ServerPlayerEntity)entity2).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, 0.0F));
            }

            if (!entity.isAlive() && this.piercingKilledEntities != null) {
               this.piercingKilledEntities.add(livingEntity);
            }

            if (!this.world.isClient && entity2 instanceof ServerPlayerEntity) {
               ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity2;
               if (this.piercingKilledEntities != null && this.isShotFromCrossbow()) {
                  Criteria.KILLED_BY_CROSSBOW.trigger(serverPlayerEntity, this.piercingKilledEntities);
               } else if (!entity.isAlive() && this.isShotFromCrossbow()) {
                  Criteria.KILLED_BY_CROSSBOW.trigger(serverPlayerEntity, Arrays.asList(entity));
               }
            }
         }

         this.playSound(this.sound, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
         if (this.getPierceLevel() <= 0) {
            this.remove();
         }
      } else {
         entity.setFireTicks(j);
         this.setVelocity(this.getVelocity().multiply(-0.1D));
         this.yaw += 180.0F;
         this.prevYaw += 180.0F;
         if (!this.world.isClient && this.getVelocity().lengthSquared() < 1.0E-7D) {
            if (this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
               this.dropStack(this.asItemStack(), 0.1F);
            }

            this.remove();
         }
      }

   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      this.inBlockState = this.world.getBlockState(blockHitResult.getBlockPos());
      super.onBlockHit(blockHitResult);
      Vec3d vec3d = blockHitResult.getPos().subtract(this.getX(), this.getY(), this.getZ());
      this.setVelocity(vec3d);
      Vec3d vec3d2 = vec3d.normalize().multiply(0.05000000074505806D);
      this.setPos(this.getX() - vec3d2.x, this.getY() - vec3d2.y, this.getZ() - vec3d2.z);
      this.playSound(this.getSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      this.inGround = true;
      this.shake = 7;
      this.setCritical(false);
      this.setPierceLevel((byte)0);
      this.setSound(SoundEvents.ENTITY_ARROW_HIT);
      this.setShotFromCrossbow(false);
      this.clearPiercingStatus();
   }

   protected SoundEvent getHitSound() {
      return SoundEvents.ENTITY_ARROW_HIT;
   }

   protected final SoundEvent getSound() {
      return this.sound;
   }

   protected void onHit(LivingEntity target) {
   }

   @Nullable
   protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
      return ProjectileUtil.getEntityCollision(this.world, this, currentPosition, nextPosition, this.getBoundingBox().stretch(this.getVelocity()).expand(1.0D), this::method_26958);
   }

   protected boolean method_26958(Entity entity) {
      return super.method_26958(entity) && (this.piercedEntities == null || !this.piercedEntities.contains(entity.getEntityId()));
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putShort("life", (short)this.life);
      if (this.inBlockState != null) {
         nbt.put("inBlockState", NbtHelper.fromBlockState(this.inBlockState));
      }

      nbt.putByte("shake", (byte)this.shake);
      nbt.putBoolean("inGround", this.inGround);
      nbt.putByte("pickup", (byte)this.pickupType.ordinal());
      nbt.putDouble("damage", this.damage);
      nbt.putBoolean("crit", this.isCritical());
      nbt.putByte("PierceLevel", this.getPierceLevel());
      nbt.putString("SoundEvent", Registry.SOUND_EVENT.getId(this.sound).toString());
      nbt.putBoolean("ShotFromCrossbow", this.isShotFromCrossbow());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.life = nbt.getShort("life");
      if (nbt.contains("inBlockState", 10)) {
         this.inBlockState = NbtHelper.toBlockState(nbt.getCompound("inBlockState"));
      }

      this.shake = nbt.getByte("shake") & 255;
      this.inGround = nbt.getBoolean("inGround");
      if (nbt.contains("damage", 99)) {
         this.damage = nbt.getDouble("damage");
      }

      if (nbt.contains("pickup", 99)) {
         this.pickupType = PersistentProjectileEntity.PickupPermission.fromOrdinal(nbt.getByte("pickup"));
      } else if (nbt.contains("player", 99)) {
         this.pickupType = nbt.getBoolean("player") ? PersistentProjectileEntity.PickupPermission.ALLOWED : PersistentProjectileEntity.PickupPermission.DISALLOWED;
      }

      this.setCritical(nbt.getBoolean("crit"));
      this.setPierceLevel(nbt.getByte("PierceLevel"));
      if (nbt.contains("SoundEvent", 8)) {
         this.sound = (SoundEvent)Registry.SOUND_EVENT.getOrEmpty(new Identifier(nbt.getString("SoundEvent"))).orElse(this.getHitSound());
      }

      this.setShotFromCrossbow(nbt.getBoolean("ShotFromCrossbow"));
   }

   public void setOwner(@Nullable Entity entity) {
      super.setOwner(entity);
      if (entity instanceof PlayerEntity) {
         this.pickupType = ((PlayerEntity)entity).abilities.creativeMode ? PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY : PersistentProjectileEntity.PickupPermission.ALLOWED;
      }

   }

   public void onPlayerCollision(PlayerEntity player) {
      if (!this.world.isClient && (this.inGround || this.isNoClip()) && this.shake <= 0) {
         boolean bl = this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED || this.pickupType == PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY && player.abilities.creativeMode || this.isNoClip() && this.getOwner().getUuid() == player.getUuid();
         if (this.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED && !player.inventory.insertStack(this.asItemStack())) {
            bl = false;
         }

         if (bl) {
            player.sendPickup(this, 1);
            this.remove();
         }

      }
   }

   protected abstract ItemStack asItemStack();

   protected boolean canClimb() {
      return false;
   }

   public void setDamage(double damage) {
      this.damage = damage;
   }

   public double getDamage() {
      return this.damage;
   }

   public void setPunch(int punch) {
      this.punch = punch;
   }

   public boolean isAttackable() {
      return false;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.13F;
   }

   public void setCritical(boolean critical) {
      this.setProjectileFlag(1, critical);
   }

   public void setPierceLevel(byte level) {
      this.dataTracker.set(PIERCE_LEVEL, level);
   }

   private void setProjectileFlag(int index, boolean flag) {
      byte b = (Byte)this.dataTracker.get(PROJECTILE_FLAGS);
      if (flag) {
         this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b | index));
      } else {
         this.dataTracker.set(PROJECTILE_FLAGS, (byte)(b & ~index));
      }

   }

   public boolean isCritical() {
      byte b = (Byte)this.dataTracker.get(PROJECTILE_FLAGS);
      return (b & 1) != 0;
   }

   public boolean isShotFromCrossbow() {
      byte b = (Byte)this.dataTracker.get(PROJECTILE_FLAGS);
      return (b & 4) != 0;
   }

   public byte getPierceLevel() {
      return (Byte)this.dataTracker.get(PIERCE_LEVEL);
   }

   public void applyEnchantmentEffects(LivingEntity entity, float damageModifier) {
      int i = EnchantmentHelper.getEquipmentLevel(Enchantments.POWER, entity);
      int j = EnchantmentHelper.getEquipmentLevel(Enchantments.PUNCH, entity);
      this.setDamage((double)(damageModifier * 2.0F) + this.random.nextGaussian() * 0.25D + (double)((float)this.world.getDifficulty().getId() * 0.11F));
      if (i > 0) {
         this.setDamage(this.getDamage() + (double)i * 0.5D + 0.5D);
      }

      if (j > 0) {
         this.setPunch(j);
      }

      if (EnchantmentHelper.getEquipmentLevel(Enchantments.FLAME, entity) > 0) {
         this.setOnFireFor(100);
      }

   }

   protected float getDragInWater() {
      return 0.6F;
   }

   public void setNoClip(boolean noClip) {
      this.noClip = noClip;
      this.setProjectileFlag(2, noClip);
   }

   public boolean isNoClip() {
      if (!this.world.isClient) {
         return this.noClip;
      } else {
         return ((Byte)this.dataTracker.get(PROJECTILE_FLAGS) & 2) != 0;
      }
   }

   public void setShotFromCrossbow(boolean shotFromCrossbow) {
      this.setProjectileFlag(4, shotFromCrossbow);
   }

   public Packet<?> createSpawnPacket() {
      Entity entity = this.getOwner();
      return new EntitySpawnS2CPacket(this, entity == null ? 0 : entity.getEntityId());
   }

   static {
      PROJECTILE_FLAGS = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
      PIERCE_LEVEL = DataTracker.registerData(PersistentProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
   }

   public static enum PickupPermission {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static PersistentProjectileEntity.PickupPermission fromOrdinal(int ordinal) {
         if (ordinal < 0 || ordinal > values().length) {
            ordinal = 0;
         }

         return values()[ordinal];
      }
   }
}
