package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsBridgeScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class GameMenuScreen extends Screen {
   private final boolean showMenu;

   public GameMenuScreen(boolean showMenu) {
      super(showMenu ? new TranslatableText("menu.game") : new TranslatableText("menu.paused"));
      this.showMenu = showMenu;
   }

   protected void init() {
      if (this.showMenu) {
         this.initWidgets();
      }

   }

   private void initWidgets() {
      int i = true;
      int j = true;
      this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslatableText("menu.returnToGame"), (buttonWidgetx) -> {
         this.client.openScreen((Screen)null);
         this.client.mouse.lockCursor();
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslatableText("gui.advancements"), (buttonWidgetx) -> {
         this.client.openScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler()));
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslatableText("gui.stats"), (buttonWidgetx) -> {
         this.client.openScreen(new StatsScreen(this, this.client.player.getStatHandler()));
      }));
      String string = SharedConstants.getGameVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
      this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslatableText("menu.sendFeedback"), (buttonWidgetx) -> {
         this.client.openScreen(new ConfirmChatLinkScreen((bl) -> {
            if (bl) {
               Util.getOperatingSystem().open(string);
            }

            this.client.openScreen(this);
         }, string, true));
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TranslatableText("menu.reportBugs"), (buttonWidgetx) -> {
         this.client.openScreen(new ConfirmChatLinkScreen((bl) -> {
            if (bl) {
               Util.getOperatingSystem().open("https://aka.ms/snapshotbugs?ref=game");
            }

            this.client.openScreen(this);
         }, "https://aka.ms/snapshotbugs?ref=game", true));
      }));
      this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslatableText("menu.options"), (buttonWidgetx) -> {
         this.client.openScreen(new OptionsScreen(this, this.client.options));
      }));
      ButtonWidget buttonWidget = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslatableText("menu.shareToLan"), (buttonWidgetx) -> {
         this.client.openScreen(new OpenToLanScreen(this));
      }));
      buttonWidget.active = this.client.isIntegratedServerRunning() && !this.client.getServer().isRemote();
      ButtonWidget buttonWidget2 = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, new TranslatableText("menu.returnToMenu"), (buttonWidgetx) -> {
         boolean bl = this.client.isInSingleplayer();
         boolean bl2 = this.client.isConnectedToRealms();
         buttonWidgetx.active = false;
         this.client.world.disconnect();
         if (bl) {
            this.client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
         } else {
            this.client.disconnect();
         }

         if (bl) {
            this.client.openScreen(new TitleScreen());
         } else if (bl2) {
            RealmsBridgeScreen realmsBridgeScreen = new RealmsBridgeScreen();
            realmsBridgeScreen.switchToRealms(new TitleScreen());
         } else {
            this.client.openScreen(new MultiplayerScreen(new TitleScreen()));
         }

      }));
      if (!this.client.isInSingleplayer()) {
         buttonWidget2.setMessage(new TranslatableText("menu.disconnect"));
      }

   }

   public void tick() {
      super.tick();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.showMenu) {
         this.renderBackground(matrices);
         drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
      } else {
         drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }
}
