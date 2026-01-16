package bx.sql.duckdb;

import bx.sql.ConsoleQuery;
import bx.sql.CsvImport;
import bx.util.BxTest;
import bx.util.Slogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;

public class ConsoleQueryTest extends BxTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void test() {
    loadAdsbTable("adsb");

    ConsoleQuery.with(dataSource()).table("adsb").show();
    ConsoleQuery.with(dataSource())
        .select(c -> c.sql("select * from adsb limit :limit").param("limit", 5));
  }

  @Test
  public void testLoggingShouldNotThrowExceptions() {

    ConsoleQuery.with(dataSource()).stdout().select(c -> c.sql("select * from does_not_exist"));
  }

  @Test
  public void testIt() {
    var client = getH2Db().getInstance().getJdbcClient();

    client.sql("create table test (name char(20), age int)").update();
    client.sql("insert into test (name, age) values ('homer',8)").update();

    ConsoleQuery.with(client).table("test").show();
  }

  @Test
  public void testDoc() {

    var dataSource = dataSource();

    JdbcClient.create(dataSource).sql("create table actor(id int, name varchar(30))").update();
    JdbcClient.create(dataSource)
        .sql("insert into actor(id,name) values (:id,:name)")
        .param("id", 1)
        .param("name", "Leonardo DiCaprio")
        .update();

    JdbcClient.create(dataSource)
        .sql("insert into actor(id,name) values (:id,:name)")
        .param("id", 2)
        .param("name", "Chase Infiniti")
        .update();

    JdbcClient.create(dataSource)
        .sql("insert into actor(id,name) values (:id,:name)")
        .param("id", 3)
        .param("name", "Benicio del Toro")
        .update();

    ConsoleQuery.withDefaultDb()
        .select(c -> c.sql("Select * from actor where id=:id").param("id", 1));

    ConsoleQuery.withDefaultDb().select("Select * from actor where id=:id", c -> c.param("id", 1));
  }

  @Test
  public void testCsvImport() {

    var dataSource = dataSource();
    var jdbcClient = JdbcClient.create(dataSource);

    jdbcClient.sql("create table actor(id int, name varchar(30))").update();

    String csv =
        """
        id,name
        1,Leonardo DiCaprio
        2,Chase Infiniti
        3,Benicio del Toro
        """;

    CsvImport.into(dataSource).fromString(csv).table("actor").importData();

    ConsoleQuery.with(dataSource).table("actor").show();
  }
}
