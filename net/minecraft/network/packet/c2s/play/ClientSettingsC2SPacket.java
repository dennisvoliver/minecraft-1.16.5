package net.minecraft.network.packet.c2s.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Arm;

public class ClientSettingsC2SPacket implements Packet<ServerPlayPacketListener> {
   private String language;
   private int viewDistance;
   private ChatVisibility chatVisibility;
   private boolean chatColors;
   private int playerModelBitMask;
   private Arm mainArm;

   public ClientSettingsC2SPacket() {
   }

   @Environment(EnvType.CLIENT)
   public ClientSettingsC2SPacket(String language, int viewDistance, ChatVisibility chatVisibility, boolean chatColors, int modelBitMask, Arm mainArm) {
      this.language = language;
      this.viewDistance = viewDistance;
      this.chatVisibility = chatVisibility;
      this.chatColors = chatColors;
      this.playerModelBitMask = modelBitMask;
      this.mainArm = mainArm;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.language = buf.readString(16);
      this.viewDistance = buf.readByte();
      this.chatVisibility = (ChatVisibility)buf.readEnumConstant(ChatVisibility.class);
      this.chatColors = buf.readBoolean();
      this.playerModelBitMask = buf.readUnsignedByte();
      this.mainArm = (Arm)buf.readEnumConstant(Arm.class);
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeString(this.language);
      buf.writeByte(this.viewDistance);
      buf.writeEnumConstant(this.chatVisibility);
      buf.writeBoolean(this.chatColors);
      buf.writeByte(this.playerModelBitMask);
      buf.writeEnumConstant(this.mainArm);
   }

   public void apply(ServerPlayPacketListener serverPlayPacketListener) {
      serverPlayPacketListener.onClientSettings(this);
   }

   public ChatVisibility getChatVisibility() {
      return this.chatVisibility;
   }

   public boolean hasChatColors() {
      return this.chatColors;
   }

   public int getPlayerModelBitMask() {
      return this.playerModelBitMask;
   }

   public Arm getMainArm() {
      return this.mainArm;
   }
}
