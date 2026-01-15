package bx.sql;

import bx.util.BxTest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class CsvImportTest extends BxTest {

  @Test
  public void testIt() {

    JdbcClient c = JdbcClient.create(dataSource());

    c.sql("create table dog (name varchar(20) ,BREED varchar(20), age int, extra_column int)")
        .update();

    String data =
        """
        name,breed,age,extra_csv
        Homer,Schnoodle,8,
        Rosie,Golden Schnoodle,3,
        """;
    CsvImport.into(dataSource()).table("dog").fromString(data).importData();

    ConsoleQuery.with(c).table("dog").select();
  }
}
