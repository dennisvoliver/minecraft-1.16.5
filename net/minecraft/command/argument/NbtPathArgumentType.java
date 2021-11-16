package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgumentType implements ArgumentType<NbtPathArgumentType.NbtPath> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
   public static final SimpleCommandExceptionType INVALID_PATH_NODE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("arguments.nbtpath.node.invalid"));
   public static final DynamicCommandExceptionType NOTHING_FOUND_EXCEPTION = new DynamicCommandExceptionType((object) -> {
      return new TranslatableText("arguments.nbtpath.nothing_found", new Object[]{object});
   });

   public static NbtPathArgumentType nbtPath() {
      return new NbtPathArgumentType();
   }

   public static NbtPathArgumentType.NbtPath getNbtPath(CommandContext<ServerCommandSource> context, String name) {
      return (NbtPathArgumentType.NbtPath)context.getArgument(name, NbtPathArgumentType.NbtPath.class);
   }

   public NbtPathArgumentType.NbtPath parse(StringReader stringReader) throws CommandSyntaxException {
      List<NbtPathArgumentType.PathNode> list = Lists.newArrayList();
      int i = stringReader.getCursor();
      Object2IntMap<NbtPathArgumentType.PathNode> object2IntMap = new Object2IntOpenHashMap();
      boolean bl = true;

      while(stringReader.canRead() && stringReader.peek() != ' ') {
         NbtPathArgumentType.PathNode pathNode = parseNode(stringReader, bl);
         list.add(pathNode);
         object2IntMap.put(pathNode, stringReader.getCursor() - i);
         bl = false;
         if (stringReader.canRead()) {
            char c = stringReader.peek();
            if (c != ' ' && c != '[' && c != '{') {
               stringReader.expect('.');
            }
         }
      }

      return new NbtPathArgumentType.NbtPath(stringReader.getString().substring(i, stringReader.getCursor()), (NbtPathArgumentType.PathNode[])list.toArray(new NbtPathArgumentType.PathNode[0]), object2IntMap);
   }

   private static NbtPathArgumentType.PathNode parseNode(StringReader reader, boolean root) throws CommandSyntaxException {
      String string;
      switch(reader.peek()) {
      case '"':
         string = reader.readString();
         return readCompoundChildNode(reader, string);
      case '[':
         reader.skip();
         int i = reader.peek();
         if (i == '{') {
            NbtCompound nbtCompound2 = (new StringNbtReader(reader)).parseCompound();
            reader.expect(']');
            return new NbtPathArgumentType.FilteredListElementNode(nbtCompound2);
         } else {
            if (i == ']') {
               reader.skip();
               return NbtPathArgumentType.AllListElementNode.INSTANCE;
            }

            int j = reader.readInt();
            reader.expect(']');
            return new NbtPathArgumentType.IndexedListElementNode(j);
         }
      case '{':
         if (!root) {
            throw INVALID_PATH_NODE_EXCEPTION.createWithContext(reader);
         }

         NbtCompound nbtCompound = (new StringNbtReader(reader)).parseCompound();
         return new NbtPathArgumentType.FilteredRootNode(nbtCompound);
      default:
         string = readName(reader);
         return readCompoundChildNode(reader, string);
      }
   }

   private static NbtPathArgumentType.PathNode readCompoundChildNode(StringReader reader, String name) throws CommandSyntaxException {
      if (reader.canRead() && reader.peek() == '{') {
         NbtCompound nbtCompound = (new StringNbtReader(reader)).parseCompound();
         return new NbtPathArgumentType.FilteredNamedNode(name, nbtCompound);
      } else {
         return new NbtPathArgumentType.NamedNode(name);
      }
   }

   private static String readName(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();

      while(reader.canRead() && isNameCharacter(reader.peek())) {
         reader.skip();
      }

      if (reader.getCursor() == i) {
         throw INVALID_PATH_NODE_EXCEPTION.createWithContext(reader);
      } else {
         return reader.getString().substring(i, reader.getCursor());
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static boolean isNameCharacter(char c) {
      return c != ' ' && c != '"' && c != '[' && c != ']' && c != '.' && c != '{' && c != '}';
   }

   private static Predicate<NbtElement> getPredicate(NbtCompound filter) {
      return (nbtElement) -> {
         return NbtHelper.matches(filter, nbtElement, true);
      };
   }

   static class FilteredRootNode implements NbtPathArgumentType.PathNode {
      private final Predicate<NbtElement> matcher;

      public FilteredRootNode(NbtCompound filter) {
         this.matcher = NbtPathArgumentType.getPredicate(filter);
      }

      public void get(NbtElement current, List<NbtElement> results) {
         if (current instanceof NbtCompound && this.matcher.test(current)) {
            results.add(current);
         }

      }

      public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
         this.get(current, results);
      }

      public NbtElement init() {
         return new NbtCompound();
      }

      public int set(NbtElement current, Supplier<NbtElement> source) {
         return 0;
      }

      public int clear(NbtElement current) {
         return 0;
      }
   }

   static class FilteredNamedNode implements NbtPathArgumentType.PathNode {
      private final String name;
      private final NbtCompound filter;
      private final Predicate<NbtElement> predicate;

      public FilteredNamedNode(String name, NbtCompound filter) {
         this.name = name;
         this.filter = filter;
         this.predicate = NbtPathArgumentType.getPredicate(filter);
      }

      public void get(NbtElement current, List<NbtElement> results) {
         if (current instanceof NbtCompound) {
            NbtElement nbtElement = ((NbtCompound)current).get(this.name);
            if (this.predicate.test(nbtElement)) {
               results.add(nbtElement);
            }
         }

      }

      public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
         if (current instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)current;
            NbtElement nbtElement = nbtCompound.get(this.name);
            if (nbtElement == null) {
               NbtElement nbtElement = this.filter.copy();
               nbtCompound.put(this.name, nbtElement);
               results.add(nbtElement);
            } else if (this.predicate.test(nbtElement)) {
               results.add(nbtElement);
            }
         }

      }

      public NbtElement init() {
         return new NbtCompound();
      }

      public int set(NbtElement current, Supplier<NbtElement> source) {
         if (current instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)current;
            NbtElement nbtElement = nbtCompound.get(this.name);
            if (this.predicate.test(nbtElement)) {
               NbtElement nbtElement2 = (NbtElement)source.get();
               if (!nbtElement2.equals(nbtElement)) {
                  nbtCompound.put(this.name, nbtElement2);
                  return 1;
               }
            }
         }

         return 0;
      }

      public int clear(NbtElement current) {
         if (current instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)current;
            NbtElement nbtElement = nbtCompound.get(this.name);
            if (this.predicate.test(nbtElement)) {
               nbtCompound.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class AllListElementNode implements NbtPathArgumentType.PathNode {
      public static final NbtPathArgumentType.AllListElementNode INSTANCE = new NbtPathArgumentType.AllListElementNode();

      private AllListElementNode() {
      }

      public void get(NbtElement current, List<NbtElement> results) {
         if (current instanceof AbstractNbtList) {
            results.addAll((AbstractNbtList)current);
         }

      }

      public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
         if (current instanceof AbstractNbtList) {
            AbstractNbtList<?> abstractNbtList = (AbstractNbtList)current;
            if (abstractNbtList.isEmpty()) {
               NbtElement nbtElement = (NbtElement)source.get();
               if (abstractNbtList.addElement(0, nbtElement)) {
                  results.add(nbtElement);
               }
            } else {
               results.addAll(abstractNbtList);
            }
         }

      }

      public NbtElement init() {
         return new NbtList();
      }

      public int set(NbtElement current, Supplier<NbtElement> source) {
         if (!(current instanceof AbstractNbtList)) {
            return 0;
         } else {
            AbstractNbtList<?> abstractNbtList = (AbstractNbtList)current;
            int i = abstractNbtList.size();
            if (i == 0) {
               abstractNbtList.addElement(0, (NbtElement)source.get());
               return 1;
            } else {
               NbtElement nbtElement = (NbtElement)source.get();
               Stream var10001 = abstractNbtList.stream();
               nbtElement.getClass();
               int j = i - (int)var10001.filter(nbtElement::equals).count();
               if (j == 0) {
                  return 0;
               } else {
                  abstractNbtList.clear();
                  if (!abstractNbtList.addElement(0, nbtElement)) {
                     return 0;
                  } else {
                     for(int k = 1; k < i; ++k) {
                        abstractNbtList.addElement(k, (NbtElement)source.get());
                     }

                     return j;
                  }
               }
            }
         }
      }

      public int clear(NbtElement current) {
         if (current instanceof AbstractNbtList) {
            AbstractNbtList<?> abstractNbtList = (AbstractNbtList)current;
            int i = abstractNbtList.size();
            if (i > 0) {
               abstractNbtList.clear();
               return i;
            }
         }

         return 0;
      }
   }

   static class FilteredListElementNode implements NbtPathArgumentType.PathNode {
      private final NbtCompound filter;
      private final Predicate<NbtElement> predicate;

      public FilteredListElementNode(NbtCompound filter) {
         this.filter = filter;
         this.predicate = NbtPathArgumentType.getPredicate(filter);
      }

      public void get(NbtElement current, List<NbtElement> results) {
         if (current instanceof NbtList) {
            NbtList nbtList = (NbtList)current;
            nbtList.stream().filter(this.predicate).forEach(results::add);
         }

      }

      public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
         MutableBoolean mutableBoolean = new MutableBoolean();
         if (current instanceof NbtList) {
            NbtList nbtList = (NbtList)current;
            nbtList.stream().filter(this.predicate).forEach((nbtElement) -> {
               results.add(nbtElement);
               mutableBoolean.setTrue();
            });
            if (mutableBoolean.isFalse()) {
               NbtCompound nbtCompound = this.filter.copy();
               nbtList.add(nbtCompound);
               results.add(nbtCompound);
            }
         }

      }

      public NbtElement init() {
         return new NbtList();
      }

      public int set(NbtElement current, Supplier<NbtElement> source) {
         int i = 0;
         if (current instanceof NbtList) {
            NbtList nbtList = (NbtList)current;
            int j = nbtList.size();
            if (j == 0) {
               nbtList.add(source.get());
               ++i;
            } else {
               for(int k = 0; k < j; ++k) {
                  NbtElement nbtElement = nbtList.get(k);
                  if (this.predicate.test(nbtElement)) {
                     NbtElement nbtElement2 = (NbtElement)source.get();
                     if (!nbtElement2.equals(nbtElement) && nbtList.setElement(k, nbtElement2)) {
                        ++i;
                     }
                  }
               }
            }
         }

         return i;
      }

      public int clear(NbtElement current) {
         int i = 0;
         if (current instanceof NbtList) {
            NbtList nbtList = (NbtList)current;

            for(int j = nbtList.size() - 1; j >= 0; --j) {
               if (this.predicate.test(nbtList.get(j))) {
                  nbtList.remove(j);
                  ++i;
               }
            }
         }

         return i;
      }
   }

   static class IndexedListElementNode implements NbtPathArgumentType.PathNode {
      private final int index;

      public IndexedListElementNode(int index) {
         this.index = index;
      }

      public void get(NbtElement current, List<NbtElement> results) {
         if (current instanceof AbstractNbtList) {
            AbstractNbtList<?> abstractNbtList = (AbstractNbtList)current;
            int i = abstractNbtList.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               results.add(abstractNbtList.get(j));
            }
         }

      }

      public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
         this.get(current, results);
      }

      public NbtElement init() {
         return new NbtList();
      }

      public int set(NbtElement current, Supplier<NbtElement> source) {
         if (current instanceof AbstractNbtList) {
            AbstractNbtList<?> abstractNbtList = (AbstractNbtList)current;
            int i = abstractNbtList.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               NbtElement nbtElement = (NbtElement)abstractNbtList.get(j);
               NbtElement nbtElement2 = (NbtElement)source.get();
               if (!nbtElement2.equals(nbtElement) && abstractNbtList.setElement(j, nbtElement2)) {
                  return 1;
               }
            }
         }

         return 0;
      }

      public int clear(NbtElement current) {
         if (current instanceof AbstractNbtList) {
            AbstractNbtList<?> abstractNbtList = (AbstractNbtList)current;
            int i = abstractNbtList.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               abstractNbtList.remove(j);
               return 1;
            }
         }

         return 0;
      }
   }

   static class NamedNode implements NbtPathArgumentType.PathNode {
      private final String name;

      public NamedNode(String name) {
         this.name = name;
      }

      public void get(NbtElement current, List<NbtElement> results) {
         if (current instanceof NbtCompound) {
            NbtElement nbtElement = ((NbtCompound)current).get(this.name);
            if (nbtElement != null) {
               results.add(nbtElement);
            }
         }

      }

      public void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results) {
         if (current instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)current;
            NbtElement nbtElement2;
            if (nbtCompound.contains(this.name)) {
               nbtElement2 = nbtCompound.get(this.name);
            } else {
               nbtElement2 = (NbtElement)source.get();
               nbtCompound.put(this.name, nbtElement2);
            }

            results.add(nbtElement2);
         }

      }

      public NbtElement init() {
         return new NbtCompound();
      }

      public int set(NbtElement current, Supplier<NbtElement> source) {
         if (current instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)current;
            NbtElement nbtElement = (NbtElement)source.get();
            NbtElement nbtElement2 = nbtCompound.put(this.name, nbtElement);
            if (!nbtElement.equals(nbtElement2)) {
               return 1;
            }
         }

         return 0;
      }

      public int clear(NbtElement current) {
         if (current instanceof NbtCompound) {
            NbtCompound nbtCompound = (NbtCompound)current;
            if (nbtCompound.contains(this.name)) {
               nbtCompound.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   interface PathNode {
      void get(NbtElement current, List<NbtElement> results);

      void getOrInit(NbtElement current, Supplier<NbtElement> source, List<NbtElement> results);

      NbtElement init();

      int set(NbtElement current, Supplier<NbtElement> source);

      int clear(NbtElement current);

      default List<NbtElement> get(List<NbtElement> tags) {
         return this.process(tags, this::get);
      }

      default List<NbtElement> getOrInit(List<NbtElement> tags, Supplier<NbtElement> supplier) {
         return this.process(tags, (current, results) -> {
            this.getOrInit(current, supplier, results);
         });
      }

      default List<NbtElement> process(List<NbtElement> tags, BiConsumer<NbtElement, List<NbtElement>> action) {
         List<NbtElement> list = Lists.newArrayList();
         Iterator var4 = tags.iterator();

         while(var4.hasNext()) {
            NbtElement nbtElement = (NbtElement)var4.next();
            action.accept(nbtElement, list);
         }

         return list;
      }
   }

   public static class NbtPath {
      private final String string;
      private final Object2IntMap<NbtPathArgumentType.PathNode> nodeEndIndices;
      private final NbtPathArgumentType.PathNode[] nodes;

      public NbtPath(String string, NbtPathArgumentType.PathNode[] nodes, Object2IntMap<NbtPathArgumentType.PathNode> nodeEndIndices) {
         this.string = string;
         this.nodes = nodes;
         this.nodeEndIndices = nodeEndIndices;
      }

      public List<NbtElement> get(NbtElement tag) throws CommandSyntaxException {
         List<NbtElement> list = Collections.singletonList(tag);
         NbtPathArgumentType.PathNode[] var3 = this.nodes;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            NbtPathArgumentType.PathNode pathNode = var3[var5];
            list = pathNode.get(list);
            if (list.isEmpty()) {
               throw this.createNothingFoundException(pathNode);
            }
         }

         return list;
      }

      public int count(NbtElement tag) {
         List<NbtElement> list = Collections.singletonList(tag);
         NbtPathArgumentType.PathNode[] var3 = this.nodes;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            NbtPathArgumentType.PathNode pathNode = var3[var5];
            list = pathNode.get(list);
            if (list.isEmpty()) {
               return 0;
            }
         }

         return list.size();
      }

      private List<NbtElement> getTerminals(NbtElement start) throws CommandSyntaxException {
         List<NbtElement> list = Collections.singletonList(start);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            NbtPathArgumentType.PathNode pathNode = this.nodes[i];
            int j = i + 1;
            NbtPathArgumentType.PathNode var10002 = this.nodes[j];
            var10002.getClass();
            list = pathNode.getOrInit(list, var10002::init);
            if (list.isEmpty()) {
               throw this.createNothingFoundException(pathNode);
            }
         }

         return list;
      }

      public List<NbtElement> getOrInit(NbtElement tag, Supplier<NbtElement> source) throws CommandSyntaxException {
         List<NbtElement> list = this.getTerminals(tag);
         NbtPathArgumentType.PathNode pathNode = this.nodes[this.nodes.length - 1];
         return pathNode.getOrInit(list, source);
      }

      private static int forEach(List<NbtElement> tags, Function<NbtElement, Integer> operation) {
         return (Integer)tags.stream().map(operation).reduce(0, (integer, integer2) -> {
            return integer + integer2;
         });
      }

      public int put(NbtElement tag, Supplier<NbtElement> source) throws CommandSyntaxException {
         List<NbtElement> list = this.getTerminals(tag);
         NbtPathArgumentType.PathNode pathNode = this.nodes[this.nodes.length - 1];
         return forEach(list, (nbtElement) -> {
            return pathNode.set(nbtElement, source);
         });
      }

      public int remove(NbtElement tag) {
         List<NbtElement> list = Collections.singletonList(tag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            list = this.nodes[i].get(list);
         }

         NbtPathArgumentType.PathNode pathNode = this.nodes[this.nodes.length - 1];
         pathNode.getClass();
         return forEach(list, pathNode::clear);
      }

      private CommandSyntaxException createNothingFoundException(NbtPathArgumentType.PathNode node) {
         int i = this.nodeEndIndices.getInt(node);
         return NbtPathArgumentType.NOTHING_FOUND_EXCEPTION.create(this.string.substring(0, i));
      }

      public String toString() {
         return this.string;
      }
   }
}
