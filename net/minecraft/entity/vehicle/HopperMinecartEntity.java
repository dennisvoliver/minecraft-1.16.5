package net.minecraft.entity.vehicle;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class HopperMinecartEntity extends StorageMinecartEntity implements Hopper {
   private boolean enabled = true;
   private int transferCooldown = -1;
   private final BlockPos currentBlockPos;

   public HopperMinecartEntity(EntityType<? extends HopperMinecartEntity> entityType, World world) {
      super(entityType, world);
      this.currentBlockPos = BlockPos.ORIGIN;
   }

   public HopperMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.HOPPER_MINECART, x, y, z, world);
      this.currentBlockPos = BlockPos.ORIGIN;
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.HOPPER;
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.HOPPER.getDefaultState();
   }

   public int getDefaultBlockOffset() {
      return 1;
   }

   public int size() {
      return 5;
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
      boolean bl = !powered;
      if (bl != this.isEnabled()) {
         this.setEnabled(bl);
      }

   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public World getWorld() {
      return this.world;
   }

   public double getHopperX() {
      return this.getX();
   }

   public double getHopperY() {
      return this.getY() + 0.5D;
   }

   public double getHopperZ() {
      return this.getZ();
   }

   public void tick() {
      super.tick();
      if (!this.world.isClient && this.isAlive() && this.isEnabled()) {
         BlockPos blockPos = this.getBlockPos();
         if (blockPos.equals(this.currentBlockPos)) {
            --this.transferCooldown;
         } else {
            this.setTransferCooldown(0);
         }

         if (!this.isCoolingDown()) {
            this.setTransferCooldown(0);
            if (this.canOperate()) {
               this.setTransferCooldown(4);
               this.markDirty();
            }
         }
      }

   }

   public boolean canOperate() {
      if (HopperBlockEntity.extract(this)) {
         return true;
      } else {
         List<ItemEntity> list = this.world.getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(0.25D, 0.0D, 0.25D), EntityPredicates.VALID_ENTITY);
         if (!list.isEmpty()) {
            HopperBlockEntity.extract(this, (ItemEntity)list.get(0));
         }

         return false;
      }
   }

   public void dropItems(DamageSource damageSource) {
      super.dropItems(damageSource);
      if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
         this.dropItem(Blocks.HOPPER);
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("TransferCooldown", this.transferCooldown);
      nbt.putBoolean("Enabled", this.enabled);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.transferCooldown = nbt.getInt("TransferCooldown");
      this.enabled = nbt.contains("Enabled") ? nbt.getBoolean("Enabled") : true;
   }

   public void setTransferCooldown(int cooldown) {
      this.transferCooldown = cooldown;
   }

   public boolean isCoolingDown() {
      return this.transferCooldown > 0;
   }

   public ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new HopperScreenHandler(syncId, playerInventory, this);
   }
}
