package net.minecraft.client.realms.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsWorldSlotButton extends ButtonWidget implements TickableElement {
   public static final Identifier SLOT_FRAME = new Identifier("realms", "textures/gui/realms/slot_frame.png");
   public static final Identifier EMPTY_FRAME = new Identifier("realms", "textures/gui/realms/empty_frame.png");
   public static final Identifier PANORAMA_0 = new Identifier("minecraft", "textures/gui/title/background/panorama_0.png");
   public static final Identifier PANORAMA_2 = new Identifier("minecraft", "textures/gui/title/background/panorama_2.png");
   public static final Identifier PANORAMA_3 = new Identifier("minecraft", "textures/gui/title/background/panorama_3.png");
   private static final Text field_26468 = new TranslatableText("mco.configure.world.slot.tooltip.active");
   private static final Text field_26469 = new TranslatableText("mco.configure.world.slot.tooltip.minigame");
   private static final Text field_26470 = new TranslatableText("mco.configure.world.slot.tooltip");
   private final Supplier<RealmsServer> serverDataProvider;
   private final Consumer<Text> toolTipSetter;
   private final int slotIndex;
   private int animTick;
   @Nullable
   private RealmsWorldSlotButton.State state;

   public RealmsWorldSlotButton(int x, int y, int width, int height, Supplier<RealmsServer> serverDataProvider, Consumer<Text> toolTipSetter, int id, ButtonWidget.PressAction action) {
      super(x, y, width, height, LiteralText.EMPTY, action);
      this.serverDataProvider = serverDataProvider;
      this.slotIndex = id;
      this.toolTipSetter = toolTipSetter;
   }

   @Nullable
   public RealmsWorldSlotButton.State getState() {
      return this.state;
   }

   public void tick() {
      ++this.animTick;
      RealmsServer realmsServer = (RealmsServer)this.serverDataProvider.get();
      if (realmsServer != null) {
         RealmsWorldOptions realmsWorldOptions = (RealmsWorldOptions)realmsServer.slots.get(this.slotIndex);
         boolean bl = this.slotIndex == 4;
         boolean bl4;
         String string3;
         long m;
         String string4;
         boolean bl5;
         if (bl) {
            bl4 = realmsServer.worldType == RealmsServer.WorldType.MINIGAME;
            string3 = "Minigame";
            m = (long)realmsServer.minigameId;
            string4 = realmsServer.minigameImage;
            bl5 = realmsServer.minigameId == -1;
         } else {
            bl4 = realmsServer.activeSlot == this.slotIndex && realmsServer.worldType != RealmsServer.WorldType.MINIGAME;
            string3 = realmsWorldOptions.getSlotName(this.slotIndex);
            m = realmsWorldOptions.templateId;
            string4 = realmsWorldOptions.templateImage;
            bl5 = realmsWorldOptions.empty;
         }

         RealmsWorldSlotButton.Action action = method_27455(realmsServer, bl4, bl);
         Pair<Text, Text> pair = this.method_27454(realmsServer, string3, bl5, bl, action);
         this.state = new RealmsWorldSlotButton.State(bl4, string3, m, string4, bl5, bl, action, (Text)pair.getFirst());
         this.setMessage((Text)pair.getSecond());
      }
   }

   private static RealmsWorldSlotButton.Action method_27455(RealmsServer realmsServer, boolean bl, boolean bl2) {
      if (bl) {
         if (!realmsServer.expired && realmsServer.state != RealmsServer.State.UNINITIALIZED) {
            return RealmsWorldSlotButton.Action.JOIN;
         }
      } else {
         if (!bl2) {
            return RealmsWorldSlotButton.Action.SWITCH_SLOT;
         }

         if (!realmsServer.expired) {
            return RealmsWorldSlotButton.Action.SWITCH_SLOT;
         }
      }

      return RealmsWorldSlotButton.Action.NOTHING;
   }

   private Pair<Text, Text> method_27454(RealmsServer realmsServer, String string, boolean bl, boolean bl2, RealmsWorldSlotButton.Action action) {
      if (action == RealmsWorldSlotButton.Action.NOTHING) {
         return Pair.of((Object)null, new LiteralText(string));
      } else {
         Object text3;
         if (bl2) {
            if (bl) {
               text3 = LiteralText.EMPTY;
            } else {
               text3 = (new LiteralText(" ")).append(string).append(" ").append(realmsServer.minigameName);
            }
         } else {
            text3 = (new LiteralText(" ")).append(string);
         }

         Text text5;
         if (action == RealmsWorldSlotButton.Action.JOIN) {
            text5 = field_26468;
         } else {
            text5 = bl2 ? field_26469 : field_26470;
         }

         Text text6 = text5.shallowCopy().append((Text)text3);
         return Pair.of(text5, text6);
      }
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.state != null) {
         this.drawSlotFrame(matrices, this.x, this.y, mouseX, mouseY, this.state.isCurrentlyActiveSlot, this.state.slotName, this.slotIndex, this.state.imageId, this.state.image, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
      }
   }

   private void drawSlotFrame(MatrixStack matrices, int x, int y, int mouseX, int mouseY, boolean active, String slotName, int slotIndex, long imageId, @Nullable String image, boolean empty, boolean minigame, RealmsWorldSlotButton.Action action, @Nullable Text actionPrompt) {
      boolean bl = this.isHovered();
      if (this.isMouseOver((double)mouseX, (double)mouseY) && actionPrompt != null) {
         this.toolTipSetter.accept(actionPrompt);
      }

      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      TextureManager textureManager = minecraftClient.getTextureManager();
      if (minigame) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else if (empty) {
         textureManager.bindTexture(EMPTY_FRAME);
      } else if (image != null && imageId != -1L) {
         RealmsTextureManager.bindWorldTemplate(String.valueOf(imageId), image);
      } else if (slotIndex == 1) {
         textureManager.bindTexture(PANORAMA_0);
      } else if (slotIndex == 2) {
         textureManager.bindTexture(PANORAMA_2);
      } else if (slotIndex == 3) {
         textureManager.bindTexture(PANORAMA_3);
      }

      if (active) {
         float f = 0.85F + 0.15F * MathHelper.cos((float)this.animTick * 0.2F);
         RenderSystem.color4f(f, f, f, 1.0F);
      } else {
         RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      drawTexture(matrices, x + 3, y + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      textureManager.bindTexture(SLOT_FRAME);
      boolean bl2 = bl && action != RealmsWorldSlotButton.Action.NOTHING;
      if (bl2) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      } else if (active) {
         RenderSystem.color4f(0.8F, 0.8F, 0.8F, 1.0F);
      } else {
         RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
      }

      drawTexture(matrices, x, y, 0.0F, 0.0F, 80, 80, 80, 80);
      drawCenteredText(matrices, minecraftClient.textRenderer, slotName, x + 40, y + 66, 16777215);
   }

   @Environment(EnvType.CLIENT)
   public static class State {
      private final boolean isCurrentlyActiveSlot;
      private final String slotName;
      private final long imageId;
      private final String image;
      public final boolean empty;
      public final boolean minigame;
      public final RealmsWorldSlotButton.Action action;
      @Nullable
      private final Text actionPrompt;

      State(boolean isCurrentlyActiveSlot, String slotName, long imageId, @Nullable String image, boolean empty, boolean minigame, RealmsWorldSlotButton.Action action, @Nullable Text actionPrompt) {
         this.isCurrentlyActiveSlot = isCurrentlyActiveSlot;
         this.slotName = slotName;
         this.imageId = imageId;
         this.image = image;
         this.empty = empty;
         this.minigame = minigame;
         this.action = action;
         this.actionPrompt = actionPrompt;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Action {
      NOTHING,
      SWITCH_SLOT,
      JOIN;
   }
}
