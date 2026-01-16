package bx.sql;

import bx.util.BxTest;
import bx.util.Classes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlUtilTest extends BxTest {

  @Test
  public void testFindEnclosingClass() {

    Assertions.assertThat(Classes.findEnclosingClassName().orElse(""))
        .isEqualTo(this.getClass().getName());
  }
}
