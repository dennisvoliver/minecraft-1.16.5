package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class SetLoreLootFunction extends ConditionalLootFunction {
   private final boolean replace;
   private final List<Text> lore;
   @Nullable
   private final LootContext.EntityTarget entity;

   public SetLoreLootFunction(LootCondition[] conditions, boolean replace, List<Text> lore, @Nullable LootContext.EntityTarget entity) {
      super(conditions);
      this.replace = replace;
      this.lore = ImmutableList.copyOf((Collection)lore);
      this.entity = entity;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_LORE;
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return this.entity != null ? ImmutableSet.of(this.entity.getParameter()) : ImmutableSet.of();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      NbtList nbtList = this.getLoreForMerge(stack, !this.lore.isEmpty());
      if (nbtList != null) {
         if (this.replace) {
            nbtList.clear();
         }

         UnaryOperator<Text> unaryOperator = SetNameLootFunction.applySourceEntity(context, this.entity);
         this.lore.stream().map(unaryOperator).map(Text.Serializer::toJson).map(NbtString::of).forEach(nbtList::add);
      }

      return stack;
   }

   @Nullable
   private NbtList getLoreForMerge(ItemStack stack, boolean otherLoreExists) {
      NbtCompound nbtCompound3;
      if (stack.hasTag()) {
         nbtCompound3 = stack.getTag();
      } else {
         if (!otherLoreExists) {
            return null;
         }

         nbtCompound3 = new NbtCompound();
         stack.setTag(nbtCompound3);
      }

      NbtCompound nbtCompound6;
      if (nbtCompound3.contains("display", 10)) {
         nbtCompound6 = nbtCompound3.getCompound("display");
      } else {
         if (!otherLoreExists) {
            return null;
         }

         nbtCompound6 = new NbtCompound();
         nbtCompound3.put("display", nbtCompound6);
      }

      if (nbtCompound6.contains("Lore", 9)) {
         return nbtCompound6.getList("Lore", 8);
      } else if (otherLoreExists) {
         NbtList nbtList = new NbtList();
         nbtCompound6.put("Lore", nbtList);
         return nbtList;
      } else {
         return null;
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer<SetLoreLootFunction> {
      public void toJson(JsonObject jsonObject, SetLoreLootFunction setLoreLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)setLoreLootFunction, jsonSerializationContext);
         jsonObject.addProperty("replace", setLoreLootFunction.replace);
         JsonArray jsonArray = new JsonArray();
         Iterator var5 = setLoreLootFunction.lore.iterator();

         while(var5.hasNext()) {
            Text text = (Text)var5.next();
            jsonArray.add(Text.Serializer.toJsonTree(text));
         }

         jsonObject.add("lore", jsonArray);
         if (setLoreLootFunction.entity != null) {
            jsonObject.add("entity", jsonSerializationContext.serialize(setLoreLootFunction.entity));
         }

      }

      public SetLoreLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         boolean bl = JsonHelper.getBoolean(jsonObject, "replace", false);
         List<Text> list = (List)Streams.stream((Iterable)JsonHelper.getArray(jsonObject, "lore")).map(Text.Serializer::fromJson).collect(ImmutableList.toImmutableList());
         LootContext.EntityTarget entityTarget = (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", (Object)null, jsonDeserializationContext, LootContext.EntityTarget.class);
         return new SetLoreLootFunction(lootConditions, bl, list, entityTarget);
      }
   }
}
