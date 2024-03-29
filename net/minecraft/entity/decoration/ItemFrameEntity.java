package net.minecraft.entity.decoration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ItemFrameEntity extends AbstractDecorationEntity {
   private static final Logger ITEM_FRAME_LOGGER = LogManager.getLogger();
   private static final TrackedData<ItemStack> ITEM_STACK;
   private static final TrackedData<Integer> ROTATION;
   private float itemDropChance = 1.0F;
   private boolean fixed;

   public ItemFrameEntity(EntityType<? extends ItemFrameEntity> entityType, World world) {
      super(entityType, world);
   }

   public ItemFrameEntity(World world, BlockPos pos, Direction facing) {
      super(EntityType.ITEM_FRAME, world, pos);
      this.setFacing(facing);
   }

   protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
      return 0.0F;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(ITEM_STACK, ItemStack.EMPTY);
      this.getDataTracker().startTracking(ROTATION, 0);
   }

   protected void setFacing(Direction facing) {
      Validate.notNull(facing);
      this.facing = facing;
      if (facing.getAxis().isHorizontal()) {
         this.pitch = 0.0F;
         this.yaw = (float)(this.facing.getHorizontal() * 90);
      } else {
         this.pitch = (float)(-90 * facing.getDirection().offset());
         this.yaw = 0.0F;
      }

      this.prevPitch = this.pitch;
      this.prevYaw = this.yaw;
      this.updateAttachmentPosition();
   }

   protected void updateAttachmentPosition() {
      if (this.facing != null) {
         double d = 0.46875D;
         double e = (double)this.attachmentPos.getX() + 0.5D - (double)this.facing.getOffsetX() * 0.46875D;
         double f = (double)this.attachmentPos.getY() + 0.5D - (double)this.facing.getOffsetY() * 0.46875D;
         double g = (double)this.attachmentPos.getZ() + 0.5D - (double)this.facing.getOffsetZ() * 0.46875D;
         this.setPos(e, f, g);
         double h = (double)this.getWidthPixels();
         double i = (double)this.getHeightPixels();
         double j = (double)this.getWidthPixels();
         Direction.Axis axis = this.facing.getAxis();
         switch(axis) {
         case X:
            h = 1.0D;
            break;
         case Y:
            i = 1.0D;
            break;
         case Z:
            j = 1.0D;
         }

         h /= 32.0D;
         i /= 32.0D;
         j /= 32.0D;
         this.setBoundingBox(new Box(e - h, f - i, g - j, e + h, f + i, g + j));
      }
   }

   public boolean canStayAttached() {
      if (this.fixed) {
         return true;
      } else if (!this.world.isSpaceEmpty(this)) {
         return false;
      } else {
         BlockState blockState = this.world.getBlockState(this.attachmentPos.offset(this.facing.getOpposite()));
         return blockState.getMaterial().isSolid() || this.facing.getAxis().isHorizontal() && AbstractRedstoneGateBlock.isRedstoneGate(blockState) ? this.world.getOtherEntities(this, this.getBoundingBox(), PREDICATE).isEmpty() : false;
      }
   }

   public void move(MovementType movementType, Vec3d movement) {
      if (!this.fixed) {
         super.move(movementType, movement);
      }

   }

   public void addVelocity(double deltaX, double deltaY, double deltaZ) {
      if (!this.fixed) {
         super.addVelocity(deltaX, deltaY, deltaZ);
      }

   }

   public float getTargetingMargin() {
      return 0.0F;
   }

   public void kill() {
      this.removeFromFrame(this.getHeldItemStack());
      super.kill();
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.fixed) {
         return source != DamageSource.OUT_OF_WORLD && !source.isSourceCreativePlayer() ? false : super.damage(source, amount);
      } else if (this.isInvulnerableTo(source)) {
         return false;
      } else if (!source.isExplosive() && !this.getHeldItemStack().isEmpty()) {
         if (!this.world.isClient) {
            this.dropHeldStack(source.getAttacker(), false);
            this.playSound(SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
         }

         return true;
      } else {
         return super.damage(source, amount);
      }
   }

   public int getWidthPixels() {
      return 12;
   }

   public int getHeightPixels() {
      return 12;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = 16.0D;
      d *= 64.0D * getRenderDistanceMultiplier();
      return distance < d * d;
   }

   public void onBreak(@Nullable Entity entity) {
      this.playSound(SoundEvents.ENTITY_ITEM_FRAME_BREAK, 1.0F, 1.0F);
      this.dropHeldStack(entity, true);
   }

   public void onPlace() {
      this.playSound(SoundEvents.ENTITY_ITEM_FRAME_PLACE, 1.0F, 1.0F);
   }

   private void dropHeldStack(@Nullable Entity entity, boolean alwaysDrop) {
      if (!this.fixed) {
         ItemStack itemStack = this.getHeldItemStack();
         this.setHeldItemStack(ItemStack.EMPTY);
         if (!this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            if (entity == null) {
               this.removeFromFrame(itemStack);
            }

         } else {
            if (entity instanceof PlayerEntity) {
               PlayerEntity playerEntity = (PlayerEntity)entity;
               if (playerEntity.abilities.creativeMode) {
                  this.removeFromFrame(itemStack);
                  return;
               }
            }

            if (alwaysDrop) {
               this.dropItem(Items.ITEM_FRAME);
            }

            if (!itemStack.isEmpty()) {
               itemStack = itemStack.copy();
               this.removeFromFrame(itemStack);
               if (this.random.nextFloat() < this.itemDropChance) {
                  this.dropStack(itemStack);
               }
            }

         }
      }
   }

   private void removeFromFrame(ItemStack map) {
      if (map.getItem() == Items.FILLED_MAP) {
         MapState mapState = FilledMapItem.getOrCreateMapState(map, this.world);
         mapState.removeFrame(this.attachmentPos, this.getEntityId());
         mapState.setDirty(true);
      }

      map.setHolder((Entity)null);
   }

   public ItemStack getHeldItemStack() {
      return (ItemStack)this.getDataTracker().get(ITEM_STACK);
   }

   public void setHeldItemStack(ItemStack stack) {
      this.setHeldItemStack(stack, true);
   }

   public void setHeldItemStack(ItemStack value, boolean update) {
      if (!value.isEmpty()) {
         value = value.copy();
         value.setCount(1);
         value.setHolder(this);
      }

      this.getDataTracker().set(ITEM_STACK, value);
      if (!value.isEmpty()) {
         this.playSound(SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
      }

      if (update && this.attachmentPos != null) {
         this.world.updateComparators(this.attachmentPos, Blocks.AIR);
      }

   }

   public boolean equip(int slot, ItemStack item) {
      if (slot == 0) {
         this.setHeldItemStack(item);
         return true;
      } else {
         return false;
      }
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (data.equals(ITEM_STACK)) {
         ItemStack itemStack = this.getHeldItemStack();
         if (!itemStack.isEmpty() && itemStack.getFrame() != this) {
            itemStack.setHolder(this);
         }
      }

   }

   public int getRotation() {
      return (Integer)this.getDataTracker().get(ROTATION);
   }

   public void setRotation(int value) {
      this.setRotation(value, true);
   }

   private void setRotation(int value, boolean updateComparators) {
      this.getDataTracker().set(ROTATION, value % 8);
      if (updateComparators && this.attachmentPos != null) {
         this.world.updateComparators(this.attachmentPos, Blocks.AIR);
      }

   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (!this.getHeldItemStack().isEmpty()) {
         nbt.put("Item", this.getHeldItemStack().writeNbt(new NbtCompound()));
         nbt.putByte("ItemRotation", (byte)this.getRotation());
         nbt.putFloat("ItemDropChance", this.itemDropChance);
      }

      nbt.putByte("Facing", (byte)this.facing.getId());
      nbt.putBoolean("Invisible", this.isInvisible());
      nbt.putBoolean("Fixed", this.fixed);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      NbtCompound nbtCompound = nbt.getCompound("Item");
      if (nbtCompound != null && !nbtCompound.isEmpty()) {
         ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
         if (itemStack.isEmpty()) {
            ITEM_FRAME_LOGGER.warn((String)"Unable to load item from: {}", (Object)nbtCompound);
         }

         ItemStack itemStack2 = this.getHeldItemStack();
         if (!itemStack2.isEmpty() && !ItemStack.areEqual(itemStack, itemStack2)) {
            this.removeFromFrame(itemStack2);
         }

         this.setHeldItemStack(itemStack, false);
         this.setRotation(nbt.getByte("ItemRotation"), false);
         if (nbt.contains("ItemDropChance", 99)) {
            this.itemDropChance = nbt.getFloat("ItemDropChance");
         }
      }

      this.setFacing(Direction.byId(nbt.getByte("Facing")));
      this.setInvisible(nbt.getBoolean("Invisible"));
      this.fixed = nbt.getBoolean("Fixed");
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.getStackInHand(hand);
      boolean bl = !this.getHeldItemStack().isEmpty();
      boolean bl2 = !itemStack.isEmpty();
      if (this.fixed) {
         return ActionResult.PASS;
      } else if (!this.world.isClient) {
         if (!bl) {
            if (bl2 && !this.removed) {
               this.setHeldItemStack(itemStack);
               if (!player.abilities.creativeMode) {
                  itemStack.decrement(1);
               }
            }
         } else {
            this.playSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
            this.setRotation(this.getRotation() + 1);
         }

         return ActionResult.CONSUME;
      } else {
         return !bl && !bl2 ? ActionResult.PASS : ActionResult.SUCCESS;
      }
   }

   public int getComparatorPower() {
      return this.getHeldItemStack().isEmpty() ? 0 : this.getRotation() % 8 + 1;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, this.getType(), this.facing.getId(), this.getDecorationBlockPos());
   }

   static {
      ITEM_STACK = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
      ROTATION = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
