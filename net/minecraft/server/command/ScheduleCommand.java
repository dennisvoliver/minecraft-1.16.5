package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.FunctionTagTimerCallback;
import net.minecraft.world.timer.FunctionTimerCallback;
import net.minecraft.world.timer.Timer;

public class ScheduleCommand {
   private static final SimpleCommandExceptionType SAME_TICK_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.schedule.same_tick"));
   private static final DynamicCommandExceptionType CLEARED_FAILURE_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.schedule.cleared.failure", new Object[]{object});
   });
   private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (commandContext, suggestionsBuilder) -> {
      return CommandSource.suggestMatching((Iterable)((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getSaveProperties().getMainWorldProperties().getScheduledEvents().method_22592(), suggestionsBuilder);
   };

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("schedule").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      })).then(CommandManager.literal("function").then(CommandManager.argument("function", CommandFunctionArgumentType.commandFunction()).suggests(FunctionCommand.SUGGESTION_PROVIDER).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("time", TimeArgumentType.time()).executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), CommandFunctionArgumentType.getFunctionOrTag(commandContext, "function"), IntegerArgumentType.getInteger(commandContext, "time"), true);
      })).then(CommandManager.literal("append").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), CommandFunctionArgumentType.getFunctionOrTag(commandContext, "function"), IntegerArgumentType.getInteger(commandContext, "time"), false);
      }))).then(CommandManager.literal("replace").executes((commandContext) -> {
         return execute((ServerCommandSource)commandContext.getSource(), CommandFunctionArgumentType.getFunctionOrTag(commandContext, "function"), IntegerArgumentType.getInteger(commandContext, "time"), true);
      })))))).then(CommandManager.literal("clear").then(CommandManager.argument("function", StringArgumentType.greedyString()).suggests(SUGGESTION_PROVIDER).executes((commandContext) -> {
         return method_22833((ServerCommandSource)commandContext.getSource(), StringArgumentType.getString(commandContext, "function"));
      }))));
   }

   private static int execute(ServerCommandSource source, Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> function, int time, boolean replace) throws CommandSyntaxException {
      if (time == 0) {
         throw SAME_TICK_EXCEPTION.create();
      } else {
         long l = source.getWorld().getTime() + (long)time;
         Identifier identifier = (Identifier)function.getFirst();
         Timer<MinecraftServer> timer = source.getMinecraftServer().getSaveProperties().getMainWorldProperties().getScheduledEvents();
         ((Either)function.getSecond()).ifLeft((commandFunction) -> {
            String string = identifier.toString();
            if (replace) {
               timer.method_22593(string);
            }

            timer.setEvent(string, l, new FunctionTimerCallback(identifier));
            source.sendFeedback(new TranslatableText("commands.schedule.created.function", new Object[]{identifier, time, l}), true);
         }).ifRight((tag) -> {
            String string = "#" + identifier.toString();
            if (replace) {
               timer.method_22593(string);
            }

            timer.setEvent(string, l, new FunctionTagTimerCallback(identifier));
            source.sendFeedback(new TranslatableText("commands.schedule.created.tag", new Object[]{identifier, time, l}), true);
         });
         return (int)Math.floorMod(l, 2147483647L);
      }
   }

   private static int method_22833(ServerCommandSource serverCommandSource, String string) throws CommandSyntaxException {
      int i = serverCommandSource.getMinecraftServer().getSaveProperties().getMainWorldProperties().getScheduledEvents().method_22593(string);
      if (i == 0) {
         throw CLEARED_FAILURE_EXCEPTION.create(string);
      } else {
         serverCommandSource.sendFeedback(new TranslatableText("commands.schedule.cleared.success", new Object[]{i, string}), true);
         return i;
      }
   }
}
