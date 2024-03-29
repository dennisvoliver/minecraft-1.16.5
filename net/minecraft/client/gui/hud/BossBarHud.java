package net.minecraft.client.gui.hud;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BossBarHud extends DrawableHelper {
   private static final Identifier BARS_TEXTURE = new Identifier("textures/gui/bars.png");
   private final MinecraftClient client;
   private final Map<UUID, ClientBossBar> bossBars = Maps.newLinkedHashMap();

   public BossBarHud(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices) {
      if (!this.bossBars.isEmpty()) {
         int i = this.client.getWindow().getScaledWidth();
         int j = 12;
         Iterator var4 = this.bossBars.values().iterator();

         while(var4.hasNext()) {
            ClientBossBar clientBossBar = (ClientBossBar)var4.next();
            int k = i / 2 - 91;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.client.getTextureManager().bindTexture(BARS_TEXTURE);
            this.renderBossBar(matrices, k, j, clientBossBar);
            Text text = clientBossBar.getName();
            int m = this.client.textRenderer.getWidth((StringVisitable)text);
            int n = i / 2 - m / 2;
            int o = j - 9;
            this.client.textRenderer.drawWithShadow(matrices, text, (float)n, (float)o, 16777215);
            this.client.textRenderer.getClass();
            j += 10 + 9;
            if (j >= this.client.getWindow().getScaledHeight() / 3) {
               break;
            }
         }

      }
   }

   private void renderBossBar(MatrixStack matrices, int x, int y, BossBar bossBar) {
      this.drawTexture(matrices, x, y, 0, bossBar.getColor().ordinal() * 5 * 2, 182, 5);
      if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
         this.drawTexture(matrices, x, y, 0, 80 + (bossBar.getStyle().ordinal() - 1) * 5 * 2, 182, 5);
      }

      int i = (int)(bossBar.getPercent() * 183.0F);
      if (i > 0) {
         this.drawTexture(matrices, x, y, 0, bossBar.getColor().ordinal() * 5 * 2 + 5, i, 5);
         if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
            this.drawTexture(matrices, x, y, 0, 80 + (bossBar.getStyle().ordinal() - 1) * 5 * 2 + 5, i, 5);
         }
      }

   }

   public void handlePacket(BossBarS2CPacket packet) {
      if (packet.getType() == BossBarS2CPacket.Type.ADD) {
         this.bossBars.put(packet.getUuid(), new ClientBossBar(packet));
      } else if (packet.getType() == BossBarS2CPacket.Type.REMOVE) {
         this.bossBars.remove(packet.getUuid());
      } else {
         ((ClientBossBar)this.bossBars.get(packet.getUuid())).handlePacket(packet);
      }

   }

   public void clear() {
      this.bossBars.clear();
   }

   public boolean shouldPlayDragonMusic() {
      if (!this.bossBars.isEmpty()) {
         Iterator var1 = this.bossBars.values().iterator();

         while(var1.hasNext()) {
            BossBar bossBar = (BossBar)var1.next();
            if (bossBar.hasDragonMusic()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldDarkenSky() {
      if (!this.bossBars.isEmpty()) {
         Iterator var1 = this.bossBars.values().iterator();

         while(var1.hasNext()) {
            BossBar bossBar = (BossBar)var1.next();
            if (bossBar.shouldDarkenSky()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldThickenFog() {
      if (!this.bossBars.isEmpty()) {
         Iterator var1 = this.bossBars.values().iterator();

         while(var1.hasNext()) {
            BossBar bossBar = (BossBar)var1.next();
            if (bossBar.shouldThickenFog()) {
               return true;
            }
         }
      }

      return false;
   }
}
