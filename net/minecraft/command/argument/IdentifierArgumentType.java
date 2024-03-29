package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class IdentifierArgumentType implements ArgumentType<Identifier> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType UNKNOWN_ADVANCEMENT_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("advancement.advancementNotFound", new Object[]{object});
   });
   private static final DynamicCommandExceptionType UNKNOWN_RECIPE_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("recipe.notFound", new Object[]{object});
   });
   private static final DynamicCommandExceptionType field_21506 = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("predicate.unknown", new Object[]{object});
   });
   private static final DynamicCommandExceptionType field_24267 = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("attribute.unknown", new Object[]{object});
   });

   public static IdentifierArgumentType identifier() {
      return new IdentifierArgumentType();
   }

   public static Advancement getAdvancementArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
      Identifier identifier = (Identifier)context.getArgument(argumentName, Identifier.class);
      Advancement advancement = ((ServerCommandSource)context.getSource()).getMinecraftServer().getAdvancementLoader().get(identifier);
      if (advancement == null) {
         throw UNKNOWN_ADVANCEMENT_EXCEPTION.create(identifier);
      } else {
         return advancement;
      }
   }

   public static Recipe<?> getRecipeArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
      RecipeManager recipeManager = ((ServerCommandSource)context.getSource()).getMinecraftServer().getRecipeManager();
      Identifier identifier = (Identifier)context.getArgument(argumentName, Identifier.class);
      return (Recipe)recipeManager.get(identifier).orElseThrow(() -> {
         return UNKNOWN_RECIPE_EXCEPTION.create(identifier);
      });
   }

   public static LootCondition method_23727(CommandContext<ServerCommandSource> commandContext, String string) throws CommandSyntaxException {
      Identifier identifier = (Identifier)commandContext.getArgument(string, Identifier.class);
      LootConditionManager lootConditionManager = ((ServerCommandSource)commandContext.getSource()).getMinecraftServer().getPredicateManager();
      LootCondition lootCondition = lootConditionManager.get(identifier);
      if (lootCondition == null) {
         throw field_21506.create(identifier);
      } else {
         return lootCondition;
      }
   }

   public static EntityAttribute method_27575(CommandContext<ServerCommandSource> commandContext, String string) throws CommandSyntaxException {
      Identifier identifier = (Identifier)commandContext.getArgument(string, Identifier.class);
      return (EntityAttribute)Registry.ATTRIBUTE.getOrEmpty(identifier).orElseThrow(() -> {
         return field_24267.create(identifier);
      });
   }

   public static Identifier getIdentifier(CommandContext<ServerCommandSource> context, String name) {
      return (Identifier)context.getArgument(name, Identifier.class);
   }

   public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
      return Identifier.fromCommandInput(stringReader);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
