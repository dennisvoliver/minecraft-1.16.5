package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class DataCommand {
   private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.merge.failed"));
   private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.get.invalid", new Object[]{object});
   });
   private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.get.unknown", new Object[]{object});
   });
   private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.data.get.multiple"));
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_LIST_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.modify.expected_list", new Object[]{object});
   });
   private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.modify.expected_object", new Object[]{object});
   });
   private static final DynamicCommandExceptionType MODIFY_INVALID_INDEX_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("commands.data.modify.invalid_index", new Object[]{object});
   });
   public static final List<Function<String, DataCommand.ObjectType>> OBJECT_TYPE_FACTORIES;
   public static final List<DataCommand.ObjectType> TARGET_OBJECT_TYPES;
   public static final List<DataCommand.ObjectType> SOURCE_OBJECT_TYPES;

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("data").requires((serverCommandSource) -> {
         return serverCommandSource.hasPermissionLevel(2);
      });
      Iterator var2 = TARGET_OBJECT_TYPES.iterator();

      while(var2.hasNext()) {
         DataCommand.ObjectType objectType = (DataCommand.ObjectType)var2.next();
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(objectType.addArgumentsToBuilder(CommandManager.literal("merge"), (argumentBuilder) -> {
            return argumentBuilder.then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes((commandContext) -> {
               return executeMerge((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtCompoundArgumentType.getNbtCompound(commandContext, "nbt"));
            }));
         }))).then(objectType.addArgumentsToBuilder(CommandManager.literal("get"), (argumentBuilder) -> {
            return argumentBuilder.executes((commandContext) -> {
               return executeGet((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext));
            }).then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes((commandContext) -> {
               return executeGet((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"));
            })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((commandContext) -> {
               return executeGet((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"), DoubleArgumentType.getDouble(commandContext, "scale"));
            })));
         }))).then(objectType.addArgumentsToBuilder(CommandManager.literal("remove"), (argumentBuilder) -> {
            return argumentBuilder.then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes((commandContext) -> {
               return executeRemove((ServerCommandSource)commandContext.getSource(), objectType.getObject(commandContext), NbtPathArgumentType.getNbtPath(commandContext, "path"));
            }));
         }))).then(addModifyArgument((argumentBuilder, modifyArgumentCreator) -> {
            argumentBuilder.then(CommandManager.literal("insert").then(CommandManager.argument("index", IntegerArgumentType.integer()).then(modifyArgumentCreator.create((commandContext, nbtCompound, nbtPath, list) -> {
               int i = IntegerArgumentType.getInteger(commandContext, "index");
               return executeInsert(i, nbtCompound, nbtPath, list);
            })))).then(CommandManager.literal("prepend").then(modifyArgumentCreator.create((commandContext, nbtCompound, nbtPath, list) -> {
               return executeInsert(0, nbtCompound, nbtPath, list);
            }))).then(CommandManager.literal("append").then(modifyArgumentCreator.create((commandContext, nbtCompound, nbtPath, list) -> {
               return executeInsert(-1, nbtCompound, nbtPath, list);
            }))).then(CommandManager.literal("set").then(modifyArgumentCreator.create((commandContext, nbtCompound, nbtPath, list) -> {
               NbtElement var10002 = (NbtElement)Iterables.getLast(list);
               var10002.getClass();
               return nbtPath.put(nbtCompound, var10002::copy);
            }))).then(CommandManager.literal("merge").then(modifyArgumentCreator.create((commandContext, nbtCompound, nbtPath, list) -> {
               Collection<NbtElement> collection = nbtPath.getOrInit(nbtCompound, NbtCompound::new);
               int i = 0;

               NbtCompound nbtCompound2;
               NbtCompound nbtCompound3;
               for(Iterator var6 = collection.iterator(); var6.hasNext(); i += nbtCompound3.equals(nbtCompound2) ? 0 : 1) {
                  NbtElement nbtElement = (NbtElement)var6.next();
                  if (!(nbtElement instanceof NbtCompound)) {
                     throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(nbtElement);
                  }

                  nbtCompound2 = (NbtCompound)nbtElement;
                  nbtCompound3 = nbtCompound2.copy();
                  Iterator var10 = list.iterator();

                  while(var10.hasNext()) {
                     NbtElement nbtElement2 = (NbtElement)var10.next();
                     if (!(nbtElement2 instanceof NbtCompound)) {
                        throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create(nbtElement2);
                     }

                     nbtCompound2.copyFrom((NbtCompound)nbtElement2);
                  }
               }

               return i;
            })));
         }));
      }

      dispatcher.register(literalArgumentBuilder);
   }

   private static int executeInsert(int integer, NbtCompound sourceNbt, NbtPathArgumentType.NbtPath path, List<NbtElement> elements) throws CommandSyntaxException {
      Collection<NbtElement> collection = path.getOrInit(sourceNbt, NbtList::new);
      int i = 0;

      boolean bl;
      for(Iterator var6 = collection.iterator(); var6.hasNext(); i += bl ? 1 : 0) {
         NbtElement nbtElement = (NbtElement)var6.next();
         if (!(nbtElement instanceof AbstractNbtList)) {
            throw MODIFY_EXPECTED_LIST_EXCEPTION.create(nbtElement);
         }

         bl = false;
         AbstractNbtList<?> abstractNbtList = (AbstractNbtList)nbtElement;
         int j = integer < 0 ? abstractNbtList.size() + integer + 1 : integer;
         Iterator var11 = elements.iterator();

         while(var11.hasNext()) {
            NbtElement nbtElement2 = (NbtElement)var11.next();

            try {
               if (abstractNbtList.addElement(j, nbtElement2.copy())) {
                  ++j;
                  bl = true;
               }
            } catch (IndexOutOfBoundsException var14) {
               throw MODIFY_INVALID_INDEX_EXCEPTION.create(j);
            }
         }
      }

      return i;
   }

   private static ArgumentBuilder<ServerCommandSource, ?> addModifyArgument(BiConsumer<ArgumentBuilder<ServerCommandSource, ?>, DataCommand.ModifyArgumentCreator> subArgumentAdder) {
      LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("modify");
      Iterator var2 = TARGET_OBJECT_TYPES.iterator();

      while(var2.hasNext()) {
         DataCommand.ObjectType objectType = (DataCommand.ObjectType)var2.next();
         objectType.addArgumentsToBuilder(literalArgumentBuilder, (argumentBuilder) -> {
            ArgumentBuilder<ServerCommandSource, ?> argumentBuilder2 = CommandManager.argument("targetPath", NbtPathArgumentType.nbtPath());
            Iterator var4 = SOURCE_OBJECT_TYPES.iterator();

            while(var4.hasNext()) {
               DataCommand.ObjectType objectType2 = (DataCommand.ObjectType)var4.next();
               subArgumentAdder.accept(argumentBuilder2, (modifyOperation) -> {
                  return objectType2.addArgumentsToBuilder(CommandManager.literal("from"), (argumentBuilder) -> {
                     return argumentBuilder.executes((commandContext) -> {
                        List<NbtElement> list = Collections.singletonList(objectType2.getObject(commandContext).getNbt());
                        return executeModify(commandContext, objectType, modifyOperation, list);
                     }).then(CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes((commandContext) -> {
                        DataCommandObject dataCommandObject = objectType2.getObject(commandContext);
                        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(commandContext, "sourcePath");
                        List<NbtElement> list = nbtPath.get(dataCommandObject.getNbt());
                        return executeModify(commandContext, objectType, modifyOperation, list);
                     }));
                  });
               });
            }

            subArgumentAdder.accept(argumentBuilder2, (modifyOperation) -> {
               return (LiteralArgumentBuilder)CommandManager.literal("value").then(CommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes((commandContext) -> {
                  List<NbtElement> list = Collections.singletonList(NbtElementArgumentType.getNbtElement(commandContext, "value"));
                  return executeModify(commandContext, objectType, modifyOperation, list);
               }));
            });
            return argumentBuilder.then((ArgumentBuilder)argumentBuilder2);
         });
      }

      return literalArgumentBuilder;
   }

   private static int executeModify(CommandContext<ServerCommandSource> context, DataCommand.ObjectType objectType, DataCommand.ModifyOperation modifier, List<NbtElement> elements) throws CommandSyntaxException {
      DataCommandObject dataCommandObject = objectType.getObject(context);
      NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
      NbtCompound nbtCompound = dataCommandObject.getNbt();
      int i = modifier.modify(context, nbtCompound, nbtPath, elements);
      if (i == 0) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         dataCommandObject.setNbt(nbtCompound);
         ((ServerCommandSource)context.getSource()).sendFeedback(dataCommandObject.feedbackModify(), true);
         return i;
      }
   }

   private static int executeRemove(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      NbtCompound nbtCompound = object.getNbt();
      int i = path.remove(nbtCompound);
      if (i == 0) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         object.setNbt(nbtCompound);
         source.sendFeedback(object.feedbackModify(), true);
         return i;
      }
   }

   private static NbtElement getNbt(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
      Collection<NbtElement> collection = path.get(object.getNbt());
      Iterator<NbtElement> iterator = collection.iterator();
      NbtElement nbtElement = (NbtElement)iterator.next();
      if (iterator.hasNext()) {
         throw GET_MULTIPLE_EXCEPTION.create();
      } else {
         return nbtElement;
      }
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
      NbtElement nbtElement = getNbt(path, object);
      int m;
      if (nbtElement instanceof AbstractNbtNumber) {
         m = MathHelper.floor(((AbstractNbtNumber)nbtElement).doubleValue());
      } else if (nbtElement instanceof AbstractNbtList) {
         m = ((AbstractNbtList)nbtElement).size();
      } else if (nbtElement instanceof NbtCompound) {
         m = ((NbtCompound)nbtElement).getSize();
      } else {
         if (!(nbtElement instanceof NbtString)) {
            throw GET_UNKNOWN_EXCEPTION.create(path.toString());
         }

         m = nbtElement.asString().length();
      }

      source.sendFeedback(object.feedbackQuery(nbtElement), false);
      return m;
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
      NbtElement nbtElement = getNbt(path, object);
      if (!(nbtElement instanceof AbstractNbtNumber)) {
         throw GET_INVALID_EXCEPTION.create(path.toString());
      } else {
         int i = MathHelper.floor(((AbstractNbtNumber)nbtElement).doubleValue() * scale);
         source.sendFeedback(object.feedbackGet(path, scale, i), false);
         return i;
      }
   }

   private static int executeGet(ServerCommandSource source, DataCommandObject object) throws CommandSyntaxException {
      source.sendFeedback(object.feedbackQuery(object.getNbt()), false);
      return 1;
   }

   private static int executeMerge(ServerCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
      NbtCompound nbtCompound = object.getNbt();
      NbtCompound nbtCompound2 = nbtCompound.copy().copyFrom(nbt);
      if (nbtCompound.equals(nbtCompound2)) {
         throw MERGE_FAILED_EXCEPTION.create();
      } else {
         object.setNbt(nbtCompound2);
         source.sendFeedback(object.feedbackModify(), true);
         return 1;
      }
   }

   static {
      OBJECT_TYPE_FACTORIES = ImmutableList.of(EntityDataObject.TYPE_FACTORY, BlockDataObject.TYPE_FACTORY, StorageDataObject.TYPE_FACTORY);
      TARGET_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map((function) -> {
         return (DataCommand.ObjectType)function.apply("target");
      }).collect(ImmutableList.toImmutableList());
      SOURCE_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map((function) -> {
         return (DataCommand.ObjectType)function.apply("source");
      }).collect(ImmutableList.toImmutableList());
   }

   public interface ObjectType {
      DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

      ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder);
   }

   interface ModifyArgumentCreator {
      ArgumentBuilder<ServerCommandSource, ?> create(DataCommand.ModifyOperation modifier);
   }

   interface ModifyOperation {
      int modify(CommandContext<ServerCommandSource> context, NbtCompound sourceTag, NbtPathArgumentType.NbtPath path, List<NbtElement> tags) throws CommandSyntaxException;
   }
}
