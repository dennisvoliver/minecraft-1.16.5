package net.minecraft.tag;

import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class TagManagerLoader implements ResourceReloader {
   private final TagGroupLoader<Block> blocks;
   private final TagGroupLoader<Item> items;
   private final TagGroupLoader<Fluid> fluids;
   private final TagGroupLoader<EntityType<?>> entityTypes;
   private TagManager tagManager;

   public TagManagerLoader() {
      this.blocks = new TagGroupLoader(Registry.BLOCK::getOrEmpty, "tags/blocks", "block");
      this.items = new TagGroupLoader(Registry.ITEM::getOrEmpty, "tags/items", "item");
      this.fluids = new TagGroupLoader(Registry.FLUID::getOrEmpty, "tags/fluids", "fluid");
      this.entityTypes = new TagGroupLoader(Registry.ENTITY_TYPE::getOrEmpty, "tags/entity_types", "entity_type");
      this.tagManager = TagManager.EMPTY;
   }

   public TagManager getTagManager() {
      return this.tagManager;
   }

   public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture<Map<Identifier, Tag.Builder>> completableFuture = this.blocks.prepareReload(manager, prepareExecutor);
      CompletableFuture<Map<Identifier, Tag.Builder>> completableFuture2 = this.items.prepareReload(manager, prepareExecutor);
      CompletableFuture<Map<Identifier, Tag.Builder>> completableFuture3 = this.fluids.prepareReload(manager, prepareExecutor);
      CompletableFuture<Map<Identifier, Tag.Builder>> completableFuture4 = this.entityTypes.prepareReload(manager, prepareExecutor);
      CompletableFuture var10000 = CompletableFuture.allOf(completableFuture, completableFuture2, completableFuture3, completableFuture4);
      synchronizer.getClass();
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((void_) -> {
         TagGroup<Block> tagGroup = this.blocks.buildGroup((Map)completableFuture.join());
         TagGroup<Item> tagGroup2 = this.items.buildGroup((Map)completableFuture2.join());
         TagGroup<Fluid> tagGroup3 = this.fluids.buildGroup((Map)completableFuture3.join());
         TagGroup<EntityType<?>> tagGroup4 = this.entityTypes.buildGroup((Map)completableFuture4.join());
         TagManager tagManager = TagManager.create(tagGroup, tagGroup2, tagGroup3, tagGroup4);
         Multimap<Identifier, Identifier> multimap = RequiredTagListRegistry.getMissingTags(tagManager);
         if (!multimap.isEmpty()) {
            throw new IllegalStateException("Missing required tags: " + (String)multimap.entries().stream().map((entry) -> {
               return entry.getKey() + ":" + entry.getValue();
            }).sorted().collect(Collectors.joining(",")));
         } else {
            ServerTagManagerHolder.setTagManager(tagManager);
            this.tagManager = tagManager;
         }
      }, applyExecutor);
   }
}
