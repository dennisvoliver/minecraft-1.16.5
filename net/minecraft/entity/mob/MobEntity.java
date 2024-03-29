package net.minecraft.entity.mob;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class MobEntity extends LivingEntity {
   private static final TrackedData<Byte> MOB_FLAGS;
   public int ambientSoundChance;
   protected int experiencePoints;
   protected LookControl lookControl;
   protected MoveControl moveControl;
   protected JumpControl jumpControl;
   private final BodyControl bodyControl;
   protected EntityNavigation navigation;
   protected final GoalSelector goalSelector;
   protected final GoalSelector targetSelector;
   private LivingEntity target;
   private final MobVisibilityCache visibilityCache;
   private final DefaultedList<ItemStack> handItems;
   protected final float[] handDropChances;
   private final DefaultedList<ItemStack> armorItems;
   protected final float[] armorDropChances;
   private boolean pickUpLoot;
   private boolean persistent;
   private final Map<PathNodeType, Float> pathfindingPenalties;
   private Identifier lootTable;
   private long lootTableSeed;
   @Nullable
   private Entity holdingEntity;
   private int holdingEntityId;
   @Nullable
   private NbtCompound leashNbt;
   private BlockPos positionTarget;
   private float positionTargetRange;

   protected MobEntity(EntityType<? extends MobEntity> entityType, World world) {
      super(entityType, world);
      this.handItems = DefaultedList.ofSize(2, ItemStack.EMPTY);
      this.handDropChances = new float[2];
      this.armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
      this.armorDropChances = new float[4];
      this.pathfindingPenalties = Maps.newEnumMap(PathNodeType.class);
      this.positionTarget = BlockPos.ORIGIN;
      this.positionTargetRange = -1.0F;
      this.goalSelector = new GoalSelector(world.getProfilerSupplier());
      this.targetSelector = new GoalSelector(world.getProfilerSupplier());
      this.lookControl = new LookControl(this);
      this.moveControl = new MoveControl(this);
      this.jumpControl = new JumpControl(this);
      this.bodyControl = this.createBodyControl();
      this.navigation = this.createNavigation(world);
      this.visibilityCache = new MobVisibilityCache(this);
      Arrays.fill(this.armorDropChances, 0.085F);
      Arrays.fill(this.handDropChances, 0.085F);
      if (world != null && !world.isClient) {
         this.initGoals();
      }

   }

   protected void initGoals() {
   }

   public static DefaultAttributeContainer.Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
   }

   protected EntityNavigation createNavigation(World world) {
      return new MobNavigation(this, world);
   }

   /**
    * When true, causes this entity to take over pathfinding for its controlling passenger.
    */
   protected boolean movesIndependently() {
      return false;
   }

   public float getPathfindingPenalty(PathNodeType nodeType) {
      MobEntity mobEntity2;
      if (this.getVehicle() instanceof MobEntity && ((MobEntity)this.getVehicle()).movesIndependently()) {
         mobEntity2 = (MobEntity)this.getVehicle();
      } else {
         mobEntity2 = this;
      }

      Float float_ = (Float)mobEntity2.pathfindingPenalties.get(nodeType);
      return float_ == null ? nodeType.getDefaultPenalty() : float_;
   }

   public void setPathfindingPenalty(PathNodeType nodeType, float penalty) {
      this.pathfindingPenalties.put(nodeType, penalty);
   }

   public boolean method_29244(PathNodeType pathNodeType) {
      return pathNodeType != PathNodeType.DANGER_FIRE && pathNodeType != PathNodeType.DANGER_CACTUS && pathNodeType != PathNodeType.DANGER_OTHER && pathNodeType != PathNodeType.WALKABLE_DOOR;
   }

   protected BodyControl createBodyControl() {
      return new BodyControl(this);
   }

   public LookControl getLookControl() {
      return this.lookControl;
   }

   public MoveControl getMoveControl() {
      if (this.hasVehicle() && this.getVehicle() instanceof MobEntity) {
         MobEntity mobEntity = (MobEntity)this.getVehicle();
         return mobEntity.getMoveControl();
      } else {
         return this.moveControl;
      }
   }

   public JumpControl getJumpControl() {
      return this.jumpControl;
   }

   public EntityNavigation getNavigation() {
      if (this.hasVehicle() && this.getVehicle() instanceof MobEntity) {
         MobEntity mobEntity = (MobEntity)this.getVehicle();
         return mobEntity.getNavigation();
      } else {
         return this.navigation;
      }
   }

   public MobVisibilityCache getVisibilityCache() {
      return this.visibilityCache;
   }

   @Nullable
   public LivingEntity getTarget() {
      return this.target;
   }

   public void setTarget(@Nullable LivingEntity target) {
      this.target = target;
   }

   public boolean canTarget(EntityType<?> type) {
      return type != EntityType.GHAST;
   }

   public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
      return false;
   }

   public void onEatingGrass() {
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(MOB_FLAGS, (byte)0);
   }

   public int getMinAmbientSoundDelay() {
      return 80;
   }

   public void playAmbientSound() {
      SoundEvent soundEvent = this.getAmbientSound();
      if (soundEvent != null) {
         this.playSound(soundEvent, this.getSoundVolume(), this.getSoundPitch());
      }

   }

   public void baseTick() {
      super.baseTick();
      this.world.getProfiler().push("mobBaseTick");
      if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundChance++) {
         this.resetSoundDelay();
         this.playAmbientSound();
      }

      this.world.getProfiler().pop();
   }

   protected void playHurtSound(DamageSource source) {
      this.resetSoundDelay();
      super.playHurtSound(source);
   }

   private void resetSoundDelay() {
      this.ambientSoundChance = -this.getMinAmbientSoundDelay();
   }

   protected int getXpToDrop(PlayerEntity player) {
      if (this.experiencePoints > 0) {
         int i = this.experiencePoints;

         int k;
         for(k = 0; k < this.armorItems.size(); ++k) {
            if (!((ItemStack)this.armorItems.get(k)).isEmpty() && this.armorDropChances[k] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         for(k = 0; k < this.handItems.size(); ++k) {
            if (!((ItemStack)this.handItems.get(k)).isEmpty() && this.handDropChances[k] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         return i;
      } else {
         return this.experiencePoints;
      }
   }

   public void playSpawnEffects() {
      if (this.world.isClient) {
         for(int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            double g = 10.0D;
            this.world.addParticle(ParticleTypes.POOF, this.offsetX(1.0D) - d * 10.0D, this.getRandomBodyY() - e * 10.0D, this.getParticleZ(1.0D) - f * 10.0D, d, e, f);
         }
      } else {
         this.world.sendEntityStatus(this, (byte)20);
      }

   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 20) {
         this.playSpawnEffects();
      } else {
         super.handleStatus(status);
      }

   }

   public void tick() {
      super.tick();
      if (!this.world.isClient) {
         this.updateLeash();
         if (this.age % 5 == 0) {
            this.updateGoalControls();
         }
      }

   }

   protected void updateGoalControls() {
      boolean bl = !(this.getPrimaryPassenger() instanceof MobEntity);
      boolean bl2 = !(this.getVehicle() instanceof BoatEntity);
      this.goalSelector.setControlEnabled(Goal.Control.MOVE, bl);
      this.goalSelector.setControlEnabled(Goal.Control.JUMP, bl && bl2);
      this.goalSelector.setControlEnabled(Goal.Control.LOOK, bl);
   }

   protected float turnHead(float bodyRotation, float headRotation) {
      this.bodyControl.tick();
      return headRotation;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("CanPickUpLoot", this.canPickUpLoot());
      nbt.putBoolean("PersistenceRequired", this.persistent);
      NbtList nbtList = new NbtList();

      NbtCompound nbtCompound;
      for(Iterator var3 = this.armorItems.iterator(); var3.hasNext(); nbtList.add(nbtCompound)) {
         ItemStack itemStack = (ItemStack)var3.next();
         nbtCompound = new NbtCompound();
         if (!itemStack.isEmpty()) {
            itemStack.writeNbt(nbtCompound);
         }
      }

      nbt.put("ArmorItems", nbtList);
      NbtList nbtList2 = new NbtList();

      NbtCompound nbtCompound3;
      for(Iterator var11 = this.handItems.iterator(); var11.hasNext(); nbtList2.add(nbtCompound3)) {
         ItemStack itemStack2 = (ItemStack)var11.next();
         nbtCompound3 = new NbtCompound();
         if (!itemStack2.isEmpty()) {
            itemStack2.writeNbt(nbtCompound3);
         }
      }

      nbt.put("HandItems", nbtList2);
      NbtList nbtList3 = new NbtList();
      float[] var14 = this.armorDropChances;
      int var16 = var14.length;

      int var7;
      for(var7 = 0; var7 < var16; ++var7) {
         float f = var14[var7];
         nbtList3.add(NbtFloat.of(f));
      }

      nbt.put("ArmorDropChances", nbtList3);
      NbtList nbtList4 = new NbtList();
      float[] var17 = this.handDropChances;
      var7 = var17.length;

      for(int var19 = 0; var19 < var7; ++var19) {
         float g = var17[var19];
         nbtList4.add(NbtFloat.of(g));
      }

      nbt.put("HandDropChances", nbtList4);
      if (this.holdingEntity != null) {
         nbtCompound3 = new NbtCompound();
         if (this.holdingEntity instanceof LivingEntity) {
            UUID uUID = this.holdingEntity.getUuid();
            nbtCompound3.putUuid("UUID", uUID);
         } else if (this.holdingEntity instanceof AbstractDecorationEntity) {
            BlockPos blockPos = ((AbstractDecorationEntity)this.holdingEntity).getDecorationBlockPos();
            nbtCompound3.putInt("X", blockPos.getX());
            nbtCompound3.putInt("Y", blockPos.getY());
            nbtCompound3.putInt("Z", blockPos.getZ());
         }

         nbt.put("Leash", nbtCompound3);
      } else if (this.leashNbt != null) {
         nbt.put("Leash", this.leashNbt.copy());
      }

      nbt.putBoolean("LeftHanded", this.isLeftHanded());
      if (this.lootTable != null) {
         nbt.putString("DeathLootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            nbt.putLong("DeathLootTableSeed", this.lootTableSeed);
         }
      }

      if (this.isAiDisabled()) {
         nbt.putBoolean("NoAI", this.isAiDisabled());
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("CanPickUpLoot", 1)) {
         this.setCanPickUpLoot(nbt.getBoolean("CanPickUpLoot"));
      }

      this.persistent = nbt.getBoolean("PersistenceRequired");
      NbtList nbtList4;
      int l;
      if (nbt.contains("ArmorItems", 9)) {
         nbtList4 = nbt.getList("ArmorItems", 10);

         for(l = 0; l < this.armorItems.size(); ++l) {
            this.armorItems.set(l, ItemStack.fromNbt(nbtList4.getCompound(l)));
         }
      }

      if (nbt.contains("HandItems", 9)) {
         nbtList4 = nbt.getList("HandItems", 10);

         for(l = 0; l < this.handItems.size(); ++l) {
            this.handItems.set(l, ItemStack.fromNbt(nbtList4.getCompound(l)));
         }
      }

      if (nbt.contains("ArmorDropChances", 9)) {
         nbtList4 = nbt.getList("ArmorDropChances", 5);

         for(l = 0; l < nbtList4.size(); ++l) {
            this.armorDropChances[l] = nbtList4.getFloat(l);
         }
      }

      if (nbt.contains("HandDropChances", 9)) {
         nbtList4 = nbt.getList("HandDropChances", 5);

         for(l = 0; l < nbtList4.size(); ++l) {
            this.handDropChances[l] = nbtList4.getFloat(l);
         }
      }

      if (nbt.contains("Leash", 10)) {
         this.leashNbt = nbt.getCompound("Leash");
      }

      this.setLeftHanded(nbt.getBoolean("LeftHanded"));
      if (nbt.contains("DeathLootTable", 8)) {
         this.lootTable = new Identifier(nbt.getString("DeathLootTable"));
         this.lootTableSeed = nbt.getLong("DeathLootTableSeed");
      }

      this.setAiDisabled(nbt.getBoolean("NoAI"));
   }

   protected void dropLoot(DamageSource source, boolean causedByPlayer) {
      super.dropLoot(source, causedByPlayer);
      this.lootTable = null;
   }

   protected LootContext.Builder getLootContextBuilder(boolean causedByPlayer, DamageSource source) {
      return super.getLootContextBuilder(causedByPlayer, source).random(this.lootTableSeed, this.random);
   }

   public final Identifier getLootTable() {
      return this.lootTable == null ? this.getLootTableId() : this.lootTable;
   }

   protected Identifier getLootTableId() {
      return super.getLootTable();
   }

   public void setForwardSpeed(float forwardSpeed) {
      this.forwardSpeed = forwardSpeed;
   }

   public void setUpwardSpeed(float upwardSpeed) {
      this.upwardSpeed = upwardSpeed;
   }

   public void setSidewaysSpeed(float sidewaysMovement) {
      this.sidewaysSpeed = sidewaysMovement;
   }

   public void setMovementSpeed(float movementSpeed) {
      super.setMovementSpeed(movementSpeed);
      this.setForwardSpeed(movementSpeed);
   }

   public void tickMovement() {
      super.tickMovement();
      this.world.getProfiler().push("looting");
      if (!this.world.isClient && this.canPickUpLoot() && this.isAlive() && !this.dead && this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
         List<ItemEntity> list = this.world.getNonSpectatingEntities(ItemEntity.class, this.getBoundingBox().expand(1.0D, 0.0D, 1.0D));
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            ItemEntity itemEntity = (ItemEntity)var2.next();
            if (!itemEntity.removed && !itemEntity.getStack().isEmpty() && !itemEntity.cannotPickup() && this.canGather(itemEntity.getStack())) {
               this.loot(itemEntity);
            }
         }
      }

      this.world.getProfiler().pop();
   }

   protected void loot(ItemEntity item) {
      ItemStack itemStack = item.getStack();
      if (this.tryEquip(itemStack)) {
         this.method_29499(item);
         this.sendPickup(item, itemStack.getCount());
         item.remove();
      }

   }

   public boolean tryEquip(ItemStack equipment) {
      EquipmentSlot equipmentSlot = getPreferredEquipmentSlot(equipment);
      ItemStack itemStack = this.getEquippedStack(equipmentSlot);
      boolean bl = this.prefersNewEquipment(equipment, itemStack);
      if (bl && this.canPickupItem(equipment)) {
         double d = (double)this.getDropChance(equipmentSlot);
         if (!itemStack.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d) {
            this.dropStack(itemStack);
         }

         this.equipLootStack(equipmentSlot, equipment);
         this.onEquipStack(equipment);
         return true;
      } else {
         return false;
      }
   }

   protected void equipLootStack(EquipmentSlot slot, ItemStack stack) {
      this.equipStack(slot, stack);
      this.updateDropChances(slot);
      this.persistent = true;
   }

   public void updateDropChances(EquipmentSlot slot) {
      switch(slot.getType()) {
      case HAND:
         this.handDropChances[slot.getEntitySlotId()] = 2.0F;
         break;
      case ARMOR:
         this.armorDropChances[slot.getEntitySlotId()] = 2.0F;
      }

   }

   protected boolean prefersNewEquipment(ItemStack newStack, ItemStack oldStack) {
      if (oldStack.isEmpty()) {
         return true;
      } else if (newStack.getItem() instanceof SwordItem) {
         if (!(oldStack.getItem() instanceof SwordItem)) {
            return true;
         } else {
            SwordItem swordItem = (SwordItem)newStack.getItem();
            SwordItem swordItem2 = (SwordItem)oldStack.getItem();
            if (swordItem.getAttackDamage() != swordItem2.getAttackDamage()) {
               return swordItem.getAttackDamage() > swordItem2.getAttackDamage();
            } else {
               return this.prefersNewDamageableItem(newStack, oldStack);
            }
         }
      } else if (newStack.getItem() instanceof BowItem && oldStack.getItem() instanceof BowItem) {
         return this.prefersNewDamageableItem(newStack, oldStack);
      } else if (newStack.getItem() instanceof CrossbowItem && oldStack.getItem() instanceof CrossbowItem) {
         return this.prefersNewDamageableItem(newStack, oldStack);
      } else if (newStack.getItem() instanceof ArmorItem) {
         if (EnchantmentHelper.hasBindingCurse(oldStack)) {
            return false;
         } else if (!(oldStack.getItem() instanceof ArmorItem)) {
            return true;
         } else {
            ArmorItem armorItem = (ArmorItem)newStack.getItem();
            ArmorItem armorItem2 = (ArmorItem)oldStack.getItem();
            if (armorItem.getProtection() != armorItem2.getProtection()) {
               return armorItem.getProtection() > armorItem2.getProtection();
            } else if (armorItem.method_26353() != armorItem2.method_26353()) {
               return armorItem.method_26353() > armorItem2.method_26353();
            } else {
               return this.prefersNewDamageableItem(newStack, oldStack);
            }
         }
      } else {
         if (newStack.getItem() instanceof MiningToolItem) {
            if (oldStack.getItem() instanceof BlockItem) {
               return true;
            }

            if (oldStack.getItem() instanceof MiningToolItem) {
               MiningToolItem miningToolItem = (MiningToolItem)newStack.getItem();
               MiningToolItem miningToolItem2 = (MiningToolItem)oldStack.getItem();
               if (miningToolItem.getAttackDamage() != miningToolItem2.getAttackDamage()) {
                  return miningToolItem.getAttackDamage() > miningToolItem2.getAttackDamage();
               }

               return this.prefersNewDamageableItem(newStack, oldStack);
            }
         }

         return false;
      }
   }

   public boolean prefersNewDamageableItem(ItemStack newStack, ItemStack oldStack) {
      if (newStack.getDamage() >= oldStack.getDamage() && (!newStack.hasTag() || oldStack.hasTag())) {
         if (newStack.hasTag() && oldStack.hasTag()) {
            return newStack.getTag().getKeys().stream().anyMatch((string) -> {
               return !string.equals("Damage");
            }) && !oldStack.getTag().getKeys().stream().anyMatch((string) -> {
               return !string.equals("Damage");
            });
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean canPickupItem(ItemStack stack) {
      return true;
   }

   public boolean canGather(ItemStack stack) {
      return this.canPickupItem(stack);
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return true;
   }

   public boolean cannotDespawn() {
      return this.hasVehicle();
   }

   protected boolean isDisallowedInPeaceful() {
      return false;
   }

   public void checkDespawn() {
      if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.isDisallowedInPeaceful()) {
         this.remove();
      } else if (!this.isPersistent() && !this.cannotDespawn()) {
         Entity entity = this.world.getClosestPlayer(this, -1.0D);
         if (entity != null) {
            double d = entity.squaredDistanceTo((Entity)this);
            int i = this.getType().getSpawnGroup().getImmediateDespawnRange();
            int j = i * i;
            if (d > (double)j && this.canImmediatelyDespawn(d)) {
               this.remove();
            }

            int k = this.getType().getSpawnGroup().getDespawnStartRange();
            int l = k * k;
            if (this.despawnCounter > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.canImmediatelyDespawn(d)) {
               this.remove();
            } else if (d < (double)l) {
               this.despawnCounter = 0;
            }
         }

      } else {
         this.despawnCounter = 0;
      }
   }

   protected final void tickNewAi() {
      ++this.despawnCounter;
      this.world.getProfiler().push("sensing");
      this.visibilityCache.clear();
      this.world.getProfiler().pop();
      this.world.getProfiler().push("targetSelector");
      this.targetSelector.tick();
      this.world.getProfiler().pop();
      this.world.getProfiler().push("goalSelector");
      this.goalSelector.tick();
      this.world.getProfiler().pop();
      this.world.getProfiler().push("navigation");
      this.navigation.tick();
      this.world.getProfiler().pop();
      this.world.getProfiler().push("mob tick");
      this.mobTick();
      this.world.getProfiler().pop();
      this.world.getProfiler().push("controls");
      this.world.getProfiler().push("move");
      this.moveControl.tick();
      this.world.getProfiler().swap("look");
      this.lookControl.tick();
      this.world.getProfiler().swap("jump");
      this.jumpControl.tick();
      this.world.getProfiler().pop();
      this.world.getProfiler().pop();
      this.sendAiDebugData();
   }

   protected void sendAiDebugData() {
      DebugInfoSender.sendGoalSelector(this.world, this, this.goalSelector);
   }

   protected void mobTick() {
   }

   public int getLookPitchSpeed() {
      return 40;
   }

   public int getBodyYawSpeed() {
      return 75;
   }

   public int getLookYawSpeed() {
      return 10;
   }

   public void lookAtEntity(Entity targetEntity, float maxYawChange, float maxPitchChange) {
      double d = targetEntity.getX() - this.getX();
      double e = targetEntity.getZ() - this.getZ();
      double g;
      if (targetEntity instanceof LivingEntity) {
         LivingEntity livingEntity = (LivingEntity)targetEntity;
         g = livingEntity.getEyeY() - this.getEyeY();
      } else {
         g = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0D - this.getEyeY();
      }

      double h = (double)MathHelper.sqrt(d * d + e * e);
      float i = (float)(MathHelper.atan2(e, d) * 57.2957763671875D) - 90.0F;
      float j = (float)(-(MathHelper.atan2(g, h) * 57.2957763671875D));
      this.pitch = this.changeAngle(this.pitch, j, maxPitchChange);
      this.yaw = this.changeAngle(this.yaw, i, maxYawChange);
   }

   private float changeAngle(float oldAngle, float newAngle, float maxChangeInAngle) {
      float f = MathHelper.wrapDegrees(newAngle - oldAngle);
      if (f > maxChangeInAngle) {
         f = maxChangeInAngle;
      }

      if (f < -maxChangeInAngle) {
         f = -maxChangeInAngle;
      }

      return oldAngle + f;
   }

   public static boolean canMobSpawn(EntityType<? extends MobEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      BlockPos blockPos = pos.down();
      return spawnReason == SpawnReason.SPAWNER || world.getBlockState(blockPos).allowsSpawning(world, blockPos, type);
   }

   public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
      return true;
   }

   public boolean canSpawn(WorldView world) {
      return !world.containsFluid(this.getBoundingBox()) && world.intersectsEntities(this);
   }

   public int getLimitPerChunk() {
      return 4;
   }

   public boolean spawnsTooManyForEachTry(int count) {
      return false;
   }

   public int getSafeFallDistance() {
      if (this.getTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i -= (3 - this.world.getDifficulty().getId()) * 4;
         if (i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable<ItemStack> getItemsHand() {
      return this.handItems;
   }

   public Iterable<ItemStack> getArmorItems() {
      return this.armorItems;
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      switch(slot.getType()) {
      case HAND:
         return (ItemStack)this.handItems.get(slot.getEntitySlotId());
      case ARMOR:
         return (ItemStack)this.armorItems.get(slot.getEntitySlotId());
      default:
         return ItemStack.EMPTY;
      }
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
      switch(slot.getType()) {
      case HAND:
         this.handItems.set(slot.getEntitySlotId(), stack);
         break;
      case ARMOR:
         this.armorItems.set(slot.getEntitySlotId(), stack);
      }

   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
      super.dropEquipment(source, lootingMultiplier, allowDrops);
      EquipmentSlot[] var4 = EquipmentSlot.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         EquipmentSlot equipmentSlot = var4[var6];
         ItemStack itemStack = this.getEquippedStack(equipmentSlot);
         float f = this.getDropChance(equipmentSlot);
         boolean bl = f > 1.0F;
         if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack) && (allowDrops || bl) && Math.max(this.random.nextFloat() - (float)lootingMultiplier * 0.01F, 0.0F) < f) {
            if (!bl && itemStack.isDamageable()) {
               itemStack.setDamage(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
            }

            this.dropStack(itemStack);
            this.equipStack(equipmentSlot, ItemStack.EMPTY);
         }
      }

   }

   protected float getDropChance(EquipmentSlot slot) {
      float h;
      switch(slot.getType()) {
      case HAND:
         h = this.handDropChances[slot.getEntitySlotId()];
         break;
      case ARMOR:
         h = this.armorDropChances[slot.getEntitySlotId()];
         break;
      default:
         h = 0.0F;
      }

      return h;
   }

   protected void initEquipment(LocalDifficulty difficulty) {
      if (this.random.nextFloat() < 0.15F * difficulty.getClampedLocalDifficulty()) {
         int i = this.random.nextInt(2);
         float f = this.world.getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
         if (this.random.nextFloat() < 0.095F) {
            ++i;
         }

         if (this.random.nextFloat() < 0.095F) {
            ++i;
         }

         if (this.random.nextFloat() < 0.095F) {
            ++i;
         }

         boolean bl = true;
         EquipmentSlot[] var5 = EquipmentSlot.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            EquipmentSlot equipmentSlot = var5[var7];
            if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
               ItemStack itemStack = this.getEquippedStack(equipmentSlot);
               if (!bl && this.random.nextFloat() < f) {
                  break;
               }

               bl = false;
               if (itemStack.isEmpty()) {
                  Item item = getEquipmentForSlot(equipmentSlot, i);
                  if (item != null) {
                     this.equipStack(equipmentSlot, new ItemStack(item));
                  }
               }
            }
         }
      }

   }

   public static EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
      Item item = stack.getItem();
      if (item != Blocks.CARVED_PUMPKIN.asItem() && (!(item instanceof BlockItem) || !(((BlockItem)item).getBlock() instanceof AbstractSkullBlock))) {
         if (item instanceof ArmorItem) {
            return ((ArmorItem)item).getSlotType();
         } else if (item == Items.ELYTRA) {
            return EquipmentSlot.CHEST;
         } else {
            return item == Items.SHIELD ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
         }
      } else {
         return EquipmentSlot.HEAD;
      }
   }

   @Nullable
   public static Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
      switch(equipmentSlot) {
      case HEAD:
         if (equipmentLevel == 0) {
            return Items.LEATHER_HELMET;
         } else if (equipmentLevel == 1) {
            return Items.GOLDEN_HELMET;
         } else if (equipmentLevel == 2) {
            return Items.CHAINMAIL_HELMET;
         } else if (equipmentLevel == 3) {
            return Items.IRON_HELMET;
         } else if (equipmentLevel == 4) {
            return Items.DIAMOND_HELMET;
         }
      case CHEST:
         if (equipmentLevel == 0) {
            return Items.LEATHER_CHESTPLATE;
         } else if (equipmentLevel == 1) {
            return Items.GOLDEN_CHESTPLATE;
         } else if (equipmentLevel == 2) {
            return Items.CHAINMAIL_CHESTPLATE;
         } else if (equipmentLevel == 3) {
            return Items.IRON_CHESTPLATE;
         } else if (equipmentLevel == 4) {
            return Items.DIAMOND_CHESTPLATE;
         }
      case LEGS:
         if (equipmentLevel == 0) {
            return Items.LEATHER_LEGGINGS;
         } else if (equipmentLevel == 1) {
            return Items.GOLDEN_LEGGINGS;
         } else if (equipmentLevel == 2) {
            return Items.CHAINMAIL_LEGGINGS;
         } else if (equipmentLevel == 3) {
            return Items.IRON_LEGGINGS;
         } else if (equipmentLevel == 4) {
            return Items.DIAMOND_LEGGINGS;
         }
      case FEET:
         if (equipmentLevel == 0) {
            return Items.LEATHER_BOOTS;
         } else if (equipmentLevel == 1) {
            return Items.GOLDEN_BOOTS;
         } else if (equipmentLevel == 2) {
            return Items.CHAINMAIL_BOOTS;
         } else if (equipmentLevel == 3) {
            return Items.IRON_BOOTS;
         } else if (equipmentLevel == 4) {
            return Items.DIAMOND_BOOTS;
         }
      default:
         return null;
      }
   }

   protected void updateEnchantments(LocalDifficulty difficulty) {
      float f = difficulty.getClampedLocalDifficulty();
      this.method_30759(f);
      EquipmentSlot[] var3 = EquipmentSlot.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         EquipmentSlot equipmentSlot = var3[var5];
         if (equipmentSlot.getType() == EquipmentSlot.Type.ARMOR) {
            this.method_30758(f, equipmentSlot);
         }
      }

   }

   protected void method_30759(float f) {
      if (!this.getMainHandStack().isEmpty() && this.random.nextFloat() < 0.25F * f) {
         this.equipStack(EquipmentSlot.MAINHAND, EnchantmentHelper.enchant(this.random, this.getMainHandStack(), (int)(5.0F + f * (float)this.random.nextInt(18)), false));
      }

   }

   protected void method_30758(float f, EquipmentSlot equipmentSlot) {
      ItemStack itemStack = this.getEquippedStack(equipmentSlot);
      if (!itemStack.isEmpty() && this.random.nextFloat() < 0.5F * f) {
         this.equipStack(equipmentSlot, EnchantmentHelper.enchant(this.random, itemStack, (int)(5.0F + f * (float)this.random.nextInt(18)), false));
      }

   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).addPersistentModifier(new EntityAttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, EntityAttributeModifier.Operation.MULTIPLY_BASE));
      if (this.random.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return entityData;
   }

   public boolean canBeControlledByRider() {
      return false;
   }

   public void setPersistent() {
      this.persistent = true;
   }

   public void setEquipmentDropChance(EquipmentSlot slot, float chance) {
      switch(slot.getType()) {
      case HAND:
         this.handDropChances[slot.getEntitySlotId()] = chance;
         break;
      case ARMOR:
         this.armorDropChances[slot.getEntitySlotId()] = chance;
      }

   }

   public boolean canPickUpLoot() {
      return this.pickUpLoot;
   }

   public void setCanPickUpLoot(boolean pickUpLoot) {
      this.pickUpLoot = pickUpLoot;
   }

   public boolean canEquip(ItemStack stack) {
      EquipmentSlot equipmentSlot = getPreferredEquipmentSlot(stack);
      return this.getEquippedStack(equipmentSlot).isEmpty() && this.canPickUpLoot();
   }

   public boolean isPersistent() {
      return this.persistent;
   }

   public final ActionResult interact(PlayerEntity player, Hand hand) {
      if (!this.isAlive()) {
         return ActionResult.PASS;
      } else if (this.getHoldingEntity() == player) {
         this.detachLeash(true, !player.abilities.creativeMode);
         return ActionResult.success(this.world.isClient);
      } else {
         ActionResult actionResult = this.method_29506(player, hand);
         if (actionResult.isAccepted()) {
            return actionResult;
         } else {
            actionResult = this.interactMob(player, hand);
            return actionResult.isAccepted() ? actionResult : super.interact(player, hand);
         }
      }
   }

   private ActionResult method_29506(PlayerEntity playerEntity, Hand hand) {
      ItemStack itemStack = playerEntity.getStackInHand(hand);
      if (itemStack.getItem() == Items.LEAD && this.canBeLeashedBy(playerEntity)) {
         this.attachLeash(playerEntity, true);
         itemStack.decrement(1);
         return ActionResult.success(this.world.isClient);
      } else {
         if (itemStack.getItem() == Items.NAME_TAG) {
            ActionResult actionResult = itemStack.useOnEntity(playerEntity, this, hand);
            if (actionResult.isAccepted()) {
               return actionResult;
            }
         }

         if (itemStack.getItem() instanceof SpawnEggItem) {
            if (this.world instanceof ServerWorld) {
               SpawnEggItem spawnEggItem = (SpawnEggItem)itemStack.getItem();
               Optional<MobEntity> optional = spawnEggItem.spawnBaby(playerEntity, this, this.getType(), (ServerWorld)this.world, this.getPos(), itemStack);
               optional.ifPresent((mobEntity) -> {
                  this.onPlayerSpawnedChild(playerEntity, mobEntity);
               });
               return optional.isPresent() ? ActionResult.SUCCESS : ActionResult.PASS;
            } else {
               return ActionResult.CONSUME;
            }
         } else {
            return ActionResult.PASS;
         }
      }
   }

   protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
   }

   protected ActionResult interactMob(PlayerEntity player, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean isInWalkTargetRange() {
      return this.isInWalkTargetRange(this.getBlockPos());
   }

   public boolean isInWalkTargetRange(BlockPos pos) {
      if (this.positionTargetRange == -1.0F) {
         return true;
      } else {
         return this.positionTarget.getSquaredDistance(pos) < (double)(this.positionTargetRange * this.positionTargetRange);
      }
   }

   public void setPositionTarget(BlockPos target, int range) {
      this.positionTarget = target;
      this.positionTargetRange = (float)range;
   }

   public BlockPos getPositionTarget() {
      return this.positionTarget;
   }

   public float getPositionTargetRange() {
      return this.positionTargetRange;
   }

   public boolean hasPositionTarget() {
      return this.positionTargetRange != -1.0F;
   }

   @Nullable
   public <T extends MobEntity> T method_29243(EntityType<T> entityType, boolean bl) {
      if (this.removed) {
         return null;
      } else {
         T mobEntity = (MobEntity)entityType.create(this.world);
         mobEntity.copyPositionAndRotation(this);
         mobEntity.setBaby(this.isBaby());
         mobEntity.setAiDisabled(this.isAiDisabled());
         if (this.hasCustomName()) {
            mobEntity.setCustomName(this.getCustomName());
            mobEntity.setCustomNameVisible(this.isCustomNameVisible());
         }

         if (this.isPersistent()) {
            mobEntity.setPersistent();
         }

         mobEntity.setInvulnerable(this.isInvulnerable());
         if (bl) {
            mobEntity.setCanPickUpLoot(this.canPickUpLoot());
            EquipmentSlot[] var4 = EquipmentSlot.values();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               EquipmentSlot equipmentSlot = var4[var6];
               ItemStack itemStack = this.getEquippedStack(equipmentSlot);
               if (!itemStack.isEmpty()) {
                  mobEntity.equipStack(equipmentSlot, itemStack.copy());
                  mobEntity.setEquipmentDropChance(equipmentSlot, this.getDropChance(equipmentSlot));
                  itemStack.setCount(0);
               }
            }
         }

         this.world.spawnEntity(mobEntity);
         if (this.hasVehicle()) {
            Entity entity = this.getVehicle();
            this.stopRiding();
            mobEntity.startRiding(entity, true);
         }

         this.remove();
         return mobEntity;
      }
   }

   protected void updateLeash() {
      if (this.leashNbt != null) {
         this.readLeashNbt();
      }

      if (this.holdingEntity != null) {
         if (!this.isAlive() || !this.holdingEntity.isAlive()) {
            this.detachLeash(true, true);
         }

      }
   }

   public void detachLeash(boolean sendPacket, boolean dropItem) {
      if (this.holdingEntity != null) {
         this.teleporting = false;
         if (!(this.holdingEntity instanceof PlayerEntity)) {
            this.holdingEntity.teleporting = false;
         }

         this.holdingEntity = null;
         this.leashNbt = null;
         if (!this.world.isClient && dropItem) {
            this.dropItem(Items.LEAD);
         }

         if (!this.world.isClient && sendPacket && this.world instanceof ServerWorld) {
            ((ServerWorld)this.world).getChunkManager().sendToOtherNearbyPlayers(this, new EntityAttachS2CPacket(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return !this.isLeashed() && !(this instanceof Monster);
   }

   public boolean isLeashed() {
      return this.holdingEntity != null;
   }

   @Nullable
   public Entity getHoldingEntity() {
      if (this.holdingEntity == null && this.holdingEntityId != 0 && this.world.isClient) {
         this.holdingEntity = this.world.getEntityById(this.holdingEntityId);
      }

      return this.holdingEntity;
   }

   public void attachLeash(Entity entity, boolean sendPacket) {
      this.holdingEntity = entity;
      this.leashNbt = null;
      this.teleporting = true;
      if (!(this.holdingEntity instanceof PlayerEntity)) {
         this.holdingEntity.teleporting = true;
      }

      if (!this.world.isClient && sendPacket && this.world instanceof ServerWorld) {
         ((ServerWorld)this.world).getChunkManager().sendToOtherNearbyPlayers(this, new EntityAttachS2CPacket(this, this.holdingEntity));
      }

      if (this.hasVehicle()) {
         this.stopRiding();
      }

   }

   @Environment(EnvType.CLIENT)
   public void setHoldingEntityId(int id) {
      this.holdingEntityId = id;
      this.detachLeash(false, false);
   }

   public boolean startRiding(Entity entity, boolean force) {
      boolean bl = super.startRiding(entity, force);
      if (bl && this.isLeashed()) {
         this.detachLeash(true, true);
      }

      return bl;
   }

   private void readLeashNbt() {
      if (this.leashNbt != null && this.world instanceof ServerWorld) {
         if (this.leashNbt.containsUuid("UUID")) {
            UUID uUID = this.leashNbt.getUuid("UUID");
            Entity entity = ((ServerWorld)this.world).getEntity(uUID);
            if (entity != null) {
               this.attachLeash(entity, true);
               return;
            }
         } else if (this.leashNbt.contains("X", 99) && this.leashNbt.contains("Y", 99) && this.leashNbt.contains("Z", 99)) {
            BlockPos blockPos = new BlockPos(this.leashNbt.getInt("X"), this.leashNbt.getInt("Y"), this.leashNbt.getInt("Z"));
            this.attachLeash(LeashKnotEntity.getOrCreate(this.world, blockPos), true);
            return;
         }

         if (this.age > 100) {
            this.dropItem(Items.LEAD);
            this.leashNbt = null;
         }
      }

   }

   public boolean equip(int slot, ItemStack item) {
      EquipmentSlot equipmentSlot7;
      if (slot == 98) {
         equipmentSlot7 = EquipmentSlot.MAINHAND;
      } else if (slot == 99) {
         equipmentSlot7 = EquipmentSlot.OFFHAND;
      } else if (slot == 100 + EquipmentSlot.HEAD.getEntitySlotId()) {
         equipmentSlot7 = EquipmentSlot.HEAD;
      } else if (slot == 100 + EquipmentSlot.CHEST.getEntitySlotId()) {
         equipmentSlot7 = EquipmentSlot.CHEST;
      } else if (slot == 100 + EquipmentSlot.LEGS.getEntitySlotId()) {
         equipmentSlot7 = EquipmentSlot.LEGS;
      } else {
         if (slot != 100 + EquipmentSlot.FEET.getEntitySlotId()) {
            return false;
         }

         equipmentSlot7 = EquipmentSlot.FEET;
      }

      if (!item.isEmpty() && !canEquipmentSlotContain(equipmentSlot7, item) && equipmentSlot7 != EquipmentSlot.HEAD) {
         return false;
      } else {
         this.equipStack(equipmentSlot7, item);
         return true;
      }
   }

   public boolean isLogicalSideForUpdatingMovement() {
      return this.canBeControlledByRider() && super.isLogicalSideForUpdatingMovement();
   }

   public static boolean canEquipmentSlotContain(EquipmentSlot slot, ItemStack item) {
      EquipmentSlot equipmentSlot = getPreferredEquipmentSlot(item);
      return equipmentSlot == slot || equipmentSlot == EquipmentSlot.MAINHAND && slot == EquipmentSlot.OFFHAND || equipmentSlot == EquipmentSlot.OFFHAND && slot == EquipmentSlot.MAINHAND;
   }

   public boolean canMoveVoluntarily() {
      return super.canMoveVoluntarily() && !this.isAiDisabled();
   }

   public void setAiDisabled(boolean aiDisabled) {
      byte b = (Byte)this.dataTracker.get(MOB_FLAGS);
      this.dataTracker.set(MOB_FLAGS, aiDisabled ? (byte)(b | 1) : (byte)(b & -2));
   }

   public void setLeftHanded(boolean leftHanded) {
      byte b = (Byte)this.dataTracker.get(MOB_FLAGS);
      this.dataTracker.set(MOB_FLAGS, leftHanded ? (byte)(b | 2) : (byte)(b & -3));
   }

   public void setAttacking(boolean attacking) {
      byte b = (Byte)this.dataTracker.get(MOB_FLAGS);
      this.dataTracker.set(MOB_FLAGS, attacking ? (byte)(b | 4) : (byte)(b & -5));
   }

   public boolean isAiDisabled() {
      return ((Byte)this.dataTracker.get(MOB_FLAGS) & 1) != 0;
   }

   public boolean isLeftHanded() {
      return ((Byte)this.dataTracker.get(MOB_FLAGS) & 2) != 0;
   }

   public boolean isAttacking() {
      return ((Byte)this.dataTracker.get(MOB_FLAGS) & 4) != 0;
   }

   public void setBaby(boolean baby) {
   }

   public Arm getMainArm() {
      return this.isLeftHanded() ? Arm.LEFT : Arm.RIGHT;
   }

   public boolean canTarget(LivingEntity target) {
      return target.getType() == EntityType.PLAYER && ((PlayerEntity)target).abilities.invulnerable ? false : super.canTarget(target);
   }

   public boolean tryAttack(Entity target) {
      float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      float g = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (target instanceof LivingEntity) {
         f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
         g += (float)EnchantmentHelper.getKnockback(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         target.setOnFireFor(i * 4);
      }

      boolean bl = target.damage(DamageSource.mob(this), f);
      if (bl) {
         if (g > 0.0F && target instanceof LivingEntity) {
            ((LivingEntity)target).takeKnockback(g * 0.5F, (double)MathHelper.sin(this.yaw * 0.017453292F), (double)(-MathHelper.cos(this.yaw * 0.017453292F)));
            this.setVelocity(this.getVelocity().multiply(0.6D, 1.0D, 0.6D));
         }

         if (target instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)target;
            this.disablePlayerShield(playerEntity, this.getMainHandStack(), playerEntity.isUsingItem() ? playerEntity.getActiveItem() : ItemStack.EMPTY);
         }

         this.dealDamage(this, target);
         this.onAttacking(target);
      }

      return bl;
   }

   private void disablePlayerShield(PlayerEntity player, ItemStack mobStack, ItemStack playerStack) {
      if (!mobStack.isEmpty() && !playerStack.isEmpty() && mobStack.getItem() instanceof AxeItem && playerStack.getItem() == Items.SHIELD) {
         float f = 0.25F + (float)EnchantmentHelper.getEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            player.getItemCooldownManager().set(Items.SHIELD, 100);
            this.world.sendEntityStatus(player, (byte)30);
         }
      }

   }

   protected boolean isAffectedByDaylight() {
      if (this.world.isDay() && !this.world.isClient) {
         float f = this.getBrightnessAtEyes();
         BlockPos blockPos = this.getVehicle() instanceof BoatEntity ? (new BlockPos(this.getX(), (double)Math.round(this.getY()), this.getZ())).up() : new BlockPos(this.getX(), (double)Math.round(this.getY()), this.getZ());
         if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.isSkyVisible(blockPos)) {
            return true;
         }
      }

      return false;
   }

   protected void swimUpward(Tag<Fluid> fluid) {
      if (this.getNavigation().canSwim()) {
         super.swimUpward(fluid);
      } else {
         this.setVelocity(this.getVelocity().add(0.0D, 0.3D, 0.0D));
      }

   }

   protected void method_30076() {
      super.method_30076();
      this.detachLeash(true, false);
   }

   static {
      MOB_FLAGS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BYTE);
   }
}
