package net.minecraft.state.property;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.math.Direction;

/**
 * Represents a property that has direction values.
 * 
 * <p>See {@link net.minecraft.state.property.Properties} for example
 * usages.
 */
public class DirectionProperty extends EnumProperty<Direction> {
   protected DirectionProperty(String name, Collection<Direction> values) {
      super(name, Direction.class, values);
   }

   /**
    * Creates a direction property with the values allowed by the given
    * filter out of all 6 directions.
    * 
    * @param name the name of the property; see {@linkplain Property#name the note on the
    * name}
    * @param filter the filter which specifies if a value is allowed; required to allow
    * 2 or more values
    */
   public static DirectionProperty of(String name, Predicate<Direction> filter) {
      return of(name, (Collection)Arrays.stream(Direction.values()).filter(filter).collect(Collectors.toList()));
   }

   /**
    * Creates a direction property with the given values.
    * 
    * @param name the name of the property; see {@linkplain Property#name the note on the
    * name}
    * @param values the values the property contains; required to have 2 or more values
    */
   public static DirectionProperty of(String name, Direction... values) {
      return of(name, (Collection)Lists.newArrayList((Object[])values));
   }

   /**
    * Creates a direction property with the given values.
    * 
    * @param name the name of the property; see {@linkplain Property#name the note on the
    * name}
    * @param values the values the property contains; required to have 2 or more values
    */
   public static DirectionProperty of(String name, Collection<Direction> values) {
      return new DirectionProperty(name, values);
   }
}
