package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.CharPredicate;
import net.minecraft.datafixer.Schemas;
import net.minecraft.state.property.Property;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Util {
   private static final AtomicInteger NEXT_WORKER_ID = new AtomicInteger(1);
   private static final ExecutorService BOOTSTRAP_EXECUTOR = createWorker("Bootstrap");
   private static final ExecutorService MAIN_WORKER_EXECUTOR = createWorker("Main");
   private static final ExecutorService IO_WORKER_EXECUTOR = createIoWorker();
   public static LongSupplier nanoTimeSupplier = System::nanoTime;
   public static final UUID NIL_UUID = new UUID(0L, 0L);
   private static final Logger LOGGER = LogManager.getLogger();

   public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
      return Collectors.toMap(Entry::getKey, Entry::getValue);
   }

   public static <T extends Comparable<T>> String getValueAsString(Property<T> property, Object value) {
      return property.name((Comparable)value);
   }

   public static String createTranslationKey(String type, @Nullable Identifier id) {
      return id == null ? type + ".unregistered_sadface" : type + '.' + id.getNamespace() + '.' + id.getPath().replace('/', '.');
   }

   public static long getMeasuringTimeMs() {
      return getMeasuringTimeNano() / 1000000L;
   }

   public static long getMeasuringTimeNano() {
      return nanoTimeSupplier.getAsLong();
   }

   public static long getEpochTimeMs() {
      return Instant.now().toEpochMilli();
   }

   private static ExecutorService createWorker(String name) {
      int i = MathHelper.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
      Object executorService2;
      if (i <= 0) {
         executorService2 = MoreExecutors.newDirectExecutorService();
      } else {
         executorService2 = new ForkJoinPool(i, (forkJoinPool) -> {
            ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
               protected void onTermination(Throwable throwable) {
                  if (throwable != null) {
                     Util.LOGGER.warn((String)"{} died", (Object)this.getName(), (Object)throwable);
                  } else {
                     Util.LOGGER.debug((String)"{} shutdown", (Object)this.getName());
                  }

                  super.onTermination(throwable);
               }
            };
            forkJoinWorkerThread.setName("Worker-" + name + "-" + NEXT_WORKER_ID.getAndIncrement());
            return forkJoinWorkerThread;
         }, Util::method_18347, true);
      }

      return (ExecutorService)executorService2;
   }

   public static Executor getBootstrapExecutor() {
      return BOOTSTRAP_EXECUTOR;
   }

   public static Executor getMainWorkerExecutor() {
      return MAIN_WORKER_EXECUTOR;
   }

   public static Executor getIoWorkerExecutor() {
      return IO_WORKER_EXECUTOR;
   }

   public static void shutdownExecutors() {
      attemptShutdown(MAIN_WORKER_EXECUTOR);
      attemptShutdown(IO_WORKER_EXECUTOR);
   }

   private static void attemptShutdown(ExecutorService service) {
      service.shutdown();

      boolean bl2;
      try {
         bl2 = service.awaitTermination(3L, TimeUnit.SECONDS);
      } catch (InterruptedException var3) {
         bl2 = false;
      }

      if (!bl2) {
         service.shutdownNow();
      }

   }

   private static ExecutorService createIoWorker() {
      return Executors.newCachedThreadPool((runnable) -> {
         Thread thread = new Thread(runnable);
         thread.setName("IO-Worker-" + NEXT_WORKER_ID.getAndIncrement());
         thread.setUncaughtExceptionHandler(Util::method_18347);
         return thread;
      });
   }

   @Environment(EnvType.CLIENT)
   public static <T> CompletableFuture<T> completeExceptionally(Throwable throwable) {
      CompletableFuture<T> completableFuture = new CompletableFuture();
      completableFuture.completeExceptionally(throwable);
      return completableFuture;
   }

   @Environment(EnvType.CLIENT)
   public static void throwUnchecked(Throwable t) {
      throw t instanceof RuntimeException ? (RuntimeException)t : new RuntimeException(t);
   }

   private static void method_18347(Thread thread, Throwable throwable) {
      throwOrPause(throwable);
      if (throwable instanceof CompletionException) {
         throwable = throwable.getCause();
      }

      if (throwable instanceof CrashException) {
         Bootstrap.println(((CrashException)throwable).getReport().asString());
         System.exit(-1);
      }

      LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
   }

   @Nullable
   public static Type<?> getChoiceType(TypeReference typeReference, String id) {
      return !SharedConstants.useChoiceTypeRegistrations ? null : getChoiceTypeInternal(typeReference, id);
   }

   @Nullable
   private static Type<?> getChoiceTypeInternal(TypeReference typeReference, String id) {
      Type type = null;

      try {
         type = Schemas.getFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion())).getChoiceType(typeReference, id);
      } catch (IllegalArgumentException var4) {
         LOGGER.error((String)"No data fixer registered for {}", (Object)id);
         if (SharedConstants.isDevelopment) {
            throw var4;
         }
      }

      return type;
   }

   public static Util.OperatingSystem getOperatingSystem() {
      String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (string.contains("win")) {
         return Util.OperatingSystem.WINDOWS;
      } else if (string.contains("mac")) {
         return Util.OperatingSystem.OSX;
      } else if (string.contains("solaris")) {
         return Util.OperatingSystem.SOLARIS;
      } else if (string.contains("sunos")) {
         return Util.OperatingSystem.SOLARIS;
      } else if (string.contains("linux")) {
         return Util.OperatingSystem.LINUX;
      } else {
         return string.contains("unix") ? Util.OperatingSystem.LINUX : Util.OperatingSystem.UNKNOWN;
      }
   }

   public static Stream<String> getJVMFlags() {
      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      return runtimeMXBean.getInputArguments().stream().filter((string) -> {
         return string.startsWith("-X");
      });
   }

   public static <T> T getLast(List<T> list) {
      return list.get(list.size() - 1);
   }

   public static <T> T next(Iterable<T> iterable, @Nullable T object) {
      Iterator<T> iterator = iterable.iterator();
      T object2 = iterator.next();
      if (object != null) {
         Object object3 = object2;

         while(object3 != object) {
            if (iterator.hasNext()) {
               object3 = iterator.next();
            }
         }

         if (iterator.hasNext()) {
            return iterator.next();
         }
      }

      return object2;
   }

   public static <T> T previous(Iterable<T> iterable, @Nullable T object) {
      Iterator<T> iterator = iterable.iterator();

      Object object2;
      Object object3;
      for(object2 = null; iterator.hasNext(); object2 = object3) {
         object3 = iterator.next();
         if (object3 == object) {
            if (object2 == null) {
               object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
            }
            break;
         }
      }

      return object2;
   }

   public static <T> T make(Supplier<T> factory) {
      return factory.get();
   }

   public static <T> T make(T object, Consumer<T> initializer) {
      initializer.accept(object);
      return object;
   }

   public static <K> Strategy<K> identityHashStrategy() {
      return Util.IdentityHashStrategy.INSTANCE;
   }

   /**
    * Combines a list of {@code futures} into one future that holds a list
    * of their results.
    * 
    * <p>The returned future is fail-fast; if any of the input futures fails,
    * this returned future will be immediately completed exceptionally than
    * waiting for other input futures.
    * 
    * @return the combined future
    * 
    * @param futures the completable futures to combine
    */
   public static <V> CompletableFuture<List<V>> combine(List<? extends CompletableFuture<? extends V>> futures) {
      List<V> list = Lists.newArrayListWithCapacity(futures.size());
      CompletableFuture<?>[] completableFutures = new CompletableFuture[futures.size()];
      CompletableFuture<Void> completableFuture = new CompletableFuture();
      futures.forEach((completableFuture2) -> {
         int i = list.size();
         list.add((Object)null);
         completableFutures[i] = completableFuture2.whenComplete((object, throwable) -> {
            if (throwable != null) {
               completableFuture.completeExceptionally(throwable);
            } else {
               list.set(i, object);
            }

         });
      });
      return CompletableFuture.allOf(completableFutures).applyToEither(completableFuture, (void_) -> {
         return list;
      });
   }

   public static <T> Stream<T> stream(Optional<? extends T> optional) {
      return (Stream)DataFixUtils.orElseGet(optional.map(Stream::of), Stream::empty);
   }

   public static <T> Optional<T> ifPresentOrElse(Optional<T> optional, Consumer<T> presentAction, Runnable elseAction) {
      if (optional.isPresent()) {
         presentAction.accept(optional.get());
      } else {
         elseAction.run();
      }

      return optional;
   }

   public static Runnable debugRunnable(Runnable runnable, Supplier<String> messageSupplier) {
      return runnable;
   }

   public static <T extends Throwable> T throwOrPause(T t) {
      if (SharedConstants.isDevelopment) {
         LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);

         while(true) {
            try {
               Thread.sleep(1000L);
               LOGGER.error("paused");
            } catch (InterruptedException var2) {
               return t;
            }
         }
      } else {
         return t;
      }
   }

   public static String getInnermostMessage(Throwable t) {
      if (t.getCause() != null) {
         return getInnermostMessage(t.getCause());
      } else {
         return t.getMessage() != null ? t.getMessage() : t.toString();
      }
   }

   public static <T> T getRandom(T[] array, Random random) {
      return array[random.nextInt(array.length)];
   }

   public static int getRandom(int[] array, Random random) {
      return array[random.nextInt(array.length)];
   }

   private static BooleanSupplier renameTask(final Path src, final Path dest) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.move(src, dest);
               return true;
            } catch (IOException var2) {
               Util.LOGGER.error((String)"Failed to rename", (Throwable)var2);
               return false;
            }
         }

         public String toString() {
            return "rename " + src + " to " + dest;
         }
      };
   }

   private static BooleanSupplier deleteTask(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.deleteIfExists(path);
               return true;
            } catch (IOException var2) {
               Util.LOGGER.warn((String)"Failed to delete", (Throwable)var2);
               return false;
            }
         }

         public String toString() {
            return "delete old " + path;
         }
      };
   }

   private static BooleanSupplier deletionVerifyTask(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return !Files.exists(path, new LinkOption[0]);
         }

         public String toString() {
            return "verify that " + path + " is deleted";
         }
      };
   }

   private static BooleanSupplier existenceCheckTask(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return Files.isRegularFile(path, new LinkOption[0]);
         }

         public String toString() {
            return "verify that " + path + " is present";
         }
      };
   }

   private static boolean attemptTasks(BooleanSupplier... tasks) {
      BooleanSupplier[] var1 = tasks;
      int var2 = tasks.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         BooleanSupplier booleanSupplier = var1[var3];
         if (!booleanSupplier.getAsBoolean()) {
            LOGGER.warn((String)"Failed to execute {}", (Object)booleanSupplier);
            return false;
         }
      }

      return true;
   }

   private static boolean attemptTasks(int retries, String taskName, BooleanSupplier... tasks) {
      for(int i = 0; i < retries; ++i) {
         if (attemptTasks(tasks)) {
            return true;
         }

         LOGGER.error((String)"Failed to {}, retrying {}/{}", (Object)taskName, i, retries);
      }

      LOGGER.error((String)"Failed to {}, aborting, progress might be lost", (Object)taskName);
      return false;
   }

   public static void backupAndReplace(File current, File newFile, File backup) {
      backupAndReplace(current.toPath(), newFile.toPath(), backup.toPath());
   }

   /**
    * Copies {@code current} to {@code backup} and then replaces {@code current} with {@code newPath}
    */
   public static void backupAndReplace(Path current, Path newPath, Path backup) {
      int i = true;
      if (!Files.exists(current, new LinkOption[0]) || attemptTasks(10, "create backup " + backup, deleteTask(backup), renameTask(current, backup), existenceCheckTask(backup))) {
         if (attemptTasks(10, "remove old " + current, deleteTask(current), deletionVerifyTask(current))) {
            if (!attemptTasks(10, "replace " + current + " with " + newPath, renameTask(newPath, current), existenceCheckTask(current))) {
               attemptTasks(10, "restore " + current + " from " + backup, renameTask(backup, current), existenceCheckTask(current));
            }

         }
      }
   }

   /**
    * Moves the {@code cursor} in the {@code string} by a {@code delta} amount.
    * Skips surrogate characters.
    */
   @Environment(EnvType.CLIENT)
   public static int moveCursor(String string, int cursor, int delta) {
      int i = string.length();
      int j;
      if (delta >= 0) {
         for(j = 0; cursor < i && j < delta; ++j) {
            if (Character.isHighSurrogate(string.charAt(cursor++)) && cursor < i && Character.isLowSurrogate(string.charAt(cursor))) {
               ++cursor;
            }
         }
      } else {
         for(j = delta; cursor > 0 && j < 0; ++j) {
            --cursor;
            if (Character.isLowSurrogate(string.charAt(cursor)) && cursor > 0 && Character.isHighSurrogate(string.charAt(cursor - 1))) {
               --cursor;
            }
         }
      }

      return cursor;
   }

   public static Consumer<String> method_29188(String string, Consumer<String> consumer) {
      return (string2) -> {
         consumer.accept(string + string2);
      };
   }

   public static DataResult<int[]> toArray(IntStream stream, int length) {
      int[] is = stream.limit((long)(length + 1)).toArray();
      if (is.length != length) {
         String string = "Input is not a list of " + length + " ints";
         return is.length >= length ? DataResult.error(string, (Object)Arrays.copyOf(is, length)) : DataResult.error(string);
      } else {
         return DataResult.success(is);
      }
   }

   public static void startTimerHack() {
      Thread thread = new Thread("Timer hack thread") {
         public void run() {
            while(true) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException var2) {
                  Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                  return;
               }
            }
         }
      };
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
      thread.start();
   }

   /**
    * Copies a file contained in the folder {@code src} to the folder {@code dest}.
    * This will replicate any path structure that may exist between {@code src} and {@code toCopy}.
    */
   @Environment(EnvType.CLIENT)
   public static void relativeCopy(Path src, Path dest, Path toCopy) throws IOException {
      Path path = src.relativize(toCopy);
      Path path2 = dest.resolve(path);
      Files.copy(toCopy, path2);
   }

   @Environment(EnvType.CLIENT)
   public static String replaceInvalidChars(String string, CharPredicate predicate) {
      return (String)string.toLowerCase(Locale.ROOT).chars().mapToObj((i) -> {
         return predicate.test((char)i) ? Character.toString((char)i) : "_";
      }).collect(Collectors.joining());
   }

   static enum IdentityHashStrategy implements Strategy<Object> {
      INSTANCE;

      public int hashCode(Object object) {
         return System.identityHashCode(object);
      }

      public boolean equals(Object object, Object object2) {
         return object == object2;
      }
   }

   public static enum OperatingSystem {
      LINUX,
      SOLARIS,
      WINDOWS {
         @Environment(EnvType.CLIENT)
         protected String[] getURLOpenCommand(URL url) {
            return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
         }
      },
      OSX {
         @Environment(EnvType.CLIENT)
         protected String[] getURLOpenCommand(URL url) {
            return new String[]{"open", url.toString()};
         }
      },
      UNKNOWN;

      private OperatingSystem() {
      }

      @Environment(EnvType.CLIENT)
      public void open(URL url) {
         try {
            Process process = (Process)AccessController.doPrivileged(() -> {
               return Runtime.getRuntime().exec(this.getURLOpenCommand(url));
            });
            Iterator var3 = IOUtils.readLines(process.getErrorStream()).iterator();

            while(var3.hasNext()) {
               String string = (String)var3.next();
               Util.LOGGER.error(string);
            }

            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
         } catch (IOException | PrivilegedActionException var5) {
            Util.LOGGER.error((String)"Couldn't open url '{}'", (Object)url, (Object)var5);
         }

      }

      @Environment(EnvType.CLIENT)
      public void open(URI uRI) {
         try {
            this.open(uRI.toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error((String)"Couldn't open uri '{}'", (Object)uRI, (Object)var3);
         }

      }

      @Environment(EnvType.CLIENT)
      public void open(File file) {
         try {
            this.open(file.toURI().toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error((String)"Couldn't open file '{}'", (Object)file, (Object)var3);
         }

      }

      @Environment(EnvType.CLIENT)
      protected String[] getURLOpenCommand(URL url) {
         String string = url.toString();
         if ("file".equals(url.getProtocol())) {
            string = string.replace("file:", "file://");
         }

         return new String[]{"xdg-open", string};
      }

      @Environment(EnvType.CLIENT)
      public void open(String string) {
         try {
            this.open((new URI(string)).toURL());
         } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
            Util.LOGGER.error((String)"Couldn't open uri '{}'", (Object)string, (Object)var3);
         }

      }
   }
}
