package net.minecraft.world.level;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.timer.Timer;

public class UnmodifiableLevelProperties implements ServerWorldProperties {
   private final SaveProperties field_24179;
   private final ServerWorldProperties worldProperties;

   public UnmodifiableLevelProperties(SaveProperties saveProperties, ServerWorldProperties serverWorldProperties) {
      this.field_24179 = saveProperties;
      this.worldProperties = serverWorldProperties;
   }

   public int getSpawnX() {
      return this.worldProperties.getSpawnX();
   }

   public int getSpawnY() {
      return this.worldProperties.getSpawnY();
   }

   public int getSpawnZ() {
      return this.worldProperties.getSpawnZ();
   }

   public float getSpawnAngle() {
      return this.worldProperties.getSpawnAngle();
   }

   public long getTime() {
      return this.worldProperties.getTime();
   }

   public long getTimeOfDay() {
      return this.worldProperties.getTimeOfDay();
   }

   public String getLevelName() {
      return this.field_24179.getLevelName();
   }

   public int getClearWeatherTime() {
      return this.worldProperties.getClearWeatherTime();
   }

   public void setClearWeatherTime(int clearWeatherTime) {
   }

   public boolean isThundering() {
      return this.worldProperties.isThundering();
   }

   public int getThunderTime() {
      return this.worldProperties.getThunderTime();
   }

   public boolean isRaining() {
      return this.worldProperties.isRaining();
   }

   public int getRainTime() {
      return this.worldProperties.getRainTime();
   }

   public GameMode getGameMode() {
      return this.field_24179.getGameMode();
   }

   public void setSpawnX(int spawnX) {
   }

   public void setSpawnY(int spawnY) {
   }

   public void setSpawnZ(int spawnZ) {
   }

   public void setSpawnAngle(float angle) {
   }

   public void setTime(long time) {
   }

   public void setTimeOfDay(long timeOfDay) {
   }

   public void setSpawnPos(BlockPos pos, float angle) {
   }

   public void setThundering(boolean thundering) {
   }

   public void setThunderTime(int thunderTime) {
   }

   public void setRaining(boolean raining) {
   }

   public void setRainTime(int rainTime) {
   }

   public void setGameMode(GameMode gameMode) {
   }

   public boolean isHardcore() {
      return this.field_24179.isHardcore();
   }

   public boolean areCommandsAllowed() {
      return this.field_24179.areCommandsAllowed();
   }

   public boolean isInitialized() {
      return this.worldProperties.isInitialized();
   }

   public void setInitialized(boolean initialized) {
   }

   public GameRules getGameRules() {
      return this.field_24179.getGameRules();
   }

   public WorldBorder.Properties getWorldBorder() {
      return this.worldProperties.getWorldBorder();
   }

   public void setWorldBorder(WorldBorder.Properties properties) {
   }

   public Difficulty getDifficulty() {
      return this.field_24179.getDifficulty();
   }

   public boolean isDifficultyLocked() {
      return this.field_24179.isDifficultyLocked();
   }

   public Timer<MinecraftServer> getScheduledEvents() {
      return this.worldProperties.getScheduledEvents();
   }

   public int getWanderingTraderSpawnDelay() {
      return 0;
   }

   public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay) {
   }

   public int getWanderingTraderSpawnChance() {
      return 0;
   }

   public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance) {
   }

   public void setWanderingTraderId(UUID uuid) {
   }

   public void populateCrashReport(CrashReportSection reportSection) {
      reportSection.add("Derived", (Object)true);
      this.worldProperties.populateCrashReport(reportSection);
   }
}
