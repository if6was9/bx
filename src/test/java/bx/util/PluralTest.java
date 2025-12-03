package bx.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PluralTest {

  @Test
  public void testIt() {

    Assertions.assertThat(Plural.toPlural("kiss")).isEqualTo("kisses");
    Assertions.assertThat(Plural.toPlural("miss")).isEqualTo("misses");
    Assertions.assertThat(Plural.toPlural("batch")).isEqualTo("batches");
    Assertions.assertThat(Plural.toPlural("fairy")).isEqualTo("fairies");
    Assertions.assertThat(Plural.toPlural("dairy")).isEqualTo("dairies");
    Assertions.assertThat(Plural.toPlural("leaf")).isEqualTo("leaves");
    Assertions.assertThat(Plural.toPlural("wife")).isEqualTo("wives");
    Assertions.assertThat(Plural.toPlural("roof")).isEqualTo("roofs");
    Assertions.assertThat(Plural.toPlural("potato")).isEqualTo("potatoes");
    Assertions.assertThat(Plural.toPlural("photo")).isEqualTo("photos");
    Assertions.assertThat(Plural.toPlural("piano")).isEqualTo("pianos");
    Assertions.assertThat(Plural.toPlural("axis")).isEqualTo("axes");
    Assertions.assertThat(Plural.toPlural("CPU")).isEqualTo("CPUs");
  }
}
