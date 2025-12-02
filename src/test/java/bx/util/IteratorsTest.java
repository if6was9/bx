package bx.util;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class IteratorsTest {

  @Test
  public void testIteratorToStream() {

    List<String> src = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      src.add(Integer.toString(i));
    }

    List<String> copy = Iterators.toStream(src.iterator()).toList();

    Assertions.assertThat(copy).containsExactlyElementsOf(src);

    Assertions.assertThat(Iterators.toStream(src).toList()).containsExactlyElementsOf(src);
  }

  @Test
  public void testIterableToStream() {

    List<String> src = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      src.add(Integer.toString(i));
    }

    List<String> copy = Iterators.toStream(src).toList();
  }
}
