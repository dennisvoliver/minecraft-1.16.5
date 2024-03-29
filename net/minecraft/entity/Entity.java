package net.minecraft.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.Packet;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.ReusableStream;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.PortalUtil;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.AreaHelper;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class Entity implements Nameable, CommandOutput {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger();
   private static final List<ItemStack> EMPTY_STACK_LIST = Collections.emptyList();
   private static final Box NULL_BOX = new Box(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static double renderDistanceMultiplier = 1.0D;
   private final EntityType<?> type;
   private int entityId;
   public boolean inanimate;
   private final List<Entity> passengerList;
   protected int ridingCooldown;
   @Nullable
   private Entity vehicle;
   public boolean teleporting;
   public World world;
   public double prevX;
   public double prevY;
   public double prevZ;
   private Vec3d pos;
   private BlockPos blockPos;
   private Vec3d velocity;
   public float yaw;
   public float pitch;
   public float prevYaw;
   public float prevPitch;
   private Box entityBounds;
   protected boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean velocityModified;
   protected Vec3d movementMultiplier;
   public boolean removed;
   public float prevHorizontalSpeed;
   public float horizontalSpeed;
   public float distanceTraveled;
   public float fallDistance;
   private float nextStepSoundDistance;
   private float nextFlySoundDistance;
   public double lastRenderX;
   public double lastRenderY;
   public double lastRenderZ;
   public float stepHeight;
   public boolean noClip;
   public float pushSpeedReduction;
   protected final Random random;
   public int age;
   private int fireTicks;
   protected boolean touchingWater;
   protected Object2DoubleMap<Tag<Fluid>> fluidHeight;
   protected boolean submergedInWater;
   @Nullable
   protected Tag<Fluid> field_25599;
   public int timeUntilRegen;
   protected boolean firstUpdate;
   protected final DataTracker dataTracker;
   protected static final TrackedData<Byte> FLAGS;
   private static final TrackedData<Integer> AIR;
   private static final TrackedData<Optional<Text>> CUSTOM_NAME;
   private static final TrackedData<Boolean> NAME_VISIBLE;
   private static final TrackedData<Boolean> SILENT;
   private static final TrackedData<Boolean> NO_GRAVITY;
   protected static final TrackedData<EntityPose> POSE;
   public boolean updateNeeded;
   public int chunkX;
   public int chunkY;
   public int chunkZ;
   private boolean chunkPosUpdateRequested;
   private Vec3d trackedPosition;
   public boolean ignoreCameraFrustum;
   public boolean velocityDirty;
   private int netherPortalCooldown;
   protected boolean inNetherPortal;
   protected int netherPortalTime;
   protected BlockPos lastNetherPortalPosition;
   private boolean invulnerable;
   protected UUID uuid;
   protected String uuidString;
   protected boolean glowing;
   private final Set<String> scoreboardTags;
   private boolean teleportRequested;
   private final double[] pistonMovementDelta;
   private long pistonMovementTick;
   private EntityDimensions dimensions;
   private float standingEyeHeight;

   public Entity(EntityType<?> type, World world) {
      this.entityId = ENTITY_ID_COUNTER.incrementAndGet();
      this.passengerList = Lists.newArrayList();
      this.velocity = Vec3d.ZERO;
      this.entityBounds = NULL_BOX;
      this.movementMultiplier = Vec3d.ZERO;
      this.nextStepSoundDistance = 1.0F;
      this.nextFlySoundDistance = 1.0F;
      this.random = new Random();
      this.fireTicks = -this.getBurningDuration();
      this.fluidHeight = new Object2DoubleArrayMap(2);
      this.firstUpdate = true;
      this.uuid = MathHelper.randomUuid(this.random);
      this.uuidString = this.uuid.toString();
      this.scoreboardTags = Sets.newHashSet();
      this.pistonMovementDelta = new double[]{0.0D, 0.0D, 0.0D};
      this.type = type;
      this.world = world;
      this.dimensions = type.getDimensions();
      this.pos = Vec3d.ZERO;
      this.blockPos = BlockPos.ORIGIN;
      this.trackedPosition = Vec3d.ZERO;
      this.setPosition(0.0D, 0.0D, 0.0D);
      this.dataTracker = new DataTracker(this);
      this.dataTracker.startTracking(FLAGS, (byte)0);
      this.dataTracker.startTracking(AIR, this.getMaxAir());
      this.dataTracker.startTracking(NAME_VISIBLE, false);
      this.dataTracker.startTracking(CUSTOM_NAME, Optional.empty());
      this.dataTracker.startTracking(SILENT, false);
      this.dataTracker.startTracking(NO_GRAVITY, false);
      this.dataTracker.startTracking(POSE, EntityPose.STANDING);
      this.initDataTracker();
      this.standingEyeHeight = this.getEyeHeight(EntityPose.STANDING, this.dimensions);
   }

   @Environment(EnvType.CLIENT)
   public boolean method_30632(BlockPos blockPos, BlockState blockState) {
      VoxelShape voxelShape = blockState.getCollisionShape(this.world, blockPos, ShapeContext.of(this));
      VoxelShape voxelShape2 = voxelShape.offset((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
      return VoxelShapes.matchesAnywhere(voxelShape2, VoxelShapes.cuboid(this.getBoundingBox()), BooleanBiFunction.AND);
   }

   @Environment(EnvType.CLIENT)
   public int getTeamColorValue() {
      AbstractTeam abstractTeam = this.getScoreboardTeam();
      return abstractTeam != null && abstractTeam.getColor().getColorValue() != null ? abstractTeam.getColor().getColorValue() : 16777215;
   }

   public boolean isSpectator() {
      return false;
   }

   /**
    * Removes all the passengers and removes this entity from any vehicles it is riding.
    */
   public final void detach() {
      if (this.hasPassengers()) {
         this.removeAllPassengers();
      }

      if (this.hasVehicle()) {
         this.stopRiding();
      }

   }

   public void updateTrackedPosition(double x, double y, double z) {
      this.updateTrackedPosition(new Vec3d(x, y, z));
   }

   public void updateTrackedPosition(Vec3d pos) {
      this.trackedPosition = pos;
   }

   @Environment(EnvType.CLIENT)
   public Vec3d getTrackedPosition() {
      return this.trackedPosition;
   }

   public EntityType<?> getType() {
      return this.type;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int id) {
      this.entityId = id;
   }

   public Set<String> getScoreboardTags() {
      return this.scoreboardTags;
   }

   public boolean addScoreboardTag(String tag) {
      return this.scoreboardTags.size() >= 1024 ? false : this.scoreboardTags.add(tag);
   }

   public boolean removeScoreboardTag(String tag) {
      return this.scoreboardTags.remove(tag);
   }

   public void kill() {
      this.remove();
   }

   protected abstract void initDataTracker();

   public DataTracker getDataTracker() {
      return this.dataTracker;
   }

   public boolean equals(Object o) {
      if (o instanceof Entity) {
         return ((Entity)o).entityId == this.entityId;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.entityId;
   }

   @Environment(EnvType.CLIENT)
   protected void afterSpawn() {
      if (this.world != null) {
         for(double d = this.getY(); d > 0.0D && d < 256.0D; ++d) {
            this.setPosition(this.getX(), d, this.getZ());
            if (this.world.isSpaceEmpty(this)) {
               break;
            }
         }

         this.setVelocity(Vec3d.ZERO);
         this.pitch = 0.0F;
      }
   }

   public void remove() {
      this.removed = true;
   }

   public void setPose(EntityPose pose) {
      this.dataTracker.set(POSE, pose);
   }

   public EntityPose getPose() {
      return (EntityPose)this.dataTracker.get(POSE);
   }

   /**
    * Checks if the distance between this entity and the {@code other} entity is less
    * than {@code radius}.
    */
   public boolean isInRange(Entity other, double radius) {
      double d = other.pos.x - this.pos.x;
      double e = other.pos.y - this.pos.y;
      double f = other.pos.z - this.pos.z;
      return d * d + e * e + f * f < radius * radius;
   }

   protected void setRotation(float yaw, float pitch) {
      this.yaw = yaw % 360.0F;
      this.pitch = pitch % 360.0F;
   }

   public void setPosition(double x, double y, double z) {
      this.setPos(x, y, z);
      this.setBoundingBox(this.dimensions.getBoxAt(x, y, z));
   }

   protected void refreshPosition() {
      this.setPosition(this.pos.x, this.pos.y, this.pos.z);
   }

   @Environment(EnvType.CLIENT)
   public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
      double d = cursorDeltaY * 0.15D;
      double e = cursorDeltaX * 0.15D;
      this.pitch = (float)((double)this.pitch + d);
      this.yaw = (float)((double)this.yaw + e);
      this.pitch = MathHelper.clamp(this.pitch, -90.0F, 90.0F);
      this.prevPitch = (float)((double)this.prevPitch + d);
      this.prevYaw = (float)((double)this.prevYaw + e);
      this.prevPitch = MathHelper.clamp(this.prevPitch, -90.0F, 90.0F);
      if (this.vehicle != null) {
         this.vehicle.onPassengerLookAround(this);
      }

   }

   public void tick() {
      if (!this.world.isClient) {
         this.setFlag(6, this.isGlowing());
      }

      this.baseTick();
   }

   public void baseTick() {
      this.world.getProfiler().push("entityBaseTick");
      if (this.hasVehicle() && this.getVehicle().removed) {
         this.stopRiding();
      }

      if (this.ridingCooldown > 0) {
         --this.ridingCooldown;
      }

      this.prevHorizontalSpeed = this.horizontalSpeed;
      this.prevPitch = this.pitch;
      this.prevYaw = this.yaw;
      this.tickNetherPortal();
      if (this.shouldSpawnSprintingParticles()) {
         this.spawnSprintingParticles();
      }

      this.updateWaterState();
      this.updateSubmergedInWaterState();
      this.updateSwimming();
      if (this.world.isClient) {
         this.extinguish();
      } else if (this.fireTicks > 0) {
         if (this.isFireImmune()) {
            this.setFireTicks(this.fireTicks - 4);
            if (this.fireTicks < 0) {
               this.extinguish();
            }
         } else {
            if (this.fireTicks % 20 == 0 && !this.isInLava()) {
               this.damage(DamageSource.ON_FIRE, 1.0F);
            }

            this.setFireTicks(this.fireTicks - 1);
         }
      }

      if (this.isInLava()) {
         this.setOnFireFromLava();
         this.fallDistance *= 0.5F;
      }

      if (this.getY() < -64.0D) {
         this.tickInVoid();
      }

      if (!this.world.isClient) {
         this.setFlag(0, this.fireTicks > 0);
      }

      this.firstUpdate = false;
      this.world.getProfiler().pop();
   }

   public void resetNetherPortalCooldown() {
      this.netherPortalCooldown = this.getDefaultNetherPortalCooldown();
   }

   public boolean hasNetherPortalCooldown() {
      return this.netherPortalCooldown > 0;
   }

   protected void tickNetherPortalCooldown() {
      if (this.hasNetherPortalCooldown()) {
         --this.netherPortalCooldown;
      }

   }

   public int getMaxNetherPortalTime() {
      return 0;
   }

   protected void setOnFireFromLava() {
      if (!this.isFireImmune()) {
         this.setOnFireFor(15);
         this.damage(DamageSource.LAVA, 4.0F);
      }
   }

   public void setOnFireFor(int seconds) {
      int i = seconds * 20;
      if (this instanceof LivingEntity) {
         i = ProtectionEnchantment.transformFireDuration((LivingEntity)this, i);
      }

      if (this.fireTicks < i) {
         this.setFireTicks(i);
      }

   }

   public void setFireTicks(int ticks) {
      this.fireTicks = ticks;
   }

   public int getFireTicks() {
      return this.fireTicks;
   }

   public void extinguish() {
      this.setFireTicks(0);
   }

   /**
    * Called when the entity is 64 blocks below the world's minimum Y position which is {@code 0}.
    * 
    * <p>{@linkplain LivingEntity Living entities} use this to deal {@linkplain net.minecraft.entity.damage.DamageSource#OUT_OF_WORLD out of world damage}.
    */
   protected void tickInVoid() {
      this.remove();
   }

   public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
      return this.doesNotCollide(this.getBoundingBox().offset(offsetX, offsetY, offsetZ));
   }

   private boolean doesNotCollide(Box box) {
      return this.world.isSpaceEmpty(this, box) && !this.world.containsFluid(box);
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void move(MovementType movementType, Vec3d movement) {
      if (this.noClip) {
         this.setBoundingBox(this.getBoundingBox().offset(movement));
         this.moveToBoundingBoxCenter();
      } else {
         if (movementType == MovementType.PISTON) {
            movement = this.adjustMovementForPiston(movement);
            if (movement.equals(Vec3d.ZERO)) {
               return;
            }
         }

         this.world.getProfiler().push("move");
         if (this.movementMultiplier.lengthSquared() > 1.0E-7D) {
            movement = movement.multiply(this.movementMultiplier);
            this.movementMultiplier = Vec3d.ZERO;
            this.setVelocity(Vec3d.ZERO);
         }

         movement = this.adjustMovementForSneaking(movement, movementType);
         Vec3d vec3d = this.adjustMovementForCollisions(movement);
         if (vec3d.lengthSquared() > 1.0E-7D) {
            this.setBoundingBox(this.getBoundingBox().offset(vec3d));
            this.moveToBoundingBoxCenter();
         }

         this.world.getProfiler().pop();
         this.world.getProfiler().push("rest");
         this.horizontalCollision = !MathHelper.approximatelyEquals(movement.x, vec3d.x) || !MathHelper.approximatelyEquals(movement.z, vec3d.z);
         this.verticalCollision = movement.y != vec3d.y;
         this.onGround = this.verticalCollision && movement.y < 0.0D;
         BlockPos blockPos = this.getLandingPos();
         BlockState blockState = this.world.getBlockState(blockPos);
         this.fall(vec3d.y, this.onGround, blockState, blockPos);
         Vec3d vec3d2 = this.getVelocity();
         if (movement.x != vec3d.x) {
            this.setVelocity(0.0D, vec3d2.y, vec3d2.z);
         }

         if (movement.z != vec3d.z) {
            this.setVelocity(vec3d2.x, vec3d2.y, 0.0D);
         }

         Block block = blockState.getBlock();
         if (movement.y != vec3d.y) {
            block.onEntityLand(this.world, this);
         }

         if (this.onGround && !this.bypassesSteppingEffects()) {
            block.onSteppedOn(this.world, blockPos, this);
         }

         if (this.canClimb() && !this.hasVehicle()) {
            double d = vec3d.x;
            double e = vec3d.y;
            double f = vec3d.z;
            if (!block.isIn(BlockTags.CLIMBABLE)) {
               e = 0.0D;
            }

            this.horizontalSpeed = (float)((double)this.horizontalSpeed + (double)MathHelper.sqrt(squaredHorizontalLength(vec3d)) * 0.6D);
            this.distanceTraveled = (float)((double)this.distanceTraveled + (double)MathHelper.sqrt(d * d + e * e + f * f) * 0.6D);
            if (this.distanceTraveled > this.nextStepSoundDistance && !blockState.isAir()) {
               this.nextStepSoundDistance = this.calculateNextStepSoundDistance();
               if (this.isTouchingWater()) {
                  Entity entity = this.hasPassengers() && this.getPrimaryPassenger() != null ? this.getPrimaryPassenger() : this;
                  float g = entity == this ? 0.35F : 0.4F;
                  Vec3d vec3d3 = entity.getVelocity();
                  float h = MathHelper.sqrt(vec3d3.x * vec3d3.x * 0.20000000298023224D + vec3d3.y * vec3d3.y + vec3d3.z * vec3d3.z * 0.20000000298023224D) * g;
                  if (h > 1.0F) {
                     h = 1.0F;
                  }

                  this.playSwimSound(h);
               } else {
                  this.playStepSound(blockPos, blockState);
               }
            } else if (this.distanceTraveled > this.nextFlySoundDistance && this.hasWings() && blockState.isAir()) {
               this.nextFlySoundDistance = this.playFlySound(this.distanceTraveled);
            }
         }

         try {
            this.checkBlockCollision();
         } catch (Throwable var18) {
            CrashReport crashReport = CrashReport.create(var18, "Checking entity block collision");
            CrashReportSection crashReportSection = crashReport.addElement("Entity being checked for collision");
            this.populateCrashReport(crashReportSection);
            throw new CrashException(crashReport);
         }

         float i = this.getVelocityMultiplier();
         this.setVelocity(this.getVelocity().multiply((double)i, 1.0D, (double)i));
         if (this.world.method_29556(this.getBoundingBox().contract(0.001D)).noneMatch((blockStatex) -> {
            return blockStatex.isIn(BlockTags.FIRE) || blockStatex.isOf(Blocks.LAVA);
         }) && this.fireTicks <= 0) {
            this.setFireTicks(-this.getBurningDuration());
         }

         if (this.isWet() && this.isOnFire()) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            this.setFireTicks(-this.getBurningDuration());
         }

         this.world.getProfiler().pop();
      }
   }

   protected BlockPos getLandingPos() {
      int i = MathHelper.floor(this.pos.x);
      int j = MathHelper.floor(this.pos.y - 0.20000000298023224D);
      int k = MathHelper.floor(this.pos.z);
      BlockPos blockPos = new BlockPos(i, j, k);
      if (this.world.getBlockState(blockPos).isAir()) {
         BlockPos blockPos2 = blockPos.down();
         BlockState blockState = this.world.getBlockState(blockPos2);
         Block block = blockState.getBlock();
         if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS) || block instanceof FenceGateBlock) {
            return blockPos2;
         }
      }

      return blockPos;
   }

   protected float getJumpVelocityMultiplier() {
      float f = this.world.getBlockState(this.getBlockPos()).getBlock().getJumpVelocityMultiplier();
      float g = this.world.getBlockState(this.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
      return (double)f == 1.0D ? g : f;
   }

   protected float getVelocityMultiplier() {
      Block block = this.world.getBlockState(this.getBlockPos()).getBlock();
      float f = block.getVelocityMultiplier();
      if (block != Blocks.WATER && block != Blocks.BUBBLE_COLUMN) {
         return (double)f == 1.0D ? this.world.getBlockState(this.getVelocityAffectingPos()).getBlock().getVelocityMultiplier() : f;
      } else {
         return f;
      }
   }

   protected BlockPos getVelocityAffectingPos() {
      return new BlockPos(this.pos.x, this.getBoundingBox().minY - 0.5000001D, this.pos.z);
   }

   protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
      return movement;
   }

   protected Vec3d adjustMovementForPiston(Vec3d movement) {
      if (movement.lengthSquared() <= 1.0E-7D) {
         return movement;
      } else {
         long l = this.world.getTime();
         if (l != this.pistonMovementTick) {
            Arrays.fill(this.pistonMovementDelta, 0.0D);
            this.pistonMovementTick = l;
         }

         double f;
         if (movement.x != 0.0D) {
            f = this.calculatePistonMovementFactor(Direction.Axis.X, movement.x);
            return Math.abs(f) <= 9.999999747378752E-6D ? Vec3d.ZERO : new Vec3d(f, 0.0D, 0.0D);
         } else if (movement.y != 0.0D) {
            f = this.calculatePistonMovementFactor(Direction.Axis.Y, movement.y);
            return Math.abs(f) <= 9.999999747378752E-6D ? Vec3d.ZERO : new Vec3d(0.0D, f, 0.0D);
         } else if (movement.z != 0.0D) {
            f = this.calculatePistonMovementFactor(Direction.Axis.Z, movement.z);
            return Math.abs(f) <= 9.999999747378752E-6D ? Vec3d.ZERO : new Vec3d(0.0D, 0.0D, f);
         } else {
            return Vec3d.ZERO;
         }
      }
   }

   private double calculatePistonMovementFactor(Direction.Axis axis, double offsetFactor) {
      int i = axis.ordinal();
      double d = MathHelper.clamp(offsetFactor + this.pistonMovementDelta[i], -0.51D, 0.51D);
      offsetFactor = d - this.pistonMovementDelta[i];
      this.pistonMovementDelta[i] = d;
      return offsetFactor;
   }

   private Vec3d adjustMovementForCollisions(Vec3d movement) {
      Box box = this.getBoundingBox();
      ShapeContext shapeContext = ShapeContext.of(this);
      VoxelShape voxelShape = this.world.getWorldBorder().asVoxelShape();
      Stream<VoxelShape> stream = VoxelShapes.matchesAnywhere(voxelShape, VoxelShapes.cuboid(box.contract(1.0E-7D)), BooleanBiFunction.AND) ? Stream.empty() : Stream.of(voxelShape);
      Stream<VoxelShape> stream2 = this.world.getEntityCollisions(this, box.stretch(movement), (entity) -> {
         return true;
      });
      ReusableStream<VoxelShape> reusableStream = new ReusableStream(Stream.concat(stream2, stream));
      Vec3d vec3d = movement.lengthSquared() == 0.0D ? movement : adjustMovementForCollisions(this, movement, box, this.world, shapeContext, reusableStream);
      boolean bl = movement.x != vec3d.x;
      boolean bl2 = movement.y != vec3d.y;
      boolean bl3 = movement.z != vec3d.z;
      boolean bl4 = this.onGround || bl2 && movement.y < 0.0D;
      if (this.stepHeight > 0.0F && bl4 && (bl || bl3)) {
         Vec3d vec3d2 = adjustMovementForCollisions(this, new Vec3d(movement.x, (double)this.stepHeight, movement.z), box, this.world, shapeContext, reusableStream);
         Vec3d vec3d3 = adjustMovementForCollisions(this, new Vec3d(0.0D, (double)this.stepHeight, 0.0D), box.stretch(movement.x, 0.0D, movement.z), this.world, shapeContext, reusableStream);
         if (vec3d3.y < (double)this.stepHeight) {
            Vec3d vec3d4 = adjustMovementForCollisions(this, new Vec3d(movement.x, 0.0D, movement.z), box.offset(vec3d3), this.world, shapeContext, reusableStream).add(vec3d3);
            if (squaredHorizontalLength(vec3d4) > squaredHorizontalLength(vec3d2)) {
               vec3d2 = vec3d4;
            }
         }

         if (squaredHorizontalLength(vec3d2) > squaredHorizontalLength(vec3d)) {
            return vec3d2.add(adjustMovementForCollisions(this, new Vec3d(0.0D, -vec3d2.y + movement.y, 0.0D), box.offset(vec3d2), this.world, shapeContext, reusableStream));
         }
      }

      return vec3d;
   }

   public static double squaredHorizontalLength(Vec3d vector) {
      return vector.x * vector.x + vector.z * vector.z;
   }

   public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, ShapeContext context, ReusableStream<VoxelShape> collisions) {
      boolean bl = movement.x == 0.0D;
      boolean bl2 = movement.y == 0.0D;
      boolean bl3 = movement.z == 0.0D;
      if ((!bl || !bl2) && (!bl || !bl3) && (!bl2 || !bl3)) {
         ReusableStream<VoxelShape> reusableStream = new ReusableStream(Stream.concat(collisions.stream(), world.getBlockCollisions(entity, entityBoundingBox.stretch(movement))));
         return adjustMovementForCollisions(movement, entityBoundingBox, reusableStream);
      } else {
         return adjustSingleAxisMovementForCollisions(movement, entityBoundingBox, world, context, collisions);
      }
   }

   public static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, ReusableStream<VoxelShape> collisions) {
      double d = movement.x;
      double e = movement.y;
      double f = movement.z;
      if (e != 0.0D) {
         e = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, collisions.stream(), e);
         if (e != 0.0D) {
            entityBoundingBox = entityBoundingBox.offset(0.0D, e, 0.0D);
         }
      }

      boolean bl = Math.abs(d) < Math.abs(f);
      if (bl && f != 0.0D) {
         f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions.stream(), f);
         if (f != 0.0D) {
            entityBoundingBox = entityBoundingBox.offset(0.0D, 0.0D, f);
         }
      }

      if (d != 0.0D) {
         d = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, collisions.stream(), d);
         if (!bl && d != 0.0D) {
            entityBoundingBox = entityBoundingBox.offset(d, 0.0D, 0.0D);
         }
      }

      if (!bl && f != 0.0D) {
         f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions.stream(), f);
      }

      return new Vec3d(d, e, f);
   }

   public static Vec3d adjustSingleAxisMovementForCollisions(Vec3d movement, Box entityBoundingBox, WorldView world, ShapeContext context, ReusableStream<VoxelShape> collisions) {
      double d = movement.x;
      double e = movement.y;
      double f = movement.z;
      if (e != 0.0D) {
         e = VoxelShapes.calculatePushVelocity(Direction.Axis.Y, entityBoundingBox, world, e, context, collisions.stream());
         if (e != 0.0D) {
            entityBoundingBox = entityBoundingBox.offset(0.0D, e, 0.0D);
         }
      }

      boolean bl = Math.abs(d) < Math.abs(f);
      if (bl && f != 0.0D) {
         f = VoxelShapes.calculatePushVelocity(Direction.Axis.Z, entityBoundingBox, world, f, context, collisions.stream());
         if (f != 0.0D) {
            entityBoundingBox = entityBoundingBox.offset(0.0D, 0.0D, f);
         }
      }

      if (d != 0.0D) {
         d = VoxelShapes.calculatePushVelocity(Direction.Axis.X, entityBoundingBox, world, d, context, collisions.stream());
         if (!bl && d != 0.0D) {
            entityBoundingBox = entityBoundingBox.offset(d, 0.0D, 0.0D);
         }
      }

      if (!bl && f != 0.0D) {
         f = VoxelShapes.calculatePushVelocity(Direction.Axis.Z, entityBoundingBox, world, f, context, collisions.stream());
      }

      return new Vec3d(d, e, f);
   }

   protected float calculateNextStepSoundDistance() {
      return (float)((int)this.distanceTraveled + 1);
   }

   public void moveToBoundingBoxCenter() {
      Box box = this.getBoundingBox();
      this.setPos((box.minX + box.maxX) / 2.0D, box.minY, (box.minZ + box.maxZ) / 2.0D);
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_GENERIC_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_GENERIC_SPLASH;
   }

   protected SoundEvent getHighSpeedSplashSound() {
      return SoundEvents.ENTITY_GENERIC_SPLASH;
   }

   protected void checkBlockCollision() {
      Box box = this.getBoundingBox();
      BlockPos blockPos = new BlockPos(box.minX + 0.001D, box.minY + 0.001D, box.minZ + 0.001D);
      BlockPos blockPos2 = new BlockPos(box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D);
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      if (this.world.isRegionLoaded(blockPos, blockPos2)) {
         for(int i = blockPos.getX(); i <= blockPos2.getX(); ++i) {
            for(int j = blockPos.getY(); j <= blockPos2.getY(); ++j) {
               for(int k = blockPos.getZ(); k <= blockPos2.getZ(); ++k) {
                  mutable.set(i, j, k);
                  BlockState blockState = this.world.getBlockState(mutable);

                  try {
                     blockState.onEntityCollision(this.world, mutable, this);
                     this.onBlockCollision(blockState);
                  } catch (Throwable var12) {
                     CrashReport crashReport = CrashReport.create(var12, "Colliding entity with block");
                     CrashReportSection crashReportSection = crashReport.addElement("Block being collided with");
                     CrashReportSection.addBlockInfo(crashReportSection, mutable, blockState);
                     throw new CrashException(crashReport);
                  }
               }
            }
         }
      }

   }

   protected void onBlockCollision(BlockState state) {
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      if (!state.getMaterial().isLiquid()) {
         BlockState blockState = this.world.getBlockState(pos.up());
         BlockSoundGroup blockSoundGroup = blockState.isOf(Blocks.SNOW) ? blockState.getSoundGroup() : state.getSoundGroup();
         this.playSound(blockSoundGroup.getStepSound(), blockSoundGroup.getVolume() * 0.15F, blockSoundGroup.getPitch());
      }
   }

   protected void playSwimSound(float volume) {
      this.playSound(this.getSwimSound(), volume, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   protected float playFlySound(float distance) {
      return 0.0F;
   }

   protected boolean hasWings() {
      return false;
   }

   public void playSound(SoundEvent sound, float volume, float pitch) {
      if (!this.isSilent()) {
         this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundCategory(), volume, pitch);
      }

   }

   public boolean isSilent() {
      return (Boolean)this.dataTracker.get(SILENT);
   }

   public void setSilent(boolean silent) {
      this.dataTracker.set(SILENT, silent);
   }

   public boolean hasNoGravity() {
      return (Boolean)this.dataTracker.get(NO_GRAVITY);
   }

   public void setNoGravity(boolean noGravity) {
      this.dataTracker.set(NO_GRAVITY, noGravity);
   }

   protected boolean canClimb() {
      return true;
   }

   protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
      if (onGround) {
         if (this.fallDistance > 0.0F) {
            landedState.getBlock().onLandedUpon(this.world, landedPosition, this, this.fallDistance);
         }

         this.fallDistance = 0.0F;
      } else if (heightDifference < 0.0D) {
         this.fallDistance = (float)((double)this.fallDistance - heightDifference);
      }

   }

   public boolean isFireImmune() {
      return this.getType().isFireImmune();
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      if (this.hasPassengers()) {
         Iterator var3 = this.getPassengerList().iterator();

         while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            entity.handleFallDamage(fallDistance, damageMultiplier);
         }
      }

      return false;
   }

   /**
    * Returns whether this entity's hitbox is touching water fluid.
    */
   public boolean isTouchingWater() {
      return this.touchingWater;
   }

   private boolean isBeingRainedOn() {
      BlockPos blockPos = this.getBlockPos();
      return this.world.hasRain(blockPos) || this.world.hasRain(new BlockPos((double)blockPos.getX(), this.getBoundingBox().maxY, (double)blockPos.getZ()));
   }

   private boolean isInsideBubbleColumn() {
      return this.world.getBlockState(this.getBlockPos()).isOf(Blocks.BUBBLE_COLUMN);
   }

   public boolean isTouchingWaterOrRain() {
      return this.isTouchingWater() || this.isBeingRainedOn();
   }

   /**
    * Returns whether this entity is touching water, or is being rained on, or is inside a bubble column...
    * 
    * @see net.minecraft.entity.Entity#isTouchingWater()
    * @see net.minecraft.entity.Entity#isBeingRainedOn()
    * @see net.minecraft.entity.Entity#isInsideBubbleColumn()
    */
   public boolean isWet() {
      return this.isTouchingWater() || this.isBeingRainedOn() || this.isInsideBubbleColumn();
   }

   public boolean isInsideWaterOrBubbleColumn() {
      return this.isTouchingWater() || this.isInsideBubbleColumn();
   }

   /**
    * Returns whether this entity's hitbox is fully submerged in water.
    */
   public boolean isSubmergedInWater() {
      return this.submergedInWater && this.isTouchingWater();
   }

   public void updateSwimming() {
      if (this.isSwimming()) {
         this.setSwimming(this.isSprinting() && this.isTouchingWater() && !this.hasVehicle());
      } else {
         this.setSwimming(this.isSprinting() && this.isSubmergedInWater() && !this.hasVehicle());
      }

   }

   protected boolean updateWaterState() {
      this.fluidHeight.clear();
      this.checkWaterState();
      double d = this.world.getDimension().isUltrawarm() ? 0.007D : 0.0023333333333333335D;
      boolean bl = this.updateMovementInFluid(FluidTags.LAVA, d);
      return this.isTouchingWater() || bl;
   }

   void checkWaterState() {
      if (this.getVehicle() instanceof BoatEntity) {
         this.touchingWater = false;
      } else if (this.updateMovementInFluid(FluidTags.WATER, 0.014D)) {
         if (!this.touchingWater && !this.firstUpdate) {
            this.onSwimmingStart();
         }

         this.fallDistance = 0.0F;
         this.touchingWater = true;
         this.extinguish();
      } else {
         this.touchingWater = false;
      }

   }

   private void updateSubmergedInWaterState() {
      this.submergedInWater = this.isSubmergedIn(FluidTags.WATER);
      this.field_25599 = null;
      double d = this.getEyeY() - 0.1111111119389534D;
      Entity entity = this.getVehicle();
      if (entity instanceof BoatEntity) {
         BoatEntity boatEntity = (BoatEntity)entity;
         if (!boatEntity.isSubmergedInWater() && boatEntity.getBoundingBox().maxY >= d && boatEntity.getBoundingBox().minY <= d) {
            return;
         }
      }

      BlockPos blockPos = new BlockPos(this.getX(), d, this.getZ());
      FluidState fluidState = this.world.getFluidState(blockPos);
      Iterator var6 = FluidTags.getRequiredTags().iterator();

      Tag tag;
      do {
         if (!var6.hasNext()) {
            return;
         }

         tag = (Tag)var6.next();
      } while(!fluidState.isIn(tag));

      double e = (double)((float)blockPos.getY() + fluidState.getHeight(this.world, blockPos));
      if (e > d) {
         this.field_25599 = tag;
      }

   }

   protected void onSwimmingStart() {
      Entity entity = this.hasPassengers() && this.getPrimaryPassenger() != null ? this.getPrimaryPassenger() : this;
      float f = entity == this ? 0.2F : 0.9F;
      Vec3d vec3d = entity.getVelocity();
      float g = MathHelper.sqrt(vec3d.x * vec3d.x * 0.20000000298023224D + vec3d.y * vec3d.y + vec3d.z * vec3d.z * 0.20000000298023224D) * f;
      if (g > 1.0F) {
         g = 1.0F;
      }

      if ((double)g < 0.25D) {
         this.playSound(this.getSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      } else {
         this.playSound(this.getHighSpeedSplashSound(), g, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      }

      float h = (float)MathHelper.floor(this.getY());

      int j;
      double k;
      double l;
      for(j = 0; (float)j < 1.0F + this.dimensions.width * 20.0F; ++j) {
         k = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         l = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         this.world.addParticle(ParticleTypes.BUBBLE, this.getX() + k, (double)(h + 1.0F), this.getZ() + l, vec3d.x, vec3d.y - this.random.nextDouble() * 0.20000000298023224D, vec3d.z);
      }

      for(j = 0; (float)j < 1.0F + this.dimensions.width * 20.0F; ++j) {
         k = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         l = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         this.world.addParticle(ParticleTypes.SPLASH, this.getX() + k, (double)(h + 1.0F), this.getZ() + l, vec3d.x, vec3d.y, vec3d.z);
      }

   }

   protected BlockState getLandingBlockState() {
      return this.world.getBlockState(this.getLandingPos());
   }

   public boolean shouldSpawnSprintingParticles() {
      return this.isSprinting() && !this.isTouchingWater() && !this.isSpectator() && !this.isInSneakingPose() && !this.isInLava() && this.isAlive();
   }

   protected void spawnSprintingParticles() {
      int i = MathHelper.floor(this.getX());
      int j = MathHelper.floor(this.getY() - 0.20000000298023224D);
      int k = MathHelper.floor(this.getZ());
      BlockPos blockPos = new BlockPos(i, j, k);
      BlockState blockState = this.world.getBlockState(blockPos);
      if (blockState.getRenderType() != BlockRenderType.INVISIBLE) {
         Vec3d vec3d = this.getVelocity();
         this.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width, vec3d.x * -4.0D, 1.5D, vec3d.z * -4.0D);
      }

   }

   public boolean isSubmergedIn(Tag<Fluid> fluidTag) {
      return this.field_25599 == fluidTag;
   }

   public boolean isInLava() {
      return !this.firstUpdate && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0D;
   }

   public void updateVelocity(float speed, Vec3d movementInput) {
      Vec3d vec3d = movementInputToVelocity(movementInput, speed, this.yaw);
      this.setVelocity(this.getVelocity().add(vec3d));
   }

   private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
      double d = movementInput.lengthSquared();
      if (d < 1.0E-7D) {
         return Vec3d.ZERO;
      } else {
         Vec3d vec3d = (d > 1.0D ? movementInput.normalize() : movementInput).multiply((double)speed);
         float f = MathHelper.sin(yaw * 0.017453292F);
         float g = MathHelper.cos(yaw * 0.017453292F);
         return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
      }
   }

   public float getBrightnessAtEyes() {
      BlockPos.Mutable mutable = new BlockPos.Mutable(this.getX(), 0.0D, this.getZ());
      if (this.world.isChunkLoaded(mutable)) {
         mutable.setY(MathHelper.floor(this.getEyeY()));
         return this.world.getBrightness(mutable);
      } else {
         return 0.0F;
      }
   }

   public void setWorld(World world) {
      this.world = world;
   }

   public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
      this.updatePosition(x, y, z);
      this.yaw = yaw % 360.0F;
      this.pitch = MathHelper.clamp(pitch, -90.0F, 90.0F) % 360.0F;
      this.prevYaw = this.yaw;
      this.prevPitch = this.pitch;
   }

   public void updatePosition(double x, double y, double z) {
      double d = MathHelper.clamp(x, -3.0E7D, 3.0E7D);
      double e = MathHelper.clamp(z, -3.0E7D, 3.0E7D);
      this.prevX = d;
      this.prevY = y;
      this.prevZ = e;
      this.setPosition(d, y, e);
   }

   public void refreshPositionAfterTeleport(Vec3d pos) {
      this.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
   }

   public void refreshPositionAfterTeleport(double x, double y, double z) {
      this.refreshPositionAndAngles(x, y, z, this.yaw, this.pitch);
   }

   public void refreshPositionAndAngles(BlockPos pos, float yaw, float pitch) {
      this.refreshPositionAndAngles((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, yaw, pitch);
   }

   public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
      this.resetPosition(x, y, z);
      this.yaw = yaw;
      this.pitch = pitch;
      this.refreshPosition();
   }

   public void resetPosition(double x, double y, double z) {
      this.setPos(x, y, z);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
      this.lastRenderX = x;
      this.lastRenderY = y;
      this.lastRenderZ = z;
   }

   public float distanceTo(Entity entity) {
      float f = (float)(this.getX() - entity.getX());
      float g = (float)(this.getY() - entity.getY());
      float h = (float)(this.getZ() - entity.getZ());
      return MathHelper.sqrt(f * f + g * g + h * h);
   }

   public double squaredDistanceTo(double x, double y, double z) {
      double d = this.getX() - x;
      double e = this.getY() - y;
      double f = this.getZ() - z;
      return d * d + e * e + f * f;
   }

   public double squaredDistanceTo(Entity entity) {
      return this.squaredDistanceTo(entity.getPos());
   }

   public double squaredDistanceTo(Vec3d vector) {
      double d = this.getX() - vector.x;
      double e = this.getY() - vector.y;
      double f = this.getZ() - vector.z;
      return d * d + e * e + f * f;
   }

   public void onPlayerCollision(PlayerEntity player) {
   }

   public void pushAwayFrom(Entity entity) {
      if (!this.isConnectedThroughVehicle(entity)) {
         if (!entity.noClip && !this.noClip) {
            double d = entity.getX() - this.getX();
            double e = entity.getZ() - this.getZ();
            double f = MathHelper.absMax(d, e);
            if (f >= 0.009999999776482582D) {
               f = (double)MathHelper.sqrt(f);
               d /= f;
               e /= f;
               double g = 1.0D / f;
               if (g > 1.0D) {
                  g = 1.0D;
               }

               d *= g;
               e *= g;
               d *= 0.05000000074505806D;
               e *= 0.05000000074505806D;
               d *= (double)(1.0F - this.pushSpeedReduction);
               e *= (double)(1.0F - this.pushSpeedReduction);
               if (!this.hasPassengers()) {
                  this.addVelocity(-d, 0.0D, -e);
               }

               if (!entity.hasPassengers()) {
                  entity.addVelocity(d, 0.0D, e);
               }
            }

         }
      }
   }

   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      this.setVelocity(this.getVelocity().add(deltaX, deltaY, deltaZ));
      this.velocityDirty = true;
   }

   protected void scheduleVelocityUpdate() {
      this.velocityModified = true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else {
         this.scheduleVelocityUpdate();
         return false;
      }
   }

   public final Vec3d getRotationVec(float tickDelta) {
      return this.getRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
   }

   public float getPitch(float tickDelta) {
      return tickDelta == 1.0F ? this.pitch : MathHelper.lerp(tickDelta, this.prevPitch, this.pitch);
   }

   public float getYaw(float tickDelta) {
      return tickDelta == 1.0F ? this.yaw : MathHelper.lerp(tickDelta, this.prevYaw, this.yaw);
   }

   protected final Vec3d getRotationVector(float pitch, float yaw) {
      float f = pitch * 0.017453292F;
      float g = -yaw * 0.017453292F;
      float h = MathHelper.cos(g);
      float i = MathHelper.sin(g);
      float j = MathHelper.cos(f);
      float k = MathHelper.sin(f);
      return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
   }

   public final Vec3d getOppositeRotationVector(float tickDelta) {
      return this.getOppositeRotationVector(this.getPitch(tickDelta), this.getYaw(tickDelta));
   }

   protected final Vec3d getOppositeRotationVector(float pitch, float yaw) {
      return this.getRotationVector(pitch - 90.0F, yaw);
   }

   public final Vec3d getCameraPosVec(float tickDelta) {
      if (tickDelta == 1.0F) {
         return new Vec3d(this.getX(), this.getEyeY(), this.getZ());
      } else {
         double d = MathHelper.lerp((double)tickDelta, this.prevX, this.getX());
         double e = MathHelper.lerp((double)tickDelta, this.prevY, this.getY()) + (double)this.getStandingEyeHeight();
         double f = MathHelper.lerp((double)tickDelta, this.prevZ, this.getZ());
         return new Vec3d(d, e, f);
      }
   }

   @Environment(EnvType.CLIENT)
   public Vec3d getClientCameraPosVec(float tickDelta) {
      return this.getCameraPosVec(tickDelta);
   }

   @Environment(EnvType.CLIENT)
   public final Vec3d method_30950(float f) {
      double d = MathHelper.lerp((double)f, this.prevX, this.getX());
      double e = MathHelper.lerp((double)f, this.prevY, this.getY());
      double g = MathHelper.lerp((double)f, this.prevZ, this.getZ());
      return new Vec3d(d, e, g);
   }

   public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
      Vec3d vec3d = this.getCameraPosVec(tickDelta);
      Vec3d vec3d2 = this.getRotationVec(tickDelta);
      Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
      return this.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this));
   }

   public boolean collides() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public void updateKilledAdvancementCriterion(Entity killer, int score, DamageSource damageSource) {
      if (killer instanceof ServerPlayerEntity) {
         Criteria.ENTITY_KILLED_PLAYER.trigger((ServerPlayerEntity)killer, this, damageSource);
      }

   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      double d = this.getX() - cameraX;
      double e = this.getY() - cameraY;
      double f = this.getZ() - cameraZ;
      double g = d * d + e * e + f * f;
      return this.shouldRender(g);
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength();
      if (Double.isNaN(d)) {
         d = 1.0D;
      }

      d *= 64.0D * renderDistanceMultiplier;
      return distance < d * d;
   }

   public boolean saveSelfNbt(NbtCompound nbt) {
      String string = this.getSavedEntityId();
      if (!this.removed && string != null) {
         nbt.putString("id", string);
         this.writeNbt(nbt);
         return true;
      } else {
         return false;
      }
   }

   public boolean saveNbt(NbtCompound nbt) {
      return this.hasVehicle() ? false : this.saveSelfNbt(nbt);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      try {
         if (this.vehicle != null) {
            nbt.put("Pos", this.toNbtList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
         } else {
            nbt.put("Pos", this.toNbtList(this.getX(), this.getY(), this.getZ()));
         }

         Vec3d vec3d = this.getVelocity();
         nbt.put("Motion", this.toNbtList(vec3d.x, vec3d.y, vec3d.z));
         nbt.put("Rotation", this.toNbtList(this.yaw, this.pitch));
         nbt.putFloat("FallDistance", this.fallDistance);
         nbt.putShort("Fire", (short)this.fireTicks);
         nbt.putShort("Air", (short)this.getAir());
         nbt.putBoolean("OnGround", this.onGround);
         nbt.putBoolean("Invulnerable", this.invulnerable);
         nbt.putInt("PortalCooldown", this.netherPortalCooldown);
         nbt.putUuid("UUID", this.getUuid());
         Text text = this.getCustomName();
         if (text != null) {
            nbt.putString("CustomName", Text.Serializer.toJson(text));
         }

         if (this.isCustomNameVisible()) {
            nbt.putBoolean("CustomNameVisible", this.isCustomNameVisible());
         }

         if (this.isSilent()) {
            nbt.putBoolean("Silent", this.isSilent());
         }

         if (this.hasNoGravity()) {
            nbt.putBoolean("NoGravity", this.hasNoGravity());
         }

         if (this.glowing) {
            nbt.putBoolean("Glowing", this.glowing);
         }

         Iterator var5;
         NbtList nbtList2;
         if (!this.scoreboardTags.isEmpty()) {
            nbtList2 = new NbtList();
            var5 = this.scoreboardTags.iterator();

            while(var5.hasNext()) {
               String string = (String)var5.next();
               nbtList2.add(NbtString.of(string));
            }

            nbt.put("Tags", nbtList2);
         }

         this.writeCustomDataToNbt(nbt);
         if (this.hasPassengers()) {
            nbtList2 = new NbtList();
            var5 = this.getPassengerList().iterator();

            while(var5.hasNext()) {
               Entity entity = (Entity)var5.next();
               NbtCompound nbtCompound = new NbtCompound();
               if (entity.saveSelfNbt(nbtCompound)) {
                  nbtList2.add(nbtCompound);
               }
            }

            if (!nbtList2.isEmpty()) {
               nbt.put("Passengers", nbtList2);
            }
         }

         return nbt;
      } catch (Throwable var8) {
         CrashReport crashReport = CrashReport.create(var8, "Saving entity NBT");
         CrashReportSection crashReportSection = crashReport.addElement("Entity being saved");
         this.populateCrashReport(crashReportSection);
         throw new CrashException(crashReport);
      }
   }

   public void readNbt(NbtCompound nbt) {
      try {
         NbtList nbtList = nbt.getList("Pos", 6);
         NbtList nbtList2 = nbt.getList("Motion", 6);
         NbtList nbtList3 = nbt.getList("Rotation", 5);
         double d = nbtList2.getDouble(0);
         double e = nbtList2.getDouble(1);
         double f = nbtList2.getDouble(2);
         this.setVelocity(Math.abs(d) > 10.0D ? 0.0D : d, Math.abs(e) > 10.0D ? 0.0D : e, Math.abs(f) > 10.0D ? 0.0D : f);
         this.resetPosition(nbtList.getDouble(0), nbtList.getDouble(1), nbtList.getDouble(2));
         this.yaw = nbtList3.getFloat(0);
         this.pitch = nbtList3.getFloat(1);
         this.prevYaw = this.yaw;
         this.prevPitch = this.pitch;
         this.setHeadYaw(this.yaw);
         this.setBodyYaw(this.yaw);
         this.fallDistance = nbt.getFloat("FallDistance");
         this.fireTicks = nbt.getShort("Fire");
         this.setAir(nbt.getShort("Air"));
         this.onGround = nbt.getBoolean("OnGround");
         this.invulnerable = nbt.getBoolean("Invulnerable");
         this.netherPortalCooldown = nbt.getInt("PortalCooldown");
         if (nbt.containsUuid("UUID")) {
            this.uuid = nbt.getUuid("UUID");
            this.uuidString = this.uuid.toString();
         }

         if (Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ())) {
            if (Double.isFinite((double)this.yaw) && Double.isFinite((double)this.pitch)) {
               this.refreshPosition();
               this.setRotation(this.yaw, this.pitch);
               if (nbt.contains("CustomName", 8)) {
                  String string = nbt.getString("CustomName");

                  try {
                     this.setCustomName(Text.Serializer.fromJson(string));
                  } catch (Exception var14) {
                     LOGGER.warn((String)"Failed to parse entity custom name {}", (Object)string, (Object)var14);
                  }
               }

               this.setCustomNameVisible(nbt.getBoolean("CustomNameVisible"));
               this.setSilent(nbt.getBoolean("Silent"));
               this.setNoGravity(nbt.getBoolean("NoGravity"));
               this.setGlowing(nbt.getBoolean("Glowing"));
               if (nbt.contains("Tags", 9)) {
                  this.scoreboardTags.clear();
                  NbtList nbtList4 = nbt.getList("Tags", 8);
                  int i = Math.min(nbtList4.size(), 1024);

                  for(int j = 0; j < i; ++j) {
                     this.scoreboardTags.add(nbtList4.getString(j));
                  }
               }

               this.readCustomDataFromNbt(nbt);
               if (this.shouldSetPositionOnLoad()) {
                  this.refreshPosition();
               }

            } else {
               throw new IllegalStateException("Entity has invalid rotation");
            }
         } else {
            throw new IllegalStateException("Entity has invalid position");
         }
      } catch (Throwable var15) {
         CrashReport crashReport = CrashReport.create(var15, "Loading entity NBT");
         CrashReportSection crashReportSection = crashReport.addElement("Entity being loaded");
         this.populateCrashReport(crashReportSection);
         throw new CrashException(crashReport);
      }
   }

   protected boolean shouldSetPositionOnLoad() {
      return true;
   }

   @Nullable
   protected final String getSavedEntityId() {
      EntityType<?> entityType = this.getType();
      Identifier identifier = EntityType.getId(entityType);
      return entityType.isSaveable() && identifier != null ? identifier.toString() : null;
   }

   protected abstract void readCustomDataFromNbt(NbtCompound nbt);

   protected abstract void writeCustomDataToNbt(NbtCompound nbt);

   protected NbtList toNbtList(double... values) {
      NbtList nbtList = new NbtList();
      double[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         double d = var3[var5];
         nbtList.add(NbtDouble.of(d));
      }

      return nbtList;
   }

   protected NbtList toNbtList(float... values) {
      NbtList nbtList = new NbtList();
      float[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         float f = var3[var5];
         nbtList.add(NbtFloat.of(f));
      }

      return nbtList;
   }

   @Nullable
   public ItemEntity dropItem(ItemConvertible item) {
      return this.dropItem(item, 0);
   }

   @Nullable
   public ItemEntity dropItem(ItemConvertible item, int yOffset) {
      return this.dropStack(new ItemStack(item), (float)yOffset);
   }

   @Nullable
   public ItemEntity dropStack(ItemStack stack) {
      return this.dropStack(stack, 0.0F);
   }

   @Nullable
   public ItemEntity dropStack(ItemStack stack, float yOffset) {
      if (stack.isEmpty()) {
         return null;
      } else if (this.world.isClient) {
         return null;
      } else {
         ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY() + (double)yOffset, this.getZ(), stack);
         itemEntity.setToDefaultPickupDelay();
         this.world.spawnEntity(itemEntity);
         return itemEntity;
      }
   }

   public boolean isAlive() {
      return !this.removed;
   }

   public boolean isInsideWall() {
      if (this.noClip) {
         return false;
      } else {
         float f = 0.1F;
         float g = this.dimensions.width * 0.8F;
         Box box = Box.method_30048((double)g, 0.10000000149011612D, (double)g).offset(this.getX(), this.getEyeY(), this.getZ());
         return this.world.getBlockCollisions(this, box, (blockState, blockPos) -> {
            return blockState.shouldSuffocate(this.world, blockPos);
         }).findAny().isPresent();
      }
   }

   /**
    * Called when a player interacts with this entity.
    * 
    * @param player the player
    * @param hand the hand the player used to interact with this entity
    */
   public ActionResult interact(PlayerEntity player, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean collidesWith(Entity other) {
      return other.isCollidable() && !this.isConnectedThroughVehicle(other);
   }

   public boolean isCollidable() {
      return false;
   }

   public void tickRiding() {
      this.setVelocity(Vec3d.ZERO);
      this.tick();
      if (this.hasVehicle()) {
         this.getVehicle().updatePassengerPosition(this);
      }
   }

   public void updatePassengerPosition(Entity passenger) {
      this.updatePassengerPosition(passenger, Entity::setPosition);
   }

   private void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
      if (this.hasPassenger(passenger)) {
         double d = this.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset();
         positionUpdater.accept(passenger, this.getX(), d, this.getZ());
      }
   }

   @Environment(EnvType.CLIENT)
   public void onPassengerLookAround(Entity passenger) {
   }

   public double getHeightOffset() {
      return 0.0D;
   }

   public double getMountedHeightOffset() {
      return (double)this.dimensions.height * 0.75D;
   }

   public boolean startRiding(Entity entity) {
      return this.startRiding(entity, false);
   }

   @Environment(EnvType.CLIENT)
   public boolean isLiving() {
      return this instanceof LivingEntity;
   }

   public boolean startRiding(Entity entity, boolean force) {
      for(Entity entity2 = entity; entity2.vehicle != null; entity2 = entity2.vehicle) {
         if (entity2.vehicle == this) {
            return false;
         }
      }

      if (!force && (!this.canStartRiding(entity) || !entity.canAddPassenger(this))) {
         return false;
      } else {
         if (this.hasVehicle()) {
            this.stopRiding();
         }

         this.setPose(EntityPose.STANDING);
         this.vehicle = entity;
         this.vehicle.addPassenger(this);
         return true;
      }
   }

   protected boolean canStartRiding(Entity entity) {
      return !this.isSneaking() && this.ridingCooldown <= 0;
   }

   protected boolean wouldPoseNotCollide(EntityPose pose) {
      return this.world.isSpaceEmpty(this, this.calculateBoundsForPose(pose).contract(1.0E-7D));
   }

   public void removeAllPassengers() {
      for(int i = this.passengerList.size() - 1; i >= 0; --i) {
         ((Entity)this.passengerList.get(i)).stopRiding();
      }

   }

   public void method_29239() {
      if (this.vehicle != null) {
         Entity entity = this.vehicle;
         this.vehicle = null;
         entity.removePassenger(this);
      }

   }

   public void stopRiding() {
      this.method_29239();
   }

   protected void addPassenger(Entity passenger) {
      if (passenger.getVehicle() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         if (!this.world.isClient && passenger instanceof PlayerEntity && !(this.getPrimaryPassenger() instanceof PlayerEntity)) {
            this.passengerList.add(0, passenger);
         } else {
            this.passengerList.add(passenger);
         }

      }
   }

   protected void removePassenger(Entity passenger) {
      if (passenger.getVehicle() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         this.passengerList.remove(passenger);
         passenger.ridingCooldown = 60;
      }
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengerList().size() < 1;
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
      this.setPosition(x, y, z);
      this.setRotation(yaw, pitch);
   }

   @Environment(EnvType.CLIENT)
   public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
      this.setHeadYaw(yaw);
   }

   public float getTargetingMargin() {
      return 0.0F;
   }

   public Vec3d getRotationVector() {
      return this.getRotationVector(this.pitch, this.yaw);
   }

   public Vec2f getRotationClient() {
      return new Vec2f(this.pitch, this.yaw);
   }

   @Environment(EnvType.CLIENT)
   public Vec3d getRotationVecClient() {
      return Vec3d.fromPolar(this.getRotationClient());
   }

   public void setInNetherPortal(BlockPos pos) {
      if (this.hasNetherPortalCooldown()) {
         this.resetNetherPortalCooldown();
      } else {
         if (!this.world.isClient && !pos.equals(this.lastNetherPortalPosition)) {
            this.lastNetherPortalPosition = pos.toImmutable();
         }

         this.inNetherPortal = true;
      }
   }

   protected void tickNetherPortal() {
      if (this.world instanceof ServerWorld) {
         int i = this.getMaxNetherPortalTime();
         ServerWorld serverWorld = (ServerWorld)this.world;
         if (this.inNetherPortal) {
            MinecraftServer minecraftServer = serverWorld.getServer();
            RegistryKey<World> registryKey = this.world.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
            ServerWorld serverWorld2 = minecraftServer.getWorld(registryKey);
            if (serverWorld2 != null && minecraftServer.isNetherAllowed() && !this.hasVehicle() && this.netherPortalTime++ >= i) {
               this.world.getProfiler().push("portal");
               this.netherPortalTime = i;
               this.resetNetherPortalCooldown();
               this.moveToWorld(serverWorld2);
               this.world.getProfiler().pop();
            }

            this.inNetherPortal = false;
         } else {
            if (this.netherPortalTime > 0) {
               this.netherPortalTime -= 4;
            }

            if (this.netherPortalTime < 0) {
               this.netherPortalTime = 0;
            }
         }

         this.tickNetherPortalCooldown();
      }
   }

   public int getDefaultNetherPortalCooldown() {
      return 300;
   }

   @Environment(EnvType.CLIENT)
   public void setVelocityClient(double x, double y, double z) {
      this.setVelocity(x, y, z);
   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      switch(status) {
      case 53:
         HoneyBlock.addRegularParticles(this);
      default:
      }
   }

   @Environment(EnvType.CLIENT)
   public void animateDamage() {
   }

   public Iterable<ItemStack> getItemsHand() {
      return EMPTY_STACK_LIST;
   }

   public Iterable<ItemStack> getArmorItems() {
      return EMPTY_STACK_LIST;
   }

   public Iterable<ItemStack> getItemsEquipped() {
      return Iterables.concat(this.getItemsHand(), this.getArmorItems());
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
   }

   public boolean isOnFire() {
      boolean bl = this.world != null && this.world.isClient;
      return !this.isFireImmune() && (this.fireTicks > 0 || bl && this.getFlag(0));
   }

   public boolean hasVehicle() {
      return this.getVehicle() != null;
   }

   public boolean hasPassengers() {
      return !this.getPassengerList().isEmpty();
   }

   public boolean canBeRiddenInWater() {
      return true;
   }

   public void setSneaking(boolean sneaking) {
      this.setFlag(1, sneaking);
   }

   public boolean isSneaking() {
      return this.getFlag(1);
   }

   public boolean bypassesSteppingEffects() {
      return this.isSneaking();
   }

   public boolean bypassesLandingEffects() {
      return this.isSneaking();
   }

   public boolean isSneaky() {
      return this.isSneaking();
   }

   public boolean isDescending() {
      return this.isSneaking();
   }

   /**
    * Returns whether the entity is in a crouching pose.
    * 
    * <p>Compared to {@link #isSneaking()}, it only makes the entity appear
    * crouching and does not bring other effects of sneaking, such as no less
    * obvious name label rendering, no dismounting while riding, etc.
    * 
    * <p>This is used by vanilla for non-player entities to crouch, such as
    * for foxes and cats.
    */
   public boolean isInSneakingPose() {
      return this.getPose() == EntityPose.CROUCHING;
   }

   public boolean isSprinting() {
      return this.getFlag(3);
   }

   public void setSprinting(boolean sprinting) {
      this.setFlag(3, sprinting);
   }

   public boolean isSwimming() {
      return this.getFlag(4);
   }

   public boolean isInSwimmingPose() {
      return this.getPose() == EntityPose.SWIMMING;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldLeaveSwimmingPose() {
      return this.isInSwimmingPose() && !this.isTouchingWater();
   }

   public void setSwimming(boolean swimming) {
      this.setFlag(4, swimming);
   }

   public boolean isGlowing() {
      return this.glowing || this.world.isClient && this.getFlag(6);
   }

   public void setGlowing(boolean glowing) {
      this.glowing = glowing;
      if (!this.world.isClient) {
         this.setFlag(6, this.glowing);
      }

   }

   public boolean isInvisible() {
      return this.getFlag(5);
   }

   @Environment(EnvType.CLIENT)
   public boolean isInvisibleTo(PlayerEntity player) {
      if (player.isSpectator()) {
         return false;
      } else {
         AbstractTeam abstractTeam = this.getScoreboardTeam();
         return abstractTeam != null && player != null && player.getScoreboardTeam() == abstractTeam && abstractTeam.shouldShowFriendlyInvisibles() ? false : this.isInvisible();
      }
   }

   @Nullable
   public AbstractTeam getScoreboardTeam() {
      return this.world.getScoreboard().getPlayerTeam(this.getEntityName());
   }

   public boolean isTeammate(Entity other) {
      return this.isTeamPlayer(other.getScoreboardTeam());
   }

   public boolean isTeamPlayer(AbstractTeam team) {
      return this.getScoreboardTeam() != null ? this.getScoreboardTeam().isEqual(team) : false;
   }

   public void setInvisible(boolean invisible) {
      this.setFlag(5, invisible);
   }

   protected boolean getFlag(int index) {
      return ((Byte)this.dataTracker.get(FLAGS) & 1 << index) != 0;
   }

   protected void setFlag(int index, boolean value) {
      byte b = (Byte)this.dataTracker.get(FLAGS);
      if (value) {
         this.dataTracker.set(FLAGS, (byte)(b | 1 << index));
      } else {
         this.dataTracker.set(FLAGS, (byte)(b & ~(1 << index)));
      }

   }

   public int getMaxAir() {
      return 300;
   }

   public int getAir() {
      return (Integer)this.dataTracker.get(AIR);
   }

   public void setAir(int air) {
      this.dataTracker.set(AIR, air);
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      this.setFireTicks(this.fireTicks + 1);
      if (this.fireTicks == 0) {
         this.setOnFireFor(8);
      }

      this.damage(DamageSource.LIGHTNING_BOLT, 5.0F);
   }

   public void onBubbleColumnSurfaceCollision(boolean drag) {
      Vec3d vec3d = this.getVelocity();
      double e;
      if (drag) {
         e = Math.max(-0.9D, vec3d.y - 0.03D);
      } else {
         e = Math.min(1.8D, vec3d.y + 0.1D);
      }

      this.setVelocity(vec3d.x, e, vec3d.z);
   }

   public void onBubbleColumnCollision(boolean drag) {
      Vec3d vec3d = this.getVelocity();
      double e;
      if (drag) {
         e = Math.max(-0.3D, vec3d.y - 0.03D);
      } else {
         e = Math.min(0.7D, vec3d.y + 0.06D);
      }

      this.setVelocity(vec3d.x, e, vec3d.z);
      this.fallDistance = 0.0F;
   }

   public void onKilledOther(ServerWorld world, LivingEntity other) {
   }

   protected void pushOutOfBlocks(double x, double y, double z) {
      BlockPos blockPos = new BlockPos(x, y, z);
      Vec3d vec3d = new Vec3d(x - (double)blockPos.getX(), y - (double)blockPos.getY(), z - (double)blockPos.getZ());
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      Direction direction = Direction.UP;
      double d = Double.MAX_VALUE;
      Direction[] var13 = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
      int var14 = var13.length;

      for(int var15 = 0; var15 < var14; ++var15) {
         Direction direction2 = var13[var15];
         mutable.set(blockPos, direction2);
         if (!this.world.getBlockState(mutable).isFullCube(this.world, mutable)) {
            double e = vec3d.getComponentAlongAxis(direction2.getAxis());
            double f = direction2.getDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - e : e;
            if (f < d) {
               d = f;
               direction = direction2;
            }
         }
      }

      float g = this.random.nextFloat() * 0.2F + 0.1F;
      float h = (float)direction.getDirection().offset();
      Vec3d vec3d2 = this.getVelocity().multiply(0.75D);
      if (direction.getAxis() == Direction.Axis.X) {
         this.setVelocity((double)(h * g), vec3d2.y, vec3d2.z);
      } else if (direction.getAxis() == Direction.Axis.Y) {
         this.setVelocity(vec3d2.x, (double)(h * g), vec3d2.z);
      } else if (direction.getAxis() == Direction.Axis.Z) {
         this.setVelocity(vec3d2.x, vec3d2.y, (double)(h * g));
      }

   }

   public void slowMovement(BlockState state, Vec3d multiplier) {
      this.fallDistance = 0.0F;
      this.movementMultiplier = multiplier;
   }

   private static Text removeClickEvents(Text textComponent) {
      MutableText mutableText = textComponent.copy().setStyle(textComponent.getStyle().withClickEvent((ClickEvent)null));
      Iterator var2 = textComponent.getSiblings().iterator();

      while(var2.hasNext()) {
         Text text = (Text)var2.next();
         mutableText.append(removeClickEvents(text));
      }

      return mutableText;
   }

   public Text getName() {
      Text text = this.getCustomName();
      return text != null ? removeClickEvents(text) : this.getDefaultName();
   }

   protected Text getDefaultName() {
      return this.type.getName();
   }

   public boolean isPartOf(Entity entity) {
      return this == entity;
   }

   public float getHeadYaw() {
      return 0.0F;
   }

   public void setHeadYaw(float headYaw) {
   }

   public void setBodyYaw(float bodyYaw) {
   }

   public boolean isAttackable() {
      return true;
   }

   public boolean handleAttack(Entity attacker) {
      return false;
   }

   public String toString() {
      return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.entityId, this.world == null ? "~NULL~" : this.world.toString(), this.getX(), this.getY(), this.getZ());
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return this.invulnerable && damageSource != DamageSource.OUT_OF_WORLD && !damageSource.isSourceCreativePlayer();
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public void setInvulnerable(boolean invulnerable) {
      this.invulnerable = invulnerable;
   }

   public void copyPositionAndRotation(Entity entity) {
      this.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.yaw, entity.pitch);
   }

   public void copyFrom(Entity original) {
      NbtCompound nbtCompound = original.writeNbt(new NbtCompound());
      nbtCompound.remove("Dimension");
      this.readNbt(nbtCompound);
      this.netherPortalCooldown = original.netherPortalCooldown;
      this.lastNetherPortalPosition = original.lastNetherPortalPosition;
   }

   /**
    * Moves this entity to another world.
    * 
    * <p>Note all entities except server player entities are completely recreated at the destination.
    * 
    * @return the entity in the other world
    */
   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      if (this.world instanceof ServerWorld && !this.removed) {
         this.world.getProfiler().push("changeDimension");
         this.detach();
         this.world.getProfiler().push("reposition");
         TeleportTarget teleportTarget = this.getTeleportTarget(destination);
         if (teleportTarget == null) {
            return null;
         } else {
            this.world.getProfiler().swap("reloading");
            Entity entity = this.getType().create(destination);
            if (entity != null) {
               entity.copyFrom(this);
               entity.refreshPositionAndAngles(teleportTarget.position.x, teleportTarget.position.y, teleportTarget.position.z, teleportTarget.yaw, entity.pitch);
               entity.setVelocity(teleportTarget.velocity);
               destination.onDimensionChanged(entity);
               if (destination.getRegistryKey() == World.END) {
                  ServerWorld.createEndSpawnPlatform(destination);
               }
            }

            this.method_30076();
            this.world.getProfiler().pop();
            ((ServerWorld)this.world).resetIdleTimeout();
            destination.resetIdleTimeout();
            this.world.getProfiler().pop();
            return entity;
         }
      } else {
         return null;
      }
   }

   protected void method_30076() {
      this.removed = true;
   }

   /**
    * Determines a {@link TeleportTarget} for the entity
    * based on its current and destination worlds, plus
    * any portals that may be present.
    */
   @Nullable
   protected TeleportTarget getTeleportTarget(ServerWorld destination) {
      boolean bl = this.world.getRegistryKey() == World.END && destination.getRegistryKey() == World.OVERWORLD;
      boolean bl2 = destination.getRegistryKey() == World.END;
      if (!bl && !bl2) {
         boolean bl3 = destination.getRegistryKey() == World.NETHER;
         if (this.world.getRegistryKey() != World.NETHER && !bl3) {
            return null;
         } else {
            WorldBorder worldBorder = destination.getWorldBorder();
            double d = Math.max(-2.9999872E7D, worldBorder.getBoundWest() + 16.0D);
            double e = Math.max(-2.9999872E7D, worldBorder.getBoundNorth() + 16.0D);
            double f = Math.min(2.9999872E7D, worldBorder.getBoundEast() - 16.0D);
            double g = Math.min(2.9999872E7D, worldBorder.getBoundSouth() - 16.0D);
            double h = DimensionType.method_31109(this.world.getDimension(), destination.getDimension());
            BlockPos blockPos3 = new BlockPos(MathHelper.clamp(this.getX() * h, d, f), this.getY(), MathHelper.clamp(this.getZ() * h, e, g));
            return (TeleportTarget)this.method_30330(destination, blockPos3, bl3).map((rectangle) -> {
               BlockState blockState = this.world.getBlockState(this.lastNetherPortalPosition);
               Direction.Axis axis2;
               Vec3d vec3d2;
               if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                  axis2 = (Direction.Axis)blockState.get(Properties.HORIZONTAL_AXIS);
                  PortalUtil.Rectangle rectangle2 = PortalUtil.getLargestRectangle(this.lastNetherPortalPosition, axis2, 21, Direction.Axis.Y, 21, (blockPos) -> {
                     return this.world.getBlockState(blockPos) == blockState;
                  });
                  vec3d2 = this.method_30633(axis2, rectangle2);
               } else {
                  axis2 = Direction.Axis.X;
                  vec3d2 = new Vec3d(0.5D, 0.0D, 0.0D);
               }

               return AreaHelper.method_30484(destination, rectangle, axis2, vec3d2, this.getDimensions(this.getPose()), this.getVelocity(), this.yaw, this.pitch);
            }).orElse((Object)null);
         }
      } else {
         BlockPos blockPos2;
         if (bl2) {
            blockPos2 = ServerWorld.END_SPAWN_POS;
         } else {
            blockPos2 = destination.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, destination.getSpawnPos());
         }

         return new TeleportTarget(new Vec3d((double)blockPos2.getX() + 0.5D, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5D), this.getVelocity(), this.yaw, this.pitch);
      }
   }

   protected Vec3d method_30633(Direction.Axis axis, PortalUtil.Rectangle rectangle) {
      return AreaHelper.method_30494(rectangle, axis, this.getPos(), this.getDimensions(this.getPose()));
   }

   protected Optional<PortalUtil.Rectangle> method_30330(ServerWorld serverWorld, BlockPos blockPos, boolean bl) {
      return serverWorld.getPortalForcer().method_30483(blockPos, bl);
   }

   public boolean canUsePortals() {
      return true;
   }

   public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
      return max;
   }

   public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
      return true;
   }

   public int getSafeFallDistance() {
      return 3;
   }

   public boolean canAvoidTraps() {
      return false;
   }

   public void populateCrashReport(CrashReportSection section) {
      section.add("Entity Type", () -> {
         return EntityType.getId(this.getType()) + " (" + this.getClass().getCanonicalName() + ")";
      });
      section.add("Entity ID", (Object)this.entityId);
      section.add("Entity Name", () -> {
         return this.getName().getString();
      });
      section.add("Entity's Exact location", (Object)String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
      section.add("Entity's Block location", (Object)CrashReportSection.createPositionString(MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ())));
      Vec3d vec3d = this.getVelocity();
      section.add("Entity's Momentum", (Object)String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vec3d.x, vec3d.y, vec3d.z));
      section.add("Entity's Passengers", () -> {
         return this.getPassengerList().toString();
      });
      section.add("Entity's Vehicle", () -> {
         return this.getVehicle().toString();
      });
   }

   @Environment(EnvType.CLIENT)
   public boolean doesRenderOnFire() {
      return this.isOnFire() && !this.isSpectator();
   }

   public void setUuid(UUID uuid) {
      this.uuid = uuid;
      this.uuidString = this.uuid.toString();
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getUuidAsString() {
      return this.uuidString;
   }

   public String getEntityName() {
      return this.uuidString;
   }

   public boolean isPushedByFluids() {
      return true;
   }

   @Environment(EnvType.CLIENT)
   public static double getRenderDistanceMultiplier() {
      return renderDistanceMultiplier;
   }

   @Environment(EnvType.CLIENT)
   public static void setRenderDistanceMultiplier(double value) {
      renderDistanceMultiplier = value;
   }

   public Text getDisplayName() {
      return Team.decorateName(this.getScoreboardTeam(), this.getName()).styled((style) -> {
         return style.withHoverEvent(this.getHoverEvent()).withInsertion(this.getUuidAsString());
      });
   }

   public void setCustomName(@Nullable Text name) {
      this.dataTracker.set(CUSTOM_NAME, Optional.ofNullable(name));
   }

   @Nullable
   public Text getCustomName() {
      return (Text)((Optional)this.dataTracker.get(CUSTOM_NAME)).orElse((Object)null);
   }

   public boolean hasCustomName() {
      return ((Optional)this.dataTracker.get(CUSTOM_NAME)).isPresent();
   }

   public void setCustomNameVisible(boolean visible) {
      this.dataTracker.set(NAME_VISIBLE, visible);
   }

   public boolean isCustomNameVisible() {
      return (Boolean)this.dataTracker.get(NAME_VISIBLE);
   }

   public final void teleport(double destX, double destY, double destZ) {
      if (this.world instanceof ServerWorld) {
         ChunkPos chunkPos = new ChunkPos(new BlockPos(destX, destY, destZ));
         ((ServerWorld)this.world).getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 0, this.getEntityId());
         this.world.getChunk(chunkPos.x, chunkPos.z);
         this.requestTeleport(destX, destY, destZ);
      }
   }

   public void requestTeleport(double destX, double destY, double destZ) {
      if (this.world instanceof ServerWorld) {
         ServerWorld serverWorld = (ServerWorld)this.world;
         this.refreshPositionAndAngles(destX, destY, destZ, this.yaw, this.pitch);
         this.streamPassengersRecursively().forEach((entity) -> {
            serverWorld.checkEntityChunkPos(entity);
            entity.teleportRequested = true;
            Iterator var2 = entity.passengerList.iterator();

            while(var2.hasNext()) {
               Entity entity2 = (Entity)var2.next();
               entity.updatePassengerPosition(entity2, Entity::refreshPositionAfterTeleport);
            }

         });
      }
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRenderName() {
      return this.isCustomNameVisible();
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (POSE.equals(data)) {
         this.calculateDimensions();
      }

   }

   public void calculateDimensions() {
      EntityDimensions entityDimensions = this.dimensions;
      EntityPose entityPose = this.getPose();
      EntityDimensions entityDimensions2 = this.getDimensions(entityPose);
      this.dimensions = entityDimensions2;
      this.standingEyeHeight = this.getEyeHeight(entityPose, entityDimensions2);
      if (entityDimensions2.width < entityDimensions.width) {
         double d = (double)entityDimensions2.width / 2.0D;
         this.setBoundingBox(new Box(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + (double)entityDimensions2.height, this.getZ() + d));
      } else {
         Box box = this.getBoundingBox();
         this.setBoundingBox(new Box(box.minX, box.minY, box.minZ, box.minX + (double)entityDimensions2.width, box.minY + (double)entityDimensions2.height, box.minZ + (double)entityDimensions2.width));
         if (entityDimensions2.width > entityDimensions.width && !this.firstUpdate && !this.world.isClient) {
            float f = entityDimensions.width - entityDimensions2.width;
            this.move(MovementType.SELF, new Vec3d((double)f, 0.0D, (double)f));
         }

      }
   }

   public Direction getHorizontalFacing() {
      return Direction.fromRotation((double)this.yaw);
   }

   public Direction getMovementDirection() {
      return this.getHorizontalFacing();
   }

   protected HoverEvent getHoverEvent() {
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(this.getType(), this.getUuid(), this.getName()));
   }

   public boolean canBeSpectated(ServerPlayerEntity spectator) {
      return true;
   }

   public Box getBoundingBox() {
      return this.entityBounds;
   }

   @Environment(EnvType.CLIENT)
   public Box getVisibilityBoundingBox() {
      return this.getBoundingBox();
   }

   protected Box calculateBoundsForPose(EntityPose pos) {
      EntityDimensions entityDimensions = this.getDimensions(pos);
      float f = entityDimensions.width / 2.0F;
      Vec3d vec3d = new Vec3d(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
      Vec3d vec3d2 = new Vec3d(this.getX() + (double)f, this.getY() + (double)entityDimensions.height, this.getZ() + (double)f);
      return new Box(vec3d, vec3d2);
   }

   public void setBoundingBox(Box boundingBox) {
      this.entityBounds = boundingBox;
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return dimensions.height * 0.85F;
   }

   @Environment(EnvType.CLIENT)
   public float getEyeHeight(EntityPose pose) {
      return this.getEyeHeight(pose, this.getDimensions(pose));
   }

   public final float getStandingEyeHeight() {
      return this.standingEyeHeight;
   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_29919() {
      return new Vec3d(0.0D, (double)this.getStandingEyeHeight(), (double)(this.getWidth() * 0.4F));
   }

   public boolean equip(int slot, ItemStack item) {
      return false;
   }

   public void sendSystemMessage(Text message, UUID sender) {
   }

   public World getEntityWorld() {
      return this.world;
   }

   @Nullable
   public MinecraftServer getServer() {
      return this.world.getServer();
   }

   public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean isImmuneToExplosion() {
      return false;
   }

   public void dealDamage(LivingEntity attacker, Entity target) {
      if (target instanceof LivingEntity) {
         EnchantmentHelper.onUserDamaged((LivingEntity)target, attacker);
      }

      EnchantmentHelper.onTargetDamaged(attacker, target);
   }

   public void onStartedTrackingBy(ServerPlayerEntity player) {
   }

   public void onStoppedTrackingBy(ServerPlayerEntity player) {
   }

   public float applyRotation(BlockRotation rotation) {
      float f = MathHelper.wrapDegrees(this.yaw);
      switch(rotation) {
      case CLOCKWISE_180:
         return f + 180.0F;
      case COUNTERCLOCKWISE_90:
         return f + 270.0F;
      case CLOCKWISE_90:
         return f + 90.0F;
      default:
         return f;
      }
   }

   public float applyMirror(BlockMirror mirror) {
      float f = MathHelper.wrapDegrees(this.yaw);
      switch(mirror) {
      case LEFT_RIGHT:
         return -f;
      case FRONT_BACK:
         return 180.0F - f;
      default:
         return f;
      }
   }

   public boolean entityDataRequiresOperator() {
      return false;
   }

   public boolean teleportRequested() {
      boolean bl = this.teleportRequested;
      this.teleportRequested = false;
      return bl;
   }

   public boolean isChunkPosUpdateRequested() {
      boolean bl = this.chunkPosUpdateRequested;
      this.chunkPosUpdateRequested = false;
      return bl;
   }

   @Nullable
   public Entity getPrimaryPassenger() {
      return null;
   }

   public List<Entity> getPassengerList() {
      return (List)(this.passengerList.isEmpty() ? Collections.emptyList() : Lists.newArrayList((Iterable)this.passengerList));
   }

   public boolean hasPassenger(Entity passenger) {
      Iterator var2 = this.getPassengerList().iterator();

      Entity entity;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         entity = (Entity)var2.next();
      } while(!entity.equals(passenger));

      return true;
   }

   public boolean hasPassengerType(Class<? extends Entity> clazz) {
      Iterator var2 = this.getPassengerList().iterator();

      Entity entity;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         entity = (Entity)var2.next();
      } while(!clazz.isAssignableFrom(entity.getClass()));

      return true;
   }

   public Collection<Entity> getPassengersDeep() {
      Set<Entity> set = Sets.newHashSet();
      Iterator var2 = this.getPassengerList().iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         set.add(entity);
         entity.collectPassengers(false, set);
      }

      return set;
   }

   public Stream<Entity> streamPassengersRecursively() {
      return Stream.concat(Stream.of(this), this.passengerList.stream().flatMap(Entity::streamPassengersRecursively));
   }

   public boolean hasPlayerRider() {
      Set<Entity> set = Sets.newHashSet();
      this.collectPassengers(true, set);
      return set.size() == 1;
   }

   private void collectPassengers(boolean playersOnly, Set<Entity> output) {
      Entity entity;
      for(Iterator var3 = this.getPassengerList().iterator(); var3.hasNext(); entity.collectPassengers(playersOnly, output)) {
         entity = (Entity)var3.next();
         if (!playersOnly || ServerPlayerEntity.class.isAssignableFrom(entity.getClass())) {
            output.add(entity);
         }
      }

   }

   /**
    * Gets the lowest entity this entity is riding.
    */
   public Entity getRootVehicle() {
      Entity entity;
      for(entity = this; entity.hasVehicle(); entity = entity.getVehicle()) {
      }

      return entity;
   }

   /**
    * Checks if this entity and another entity share the same root vehicle.
    * 
    * @param entity the other entity
    */
   public boolean isConnectedThroughVehicle(Entity entity) {
      return this.getRootVehicle() == entity.getRootVehicle();
   }

   @Environment(EnvType.CLIENT)
   public boolean hasPassengerDeep(Entity passenger) {
      Iterator var2 = this.getPassengerList().iterator();

      Entity entity;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         entity = (Entity)var2.next();
         if (entity.equals(passenger)) {
            return true;
         }
      } while(!entity.hasPassengerDeep(passenger));

      return true;
   }

   public boolean isLogicalSideForUpdatingMovement() {
      Entity entity = this.getPrimaryPassenger();
      if (entity instanceof PlayerEntity) {
         return ((PlayerEntity)entity).isMainPlayer();
      } else {
         return !this.world.isClient;
      }
   }

   protected static Vec3d getPassengerDismountOffset(double vehicleWidth, double passengerWidth, float passengerYaw) {
      double d = (vehicleWidth + passengerWidth + 9.999999747378752E-6D) / 2.0D;
      float f = -MathHelper.sin(passengerYaw * 0.017453292F);
      float g = MathHelper.cos(passengerYaw * 0.017453292F);
      float h = Math.max(Math.abs(f), Math.abs(g));
      return new Vec3d((double)f * d / (double)h, 0.0D, (double)g * d / (double)h);
   }

   public Vec3d updatePassengerForDismount(LivingEntity passenger) {
      return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   @Nullable
   public Entity getVehicle() {
      return this.vehicle;
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.NORMAL;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.NEUTRAL;
   }

   protected int getBurningDuration() {
      return 1;
   }

   /**
    * Creates a command source which represents this entity.
    */
   public ServerCommandSource getCommandSource() {
      return new ServerCommandSource(this, this.getPos(), this.getRotationClient(), this.world instanceof ServerWorld ? (ServerWorld)this.world : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.world.getServer(), this);
   }

   protected int getPermissionLevel() {
      return 0;
   }

   public boolean hasPermissionLevel(int permissionLevel) {
      return this.getPermissionLevel() >= permissionLevel;
   }

   public boolean shouldReceiveFeedback() {
      return this.world.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK);
   }

   public boolean shouldTrackOutput() {
      return true;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return true;
   }

   public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
      Vec3d vec3d = anchorPoint.positionAt(this);
      double d = target.x - vec3d.x;
      double e = target.y - vec3d.y;
      double f = target.z - vec3d.z;
      double g = (double)MathHelper.sqrt(d * d + f * f);
      this.pitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875D)));
      this.yaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875D) - 90.0F);
      this.setHeadYaw(this.yaw);
      this.prevPitch = this.pitch;
      this.prevYaw = this.yaw;
   }

   public boolean updateMovementInFluid(Tag<Fluid> tag, double d) {
      Box box = this.getBoundingBox().contract(0.001D);
      int i = MathHelper.floor(box.minX);
      int j = MathHelper.ceil(box.maxX);
      int k = MathHelper.floor(box.minY);
      int l = MathHelper.ceil(box.maxY);
      int m = MathHelper.floor(box.minZ);
      int n = MathHelper.ceil(box.maxZ);
      if (!this.world.isRegionLoaded(i, k, m, j, l, n)) {
         return false;
      } else {
         double e = 0.0D;
         boolean bl = this.isPushedByFluids();
         boolean bl2 = false;
         Vec3d vec3d = Vec3d.ZERO;
         int o = 0;
         BlockPos.Mutable mutable = new BlockPos.Mutable();

         for(int p = i; p < j; ++p) {
            for(int q = k; q < l; ++q) {
               for(int r = m; r < n; ++r) {
                  mutable.set(p, q, r);
                  FluidState fluidState = this.world.getFluidState(mutable);
                  if (fluidState.isIn(tag)) {
                     double f = (double)((float)q + fluidState.getHeight(this.world, mutable));
                     if (f >= box.minY) {
                        bl2 = true;
                        e = Math.max(f - box.minY, e);
                        if (bl) {
                           Vec3d vec3d2 = fluidState.getVelocity(this.world, mutable);
                           if (e < 0.4D) {
                              vec3d2 = vec3d2.multiply(e);
                           }

                           vec3d = vec3d.add(vec3d2);
                           ++o;
                        }
                     }
                  }
               }
            }
         }

         if (vec3d.length() > 0.0D) {
            if (o > 0) {
               vec3d = vec3d.multiply(1.0D / (double)o);
            }

            if (!(this instanceof PlayerEntity)) {
               vec3d = vec3d.normalize();
            }

            Vec3d vec3d3 = this.getVelocity();
            vec3d = vec3d.multiply(d * 1.0D);
            double g = 0.003D;
            if (Math.abs(vec3d3.x) < 0.003D && Math.abs(vec3d3.z) < 0.003D && vec3d.length() < 0.0045000000000000005D) {
               vec3d = vec3d.normalize().multiply(0.0045000000000000005D);
            }

            this.setVelocity(this.getVelocity().add(vec3d));
         }

         this.fluidHeight.put(tag, e);
         return bl2;
      }
   }

   public double getFluidHeight(Tag<Fluid> fluid) {
      return this.fluidHeight.getDouble(fluid);
   }

   public double method_29241() {
      return (double)this.getStandingEyeHeight() < 0.4D ? 0.0D : 0.4D;
   }

   public final float getWidth() {
      return this.dimensions.width;
   }

   public final float getHeight() {
      return this.dimensions.height;
   }

   public abstract Packet<?> createSpawnPacket();

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.type.getDimensions();
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public Vec3d getVelocity() {
      return this.velocity;
   }

   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }

   public void setVelocity(double x, double y, double z) {
      this.setVelocity(new Vec3d(x, y, z));
   }

   public final double getX() {
      return this.pos.x;
   }

   public double offsetX(double widthScale) {
      return this.pos.x + (double)this.getWidth() * widthScale;
   }

   public double getParticleX(double widthScale) {
      return this.offsetX((2.0D * this.random.nextDouble() - 1.0D) * widthScale);
   }

   public final double getY() {
      return this.pos.y;
   }

   public double getBodyY(double heightScale) {
      return this.pos.y + (double)this.getHeight() * heightScale;
   }

   public double getRandomBodyY() {
      return this.getBodyY(this.random.nextDouble());
   }

   public double getEyeY() {
      return this.pos.y + (double)this.standingEyeHeight;
   }

   public final double getZ() {
      return this.pos.z;
   }

   public double offsetZ(double widthScale) {
      return this.pos.z + (double)this.getWidth() * widthScale;
   }

   public double getParticleZ(double widthScale) {
      return this.offsetZ((2.0D * this.random.nextDouble() - 1.0D) * widthScale);
   }

   public void setPos(double x, double y, double z) {
      if (this.pos.x != x || this.pos.y != y || this.pos.z != z) {
         this.pos = new Vec3d(x, y, z);
         int i = MathHelper.floor(x);
         int j = MathHelper.floor(y);
         int k = MathHelper.floor(z);
         if (i != this.blockPos.getX() || j != this.blockPos.getY() || k != this.blockPos.getZ()) {
            this.blockPos = new BlockPos(i, j, k);
         }

         this.chunkPosUpdateRequested = true;
      }

   }

   public void checkDespawn() {
   }

   @Environment(EnvType.CLIENT)
   public Vec3d method_30951(float f) {
      return this.method_30950(f).add(0.0D, (double)this.standingEyeHeight * 0.7D, 0.0D);
   }

   static {
      FLAGS = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BYTE);
      AIR = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.INTEGER);
      CUSTOM_NAME = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.OPTIONAL_TEXT_COMPONENT);
      NAME_VISIBLE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
      SILENT = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
      NO_GRAVITY = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
      POSE = DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.ENTITY_POSE);
   }

   @FunctionalInterface
   public interface PositionUpdater {
      void accept(Entity entity, double x, double y, double z);
   }
}
