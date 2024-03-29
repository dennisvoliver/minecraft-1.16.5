package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.apache.commons.lang3.tuple.Pair;

public class MooshroomEntity extends CowEntity implements Shearable {
   private static final TrackedData<String> TYPE;
   private StatusEffect stewEffect;
   private int stewEffectDuration;
   private UUID lightningId;

   public MooshroomEntity(EntityType<? extends MooshroomEntity> entityType, World world) {
      super(entityType, world);
   }

   public float getPathfindingFavor(BlockPos pos, WorldView world) {
      return world.getBlockState(pos.down()).isOf(Blocks.MYCELIUM) ? 10.0F : world.getBrightness(pos) - 0.5F;
   }

   public static boolean canSpawn(EntityType<MooshroomEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
      return world.getBlockState(pos.down()).isOf(Blocks.MYCELIUM) && world.getBaseLightLevel(pos, 0) > 8;
   }

   public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
      UUID uUID = lightning.getUuid();
      if (!uUID.equals(this.lightningId)) {
         this.setType(this.getMooshroomType() == MooshroomEntity.Type.RED ? MooshroomEntity.Type.BROWN : MooshroomEntity.Type.RED);
         this.lightningId = uUID;
         this.playSound(SoundEvents.ENTITY_MOOSHROOM_CONVERT, 2.0F, 1.0F);
      }

   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(TYPE, MooshroomEntity.Type.RED.name);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.getItem() == Items.BOWL && !this.isBaby()) {
         boolean bl = false;
         ItemStack itemStack3;
         if (this.stewEffect != null) {
            bl = true;
            itemStack3 = new ItemStack(Items.SUSPICIOUS_STEW);
            SuspiciousStewItem.addEffectToStew(itemStack3, this.stewEffect, this.stewEffectDuration);
            this.stewEffect = null;
            this.stewEffectDuration = 0;
         } else {
            itemStack3 = new ItemStack(Items.MUSHROOM_STEW);
         }

         ItemStack itemStack4 = ItemUsage.method_30270(itemStack, player, itemStack3, false);
         player.setStackInHand(hand, itemStack4);
         SoundEvent soundEvent2;
         if (bl) {
            soundEvent2 = SoundEvents.ENTITY_MOOSHROOM_SUSPICIOUS_MILK;
         } else {
            soundEvent2 = SoundEvents.ENTITY_MOOSHROOM_MILK;
         }

         this.playSound(soundEvent2, 1.0F, 1.0F);
         return ActionResult.success(this.world.isClient);
      } else if (itemStack.getItem() == Items.SHEARS && this.isShearable()) {
         this.sheared(SoundCategory.PLAYERS);
         if (!this.world.isClient) {
            itemStack.damage(1, (LivingEntity)player, (Consumer)((playerEntity) -> {
               playerEntity.sendToolBreakStatus(hand);
            }));
         }

         return ActionResult.success(this.world.isClient);
      } else if (this.getMooshroomType() == MooshroomEntity.Type.BROWN && itemStack.getItem().isIn((Tag)ItemTags.SMALL_FLOWERS)) {
         if (this.stewEffect != null) {
            for(int i = 0; i < 2; ++i) {
               this.world.addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0D, this.getBodyY(0.5D), this.getZ() + this.random.nextDouble() / 2.0D, 0.0D, this.random.nextDouble() / 5.0D, 0.0D);
            }
         } else {
            Optional<Pair<StatusEffect, Integer>> optional = this.getStewEffectFrom(itemStack);
            if (!optional.isPresent()) {
               return ActionResult.PASS;
            }

            Pair<StatusEffect, Integer> pair = (Pair)optional.get();
            if (!player.abilities.creativeMode) {
               itemStack.decrement(1);
            }

            for(int j = 0; j < 4; ++j) {
               this.world.addParticle(ParticleTypes.EFFECT, this.getX() + this.random.nextDouble() / 2.0D, this.getBodyY(0.5D), this.getZ() + this.random.nextDouble() / 2.0D, 0.0D, this.random.nextDouble() / 5.0D, 0.0D);
            }

            this.stewEffect = (StatusEffect)pair.getLeft();
            this.stewEffectDuration = (Integer)pair.getRight();
            this.playSound(SoundEvents.ENTITY_MOOSHROOM_EAT, 2.0F, 1.0F);
         }

         return ActionResult.success(this.world.isClient);
      } else {
         return super.interactMob(player, hand);
      }
   }

   public void sheared(SoundCategory shearedSoundCategory) {
      this.world.playSoundFromEntity((PlayerEntity)null, this, SoundEvents.ENTITY_MOOSHROOM_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
      if (!this.world.isClient()) {
         ((ServerWorld)this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getBodyY(0.5D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
         this.remove();
         CowEntity cowEntity = (CowEntity)EntityType.COW.create(this.world);
         cowEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch);
         cowEntity.setHealth(this.getHealth());
         cowEntity.bodyYaw = this.bodyYaw;
         if (this.hasCustomName()) {
            cowEntity.setCustomName(this.getCustomName());
            cowEntity.setCustomNameVisible(this.isCustomNameVisible());
         }

         if (this.isPersistent()) {
            cowEntity.setPersistent();
         }

         cowEntity.setInvulnerable(this.isInvulnerable());
         this.world.spawnEntity(cowEntity);

         for(int i = 0; i < 5; ++i) {
            this.world.spawnEntity(new ItemEntity(this.world, this.getX(), this.getBodyY(1.0D), this.getZ(), new ItemStack(this.getMooshroomType().mushroom.getBlock())));
         }
      }

   }

   public boolean isShearable() {
      return this.isAlive() && !this.isBaby();
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putString("Type", this.getMooshroomType().name);
      if (this.stewEffect != null) {
         nbt.putByte("EffectId", (byte)StatusEffect.getRawId(this.stewEffect));
         nbt.putInt("EffectDuration", this.stewEffectDuration);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setType(MooshroomEntity.Type.fromName(nbt.getString("Type")));
      if (nbt.contains("EffectId", 1)) {
         this.stewEffect = StatusEffect.byRawId(nbt.getByte("EffectId"));
      }

      if (nbt.contains("EffectDuration", 3)) {
         this.stewEffectDuration = nbt.getInt("EffectDuration");
      }

   }

   private Optional<Pair<StatusEffect, Integer>> getStewEffectFrom(ItemStack flower) {
      Item item = flower.getItem();
      if (item instanceof BlockItem) {
         Block block = ((BlockItem)item).getBlock();
         if (block instanceof FlowerBlock) {
            FlowerBlock flowerBlock = (FlowerBlock)block;
            return Optional.of(Pair.of(flowerBlock.getEffectInStew(), flowerBlock.getEffectInStewDuration()));
         }
      }

      return Optional.empty();
   }

   private void setType(MooshroomEntity.Type type) {
      this.dataTracker.set(TYPE, type.name);
   }

   public MooshroomEntity.Type getMooshroomType() {
      return MooshroomEntity.Type.fromName((String)this.dataTracker.get(TYPE));
   }

   public MooshroomEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
      MooshroomEntity mooshroomEntity = (MooshroomEntity)EntityType.MOOSHROOM.create(serverWorld);
      mooshroomEntity.setType(this.chooseBabyType((MooshroomEntity)passiveEntity));
      return mooshroomEntity;
   }

   private MooshroomEntity.Type chooseBabyType(MooshroomEntity mooshroom) {
      MooshroomEntity.Type type = this.getMooshroomType();
      MooshroomEntity.Type type2 = mooshroom.getMooshroomType();
      MooshroomEntity.Type type4;
      if (type == type2 && this.random.nextInt(1024) == 0) {
         type4 = type == MooshroomEntity.Type.BROWN ? MooshroomEntity.Type.RED : MooshroomEntity.Type.BROWN;
      } else {
         type4 = this.random.nextBoolean() ? type : type2;
      }

      return type4;
   }

   static {
      TYPE = DataTracker.registerData(MooshroomEntity.class, TrackedDataHandlerRegistry.STRING);
   }

   public static enum Type {
      RED("red", Blocks.RED_MUSHROOM.getDefaultState()),
      BROWN("brown", Blocks.BROWN_MUSHROOM.getDefaultState());

      private final String name;
      private final BlockState mushroom;

      private Type(String name, BlockState mushroom) {
         this.name = name;
         this.mushroom = mushroom;
      }

      @Environment(EnvType.CLIENT)
      public BlockState getMushroomState() {
         return this.mushroom;
      }

      private static MooshroomEntity.Type fromName(String name) {
         MooshroomEntity.Type[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            MooshroomEntity.Type type = var1[var3];
            if (type.name.equals(name)) {
               return type;
            }
         }

         return RED;
      }
   }
}
