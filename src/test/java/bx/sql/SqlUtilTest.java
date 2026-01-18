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

  @Test
  public void testInterpolate() {
    Assertions.assertThat(SqlUtil.interpolateTable("create table {{table}} (abc int)", "foo"))
        .isEqualTo("create table foo (abc int)");
  }

  @Test
  public void generateFromFragment() {
    Assertions.assertThat(SqlUtil.generateSqlFromFragment(null, "foo", SqlOperation.SELECT))
        .isEqualTo("SELECT * FROM foo");
    Assertions.assertThat(SqlUtil.generateSqlFromFragment("", "foo", SqlOperation.SELECT))
        .isEqualTo("SELECT * FROM foo");
    Assertions.assertThat(
            SqlUtil.generateSqlFromFragment(" where a=b ", "foo", SqlOperation.SELECT))
        .isEqualTo("SELECT * FROM foo where a=b");
    Assertions.assertThat(SqlUtil.generateSqlFromFragment("where a=b ", "foo", SqlOperation.SELECT))
        .isEqualTo("SELECT * FROM foo where a=b");
    Assertions.assertThat(
            SqlUtil.generateSqlFromFragment(
                "select * from {{table}} where a=b", "foo", SqlOperation.SELECT))
        .isEqualTo("select * from foo where a=b");

    Assertions.assertThat(SqlUtil.generateSqlFromFragment(null, "foo", SqlOperation.DELETE))
        .isEqualTo("DELETE FROM foo");
    Assertions.assertThat(SqlUtil.generateSqlFromFragment("", "foo", SqlOperation.DELETE))
        .isEqualTo("DELETE FROM foo");
    Assertions.assertThat(SqlUtil.generateSqlFromFragment("where a=b", "foo", SqlOperation.DELETE))
        .isEqualTo("DELETE FROM foo where a=b");
    Assertions.assertThat(
            SqlUtil.generateSqlFromFragment(" where a=b ", "foo", SqlOperation.DELETE))
        .isEqualTo("DELETE FROM foo where a=b");
    Assertions.assertThat(
            SqlUtil.generateSqlFromFragment(
                "DELETE FROM {{table}} where a=b ", "foo", SqlOperation.DELETE))
        .isEqualTo("DELETE FROM foo where a=b");
  }
}
