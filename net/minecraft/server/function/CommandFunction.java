package net.minecraft.server.function;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CommandFunction {
   private final CommandFunction.Element[] elements;
   private final Identifier id;

   public CommandFunction(Identifier id, CommandFunction.Element[] elements) {
      this.id = id;
      this.elements = elements;
   }

   public Identifier getId() {
      return this.id;
   }

   public CommandFunction.Element[] getElements() {
      return this.elements;
   }

   /**
    * Parses a function in the context of {@code source}.
    * 
    * <p>Any syntax errors, such as improper comment lines or unknown commands, will be thrown at this point.
    * 
    * @param lines the raw lines (including comments) read from a function file
    */
   public static CommandFunction create(Identifier id, CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, List<String> lines) {
      List<CommandFunction.Element> list = Lists.newArrayListWithCapacity(lines.size());

      for(int i = 0; i < lines.size(); ++i) {
         int j = i + 1;
         String string = ((String)lines.get(i)).trim();
         StringReader stringReader = new StringReader(string);
         if (stringReader.canRead() && stringReader.peek() != '#') {
            if (stringReader.peek() == '/') {
               stringReader.skip();
               if (stringReader.peek() == '/') {
                  throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
               }

               String string2 = stringReader.readUnquotedString();
               throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)");
            }

            try {
               ParseResults<ServerCommandSource> parseResults = dispatcher.parse((StringReader)stringReader, source);
               if (parseResults.getReader().canRead()) {
                  throw CommandManager.getException(parseResults);
               }

               list.add(new CommandFunction.CommandElement(parseResults));
            } catch (CommandSyntaxException var10) {
               throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var10.getMessage());
            }
         }
      }

      return new CommandFunction(id, (CommandFunction.Element[])list.toArray(new CommandFunction.Element[0]));
   }

   public static class LazyContainer {
      public static final CommandFunction.LazyContainer EMPTY = new CommandFunction.LazyContainer((Identifier)null);
      @Nullable
      private final Identifier id;
      private boolean initialized;
      private Optional<CommandFunction> function = Optional.empty();

      public LazyContainer(@Nullable Identifier id) {
         this.id = id;
      }

      public LazyContainer(CommandFunction function) {
         this.initialized = true;
         this.id = null;
         this.function = Optional.of(function);
      }

      public Optional<CommandFunction> get(CommandFunctionManager manager) {
         if (!this.initialized) {
            if (this.id != null) {
               this.function = manager.getFunction(this.id);
            }

            this.initialized = true;
         }

         return this.function;
      }

      @Nullable
      public Identifier getId() {
         return (Identifier)this.function.map((commandFunction) -> {
            return commandFunction.id;
         }).orElse(this.id);
      }
   }

   public static class FunctionElement implements CommandFunction.Element {
      private final CommandFunction.LazyContainer function;

      public FunctionElement(CommandFunction commandFunction) {
         this.function = new CommandFunction.LazyContainer(commandFunction);
      }

      public void execute(CommandFunctionManager manager, ServerCommandSource source, ArrayDeque<CommandFunctionManager.Entry> stack, int maxChainLength) {
         this.function.get(manager).ifPresent((commandFunction) -> {
            CommandFunction.Element[] elements = commandFunction.getElements();
            int j = maxChainLength - stack.size();
            int k = Math.min(elements.length, j);

            for(int l = k - 1; l >= 0; --l) {
               stack.addFirst(new CommandFunctionManager.Entry(manager, source, elements[l]));
            }

         });
      }

      public String toString() {
         return "function " + this.function.getId();
      }
   }

   public static class CommandElement implements CommandFunction.Element {
      private final ParseResults<ServerCommandSource> parsed;

      public CommandElement(ParseResults<ServerCommandSource> parsed) {
         this.parsed = parsed;
      }

      public void execute(CommandFunctionManager manager, ServerCommandSource source, ArrayDeque<CommandFunctionManager.Entry> stack, int maxChainLength) throws CommandSyntaxException {
         manager.getDispatcher().execute(new ParseResults(this.parsed.getContext().withSource(source), this.parsed.getReader(), this.parsed.getExceptions()));
      }

      public String toString() {
         return this.parsed.getReader().getString();
      }
   }

   public interface Element {
      void execute(CommandFunctionManager manager, ServerCommandSource source, ArrayDeque<CommandFunctionManager.Entry> stack, int maxChainLength) throws CommandSyntaxException;
   }
}
