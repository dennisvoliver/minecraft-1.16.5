package net.minecraft.tag;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SetTag<T> implements Tag<T> {
   private final ImmutableList<T> valueList;
   private final Set<T> valueSet;
   @VisibleForTesting
   protected final Class<?> type;

   protected SetTag(Set<T> values, Class<?> type) {
      this.type = type;
      this.valueSet = values;
      this.valueList = ImmutableList.copyOf((Collection)values);
   }

   public static <T> SetTag<T> empty() {
      return new SetTag(ImmutableSet.of(), Void.class);
   }

   public static <T> SetTag<T> of(Set<T> values) {
      return new SetTag(values, getCommonType(values));
   }

   public boolean contains(T entry) {
      return this.type.isInstance(entry) && this.valueSet.contains(entry);
   }

   public List<T> values() {
      return this.valueList;
   }

   private static <T> Class<?> getCommonType(Set<T> values) {
      if (values.isEmpty()) {
         return Void.class;
      } else {
         Class<?> class_ = null;
         Iterator var2 = values.iterator();

         while(var2.hasNext()) {
            T object = var2.next();
            if (class_ == null) {
               class_ = object.getClass();
            } else {
               class_ = getCommonType(class_, object.getClass());
            }
         }

         return class_;
      }
   }

   private static Class<?> getCommonType(Class<?> first, Class<?> second) {
      while(!first.isAssignableFrom(second)) {
         first = first.getSuperclass();
      }

      return first;
   }
}
