package net.minecraft.client.option;

import com.google.common.collect.ForwardingList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class HotbarStorageEntry extends ForwardingList<ItemStack> {
   private final DefaultedList<ItemStack> delegate;

   public HotbarStorageEntry() {
      this.delegate = DefaultedList.ofSize(PlayerInventory.getHotbarSize(), ItemStack.EMPTY);
   }

   protected List<ItemStack> delegate() {
      return this.delegate;
   }

   public NbtList toNbtList() {
      NbtList nbtList = new NbtList();
      Iterator var2 = this.delegate().iterator();

      while(var2.hasNext()) {
         ItemStack itemStack = (ItemStack)var2.next();
         nbtList.add(itemStack.writeNbt(new NbtCompound()));
      }

      return nbtList;
   }

   public void readNbtList(NbtList list) {
      List<ItemStack> list2 = this.delegate();

      for(int i = 0; i < list2.size(); ++i) {
         list2.set(i, ItemStack.fromNbt(list.getCompound(i)));
      }

   }

   public boolean isEmpty() {
      Iterator var1 = this.delegate().iterator();

      ItemStack itemStack;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         itemStack = (ItemStack)var1.next();
      } while(itemStack.isEmpty());

      return false;
   }
}
