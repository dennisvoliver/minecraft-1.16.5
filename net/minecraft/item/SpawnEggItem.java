package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnEggItem extends Item {
   private static final Map<EntityType<?>, SpawnEggItem> SPAWN_EGGS = Maps.newIdentityHashMap();
   private final int primaryColor;
   private final int secondaryColor;
   private final EntityType<?> type;

   public SpawnEggItem(EntityType<?> type, int primaryColor, int secondaryColor, Item.Settings settings) {
      super(settings);
      this.type = type;
      this.primaryColor = primaryColor;
      this.secondaryColor = secondaryColor;
      SPAWN_EGGS.put(type, this);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      if (!(world instanceof ServerWorld)) {
         return ActionResult.SUCCESS;
      } else {
         ItemStack itemStack = context.getStack();
         BlockPos blockPos = context.getBlockPos();
         Direction direction = context.getSide();
         BlockState blockState = world.getBlockState(blockPos);
         if (blockState.isOf(Blocks.SPAWNER)) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof MobSpawnerBlockEntity) {
               MobSpawnerLogic mobSpawnerLogic = ((MobSpawnerBlockEntity)blockEntity).getLogic();
               EntityType<?> entityType = this.getEntityType(itemStack.getTag());
               mobSpawnerLogic.setEntityId(entityType);
               blockEntity.markDirty();
               world.updateListeners(blockPos, blockState, blockState, 3);
               itemStack.decrement(1);
               return ActionResult.CONSUME;
            }
         }

         BlockPos blockPos3;
         if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
            blockPos3 = blockPos;
         } else {
            blockPos3 = blockPos.offset(direction);
         }

         EntityType<?> entityType2 = this.getEntityType(itemStack.getTag());
         if (entityType2.spawnFromItemStack((ServerWorld)world, itemStack, context.getPlayer(), blockPos3, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockPos, blockPos3) && direction == Direction.UP) != null) {
            itemStack.decrement(1);
         }

         return ActionResult.CONSUME;
      }
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
      if (hitResult.getType() != HitResult.Type.BLOCK) {
         return TypedActionResult.pass(itemStack);
      } else if (!(world instanceof ServerWorld)) {
         return TypedActionResult.success(itemStack);
      } else {
         BlockHitResult blockHitResult = (BlockHitResult)hitResult;
         BlockPos blockPos = blockHitResult.getBlockPos();
         if (!(world.getBlockState(blockPos).getBlock() instanceof FluidBlock)) {
            return TypedActionResult.pass(itemStack);
         } else if (world.canPlayerModifyAt(user, blockPos) && user.canPlaceOn(blockPos, blockHitResult.getSide(), itemStack)) {
            EntityType<?> entityType = this.getEntityType(itemStack.getTag());
            if (entityType.spawnFromItemStack((ServerWorld)world, itemStack, user, blockPos, SpawnReason.SPAWN_EGG, false, false) == null) {
               return TypedActionResult.pass(itemStack);
            } else {
               if (!user.abilities.creativeMode) {
                  itemStack.decrement(1);
               }

               user.incrementStat(Stats.USED.getOrCreateStat(this));
               return TypedActionResult.consume(itemStack);
            }
         } else {
            return TypedActionResult.fail(itemStack);
         }
      }
   }

   public boolean isOfSameEntityType(@Nullable NbtCompound nbt, EntityType<?> type) {
      return Objects.equals(this.getEntityType(nbt), type);
   }

   @Environment(EnvType.CLIENT)
   public int getColor(int num) {
      return num == 0 ? this.primaryColor : this.secondaryColor;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static SpawnEggItem forEntity(@Nullable EntityType<?> type) {
      return (SpawnEggItem)SPAWN_EGGS.get(type);
   }

   public static Iterable<SpawnEggItem> getAll() {
      return Iterables.unmodifiableIterable((Iterable)SPAWN_EGGS.values());
   }

   public EntityType<?> getEntityType(@Nullable NbtCompound nbt) {
      if (nbt != null && nbt.contains("EntityTag", 10)) {
         NbtCompound nbtCompound = nbt.getCompound("EntityTag");
         if (nbtCompound.contains("id", 8)) {
            return (EntityType)EntityType.get(nbtCompound.getString("id")).orElse(this.type);
         }
      }

      return this.type;
   }

   public Optional<MobEntity> spawnBaby(PlayerEntity user, MobEntity entity, EntityType<? extends MobEntity> entityType, ServerWorld world, Vec3d pos, ItemStack stack) {
      if (!this.isOfSameEntityType(stack.getTag(), entityType)) {
         return Optional.empty();
      } else {
         Object mobEntity2;
         if (entity instanceof PassiveEntity) {
            mobEntity2 = ((PassiveEntity)entity).createChild(world, (PassiveEntity)entity);
         } else {
            mobEntity2 = (MobEntity)entityType.create(world);
         }

         if (mobEntity2 == null) {
            return Optional.empty();
         } else {
            ((MobEntity)mobEntity2).setBaby(true);
            if (!((MobEntity)mobEntity2).isBaby()) {
               return Optional.empty();
            } else {
               ((MobEntity)mobEntity2).refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
               world.spawnEntityAndPassengers((Entity)mobEntity2);
               if (stack.hasCustomName()) {
                  ((MobEntity)mobEntity2).setCustomName(stack.getName());
               }

               if (!user.abilities.creativeMode) {
                  stack.decrement(1);
               }

               return Optional.of(mobEntity2);
            }
         }
      }
   }
}
