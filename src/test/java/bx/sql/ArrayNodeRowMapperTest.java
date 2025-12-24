package bx.sql;

import bx.sql.duckdb.DuckCsv;
import bx.sql.mapper.Mappers;
import bx.util.BxTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class ArrayNodeRowMapperTest extends BxTest {

  @Test
  public void testArray() {

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
            .query(Mappers.jsonArrayMapper())
            .list();

    table.describe();
    table.show();
    list.forEach(
        it -> {
          System.out.println(it.toPrettyString());
        });

    Assertions.assertThat(list.get(0).get(0).asLong()).isEqualTo(1);
    Assertions.assertThat(list.get(1).get(0).asLong()).isEqualTo(2);

    Assertions.assertThat(list.get(0).get(1).asString()).isEqualTo("Homer");
    Assertions.assertThat(list.get(1).get(1).asString()).isEqualTo("Rosie");
  }
}
