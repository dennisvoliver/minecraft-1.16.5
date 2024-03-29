package net.minecraft.structure.pool;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SinglePoolElement extends StructurePoolElement {
   private static final Codec<Either<Identifier, Structure>> field_24951;
   public static final Codec<SinglePoolElement> field_24952;
   protected final Either<Identifier, Structure> field_24015;
   protected final Supplier<StructureProcessorList> processors;

   private static <T> DataResult<T> method_28877(Either<Identifier, Structure> either, DynamicOps<T> dynamicOps, T object) {
      Optional<Identifier> optional = either.left();
      return !optional.isPresent() ? DataResult.error("Can not serialize a runtime pool element") : Identifier.CODEC.encode(optional.get(), dynamicOps, object);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Supplier<StructureProcessorList>> method_28880() {
      return StructureProcessorType.REGISTRY_CODEC.fieldOf("processors").forGetter((singlePoolElement) -> {
         return singlePoolElement.processors;
      });
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<Identifier, Structure>> method_28882() {
      return field_24951.fieldOf("location").forGetter((singlePoolElement) -> {
         return singlePoolElement.field_24015;
      });
   }

   protected SinglePoolElement(Either<Identifier, Structure> either, Supplier<StructureProcessorList> supplier, StructurePool.Projection projection) {
      super(projection);
      this.field_24015 = either;
      this.processors = supplier;
   }

   public SinglePoolElement(Structure structure) {
      this(Either.right(structure), () -> {
         return StructureProcessorLists.EMPTY;
      }, StructurePool.Projection.RIGID);
   }

   private Structure method_27233(StructureManager structureManager) {
      return (Structure)this.field_24015.map(structureManager::getStructureOrBlank, Function.identity());
   }

   public List<Structure.StructureBlockInfo> getDataStructureBlocks(StructureManager structureManager, BlockPos pos, BlockRotation rotation, boolean mirroredAndRotated) {
      Structure structure = this.method_27233(structureManager);
      List<Structure.StructureBlockInfo> list = structure.getInfosForBlock(pos, (new StructurePlacementData()).setRotation(rotation), Blocks.STRUCTURE_BLOCK, mirroredAndRotated);
      List<Structure.StructureBlockInfo> list2 = Lists.newArrayList();
      Iterator var8 = list.iterator();

      while(var8.hasNext()) {
         Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var8.next();
         if (structureBlockInfo.tag != null) {
            StructureBlockMode structureBlockMode = StructureBlockMode.valueOf(structureBlockInfo.tag.getString("mode"));
            if (structureBlockMode == StructureBlockMode.DATA) {
               list2.add(structureBlockInfo);
            }
         }
      }

      return list2;
   }

   public List<Structure.StructureBlockInfo> getStructureBlockInfos(StructureManager structureManager, BlockPos pos, BlockRotation rotation, Random random) {
      Structure structure = this.method_27233(structureManager);
      List<Structure.StructureBlockInfo> list = structure.getInfosForBlock(pos, (new StructurePlacementData()).setRotation(rotation), Blocks.JIGSAW, true);
      Collections.shuffle(list, random);
      return list;
   }

   public BlockBox getBoundingBox(StructureManager structureManager, BlockPos pos, BlockRotation rotation) {
      Structure structure = this.method_27233(structureManager);
      return structure.calculateBoundingBox((new StructurePlacementData()).setRotation(rotation), pos);
   }

   public boolean generate(StructureManager structureManager, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, BlockPos pos, BlockPos blockPos, BlockRotation rotation, BlockBox box, Random random, boolean keepJigsaws) {
      Structure structure = this.method_27233(structureManager);
      StructurePlacementData structurePlacementData = this.createPlacementData(rotation, box, keepJigsaws);
      if (!structure.place(world, pos, blockPos, structurePlacementData, random, 18)) {
         return false;
      } else {
         List<Structure.StructureBlockInfo> list = Structure.process(world, pos, blockPos, structurePlacementData, this.getDataStructureBlocks(structureManager, pos, rotation, false));
         Iterator var14 = list.iterator();

         while(var14.hasNext()) {
            Structure.StructureBlockInfo structureBlockInfo = (Structure.StructureBlockInfo)var14.next();
            this.method_16756(world, structureBlockInfo, pos, rotation, random, box);
         }

         return true;
      }
   }

   protected StructurePlacementData createPlacementData(BlockRotation rotation, BlockBox box, boolean keepJigsaws) {
      StructurePlacementData structurePlacementData = new StructurePlacementData();
      structurePlacementData.setBoundingBox(box);
      structurePlacementData.setRotation(rotation);
      structurePlacementData.setUpdateNeighbors(true);
      structurePlacementData.setIgnoreEntities(false);
      structurePlacementData.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
      structurePlacementData.method_27264(true);
      if (!keepJigsaws) {
         structurePlacementData.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
      }

      ((StructureProcessorList)this.processors.get()).getList().forEach(structurePlacementData::addProcessor);
      this.getProjection().getProcessors().forEach(structurePlacementData::addProcessor);
      return structurePlacementData;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.SINGLE_POOL_ELEMENT;
   }

   public String toString() {
      return "Single[" + this.field_24015 + "]";
   }

   static {
      field_24951 = Codec.of(SinglePoolElement::method_28877, Identifier.CODEC.map(Either::left));
      field_24952 = RecordCodecBuilder.create((instance) -> {
         return instance.group(method_28882(), method_28880(), method_28883()).apply(instance, (Function3)(SinglePoolElement::new));
      });
   }
}
