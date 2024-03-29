package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderS2CPacket implements Packet<ClientPlayPacketListener> {
   private WorldBorderS2CPacket.Type type;
   private int portalTeleportPosLimit;
   private double centerX;
   private double centerZ;
   private double size;
   private double oldSize;
   private long interpolationDuration;
   private int warningTime;
   private int warningBlocks;

   public WorldBorderS2CPacket() {
   }

   public WorldBorderS2CPacket(WorldBorder border, WorldBorderS2CPacket.Type type) {
      this.type = type;
      this.centerX = border.getCenterX();
      this.centerZ = border.getCenterZ();
      this.oldSize = border.getSize();
      this.size = border.getSizeLerpTarget();
      this.interpolationDuration = border.getSizeLerpTime();
      this.portalTeleportPosLimit = border.getMaxRadius();
      this.warningBlocks = border.getWarningBlocks();
      this.warningTime = border.getWarningTime();
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.type = (WorldBorderS2CPacket.Type)buf.readEnumConstant(WorldBorderS2CPacket.Type.class);
      switch(this.type) {
      case SET_SIZE:
         this.size = buf.readDouble();
         break;
      case LERP_SIZE:
         this.oldSize = buf.readDouble();
         this.size = buf.readDouble();
         this.interpolationDuration = buf.readVarLong();
         break;
      case SET_CENTER:
         this.centerX = buf.readDouble();
         this.centerZ = buf.readDouble();
         break;
      case SET_WARNING_BLOCKS:
         this.warningBlocks = buf.readVarInt();
         break;
      case SET_WARNING_TIME:
         this.warningTime = buf.readVarInt();
         break;
      case INITIALIZE:
         this.centerX = buf.readDouble();
         this.centerZ = buf.readDouble();
         this.oldSize = buf.readDouble();
         this.size = buf.readDouble();
         this.interpolationDuration = buf.readVarLong();
         this.portalTeleportPosLimit = buf.readVarInt();
         this.warningBlocks = buf.readVarInt();
         this.warningTime = buf.readVarInt();
      }

   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeEnumConstant(this.type);
      switch(this.type) {
      case SET_SIZE:
         buf.writeDouble(this.size);
         break;
      case LERP_SIZE:
         buf.writeDouble(this.oldSize);
         buf.writeDouble(this.size);
         buf.writeVarLong(this.interpolationDuration);
         break;
      case SET_CENTER:
         buf.writeDouble(this.centerX);
         buf.writeDouble(this.centerZ);
         break;
      case SET_WARNING_BLOCKS:
         buf.writeVarInt(this.warningBlocks);
         break;
      case SET_WARNING_TIME:
         buf.writeVarInt(this.warningTime);
         break;
      case INITIALIZE:
         buf.writeDouble(this.centerX);
         buf.writeDouble(this.centerZ);
         buf.writeDouble(this.oldSize);
         buf.writeDouble(this.size);
         buf.writeVarLong(this.interpolationDuration);
         buf.writeVarInt(this.portalTeleportPosLimit);
         buf.writeVarInt(this.warningBlocks);
         buf.writeVarInt(this.warningTime);
      }

   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onWorldBorder(this);
   }

   @Environment(EnvType.CLIENT)
   public void apply(WorldBorder border) {
      switch(this.type) {
      case SET_SIZE:
         border.setSize(this.size);
         break;
      case LERP_SIZE:
         border.interpolateSize(this.oldSize, this.size, this.interpolationDuration);
         break;
      case SET_CENTER:
         border.setCenter(this.centerX, this.centerZ);
         break;
      case SET_WARNING_BLOCKS:
         border.setWarningBlocks(this.warningBlocks);
         break;
      case SET_WARNING_TIME:
         border.setWarningTime(this.warningTime);
         break;
      case INITIALIZE:
         border.setCenter(this.centerX, this.centerZ);
         if (this.interpolationDuration > 0L) {
            border.interpolateSize(this.oldSize, this.size, this.interpolationDuration);
         } else {
            border.setSize(this.size);
         }

         border.setMaxRadius(this.portalTeleportPosLimit);
         border.setWarningBlocks(this.warningBlocks);
         border.setWarningTime(this.warningTime);
      }

   }

   public static enum Type {
      SET_SIZE,
      LERP_SIZE,
      SET_CENTER,
      INITIALIZE,
      SET_WARNING_TIME,
      SET_WARNING_BLOCKS;
   }
}
