package net.minecraft.client.realms.gui.screen;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TickableElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.realms.Realms;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.client.util.NarratorManager;

@Environment(EnvType.CLIENT)
public abstract class RealmsScreen extends Screen {
   public RealmsScreen() {
      super(NarratorManager.EMPTY);
   }

   /**
    * Moved from RealmsConstants in 20w10a
    */
   protected static int row(int index) {
      return 40 + index * 13;
   }

   public void tick() {
      Iterator var1 = this.buttons.iterator();

      while(var1.hasNext()) {
         ClickableWidget clickableWidget = (ClickableWidget)var1.next();
         if (clickableWidget instanceof TickableElement) {
            ((TickableElement)clickableWidget).tick();
         }
      }

   }

   public void narrateLabels() {
      Stream var10000 = this.children.stream();
      RealmsLabel.class.getClass();
      var10000 = var10000.filter(RealmsLabel.class::isInstance);
      RealmsLabel.class.getClass();
      List<String> list = (List)var10000.map(RealmsLabel.class::cast).map(RealmsLabel::getText).collect(Collectors.toList());
      Realms.narrateNow((Iterable)list);
   }
}
