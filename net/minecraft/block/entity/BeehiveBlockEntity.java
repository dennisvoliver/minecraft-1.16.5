package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlockEntity extends BlockEntity implements Tickable {
   private final List<BeehiveBlockEntity.Bee> bees = Lists.newArrayList();
   @Nullable
   private BlockPos flowerPos = null;

   public BeehiveBlockEntity() {
      super(BlockEntityType.BEEHIVE);
   }

   public void markDirty() {
      if (this.isNearFire()) {
         this.angerBees((PlayerEntity)null, this.world.getBlockState(this.getPos()), BeehiveBlockEntity.BeeState.EMERGENCY);
      }

      super.markDirty();
   }

   public boolean isNearFire() {
      if (this.world == null) {
         return false;
      } else {
         Iterator var1 = BlockPos.iterate(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1)).iterator();

         BlockPos blockPos;
         do {
            if (!var1.hasNext()) {
               return false;
            }

            blockPos = (BlockPos)var1.next();
         } while(!(this.world.getBlockState(blockPos).getBlock() instanceof FireBlock));

         return true;
      }
   }

   public boolean hasNoBees() {
      return this.bees.isEmpty();
   }

   public boolean isFullOfBees() {
      return this.bees.size() == 3;
   }

   public void angerBees(@Nullable PlayerEntity player, BlockState state, BeehiveBlockEntity.BeeState beeState) {
      List<Entity> list = this.tryReleaseBee(state, beeState);
      if (player != null) {
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Entity entity = (Entity)var5.next();
            if (entity instanceof BeeEntity) {
               BeeEntity beeEntity = (BeeEntity)entity;
               if (player.getPos().squaredDistanceTo(entity.getPos()) <= 16.0D) {
                  if (!this.isSmoked()) {
                     beeEntity.setTarget(player);
                  } else {
                     beeEntity.setCannotEnterHiveTicks(400);
                  }
               }
            }
         }
      }

   }

   private List<Entity> tryReleaseBee(BlockState state, BeehiveBlockEntity.BeeState beeState) {
      List<Entity> list = Lists.newArrayList();
      this.bees.removeIf((bee) -> {
         return this.releaseBee(state, bee, list, beeState);
      });
      return list;
   }

   public void tryEnterHive(Entity entity, boolean hasNectar) {
      this.tryEnterHive(entity, hasNectar, 0);
   }

   public int getBeeCount() {
      return this.bees.size();
   }

   public static int getHoneyLevel(BlockState state) {
      return (Integer)state.get(BeehiveBlock.HONEY_LEVEL);
   }

   public boolean isSmoked() {
      return CampfireBlock.isLitCampfireInRange(this.world, this.getPos());
   }

   protected void sendDebugData() {
      DebugInfoSender.sendBeehiveDebugData(this);
   }

   public void tryEnterHive(Entity entity, boolean hasNectar, int ticksInHive) {
      if (this.bees.size() < 3) {
         entity.stopRiding();
         entity.removeAllPassengers();
         NbtCompound nbtCompound = new NbtCompound();
         entity.saveNbt(nbtCompound);
         this.bees.add(new BeehiveBlockEntity.Bee(nbtCompound, ticksInHive, hasNectar ? 2400 : 600));
         if (this.world != null) {
            if (entity instanceof BeeEntity) {
               BeeEntity beeEntity = (BeeEntity)entity;
               if (beeEntity.hasFlower() && (!this.hasFlowerPos() || this.world.random.nextBoolean())) {
                  this.flowerPos = beeEntity.getFlowerPos();
               }
            }

            BlockPos blockPos = this.getPos();
            this.world.playSound((PlayerEntity)null, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         entity.remove();
      }
   }

   private boolean releaseBee(BlockState state, BeehiveBlockEntity.Bee bee, @Nullable List<Entity> list, BeehiveBlockEntity.BeeState beeState) {
      if ((this.world.isNight() || this.world.isRaining()) && beeState != BeehiveBlockEntity.BeeState.EMERGENCY) {
         return false;
      } else {
         BlockPos blockPos = this.getPos();
         NbtCompound nbtCompound = bee.entityData;
         nbtCompound.remove("Passengers");
         nbtCompound.remove("Leash");
         nbtCompound.remove("UUID");
         Direction direction = (Direction)state.get(BeehiveBlock.FACING);
         BlockPos blockPos2 = blockPos.offset(direction);
         boolean bl = !this.world.getBlockState(blockPos2).getCollisionShape(this.world, blockPos2).isEmpty();
         if (bl && beeState != BeehiveBlockEntity.BeeState.EMERGENCY) {
            return false;
         } else {
            Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, this.world, (entityx) -> {
               return entityx;
            });
            if (entity != null) {
               if (!entity.getType().isIn(EntityTypeTags.BEEHIVE_INHABITORS)) {
                  return false;
               } else {
                  if (entity instanceof BeeEntity) {
                     BeeEntity beeEntity = (BeeEntity)entity;
                     if (this.hasFlowerPos() && !beeEntity.hasFlower() && this.world.random.nextFloat() < 0.9F) {
                        beeEntity.setFlowerPos(this.flowerPos);
                     }

                     if (beeState == BeehiveBlockEntity.BeeState.HONEY_DELIVERED) {
                        beeEntity.onHoneyDelivered();
                        if (state.getBlock().isIn(BlockTags.BEEHIVES)) {
                           int i = getHoneyLevel(state);
                           if (i < 5) {
                              int j = this.world.random.nextInt(100) == 0 ? 2 : 1;
                              if (i + j > 5) {
                                 --j;
                              }

                              this.world.setBlockState(this.getPos(), (BlockState)state.with(BeehiveBlock.HONEY_LEVEL, i + j));
                           }
                        }
                     }

                     this.ageBee(bee.ticksInHive, beeEntity);
                     if (list != null) {
                        list.add(beeEntity);
                     }

                     float f = entity.getWidth();
                     double d = bl ? 0.0D : 0.55D + (double)(f / 2.0F);
                     double e = (double)blockPos.getX() + 0.5D + d * (double)direction.getOffsetX();
                     double g = (double)blockPos.getY() + 0.5D - (double)(entity.getHeight() / 2.0F);
                     double h = (double)blockPos.getZ() + 0.5D + d * (double)direction.getOffsetZ();
                     entity.refreshPositionAndAngles(e, g, h, entity.yaw, entity.pitch);
                  }

                  this.world.playSound((PlayerEntity)null, blockPos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  return this.world.spawnEntity(entity);
               }
            } else {
               return false;
            }
         }
      }
   }

   private void ageBee(int ticks, BeeEntity bee) {
      int i = bee.getBreedingAge();
      if (i < 0) {
         bee.setBreedingAge(Math.min(0, i + ticks));
      } else if (i > 0) {
         bee.setBreedingAge(Math.max(0, i - ticks));
      }

      bee.setLoveTicks(Math.max(0, bee.getLoveTicks() - ticks));
      bee.resetPollinationTicks();
   }

   private boolean hasFlowerPos() {
      return this.flowerPos != null;
   }

   private void tickBees() {
      Iterator<BeehiveBlockEntity.Bee> iterator = this.bees.iterator();

      BeehiveBlockEntity.Bee bee;
      for(BlockState blockState = this.getCachedState(); iterator.hasNext(); bee.ticksInHive++) {
         bee = (BeehiveBlockEntity.Bee)iterator.next();
         if (bee.ticksInHive > bee.minOccupationTicks) {
            BeehiveBlockEntity.BeeState beeState = bee.entityData.getBoolean("HasNectar") ? BeehiveBlockEntity.BeeState.HONEY_DELIVERED : BeehiveBlockEntity.BeeState.BEE_RELEASED;
            if (this.releaseBee(blockState, bee, (List)null, beeState)) {
               iterator.remove();
            }
         }
      }

   }

   public void tick() {
      if (!this.world.isClient) {
         this.tickBees();
         BlockPos blockPos = this.getPos();
         if (this.bees.size() > 0 && this.world.getRandom().nextDouble() < 0.005D) {
            double d = (double)blockPos.getX() + 0.5D;
            double e = (double)blockPos.getY();
            double f = (double)blockPos.getZ() + 0.5D;
            this.world.playSound((PlayerEntity)null, d, e, f, SoundEvents.BLOCK_BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         this.sendDebugData();
      }
   }

   public void fromTag(BlockState state, NbtCompound tag) {
      super.fromTag(state, tag);
      this.bees.clear();
      NbtList nbtList = tag.getList("Bees", 10);

      for(int i = 0; i < nbtList.size(); ++i) {
         NbtCompound nbtCompound = nbtList.getCompound(i);
         BeehiveBlockEntity.Bee bee = new BeehiveBlockEntity.Bee(nbtCompound.getCompound("EntityData"), nbtCompound.getInt("TicksInHive"), nbtCompound.getInt("MinOccupationTicks"));
         this.bees.add(bee);
      }

      this.flowerPos = null;
      if (tag.contains("FlowerPos")) {
         this.flowerPos = NbtHelper.toBlockPos(tag.getCompound("FlowerPos"));
      }

   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.put("Bees", this.getBees());
      if (this.hasFlowerPos()) {
         nbt.put("FlowerPos", NbtHelper.fromBlockPos(this.flowerPos));
      }

      return nbt;
   }

   public NbtList getBees() {
      NbtList nbtList = new NbtList();
      Iterator var2 = this.bees.iterator();

      while(var2.hasNext()) {
         BeehiveBlockEntity.Bee bee = (BeehiveBlockEntity.Bee)var2.next();
         bee.entityData.remove("UUID");
         NbtCompound nbtCompound = new NbtCompound();
         nbtCompound.put("EntityData", bee.entityData);
         nbtCompound.putInt("TicksInHive", bee.ticksInHive);
         nbtCompound.putInt("MinOccupationTicks", bee.minOccupationTicks);
         nbtList.add(nbtCompound);
      }

      return nbtList;
   }

   static class Bee {
      private final NbtCompound entityData;
      private int ticksInHive;
      private final int minOccupationTicks;

      private Bee(NbtCompound entityData, int ticksInHive, int minOccupationTicks) {
         entityData.remove("UUID");
         this.entityData = entityData;
         this.ticksInHive = ticksInHive;
         this.minOccupationTicks = minOccupationTicks;
      }
   }

   public static enum BeeState {
      HONEY_DELIVERED,
      BEE_RELEASED,
      EMERGENCY;
   }
}
