package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FillCommand {
   private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.fill.toobig", new Object[]{object, object2});
   });
   private static final BlockStateArgument AIR_BLOCK_ARGUMENT;
   private static final SimpleCommandExceptionType FAILED_EXCEPTION;

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fill").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("block", BlockStateArgumentType.blockState()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.REPLACE, (Predicate)null);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("replace").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.REPLACE, (Predicate)null);
      })).then(CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.REPLACE, BlockPredicateArgumentType.getBlockPredicate(commandContext, "filter"));
      })))).then(CommandManager.literal("keep").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.REPLACE, (cachedBlockPosition) -> {
            return cachedBlockPosition.getWorld().isAir(cachedBlockPosition.getBlockPos());
         });
      }))).then(CommandManager.literal("outline").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.OUTLINE, (Predicate)null);
      }))).then(CommandManager.literal("hollow").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.HOLLOW, (Predicate)null);
      }))).then(CommandManager.literal("destroy").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), new BlockBox(BlockPosArgumentType.getLoadedBlockPos(commandContext, "from"), BlockPosArgumentType.getLoadedBlockPos(commandContext, "to")), BlockStateArgumentType.getBlockState(commandContext, "block"), FillCommand.Mode.DESTROY, (Predicate)null);
      }))))));
   }

   private static int execute(ServerCommandSource source, BlockBox range, BlockStateArgument block, FillCommand.Mode mode, @Nullable Predicate<CachedBlockPosition> filter) throws CommandSyntaxException {
      int i = range.getBlockCountX() * range.getBlockCountY() * range.getBlockCountZ();
      if (i > 32768) {
         throw TOO_BIG_EXCEPTION.create(32768, i);
      } else {
         List<BlockPos> list = Lists.newArrayList();
         ServerWorld serverWorld = source.getWorld();
         int j = 0;
         Iterator var9 = BlockPos.iterate(range.minX, range.minY, range.minZ, range.maxX, range.maxY, range.maxZ).iterator();

         while(true) {
            BlockPos blockPos;
            do {
               if (!var9.hasNext()) {
                  var9 = list.iterator();

                  while(var9.hasNext()) {
                     blockPos = (BlockPos)var9.next();
                     Block block2 = serverWorld.getBlockState(blockPos).getBlock();
                     serverWorld.updateNeighbors(blockPos, block2);
                  }

                  if (j == 0) {
                     throw FAILED_EXCEPTION.create();
                  }

                  source.sendFeedback(new TranslatableText("commands.fill.success", new Object[]{j}), true);
                  return j;
               }

               blockPos = (BlockPos)var9.next();
            } while(filter != null && !filter.test(new CachedBlockPosition(serverWorld, blockPos, true)));

            BlockStateArgument blockStateArgument = mode.filter.filter(range, blockPos, block, serverWorld);
            if (blockStateArgument != null) {
               BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
               Clearable.clear(blockEntity);
               if (blockStateArgument.setBlockState(serverWorld, blockPos, 2)) {
                  list.add(blockPos.toImmutable());
                  ++j;
               }
            }
         }
      }
   }

   static {
      AIR_BLOCK_ARGUMENT = new BlockStateArgument(Blocks.AIR.getDefaultState(), Collections.emptySet(), (NbtCompound)null);
      FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.fill.failed"));
   }

   static enum Mode {
      REPLACE((blockBox, blockPos, blockStateArgument, serverWorld) -> {
         return blockStateArgument;
      }),
      OUTLINE((blockBox, blockPos, blockStateArgument, serverWorld) -> {
         return blockPos.getX() != blockBox.minX && blockPos.getX() != blockBox.maxX && blockPos.getY() != blockBox.minY && blockPos.getY() != blockBox.maxY && blockPos.getZ() != blockBox.minZ && blockPos.getZ() != blockBox.maxZ ? null : blockStateArgument;
      }),
      HOLLOW((blockBox, blockPos, blockStateArgument, serverWorld) -> {
         return blockPos.getX() != blockBox.minX && blockPos.getX() != blockBox.maxX && blockPos.getY() != blockBox.minY && blockPos.getY() != blockBox.maxY && blockPos.getZ() != blockBox.minZ && blockPos.getZ() != blockBox.maxZ ? FillCommand.AIR_BLOCK_ARGUMENT : blockStateArgument;
      }),
      DESTROY((blockBox, blockPos, blockStateArgument, serverWorld) -> {
         serverWorld.breakBlock(blockPos, true);
         return blockStateArgument;
      });

      public final SetBlockCommand.Filter filter;

      private Mode(SetBlockCommand.Filter filter) {
         this.filter = filter;
      }
   }
}
