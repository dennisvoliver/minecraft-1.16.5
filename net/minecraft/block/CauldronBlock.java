package net.minecraft.block;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CauldronBlock extends Block {
   public static final IntProperty LEVEL;
   private static final VoxelShape RAYCAST_SHAPE;
   protected static final VoxelShape OUTLINE_SHAPE;

   public CauldronBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 0));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return OUTLINE_SHAPE;
   }

   public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
      return RAYCAST_SHAPE;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      int i = (Integer)state.get(LEVEL);
      float f = (float)pos.getY() + (6.0F + (float)(3 * i)) / 16.0F;
      if (!world.isClient && entity.isOnFire() && i > 0 && entity.getY() <= (double)f) {
         entity.extinguish();
         this.setLevel(world, pos, state, i - 1);
      }

   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.isEmpty()) {
         return ActionResult.PASS;
      } else {
         int i = (Integer)state.get(LEVEL);
         Item item = itemStack.getItem();
         if (item == Items.WATER_BUCKET) {
            if (i < 3 && !world.isClient) {
               if (!player.abilities.creativeMode) {
                  player.setStackInHand(hand, new ItemStack(Items.BUCKET));
               }

               player.incrementStat(Stats.FILL_CAULDRON);
               this.setLevel(world, pos, state, 3);
               world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResult.success(world.isClient);
         } else if (item == Items.BUCKET) {
            if (i == 3 && !world.isClient) {
               if (!player.abilities.creativeMode) {
                  itemStack.decrement(1);
                  if (itemStack.isEmpty()) {
                     player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
                  } else if (!player.inventory.insertStack(new ItemStack(Items.WATER_BUCKET))) {
                     player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
                  }
               }

               player.incrementStat(Stats.USE_CAULDRON);
               this.setLevel(world, pos, state, 0);
               world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResult.success(world.isClient);
         } else {
            ItemStack itemStack4;
            if (item == Items.GLASS_BOTTLE) {
               if (i > 0 && !world.isClient) {
                  if (!player.abilities.creativeMode) {
                     itemStack4 = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                     player.incrementStat(Stats.USE_CAULDRON);
                     itemStack.decrement(1);
                     if (itemStack.isEmpty()) {
                        player.setStackInHand(hand, itemStack4);
                     } else if (!player.inventory.insertStack(itemStack4)) {
                        player.dropItem(itemStack4, false);
                     } else if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)player).refreshScreenHandler(player.playerScreenHandler);
                     }
                  }

                  world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  this.setLevel(world, pos, state, i - 1);
               }

               return ActionResult.success(world.isClient);
            } else if (item == Items.POTION && PotionUtil.getPotion(itemStack) == Potions.WATER) {
               if (i < 3 && !world.isClient) {
                  if (!player.abilities.creativeMode) {
                     itemStack4 = new ItemStack(Items.GLASS_BOTTLE);
                     player.incrementStat(Stats.USE_CAULDRON);
                     player.setStackInHand(hand, itemStack4);
                     if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)player).refreshScreenHandler(player.playerScreenHandler);
                     }
                  }

                  world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  this.setLevel(world, pos, state, i + 1);
               }

               return ActionResult.success(world.isClient);
            } else {
               if (i > 0 && item instanceof DyeableItem) {
                  DyeableItem dyeableItem = (DyeableItem)item;
                  if (dyeableItem.hasColor(itemStack) && !world.isClient) {
                     dyeableItem.removeColor(itemStack);
                     this.setLevel(world, pos, state, i - 1);
                     player.incrementStat(Stats.CLEAN_ARMOR);
                     return ActionResult.SUCCESS;
                  }
               }

               if (i > 0 && item instanceof BannerItem) {
                  if (BannerBlockEntity.getPatternCount(itemStack) > 0 && !world.isClient) {
                     itemStack4 = itemStack.copy();
                     itemStack4.setCount(1);
                     BannerBlockEntity.loadFromItemStack(itemStack4);
                     player.incrementStat(Stats.CLEAN_BANNER);
                     if (!player.abilities.creativeMode) {
                        itemStack.decrement(1);
                        this.setLevel(world, pos, state, i - 1);
                     }

                     if (itemStack.isEmpty()) {
                        player.setStackInHand(hand, itemStack4);
                     } else if (!player.inventory.insertStack(itemStack4)) {
                        player.dropItem(itemStack4, false);
                     } else if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)player).refreshScreenHandler(player.playerScreenHandler);
                     }
                  }

                  return ActionResult.success(world.isClient);
               } else if (i > 0 && item instanceof BlockItem) {
                  Block block = ((BlockItem)item).getBlock();
                  if (block instanceof ShulkerBoxBlock && !world.isClient()) {
                     ItemStack itemStack5 = new ItemStack(Blocks.SHULKER_BOX, 1);
                     if (itemStack.hasTag()) {
                        itemStack5.setTag(itemStack.getTag().copy());
                     }

                     player.setStackInHand(hand, itemStack5);
                     this.setLevel(world, pos, state, i - 1);
                     player.incrementStat(Stats.CLEAN_SHULKER_BOX);
                     return ActionResult.SUCCESS;
                  } else {
                     return ActionResult.CONSUME;
                  }
               } else {
                  return ActionResult.PASS;
               }
            }
         }
      }
   }

   public void setLevel(World world, BlockPos pos, BlockState state, int level) {
      world.setBlockState(pos, (BlockState)state.with(LEVEL, MathHelper.clamp(level, 0, 3)), 2);
      world.updateComparators(pos, this);
   }

   public void rainTick(World world, BlockPos pos) {
      if (world.random.nextInt(20) == 1) {
         float f = world.getBiome(pos).getTemperature(pos);
         if (!(f < 0.15F)) {
            BlockState blockState = world.getBlockState(pos);
            if ((Integer)blockState.get(LEVEL) < 3) {
               world.setBlockState(pos, (BlockState)blockState.cycle(LEVEL), 2);
            }

         }
      }
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return (Integer)state.get(LEVEL);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LEVEL);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      LEVEL = Properties.LEVEL_3;
      RAYCAST_SHAPE = createCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
      OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), RAYCAST_SHAPE), BooleanBiFunction.ONLY_FIRST);
   }
}
