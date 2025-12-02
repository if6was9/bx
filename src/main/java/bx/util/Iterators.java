package bx.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Iterators {

  public static <T> Stream<T> toStream(Iterable<T> it) {

    if (it == null) {
      return Stream.of();
    }
    return StreamSupport.stream(it.spliterator(), false);
  }

  public static <T> Stream<T> toStream(Iterator<T> it) {
    if (it == null) {
      return Stream.of();
    }
    Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED);

    Stream<T> stream = StreamSupport.stream(spliterator, false);

    return stream;
  }
}
