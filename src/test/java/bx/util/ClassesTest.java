package bx.util;

import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassesTest {

  abstract class TestAbstractClass {}

  @Test
  public void testIsAbstract() {
    Assertions.assertThat(Classes.isAbstract(getClass())).isFalse();
    Assertions.assertThat(Classes.isAbstract(java.sql.Connection.class)).isTrue();
    Assertions.assertThat(Classes.isAbstract(TestAbstractClass.class)).isTrue();
  }

  @Test
  public void testIt() {

    Assertions.assertThat(Classes.findEnclosingClassName().get())
        .isEqualTo(ClassesTest.class.getName());

    Supplier<String> s =
        () -> {
          return Classes.findEnclosingClassName().orElse(null);
        };

    Assertions.assertThat(s.get()).isEqualTo(ClassesTest.class.getName());
  }
}
