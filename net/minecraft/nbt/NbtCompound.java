package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an NBT compound object which holds unordered key-value pairs with distinct case-sensitive string keys.
 */
public class NbtCompound implements NbtElement {
   public static final Codec<NbtCompound> CODEC;
   private static final Logger LOGGER;
   private static final Pattern PATTERN;
   public static final NbtType<NbtCompound> TYPE;
   private final Map<String, NbtElement> entries;

   protected NbtCompound(Map<String, NbtElement> entries) {
      this.entries = entries;
   }

   public NbtCompound() {
      this(Maps.newHashMap());
   }

   public void write(DataOutput output) throws IOException {
      Iterator var2 = this.entries.keySet().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         NbtElement nbtElement = (NbtElement)this.entries.get(string);
         write(string, nbtElement, output);
      }

      output.writeByte(0);
   }

   public Set<String> getKeys() {
      return this.entries.keySet();
   }

   public byte getType() {
      return 10;
   }

   public NbtType<NbtCompound> getNbtType() {
      return TYPE;
   }

   public int getSize() {
      return this.entries.size();
   }

   @Nullable
   public NbtElement put(String key, NbtElement element) {
      return (NbtElement)this.entries.put(key, element);
   }

   public void putByte(String key, byte value) {
      this.entries.put(key, NbtByte.of(value));
   }

   public void putShort(String key, short value) {
      this.entries.put(key, NbtShort.of(value));
   }

   public void putInt(String key, int value) {
      this.entries.put(key, NbtInt.of(value));
   }

   public void putLong(String key, long value) {
      this.entries.put(key, NbtLong.of(value));
   }

   /**
    * Writes a {@link UUID} to its NBT representation in this {@code NbtCompound}.
    */
   public void putUuid(String key, UUID value) {
      this.entries.put(key, NbtHelper.fromUuid(value));
   }

   /**
    * Reads a {@link UUID} from its NBT representation in this {@code NbtCompound}.
    */
   public UUID getUuid(String key) {
      return NbtHelper.toUuid(this.get(key));
   }

   /**
    * Returns {@code true} if this {@code NbtCompound} contains a valid UUID representation associated with the given key.
    * A valid UUID is represented by an int array of length 4.
    */
   public boolean containsUuid(String key) {
      NbtElement nbtElement = this.get(key);
      return nbtElement != null && nbtElement.getNbtType() == NbtIntArray.TYPE && ((NbtIntArray)nbtElement).getIntArray().length == 4;
   }

   public void putFloat(String key, float value) {
      this.entries.put(key, NbtFloat.of(value));
   }

   public void putDouble(String key, double value) {
      this.entries.put(key, NbtDouble.of(value));
   }

   public void putString(String key, String value) {
      this.entries.put(key, NbtString.of(value));
   }

   public void putByteArray(String key, byte[] value) {
      this.entries.put(key, new NbtByteArray(value));
   }

   public void putIntArray(String key, int[] value) {
      this.entries.put(key, new NbtIntArray(value));
   }

   public void putIntArray(String key, List<Integer> value) {
      this.entries.put(key, new NbtIntArray(value));
   }

   public void putLongArray(String key, long[] value) {
      this.entries.put(key, new NbtLongArray(value));
   }

   public void putLongArray(String key, List<Long> value) {
      this.entries.put(key, new NbtLongArray(value));
   }

   public void putBoolean(String key, boolean value) {
      this.entries.put(key, NbtByte.of(value));
   }

   @Nullable
   public NbtElement get(String key) {
      return (NbtElement)this.entries.get(key);
   }

   /**
    * Gets the {@linkplain NbtElement#getType NBT type} of the element stored at the specified key.
    * 
    * @return the element NBT type, or {@code 0} if it does not exist
    */
   public byte getType(String key) {
      NbtElement nbtElement = (NbtElement)this.entries.get(key);
      return nbtElement == null ? 0 : nbtElement.getType();
   }

   /**
    * Determines whether the NBT compound object contains the specified key.
    * 
    * @return {@code true} if the key exists, else {@code false}
    */
   public boolean contains(String key) {
      return this.entries.containsKey(key);
   }

   /**
    * Returns whether the NBT compound object contains an element of the specified type at the specified key.
    * <p>
    * The type restriction can also be {@code 99}, which only allows any type of number.
    * 
    * @return {@code true} if the key exists and the element type is equivalent to the given {@code type}, else {@code false}
    */
   public boolean contains(String key, int type) {
      int i = this.getType(key);
      if (i == type) {
         return true;
      } else if (type != 99) {
         return false;
      } else {
         return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
      }
   }

   public byte getByte(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNbtNumber)this.entries.get(key)).byteValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public short getShort(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNbtNumber)this.entries.get(key)).shortValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public int getInt(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNbtNumber)this.entries.get(key)).intValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0;
   }

   public long getLong(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNbtNumber)this.entries.get(key)).longValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0L;
   }

   public float getFloat(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNbtNumber)this.entries.get(key)).floatValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0F;
   }

   public double getDouble(String key) {
      try {
         if (this.contains(key, 99)) {
            return ((AbstractNbtNumber)this.entries.get(key)).doubleValue();
         }
      } catch (ClassCastException var3) {
      }

      return 0.0D;
   }

   public String getString(String key) {
      try {
         if (this.contains(key, 8)) {
            return ((NbtElement)this.entries.get(key)).asString();
         }
      } catch (ClassCastException var3) {
      }

      return "";
   }

   public byte[] getByteArray(String key) {
      try {
         if (this.contains(key, 7)) {
            return ((NbtByteArray)this.entries.get(key)).getByteArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, NbtByteArray.TYPE, var3));
      }

      return new byte[0];
   }

   public int[] getIntArray(String key) {
      try {
         if (this.contains(key, 11)) {
            return ((NbtIntArray)this.entries.get(key)).getIntArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, NbtIntArray.TYPE, var3));
      }

      return new int[0];
   }

   public long[] getLongArray(String key) {
      try {
         if (this.contains(key, 12)) {
            return ((NbtLongArray)this.entries.get(key)).getLongArray();
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, NbtLongArray.TYPE, var3));
      }

      return new long[0];
   }

   public NbtCompound getCompound(String key) {
      try {
         if (this.contains(key, 10)) {
            return (NbtCompound)this.entries.get(key);
         }
      } catch (ClassCastException var3) {
         throw new CrashException(this.createCrashReport(key, TYPE, var3));
      }

      return new NbtCompound();
   }

   public NbtList getList(String key, int type) {
      try {
         if (this.getType(key) == 9) {
            NbtList nbtList = (NbtList)this.entries.get(key);
            if (!nbtList.isEmpty() && nbtList.getHeldType() != type) {
               return new NbtList();
            }

            return nbtList;
         }
      } catch (ClassCastException var4) {
         throw new CrashException(this.createCrashReport(key, NbtList.TYPE, var4));
      }

      return new NbtList();
   }

   public boolean getBoolean(String key) {
      return this.getByte(key) != 0;
   }

   public void remove(String key) {
      this.entries.remove(key);
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder("{");
      Collection<String> collection = this.entries.keySet();
      if (LOGGER.isDebugEnabled()) {
         List<String> list = Lists.newArrayList((Iterable)this.entries.keySet());
         Collections.sort(list);
         collection = list;
      }

      String string;
      for(Iterator var5 = ((Collection)collection).iterator(); var5.hasNext(); stringBuilder.append(escapeTagKey(string)).append(':').append(this.entries.get(string))) {
         string = (String)var5.next();
         if (stringBuilder.length() != 1) {
            stringBuilder.append(',');
         }
      }

      return stringBuilder.append('}').toString();
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   private CrashReport createCrashReport(String key, NbtType<?> reader, ClassCastException classCastException) {
      CrashReport crashReport = CrashReport.create(classCastException, "Reading NBT data");
      CrashReportSection crashReportSection = crashReport.addElement("Corrupt NBT tag", 1);
      crashReportSection.add("Tag type found", () -> {
         return ((NbtElement)this.entries.get(key)).getNbtType().getCrashReportName();
      });
      crashReportSection.add("Tag type expected", reader::getCrashReportName);
      crashReportSection.add("Tag name", (Object)key);
      return crashReport;
   }

   public NbtCompound copy() {
      Map<String, NbtElement> map = Maps.newHashMap(Maps.transformValues(this.entries, NbtElement::copy));
      return new NbtCompound(map);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtCompound && Objects.equals(this.entries, ((NbtCompound)o).entries);
      }
   }

   public int hashCode() {
      return this.entries.hashCode();
   }

   private static void write(String key, NbtElement element, DataOutput output) throws IOException {
      output.writeByte(element.getType());
      if (element.getType() != 0) {
         output.writeUTF(key);
         element.write(output);
      }
   }

   private static byte readByte(DataInput input, NbtTagSizeTracker tracker) throws IOException {
      return input.readByte();
   }

   private static String readString(DataInput input, NbtTagSizeTracker tracker) throws IOException {
      return input.readUTF();
   }

   private static NbtElement read(NbtType<?> reader, String key, DataInput input, int depth, NbtTagSizeTracker tracker) {
      try {
         return reader.read(input, depth, tracker);
      } catch (IOException var8) {
         CrashReport crashReport = CrashReport.create(var8, "Loading NBT data");
         CrashReportSection crashReportSection = crashReport.addElement("NBT Tag");
         crashReportSection.add("Tag name", (Object)key);
         crashReportSection.add("Tag type", (Object)reader.getCrashReportName());
         throw new CrashException(crashReport);
      }
   }

   public NbtCompound copyFrom(NbtCompound source) {
      Iterator var2 = source.entries.keySet().iterator();

      while(var2.hasNext()) {
         String string = (String)var2.next();
         NbtElement nbtElement = (NbtElement)source.entries.get(string);
         if (nbtElement.getType() == 10) {
            if (this.contains(string, 10)) {
               NbtCompound nbtCompound = this.getCompound(string);
               nbtCompound.copyFrom((NbtCompound)nbtElement);
            } else {
               this.put(string, nbtElement.copy());
            }
         } else {
            this.put(string, nbtElement.copy());
         }
      }

      return this;
   }

   protected static String escapeTagKey(String key) {
      return PATTERN.matcher(key).matches() ? key : NbtString.escape(key);
   }

   protected static Text prettyPrintTagKey(String key) {
      if (PATTERN.matcher(key).matches()) {
         return (new LiteralText(key)).formatted(AQUA);
      } else {
         String string = NbtString.escape(key);
         String string2 = string.substring(0, 1);
         Text text = (new LiteralText(string.substring(1, string.length() - 1))).formatted(AQUA);
         return (new LiteralText(string2)).append(text).append(string2);
      }
   }

   public Text toText(String indent, int depth) {
      if (this.entries.isEmpty()) {
         return new LiteralText("{}");
      } else {
         MutableText mutableText = new LiteralText("{");
         Collection<String> collection = this.entries.keySet();
         if (LOGGER.isDebugEnabled()) {
            List<String> list = Lists.newArrayList((Iterable)this.entries.keySet());
            Collections.sort(list);
            collection = list;
         }

         if (!indent.isEmpty()) {
            mutableText.append("\n");
         }

         MutableText mutableText2;
         for(Iterator iterator = ((Collection)collection).iterator(); iterator.hasNext(); mutableText.append((Text)mutableText2)) {
            String string = (String)iterator.next();
            mutableText2 = (new LiteralText(Strings.repeat(indent, depth + 1))).append(prettyPrintTagKey(string)).append(String.valueOf(':')).append(" ").append(((NbtElement)this.entries.get(string)).toText(indent, depth + 1));
            if (iterator.hasNext()) {
               mutableText2.append(String.valueOf(',')).append(indent.isEmpty() ? " " : "\n");
            }
         }

         if (!indent.isEmpty()) {
            mutableText.append("\n").append(Strings.repeat(indent, depth));
         }

         mutableText.append("}");
         return mutableText;
      }
   }

   protected Map<String, NbtElement> toMap() {
      return Collections.unmodifiableMap(this.entries);
   }

   static {
      CODEC = Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
         NbtElement nbtElement = (NbtElement)dynamic.convert(NbtOps.INSTANCE).getValue();
         return nbtElement instanceof NbtCompound ? DataResult.success((NbtCompound)nbtElement) : DataResult.error("Not a compound tag: " + nbtElement);
      }, (nbtCompound) -> {
         return new Dynamic(NbtOps.INSTANCE, nbtCompound);
      });
      LOGGER = LogManager.getLogger();
      PATTERN = Pattern.compile("[A-Za-z0-9._+-]+");
      TYPE = new NbtType<NbtCompound>() {
         public NbtCompound read(DataInput dataInput, int i, NbtTagSizeTracker nbtTagSizeTracker) throws IOException {
            nbtTagSizeTracker.add(384L);
            if (i > 512) {
               throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
               HashMap map = Maps.newHashMap();

               byte b;
               while((b = NbtCompound.readByte(dataInput, nbtTagSizeTracker)) != 0) {
                  String string = NbtCompound.readString(dataInput, nbtTagSizeTracker);
                  nbtTagSizeTracker.add((long)(224 + 16 * string.length()));
                  NbtElement nbtElement = NbtCompound.read(NbtTypes.byId(b), string, dataInput, i + 1, nbtTagSizeTracker);
                  if (map.put(string, nbtElement) != null) {
                     nbtTagSizeTracker.add(288L);
                  }
               }

               return new NbtCompound(map);
            }
         }

         public String getCrashReportName() {
            return "COMPOUND";
         }

         public String getCommandFeedbackName() {
            return "TAG_Compound";
         }
      };
   }
}
