package bx.util;

import com.google.common.collect.Lists;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeferTest {

  @Test
  public void testDefaultSwallowSetting() {
    Assertions.assertThat(Defer.create().isSwallowEnabled()).isFalse();
  }

  @Test
  public void testNoSwallow() {

    List<String> order = Lists.newArrayList();

    Exception thrownException = null;
    try (Defer defer = Defer.create().withSwallow(false)) {
      Closeable a =
          new Closeable() {

            @Override
            public void close() throws IOException {
              order.add("a");
              throw new IllegalStateException();
            }
          };

      defer.register(a);

      Closeable b =
          new Closeable() {

            @Override
            public void close() throws IOException {

              order.add("b");
              throw new IllegalStateException();
            }
          };
      defer.register(b);

    } catch (IllegalStateException e) {
      thrownException = e;
    }
    Assertions.assertThat(thrownException).isNotNull();
    Assertions.assertThat(order).containsExactly("b", "a");
  }

  @Test
  public void testSwallow() {

    List<String> order = Lists.newArrayList();

    try (Defer defer = Defer.create().withSwallow(true)) {
      Closeable a =
          new Closeable() {

            @Override
            public void close() throws IOException {

              order.add("a");
              throw new IllegalStateException();
            }
          };

      defer.register(a);

      Closeable b =
          new Closeable() {

            @Override
            public void close() throws IOException {

              order.add("b");
              throw new IllegalStateException();
            }
          };
      defer.register(b);
    }
    Assertions.assertThat(order).containsExactly("b", "a");
  }
}
