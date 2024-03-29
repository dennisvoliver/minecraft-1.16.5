package net.minecraft.client.realms.task;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.util.Errable;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public abstract class LongRunningTask implements Errable, Runnable {
   public static final Logger LOGGER = LogManager.getLogger();
   protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

   /**
    * Moved from RealmsTasks in 20w10a.
    */
   protected static void pause(int seconds) {
      try {
         Thread.sleep((long)(seconds * 1000));
      } catch (InterruptedException var2) {
         LOGGER.error((String)"", (Throwable)var2);
      }

   }

   /**
    * Moved from Realms in 20w10a.
    */
   public static void setScreen(Screen screen) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      minecraftClient.execute(() -> {
         minecraftClient.openScreen(screen);
      });
   }

   public void setScreen(RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen) {
      this.longRunningMcoTaskScreen = longRunningMcoTaskScreen;
   }

   public void error(Text errorMessage) {
      this.longRunningMcoTaskScreen.error(errorMessage);
   }

   public void setTitle(Text title) {
      this.longRunningMcoTaskScreen.setTitle(title);
   }

   public boolean aborted() {
      return this.longRunningMcoTaskScreen.aborted();
   }

   public void tick() {
   }

   public void init() {
   }

   public void abortTask() {
   }
}
