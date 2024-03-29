package net.minecraft.util.dynamic;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryOps<T> extends ForwardingDynamicOps<T> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RegistryOps.EntryLoader entryLoader;
   private final DynamicRegistryManager.Impl registryManager;
   private final Map<RegistryKey<? extends Registry<?>>, RegistryOps.ValueHolder<?>> valueHolders;
   private final RegistryOps<JsonElement> entryOps;

   public static <T> RegistryOps<T> of(DynamicOps<T> delegate, ResourceManager resourceManager, DynamicRegistryManager.Impl impl) {
      return of(delegate, RegistryOps.EntryLoader.resourceBacked(resourceManager), impl);
   }

   public static <T> RegistryOps<T> of(DynamicOps<T> dynamicOps, RegistryOps.EntryLoader entryLoader, DynamicRegistryManager.Impl impl) {
      RegistryOps<T> registryOps = new RegistryOps(dynamicOps, entryLoader, impl, Maps.newIdentityHashMap());
      DynamicRegistryManager.load(impl, registryOps);
      return registryOps;
   }

   private RegistryOps(DynamicOps<T> delegate, RegistryOps.EntryLoader entryLoader, DynamicRegistryManager.Impl impl, IdentityHashMap<RegistryKey<? extends Registry<?>>, RegistryOps.ValueHolder<?>> identityHashMap) {
      super(delegate);
      this.entryLoader = entryLoader;
      this.registryManager = impl;
      this.valueHolders = identityHashMap;
      this.entryOps = delegate == JsonOps.INSTANCE ? this : new RegistryOps(JsonOps.INSTANCE, entryLoader, impl, identityHashMap);
   }

   /**
    * Encode an id for a registry element than a full object if possible.
    * 
    * <p>This method is called by casting an arbitrary dynamic ops to a registry
    * reading ops.
    * 
    * @see RegistryReadingOps#encodeOrId(Object, Object, RegistryKey, Codec)
    */
   protected <E> DataResult<Pair<Supplier<E>, T>> decodeOrId(T object, RegistryKey<? extends Registry<E>> key, Codec<E> codec, boolean allowInlineDefinitions) {
      Optional<MutableRegistry<E>> optional = this.registryManager.getOptionalMutable(key);
      if (!optional.isPresent()) {
         return DataResult.error("Unknown registry: " + key);
      } else {
         MutableRegistry<E> mutableRegistry = (MutableRegistry)optional.get();
         DataResult<Pair<Identifier, T>> dataResult = Identifier.CODEC.decode(this.delegate, object);
         if (!dataResult.result().isPresent()) {
            return !allowInlineDefinitions ? DataResult.error("Inline definitions not allowed here") : codec.decode(this, object).map((pairx) -> {
               return pairx.mapFirst((object) -> {
                  return () -> {
                     return object;
                  };
               });
            });
         } else {
            Pair<Identifier, T> pair = (Pair)dataResult.result().get();
            Identifier identifier = (Identifier)pair.getFirst();
            return this.readSupplier(key, mutableRegistry, codec, identifier).map((supplier) -> {
               return Pair.of(supplier, pair.getSecond());
            });
         }
      }
   }

   /**
    * Loads elements into a registry just loaded from a decoder.
    */
   public <E> DataResult<SimpleRegistry<E>> loadToRegistry(SimpleRegistry<E> registry, RegistryKey<? extends Registry<E>> key, Codec<E> codec) {
      Collection<Identifier> collection = this.entryLoader.getKnownEntryPaths(key);
      DataResult<SimpleRegistry<E>> dataResult = DataResult.success(registry, Lifecycle.stable());
      String string = key.getValue().getPath() + "/";
      Iterator var7 = collection.iterator();

      while(var7.hasNext()) {
         Identifier identifier = (Identifier)var7.next();
         String string2 = identifier.getPath();
         if (!string2.endsWith(".json")) {
            LOGGER.warn((String)"Skipping resource {} since it is not a json file", (Object)identifier);
         } else if (!string2.startsWith(string)) {
            LOGGER.warn((String)"Skipping resource {} since it does not have a registry name prefix", (Object)identifier);
         } else {
            String string3 = string2.substring(string.length(), string2.length() - ".json".length());
            Identifier identifier2 = new Identifier(identifier.getNamespace(), string3);
            dataResult = dataResult.flatMap((simpleRegistry) -> {
               return this.readSupplier(key, simpleRegistry, codec, identifier2).map((supplier) -> {
                  return simpleRegistry;
               });
            });
         }
      }

      return dataResult.setPartial((Object)registry);
   }

   /**
    * Reads a supplier for a registry element.
    * 
    * <p>This logic is used by both {@code decodeOrId} and {@code loadToRegistry}.
    */
   private <E> DataResult<Supplier<E>> readSupplier(RegistryKey<? extends Registry<E>> key, MutableRegistry<E> registry, Codec<E> codec, Identifier elementId) {
      RegistryKey<E> registryKey = RegistryKey.of(key, elementId);
      RegistryOps.ValueHolder<E> valueHolder = this.getValueHolder(key);
      DataResult<Supplier<E>> dataResult = (DataResult)valueHolder.values.get(registryKey);
      if (dataResult != null) {
         return dataResult;
      } else {
         Supplier<E> supplier = Suppliers.memoize(() -> {
            E object = registry.get(registryKey);
            if (object == null) {
               throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + registryKey);
            } else {
               return object;
            }
         });
         valueHolder.values.put(registryKey, DataResult.success(supplier));
         DataResult<Pair<E, OptionalInt>> dataResult2 = this.entryLoader.load(this.entryOps, key, registryKey, codec);
         Optional<Pair<E, OptionalInt>> optional = dataResult2.result();
         if (optional.isPresent()) {
            Pair<E, OptionalInt> pair = (Pair)optional.get();
            registry.replace((OptionalInt)pair.getSecond(), registryKey, pair.getFirst(), dataResult2.lifecycle());
         }

         DataResult dataResult4;
         if (!optional.isPresent() && registry.get(registryKey) != null) {
            dataResult4 = DataResult.success(() -> {
               return registry.get(registryKey);
            }, Lifecycle.stable());
         } else {
            dataResult4 = dataResult2.map((pairx) -> {
               return () -> {
                  return registry.get(registryKey);
               };
            });
         }

         valueHolder.values.put(registryKey, dataResult4);
         return dataResult4;
      }
   }

   private <E> RegistryOps.ValueHolder<E> getValueHolder(RegistryKey<? extends Registry<E>> registryRef) {
      return (RegistryOps.ValueHolder)this.valueHolders.computeIfAbsent(registryRef, (registryKey) -> {
         return new RegistryOps.ValueHolder();
      });
   }

   protected <E> DataResult<Registry<E>> method_31152(RegistryKey<? extends Registry<E>> registryKey) {
      return (DataResult)this.registryManager.getOptionalMutable(registryKey).map((mutableRegistry) -> {
         return DataResult.success(mutableRegistry, mutableRegistry.getLifecycle());
      }).orElseGet(() -> {
         return DataResult.error("Unknown registry: " + registryKey);
      });
   }

   public interface EntryLoader {
      /**
       * @return A collection of file Identifiers of all known entries of the given registry.
       * Note that these are file Identifiers for use in a resource manager, not the logical names of the entries.
       */
      Collection<Identifier> getKnownEntryPaths(RegistryKey<? extends Registry<?>> registryKey);

      <E> DataResult<Pair<E, OptionalInt>> load(DynamicOps<JsonElement> dynamicOps, RegistryKey<? extends Registry<E>> registryId, RegistryKey<E> entryId, Decoder<E> decoder);

      static RegistryOps.EntryLoader resourceBacked(final ResourceManager resourceManager) {
         return new RegistryOps.EntryLoader() {
            public Collection<Identifier> getKnownEntryPaths(RegistryKey<? extends Registry<?>> registryKey) {
               return resourceManager.findResources(registryKey.getValue().getPath(), (string) -> {
                  return string.endsWith(".json");
               });
            }

            public <E> DataResult<Pair<E, OptionalInt>> load(DynamicOps<JsonElement> dynamicOps, RegistryKey<? extends Registry<E>> registryId, RegistryKey<E> entryId, Decoder<E> decoder) {
               Identifier identifier = entryId.getValue();
               Identifier identifier2 = new Identifier(identifier.getNamespace(), registryId.getValue().getPath() + "/" + identifier.getPath() + ".json");

               try {
                  Resource resource = resourceManager.getResource(identifier2);
                  Throwable var8 = null;

                  DataResult var13;
                  try {
                     Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                     Throwable var10 = null;

                     try {
                        JsonParser jsonParser = new JsonParser();
                        JsonElement jsonElement = jsonParser.parse((Reader)reader);
                        var13 = decoder.parse(dynamicOps, jsonElement).map((object) -> {
                           return Pair.of(object, OptionalInt.empty());
                        });
                     } catch (Throwable var38) {
                        var10 = var38;
                        throw var38;
                     } finally {
                        if (reader != null) {
                           if (var10 != null) {
                              try {
                                 reader.close();
                              } catch (Throwable var37) {
                                 var10.addSuppressed(var37);
                              }
                           } else {
                              reader.close();
                           }
                        }

                     }
                  } catch (Throwable var40) {
                     var8 = var40;
                     throw var40;
                  } finally {
                     if (resource != null) {
                        if (var8 != null) {
                           try {
                              resource.close();
                           } catch (Throwable var36) {
                              var8.addSuppressed(var36);
                           }
                        } else {
                           resource.close();
                        }
                     }

                  }

                  return var13;
               } catch (JsonIOException | JsonSyntaxException | IOException var42) {
                  return DataResult.error("Failed to parse " + identifier2 + " file: " + var42.getMessage());
               }
            }

            public String toString() {
               return "ResourceAccess[" + resourceManager + "]";
            }
         };
      }

      public static final class Impl implements RegistryOps.EntryLoader {
         private final Map<RegistryKey<?>, JsonElement> values = Maps.newIdentityHashMap();
         private final Object2IntMap<RegistryKey<?>> entryToRawId = new Object2IntOpenCustomHashMap(Util.identityHashStrategy());
         private final Map<RegistryKey<?>, Lifecycle> entryToLifecycle = Maps.newIdentityHashMap();

         public <E> void add(DynamicRegistryManager.Impl impl, RegistryKey<E> registryKey, Encoder<E> encoder, int rawId, E object, Lifecycle lifecycle) {
            DataResult<JsonElement> dataResult = encoder.encodeStart(RegistryReadingOps.of(JsonOps.INSTANCE, impl), object);
            Optional<PartialResult<JsonElement>> optional = dataResult.error();
            if (optional.isPresent()) {
               RegistryOps.LOGGER.error((String)"Error adding element: {}", (Object)((PartialResult)optional.get()).message());
            } else {
               this.values.put(registryKey, dataResult.result().get());
               this.entryToRawId.put(registryKey, rawId);
               this.entryToLifecycle.put(registryKey, lifecycle);
            }
         }

         public Collection<Identifier> getKnownEntryPaths(RegistryKey<? extends Registry<?>> registryKey) {
            return (Collection)this.values.keySet().stream().filter((registryKey2) -> {
               return registryKey2.isOf(registryKey);
            }).map((registryKey2) -> {
               return new Identifier(registryKey2.getValue().getNamespace(), registryKey.getValue().getPath() + "/" + registryKey2.getValue().getPath() + ".json");
            }).collect(Collectors.toList());
         }

         public <E> DataResult<Pair<E, OptionalInt>> load(DynamicOps<JsonElement> dynamicOps, RegistryKey<? extends Registry<E>> registryId, RegistryKey<E> entryId, Decoder<E> decoder) {
            JsonElement jsonElement = (JsonElement)this.values.get(entryId);
            return jsonElement == null ? DataResult.error("Unknown element: " + entryId) : decoder.parse(dynamicOps, jsonElement).setLifecycle((Lifecycle)this.entryToLifecycle.get(entryId)).map((object) -> {
               return Pair.of(object, OptionalInt.of(this.entryToRawId.getInt(entryId)));
            });
         }
      }
   }

   static final class ValueHolder<E> {
      private final Map<RegistryKey<E>, DataResult<Supplier<E>>> values;

      private ValueHolder() {
         this.values = Maps.newIdentityHashMap();
      }
   }
}
