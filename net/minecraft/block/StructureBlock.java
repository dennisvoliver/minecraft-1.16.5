package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StructureBlock extends BlockWithEntity {
   public static final EnumProperty<StructureBlockMode> MODE;

   protected StructureBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new StructureBlockBlockEntity();
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof StructureBlockBlockEntity) {
         return ((StructureBlockBlockEntity)blockEntity).openScreen(player) ? ActionResult.success(world.isClient) : ActionResult.PASS;
      } else {
         return ActionResult.PASS;
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      if (!world.isClient) {
         if (placer != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StructureBlockBlockEntity) {
               ((StructureBlockBlockEntity)blockEntity).setAuthor(placer);
            }
         }

      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(MODE, StructureBlockMode.DATA);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(MODE);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
      if (world instanceof ServerWorld) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof StructureBlockBlockEntity) {
            StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)blockEntity;
            boolean bl = world.isReceivingRedstonePower(pos);
            boolean bl2 = structureBlockBlockEntity.isPowered();
            if (bl && !bl2) {
               structureBlockBlockEntity.setPowered(true);
               this.doAction((ServerWorld)world, structureBlockBlockEntity);
            } else if (!bl && bl2) {
               structureBlockBlockEntity.setPowered(false);
            }

         }
      }
   }

   private void doAction(ServerWorld world, StructureBlockBlockEntity blockEntity) {
      switch(blockEntity.getMode()) {
      case SAVE:
         blockEntity.saveStructure(false);
         break;
      case LOAD:
         blockEntity.loadStructure(world, false);
         break;
      case CORNER:
         blockEntity.unloadStructure();
      case DATA:
      }

   }

   static {
      MODE = Properties.STRUCTURE_BLOCK_MODE;
   }
}
