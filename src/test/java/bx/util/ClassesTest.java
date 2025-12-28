package bx.util;

import java.util.function.Supplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class ClassesTest {

  abstract class TestAbstractClass {}

  @Test
  public void testIsAbstract() {
    Assertions.assertThat(Classes.isAbstract(getClass())).isFalse();
    Assertions.assertThat(Classes.isAbstract(java.sql.Connection.class)).isTrue();
    Assertions.assertThat(Classes.isAbstract(TestAbstractClass.class)).isTrue();
  }

  class MyInner {
	  
	  Logger logger = Slogger.forEnclosingClass();
	  String foo() {
		  logger.atInfo().log("foo");
		  return Classes.findEnclosingClassName().orElse("NOT FOUND");
	  }
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
    
    Assertions.assertThat(new MyInner().foo()).isEqualTo("bx.util.ClassesTest$MyInner");
  }
}
