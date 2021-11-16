package net.minecraft.inventory;

import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class EnderChestInventory extends SimpleInventory {
   private EnderChestBlockEntity activeBlockEntity;

   public EnderChestInventory() {
      super(27);
   }

   public void setActiveBlockEntity(EnderChestBlockEntity blockEntity) {
      this.activeBlockEntity = blockEntity;
   }

   public void readNbtList(NbtList nbtList) {
      int j;
      for(j = 0; j < this.size(); ++j) {
         this.setStack(j, ItemStack.EMPTY);
      }

      for(j = 0; j < nbtList.size(); ++j) {
         NbtCompound nbtCompound = nbtList.getCompound(j);
         int k = nbtCompound.getByte("Slot") & 255;
         if (k >= 0 && k < this.size()) {
            this.setStack(k, ItemStack.fromNbt(nbtCompound));
         }
      }

   }

   public NbtList toNbtList() {
      NbtList nbtList = new NbtList();

      for(int i = 0; i < this.size(); ++i) {
         ItemStack itemStack = this.getStack(i);
         if (!itemStack.isEmpty()) {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);
            itemStack.writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
         }
      }

      return nbtList;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return this.activeBlockEntity != null && !this.activeBlockEntity.canPlayerUse(player) ? false : super.canPlayerUse(player);
   }

   public void onOpen(PlayerEntity player) {
      if (this.activeBlockEntity != null) {
         this.activeBlockEntity.onOpen();
      }

      super.onOpen(player);
   }

   public void onClose(PlayerEntity player) {
      if (this.activeBlockEntity != null) {
         this.activeBlockEntity.onClose();
      }

      super.onClose(player);
      this.activeBlockEntity = null;
   }
}
