package net.minecraft.world.level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.dynamic.RegistryReadingOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LevelProperties implements ServerWorldProperties, SaveProperties {
   private static final Logger LOGGER = LogManager.getLogger();
   private LevelInfo levelInfo;
   private final GeneratorOptions generatorOptions;
   private final Lifecycle lifecycle;
   private int spawnX;
   private int spawnY;
   private int spawnZ;
   private float spawnAngle;
   private long time;
   private long timeOfDay;
   @Nullable
   private final DataFixer dataFixer;
   private final int dataVersion;
   private boolean playerDataLoaded;
   @Nullable
   private NbtCompound playerData;
   private final int version;
   private int clearWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private boolean initialized;
   private boolean difficultyLocked;
   private WorldBorder.Properties worldBorder;
   private NbtCompound dragonFight;
   @Nullable
   private NbtCompound customBossEvents;
   private int wanderingTraderSpawnDelay;
   private int wanderingTraderSpawnChance;
   @Nullable
   private UUID wanderingTraderId;
   private final Set<String> serverBrands;
   private boolean modded;
   private final Timer<MinecraftServer> scheduledEvents;

   private LevelProperties(@Nullable DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, boolean modded, int spawnX, int spawnY, int spawnZ, float spawnAngle, long time, long timeOfDay, int version, int clearWeatherTime, int rainTime, boolean raining, int thunderTime, boolean thundering, boolean initialized, boolean difficultyLocked, WorldBorder.Properties worldBorder, int wanderingTraderSpawnDelay, int wanderingTraderSpawnChance, @Nullable UUID wanderingTraderId, LinkedHashSet<String> serverBrands, Timer<MinecraftServer> scheduledEvents, @Nullable NbtCompound customBossEvents, NbtCompound dragonFight, LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle) {
      this.dataFixer = dataFixer;
      this.modded = modded;
      this.spawnX = spawnX;
      this.spawnY = spawnY;
      this.spawnZ = spawnZ;
      this.spawnAngle = spawnAngle;
      this.time = time;
      this.timeOfDay = timeOfDay;
      this.version = version;
      this.clearWeatherTime = clearWeatherTime;
      this.rainTime = rainTime;
      this.raining = raining;
      this.thunderTime = thunderTime;
      this.thundering = thundering;
      this.initialized = initialized;
      this.difficultyLocked = difficultyLocked;
      this.worldBorder = worldBorder;
      this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
      this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
      this.wanderingTraderId = wanderingTraderId;
      this.serverBrands = serverBrands;
      this.playerData = playerData;
      this.dataVersion = dataVersion;
      this.scheduledEvents = scheduledEvents;
      this.customBossEvents = customBossEvents;
      this.dragonFight = dragonFight;
      this.levelInfo = levelInfo;
      this.generatorOptions = generatorOptions;
      this.lifecycle = lifecycle;
   }

   public LevelProperties(LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle) {
      this((DataFixer)null, SharedConstants.getGameVersion().getWorldVersion(), (NbtCompound)null, false, 0, 0, 0, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_BORDER, 0, 0, (UUID)null, Sets.newLinkedHashSet(), new Timer(TimerCallbackSerializer.INSTANCE), (NbtCompound)null, new NbtCompound(), levelInfo.withCopiedGameRules(), generatorOptions, lifecycle);
   }

   public static LevelProperties readProperties(Dynamic<NbtElement> dynamic, DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle) {
      long l = dynamic.get("Time").asLong(0L);
      NbtCompound nbtCompound = (NbtCompound)dynamic.get("DragonFight").result().map(Dynamic::getValue).orElseGet(() -> {
         return (NbtElement)dynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap().getValue();
      });
      return new LevelProperties(dataFixer, dataVersion, playerData, dynamic.get("WasModded").asBoolean(false), dynamic.get("SpawnX").asInt(0), dynamic.get("SpawnY").asInt(0), dynamic.get("SpawnZ").asInt(0), dynamic.get("SpawnAngle").asFloat(0.0F), l, dynamic.get("DayTime").asLong(l), saveVersionInfo.getLevelFormatVersion(), dynamic.get("clearWeatherTime").asInt(0), dynamic.get("rainTime").asInt(0), dynamic.get("raining").asBoolean(false), dynamic.get("thunderTime").asInt(0), dynamic.get("thundering").asBoolean(false), dynamic.get("initialized").asBoolean(true), dynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Properties.fromDynamic(dynamic, WorldBorder.DEFAULT_BORDER), dynamic.get("WanderingTraderSpawnDelay").asInt(0), dynamic.get("WanderingTraderSpawnChance").asInt(0), (UUID)dynamic.get("WanderingTraderId").read(DynamicSerializableUuid.CODEC).result().orElse((Object)null), (LinkedHashSet)dynamic.get("ServerBrands").asStream().flatMap((dynamicx) -> {
         return Util.stream(dynamicx.asString().result());
      }).collect(Collectors.toCollection(Sets::newLinkedHashSet)), new Timer(TimerCallbackSerializer.INSTANCE, dynamic.get("ScheduledEvents").asStream()), (NbtCompound)dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), nbtCompound, levelInfo, generatorOptions, lifecycle);
   }

   public NbtCompound cloneWorldNbt(DynamicRegistryManager registryManager, @Nullable NbtCompound playerNbt) {
      this.loadPlayerData();
      if (playerNbt == null) {
         playerNbt = this.playerData;
      }

      NbtCompound nbtCompound = new NbtCompound();
      this.updateProperties(registryManager, nbtCompound, playerNbt);
      return nbtCompound;
   }

   private void updateProperties(DynamicRegistryManager registryManager, NbtCompound levelTag, @Nullable NbtCompound playerTag) {
      NbtList nbtList = new NbtList();
      this.serverBrands.stream().map(NbtString::of).forEach(nbtList::add);
      levelTag.put("ServerBrands", nbtList);
      levelTag.putBoolean("WasModded", this.modded);
      NbtCompound nbtCompound = new NbtCompound();
      nbtCompound.putString("Name", SharedConstants.getGameVersion().getName());
      nbtCompound.putInt("Id", SharedConstants.getGameVersion().getWorldVersion());
      nbtCompound.putBoolean("Snapshot", !SharedConstants.getGameVersion().isStable());
      levelTag.put("Version", nbtCompound);
      levelTag.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
      RegistryReadingOps<NbtElement> registryReadingOps = RegistryReadingOps.of(NbtOps.INSTANCE, registryManager);
      DataResult var10000 = GeneratorOptions.CODEC.encodeStart(registryReadingOps, this.generatorOptions);
      Logger var10002 = LOGGER;
      var10002.getClass();
      var10000.resultOrPartial(Util.method_29188("WorldGenSettings: ", var10002::error)).ifPresent((nbtElement) -> {
         levelTag.put("WorldGenSettings", nbtElement);
      });
      levelTag.putInt("GameType", this.levelInfo.getGameMode().getId());
      levelTag.putInt("SpawnX", this.spawnX);
      levelTag.putInt("SpawnY", this.spawnY);
      levelTag.putInt("SpawnZ", this.spawnZ);
      levelTag.putFloat("SpawnAngle", this.spawnAngle);
      levelTag.putLong("Time", this.time);
      levelTag.putLong("DayTime", this.timeOfDay);
      levelTag.putLong("LastPlayed", Util.getEpochTimeMs());
      levelTag.putString("LevelName", this.levelInfo.getLevelName());
      levelTag.putInt("version", 19133);
      levelTag.putInt("clearWeatherTime", this.clearWeatherTime);
      levelTag.putInt("rainTime", this.rainTime);
      levelTag.putBoolean("raining", this.raining);
      levelTag.putInt("thunderTime", this.thunderTime);
      levelTag.putBoolean("thundering", this.thundering);
      levelTag.putBoolean("hardcore", this.levelInfo.isHardcore());
      levelTag.putBoolean("allowCommands", this.levelInfo.areCommandsAllowed());
      levelTag.putBoolean("initialized", this.initialized);
      this.worldBorder.toTag(levelTag);
      levelTag.putByte("Difficulty", (byte)this.levelInfo.getDifficulty().getId());
      levelTag.putBoolean("DifficultyLocked", this.difficultyLocked);
      levelTag.put("GameRules", this.levelInfo.getGameRules().toNbt());
      levelTag.put("DragonFight", this.dragonFight);
      if (playerTag != null) {
         levelTag.put("Player", playerTag);
      }

      DataPackSettings.CODEC.encodeStart(NbtOps.INSTANCE, this.levelInfo.getDataPackSettings()).result().ifPresent((nbtElement) -> {
         levelTag.put("DataPacks", nbtElement);
      });
      if (this.customBossEvents != null) {
         levelTag.put("CustomBossEvents", this.customBossEvents);
      }

      levelTag.put("ScheduledEvents", this.scheduledEvents.toNbt());
      levelTag.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
      levelTag.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
      if (this.wanderingTraderId != null) {
         levelTag.putUuid("WanderingTraderId", this.wanderingTraderId);
      }

   }

   public int getSpawnX() {
      return this.spawnX;
   }

   public int getSpawnY() {
      return this.spawnY;
   }

   public int getSpawnZ() {
      return this.spawnZ;
   }

   public float getSpawnAngle() {
      return this.spawnAngle;
   }

   public long getTime() {
      return this.time;
   }

   public long getTimeOfDay() {
      return this.timeOfDay;
   }

   private void loadPlayerData() {
      if (!this.playerDataLoaded && this.playerData != null) {
         if (this.dataVersion < SharedConstants.getGameVersion().getWorldVersion()) {
            if (this.dataFixer == null) {
               throw (NullPointerException)Util.throwOrPause(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
            }

            this.playerData = NbtHelper.update(this.dataFixer, DataFixTypes.PLAYER, this.playerData, this.dataVersion);
         }

         this.playerDataLoaded = true;
      }
   }

   public NbtCompound getPlayerData() {
      this.loadPlayerData();
      return this.playerData;
   }

   public void setSpawnX(int spawnX) {
      this.spawnX = spawnX;
   }

   public void setSpawnY(int spawnY) {
      this.spawnY = spawnY;
   }

   public void setSpawnZ(int spawnZ) {
      this.spawnZ = spawnZ;
   }

   public void setSpawnAngle(float angle) {
      this.spawnAngle = angle;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public void setTimeOfDay(long timeOfDay) {
      this.timeOfDay = timeOfDay;
   }

   public void setSpawnPos(BlockPos pos, float angle) {
      this.spawnX = pos.getX();
      this.spawnY = pos.getY();
      this.spawnZ = pos.getZ();
      this.spawnAngle = angle;
   }

   public String getLevelName() {
      return this.levelInfo.getLevelName();
   }

   public int getVersion() {
      return this.version;
   }

   public int getClearWeatherTime() {
      return this.clearWeatherTime;
   }

   public void setClearWeatherTime(int clearWeatherTime) {
      this.clearWeatherTime = clearWeatherTime;
   }

   public boolean isThundering() {
      return this.thundering;
   }

   public void setThundering(boolean thundering) {
      this.thundering = thundering;
   }

   public int getThunderTime() {
      return this.thunderTime;
   }

   public void setThunderTime(int thunderTime) {
      this.thunderTime = thunderTime;
   }

   public boolean isRaining() {
      return this.raining;
   }

   public void setRaining(boolean raining) {
      this.raining = raining;
   }

   public int getRainTime() {
      return this.rainTime;
   }

   public void setRainTime(int rainTime) {
      this.rainTime = rainTime;
   }

   public GameMode getGameMode() {
      return this.levelInfo.getGameMode();
   }

   public void setGameMode(GameMode gameMode) {
      this.levelInfo = this.levelInfo.withGameMode(gameMode);
   }

   public boolean isHardcore() {
      return this.levelInfo.isHardcore();
   }

   public boolean areCommandsAllowed() {
      return this.levelInfo.areCommandsAllowed();
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setInitialized(boolean initialized) {
      this.initialized = initialized;
   }

   public GameRules getGameRules() {
      return this.levelInfo.getGameRules();
   }

   public WorldBorder.Properties getWorldBorder() {
      return this.worldBorder;
   }

   public void setWorldBorder(WorldBorder.Properties properties) {
      this.worldBorder = properties;
   }

   public Difficulty getDifficulty() {
      return this.levelInfo.getDifficulty();
   }

   public void setDifficulty(Difficulty difficulty) {
      this.levelInfo = this.levelInfo.withDifficulty(difficulty);
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   public void setDifficultyLocked(boolean locked) {
      this.difficultyLocked = locked;
   }

   public Timer<MinecraftServer> getScheduledEvents() {
      return this.scheduledEvents;
   }

   public void populateCrashReport(CrashReportSection reportSection) {
      ServerWorldProperties.super.populateCrashReport(reportSection);
      SaveProperties.super.populateCrashReport(reportSection);
   }

   public GeneratorOptions getGeneratorOptions() {
      return this.generatorOptions;
   }

   @Environment(EnvType.CLIENT)
   public Lifecycle getLifecycle() {
      return this.lifecycle;
   }

   public NbtCompound getDragonFight() {
      return this.dragonFight;
   }

   public void setDragonFight(NbtCompound nbt) {
      this.dragonFight = nbt;
   }

   public DataPackSettings getDataPackSettings() {
      return this.levelInfo.getDataPackSettings();
   }

   public void updateLevelInfo(DataPackSettings dataPackSettings) {
      this.levelInfo = this.levelInfo.withDataPackSettings(dataPackSettings);
   }

   @Nullable
   public NbtCompound getCustomBossEvents() {
      return this.customBossEvents;
   }

   public void setCustomBossEvents(@Nullable NbtCompound nbt) {
      this.customBossEvents = nbt;
   }

   public int getWanderingTraderSpawnDelay() {
      return this.wanderingTraderSpawnDelay;
   }

   public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay) {
      this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
   }

   public int getWanderingTraderSpawnChance() {
      return this.wanderingTraderSpawnChance;
   }

   public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance) {
      this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
   }

   public void setWanderingTraderId(UUID uuid) {
      this.wanderingTraderId = uuid;
   }

   public void addServerBrand(String brand, boolean modded) {
      this.serverBrands.add(brand);
      this.modded |= modded;
   }

   public boolean isModded() {
      return this.modded;
   }

   public Set<String> getServerBrands() {
      return ImmutableSet.copyOf((Collection)this.serverBrands);
   }

   public ServerWorldProperties getMainWorldProperties() {
      return this;
   }

   @Environment(EnvType.CLIENT)
   public LevelInfo getLevelInfo() {
      return this.levelInfo.withCopiedGameRules();
   }
}
