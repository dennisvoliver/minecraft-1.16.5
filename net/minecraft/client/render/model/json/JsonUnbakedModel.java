package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class JsonUnbakedModel implements UnbakedModel {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();
   @VisibleForTesting
   static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(JsonUnbakedModel.class, new JsonUnbakedModel.Deserializer()).registerTypeAdapter(ModelElement.class, new ModelElement.Deserializer()).registerTypeAdapter(ModelElementFace.class, new ModelElementFace.Deserializer()).registerTypeAdapter(ModelElementTexture.class, new ModelElementTexture.Deserializer()).registerTypeAdapter(Transformation.class, new Transformation.Deserializer()).registerTypeAdapter(ModelTransformation.class, new ModelTransformation.Deserializer()).registerTypeAdapter(ModelOverride.class, new ModelOverride.Deserializer()).create();
   private final List<ModelElement> elements;
   @Nullable
   private final JsonUnbakedModel.GuiLight guiLight;
   private final boolean ambientOcclusion;
   private final ModelTransformation transformations;
   private final List<ModelOverride> overrides;
   public String id = "";
   @VisibleForTesting
   protected final Map<String, Either<SpriteIdentifier, String>> textureMap;
   @Nullable
   protected JsonUnbakedModel parent;
   @Nullable
   protected Identifier parentId;

   public static JsonUnbakedModel deserialize(Reader input) {
      return (JsonUnbakedModel)JsonHelper.deserialize(GSON, input, JsonUnbakedModel.class);
   }

   public static JsonUnbakedModel deserialize(String json) {
      return deserialize((Reader)(new StringReader(json)));
   }

   public JsonUnbakedModel(@Nullable Identifier parentId, List<ModelElement> elements, Map<String, Either<SpriteIdentifier, String>> textureMap, boolean ambientOcclusion, @Nullable JsonUnbakedModel.GuiLight guiLight, ModelTransformation transformations, List<ModelOverride> overrides) {
      this.elements = elements;
      this.ambientOcclusion = ambientOcclusion;
      this.guiLight = guiLight;
      this.textureMap = textureMap;
      this.parentId = parentId;
      this.transformations = transformations;
      this.overrides = overrides;
   }

   public List<ModelElement> getElements() {
      return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
   }

   public boolean useAmbientOcclusion() {
      return this.parent != null ? this.parent.useAmbientOcclusion() : this.ambientOcclusion;
   }

   public JsonUnbakedModel.GuiLight getGuiLight() {
      if (this.guiLight != null) {
         return this.guiLight;
      } else {
         return this.parent != null ? this.parent.getGuiLight() : JsonUnbakedModel.GuiLight.field_21859;
      }
   }

   public List<ModelOverride> getOverrides() {
      return this.overrides;
   }

   private ModelOverrideList compileOverrides(ModelLoader modelLoader, JsonUnbakedModel parent) {
      return this.overrides.isEmpty() ? ModelOverrideList.EMPTY : new ModelOverrideList(modelLoader, parent, modelLoader::getOrLoadModel, this.overrides);
   }

   public Collection<Identifier> getModelDependencies() {
      Set<Identifier> set = Sets.newHashSet();
      Iterator var2 = this.overrides.iterator();

      while(var2.hasNext()) {
         ModelOverride modelOverride = (ModelOverride)var2.next();
         set.add(modelOverride.getModelId());
      }

      if (this.parentId != null) {
         set.add(this.parentId);
      }

      return set;
   }

   public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
      Set<UnbakedModel> set = Sets.newLinkedHashSet();

      for(JsonUnbakedModel jsonUnbakedModel = this; jsonUnbakedModel.parentId != null && jsonUnbakedModel.parent == null; jsonUnbakedModel = jsonUnbakedModel.parent) {
         set.add(jsonUnbakedModel);
         UnbakedModel unbakedModel = (UnbakedModel)unbakedModelGetter.apply(jsonUnbakedModel.parentId);
         if (unbakedModel == null) {
            LOGGER.warn((String)"No parent '{}' while loading model '{}'", (Object)this.parentId, (Object)jsonUnbakedModel);
         }

         if (set.contains(unbakedModel)) {
            LOGGER.warn((String)"Found 'parent' loop while loading model '{}' in chain: {} -> {}", (Object)jsonUnbakedModel, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentId);
            unbakedModel = null;
         }

         if (unbakedModel == null) {
            jsonUnbakedModel.parentId = ModelLoader.MISSING_ID;
            unbakedModel = (UnbakedModel)unbakedModelGetter.apply(jsonUnbakedModel.parentId);
         }

         if (!(unbakedModel instanceof JsonUnbakedModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
         }

         jsonUnbakedModel.parent = (JsonUnbakedModel)unbakedModel;
      }

      Set<SpriteIdentifier> set2 = Sets.newHashSet((Object[])(this.resolveSprite("particle")));
      Iterator var6 = this.getElements().iterator();

      while(var6.hasNext()) {
         ModelElement modelElement = (ModelElement)var6.next();

         SpriteIdentifier spriteIdentifier;
         for(Iterator var8 = modelElement.faces.values().iterator(); var8.hasNext(); set2.add(spriteIdentifier)) {
            ModelElementFace modelElementFace = (ModelElementFace)var8.next();
            spriteIdentifier = this.resolveSprite(modelElementFace.textureId);
            if (Objects.equals(spriteIdentifier.getTextureId(), MissingSprite.getMissingSpriteId())) {
               unresolvedTextureReferences.add(Pair.of(modelElementFace.textureId, this.id));
            }
         }
      }

      this.overrides.forEach((modelOverride) -> {
         UnbakedModel unbakedModel = (UnbakedModel)unbakedModelGetter.apply(modelOverride.getModelId());
         if (!Objects.equals(unbakedModel, this)) {
            set2.addAll(unbakedModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
         }
      });
      if (this.getRootModel() == ModelLoader.GENERATION_MARKER) {
         ItemModelGenerator.LAYERS.forEach((string) -> {
            set2.add(this.resolveSprite(string));
         });
      }

      return set2;
   }

   public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
      return this.bake(loader, this, textureGetter, rotationContainer, modelId, true);
   }

   public BakedModel bake(ModelLoader loader, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier id, boolean hasDepth) {
      Sprite sprite = (Sprite)textureGetter.apply(this.resolveSprite("particle"));
      if (this.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
         return new BuiltinBakedModel(this.getTransformations(), this.compileOverrides(loader, parent), sprite, this.getGuiLight().isSide());
      } else {
         BasicBakedModel.Builder builder = (new BasicBakedModel.Builder(this, this.compileOverrides(loader, parent), hasDepth)).setParticle(sprite);
         Iterator var9 = this.getElements().iterator();

         while(var9.hasNext()) {
            ModelElement modelElement = (ModelElement)var9.next();
            Iterator var11 = modelElement.faces.keySet().iterator();

            while(var11.hasNext()) {
               Direction direction = (Direction)var11.next();
               ModelElementFace modelElementFace = (ModelElementFace)modelElement.faces.get(direction);
               Sprite sprite2 = (Sprite)textureGetter.apply(this.resolveSprite(modelElementFace.textureId));
               if (modelElementFace.cullFace == null) {
                  builder.addQuad(createQuad(modelElement, modelElementFace, sprite2, direction, settings, id));
               } else {
                  builder.addQuad(Direction.transform(settings.getRotation().getMatrix(), modelElementFace.cullFace), createQuad(modelElement, modelElementFace, sprite2, direction, settings, id));
               }
            }
         }

         return builder.build();
      }
   }

   private static BakedQuad createQuad(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side, ModelBakeSettings settings, Identifier id) {
      return QUAD_FACTORY.bake(element.from, element.to, elementFace, sprite, side, settings, element.rotation, element.shade, id);
   }

   public boolean textureExists(String name) {
      return !MissingSprite.getMissingSpriteId().equals(this.resolveSprite(name).getTextureId());
   }

   public SpriteIdentifier resolveSprite(String spriteName) {
      if (isTextureReference(spriteName)) {
         spriteName = spriteName.substring(1);
      }

      ArrayList list = Lists.newArrayList();

      while(true) {
         Either<SpriteIdentifier, String> either = this.resolveTexture(spriteName);
         Optional<SpriteIdentifier> optional = either.left();
         if (optional.isPresent()) {
            return (SpriteIdentifier)optional.get();
         }

         spriteName = (String)either.right().get();
         if (list.contains(spriteName)) {
            LOGGER.warn((String)"Unable to resolve texture due to reference chain {}->{} in {}", (Object)Joiner.on("->").join((Iterable)list), spriteName, this.id);
            return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId());
         }

         list.add(spriteName);
      }
   }

   private Either<SpriteIdentifier, String> resolveTexture(String name) {
      for(JsonUnbakedModel jsonUnbakedModel = this; jsonUnbakedModel != null; jsonUnbakedModel = jsonUnbakedModel.parent) {
         Either<SpriteIdentifier, String> either = (Either)jsonUnbakedModel.textureMap.get(name);
         if (either != null) {
            return either;
         }
      }

      return Either.left(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId()));
   }

   private static boolean isTextureReference(String reference) {
      return reference.charAt(0) == '#';
   }

   public JsonUnbakedModel getRootModel() {
      return this.parent == null ? this : this.parent.getRootModel();
   }

   public ModelTransformation getTransformations() {
      Transformation transformation = this.getTransformation(ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND);
      Transformation transformation2 = this.getTransformation(ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND);
      Transformation transformation3 = this.getTransformation(ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND);
      Transformation transformation4 = this.getTransformation(ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND);
      Transformation transformation5 = this.getTransformation(ModelTransformation.Mode.HEAD);
      Transformation transformation6 = this.getTransformation(ModelTransformation.Mode.GUI);
      Transformation transformation7 = this.getTransformation(ModelTransformation.Mode.GROUND);
      Transformation transformation8 = this.getTransformation(ModelTransformation.Mode.FIXED);
      return new ModelTransformation(transformation, transformation2, transformation3, transformation4, transformation5, transformation6, transformation7, transformation8);
   }

   private Transformation getTransformation(ModelTransformation.Mode renderMode) {
      return this.parent != null && !this.transformations.isTransformationDefined(renderMode) ? this.parent.getTransformation(renderMode) : this.transformations.getTransformation(renderMode);
   }

   public String toString() {
      return this.id;
   }

   @Environment(EnvType.CLIENT)
   public static enum GuiLight {
      /**
       * The model will be shaded from the front, like a basic item
       */
      field_21858("front"),
      /**
       * The model will be shaded from the side, like a block.
       */
      field_21859("side");

      private final String name;

      private GuiLight(String name) {
         this.name = name;
      }

      public static JsonUnbakedModel.GuiLight deserialize(String value) {
         JsonUnbakedModel.GuiLight[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            JsonUnbakedModel.GuiLight guiLight = var1[var3];
            if (guiLight.name.equals(value)) {
               return guiLight;
            }
         }

         throw new IllegalArgumentException("Invalid gui light: " + value);
      }

      public boolean isSide() {
         return this == field_21859;
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer<JsonUnbakedModel> {
      public JsonUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         List<ModelElement> list = this.deserializeElements(jsonDeserializationContext, jsonObject);
         String string = this.deserializeParent(jsonObject);
         Map<String, Either<SpriteIdentifier, String>> map = this.deserializeTextures(jsonObject);
         boolean bl = this.deserializeAmbientOcclusion(jsonObject);
         ModelTransformation modelTransformation = ModelTransformation.NONE;
         if (jsonObject.has("display")) {
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "display");
            modelTransformation = (ModelTransformation)jsonDeserializationContext.deserialize(jsonObject2, ModelTransformation.class);
         }

         List<ModelOverride> list2 = this.deserializeOverrides(jsonDeserializationContext, jsonObject);
         JsonUnbakedModel.GuiLight guiLight = null;
         if (jsonObject.has("gui_light")) {
            guiLight = JsonUnbakedModel.GuiLight.deserialize(JsonHelper.getString(jsonObject, "gui_light"));
         }

         Identifier identifier = string.isEmpty() ? null : new Identifier(string);
         return new JsonUnbakedModel(identifier, list, map, bl, guiLight, modelTransformation, list2);
      }

      protected List<ModelOverride> deserializeOverrides(JsonDeserializationContext context, JsonObject object) {
         List<ModelOverride> list = Lists.newArrayList();
         if (object.has("overrides")) {
            JsonArray jsonArray = JsonHelper.getArray(object, "overrides");
            Iterator var5 = jsonArray.iterator();

            while(var5.hasNext()) {
               JsonElement jsonElement = (JsonElement)var5.next();
               list.add(context.deserialize(jsonElement, ModelOverride.class));
            }
         }

         return list;
      }

      private Map<String, Either<SpriteIdentifier, String>> deserializeTextures(JsonObject object) {
         Identifier identifier = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
         Map<String, Either<SpriteIdentifier, String>> map = Maps.newHashMap();
         if (object.has("textures")) {
            JsonObject jsonObject = JsonHelper.getObject(object, "textures");
            Iterator var5 = jsonObject.entrySet().iterator();

            while(var5.hasNext()) {
               Entry<String, JsonElement> entry = (Entry)var5.next();
               map.put(entry.getKey(), resolveReference(identifier, ((JsonElement)entry.getValue()).getAsString()));
            }
         }

         return map;
      }

      private static Either<SpriteIdentifier, String> resolveReference(Identifier id, String name) {
         if (JsonUnbakedModel.isTextureReference(name)) {
            return Either.right(name.substring(1));
         } else {
            Identifier identifier = Identifier.tryParse(name);
            if (identifier == null) {
               throw new JsonParseException(name + " is not valid resource location");
            } else {
               return Either.left(new SpriteIdentifier(id, identifier));
            }
         }
      }

      private String deserializeParent(JsonObject json) {
         return JsonHelper.getString(json, "parent", "");
      }

      protected boolean deserializeAmbientOcclusion(JsonObject json) {
         return JsonHelper.getBoolean(json, "ambientocclusion", true);
      }

      protected List<ModelElement> deserializeElements(JsonDeserializationContext context, JsonObject json) {
         List<ModelElement> list = Lists.newArrayList();
         if (json.has("elements")) {
            Iterator var4 = JsonHelper.getArray(json, "elements").iterator();

            while(var4.hasNext()) {
               JsonElement jsonElement = (JsonElement)var4.next();
               list.add(context.deserialize(jsonElement, ModelElement.class));
            }
         }

         return list;
      }
   }
}
