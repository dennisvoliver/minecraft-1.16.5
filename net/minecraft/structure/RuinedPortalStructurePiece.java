package net.minecraft.structure;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.structure.processor.BlackstoneReplacementStructureProcessor;
import net.minecraft.structure.processor.BlockAgeStructureProcessor;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.LavaSubmergedBlockStructureProcessor;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuinedPortalStructurePiece extends SimpleStructurePiece {
   private static final Logger field_24992 = LogManager.getLogger();
   private final Identifier template;
   private final BlockRotation rotation;
   private final BlockMirror mirror;
   private final RuinedPortalStructurePiece.VerticalPlacement verticalPlacement;
   private final RuinedPortalStructurePiece.Properties properties;

   public RuinedPortalStructurePiece(BlockPos pos, RuinedPortalStructurePiece.VerticalPlacement verticalPlacement, RuinedPortalStructurePiece.Properties properties, Identifier template, Structure structure, BlockRotation rotation, BlockMirror mirror, BlockPos center) {
      super(StructurePieceType.RUINED_PORTAL, 0);
      this.pos = pos;
      this.template = template;
      this.rotation = rotation;
      this.mirror = mirror;
      this.verticalPlacement = verticalPlacement;
      this.properties = properties;
      this.processProperties(structure, center);
   }

   public RuinedPortalStructurePiece(StructureManager manager, NbtCompound tag) {
      super(StructurePieceType.RUINED_PORTAL, tag);
      this.template = new Identifier(tag.getString("Template"));
      this.rotation = BlockRotation.valueOf(tag.getString("Rotation"));
      this.mirror = BlockMirror.valueOf(tag.getString("Mirror"));
      this.verticalPlacement = RuinedPortalStructurePiece.VerticalPlacement.getFromId(tag.getString("VerticalPlacement"));
      DataResult var10001 = RuinedPortalStructurePiece.Properties.CODEC.parse(new Dynamic(NbtOps.INSTANCE, tag.get("Properties")));
      Logger var10003 = field_24992;
      var10003.getClass();
      this.properties = (RuinedPortalStructurePiece.Properties)var10001.getOrThrow(true, var10003::error);
      Structure structure = manager.getStructureOrBlank(this.template);
      this.processProperties(structure, new BlockPos(structure.getSize().getX() / 2, 0, structure.getSize().getZ() / 2));
   }

   protected void toNbt(NbtCompound tag) {
      super.toNbt(tag);
      tag.putString("Template", this.template.toString());
      tag.putString("Rotation", this.rotation.name());
      tag.putString("Mirror", this.mirror.name());
      tag.putString("VerticalPlacement", this.verticalPlacement.getId());
      DataResult var10000 = RuinedPortalStructurePiece.Properties.CODEC.encodeStart(NbtOps.INSTANCE, this.properties);
      Logger var10001 = field_24992;
      var10001.getClass();
      var10000.resultOrPartial(var10001::error).ifPresent((nbtElement) -> {
         tag.put("Properties", nbtElement);
      });
   }

   private void processProperties(Structure structure, BlockPos center) {
      BlockIgnoreStructureProcessor blockIgnoreStructureProcessor = this.properties.airPocket ? BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS : BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS;
      List<StructureProcessorRule> list = Lists.newArrayList();
      list.add(createReplacementRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
      list.add(this.createLavaReplacementRule());
      if (!this.properties.cold) {
         list.add(createReplacementRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
      }

      StructurePlacementData structurePlacementData = (new StructurePlacementData()).setRotation(this.rotation).setMirror(this.mirror).setPosition(center).addProcessor(blockIgnoreStructureProcessor).addProcessor(new RuleStructureProcessor(list)).addProcessor(new BlockAgeStructureProcessor(this.properties.mossiness)).addProcessor(new LavaSubmergedBlockStructureProcessor());
      if (this.properties.replaceWithBlackstone) {
         structurePlacementData.addProcessor(BlackstoneReplacementStructureProcessor.INSTANCE);
      }

      this.setStructureData(structure, this.pos, structurePlacementData);
   }

   private StructureProcessorRule createLavaReplacementRule() {
      if (this.verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR) {
         return createReplacementRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
      } else {
         return this.properties.cold ? createReplacementRule(Blocks.LAVA, Blocks.NETHERRACK) : createReplacementRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK);
      }
   }

   public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
      if (!boundingBox.contains(this.pos)) {
         return true;
      } else {
         boundingBox.encompass(this.structure.calculateBoundingBox(this.placementData, this.pos));
         boolean bl = super.generate(world, structureAccessor, chunkGenerator, random, boundingBox, chunkPos, pos);
         this.placeNetherrackBase(random, world);
         this.updateNetherracksInBound(random, world);
         if (this.properties.vines || this.properties.overgrown) {
            BlockPos.stream(this.getBoundingBox()).forEach((blockPos) -> {
               if (this.properties.vines) {
                  this.generateVines(random, world, blockPos);
               }

               if (this.properties.overgrown) {
                  this.generateOvergrownLeaves(random, world, blockPos);
               }

            });
         }

         return bl;
      }
   }

   protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
   }

   private void generateVines(Random random, WorldAccess world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      if (!blockState.isAir() && !blockState.isOf(Blocks.VINE)) {
         Direction direction = Direction.Type.HORIZONTAL.random(random);
         BlockPos blockPos = pos.offset(direction);
         BlockState blockState2 = world.getBlockState(blockPos);
         if (blockState2.isAir()) {
            if (Block.isFaceFullSquare(blockState.getCollisionShape(world, pos), direction)) {
               BooleanProperty booleanProperty = VineBlock.getFacingProperty(direction.getOpposite());
               world.setBlockState(blockPos, (BlockState)Blocks.VINE.getDefaultState().with(booleanProperty, true), 3);
            }
         }
      }
   }

   private void generateOvergrownLeaves(Random random, WorldAccess world, BlockPos pos) {
      if (random.nextFloat() < 0.5F && world.getBlockState(pos).isOf(Blocks.NETHERRACK) && world.getBlockState(pos.up()).isAir()) {
         world.setBlockState(pos.up(), (BlockState)Blocks.JUNGLE_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true), 3);
      }

   }

   private void updateNetherracksInBound(Random random, WorldAccess world) {
      for(int i = this.boundingBox.minX + 1; i < this.boundingBox.maxX; ++i) {
         for(int j = this.boundingBox.minZ + 1; j < this.boundingBox.maxZ; ++j) {
            BlockPos blockPos = new BlockPos(i, this.boundingBox.minY, j);
            if (world.getBlockState(blockPos).isOf(Blocks.NETHERRACK)) {
               this.updateNetherracks(random, world, blockPos.down());
            }
         }
      }

   }

   private void updateNetherracks(Random random, WorldAccess world, BlockPos pos) {
      BlockPos.Mutable mutable = pos.mutableCopy();
      this.placeNetherrackBottom(random, world, mutable);
      int i = 8;

      while(i > 0 && random.nextFloat() < 0.5F) {
         mutable.move(Direction.DOWN);
         --i;
         this.placeNetherrackBottom(random, world, mutable);
      }

   }

   private void placeNetherrackBase(Random random, WorldAccess world) {
      boolean bl = this.verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE || this.verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR;
      Vec3i vec3i = this.boundingBox.getCenter();
      int i = vec3i.getX();
      int j = vec3i.getZ();
      float[] fs = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
      int k = fs.length;
      int l = (this.boundingBox.getBlockCountX() + this.boundingBox.getBlockCountZ()) / 2;
      int m = random.nextInt(Math.max(1, 8 - l / 2));
      int n = true;
      BlockPos.Mutable mutable = BlockPos.ORIGIN.mutableCopy();

      for(int o = i - k; o <= i + k; ++o) {
         for(int p = j - k; p <= j + k; ++p) {
            int q = Math.abs(o - i) + Math.abs(p - j);
            int r = Math.max(0, q + m);
            if (r < k) {
               float f = fs[r];
               if (random.nextDouble() < (double)f) {
                  int s = getBaseHeight(world, o, p, this.verticalPlacement);
                  int t = bl ? s : Math.min(this.boundingBox.minY, s);
                  mutable.set(o, t, p);
                  if (Math.abs(t - this.boundingBox.minY) <= 3 && this.canFillNetherrack(world, mutable)) {
                     this.placeNetherrackBottom(random, world, mutable);
                     if (this.properties.overgrown) {
                        this.generateOvergrownLeaves(random, world, mutable);
                     }

                     this.updateNetherracks(random, world, mutable.down());
                  }
               }
            }
         }
      }

   }

   private boolean canFillNetherrack(WorldAccess world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      return !blockState.isOf(Blocks.AIR) && !blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.CHEST) && (this.verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.IN_NETHER || !blockState.isOf(Blocks.LAVA));
   }

   private void placeNetherrackBottom(Random random, WorldAccess world, BlockPos pos) {
      if (!this.properties.cold && random.nextFloat() < 0.07F) {
         world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), 3);
      } else {
         world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState(), 3);
      }

   }

   private static int getBaseHeight(WorldAccess world, int x, int y, RuinedPortalStructurePiece.VerticalPlacement verticalPlacement) {
      return world.getTopY(getHeightmapType(verticalPlacement), x, y) - 1;
   }

   public static Heightmap.Type getHeightmapType(RuinedPortalStructurePiece.VerticalPlacement verticalPlacement) {
      return verticalPlacement == RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Type.OCEAN_FLOOR_WG : Heightmap.Type.WORLD_SURFACE_WG;
   }

   private static StructureProcessorRule createReplacementRule(Block old, float chance, Block updated) {
      return new StructureProcessorRule(new RandomBlockMatchRuleTest(old, chance), AlwaysTrueRuleTest.INSTANCE, updated.getDefaultState());
   }

   private static StructureProcessorRule createReplacementRule(Block old, Block updated) {
      return new StructureProcessorRule(new BlockMatchRuleTest(old), AlwaysTrueRuleTest.INSTANCE, updated.getDefaultState());
   }

   public static enum VerticalPlacement {
      ON_LAND_SURFACE("on_land_surface"),
      PARTLY_BURIED("partly_buried"),
      ON_OCEAN_FLOOR("on_ocean_floor"),
      IN_MOUNTAIN("in_mountain"),
      UNDERGROUND("underground"),
      IN_NETHER("in_nether");

      private static final Map<String, RuinedPortalStructurePiece.VerticalPlacement> VERTICAL_PLACEMENTS = (Map)Arrays.stream(values()).collect(Collectors.toMap(RuinedPortalStructurePiece.VerticalPlacement::getId, (verticalPlacement) -> {
         return verticalPlacement;
      }));
      private final String id;

      private VerticalPlacement(String id) {
         this.id = id;
      }

      public String getId() {
         return this.id;
      }

      public static RuinedPortalStructurePiece.VerticalPlacement getFromId(String id) {
         return (RuinedPortalStructurePiece.VerticalPlacement)VERTICAL_PLACEMENTS.get(id);
      }
   }

   public static class Properties {
      public static final Codec<RuinedPortalStructurePiece.Properties> CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.BOOL.fieldOf("cold").forGetter((properties) -> {
            return properties.cold;
         }), Codec.FLOAT.fieldOf("mossiness").forGetter((properties) -> {
            return properties.mossiness;
         }), Codec.BOOL.fieldOf("air_pocket").forGetter((properties) -> {
            return properties.airPocket;
         }), Codec.BOOL.fieldOf("overgrown").forGetter((properties) -> {
            return properties.overgrown;
         }), Codec.BOOL.fieldOf("vines").forGetter((properties) -> {
            return properties.vines;
         }), Codec.BOOL.fieldOf("replace_with_blackstone").forGetter((properties) -> {
            return properties.replaceWithBlackstone;
         })).apply(instance, (Function6)(RuinedPortalStructurePiece.Properties::new));
      });
      public boolean cold;
      public float mossiness = 0.2F;
      public boolean airPocket;
      public boolean overgrown;
      public boolean vines;
      public boolean replaceWithBlackstone;

      public Properties() {
      }

      public <T> Properties(boolean cold, float mossiness, boolean airPocket, boolean overgrown, boolean vines, boolean replaceWithBlackstone) {
         this.cold = cold;
         this.mossiness = mossiness;
         this.airPocket = airPocket;
         this.overgrown = overgrown;
         this.vines = vines;
         this.replaceWithBlackstone = replaceWithBlackstone;
      }
   }
}
