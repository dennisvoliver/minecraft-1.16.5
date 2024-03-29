package net.minecraft.server.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FunctionLoader implements ResourceReloader {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int PATH_PREFIX_LENGTH = "functions/".length();
   private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
   private volatile Map<Identifier, CommandFunction> functions = ImmutableMap.of();
   private final TagGroupLoader<CommandFunction> tagLoader = new TagGroupLoader(this::get, "tags/functions", "function");
   private volatile TagGroup<CommandFunction> tags = TagGroup.createEmpty();
   private final int level;
   private final CommandDispatcher<ServerCommandSource> commandDispatcher;

   public Optional<CommandFunction> get(Identifier id) {
      return Optional.ofNullable(this.functions.get(id));
   }

   public Map<Identifier, CommandFunction> getFunctions() {
      return this.functions;
   }

   public TagGroup<CommandFunction> getTags() {
      return this.tags;
   }

   public Tag<CommandFunction> getOrCreateTag(Identifier id) {
      return this.tags.getTagOrEmpty(id);
   }

   public FunctionLoader(int level, CommandDispatcher<ServerCommandSource> commandDispatcher) {
      this.level = level;
      this.commandDispatcher = commandDispatcher;
   }

   public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture<Map<Identifier, Tag.Builder>> completableFuture = this.tagLoader.prepareReload(manager, prepareExecutor);
      CompletableFuture<Map<Identifier, CompletableFuture<CommandFunction>>> completableFuture2 = CompletableFuture.supplyAsync(() -> {
         return manager.findResources("functions", (string) -> {
            return string.endsWith(".mcfunction");
         });
      }, prepareExecutor).thenCompose((collection) -> {
         Map<Identifier, CompletableFuture<CommandFunction>> map = Maps.newHashMap();
         ServerCommandSource serverCommandSource = new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ZERO, Vec2f.ZERO, (ServerWorld)null, this.level, "", LiteralText.EMPTY, (MinecraftServer)null, (Entity)null);
         Iterator var6 = collection.iterator();

         while(var6.hasNext()) {
            Identifier identifier = (Identifier)var6.next();
            String string = identifier.getPath();
            Identifier identifier2 = new Identifier(identifier.getNamespace(), string.substring(PATH_PREFIX_LENGTH, string.length() - PATH_SUFFIX_LENGTH));
            map.put(identifier2, CompletableFuture.supplyAsync(() -> {
               List<String> list = readLines(manager, identifier);
               return CommandFunction.create(identifier2, this.commandDispatcher, serverCommandSource, list);
            }, prepareExecutor));
         }

         CompletableFuture<?>[] completableFutures = (CompletableFuture[])map.values().toArray(new CompletableFuture[0]);
         return CompletableFuture.allOf(completableFutures).handle((void_, throwable) -> {
            return map;
         });
      });
      CompletableFuture var10000 = completableFuture.thenCombine(completableFuture2, Pair::of);
      synchronizer.getClass();
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((pair) -> {
         Map<Identifier, CompletableFuture<CommandFunction>> map = (Map)pair.getSecond();
         Builder<Identifier, CommandFunction> builder = ImmutableMap.builder();
         map.forEach((identifier, completableFuture) -> {
            completableFuture.handle((commandFunction, throwable) -> {
               if (throwable != null) {
                  LOGGER.error((String)"Failed to load function {}", (Object)identifier, (Object)throwable);
               } else {
                  builder.put(identifier, commandFunction);
               }

               return null;
            }).join();
         });
         this.functions = builder.build();
         this.tags = this.tagLoader.buildGroup((Map)pair.getFirst());
      }, applyExecutor);
   }

   private static List<String> readLines(ResourceManager resourceManager, Identifier id) {
      try {
         Resource resource = resourceManager.getResource(id);
         Throwable var3 = null;

         List var4;
         try {
            var4 = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8);
         } catch (Throwable var14) {
            var3 = var14;
            throw var14;
         } finally {
            if (resource != null) {
               if (var3 != null) {
                  try {
                     resource.close();
                  } catch (Throwable var13) {
                     var3.addSuppressed(var13);
                  }
               } else {
                  resource.close();
               }
            }

         }

         return var4;
      } catch (IOException var16) {
         throw new CompletionException(var16);
      }
   }
}
