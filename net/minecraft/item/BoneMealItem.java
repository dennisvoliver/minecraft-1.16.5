package net.minecraft.item;

import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

public class BoneMealItem extends Item {
   public BoneMealItem(Item.Settings settings) {
      super(settings);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      BlockPos blockPos = context.getBlockPos();
      BlockPos blockPos2 = blockPos.offset(context.getSide());
      if (useOnFertilizable(context.getStack(), world, blockPos)) {
         if (!world.isClient) {
            world.syncWorldEvent(2005, blockPos, 0);
         }

         return ActionResult.success(world.isClient);
      } else {
         BlockState blockState = world.getBlockState(blockPos);
         boolean bl = blockState.isSideSolidFullSquare(world, blockPos, context.getSide());
         if (bl && useOnGround(context.getStack(), world, blockPos2, context.getSide())) {
            if (!world.isClient) {
               world.syncWorldEvent(2005, blockPos2, 0);
            }

            return ActionResult.success(world.isClient);
         } else {
            return ActionResult.PASS;
         }
      }
   }

   public static boolean useOnFertilizable(ItemStack stack, World world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      if (blockState.getBlock() instanceof Fertilizable) {
         Fertilizable fertilizable = (Fertilizable)blockState.getBlock();
         if (fertilizable.isFertilizable(world, pos, blockState, world.isClient)) {
            if (world instanceof ServerWorld) {
               if (fertilizable.canGrow(world, world.random, pos, blockState)) {
                  fertilizable.grow((ServerWorld)world, world.random, pos, blockState);
               }

               stack.decrement(1);
            }

            return true;
         }
      }

      return false;
   }

   public static boolean useOnGround(ItemStack stack, World world, BlockPos blockPos, @Nullable Direction facing) {
      if (world.getBlockState(blockPos).isOf(Blocks.WATER) && world.getFluidState(blockPos).getLevel() == 8) {
         if (!(world instanceof ServerWorld)) {
            return true;
         } else {
            label80:
            for(int i = 0; i < 128; ++i) {
               BlockPos blockPos2 = blockPos;
               BlockState blockState = Blocks.SEAGRASS.getDefaultState();

               for(int j = 0; j < i / 16; ++j) {
                  blockPos2 = blockPos2.add(RANDOM.nextInt(3) - 1, (RANDOM.nextInt(3) - 1) * RANDOM.nextInt(3) / 2, RANDOM.nextInt(3) - 1);
                  if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                     continue label80;
                  }
               }

               Optional<RegistryKey<Biome>> optional = world.getBiomeKey(blockPos2);
               if (Objects.equals(optional, Optional.of(BiomeKeys.WARM_OCEAN)) || Objects.equals(optional, Optional.of(BiomeKeys.DEEP_WARM_OCEAN))) {
                  if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                     blockState = (BlockState)((Block)BlockTags.WALL_CORALS.getRandom(world.random)).getDefaultState().with(DeadCoralWallFanBlock.FACING, facing);
                  } else if (RANDOM.nextInt(4) == 0) {
                     blockState = ((Block)BlockTags.UNDERWATER_BONEMEALS.getRandom(RANDOM)).getDefaultState();
                  }
               }

               if (blockState.getBlock().isIn(BlockTags.WALL_CORALS)) {
                  for(int k = 0; !blockState.canPlaceAt(world, blockPos2) && k < 4; ++k) {
                     blockState = (BlockState)blockState.with(DeadCoralWallFanBlock.FACING, Direction.Type.HORIZONTAL.random(RANDOM));
                  }
               }

               if (blockState.canPlaceAt(world, blockPos2)) {
                  BlockState blockState2 = world.getBlockState(blockPos2);
                  if (blockState2.isOf(Blocks.WATER) && world.getFluidState(blockPos2).getLevel() == 8) {
                     world.setBlockState(blockPos2, blockState, 3);
                  } else if (blockState2.isOf(Blocks.SEAGRASS) && RANDOM.nextInt(10) == 0) {
                     ((Fertilizable)Blocks.SEAGRASS).grow((ServerWorld)world, RANDOM, blockPos2, blockState2);
                  }
               }
            }

            stack.decrement(1);
            return true;
         }
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public static void createParticles(WorldAccess world, BlockPos pos, int count) {
      if (count == 0) {
         count = 15;
      }

      BlockState blockState = world.getBlockState(pos);
      if (!blockState.isAir()) {
         double d = 0.5D;
         double g;
         if (blockState.isOf(Blocks.WATER)) {
            count *= 3;
            g = 1.0D;
            d = 3.0D;
         } else if (blockState.isOpaqueFullCube(world, pos)) {
            pos = pos.up();
            count *= 3;
            d = 3.0D;
            g = 1.0D;
         } else {
            g = blockState.getOutlineShape(world, pos).getMax(Direction.Axis.Y);
         }

         world.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);

         for(int i = 0; i < count; ++i) {
            double h = RANDOM.nextGaussian() * 0.02D;
            double j = RANDOM.nextGaussian() * 0.02D;
            double k = RANDOM.nextGaussian() * 0.02D;
            double l = 0.5D - d;
            double m = (double)pos.getX() + l + RANDOM.nextDouble() * d * 2.0D;
            double n = (double)pos.getY() + RANDOM.nextDouble() * g;
            double o = (double)pos.getZ() + l + RANDOM.nextDouble() * d * 2.0D;
            if (!world.getBlockState((new BlockPos(m, n, o)).down()).isAir()) {
               world.addParticle(ParticleTypes.HAPPY_VILLAGER, m, n, o, h, j, k);
            }
         }

      }
   }
}
