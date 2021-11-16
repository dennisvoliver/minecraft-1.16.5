package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookItem extends Item {
   public EnchantedBookItem(Item.Settings settings) {
      super(settings);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public static NbtList getEnchantmentNbt(ItemStack stack) {
      NbtCompound nbtCompound = stack.getTag();
      return nbtCompound != null ? nbtCompound.getList("StoredEnchantments", 10) : new NbtList();
   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      ItemStack.appendEnchantments(tooltip, getEnchantmentNbt(stack));
   }

   public static void addEnchantment(ItemStack stack, EnchantmentLevelEntry entry) {
      NbtList nbtList = getEnchantmentNbt(stack);
      boolean bl = true;
      Identifier identifier = Registry.ENCHANTMENT.getId(entry.enchantment);

      for(int i = 0; i < nbtList.size(); ++i) {
         NbtCompound nbtCompound = nbtList.getCompound(i);
         Identifier identifier2 = Identifier.tryParse(nbtCompound.getString("id"));
         if (identifier2 != null && identifier2.equals(identifier)) {
            if (nbtCompound.getInt("lvl") < entry.level) {
               nbtCompound.putShort("lvl", (short)entry.level);
            }

            bl = false;
            break;
         }
      }

      if (bl) {
         NbtCompound nbtCompound2 = new NbtCompound();
         nbtCompound2.putString("id", String.valueOf(identifier));
         nbtCompound2.putShort("lvl", (short)entry.level);
         nbtList.add(nbtCompound2);
      }

      stack.getOrCreateTag().put("StoredEnchantments", nbtList);
   }

   public static ItemStack forEnchantment(EnchantmentLevelEntry info) {
      ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(itemStack, info);
      return itemStack;
   }

   public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
      Iterator var3;
      Enchantment enchantment;
      if (group == ItemGroup.SEARCH) {
         var3 = Registry.ENCHANTMENT.iterator();

         while(true) {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               enchantment = (Enchantment)var3.next();
            } while(enchantment.type == null);

            for(int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
               stacks.add(forEnchantment(new EnchantmentLevelEntry(enchantment, i)));
            }
         }
      } else if (group.getEnchantments().length != 0) {
         var3 = Registry.ENCHANTMENT.iterator();

         while(var3.hasNext()) {
            enchantment = (Enchantment)var3.next();
            if (group.containsEnchantments(enchantment.type)) {
               stacks.add(forEnchantment(new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel())));
            }
         }
      }

   }
}
