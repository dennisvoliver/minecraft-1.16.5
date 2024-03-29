package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class TriggerCommand {
   private static final SimpleCommandExceptionType FAILED_UNPRIMED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.trigger.failed.unprimed"));
   private static final SimpleCommandExceptionType FAILED_INVALID_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.trigger.failed.invalid"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("trigger").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).suggests((commandContext, suggestionsBuilder) -> {
         return suggestObjectives((ServerCommandSource)commandContext.getSource(), suggestionsBuilder);
      }).executes((commandContext) -> {
         return executeSimple((ServerCommandSource)commandContext.getSource(), getScore(((ServerCommandSource)commandContext.getSource()).getPlayer(), ScoreboardObjectiveArgumentType.getObjective(commandContext, "objective")));
      })).then(CommandManager.literal("add").then(CommandManager.argument("value", IntegerArgumentType.integer()).executes((commandContext) -> {
         return executeAdd((ServerCommandSource)commandContext.getSource(), getScore(((ServerCommandSource)commandContext.getSource()).getPlayer(), ScoreboardObjectiveArgumentType.getObjective(commandContext, "objective")), IntegerArgumentType.getInteger(commandContext, "value"));
      })))).then(CommandManager.literal("set").then(CommandManager.argument("value", IntegerArgumentType.integer()).executes((commandContext) -> {
         return executeSet((ServerCommandSource)commandContext.getSource(), getScore(((ServerCommandSource)commandContext.getSource()).getPlayer(), ScoreboardObjectiveArgumentType.getObjective(commandContext, "objective")), IntegerArgumentType.getInteger(commandContext, "value"));
      })))));
   }

   public static CompletableFuture<Suggestions> suggestObjectives(ServerCommandSource source, SuggestionsBuilder builder) {
      Entity entity = source.getEntity();
      List<String> list = Lists.newArrayList();
      if (entity != null) {
         Scoreboard scoreboard = source.getMinecraftServer().getScoreboard();
         String string = entity.getEntityName();
         Iterator var6 = scoreboard.getObjectives().iterator();

         while(var6.hasNext()) {
            ScoreboardObjective scoreboardObjective = (ScoreboardObjective)var6.next();
            if (scoreboardObjective.getCriterion() == ScoreboardCriterion.TRIGGER && scoreboard.playerHasObjective(string, scoreboardObjective)) {
               ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(string, scoreboardObjective);
               if (!scoreboardPlayerScore.isLocked()) {
                  list.add(scoreboardObjective.getName());
               }
            }
         }
      }

      return CommandSource.suggestMatching((Iterable)list, builder);
   }

   private static int executeAdd(ServerCommandSource source, ScoreboardPlayerScore score, int value) {
      score.incrementScore(value);
      source.sendFeedback(new TranslatableText("commands.trigger.add.success", new Object[]{score.getObjective().toHoverableText(), value}), true);
      return score.getScore();
   }

   private static int executeSet(ServerCommandSource source, ScoreboardPlayerScore score, int value) {
      score.setScore(value);
      source.sendFeedback(new TranslatableText("commands.trigger.set.success", new Object[]{score.getObjective().toHoverableText(), value}), true);
      return value;
   }

   private static int executeSimple(ServerCommandSource source, ScoreboardPlayerScore score) {
      score.incrementScore(1);
      source.sendFeedback(new TranslatableText("commands.trigger.simple.success", new Object[]{score.getObjective().toHoverableText()}), true);
      return score.getScore();
   }

   private static ScoreboardPlayerScore getScore(ServerPlayerEntity player, ScoreboardObjective objective) throws CommandSyntaxException {
      if (objective.getCriterion() != ScoreboardCriterion.TRIGGER) {
         throw FAILED_INVALID_EXCEPTION.create();
      } else {
         Scoreboard scoreboard = player.getScoreboard();
         String string = player.getEntityName();
         if (!scoreboard.playerHasObjective(string, objective)) {
            throw FAILED_UNPRIMED_EXCEPTION.create();
         } else {
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(string, objective);
            if (scoreboardPlayerScore.isLocked()) {
               throw FAILED_UNPRIMED_EXCEPTION.create();
            } else {
               scoreboardPlayerScore.setLocked(true);
               return scoreboardPlayerScore;
            }
         }
      }
   }
}
