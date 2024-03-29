package net.minecraft.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.SimpleMerchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class MerchantScreenHandler extends ScreenHandler {
   private final Merchant merchant;
   private final MerchantInventory merchantInventory;
   @Environment(EnvType.CLIENT)
   private int levelProgress;
   @Environment(EnvType.CLIENT)
   private boolean leveled;
   @Environment(EnvType.CLIENT)
   private boolean canRefreshTrades;

   public MerchantScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleMerchant(playerInventory.player));
   }

   public MerchantScreenHandler(int syncId, PlayerInventory playerInventory, Merchant merchant) {
      super(ScreenHandlerType.MERCHANT, syncId);
      this.merchant = merchant;
      this.merchantInventory = new MerchantInventory(merchant);
      this.addSlot(new Slot(this.merchantInventory, 0, 136, 37));
      this.addSlot(new Slot(this.merchantInventory, 1, 162, 37));
      this.addSlot(new TradeOutputSlot(playerInventory.player, merchant, this.merchantInventory, 2, 220, 37));

      int k;
      for(k = 0; k < 3; ++k) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j + k * 9 + 9, 108 + j * 18, 84 + k * 18));
         }
      }

      for(k = 0; k < 9; ++k) {
         this.addSlot(new Slot(playerInventory, k, 108 + k * 18, 142));
      }

   }

   @Environment(EnvType.CLIENT)
   public void setCanLevel(boolean canLevel) {
      this.leveled = canLevel;
   }

   public void onContentChanged(Inventory inventory) {
      this.merchantInventory.updateOffers();
      super.onContentChanged(inventory);
   }

   public void setRecipeIndex(int index) {
      this.merchantInventory.setOfferIndex(index);
   }

   public boolean canUse(PlayerEntity player) {
      return this.merchant.getCurrentCustomer() == player;
   }

   @Environment(EnvType.CLIENT)
   public int getExperience() {
      return this.merchant.getExperience();
   }

   @Environment(EnvType.CLIENT)
   public int getMerchantRewardedExperience() {
      return this.merchantInventory.getMerchantRewardedExperience();
   }

   @Environment(EnvType.CLIENT)
   public void setExperienceFromServer(int experience) {
      this.merchant.setExperienceFromServer(experience);
   }

   @Environment(EnvType.CLIENT)
   public int getLevelProgress() {
      return this.levelProgress;
   }

   @Environment(EnvType.CLIENT)
   public void setLevelProgress(int progress) {
      this.levelProgress = progress;
   }

   @Environment(EnvType.CLIENT)
   public void setRefreshTrades(boolean refreshable) {
      this.canRefreshTrades = refreshable;
   }

   @Environment(EnvType.CLIENT)
   public boolean canRefreshTrades() {
      return this.canRefreshTrades;
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return false;
   }

   public ItemStack transferSlot(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasStack()) {
         ItemStack itemStack2 = slot.getStack();
         itemStack = itemStack2.copy();
         if (index == 2) {
            if (!this.insertItem(itemStack2, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickTransfer(itemStack2, itemStack);
            this.playYesSound();
         } else if (index != 0 && index != 1) {
            if (index >= 3 && index < 30) {
               if (!this.insertItem(itemStack2, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 30 && index < 39 && !this.insertItem(itemStack2, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
         } else {
            slot.markDirty();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTakeItem(player, itemStack2);
      }

      return itemStack;
   }

   private void playYesSound() {
      if (!this.merchant.getMerchantWorld().isClient) {
         Entity entity = (Entity)this.merchant;
         this.merchant.getMerchantWorld().playSound(entity.getX(), entity.getY(), entity.getZ(), this.merchant.getYesSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
      }

   }

   public void close(PlayerEntity player) {
      super.close(player);
      this.merchant.setCurrentCustomer((PlayerEntity)null);
      if (!this.merchant.getMerchantWorld().isClient) {
         if (player.isAlive() && (!(player instanceof ServerPlayerEntity) || !((ServerPlayerEntity)player).isDisconnected())) {
            player.inventory.offerOrDrop(player.world, this.merchantInventory.removeStack(0));
            player.inventory.offerOrDrop(player.world, this.merchantInventory.removeStack(1));
         } else {
            ItemStack itemStack = this.merchantInventory.removeStack(0);
            if (!itemStack.isEmpty()) {
               player.dropItem(itemStack, false);
            }

            itemStack = this.merchantInventory.removeStack(1);
            if (!itemStack.isEmpty()) {
               player.dropItem(itemStack, false);
            }
         }

      }
   }

   public void switchTo(int recipeIndex) {
      if (this.getRecipes().size() > recipeIndex) {
         ItemStack itemStack = this.merchantInventory.getStack(0);
         if (!itemStack.isEmpty()) {
            if (!this.insertItem(itemStack, 3, 39, true)) {
               return;
            }

            this.merchantInventory.setStack(0, itemStack);
         }

         ItemStack itemStack2 = this.merchantInventory.getStack(1);
         if (!itemStack2.isEmpty()) {
            if (!this.insertItem(itemStack2, 3, 39, true)) {
               return;
            }

            this.merchantInventory.setStack(1, itemStack2);
         }

         if (this.merchantInventory.getStack(0).isEmpty() && this.merchantInventory.getStack(1).isEmpty()) {
            ItemStack itemStack3 = ((TradeOffer)this.getRecipes().get(recipeIndex)).getAdjustedFirstBuyItem();
            this.autofill(0, itemStack3);
            ItemStack itemStack4 = ((TradeOffer)this.getRecipes().get(recipeIndex)).getSecondBuyItem();
            this.autofill(1, itemStack4);
         }

      }
   }

   private void autofill(int slot, ItemStack stack) {
      if (!stack.isEmpty()) {
         for(int i = 3; i < 39; ++i) {
            ItemStack itemStack = ((Slot)this.slots.get(i)).getStack();
            if (!itemStack.isEmpty() && this.equals(stack, itemStack)) {
               ItemStack itemStack2 = this.merchantInventory.getStack(slot);
               int j = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
               int k = Math.min(stack.getMaxCount() - j, itemStack.getCount());
               ItemStack itemStack3 = itemStack.copy();
               int l = j + k;
               itemStack.decrement(k);
               itemStack3.setCount(l);
               this.merchantInventory.setStack(slot, itemStack3);
               if (l >= stack.getMaxCount()) {
                  break;
               }
            }
         }
      }

   }

   private boolean equals(ItemStack itemStack, ItemStack otherItemStack) {
      return itemStack.getItem() == otherItemStack.getItem() && ItemStack.areTagsEqual(itemStack, otherItemStack);
   }

   @Environment(EnvType.CLIENT)
   public void setOffers(TradeOfferList offers) {
      this.merchant.setOffersFromServer(offers);
   }

   public TradeOfferList getRecipes() {
      return this.merchant.getOffers();
   }

   @Environment(EnvType.CLIENT)
   public boolean isLeveled() {
      return this.leveled;
   }
}
