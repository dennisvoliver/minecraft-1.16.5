package net.minecraft.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class ContainerLock {
   /**
    * An empty container lock that can always be opened.
    */
   public static final ContainerLock EMPTY = new ContainerLock("");
   private final String key;

   public ContainerLock(String key) {
      this.key = key;
   }

   /**
    * Returns true if this lock can be opened with the key item stack.
    * <p>
    * An item stack is a valid key if the stack name matches the key string of this lock,
    * or if the key string is empty.
    * 
    * @param stack the key item stack
    */
   public boolean canOpen(ItemStack stack) {
      return this.key.isEmpty() || !stack.isEmpty() && stack.hasCustomName() && this.key.equals(stack.getName().getString());
   }

   /**
    * Inserts the key string of this lock into the {@code Lock} key of the NBT compound.
    */
   public void writeNbt(NbtCompound nbt) {
      if (!this.key.isEmpty()) {
         nbt.putString("Lock", this.key);
      }

   }

   /**
    * Creates a new {@code ContainerLock} from the {@code Lock} key of the NBT compound.
    * <p>
    * If the {@code Lock} key is not present, returns an empty lock.
    */
   public static ContainerLock fromNbt(NbtCompound nbt) {
      return nbt.contains("Lock", 8) ? new ContainerLock(nbt.getString("Lock")) : EMPTY;
   }
}
