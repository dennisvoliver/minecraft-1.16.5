package net.minecraft.client.resource;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ResourceIndex {
   protected static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, File> rootIndex = Maps.newHashMap();
   private final Map<Identifier, File> field_21556 = Maps.newHashMap();

   protected ResourceIndex() {
   }

   public ResourceIndex(File directory, String indexName) {
      File file = new File(directory, "objects");
      File file2 = new File(directory, "indexes/" + indexName + ".json");
      BufferedReader bufferedReader = null;

      try {
         bufferedReader = Files.newReader(file2, StandardCharsets.UTF_8);
         JsonObject jsonObject = JsonHelper.deserialize((Reader)bufferedReader);
         JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "objects", (JsonObject)null);
         if (jsonObject2 != null) {
            Iterator var8 = jsonObject2.entrySet().iterator();

            while(var8.hasNext()) {
               Entry<String, JsonElement> entry = (Entry)var8.next();
               JsonObject jsonObject3 = (JsonObject)entry.getValue();
               String string = (String)entry.getKey();
               String[] strings = string.split("/", 2);
               String string2 = JsonHelper.getString(jsonObject3, "hash");
               File file3 = new File(file, string2.substring(0, 2) + "/" + string2);
               if (strings.length == 1) {
                  this.rootIndex.put(strings[0], file3);
               } else {
                  this.field_21556.put(new Identifier(strings[0], strings[1]), file3);
               }
            }
         }
      } catch (JsonParseException var19) {
         LOGGER.error((String)"Unable to parse resource index file: {}", (Object)file2);
      } catch (FileNotFoundException var20) {
         LOGGER.error((String)"Can't find the resource index file: {}", (Object)file2);
      } finally {
         IOUtils.closeQuietly((Reader)bufferedReader);
      }

   }

   @Nullable
   public File getResource(Identifier identifier) {
      return (File)this.field_21556.get(identifier);
   }

   @Nullable
   public File findFile(String path) {
      return (File)this.rootIndex.get(path);
   }

   public Collection<Identifier> getFilesRecursively(String string, String string2, int i, Predicate<String> predicate) {
      return (Collection)this.field_21556.keySet().stream().filter((identifier) -> {
         String string3 = identifier.getPath();
         return identifier.getNamespace().equals(string2) && !string3.endsWith(".mcmeta") && string3.startsWith(string + "/") && predicate.test(string3);
      }).collect(Collectors.toList());
   }
}
