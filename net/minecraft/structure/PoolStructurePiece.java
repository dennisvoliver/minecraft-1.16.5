package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoolStructurePiece extends StructurePiece {
   private static final Logger field_24991 = LogManager.getLogger();
   protected final StructurePoolElement poolElement;
   protected BlockPos pos;
   private final int groundLevelDelta;
   protected final BlockRotation rotation;
   private final List<JigsawJunction> junctions = Lists.newArrayList();
   private final StructureManager structureManager;

   public PoolStructurePiece(StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, BlockRotation blockRotation, BlockBox blockBox) {
      super(StructurePieceType.JIGSAW, 0);
      this.structureManager = structureManager;
      this.poolElement = structurePoolElement;
      this.pos = blockPos;
      this.groundLevelDelta = i;
      this.rotation = blockRotation;
      this.boundingBox = blockBox;
   }

   public PoolStructurePiece(StructureManager manager, NbtCompound tag) {
      super(StructurePieceType.JIGSAW, tag);
      this.structureManager = manager;
      this.pos = new BlockPos(tag.getInt("PosX"), tag.getInt("PosY"), tag.getInt("PosZ"));
      this.groundLevelDelta = tag.getInt("ground_level_delta");
      DataResult var10001 = StructurePoolElement.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("pool_element"));
      Logger var10002 = field_24991;
      var10002.getClass();
      this.poolElement = (StructurePoolElement)var10001.resultOrPartial(var10002::error).orElse(EmptyPoolElement.INSTANCE);
      this.rotation = BlockRotation.valueOf(tag.getString("rotation"));
      this.boundingBox = this.poolElement.getBoundingBox(manager, this.pos, this.rotation);
      NbtList nbtList = tag.getList("junctions", 10);
      this.junctions.clear();
      nbtList.forEach((nbtElement) -> {
         this.junctions.add(JigsawJunction.method_28873(new Dynamic(NbtOps.INSTANCE, nbtElement)));
      });
   }

   protected void toNbt(NbtCompound tag) {
      tag.putInt("PosX", this.pos.getX());
      tag.putInt("PosY", this.pos.getY());
      tag.putInt("PosZ", this.pos.getZ());
      tag.putInt("ground_level_delta", this.groundLevelDelta);
      DataResult var10000 = StructurePoolElement.CODEC.encodeStart(NbtOps.INSTANCE, this.poolElement);
      Logger var10001 = field_24991;
      var10001.getClass();
      var10000.resultOrPartial(var10001::error).ifPresent((nbtElement) -> {
         tag.put("pool_element", nbtElement);
      });
      tag.putString("rotation", this.rotation.name());
      NbtList nbtList = new NbtList();
      Iterator var3 = this.junctions.iterator();

      while(var3.hasNext()) {
         JigsawJunction jigsawJunction = (JigsawJunction)var3.next();
         nbtList.add(jigsawJunction.serialize(NbtOps.INSTANCE).getValue());
      }

      tag.put("junctions", nbtList);
   }

   public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
      return this.generate(world, structureAccessor, chunkGenerator, random, boundingBox, pos, false);
   }

   public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, BlockPos pos, boolean keepJigsaws) {
      return this.poolElement.generate(this.structureManager, world, structureAccessor, chunkGenerator, this.pos, pos, this.rotation, boundingBox, random, keepJigsaws);
   }

   public void translate(int x, int y, int z) {
      super.translate(x, y, z);
      this.pos = this.pos.add(x, y, z);
   }

   public BlockRotation getRotation() {
      return this.rotation;
   }

   public String toString() {
      return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.pos, this.rotation, this.poolElement);
   }

   public StructurePoolElement getPoolElement() {
      return this.poolElement;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getGroundLevelDelta() {
      return this.groundLevelDelta;
   }

   public void addJunction(JigsawJunction junction) {
      this.junctions.add(junction);
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}
