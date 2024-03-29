package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");
   private static final Text ENTER_SEED_TEXT = new TranslatableText("selectWorld.enterSeed");
   private static final Text SEED_INFO_TEXT = new TranslatableText("selectWorld.seedInfo");
   private static final Text ENTER_NAME_TEXT = new TranslatableText("selectWorld.enterName");
   private static final Text RESULT_FOLDER_TEXT = new TranslatableText("selectWorld.resultFolder");
   private static final Text ALLOW_COMMANDS_INFO_TEXT = new TranslatableText("selectWorld.allowCommands.info");
   private final Screen parent;
   private TextFieldWidget levelNameField;
   private String saveDirectoryName;
   private CreateWorldScreen.Mode currentMode;
   @Nullable
   private CreateWorldScreen.Mode lastMode;
   private Difficulty field_24289;
   private Difficulty field_24290;
   private boolean cheatsEnabled;
   private boolean tweakedCheats;
   public boolean hardcore;
   protected DataPackSettings dataPackSettings;
   @Nullable
   private Path dataPackTempDir;
   @Nullable
   private ResourcePackManager packManager;
   private boolean moreOptionsOpen;
   private ButtonWidget createLevelButton;
   private ButtonWidget gameModeSwitchButton;
   private ButtonWidget difficultyButton;
   private ButtonWidget moreOptionsButton;
   private ButtonWidget gameRulesButton;
   private ButtonWidget dataPacksButton;
   private ButtonWidget enableCheatsButton;
   private Text firstGameModeDescriptionLine;
   private Text secondGameModeDescriptionLine;
   private String levelName;
   private GameRules gameRules;
   public final MoreOptionsDialog moreOptionsDialog;

   public CreateWorldScreen(@Nullable Screen parent, LevelInfo levelInfo, GeneratorOptions generatorOptions, @Nullable Path dataPackTempDir, DataPackSettings dataPackSettings, DynamicRegistryManager.Impl registryManager) {
      this(parent, dataPackSettings, new MoreOptionsDialog(registryManager, generatorOptions, GeneratorType.method_29078(generatorOptions), OptionalLong.of(generatorOptions.getSeed())));
      this.levelName = levelInfo.getLevelName();
      this.cheatsEnabled = levelInfo.areCommandsAllowed();
      this.tweakedCheats = true;
      this.field_24289 = levelInfo.getDifficulty();
      this.field_24290 = this.field_24289;
      this.gameRules.setAllValues(levelInfo.getGameRules(), (MinecraftServer)null);
      if (levelInfo.isHardcore()) {
         this.currentMode = CreateWorldScreen.Mode.HARDCORE;
      } else if (levelInfo.getGameMode().isSurvivalLike()) {
         this.currentMode = CreateWorldScreen.Mode.SURVIVAL;
      } else if (levelInfo.getGameMode().isCreative()) {
         this.currentMode = CreateWorldScreen.Mode.CREATIVE;
      }

      this.dataPackTempDir = dataPackTempDir;
   }

   public static CreateWorldScreen create(@Nullable Screen parent) {
      DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
      return new CreateWorldScreen(parent, DataPackSettings.SAFE_MODE, new MoreOptionsDialog(impl, GeneratorOptions.getDefaultOptions(impl.get(Registry.DIMENSION_TYPE_KEY), impl.get(Registry.BIOME_KEY), impl.get(Registry.NOISE_SETTINGS_WORLDGEN)), Optional.of(GeneratorType.DEFAULT), OptionalLong.empty()));
   }

   private CreateWorldScreen(@Nullable Screen parent, DataPackSettings dataPackSettings, MoreOptionsDialog moreOptionsDialog) {
      super(new TranslatableText("selectWorld.create"));
      this.currentMode = CreateWorldScreen.Mode.SURVIVAL;
      this.field_24289 = Difficulty.NORMAL;
      this.field_24290 = Difficulty.NORMAL;
      this.gameRules = new GameRules();
      this.parent = parent;
      this.levelName = I18n.translate("selectWorld.newWorld");
      this.dataPackSettings = dataPackSettings;
      this.moreOptionsDialog = moreOptionsDialog;
   }

   public void tick() {
      this.levelNameField.tick();
      this.moreOptionsDialog.tick();
   }

   protected void init() {
      this.client.keyboard.setRepeatEvents(true);
      this.levelNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 60, 200, 20, new TranslatableText("selectWorld.enterName")) {
         protected MutableText getNarrationMessage() {
            return super.getNarrationMessage().append(". ").append((Text)(new TranslatableText("selectWorld.resultFolder"))).append(" ").append(CreateWorldScreen.this.saveDirectoryName);
         }
      };
      this.levelNameField.setText(this.levelName);
      this.levelNameField.setChangedListener((string) -> {
         this.levelName = string;
         this.createLevelButton.active = !this.levelNameField.getText().isEmpty();
         this.updateSaveFolderName();
      });
      this.children.add(this.levelNameField);
      int i = this.width / 2 - 155;
      int j = this.width / 2 + 5;
      this.gameModeSwitchButton = (ButtonWidget)this.addButton(new ButtonWidget(i, 100, 150, 20, LiteralText.EMPTY, (buttonWidget) -> {
         switch(this.currentMode) {
         case SURVIVAL:
            this.tweakDefaultsTo(CreateWorldScreen.Mode.HARDCORE);
            break;
         case HARDCORE:
            this.tweakDefaultsTo(CreateWorldScreen.Mode.CREATIVE);
            break;
         case CREATIVE:
            this.tweakDefaultsTo(CreateWorldScreen.Mode.SURVIVAL);
         }

         buttonWidget.queueNarration(250);
      }) {
         public Text getMessage() {
            return new TranslatableText("options.generic_value", new Object[]{CreateWorldScreen.GAME_MODE_TEXT, new TranslatableText("selectWorld.gameMode." + CreateWorldScreen.this.currentMode.translationSuffix)});
         }

         protected MutableText getNarrationMessage() {
            return super.getNarrationMessage().append(". ").append(CreateWorldScreen.this.firstGameModeDescriptionLine).append(" ").append(CreateWorldScreen.this.secondGameModeDescriptionLine);
         }
      });
      this.difficultyButton = (ButtonWidget)this.addButton(new ButtonWidget(j, 100, 150, 20, new TranslatableText("options.difficulty"), (button) -> {
         this.field_24289 = this.field_24289.cycle();
         this.field_24290 = this.field_24289;
         button.queueNarration(250);
      }) {
         public Text getMessage() {
            return (new TranslatableText("options.difficulty")).append(": ").append(CreateWorldScreen.this.field_24290.getTranslatableName());
         }
      });
      this.enableCheatsButton = (ButtonWidget)this.addButton(new ButtonWidget(i, 151, 150, 20, new TranslatableText("selectWorld.allowCommands"), (button) -> {
         this.tweakedCheats = true;
         this.cheatsEnabled = !this.cheatsEnabled;
         button.queueNarration(250);
      }) {
         public Text getMessage() {
            return ScreenTexts.composeToggleText(super.getMessage(), CreateWorldScreen.this.cheatsEnabled && !CreateWorldScreen.this.hardcore);
         }

         protected MutableText getNarrationMessage() {
            return super.getNarrationMessage().append(". ").append((Text)(new TranslatableText("selectWorld.allowCommands.info")));
         }
      });
      this.dataPacksButton = (ButtonWidget)this.addButton(new ButtonWidget(j, 151, 150, 20, new TranslatableText("selectWorld.dataPacks"), (button) -> {
         this.method_29694();
      }));
      this.gameRulesButton = (ButtonWidget)this.addButton(new ButtonWidget(i, 185, 150, 20, new TranslatableText("selectWorld.gameRules"), (button) -> {
         this.client.openScreen(new EditGameRulesScreen(this.gameRules.copy(), (optional) -> {
            this.client.openScreen(this);
            optional.ifPresent((gameRules) -> {
               this.gameRules = gameRules;
            });
         }));
      }));
      this.moreOptionsDialog.init(this, this.client, this.textRenderer);
      this.moreOptionsButton = (ButtonWidget)this.addButton(new ButtonWidget(j, 185, 150, 20, new TranslatableText("selectWorld.moreWorldOptions"), (buttonWidget) -> {
         this.toggleMoreOptions();
      }));
      this.createLevelButton = (ButtonWidget)this.addButton(new ButtonWidget(i, this.height - 28, 150, 20, new TranslatableText("selectWorld.create"), (buttonWidget) -> {
         this.createLevel();
      }));
      this.createLevelButton.active = !this.levelName.isEmpty();
      this.addButton(new ButtonWidget(j, this.height - 28, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.onCloseScreen();
      }));
      this.setMoreOptionsOpen();
      this.setInitialFocus(this.levelNameField);
      this.tweakDefaultsTo(this.currentMode);
      this.updateSaveFolderName();
   }

   private void updateSettingsLabels() {
      this.firstGameModeDescriptionLine = new TranslatableText("selectWorld.gameMode." + this.currentMode.translationSuffix + ".line1");
      this.secondGameModeDescriptionLine = new TranslatableText("selectWorld.gameMode." + this.currentMode.translationSuffix + ".line2");
   }

   private void updateSaveFolderName() {
      this.saveDirectoryName = this.levelNameField.getText().trim();
      if (this.saveDirectoryName.isEmpty()) {
         this.saveDirectoryName = "World";
      }

      try {
         this.saveDirectoryName = FileNameUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), this.saveDirectoryName, "");
      } catch (Exception var4) {
         this.saveDirectoryName = "World";

         try {
            this.saveDirectoryName = FileNameUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), this.saveDirectoryName, "");
         } catch (Exception var3) {
            throw new RuntimeException("Could not create save folder", var3);
         }
      }

   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   private void createLevel() {
      this.client.method_29970(new SaveLevelScreen(new TranslatableText("createWorld.preparing")));
      if (this.copyTempDirDataPacks()) {
         this.clearTempResources();
         GeneratorOptions generatorOptions = this.moreOptionsDialog.getGeneratorOptions(this.hardcore);
         LevelInfo levelInfo2;
         if (generatorOptions.isDebugWorld()) {
            GameRules gameRules = new GameRules();
            ((GameRules.BooleanRule)gameRules.get(GameRules.DO_DAYLIGHT_CYCLE)).set(false, (MinecraftServer)null);
            levelInfo2 = new LevelInfo(this.levelNameField.getText().trim(), GameMode.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, DataPackSettings.SAFE_MODE);
         } else {
            levelInfo2 = new LevelInfo(this.levelNameField.getText().trim(), this.currentMode.defaultGameMode, this.hardcore, this.field_24290, this.cheatsEnabled && !this.hardcore, this.gameRules, this.dataPackSettings);
         }

         this.client.createWorld(this.saveDirectoryName, levelInfo2, this.moreOptionsDialog.getRegistryManager(), generatorOptions);
      }
   }

   private void toggleMoreOptions() {
      this.setMoreOptionsOpen(!this.moreOptionsOpen);
   }

   private void tweakDefaultsTo(CreateWorldScreen.Mode mode) {
      if (!this.tweakedCheats) {
         this.cheatsEnabled = mode == CreateWorldScreen.Mode.CREATIVE;
      }

      if (mode == CreateWorldScreen.Mode.HARDCORE) {
         this.hardcore = true;
         this.enableCheatsButton.active = false;
         this.moreOptionsDialog.bonusItemsButton.active = false;
         this.field_24290 = Difficulty.HARD;
         this.difficultyButton.active = false;
      } else {
         this.hardcore = false;
         this.enableCheatsButton.active = true;
         this.moreOptionsDialog.bonusItemsButton.active = true;
         this.field_24290 = this.field_24289;
         this.difficultyButton.active = true;
      }

      this.currentMode = mode;
      this.updateSettingsLabels();
   }

   public void setMoreOptionsOpen() {
      this.setMoreOptionsOpen(this.moreOptionsOpen);
   }

   private void setMoreOptionsOpen(boolean moreOptionsOpen) {
      this.moreOptionsOpen = moreOptionsOpen;
      this.gameModeSwitchButton.visible = !this.moreOptionsOpen;
      this.difficultyButton.visible = !this.moreOptionsOpen;
      if (this.moreOptionsDialog.isDebugWorld()) {
         this.dataPacksButton.visible = false;
         this.gameModeSwitchButton.active = false;
         if (this.lastMode == null) {
            this.lastMode = this.currentMode;
         }

         this.tweakDefaultsTo(CreateWorldScreen.Mode.DEBUG);
         this.enableCheatsButton.visible = false;
      } else {
         this.gameModeSwitchButton.active = true;
         if (this.lastMode != null) {
            this.tweakDefaultsTo(this.lastMode);
         }

         this.enableCheatsButton.visible = !this.moreOptionsOpen;
         this.dataPacksButton.visible = !this.moreOptionsOpen;
      }

      this.moreOptionsDialog.setVisible(this.moreOptionsOpen);
      this.levelNameField.setVisible(!this.moreOptionsOpen);
      if (this.moreOptionsOpen) {
         this.moreOptionsButton.setMessage(ScreenTexts.DONE);
      } else {
         this.moreOptionsButton.setMessage(new TranslatableText("selectWorld.moreWorldOptions"));
      }

      this.gameRulesButton.visible = !this.moreOptionsOpen;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode != 257 && keyCode != 335) {
         return false;
      } else {
         this.createLevel();
         return true;
      }
   }

   public void onClose() {
      if (this.moreOptionsOpen) {
         this.setMoreOptionsOpen(false);
      } else {
         this.onCloseScreen();
      }

   }

   public void onCloseScreen() {
      this.client.openScreen(this.parent);
      this.clearTempResources();
   }

   private void clearTempResources() {
      if (this.packManager != null) {
         this.packManager.close();
      }

      this.clearDataPackTempDir();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, -1);
      if (this.moreOptionsOpen) {
         drawTextWithShadow(matrices, this.textRenderer, ENTER_SEED_TEXT, this.width / 2 - 100, 47, -6250336);
         drawTextWithShadow(matrices, this.textRenderer, SEED_INFO_TEXT, this.width / 2 - 100, 85, -6250336);
         this.moreOptionsDialog.render(matrices, mouseX, mouseY, delta);
      } else {
         drawTextWithShadow(matrices, this.textRenderer, ENTER_NAME_TEXT, this.width / 2 - 100, 47, -6250336);
         drawTextWithShadow(matrices, this.textRenderer, (new LiteralText("")).append(RESULT_FOLDER_TEXT).append(" ").append(this.saveDirectoryName), this.width / 2 - 100, 85, -6250336);
         this.levelNameField.render(matrices, mouseX, mouseY, delta);
         drawTextWithShadow(matrices, this.textRenderer, this.firstGameModeDescriptionLine, this.width / 2 - 150, 122, -6250336);
         drawTextWithShadow(matrices, this.textRenderer, this.secondGameModeDescriptionLine, this.width / 2 - 150, 134, -6250336);
         if (this.enableCheatsButton.visible) {
            drawTextWithShadow(matrices, this.textRenderer, ALLOW_COMMANDS_INFO_TEXT, this.width / 2 - 150, 172, -6250336);
         }
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   protected <T extends Element> T addChild(T child) {
      return super.addChild(child);
   }

   protected <T extends ClickableWidget> T addButton(T button) {
      return super.addButton(button);
   }

   @Nullable
   protected Path getDataPackTempDir() {
      if (this.dataPackTempDir == null) {
         try {
            this.dataPackTempDir = Files.createTempDirectory("mcworld-");
         } catch (IOException var2) {
            LOGGER.warn((String)"Failed to create temporary dir", (Throwable)var2);
            SystemToast.addPackCopyFailure(this.client, this.saveDirectoryName);
            this.onCloseScreen();
         }
      }

      return this.dataPackTempDir;
   }

   private void method_29694() {
      Pair<File, ResourcePackManager> pair = this.method_30296();
      if (pair != null) {
         this.client.openScreen(new PackScreen(this, (ResourcePackManager)pair.getSecond(), this::method_29682, (File)pair.getFirst(), new TranslatableText("dataPack.title")));
      }

   }

   private void method_29682(ResourcePackManager resourcePackManager) {
      List<String> list = ImmutableList.copyOf(resourcePackManager.getEnabledNames());
      List<String> list2 = (List)resourcePackManager.getNames().stream().filter((string) -> {
         return !list.contains(string);
      }).collect(ImmutableList.toImmutableList());
      DataPackSettings dataPackSettings = new DataPackSettings(list, list2);
      if (list.equals(this.dataPackSettings.getEnabled())) {
         this.dataPackSettings = dataPackSettings;
      } else {
         this.client.send(() -> {
            this.client.openScreen(new SaveLevelScreen(new TranslatableText("dataPack.validation.working")));
         });
         ServerResourceManager.reload(resourcePackManager.createResourcePacks(), CommandManager.RegistrationEnvironment.INTEGRATED, 2, Util.getMainWorkerExecutor(), this.client).handle((serverResourceManager, throwable) -> {
            if (throwable != null) {
               LOGGER.warn("Failed to validate datapack", throwable);
               this.client.send(() -> {
                  this.client.openScreen(new ConfirmScreen((bl) -> {
                     if (bl) {
                        this.method_29694();
                     } else {
                        this.dataPackSettings = DataPackSettings.SAFE_MODE;
                        this.client.openScreen(this);
                     }

                  }, new TranslatableText("dataPack.validation.failed"), LiteralText.EMPTY, new TranslatableText("dataPack.validation.back"), new TranslatableText("dataPack.validation.reset")));
               });
            } else {
               this.client.send(() -> {
                  this.dataPackSettings = dataPackSettings;
                  this.moreOptionsDialog.loadDatapacks(serverResourceManager);
                  serverResourceManager.close();
                  this.client.openScreen(this);
               });
            }

            return null;
         });
      }
   }

   private void clearDataPackTempDir() {
      if (this.dataPackTempDir != null) {
         try {
            Stream<Path> stream = Files.walk(this.dataPackTempDir);
            Throwable var2 = null;

            try {
               stream.sorted(Comparator.reverseOrder()).forEach((path) -> {
                  try {
                     Files.delete(path);
                  } catch (IOException var2) {
                     LOGGER.warn((String)"Failed to remove temporary file {}", (Object)path, (Object)var2);
                  }

               });
            } catch (Throwable var12) {
               var2 = var12;
               throw var12;
            } finally {
               if (stream != null) {
                  if (var2 != null) {
                     try {
                        stream.close();
                     } catch (Throwable var11) {
                        var2.addSuppressed(var11);
                     }
                  } else {
                     stream.close();
                  }
               }

            }
         } catch (IOException var14) {
            LOGGER.warn((String)"Failed to list temporary dir {}", (Object)this.dataPackTempDir);
         }

         this.dataPackTempDir = null;
      }

   }

   private static void copyDataPack(Path srcFolder, Path destFolder, Path dataPackFile) {
      try {
         Util.relativeCopy(srcFolder, destFolder, dataPackFile);
      } catch (IOException var4) {
         LOGGER.warn((String)"Failed to copy datapack file from {} to {}", (Object)dataPackFile, (Object)destFolder);
         throw new CreateWorldScreen.WorldCreationException(var4);
      }
   }

   private boolean copyTempDirDataPacks() {
      if (this.dataPackTempDir != null) {
         try {
            LevelStorage.Session session = this.client.getLevelStorage().createSession(this.saveDirectoryName);
            Throwable var2 = null;

            try {
               Stream<Path> stream = Files.walk(this.dataPackTempDir);
               Throwable var4 = null;

               try {
                  Path path = session.getDirectory(WorldSavePath.DATAPACKS);
                  Files.createDirectories(path);
                  stream.filter((pathx) -> {
                     return !pathx.equals(this.dataPackTempDir);
                  }).forEach((path2) -> {
                     copyDataPack(this.dataPackTempDir, path, path2);
                  });
               } catch (Throwable var29) {
                  var4 = var29;
                  throw var29;
               } finally {
                  if (stream != null) {
                     if (var4 != null) {
                        try {
                           stream.close();
                        } catch (Throwable var28) {
                           var4.addSuppressed(var28);
                        }
                     } else {
                        stream.close();
                     }
                  }

               }
            } catch (Throwable var31) {
               var2 = var31;
               throw var31;
            } finally {
               if (session != null) {
                  if (var2 != null) {
                     try {
                        session.close();
                     } catch (Throwable var27) {
                        var2.addSuppressed(var27);
                     }
                  } else {
                     session.close();
                  }
               }

            }
         } catch (CreateWorldScreen.WorldCreationException | IOException var33) {
            LOGGER.warn((String)"Failed to copy datapacks to world {}", (Object)this.saveDirectoryName, (Object)var33);
            SystemToast.addPackCopyFailure(this.client, this.saveDirectoryName);
            this.onCloseScreen();
            return false;
         }
      }

      return true;
   }

   @Nullable
   public static Path method_29685(Path path, MinecraftClient minecraftClient) {
      MutableObject mutableObject = new MutableObject();

      try {
         Stream<Path> stream = Files.walk(path);
         Throwable var4 = null;

         try {
            stream.filter((path2) -> {
               return !path2.equals(path);
            }).forEach((path2) -> {
               Path path3 = (Path)mutableObject.getValue();
               if (path3 == null) {
                  try {
                     path3 = Files.createTempDirectory("mcworld-");
                  } catch (IOException var5) {
                     LOGGER.warn("Failed to create temporary dir");
                     throw new CreateWorldScreen.WorldCreationException(var5);
                  }

                  mutableObject.setValue(path3);
               }

               copyDataPack(path, path3, path2);
            });
         } catch (Throwable var14) {
            var4 = var14;
            throw var14;
         } finally {
            if (stream != null) {
               if (var4 != null) {
                  try {
                     stream.close();
                  } catch (Throwable var13) {
                     var4.addSuppressed(var13);
                  }
               } else {
                  stream.close();
               }
            }

         }
      } catch (CreateWorldScreen.WorldCreationException | IOException var16) {
         LOGGER.warn((String)"Failed to copy datapacks from world {}", (Object)path, (Object)var16);
         SystemToast.addPackCopyFailure(minecraftClient, path.toString());
         return null;
      }

      return (Path)mutableObject.getValue();
   }

   @Nullable
   private Pair<File, ResourcePackManager> method_30296() {
      Path path = this.getDataPackTempDir();
      if (path != null) {
         File file = path.toFile();
         if (this.packManager == null) {
            this.packManager = new ResourcePackManager(new ResourcePackProvider[]{new VanillaDataPackProvider(), new FileResourcePackProvider(file, ResourcePackSource.field_25347)});
            this.packManager.scanPacks();
         }

         this.packManager.setEnabledProfiles(this.dataPackSettings.getEnabled());
         return Pair.of(file, this.packManager);
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   static class WorldCreationException extends RuntimeException {
      public WorldCreationException(Throwable throwable) {
         super(throwable);
      }
   }

   @Environment(EnvType.CLIENT)
   static enum Mode {
      SURVIVAL("survival", GameMode.SURVIVAL),
      HARDCORE("hardcore", GameMode.SURVIVAL),
      CREATIVE("creative", GameMode.CREATIVE),
      DEBUG("spectator", GameMode.SPECTATOR);

      private final String translationSuffix;
      private final GameMode defaultGameMode;

      private Mode(String translationSuffix, GameMode defaultGameMode) {
         this.translationSuffix = translationSuffix;
         this.defaultGameMode = defaultGameMode;
      }
   }
}
