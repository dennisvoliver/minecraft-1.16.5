package net.minecraft.client.util;

import com.mojang.text2speech.Narrator;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ClientChatListener;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class NarratorManager implements ClientChatListener {
   public static final Text EMPTY;
   private static final Logger LOGGER;
   public static final NarratorManager INSTANCE;
   private final Narrator narrator = Narrator.getNarrator();

   public void onChatMessage(MessageType messageType, Text message, UUID sender) {
      NarratorMode narratorMode = getNarratorOption();
      if (narratorMode != NarratorMode.OFF && this.narrator.active()) {
         if (narratorMode == NarratorMode.ALL || narratorMode == NarratorMode.CHAT && messageType == MessageType.CHAT || narratorMode == NarratorMode.SYSTEM && messageType == MessageType.SYSTEM) {
            Object text2;
            if (message instanceof TranslatableText && "chat.type.text".equals(((TranslatableText)message).getKey())) {
               text2 = new TranslatableText("chat.type.text.narrate", ((TranslatableText)message).getArgs());
            } else {
               text2 = message;
            }

            this.narrate(messageType.interruptsNarration(), ((Text)text2).getString());
         }

      }
   }

   public void narrate(String text) {
      NarratorMode narratorMode = getNarratorOption();
      if (this.narrator.active() && narratorMode != NarratorMode.OFF && narratorMode != NarratorMode.CHAT && !text.isEmpty()) {
         this.narrator.clear();
         this.narrate(true, text);
      }

   }

   private static NarratorMode getNarratorOption() {
      return MinecraftClient.getInstance().options.narrator;
   }

   private void narrate(boolean interrupt, String message) {
      if (SharedConstants.isDevelopment) {
         LOGGER.debug((String)"Narrating: {}", (Object)message.replaceAll("\n", "\\\\n"));
      }

      this.narrator.say(message, interrupt);
   }

   public void addToast(NarratorMode option) {
      this.clear();
      this.narrator.say((new TranslatableText("options.narrator")).append(" : ").append(option.getName()).getString(), true);
      ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
      if (this.narrator.active()) {
         if (option == NarratorMode.OFF) {
            SystemToast.show(toastManager, SystemToast.Type.NARRATOR_TOGGLE, new TranslatableText("narrator.toast.disabled"), (Text)null);
         } else {
            SystemToast.show(toastManager, SystemToast.Type.NARRATOR_TOGGLE, new TranslatableText("narrator.toast.enabled"), option.getName());
         }
      } else {
         SystemToast.show(toastManager, SystemToast.Type.NARRATOR_TOGGLE, new TranslatableText("narrator.toast.disabled"), new TranslatableText("options.narrator.notavailable"));
      }

   }

   public boolean isActive() {
      return this.narrator.active();
   }

   public void clear() {
      if (getNarratorOption() != NarratorMode.OFF && this.narrator.active()) {
         this.narrator.clear();
      }
   }

   public void destroy() {
      this.narrator.destroy();
   }

   static {
      EMPTY = LiteralText.EMPTY;
      LOGGER = LogManager.getLogger();
      INSTANCE = new NarratorManager();
   }
}
