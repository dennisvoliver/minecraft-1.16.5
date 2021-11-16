package net.minecraft.client.gui.screen.option;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class LanguageOptionsScreen extends GameOptionsScreen {
   private static final Text LANGUAGE_WARNING_TEXT;
   private LanguageOptionsScreen.LanguageSelectionListWidget languageSelectionList;
   private final LanguageManager languageManager;
   private OptionButtonWidget forceUnicodeButton;
   private ButtonWidget doneButton;

   public LanguageOptionsScreen(Screen parent, GameOptions options, LanguageManager languageManager) {
      super(parent, options, new TranslatableText("options.language"));
      this.languageManager = languageManager;
   }

   protected void init() {
      this.languageSelectionList = new LanguageOptionsScreen.LanguageSelectionListWidget(this.client);
      this.children.add(this.languageSelectionList);
      this.forceUnicodeButton = (OptionButtonWidget)this.addButton(new OptionButtonWidget(this.width / 2 - 155, this.height - 38, 150, 20, Option.FORCE_UNICODE_FONT, Option.FORCE_UNICODE_FONT.getDisplayString(this.gameOptions), (button) -> {
         Option.FORCE_UNICODE_FONT.toggle(this.gameOptions);
         this.gameOptions.write();
         button.setMessage(Option.FORCE_UNICODE_FONT.getDisplayString(this.gameOptions));
         this.client.onResolutionChanged();
      }));
      this.doneButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height - 38, 150, 20, ScreenTexts.DONE, (button) -> {
         LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry languageEntry = (LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry)this.languageSelectionList.getSelected();
         if (languageEntry != null && !languageEntry.languageDefinition.getCode().equals(this.languageManager.getLanguage().getCode())) {
            this.languageManager.setLanguage(languageEntry.languageDefinition);
            this.gameOptions.language = languageEntry.languageDefinition.getCode();
            this.client.reloadResources();
            this.doneButton.setMessage(ScreenTexts.DONE);
            this.forceUnicodeButton.setMessage(Option.FORCE_UNICODE_FONT.getDisplayString(this.gameOptions));
            this.gameOptions.write();
         }

         this.client.openScreen(this.parent);
      }));
      super.init();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.languageSelectionList.render(matrices, mouseX, mouseY, delta);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 16, 16777215);
      drawCenteredText(matrices, this.textRenderer, LANGUAGE_WARNING_TEXT, this.width / 2, this.height - 56, 8421504);
      super.render(matrices, mouseX, mouseY, delta);
   }

   static {
      LANGUAGE_WARNING_TEXT = (new LiteralText("(")).append(new TranslatableText("options.languageWarning")).append(")").formatted(Formatting.GRAY);
   }

   @Environment(EnvType.CLIENT)
   class LanguageSelectionListWidget extends AlwaysSelectedEntryListWidget<LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry> {
      public LanguageSelectionListWidget(MinecraftClient client) {
         super(client, LanguageOptionsScreen.this.width, LanguageOptionsScreen.this.height, 32, LanguageOptionsScreen.this.height - 65 + 4, 18);
         Iterator var3 = LanguageOptionsScreen.this.languageManager.getAllLanguages().iterator();

         while(var3.hasNext()) {
            LanguageDefinition languageDefinition = (LanguageDefinition)var3.next();
            LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry languageEntry = new LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry(languageDefinition);
            this.addEntry(languageEntry);
            if (LanguageOptionsScreen.this.languageManager.getLanguage().getCode().equals(languageDefinition.getCode())) {
               this.setSelected(languageEntry);
            }
         }

         if (this.getSelected() != null) {
            this.centerScrollOn(this.getSelected());
         }

      }

      protected int getScrollbarPositionX() {
         return super.getScrollbarPositionX() + 20;
      }

      public int getRowWidth() {
         return super.getRowWidth() + 50;
      }

      public void setSelected(@Nullable LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry languageEntry) {
         super.setSelected(languageEntry);
         if (languageEntry != null) {
            NarratorManager.INSTANCE.narrate((new TranslatableText("narrator.select", new Object[]{languageEntry.languageDefinition})).getString());
         }

      }

      protected void renderBackground(MatrixStack matrices) {
         LanguageOptionsScreen.this.renderBackground(matrices);
      }

      protected boolean isFocused() {
         return LanguageOptionsScreen.this.getFocused() == this;
      }

      @Environment(EnvType.CLIENT)
      public class LanguageEntry extends AlwaysSelectedEntryListWidget.Entry<LanguageOptionsScreen.LanguageSelectionListWidget.LanguageEntry> {
         private final LanguageDefinition languageDefinition;

         public LanguageEntry(LanguageDefinition languageDefinition) {
            this.languageDefinition = languageDefinition;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String string = this.languageDefinition.toString();
            LanguageOptionsScreen.this.textRenderer.drawWithShadow(matrices, string, (float)(LanguageSelectionListWidget.this.width / 2 - LanguageOptionsScreen.this.textRenderer.getWidth(string) / 2), (float)(y + 1), 16777215, true);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
               this.onPressed();
               return true;
            } else {
               return false;
            }
         }

         private void onPressed() {
            LanguageSelectionListWidget.this.setSelected(this);
         }
      }
   }
}
