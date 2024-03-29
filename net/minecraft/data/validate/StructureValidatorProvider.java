package net.minecraft.data.validate;

import net.minecraft.data.SnbtProvider;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.Structure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureValidatorProvider implements SnbtProvider.Tweaker {
   private static final Logger field_24617 = LogManager.getLogger();

   public NbtCompound write(String name, NbtCompound nbt) {
      return name.startsWith("data/minecraft/structures/") ? internalUpdate(name, addDataVersion(nbt)) : nbt;
   }

   private static NbtCompound addDataVersion(NbtCompound nbt) {
      if (!nbt.contains("DataVersion", 99)) {
         nbt.putInt("DataVersion", 500);
      }

      return nbt;
   }

   private static NbtCompound internalUpdate(String name, NbtCompound nbt) {
      Structure structure = new Structure();
      int i = nbt.getInt("DataVersion");
      int j = true;
      if (i < 2532) {
         field_24617.warn("SNBT Too old, do not forget to update: " + i + " < " + 2532 + ": " + name);
      }

      NbtCompound nbtCompound = NbtHelper.update(Schemas.getFixer(), DataFixTypes.STRUCTURE, nbt, i);
      structure.readNbt(nbtCompound);
      return structure.writeNbt(new NbtCompound());
   }
}
