package net.minecraft.structure.processor;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BlockRotStructureProcessor extends StructureProcessor {
   public static final Codec<BlockRotStructureProcessor> CODEC;
   private final float integrity;

   public BlockRotStructureProcessor(float integrity) {
      this.integrity = integrity;
   }

   @Nullable
   public Structure.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos blockPos, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData structurePlacementData) {
      Random random = structurePlacementData.getRandom(structureBlockInfo2.pos);
      return !(this.integrity >= 1.0F) && !(random.nextFloat() <= this.integrity) ? null : structureBlockInfo2;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_ROT;
   }

   static {
      CODEC = Codec.FLOAT.fieldOf("integrity").orElse(1.0F).xmap(BlockRotStructureProcessor::new, (blockRotStructureProcessor) -> {
         return blockRotStructureProcessor.integrity;
      }).codec();
   }
}
