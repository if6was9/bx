package bx.sql;

import bx.sql.duckdb.DuckCsv;
import bx.sql.mapper.Mappers;
import bx.util.BxTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class ObjectNodeRowMapperTest extends BxTest {

  @Test
  public void testIt() {

    String csv =
        """
        id,name,dob,null_col
        1,Homer,2017-07-28,
        2,Rosie,2023-02-28,""
        """;

    var table = DuckCsv.using(dataSource()).fromString(csv).load();

    var list =
        JdbcClient.create(dataSource())
            .sql("select * from " + table.getName())
            .query(Mappers.jsonObjectMapper())
            .list();

    table.describe();
    table.show();
    list.forEach(
        it -> {
          System.out.println(it.toPrettyString());
        });

    list.forEach(
        it -> {
          Assertions.assertThat(it.path("null_col").isNull()).isTrue();
        });
    Assertions.assertThat(list.get(0).path("name").asString("")).isEqualTo("Homer");
    Assertions.assertThat(list.get(0).path("id").isLong()).isTrue();
    Assertions.assertThat(list.get(1).path("name").asString("")).isEqualTo("Rosie");
    Assertions.assertThat(list.get(1).path("id").isLong()).isTrue();
  }
}
