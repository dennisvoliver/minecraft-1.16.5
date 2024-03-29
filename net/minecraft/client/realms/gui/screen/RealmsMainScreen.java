package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.KeyCombo;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.realms.Realms;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.PingResult;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerPlayerList;
import net.minecraft.client.realms.dto.RealmsServerPlayerLists;
import net.minecraft.client.realms.dto.RegionPingResult;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsDataFetcher;
import net.minecraft.client.realms.task.RealmsGetServerDetailsTask;
import net.minecraft.client.realms.util.RealmsPersistence;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Identifier ON_ICON = new Identifier("realms", "textures/gui/realms/on_icon.png");
   private static final Identifier OFF_ICON = new Identifier("realms", "textures/gui/realms/off_icon.png");
   private static final Identifier EXPIRED_ICON = new Identifier("realms", "textures/gui/realms/expired_icon.png");
   private static final Identifier EXPIRES_SOON_ICON = new Identifier("realms", "textures/gui/realms/expires_soon_icon.png");
   private static final Identifier LEAVE_ICON = new Identifier("realms", "textures/gui/realms/leave_icon.png");
   private static final Identifier INVITATION_ICON = new Identifier("realms", "textures/gui/realms/invitation_icons.png");
   private static final Identifier INVITE_ICON = new Identifier("realms", "textures/gui/realms/invite_icon.png");
   private static final Identifier WORLD_ICON = new Identifier("realms", "textures/gui/realms/world_icon.png");
   private static final Identifier REALMS = new Identifier("realms", "textures/gui/title/realms.png");
   private static final Identifier CONFIGURE_ICON = new Identifier("realms", "textures/gui/realms/configure_icon.png");
   private static final Identifier QUESTIONMARK = new Identifier("realms", "textures/gui/realms/questionmark.png");
   private static final Identifier NEWS_ICON = new Identifier("realms", "textures/gui/realms/news_icon.png");
   private static final Identifier POPUP = new Identifier("realms", "textures/gui/realms/popup.png");
   private static final Identifier DARKEN = new Identifier("realms", "textures/gui/realms/darken.png");
   private static final Identifier CROSS_ICON = new Identifier("realms", "textures/gui/realms/cross_icon.png");
   private static final Identifier TRIAL_ICON = new Identifier("realms", "textures/gui/realms/trial_icon.png");
   private static final Identifier WIDGETS = new Identifier("minecraft", "textures/gui/widgets.png");
   private static final Text field_26447 = new TranslatableText("mco.invites.nopending");
   private static final Text field_26448 = new TranslatableText("mco.invites.pending");
   private static final List<Text> field_26449 = ImmutableList.of(new TranslatableText("mco.trial.message.line1"), new TranslatableText("mco.trial.message.line2"));
   private static final Text field_26450 = new TranslatableText("mco.selectServer.uninitialized");
   private static final Text field_26451 = new TranslatableText("mco.selectServer.expiredList");
   private static final Text field_26452 = new TranslatableText("mco.selectServer.expiredRenew");
   private static final Text field_26453 = new TranslatableText("mco.selectServer.expiredTrial");
   private static final Text field_26454 = new TranslatableText("mco.selectServer.expiredSubscribe");
   private static final Text field_26455 = (new TranslatableText("mco.selectServer.minigame")).append(" ");
   private static final Text field_26456 = new TranslatableText("mco.selectServer.popup");
   private static final Text field_26457 = new TranslatableText("mco.selectServer.expired");
   private static final Text field_26458 = new TranslatableText("mco.selectServer.expires.soon");
   private static final Text field_26459 = new TranslatableText("mco.selectServer.expires.day");
   private static final Text field_26460 = new TranslatableText("mco.selectServer.open");
   private static final Text field_26461 = new TranslatableText("mco.selectServer.closed");
   private static final Text field_26462 = new TranslatableText("mco.selectServer.leave");
   private static final Text field_26463 = new TranslatableText("mco.selectServer.configure");
   private static final Text field_26464 = new TranslatableText("mco.selectServer.info");
   private static final Text field_26465 = new TranslatableText("mco.news");
   private static List<Identifier> IMAGES = ImmutableList.of();
   private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
   private static boolean overrideConfigure;
   private static int lastScrollYPosition = -1;
   private static volatile boolean hasParentalConsent;
   private static volatile boolean checkedParentalConsent;
   private static volatile boolean checkedClientCompatibility;
   private static Screen realmsGenericErrorScreen;
   private static boolean regionsPinged;
   private final RateLimiter rateLimiter;
   private boolean dontSetConnectedToRealms;
   private final Screen lastScreen;
   private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
   private long selectedServerId = -1L;
   private ButtonWidget playButton;
   private ButtonWidget backButton;
   private ButtonWidget renewButton;
   private ButtonWidget configureButton;
   private ButtonWidget leaveButton;
   private List<Text> toolTip;
   private List<RealmsServer> realmsServers = Lists.newArrayList();
   private volatile int numberOfPendingInvites;
   private int animTick;
   private boolean hasFetchedServers;
   private boolean popupOpenedByUser;
   private boolean justClosedPopup;
   private volatile boolean trialsAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   private volatile boolean hasUnreadNews;
   private volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   private boolean hasSwitchedCarouselImage;
   private List<KeyCombo> keyCombos;
   private int clicks;
   private ReentrantLock connectLock = new ReentrantLock();
   private MultilineText field_26466;
   private RealmsMainScreen.HoverState hoverState;
   private ButtonWidget showPopupButton;
   private ButtonWidget pendingInvitesButton;
   private ButtonWidget newsButton;
   private ButtonWidget createTrialButton;
   private ButtonWidget buyARealmButton;
   private ButtonWidget closeButton;

   public RealmsMainScreen(Screen screen) {
      this.field_26466 = MultilineText.EMPTY;
      this.lastScreen = screen;
      this.rateLimiter = RateLimiter.create(0.01666666753590107D);
   }

   private boolean shouldShowMessageInList() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.trialsAvailable && !this.createdTrial) {
            return true;
         } else {
            Iterator var1 = this.realmsServers.iterator();

            RealmsServer realmsServer;
            do {
               if (!var1.hasNext()) {
                  return true;
               }

               realmsServer = (RealmsServer)var1.next();
            } while(!realmsServer.ownerUUID.equals(this.client.getSession().getUuid()));

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean shouldShowPopup() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.popupOpenedByUser) {
            return true;
         } else {
            return this.trialsAvailable && !this.createdTrial && this.realmsServers.isEmpty() ? true : this.realmsServers.isEmpty();
         }
      } else {
         return false;
      }
   }

   public void init() {
      this.keyCombos = Lists.newArrayList((Object[])(new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
         overrideConfigure = !overrideConfigure;
      }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.switchToProd();
         } else {
            this.switchToStage();
         }

      }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.switchToProd();
         } else {
            this.switchToLocal();
         }

      })));
      if (realmsGenericErrorScreen != null) {
         this.client.openScreen(realmsGenericErrorScreen);
      } else {
         this.connectLock = new ReentrantLock();
         if (checkedClientCompatibility && !hasParentalConsent()) {
            this.checkParentalConsent();
         }

         this.checkClientCompatibility();
         this.checkUnreadNews();
         if (!this.dontSetConnectedToRealms) {
            this.client.setConnectedToRealms(false);
         }

         this.client.keyboard.setRepeatEvents(true);
         if (hasParentalConsent()) {
            realmsDataFetcher.forceUpdate();
         }

         this.showingPopup = false;
         if (hasParentalConsent() && this.hasFetchedServers) {
            this.addButtons();
         }

         this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
         if (lastScrollYPosition != -1) {
            this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
         }

         this.addChild(this.realmSelectionList);
         this.focusOn(this.realmSelectionList);
         this.field_26466 = MultilineText.create(this.textRenderer, field_26456, 100);
      }
   }

   private static boolean hasParentalConsent() {
      return checkedParentalConsent && hasParentalConsent;
   }

   public void addButtons() {
      this.leaveButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 202, this.height - 32, 90, 20, new TranslatableText("mco.selectServer.leave"), (buttonWidget) -> {
         this.leaveClicked(this.findServer(this.selectedServerId));
      }));
      this.configureButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 190, this.height - 32, 90, 20, new TranslatableText("mco.selectServer.configure"), (buttonWidget) -> {
         this.configureClicked(this.findServer(this.selectedServerId));
      }));
      this.playButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 93, this.height - 32, 90, 20, new TranslatableText("mco.selectServer.play"), (buttonWidget) -> {
         RealmsServer realmsServer = this.findServer(this.selectedServerId);
         if (realmsServer != null) {
            this.play(realmsServer, this);
         }
      }));
      this.backButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 32, 90, 20, ScreenTexts.BACK, (buttonWidget) -> {
         if (!this.justClosedPopup) {
            this.client.openScreen(this.lastScreen);
         }

      }));
      this.renewButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 100, this.height - 32, 90, 20, new TranslatableText("mco.selectServer.expiredRenew"), (buttonWidget) -> {
         this.onRenew();
      }));
      this.pendingInvitesButton = (ButtonWidget)this.addButton(new RealmsMainScreen.PendingInvitesButton());
      this.newsButton = (ButtonWidget)this.addButton(new RealmsMainScreen.NewsButton());
      this.showPopupButton = (ButtonWidget)this.addButton(new RealmsMainScreen.ShowPopupButton());
      this.closeButton = (ButtonWidget)this.addButton(new RealmsMainScreen.CloseButton());
      this.createTrialButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20, new TranslatableText("mco.selectServer.trial"), (buttonWidget) -> {
         if (this.trialsAvailable && !this.createdTrial) {
            Util.getOperatingSystem().open("https://aka.ms/startjavarealmstrial");
            this.client.openScreen(this.lastScreen);
         }
      }));
      this.buyARealmButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20, new TranslatableText("mco.selectServer.buy"), (buttonWidget) -> {
         Util.getOperatingSystem().open("https://aka.ms/BuyJavaRealms");
      }));
      RealmsServer realmsServer = this.findServer(this.selectedServerId);
      this.updateButtonStates(realmsServer);
   }

   private void updateButtonStates(@Nullable RealmsServer server) {
      this.playButton.active = this.shouldPlayButtonBeActive(server) && !this.shouldShowPopup();
      this.renewButton.visible = this.shouldRenewButtonBeActive(server);
      this.configureButton.visible = this.shouldConfigureButtonBeVisible(server);
      this.leaveButton.visible = this.shouldLeaveButtonBeVisible(server);
      boolean bl = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
      this.createTrialButton.visible = bl;
      this.createTrialButton.active = bl;
      this.buyARealmButton.visible = this.shouldShowPopup();
      this.closeButton.visible = this.shouldShowPopup() && this.popupOpenedByUser;
      this.renewButton.active = !this.shouldShowPopup();
      this.configureButton.active = !this.shouldShowPopup();
      this.leaveButton.active = !this.shouldShowPopup();
      this.newsButton.active = true;
      this.pendingInvitesButton.active = true;
      this.backButton.active = true;
      this.showPopupButton.active = !this.shouldShowPopup();
   }

   private boolean shouldShowPopupButton() {
      return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
   }

   private boolean shouldPlayButtonBeActive(@Nullable RealmsServer server) {
      return server != null && !server.expired && server.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(@Nullable RealmsServer server) {
      return server != null && server.expired && this.isSelfOwnedServer(server);
   }

   private boolean shouldConfigureButtonBeVisible(@Nullable RealmsServer server) {
      return server != null && this.isSelfOwnedServer(server);
   }

   private boolean shouldLeaveButtonBeVisible(@Nullable RealmsServer server) {
      return server != null && !this.isSelfOwnedServer(server);
   }

   public void tick() {
      super.tick();
      this.justClosedPopup = false;
      ++this.animTick;
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

      if (hasParentalConsent()) {
         realmsDataFetcher.init();
         Iterator var4;
         RealmsServer realmsServer3;
         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
            List<RealmsServer> list = realmsDataFetcher.getServers();
            this.realmSelectionList.clear();
            boolean bl = !this.hasFetchedServers;
            if (bl) {
               this.hasFetchedServers = true;
            }

            if (list != null) {
               boolean bl2 = false;
               var4 = list.iterator();

               while(var4.hasNext()) {
                  realmsServer3 = (RealmsServer)var4.next();
                  if (this.method_25001(realmsServer3)) {
                     bl2 = true;
                  }
               }

               this.realmsServers = list;
               if (this.shouldShowMessageInList()) {
                  this.realmSelectionList.method_30161(new RealmsMainScreen.RealmSelectionListTrialEntry());
               }

               var4 = this.realmsServers.iterator();

               while(var4.hasNext()) {
                  realmsServer3 = (RealmsServer)var4.next();
                  this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListEntry(realmsServer3));
               }

               if (!regionsPinged && bl2) {
                  regionsPinged = true;
                  this.pingRegions();
               }
            }

            if (bl) {
               this.addButtons();
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
            if (this.numberOfPendingInvites > 0 && this.rateLimiter.tryAcquire(1)) {
               Realms.narrateNow(I18n.translate("mco.configure.world.invite.narration", this.numberOfPendingInvites));
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
            boolean bl3 = realmsDataFetcher.isTrialAvailable();
            if (bl3 != this.trialsAvailable && this.shouldShowPopup()) {
               this.trialsAvailable = bl3;
               this.showingPopup = false;
            } else {
               this.trialsAvailable = bl3;
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
            RealmsServerPlayerLists realmsServerPlayerLists = realmsDataFetcher.getLivestats();
            Iterator var8 = realmsServerPlayerLists.servers.iterator();

            label87:
            while(true) {
               while(true) {
                  if (!var8.hasNext()) {
                     break label87;
                  }

                  RealmsServerPlayerList realmsServerPlayerList = (RealmsServerPlayerList)var8.next();
                  var4 = this.realmsServers.iterator();

                  while(var4.hasNext()) {
                     realmsServer3 = (RealmsServer)var4.next();
                     if (realmsServer3.id == realmsServerPlayerList.serverId) {
                        realmsServer3.updateServerPing(realmsServerPlayerList);
                        break;
                     }
                  }
               }
            }
         }

         if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
            this.hasUnreadNews = realmsDataFetcher.hasUnreadNews();
            this.newsLink = realmsDataFetcher.newsLink();
         }

         realmsDataFetcher.markClean();
         if (this.shouldShowPopup()) {
            ++this.carouselTick;
         }

         if (this.showPopupButton != null) {
            this.showPopupButton.visible = this.shouldShowPopupButton();
         }

      }
   }

   private void pingRegions() {
      (new Thread(() -> {
         List<RegionPingResult> list = Ping.pingAllRegions();
         RealmsClient realmsClient = RealmsClient.createRealmsClient();
         PingResult pingResult = new PingResult();
         pingResult.pingResults = list;
         pingResult.worldIds = this.getOwnedNonExpiredWorldIds();

         try {
            realmsClient.sendPingResults(pingResult);
         } catch (Throwable var5) {
            LOGGER.warn("Could not send ping result to Realms: ", var5);
         }

      })).start();
   }

   private List<Long> getOwnedNonExpiredWorldIds() {
      List<Long> list = Lists.newArrayList();
      Iterator var2 = this.realmsServers.iterator();

      while(var2.hasNext()) {
         RealmsServer realmsServer = (RealmsServer)var2.next();
         if (this.method_25001(realmsServer)) {
            list.add(realmsServer.id);
         }
      }

      return list;
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
      this.stopRealmsFetcher();
   }

   private void onRenew() {
      RealmsServer realmsServer = this.findServer(this.selectedServerId);
      if (realmsServer != null) {
         String string = "https://aka.ms/ExtendJavaRealms?subscriptionId=" + realmsServer.remoteSubscriptionId + "&profileId=" + this.client.getSession().getUuid() + "&ref=" + (realmsServer.expiredTrial ? "expiredTrial" : "expiredRealm");
         this.client.keyboard.setClipboard(string);
         Util.getOperatingSystem().open(string);
      }
   }

   private void checkClientCompatibility() {
      if (!checkedClientCompatibility) {
         checkedClientCompatibility = true;
         (new Thread("MCO Compatability Checker #1") {
            public void run() {
               RealmsClient realmsClient = RealmsClient.createRealmsClient();

               try {
                  RealmsClient.CompatibleVersionResponse compatibleVersionResponse = realmsClient.clientCompatible();
                  if (compatibleVersionResponse == RealmsClient.CompatibleVersionResponse.OUTDATED) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.openScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  if (compatibleVersionResponse == RealmsClient.CompatibleVersionResponse.OTHER) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.openScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  RealmsMainScreen.this.checkParentalConsent();
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.checkedClientCompatibility = false;
                  RealmsMainScreen.LOGGER.error((String)"Couldn't connect to realms", (Throwable)var3);
                  if (var3.httpResultCode == 401) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(new TranslatableText("mco.error.invalid.session.title"), new TranslatableText("mco.error.invalid.session.message"), RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.openScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                  } else {
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.client.openScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
                     });
                  }
               }

            }
         }).start();
      }

   }

   private void checkUnreadNews() {
   }

   private void checkParentalConsent() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient realmsClient = RealmsClient.createRealmsClient();

            try {
               Boolean boolean_ = realmsClient.mcoEnabled();
               if (boolean_) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.hasParentalConsent = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.hasParentalConsent = false;
                  RealmsMainScreen.this.client.execute(() -> {
                     RealmsMainScreen.this.client.openScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
                  });
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException var3) {
               RealmsMainScreen.LOGGER.error((String)"Couldn't connect to realms", (Throwable)var3);
               RealmsMainScreen.this.client.execute(() -> {
                  RealmsMainScreen.this.client.openScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen));
               });
            }

         }
      }).start();
   }

   private void switchToStage() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient realmsClient = RealmsClient.createRealmsClient();

               try {
                  Boolean boolean_ = realmsClient.stageAvailable();
                  if (boolean_) {
                     RealmsClient.switchToStage();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.realmsDataFetcher.forceUpdate();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
               }

            }
         }).start();
      }

   }

   private void switchToLocal() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient realmsClient = RealmsClient.createRealmsClient();

               try {
                  Boolean boolean_ = realmsClient.stageAvailable();
                  if (boolean_) {
                     RealmsClient.switchToLocal();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.realmsDataFetcher.forceUpdate();
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
               }

            }
         }).start();
      }

   }

   private void switchToProd() {
      RealmsClient.switchToProd();
      realmsDataFetcher.forceUpdate();
   }

   private void stopRealmsFetcher() {
      realmsDataFetcher.stop();
   }

   private void configureClicked(RealmsServer realmsServer) {
      if (this.client.getSession().getUuid().equals(realmsServer.ownerUUID) || overrideConfigure) {
         this.saveListScrollPosition();
         this.client.openScreen(new RealmsConfigureWorldScreen(this, realmsServer.id));
      }

   }

   private void leaveClicked(@Nullable RealmsServer selectedServer) {
      if (selectedServer != null && !this.client.getSession().getUuid().equals(selectedServer.ownerUUID)) {
         this.saveListScrollPosition();
         Text text = new TranslatableText("mco.configure.world.leave.question.line1");
         Text text2 = new TranslatableText("mco.configure.world.leave.question.line2");
         this.client.openScreen(new RealmsLongConfirmationScreen(this::method_24991, RealmsLongConfirmationScreen.Type.Info, text, text2, true));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
   }

   @Nullable
   private RealmsServer findServer(long id) {
      Iterator var3 = this.realmsServers.iterator();

      RealmsServer realmsServer;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         realmsServer = (RealmsServer)var3.next();
      } while(realmsServer.id != id);

      return realmsServer;
   }

   private void method_24991(boolean bl) {
      if (bl) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  RealmsServer realmsServer = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                  if (realmsServer != null) {
                     RealmsClient realmsClient = RealmsClient.createRealmsClient();
                     realmsClient.uninviteMyselfFrom(realmsServer.id);
                     RealmsMainScreen.this.client.execute(() -> {
                        RealmsMainScreen.this.method_31174(realmsServer);
                     });
                  }
               } catch (RealmsServiceException var3) {
                  RealmsMainScreen.LOGGER.error("Couldn't configure world");
                  RealmsMainScreen.this.client.execute(() -> {
                     RealmsMainScreen.this.client.openScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this));
                  });
               }

            }
         }).start();
      }

      this.client.openScreen(this);
   }

   private void method_31174(RealmsServer realmsServer) {
      realmsDataFetcher.removeItem(realmsServer);
      this.realmsServers.remove(realmsServer);
      this.realmSelectionList.children().removeIf((entry) -> {
         return entry instanceof RealmsMainScreen.RealmSelectionListEntry && ((RealmsMainScreen.RealmSelectionListEntry)entry).mServerData.id == this.selectedServerId;
      });
      this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      this.updateButtonStates((RealmsServer)null);
      this.selectedServerId = -1L;
      this.playButton.active = false;
   }

   public void removeSelection() {
      this.selectedServerId = -1L;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.keyCombos.forEach(KeyCombo::reset);
         this.onClosePopup();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void onClosePopup() {
      if (this.shouldShowPopup() && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
      } else {
         this.client.openScreen(this.lastScreen);
      }

   }

   public boolean charTyped(char chr, int modifiers) {
      this.keyCombos.forEach((keyCombo) -> {
         keyCombo.keyPressed(chr);
      });
      return true;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.hoverState = RealmsMainScreen.HoverState.NONE;
      this.toolTip = null;
      this.renderBackground(matrices);
      this.realmSelectionList.render(matrices, mouseX, mouseY, delta);
      this.drawRealmsLogo(matrices, this.width / 2 - 50, 7);
      if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
         this.renderStage(matrices);
      }

      if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
         this.renderLocal(matrices);
      }

      if (this.shouldShowPopup()) {
         this.drawPopup(matrices, mouseX, mouseY);
      } else {
         if (this.showingPopup) {
            this.updateButtonStates((RealmsServer)null);
            if (!this.children.contains(this.realmSelectionList)) {
               this.children.add(this.realmSelectionList);
            }

            RealmsServer realmsServer = this.findServer(this.selectedServerId);
            this.playButton.active = this.shouldPlayButtonBeActive(realmsServer);
         }

         this.showingPopup = false;
      }

      super.render(matrices, mouseX, mouseY, delta);
      if (this.toolTip != null) {
         this.renderMousehoverTooltip(matrices, this.toolTip, mouseX, mouseY);
      }

      if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
         this.client.getTextureManager().bindTexture(TRIAL_ICON);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         int i = true;
         int j = true;
         int k = 0;
         if ((Util.getMeasuringTimeMs() / 800L & 1L) == 1L) {
            k = 8;
         }

         DrawableHelper.drawTexture(matrices, this.createTrialButton.x + this.createTrialButton.getWidth() - 8 - 4, this.createTrialButton.y + this.createTrialButton.getHeight() / 2 - 4, 0.0F, (float)k, 8, 8, 8, 16);
      }

   }

   private void drawRealmsLogo(MatrixStack matrices, int x, int y) {
      this.client.getTextureManager().bindTexture(REALMS);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.pushMatrix();
      RenderSystem.scalef(0.5F, 0.5F, 0.5F);
      DrawableHelper.drawTexture(matrices, x * 2, y * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
      RenderSystem.popMatrix();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isOutsidePopup(mouseX, mouseY) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private boolean isOutsidePopup(double xm, double ym) {
      int i = this.popupX0();
      int j = this.popupY0();
      return xm < (double)(i - 5) || xm > (double)(i + 315) || ym < (double)(j - 5) || ym > (double)(j + 171);
   }

   private void drawPopup(MatrixStack matrices, int mouseX, int mouseY) {
      int i = this.popupX0();
      int j = this.popupY0();
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         this.updateButtonStates((RealmsServer)null);
         if (this.children.contains(this.realmSelectionList)) {
            Element element = this.realmSelectionList;
            if (!this.children.remove(element)) {
               LOGGER.error("Unable to remove widget: " + element);
            }
         }

         Realms.narrateNow(field_26456.getString());
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
      RenderSystem.enableBlend();
      this.client.getTextureManager().bindTexture(DARKEN);
      int k = false;
      int l = true;
      DrawableHelper.drawTexture(matrices, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
      RenderSystem.disableBlend();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(POPUP);
      DrawableHelper.drawTexture(matrices, i, j, 0.0F, 0.0F, 310, 166, 310, 166);
      if (!IMAGES.isEmpty()) {
         this.client.getTextureManager().bindTexture((Identifier)IMAGES.get(this.carouselIndex));
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         DrawableHelper.drawTexture(matrices, i + 7, j + 7, 0.0F, 0.0F, 195, 152, 195, 152);
         if (this.carouselTick % 95 < 5) {
            if (!this.hasSwitchedCarouselImage) {
               this.carouselIndex = (this.carouselIndex + 1) % IMAGES.size();
               this.hasSwitchedCarouselImage = true;
            }
         } else {
            this.hasSwitchedCarouselImage = false;
         }
      }

      this.field_26466.draw(matrices, this.width / 2 + 52, j + 7, 10, 5000268);
   }

   private int popupX0() {
      return (this.width - 310) / 2;
   }

   private int popupY0() {
      return this.height / 2 - 80;
   }

   private void drawInvitationPendingIcon(MatrixStack matrixStack, int i, int j, int k, int l, boolean bl, boolean bl2) {
      int m = this.numberOfPendingInvites;
      boolean bl3 = this.inPendingInvitationArea((double)i, (double)j);
      boolean bl4 = bl2 && bl;
      if (bl4) {
         float f = 0.25F + (1.0F + MathHelper.sin((float)this.animTick * 0.5F)) * 0.25F;
         int n = -16777216 | (int)(f * 64.0F) << 16 | (int)(f * 64.0F) << 8 | (int)(f * 64.0F) << 0;
         this.fillGradient(matrixStack, k - 2, l - 2, k + 18, l + 18, n, n);
         n = -16777216 | (int)(f * 255.0F) << 16 | (int)(f * 255.0F) << 8 | (int)(f * 255.0F) << 0;
         this.fillGradient(matrixStack, k - 2, l - 2, k + 18, l - 1, n, n);
         this.fillGradient(matrixStack, k - 2, l - 2, k - 1, l + 18, n, n);
         this.fillGradient(matrixStack, k + 17, l - 2, k + 18, l + 18, n, n);
         this.fillGradient(matrixStack, k - 2, l + 17, k + 18, l + 18, n, n);
      }

      this.client.getTextureManager().bindTexture(INVITE_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      boolean bl5 = bl2 && bl;
      float g = bl5 ? 16.0F : 0.0F;
      DrawableHelper.drawTexture(matrixStack, k, l - 6, g, 0.0F, 15, 25, 31, 25);
      boolean bl6 = bl2 && m != 0;
      int q;
      if (bl6) {
         q = (Math.min(m, 6) - 1) * 8;
         int p = (int)(Math.max(0.0F, Math.max(MathHelper.sin((float)(10 + this.animTick) * 0.57F), MathHelper.cos((float)this.animTick * 0.35F))) * -6.0F);
         this.client.getTextureManager().bindTexture(INVITATION_ICON);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         float h = bl3 ? 8.0F : 0.0F;
         DrawableHelper.drawTexture(matrixStack, k + 4, l + 4 + p, (float)q, h, 8, 8, 48, 16);
      }

      q = i + 12;
      boolean bl7 = bl2 && bl3;
      if (bl7) {
         Text text = m == 0 ? field_26447 : field_26448;
         int s = this.textRenderer.getWidth((StringVisitable)text);
         this.fillGradient(matrixStack, q - 3, j - 3, q + s + 3, j + 8 + 3, -1073741824, -1073741824);
         this.textRenderer.drawWithShadow(matrixStack, (Text)text, (float)q, (float)j, -1);
      }

   }

   private boolean inPendingInvitationArea(double xm, double ym) {
      int i = this.width / 2 + 50;
      int j = this.width / 2 + 66;
      int k = 11;
      int l = 23;
      if (this.numberOfPendingInvites != 0) {
         i -= 3;
         j += 3;
         k -= 5;
         l += 5;
      }

      return (double)i <= xm && xm <= (double)j && (double)k <= ym && ym <= (double)l;
   }

   public void play(RealmsServer realmsServer, Screen screen) {
      if (realmsServer != null) {
         try {
            if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.connectLock.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException var4) {
            return;
         }

         this.dontSetConnectedToRealms = true;
         this.client.openScreen(new RealmsLongRunningMcoTaskScreen(screen, new RealmsGetServerDetailsTask(this, screen, realmsServer, this.connectLock)));
      }

   }

   private boolean isSelfOwnedServer(RealmsServer serverData) {
      return serverData.ownerUUID != null && serverData.ownerUUID.equals(this.client.getSession().getUuid());
   }

   private boolean method_25001(RealmsServer realmsServer) {
      return this.isSelfOwnedServer(realmsServer) && !realmsServer.expired;
   }

   private void drawExpired(MatrixStack matrixStack, int i, int j, int k, int l) {
      this.client.getTextureManager().bindTexture(EXPIRED_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      DrawableHelper.drawTexture(matrixStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
      if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
         this.method_27452(field_26457);
      }

   }

   private void method_24987(MatrixStack matrixStack, int i, int j, int k, int l, int m) {
      this.client.getTextureManager().bindTexture(EXPIRES_SOON_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.animTick % 20 < 10) {
         DrawableHelper.drawTexture(matrixStack, i, j, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         DrawableHelper.drawTexture(matrixStack, i, j, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
         if (m <= 0) {
            this.method_27452(field_26458);
         } else if (m == 1) {
            this.method_27452(field_26459);
         } else {
            this.method_27452(new TranslatableText("mco.selectServer.expires.days", new Object[]{m}));
         }
      }

   }

   private void drawOpen(MatrixStack matrixStack, int i, int j, int k, int l) {
      this.client.getTextureManager().bindTexture(ON_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      DrawableHelper.drawTexture(matrixStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
      if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
         this.method_27452(field_26460);
      }

   }

   private void drawClose(MatrixStack matrixStack, int i, int j, int k, int l) {
      this.client.getTextureManager().bindTexture(OFF_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      DrawableHelper.drawTexture(matrixStack, i, j, 0.0F, 0.0F, 10, 28, 10, 28);
      if (k >= i && k <= i + 9 && l >= j && l <= j + 27 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
         this.method_27452(field_26461);
      }

   }

   private void drawLeave(MatrixStack matrixStack, int i, int j, int k, int l) {
      boolean bl = false;
      if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
         bl = true;
      }

      this.client.getTextureManager().bindTexture(LEAVE_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float f = bl ? 28.0F : 0.0F;
      DrawableHelper.drawTexture(matrixStack, i, j, f, 0.0F, 28, 28, 56, 28);
      if (bl) {
         this.method_27452(field_26462);
         this.hoverState = RealmsMainScreen.HoverState.LEAVE;
      }

   }

   private void drawConfigure(MatrixStack matrixStack, int i, int j, int k, int l) {
      boolean bl = false;
      if (k >= i && k <= i + 28 && l >= j && l <= j + 28 && l < this.height - 40 && l > 32 && !this.shouldShowPopup()) {
         bl = true;
      }

      this.client.getTextureManager().bindTexture(CONFIGURE_ICON);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float f = bl ? 28.0F : 0.0F;
      DrawableHelper.drawTexture(matrixStack, i, j, f, 0.0F, 28, 28, 56, 28);
      if (bl) {
         this.method_27452(field_26463);
         this.hoverState = RealmsMainScreen.HoverState.CONFIGURE;
      }

   }

   protected void renderMousehoverTooltip(MatrixStack matrixStack, List<Text> list, int i, int j) {
      if (!list.isEmpty()) {
         int k = 0;
         int l = 0;
         Iterator var7 = list.iterator();

         while(var7.hasNext()) {
            Text text = (Text)var7.next();
            int m = this.textRenderer.getWidth((StringVisitable)text);
            if (m > l) {
               l = m;
            }
         }

         int n = i - l - 5;
         int o = j;
         if (n < 0) {
            n = i + 12;
         }

         for(Iterator var14 = list.iterator(); var14.hasNext(); k += 10) {
            Text text2 = (Text)var14.next();
            int p = o - (k == 0 ? 3 : 0) + k;
            this.fillGradient(matrixStack, n - 3, p, n + l + 3, o + 8 + 3 + k, -1073741824, -1073741824);
            this.textRenderer.drawWithShadow(matrixStack, text2, (float)n, (float)(o + k), 16777215);
         }

      }
   }

   private void renderMoreInfo(MatrixStack matrixStack, int i, int j, int k, int l, boolean bl) {
      boolean bl2 = false;
      if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
         bl2 = true;
      }

      this.client.getTextureManager().bindTexture(QUESTIONMARK);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      float f = bl ? 20.0F : 0.0F;
      DrawableHelper.drawTexture(matrixStack, k, l, f, 0.0F, 20, 20, 40, 20);
      if (bl2) {
         this.method_27452(field_26464);
      }

   }

   private void renderNews(MatrixStack matrixStack, int i, int j, boolean bl, int k, int l, boolean bl2, boolean bl3) {
      boolean bl4 = false;
      if (i >= k && i <= k + 20 && j >= l && j <= l + 20) {
         bl4 = true;
      }

      this.client.getTextureManager().bindTexture(NEWS_ICON);
      if (bl3) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
      }

      boolean bl5 = bl3 && bl2;
      float f = bl5 ? 20.0F : 0.0F;
      DrawableHelper.drawTexture(matrixStack, k, l, f, 0.0F, 20, 20, 40, 20);
      if (bl4 && bl3) {
         this.method_27452(field_26465);
      }

      if (bl && bl3) {
         int m = bl4 ? 0 : (int)(Math.max(0.0F, Math.max(MathHelper.sin((float)(10 + this.animTick) * 0.57F), MathHelper.cos((float)this.animTick * 0.35F))) * -6.0F);
         this.client.getTextureManager().bindTexture(INVITATION_ICON);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         DrawableHelper.drawTexture(matrixStack, k + 10, l + 2 + m, 40.0F, 0.0F, 8, 8, 48, 16);
      }

   }

   private void renderLocal(MatrixStack matrixStack) {
      String string = "LOCAL!";
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.pushMatrix();
      RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
      RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      RenderSystem.scalef(1.5F, 1.5F, 1.5F);
      this.textRenderer.draw(matrixStack, "LOCAL!", 0.0F, 0.0F, 8388479);
      RenderSystem.popMatrix();
   }

   private void renderStage(MatrixStack matrixStack) {
      String string = "STAGE!";
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.pushMatrix();
      RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
      RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      RenderSystem.scalef(1.5F, 1.5F, 1.5F);
      this.textRenderer.draw(matrixStack, (String)"STAGE!", 0.0F, 0.0F, -256);
      RenderSystem.popMatrix();
   }

   public RealmsMainScreen newScreen() {
      RealmsMainScreen realmsMainScreen = new RealmsMainScreen(this.lastScreen);
      realmsMainScreen.init(this.client, this.width, this.height);
      return realmsMainScreen;
   }

   public static void method_23765(ResourceManager manager) {
      Collection<Identifier> collection = manager.findResources("textures/gui/images", (string) -> {
         return string.endsWith(".png");
      });
      IMAGES = (List)collection.stream().filter((identifier) -> {
         return identifier.getNamespace().equals("realms");
      }).collect(ImmutableList.toImmutableList());
   }

   private void method_27452(Text... texts) {
      this.toolTip = Arrays.asList(texts);
   }

   private void method_24985(ButtonWidget buttonWidget) {
      this.client.openScreen(new RealmsPendingInvitesScreen(this.lastScreen));
   }

   @Environment(EnvType.CLIENT)
   class CloseButton extends ButtonWidget {
      public CloseButton() {
         super(RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, 12, 12, new TranslatableText("mco.selectServer.close"), (buttonWidget) -> {
            RealmsMainScreen.this.onClosePopup();
         });
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RealmsMainScreen.this.client.getTextureManager().bindTexture(RealmsMainScreen.CROSS_ICON);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         float f = this.isHovered() ? 12.0F : 0.0F;
         drawTexture(matrices, this.x, this.y, 0.0F, f, 12, 12, 12, 24);
         if (this.isMouseOver((double)mouseX, (double)mouseY)) {
            RealmsMainScreen.this.method_27452(this.getMessage());
         }

      }
   }

   @Environment(EnvType.CLIENT)
   class ShowPopupButton extends ButtonWidget {
      public ShowPopupButton() {
         super(RealmsMainScreen.this.width - 37, 6, 20, 20, new TranslatableText("mco.selectServer.info"), (buttonWidget) -> {
            RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser;
         });
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RealmsMainScreen.this.renderMoreInfo(matrices, mouseX, mouseY, this.x, this.y, this.isHovered());
      }
   }

   @Environment(EnvType.CLIENT)
   class NewsButton extends ButtonWidget {
      public NewsButton() {
         super(RealmsMainScreen.this.width - 62, 6, 20, 20, LiteralText.EMPTY, (buttonWidget) -> {
            if (RealmsMainScreen.this.newsLink != null) {
               Util.getOperatingSystem().open(RealmsMainScreen.this.newsLink);
               if (RealmsMainScreen.this.hasUnreadNews) {
                  RealmsPersistence.RealmsPersistenceData realmsPersistenceData = RealmsPersistence.readFile();
                  realmsPersistenceData.hasUnreadNews = false;
                  RealmsMainScreen.this.hasUnreadNews = false;
                  RealmsPersistence.writeFile(realmsPersistenceData);
               }

            }
         });
         this.setMessage(new TranslatableText("mco.news"));
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RealmsMainScreen.this.renderNews(matrices, mouseX, mouseY, RealmsMainScreen.this.hasUnreadNews, this.x, this.y, this.isHovered(), this.active);
      }
   }

   @Environment(EnvType.CLIENT)
   class PendingInvitesButton extends ButtonWidget implements TickableElement {
      public PendingInvitesButton() {
         super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, LiteralText.EMPTY, (buttonWidget) -> {
            RealmsMainScreen.this.method_24985(buttonWidget);
         });
      }

      public void tick() {
         this.setMessage(new TranslatableText(RealmsMainScreen.this.numberOfPendingInvites == 0 ? "mco.invites.nopending" : "mco.invites.pending"));
      }

      public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
         RealmsMainScreen.this.drawInvitationPendingIcon(matrices, mouseX, mouseY, this.x, this.y, this.isHovered(), this.active);
      }
   }

   @Environment(EnvType.CLIENT)
   class RealmSelectionListEntry extends RealmsMainScreen.Entry {
      private final RealmsServer mServerData;

      public RealmSelectionListEntry(RealmsServer serverData) {
         super(null);
         this.mServerData = serverData;
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.method_20945(this.mServerData, matrices, x, y, mouseX, mouseY);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.mServerData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.selectedServerId = -1L;
            RealmsMainScreen.this.client.openScreen(new RealmsCreateRealmScreen(this.mServerData, RealmsMainScreen.this));
         } else {
            RealmsMainScreen.this.selectedServerId = this.mServerData.id;
         }

         return true;
      }

      private void method_20945(RealmsServer realmsServer, MatrixStack matrixStack, int i, int j, int k, int l) {
         this.renderMcoServerItem(realmsServer, matrixStack, i + 36, j, k, l);
      }

      private void renderMcoServerItem(RealmsServer serverData, MatrixStack matrixStack, int i, int j, int k, int l) {
         if (serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.client.getTextureManager().bindTexture(RealmsMainScreen.WORLD_ICON);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableAlphaTest();
            DrawableHelper.drawTexture(matrixStack, i + 10, j + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            float f = 0.5F + (1.0F + MathHelper.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
            int m = -16777216 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
            DrawableHelper.drawCenteredText(matrixStack, RealmsMainScreen.this.textRenderer, RealmsMainScreen.field_26450, i + 10 + 40 + 75, j + 12, m);
         } else {
            int n = true;
            int o = true;
            if (serverData.expired) {
               RealmsMainScreen.this.drawExpired(matrixStack, i + 225 - 14, j + 2, k, l);
            } else if (serverData.state == RealmsServer.State.CLOSED) {
               RealmsMainScreen.this.drawClose(matrixStack, i + 225 - 14, j + 2, k, l);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.daysLeft < 7) {
               RealmsMainScreen.this.method_24987(matrixStack, i + 225 - 14, j + 2, k, l, serverData.daysLeft);
            } else if (serverData.state == RealmsServer.State.OPEN) {
               RealmsMainScreen.this.drawOpen(matrixStack, i + 225 - 14, j + 2, k, l);
            }

            if (!RealmsMainScreen.this.isSelfOwnedServer(serverData) && !RealmsMainScreen.overrideConfigure) {
               RealmsMainScreen.this.drawLeave(matrixStack, i + 225, j + 2, k, l);
            } else {
               RealmsMainScreen.this.drawConfigure(matrixStack, i + 225, j + 2, k, l);
            }

            if (!"0".equals(serverData.serverPing.nrOfPlayers)) {
               String string = Formatting.GRAY + "" + serverData.serverPing.nrOfPlayers;
               RealmsMainScreen.this.textRenderer.draw(matrixStack, string, (float)(i + 207 - RealmsMainScreen.this.textRenderer.getWidth(string)), (float)(j + 3), 8421504);
               if (k >= i + 207 - RealmsMainScreen.this.textRenderer.getWidth(string) && k <= i + 207 && l >= j + 1 && l <= j + 10 && l < RealmsMainScreen.this.height - 40 && l > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  RealmsMainScreen.this.method_27452(new LiteralText(serverData.serverPing.playerList));
               }
            }

            if (RealmsMainScreen.this.isSelfOwnedServer(serverData) && serverData.expired) {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.enableBlend();
               RealmsMainScreen.this.client.getTextureManager().bindTexture(RealmsMainScreen.WIDGETS);
               RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
               Text text3;
               Text text4;
               if (serverData.expiredTrial) {
                  text3 = RealmsMainScreen.field_26453;
                  text4 = RealmsMainScreen.field_26454;
               } else {
                  text3 = RealmsMainScreen.field_26451;
                  text4 = RealmsMainScreen.field_26452;
               }

               int p = RealmsMainScreen.this.textRenderer.getWidth((StringVisitable)text4) + 17;
               int q = true;
               int r = i + RealmsMainScreen.this.textRenderer.getWidth((StringVisitable)text3) + 8;
               int s = j + 13;
               boolean bl = false;
               if (k >= r && k < r + p && l > s && l <= s + 16 & l < RealmsMainScreen.this.height - 40 && l > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  bl = true;
                  RealmsMainScreen.this.hoverState = RealmsMainScreen.HoverState.EXPIRED;
               }

               int t = bl ? 2 : 1;
               DrawableHelper.drawTexture(matrixStack, r, s, 0.0F, (float)(46 + t * 20), p / 2, 8, 256, 256);
               DrawableHelper.drawTexture(matrixStack, r + p / 2, s, (float)(200 - p / 2), (float)(46 + t * 20), p / 2, 8, 256, 256);
               DrawableHelper.drawTexture(matrixStack, r, s + 8, 0.0F, (float)(46 + t * 20 + 12), p / 2, 8, 256, 256);
               DrawableHelper.drawTexture(matrixStack, r + p / 2, s + 8, (float)(200 - p / 2), (float)(46 + t * 20 + 12), p / 2, 8, 256, 256);
               RenderSystem.disableBlend();
               int u = j + 11 + 5;
               int v = bl ? 16777120 : 16777215;
               RealmsMainScreen.this.textRenderer.draw(matrixStack, text3, (float)(i + 2), (float)(u + 1), 15553363);
               DrawableHelper.drawCenteredText(matrixStack, RealmsMainScreen.this.textRenderer, text4, r + p / 2, u + 1, v);
            } else {
               if (serverData.worldType == RealmsServer.WorldType.MINIGAME) {
                  int w = 13413468;
                  int x = RealmsMainScreen.this.textRenderer.getWidth((StringVisitable)RealmsMainScreen.field_26455);
                  RealmsMainScreen.this.textRenderer.draw(matrixStack, RealmsMainScreen.field_26455, (float)(i + 2), (float)(j + 12), 13413468);
                  RealmsMainScreen.this.textRenderer.draw(matrixStack, serverData.getMinigameName(), (float)(i + 2 + x), (float)(j + 12), 7105644);
               } else {
                  RealmsMainScreen.this.textRenderer.draw(matrixStack, serverData.getDescription(), (float)(i + 2), (float)(j + 12), 7105644);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(serverData)) {
                  RealmsMainScreen.this.textRenderer.draw(matrixStack, serverData.owner, (float)(i + 2), (float)(j + 12 + 11), 5000268);
               }
            }

            RealmsMainScreen.this.textRenderer.draw(matrixStack, serverData.getName(), (float)(i + 2), (float)(j + 1), 16777215);
            RealmsTextureManager.withBoundFace(serverData.ownerUUID, () -> {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               DrawableHelper.drawTexture(matrixStack, i - 36, j, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
               DrawableHelper.drawTexture(matrixStack, i - 36, j, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);
            });
         }
      }
   }

   @Environment(EnvType.CLIENT)
   class RealmSelectionListTrialEntry extends RealmsMainScreen.Entry {
      private RealmSelectionListTrialEntry() {
         super(null);
      }

      public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.renderTrialItem(matrices, index, x, y, mouseX, mouseY);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         RealmsMainScreen.this.popupOpenedByUser = true;
         return true;
      }

      private void renderTrialItem(MatrixStack matrixStack, int index, int x, int y, int mouseX, int mouseY) {
         int i = y + 8;
         int j = 0;
         boolean bl = false;
         if (x <= mouseX && mouseX <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && y <= mouseY && mouseY <= y + 32) {
            bl = true;
         }

         int k = 8388479;
         if (bl && !RealmsMainScreen.this.shouldShowPopup()) {
            k = 6077788;
         }

         for(Iterator var11 = RealmsMainScreen.field_26449.iterator(); var11.hasNext(); j += 10) {
            Text text = (Text)var11.next();
            DrawableHelper.drawCenteredText(matrixStack, RealmsMainScreen.this.textRenderer, text, RealmsMainScreen.this.width / 2, i + j, k);
         }

      }
   }

   @Environment(EnvType.CLIENT)
   abstract class Entry extends AlwaysSelectedEntryListWidget.Entry<RealmsMainScreen.Entry> {
      private Entry() {
      }
   }

   @Environment(EnvType.CLIENT)
   class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
      private boolean field_25723;

      public RealmSelectionList() {
         super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 40, 36);
      }

      public void clear() {
         super.clear();
         this.field_25723 = false;
      }

      public int method_30161(RealmsMainScreen.Entry entry) {
         this.field_25723 = true;
         return this.addEntry(entry);
      }

      public boolean isFocused() {
         return RealmsMainScreen.this.getFocused() == this;
      }

      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
            return super.keyPressed(keyCode, scanCode, modifiers);
         } else {
            AlwaysSelectedEntryListWidget.Entry entry = (AlwaysSelectedEntryListWidget.Entry)this.getSelected();
            return entry == null ? super.keyPressed(keyCode, scanCode, modifiers) : entry.mouseClicked(0.0D, 0.0D, 0);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (button == 0 && mouseX < (double)this.getScrollbarPositionX() && mouseY >= (double)this.top && mouseY <= (double)this.bottom) {
            int i = RealmsMainScreen.this.realmSelectionList.getRowLeft();
            int j = this.getScrollbarPositionX();
            int k = (int)Math.floor(mouseY - (double)this.top) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int l = k / this.itemHeight;
            if (mouseX >= (double)i && mouseX <= (double)j && l >= 0 && k >= 0 && l < this.getEntryCount()) {
               this.itemClicked(k, l, mouseX, mouseY, this.width);
               RealmsMainScreen.this.clicks = RealmsMainScreen.this.clicks + 7;
               this.setSelected(l);
            }

            return true;
         } else {
            return super.mouseClicked(mouseX, mouseY, button);
         }
      }

      public void setSelected(int index) {
         this.setSelectedItem(index);
         if (index != -1) {
            RealmsServer realmsServer3;
            if (this.field_25723) {
               if (index == 0) {
                  realmsServer3 = null;
               } else {
                  if (index - 1 >= RealmsMainScreen.this.realmsServers.size()) {
                     RealmsMainScreen.this.selectedServerId = -1L;
                     return;
                  }

                  realmsServer3 = (RealmsServer)RealmsMainScreen.this.realmsServers.get(index - 1);
               }
            } else {
               if (index >= RealmsMainScreen.this.realmsServers.size()) {
                  RealmsMainScreen.this.selectedServerId = -1L;
                  return;
               }

               realmsServer3 = (RealmsServer)RealmsMainScreen.this.realmsServers.get(index);
            }

            RealmsMainScreen.this.updateButtonStates(realmsServer3);
            if (realmsServer3 == null) {
               RealmsMainScreen.this.selectedServerId = -1L;
            } else if (realmsServer3.state == RealmsServer.State.UNINITIALIZED) {
               RealmsMainScreen.this.selectedServerId = -1L;
            } else {
               RealmsMainScreen.this.selectedServerId = realmsServer3.id;
               if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active) {
                  RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
               }

            }
         }
      }

      public void setSelected(@Nullable RealmsMainScreen.Entry entry) {
         super.setSelected(entry);
         int i = this.children().indexOf(entry);
         if (this.field_25723 && i == 0) {
            Realms.narrateNow(I18n.translate("mco.trial.message.line1"), I18n.translate("mco.trial.message.line2"));
         } else if (!this.field_25723 || i > 0) {
            RealmsServer realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(i - (this.field_25723 ? 1 : 0));
            RealmsMainScreen.this.selectedServerId = realmsServer.id;
            RealmsMainScreen.this.updateButtonStates(realmsServer);
            if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
               Realms.narrateNow(I18n.translate("mco.selectServer.uninitialized") + I18n.translate("mco.gui.button"));
            } else {
               Realms.narrateNow(I18n.translate("narrator.select", realmsServer.name));
            }
         }

      }

      public void itemClicked(int cursorY, int selectionIndex, double mouseX, double mouseY, int listWidth) {
         if (this.field_25723) {
            if (selectionIndex == 0) {
               RealmsMainScreen.this.popupOpenedByUser = true;
               return;
            }

            --selectionIndex;
         }

         if (selectionIndex < RealmsMainScreen.this.realmsServers.size()) {
            RealmsServer realmsServer = (RealmsServer)RealmsMainScreen.this.realmsServers.get(selectionIndex);
            if (realmsServer != null) {
               if (realmsServer.state == RealmsServer.State.UNINITIALIZED) {
                  RealmsMainScreen.this.selectedServerId = -1L;
                  MinecraftClient.getInstance().openScreen(new RealmsCreateRealmScreen(realmsServer, RealmsMainScreen.this));
               } else {
                  RealmsMainScreen.this.selectedServerId = realmsServer.id;
               }

               if (RealmsMainScreen.this.hoverState == RealmsMainScreen.HoverState.CONFIGURE) {
                  RealmsMainScreen.this.selectedServerId = realmsServer.id;
                  RealmsMainScreen.this.configureClicked(realmsServer);
               } else if (RealmsMainScreen.this.hoverState == RealmsMainScreen.HoverState.LEAVE) {
                  RealmsMainScreen.this.selectedServerId = realmsServer.id;
                  RealmsMainScreen.this.leaveClicked(realmsServer);
               } else if (RealmsMainScreen.this.hoverState == RealmsMainScreen.HoverState.EXPIRED) {
                  RealmsMainScreen.this.onRenew();
               }

            }
         }
      }

      public int getMaxPosition() {
         return this.getEntryCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }
   }

   @Environment(EnvType.CLIENT)
   static enum HoverState {
      NONE,
      EXPIRED,
      LEAVE,
      CONFIGURE;
   }
}
