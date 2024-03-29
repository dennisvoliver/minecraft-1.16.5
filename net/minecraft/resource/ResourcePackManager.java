package net.minecraft.resource;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

/**
 * A resource pack manager manages a list of {@link ResourcePackProfile}s and
 * builds {@linkplain #createResourcePacks() a list of resource packs} when the
 * resource manager reloads.
 */
public class ResourcePackManager implements AutoCloseable {
   private final Set<ResourcePackProvider> providers;
   private Map<String, ResourcePackProfile> profiles;
   private List<ResourcePackProfile> enabled;
   private final ResourcePackProfile.Factory profileFactory;

   public ResourcePackManager(ResourcePackProfile.Factory profileFactory, ResourcePackProvider... providers) {
      this.profiles = ImmutableMap.of();
      this.enabled = ImmutableList.of();
      this.profileFactory = profileFactory;
      this.providers = ImmutableSet.copyOf((Object[])providers);
   }

   public ResourcePackManager(ResourcePackProvider... resourcePackProviders) {
      this(ResourcePackProfile::new, resourcePackProviders);
   }

   public void scanPacks() {
      List<String> list = (List)this.enabled.stream().map(ResourcePackProfile::getName).collect(ImmutableList.toImmutableList());
      this.close();
      this.profiles = this.providePackProfiles();
      this.enabled = this.buildEnabledProfiles(list);
   }

   private Map<String, ResourcePackProfile> providePackProfiles() {
      Map<String, ResourcePackProfile> map = Maps.newTreeMap();
      Iterator var2 = this.providers.iterator();

      while(var2.hasNext()) {
         ResourcePackProvider resourcePackProvider = (ResourcePackProvider)var2.next();
         resourcePackProvider.register((resourcePackProfile) -> {
            ResourcePackProfile var10000 = (ResourcePackProfile)map.put(resourcePackProfile.getName(), resourcePackProfile);
         }, this.profileFactory);
      }

      return ImmutableMap.copyOf((Map)map);
   }

   public void setEnabledProfiles(Collection<String> enabled) {
      this.enabled = this.buildEnabledProfiles(enabled);
   }

   private List<ResourcePackProfile> buildEnabledProfiles(Collection<String> enabledNames) {
      List<ResourcePackProfile> list = (List)this.streamProfilesByName(enabledNames).collect(Collectors.toList());
      Iterator var3 = this.profiles.values().iterator();

      while(var3.hasNext()) {
         ResourcePackProfile resourcePackProfile = (ResourcePackProfile)var3.next();
         if (resourcePackProfile.isAlwaysEnabled() && !list.contains(resourcePackProfile)) {
            resourcePackProfile.getInitialPosition().insert(list, resourcePackProfile, Functions.identity(), false);
         }
      }

      return ImmutableList.copyOf((Collection)list);
   }

   private Stream<ResourcePackProfile> streamProfilesByName(Collection<String> names) {
      Stream var10000 = names.stream();
      Map var10001 = this.profiles;
      var10001.getClass();
      return var10000.map(var10001::get).filter(Objects::nonNull);
   }

   public Collection<String> getNames() {
      return this.profiles.keySet();
   }

   public Collection<ResourcePackProfile> getProfiles() {
      return this.profiles.values();
   }

   public Collection<String> getEnabledNames() {
      return (Collection)this.enabled.stream().map(ResourcePackProfile::getName).collect(ImmutableSet.toImmutableSet());
   }

   public Collection<ResourcePackProfile> getEnabledProfiles() {
      return this.enabled;
   }

   @Nullable
   public ResourcePackProfile getProfile(String name) {
      return (ResourcePackProfile)this.profiles.get(name);
   }

   public void close() {
      this.profiles.values().forEach(ResourcePackProfile::close);
   }

   public boolean hasProfile(String name) {
      return this.profiles.containsKey(name);
   }

   public List<ResourcePack> createResourcePacks() {
      return (List)this.enabled.stream().map(ResourcePackProfile::createResourcePack).collect(ImmutableList.toImmutableList());
   }
}
