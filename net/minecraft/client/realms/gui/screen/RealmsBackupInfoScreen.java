package net.minecraft.client.realms.gui.screen;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
   private final Screen parent;
   private final Backup backup;
   private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

   public RealmsBackupInfoScreen(Screen parent, Backup backup) {
      this.parent = parent;
      this.backup = backup;
   }

   public void tick() {
   }

   public void init() {
      this.client.keyboard.setRepeatEvents(true);
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, ScreenTexts.BACK, (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
      this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.client);
      this.addChild(this.backupInfoList);
      this.focusOn(this.backupInfoList);
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.client.openScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, "Changes from last backup", this.width / 2, 10, 16777215);
      this.backupInfoList.render(matrices, mouseX, mouseY, delta);
      super.render(matrices, mouseX, mouseY, delta);
   }

   private Text checkForSpecificMetadata(String key, String value) {
      String string = key.toLowerCase(Locale.ROOT);
      if (string.contains("game") && string.contains("mode")) {
         return this.gameModeMetadata(value);
      } else {
         return (Text)(string.contains("game") && string.contains("difficulty") ? this.gameDifficultyMetadata(value) : new LiteralText(value));
      }
   }

   private Text gameDifficultyMetadata(String value) {
      try {
         return RealmsSlotOptionsScreen.DIFFICULTIES[Integer.parseInt(value)];
      } catch (Exception var3) {
         return new LiteralText("UNKNOWN");
      }
   }

   private Text gameModeMetadata(String value) {
      try {
         return RealmsSlotOptionsScreen.GAME_MODES[Integer.parseInt(value)];
      } catch (Exception var3) {
         return new LiteralText("UNKNOWN");
      }
   }

   @Environment(EnvType.CLIENT)
   class BackupInfoList extends AlwaysSelectedEntryListWidget<RealmsBackupInfoScreen.BackupInfoListEntry> {
      public BackupInfoList(MinecraftClient minecraftClient) {
         super(minecraftClient, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
         this.setRenderSelection(false);
         if (RealmsBackupInfoScreen.this.backup.changeList != null) {
            RealmsBackupInfoScreen.this.backup.changeList.forEach((key, value) -> {
               this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(key, value));
            });
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class BackupInfoListEntry extends AlwaysSelectedEntryListWidget.Entry<RealmsBackupInfoScreen.BackupInfoListEntry> {
      private final String key;
      private final String value;

      public BackupInfoListEntry(String key, String value) {
         this.key = key;
         this.value = value;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         TextRenderer textRenderer = RealmsBackupInfoScreen.this.client.textRenderer;
         DrawableHelper.drawStringWithShadow(matrices, textRenderer, this.key, x, y, 10526880);
         DrawableHelper.drawTextWithShadow(matrices, textRenderer, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), x, y + 12, 16777215);
      }
   }
}
