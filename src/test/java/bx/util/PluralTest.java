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
    Assertions.assertThat(Plural.toPlural("Piano")).isEqualTo("Pianos");
    Assertions.assertThat(Plural.toPlural("doggie")).isEqualTo("doggies");
    Assertions.assertThat(Plural.toPlural("baby")).isEqualTo("babies");

    Assertions.assertThat(Plural.toPlural("axis")).isEqualTo("axes");
    Assertions.assertThat(Plural.toPlural("Axis")).isEqualTo("Axes");
    Assertions.assertThat(Plural.toPlural("CPU")).isEqualTo("CPUs");
    Assertions.assertThat(Plural.toPlural("")).isEqualTo("");
    Assertions.assertThat(Plural.toPlural(null)).isEqualTo("");
  }

  @Test
  public void testToCount() {
    Assertions.assertThat(Plural.toCount(0, "row")).isEqualTo("0 rows");
  }

  @Test
  public void testRecase() {
    Assertions.assertThat(Plural.recase("row", "row")).isEqualTo("row");
    Assertions.assertThat(Plural.recase("rows", "row")).isEqualTo("rows");
    Assertions.assertThat(Plural.recase("roofs", "FOOF")).isEqualTo("roofs");
    Assertions.assertThat(Plural.recase("roofs", "Roof")).isEqualTo("Roofs");
    Assertions.assertThat(Plural.recase("roofs", "ROOFSX")).isEqualTo("ROOFS");
    Assertions.assertThat(Plural.recase("roofs", "")).isEqualTo("roofs");
    Assertions.assertThat(Plural.recase("roofs", null)).isEqualTo("roofs");
  }
}
