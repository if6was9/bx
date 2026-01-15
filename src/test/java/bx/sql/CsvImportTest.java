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

  @Test
  public void testDate() {

    JdbcClient c = JdbcClient.create(dataSource());

    c.sql("create table dog (name varchar(20) , birthdate date)").update();

    String data =
        """
        name,birthdate
        Homer,2016-07-18
        """;
    CsvImport.into(dataSource()).table("dog").fromString(data).importData();

    ConsoleQuery.with(c).table("dog").select();
  }

  @Test
  public void testTimestamp() {

    JdbcClient c = JdbcClient.create(dataSource());

    c.sql("create table event (event varchar(20) , ts timestamp)").update();

    String data =
        """
        event,ts
        a,2016-07-18T12:13:14
        """;
    CsvImport.into(dataSource()).table("event").fromString(data).importData();

    ConsoleQuery.with(c).table("event").select();
  }

  @Test
  public void testTimestampWithZone() {

    JdbcClient c = JdbcClient.create(dataSource());

    c.sql("create table event (event varchar(20) , ts timestamptz)").update();

    String data =
        """
        event,ts
        a,2016-07-18T12:13:14Z
        b,2016-07-18T12:13:14-07:00
        """;
    CsvImport.into(dataSource()).table("event").fromString(data).importData();

    ConsoleQuery.with(c).table("event").select();
  }
}
