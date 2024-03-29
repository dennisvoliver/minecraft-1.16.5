package net.minecraft.entity.mob;

import java.util.Random;
import java.util.UUID;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.IntRange;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ZombifiedPiglinEntity extends ZombieEntity implements Angerable {
   private static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
   private static final EntityAttributeModifier ATTACKING_SPEED_BOOST;
   private static final IntRange field_25382;
   private int angrySoundDelay;
   private static final IntRange ANGER_TIME_RANGE;
   private int angerTime;
   private UUID targetUuid;
   private static final IntRange field_25609;
   private int field_25608;

   public ZombifiedPiglinEntity(EntityType<? extends ZombifiedPiglinEntity> entityType, World world) {
      super(entityType, world);
      this.setPathfindingPenalty(PathNodeType.LAVA, 8.0F);
   }

   public void setAngryAt(@Nullable UUID uuid) {
      this.targetUuid = uuid;
   }

   public double getHeightOffset() {
      return this.isBaby() ? -0.05D : -0.45D;
   }

   protected void initCustomGoals() {
      this.goalSelector.add(2, new ZombieAttackGoal(this, 1.0D, false));
      this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0D));
      this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
      this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
      this.targetSelector.add(3, new UniversalAngerGoal(this, true));
   }

   public static DefaultAttributeContainer.Builder createZombifiedPiglinAttributes() {
      return ZombieEntity.createZombieAttributes().add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23000000417232513D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D);
   }

   protected boolean canConvertInWater() {
      return false;
   }

   protected void mobTick() {
      EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (this.hasAngerTime()) {
         if (!this.isBaby() && !entityAttributeInstance.hasModifier(ATTACKING_SPEED_BOOST)) {
            entityAttributeInstance.addTemporaryModifier(ATTACKING_SPEED_BOOST);
         }

         this.method_30080();
      } else if (entityAttributeInstance.hasModifier(ATTACKING_SPEED_BOOST)) {
         entityAttributeInstance.removeModifier(ATTACKING_SPEED_BOOST);
      }

      this.tickAngerLogic((ServerWorld)this.world, true);
      if (this.getTarget() != null) {
         this.method_29941();
      }

      if (this.hasAngerTime()) {
         this.playerHitTimer = this.age;
      }

      super.mobTick();
   }

   private void method_30080() {
      if (this.angrySoundDelay > 0) {
         --this.angrySoundDelay;
         if (this.angrySoundDelay == 0) {
            this.method_29533();
         }
      }

   }

   private void method_29941() {
      if (this.field_25608 > 0) {
         --this.field_25608;
      } else {
         if (this.getVisibilityCache().canSee(this.getTarget())) {
            this.method_29942();
         }

         this.field_25608 = field_25609.choose(this.random);
      }
   }

   private void method_29942() {
      double d = this.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
      Box box = Box.method_29968(this.getPos()).expand(d, 10.0D, d);
      this.world.getEntitiesIncludingUngeneratedChunks(ZombifiedPiglinEntity.class, box).stream().filter((zombifiedPiglinEntity) -> {
         return zombifiedPiglinEntity != this;
      }).filter((zombifiedPiglinEntity) -> {
         return zombifiedPiglinEntity.getTarget() == null;
      }).filter((zombifiedPiglinEntity) -> {
         return !zombifiedPiglinEntity.isTeammate(this.getTarget());
      }).forEach((zombifiedPiglinEntity) -> {
         zombifiedPiglinEntity.setTarget(this.getTarget());
      });
   }

   private void method_29533() {
      this.playSound(SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getSoundPitch() * 1.8F);
   }

   public void setTarget(@Nullable LivingEntity target) {
      if (this.getTarget() == null && target != null) {
         this.angrySoundDelay = field_25382.choose(this.random);
         this.field_25608 = field_25609.choose(this.random);
      }

      if (target instanceof PlayerEntity) {
         this.setAttacking((PlayerEntity)target);
      }

      super.setTarget(target);
   }

   public void chooseRandomAngerTime() {
      this.setAngerTime(ANGER_TIME_RANGE.choose(this.random));
   }

   public static boolean canSpawn(EntityType<ZombifiedPiglinEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getDifficulty() != Difficulty.PEACEFUL && world.getBlockState(pos.down()).getBlock() != Blocks.NETHER_WART_BLOCK;
   }

   public boolean canSpawn(WorldView world) {
      return world.intersectsEntities(this) && !world.containsFluid(this.getBoundingBox());
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      this.writeAngerToNbt(nbt);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.angerFromTag((ServerWorld)this.world, nbt);
   }

   public void setAngerTime(int ticks) {
      this.angerTime = ticks;
   }

   public int getAngerTime() {
      return this.angerTime;
   }

   public boolean damage(DamageSource source, float amount) {
      return this.isInvulnerableTo(source) ? false : super.damage(source, amount);
   }

   protected SoundEvent getAmbientSound() {
      return this.hasAngerTime() ? SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
   }

   protected void initEquipment(LocalDifficulty difficulty) {
      this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
   }

   protected ItemStack getSkull() {
      return ItemStack.EMPTY;
   }

   protected void initAttributes() {
      this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(0.0D);
   }

   public UUID getAngryAt() {
      return this.targetUuid;
   }

   public boolean isAngryAt(PlayerEntity player) {
      return this.shouldAngerAt(player);
   }

   static {
      ATTACKING_SPEED_BOOST = new EntityAttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.05D, EntityAttributeModifier.Operation.ADDITION);
      field_25382 = Durations.betweenSeconds(0, 1);
      ANGER_TIME_RANGE = Durations.betweenSeconds(20, 39);
      field_25609 = Durations.betweenSeconds(4, 6);
   }
}
