package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandSource;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.predicate.NumberRange;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class ExecuteCommand {
   private static final Dynamic2CommandExceptionType BLOCKS_TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((object, object2) -> {
      return new TranslatableText("commands.execute.blocks.toobig", new Object[]{object, object2});
   });
   private static final SimpleCommandExceptionType CONDITIONAL_FAIL_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.execute.conditional.fail"));
   private static final DynamicCommandExceptionType CONDITIONAL_FAIL_COUNT_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.execute.conditional.fail_count", new Object[]{object});
   });
   private static final BinaryOperator<ResultConsumer<ServerCommandSource>> BINARY_RESULT_CONSUMER = (resultConsumer, resultConsumer2) -> {
      return (commandContext, bl, i) -> {
         resultConsumer.onCommandComplete(commandContext, bl, i);
         resultConsumer2.onCommandComplete(commandContext, bl, i);
      };
   };
   private static final SuggestionProvider<ServerCommandSource> LOOT_CONDITIONS = (commandContext, suggestionsBuilder) -> {
      LootConditionManager lootConditionManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPredicateManager();
      return CommandSource.suggestIdentifiers((Iterable)lootConditionManager.getIds(), suggestionsBuilder);
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("execute").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      }));
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("execute").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("run").redirect(dispatcher.getRoot()))).then(addConditionArguments(literalCommandNode, CommandManager.literal("if"), true))).then(addConditionArguments(literalCommandNode, CommandManager.literal("unless"), false))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (commandContext) -> {
         List<ServerCommandSource> list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(commandContext, "targets").iterator();

         while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            list.add(((ServerCommandSource)commandContext.getSource()).withEntity(entity));
         }

         return list;
      })))).then(CommandManager.literal("at").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (commandContext) -> {
         List<ServerCommandSource> list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(commandContext, "targets").iterator();

         while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            list.add(((ServerCommandSource)commandContext.getSource()).withWorld((ServerWorld)entity.world).withPosition(entity.getPos()).withRotation(entity.getRotationClient()));
         }

         return list;
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("store").then(addStoreArguments(literalCommandNode, CommandManager.literal("result"), true))).then(addStoreArguments(literalCommandNode, CommandManager.literal("success"), false)))).then(((LiteralArgumentBuilder)CommandManager.literal("positioned").then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect(literalCommandNode, (commandContext) -> {
         return ((ServerCommandSource)commandContext.getSource()).withPosition(Vec3ArgumentType.getVec3(commandContext, "pos")).withEntityAnchor(EntityAnchorArgumentType.EntityAnchor.FEET);
      }))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (commandContext) -> {
         List<ServerCommandSource> list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(commandContext, "targets").iterator();

         while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            list.add(((ServerCommandSource)commandContext.getSource()).withPosition(entity.getPos()));
         }

         return list;
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("rotated").then(CommandManager.argument("rot", RotationArgumentType.rotation()).redirect(literalCommandNode, (commandContext) -> {
         return ((ServerCommandSource)commandContext.getSource()).withRotation(RotationArgumentType.getRotation(commandContext, "rot").toAbsoluteRotation((ServerCommandSource)commandContext.getSource()));
      }))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork(literalCommandNode, (commandContext) -> {
         List<ServerCommandSource> list = Lists.newArrayList();
         Iterator var2 = EntityArgumentType.getOptionalEntities(commandContext, "targets").iterator();

         while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            list.add(((ServerCommandSource)commandContext.getSource()).withRotation(entity.getRotationClient()));
         }

         return list;
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).fork(literalCommandNode, (commandContext) -> {
         List<ServerCommandSource> list = Lists.newArrayList();
         EntityAnchorArgumentType.EntityAnchor entityAnchor = EntityAnchorArgumentType.getEntityAnchor(commandContext, "anchor");
         Iterator var3 = EntityArgumentType.getOptionalEntities(commandContext, "targets").iterator();

         while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            list.add(((ServerCommandSource)commandContext.getSource()).withLookingAt(entity, entityAnchor));
         }

         return list;
      }))))).then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect(literalCommandNode, (commandContext) -> {
         return ((ServerCommandSource)commandContext.getSource()).withLookingAt(Vec3ArgumentType.getVec3(commandContext, "pos"));
      })))).then(CommandManager.literal("align").then(CommandManager.argument("axes", SwizzleArgumentType.swizzle()).redirect(literalCommandNode, (commandContext) -> {
         return ((ServerCommandSource)commandContext.getSource()).withPosition(((ServerCommandSource)commandContext.getSource()).getPosition().floorAlongAxes(SwizzleArgumentType.getSwizzle(commandContext, "axes")));
      })))).then(CommandManager.literal("anchored").then(CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).redirect(literalCommandNode, (commandContext) -> {
         return ((ServerCommandSource)commandContext.getSource()).withEntityAnchor(EntityAnchorArgumentType.getEntityAnchor(commandContext, "anchor"));
      })))).then(CommandManager.literal("in").then(CommandManager.argument("dimension", DimensionArgumentType.dimension()).redirect(literalCommandNode, (commandContext) -> {
         return ((ServerCommandSource)commandContext.getSource()).withWorld(DimensionArgumentType.getDimensionArgument(commandContext, "dimension"));
      }))));
   }

   private static ArgumentBuilder<ServerCommandSource, ?> addStoreArguments(LiteralCommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder, boolean requestResult) {
      builder.then(CommandManager.literal("score").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).redirect(node, (commandContext) -> {
         return executeStoreScore((ServerCommandSource)commandContext.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(commandContext, "targets"), ScoreboardObjectiveArgumentType.getObjective(commandContext, "objective"), requestResult);
      }))));
      builder.then(CommandManager.literal("bossbar").then(((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(BossBarCommand.SUGGESTION_PROVIDER).then(CommandManager.literal("value").redirect(node, (commandContext) -> {
         return executeStoreBossbar((ServerCommandSource)commandContext.getSource(), BossBarCommand.getBossBar(commandContext), true, requestResult);
      }))).then(CommandManager.literal("max").redirect(node, (commandContext) -> {
         return executeStoreBossbar((ServerCommandSource)commandContext.getSource(), BossBarCommand.getBossBar(commandContext), false, requestResult);
      }))));
      Iterator var3 = DataCommand.TARGET_OBJECT_TYPES.iterator();

      while(var3.hasNext()) {
         DataCommand.ObjectType objectType = (DataCommand.ObjectType)var3.next();
         objectType.addArgumentsToBuilder(builder, (argumentBuilder) -> {
            return argumentBuilder.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).then(CommandManager.literal("int").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (commandContext) -> {
               return executeStoreData((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), (i) -> {
                  return NbtInt.of((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale")));
               }, requestResult);
            })))).then(CommandManager.literal("float").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (commandContext) -> {
               return executeStoreData((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), (i) -> {
                  return NbtFloat.of((float)((double)i * DoubleArgumentType.getDouble(commandContext, "scale")));
               }, requestResult);
            })))).then(CommandManager.literal("short").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (commandContext) -> {
               return executeStoreData((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), (i) -> {
                  return NbtShort.of((short)((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))));
               }, requestResult);
            })))).then(CommandManager.literal("long").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (commandContext) -> {
               return executeStoreData((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), (i) -> {
                  return NbtLong.of((long)((double)i * DoubleArgumentType.getDouble(commandContext, "scale")));
               }, requestResult);
            })))).then(CommandManager.literal("double").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (commandContext) -> {
               return executeStoreData((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), (i) -> {
                  return NbtDouble.of((double)i * DoubleArgumentType.getDouble(commandContext, "scale"));
               }, requestResult);
            })))).then(CommandManager.literal("byte").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect(node, (commandContext) -> {
               return executeStoreData((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), (i) -> {
                  return NbtByte.of((byte)((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))));
               }, requestResult);
            }))));
         });
      }

      return builder;
   }

   private static ServerCommandSource executeStoreScore(ServerCommandSource source, Collection<String> targets, ScoreboardObjective objective, boolean requestResult) {
      Scoreboard scoreboard = source.getMinecraftServer().getScoreboard();
      return source.mergeConsumers((commandContext, bl2, i) -> {
         Iterator var7 = targets.iterator();

         while(var7.hasNext()) {
            String string = (String)var7.next();
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(string, objective);
            int j = requestResult ? i : (bl2 ? 1 : 0);
            scoreboardPlayerScore.setScore(j);
         }

      }, BINARY_RESULT_CONSUMER);
   }

   private static ServerCommandSource executeStoreBossbar(ServerCommandSource source, CommandBossBar bossBar, boolean storeInValue, boolean requestResult) {
      return source.mergeConsumers((commandContext, bl3, i) -> {
         int j = requestResult ? i : (bl3 ? 1 : 0);
         if (storeInValue) {
            bossBar.setValue(j);
         } else {
            bossBar.setMaxValue(j);
         }

      }, BINARY_RESULT_CONSUMER);
   }

   private static ServerCommandSource executeStoreData(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, IntFunction<NbtElement> nbtSetter, boolean requestResult) {
      return source.mergeConsumers((commandContext, bl2, i) -> {
         try {
            NbtCompound nbtCompound = object.getNbt();
            int j = requestResult ? i : (bl2 ? 1 : 0);
            path.put(nbtCompound, () -> {
               return (NbtElement)nbtSetter.apply(j);
            });
            object.setNbt(nbtCompound);
         } catch (CommandSyntaxException var9) {
         }

      }, BINARY_RESULT_CONSUMER);
   }

   private static ArgumentBuilder<ServerCommandSource, ?> addConditionArguments(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)argumentBuilder.then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(addConditionLogic(root, CommandManager.argument("block", BlockPredicateArgumentType.blockPredicate()), positive, (commandContext) -> {
         return BlockPredicateArgumentType.getBlockPredicate(commandContext, "block").test(new CachedBlockPosition(((ServerCommandSource)commandContext.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos(commandContext, "pos"), true));
      }))))).then(CommandManager.literal("score").then(CommandManager.argument("target", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targetObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.literal("=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (commandContext) -> {
         return testScoreCondition(commandContext, Integer::equals);
      }))))).then(CommandManager.literal("<").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (commandContext) -> {
         return testScoreCondition(commandContext, (integer, integer2) -> {
            return integer < integer2;
         });
      }))))).then(CommandManager.literal("<=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (commandContext) -> {
         return testScoreCondition(commandContext, (integer, integer2) -> {
            return integer <= integer2;
         });
      }))))).then(CommandManager.literal(">").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (commandContext) -> {
         return testScoreCondition(commandContext, (integer, integer2) -> {
            return integer > integer2;
         });
      }))))).then(CommandManager.literal(">=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, (commandContext) -> {
         return testScoreCondition(commandContext, (integer, integer2) -> {
            return integer >= integer2;
         });
      }))))).then(CommandManager.literal("matches").then(addConditionLogic(root, CommandManager.argument("range", NumberRangeArgumentType.intRange()), positive, (commandContext) -> {
         return testScoreMatch(commandContext, NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument(commandContext, "range"));
      }))))))).then(CommandManager.literal("blocks").then(CommandManager.argument("start", BlockPosArgumentType.blockPos()).then(CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).then(addBlocksConditionLogic(root, CommandManager.literal("all"), positive, false))).then(addBlocksConditionLogic(root, CommandManager.literal("masked"), positive, true))))))).then(CommandManager.literal("entity").then(((RequiredArgumentBuilder)CommandManager.argument("entities", EntityArgumentType.entities()).fork(root, (commandContext) -> {
         return getSourceOrEmptyForConditionFork(commandContext, positive, !EntityArgumentType.getOptionalEntities(commandContext, "entities").isEmpty());
      })).executes(getExistsConditionExecute(positive, (commandContext) -> {
         return EntityArgumentType.getOptionalEntities(commandContext, "entities").size();
      }))))).then(CommandManager.literal("predicate").then(addConditionLogic(root, CommandManager.argument("predicate", IdentifierArgumentType.identifier()).suggests(LOOT_CONDITIONS), positive, (commandContext) -> {
         return testLootCondition((ServerCommandSource)commandContext.getSource(), IdentifierArgumentType.method_23727(commandContext, "predicate"));
      })));
      Iterator var3 = DataCommand.SOURCE_OBJECT_TYPES.iterator();

      while(var3.hasNext()) {
         DataCommand.ObjectType objectType = (DataCommand.ObjectType)var3.next();
         argumentBuilder.then(objectType.addArgumentsToBuilder(CommandManager.literal("data"), (argumentBuilderx) -> {
            return argumentBuilderx.then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).fork(root, (commandContext) -> {
               return getSourceOrEmptyForConditionFork(commandContext, positive, countPathMatches(objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path")) > 0);
            })).executes(getExistsConditionExecute(positive, (commandContext) -> {
               return countPathMatches(objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"));
            })));
         }));
      }

      return argumentBuilder;
   }

   private static Command<ServerCommandSource> getExistsConditionExecute(boolean positive, ExecuteCommand.ExistsCondition condition) {
      return positive ? (commandContext) -> {
         int i = condition.test(commandContext);
         if (i > 0) {
            ((ServerCommandSource)commandContext.getSource()).sendFeedback(new TranslatableText("commands.execute.conditional.pass_count", new Object[]{i}), false);
            return i;
         } else {
            throw CONDITIONAL_FAIL_EXCEPTION.create();
         }
      } : (commandContext) -> {
         int i = condition.test(commandContext);
         if (i == 0) {
            ((ServerCommandSource)commandContext.getSource()).sendFeedback(new TranslatableText("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create(i);
         }
      };
   }

   private static int countPathMatches(DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      return path.count(object.getNbt());
   }

   private static boolean testScoreCondition(CommandContext<ServerCommandSource> context, BiPredicate<Integer, Integer> condition) throws CommandSyntaxException {
      String string = ScoreHolderArgumentType.getScoreHolder(context, "target");
      ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
      String string2 = ScoreHolderArgumentType.getScoreHolder(context, "source");
      ScoreboardObjective scoreboardObjective2 = ScoreboardObjectiveArgumentType.getObjective(context, "sourceObjective");
      Scoreboard scoreboard = ((ServerCommandSource)context.getSource()).getMinecraftServer().getScoreboard();
      if (scoreboard.playerHasObjective(string, scoreboardObjective) && scoreboard.playerHasObjective(string2, scoreboardObjective2)) {
         ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(string, scoreboardObjective);
         ScoreboardPlayerScore scoreboardPlayerScore2 = scoreboard.getPlayerScore(string2, scoreboardObjective2);
         return condition.test(scoreboardPlayerScore.getScore(), scoreboardPlayerScore2.getScore());
      } else {
         return false;
      }
   }

   private static boolean testScoreMatch(CommandContext<ServerCommandSource> context, NumberRange.IntRange range) throws CommandSyntaxException {
      String string = ScoreHolderArgumentType.getScoreHolder(context, "target");
      ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
      Scoreboard scoreboard = ((ServerCommandSource)context.getSource()).getMinecraftServer().getScoreboard();
      return !scoreboard.playerHasObjective(string, scoreboardObjective) ? false : range.test(scoreboard.getPlayerScore(string, scoreboardObjective).getScore());
   }

   private static boolean testLootCondition(ServerCommandSource source, LootCondition condition) {
      ServerWorld serverWorld = source.getWorld();
      LootContext.Builder builder = (new LootContext.Builder(serverWorld)).parameter(LootContextParameters.ORIGIN, source.getPosition()).optionalParameter(LootContextParameters.THIS_ENTITY, source.getEntity());
      return condition.test(builder.build(LootContextTypes.COMMAND));
   }

   private static Collection<ServerCommandSource> getSourceOrEmptyForConditionFork(CommandContext<ServerCommandSource> context, boolean positive, boolean value) {
      return (Collection)(value == positive ? Collections.singleton(context.getSource()) : Collections.emptyList());
   }

   private static ArgumentBuilder<ServerCommandSource, ?> addConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, ExecuteCommand.Condition condition) {
      return builder.fork(root, (commandContext) -> {
         return getSourceOrEmptyForConditionFork(commandContext, positive, condition.test(commandContext));
      }).executes((commandContext) -> {
         if (positive == condition.test(commandContext)) {
            ((ServerCommandSource)commandContext.getSource()).sendFeedback(new TranslatableText("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw CONDITIONAL_FAIL_EXCEPTION.create();
         }
      });
   }

   private static ArgumentBuilder<ServerCommandSource, ?> addBlocksConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, boolean masked) {
      return builder.fork(root, (commandContext) -> {
         return getSourceOrEmptyForConditionFork(commandContext, positive, testBlocksCondition(commandContext, masked).isPresent());
      }).executes(positive ? (commandContext) -> {
         return executePositiveBlockCondition(commandContext, masked);
      } : (commandContext) -> {
         return executeNegativeBlockCondition(commandContext, masked);
      });
   }

   private static int executePositiveBlockCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
      OptionalInt optionalInt = testBlocksCondition(context, masked);
      if (optionalInt.isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(new TranslatableText("commands.execute.conditional.pass_count", new Object[]{optionalInt.getAsInt()}), false);
         return optionalInt.getAsInt();
      } else {
         throw CONDITIONAL_FAIL_EXCEPTION.create();
      }
   }

   private static int executeNegativeBlockCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
      OptionalInt optionalInt = testBlocksCondition(context, masked);
      if (optionalInt.isPresent()) {
         throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create(optionalInt.getAsInt());
      } else {
         ((ServerCommandSource)context.getSource()).sendFeedback(new TranslatableText("commands.execute.conditional.pass"), false);
         return 1;
      }
   }

   private static OptionalInt testBlocksCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
      return testBlocksCondition(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos(context, "start"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), masked);
   }

   private static OptionalInt testBlocksCondition(ServerWorld world, BlockPos start, BlockPos end, BlockPos destination, boolean masked) throws CommandSyntaxException {
      BlockBox blockBox = new BlockBox(start, end);
      BlockBox blockBox2 = new BlockBox(destination, destination.add(blockBox.getDimensions()));
      BlockPos blockPos = new BlockPos(blockBox2.minX - blockBox.minX, blockBox2.minY - blockBox.minY, blockBox2.minZ - blockBox.minZ);
      int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
      if (i > 32768) {
         throw BLOCKS_TOOBIG_EXCEPTION.create(32768, i);
      } else {
         int j = 0;

         for(int k = blockBox.minZ; k <= blockBox.maxZ; ++k) {
            for(int l = blockBox.minY; l <= blockBox.maxY; ++l) {
               for(int m = blockBox.minX; m <= blockBox.maxX; ++m) {
                  BlockPos blockPos2 = new BlockPos(m, l, k);
                  BlockPos blockPos3 = blockPos2.add(blockPos);
                  BlockState blockState = world.getBlockState(blockPos2);
                  if (!masked || !blockState.isOf(Blocks.AIR)) {
                     if (blockState != world.getBlockState(blockPos3)) {
                        return OptionalInt.empty();
                     }

                     BlockEntity blockEntity = world.getBlockEntity(blockPos2);
                     BlockEntity blockEntity2 = world.getBlockEntity(blockPos3);
                     if (blockEntity != null) {
                        if (blockEntity2 == null) {
                           return OptionalInt.empty();
                        }

                        NbtCompound nbtCompound = blockEntity.writeNbt(new NbtCompound());
                        nbtCompound.remove("x");
                        nbtCompound.remove("y");
                        nbtCompound.remove("z");
                        NbtCompound nbtCompound2 = blockEntity2.writeNbt(new NbtCompound());
                        nbtCompound2.remove("x");
                        nbtCompound2.remove("y");
                        nbtCompound2.remove("z");
                        if (!nbtCompound.equals(nbtCompound2)) {
                           return OptionalInt.empty();
                        }
                     }

                     ++j;
                  }
               }
            }
         }

         return OptionalInt.of(j);
      }
   }

   @FunctionalInterface
   interface ExistsCondition {
      int test(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface Condition {
      boolean test(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
   }
}
