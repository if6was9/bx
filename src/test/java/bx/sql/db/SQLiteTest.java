package bx.sql.db;

import bx.sql.Db;
import bx.util.BxTest;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class SQLiteTest extends BxTest {

  @Test
  public void testIt() throws IOException {

    String url =
        String.format(
            "jdbc:sqlite:%s", Files.createTempFile("sqlite", ".db").toFile().getAbsolutePath());

    Db db = Db.create(url);

    defer(db);

    db.getJdbcClient()
        .sql("create table if not exists dog (name varchar(20), breed varchar(20))")
        .update();
    db.getJdbcClient()
        .sql("insert into dog (name,breed) values (:name,:breed)")
        .param("name", "Homer")
        .param("breed", "Schnoodle")
        .update();
    db.consoleQuery().select("select * from dog");

    System.out.println(db.toString());
  }
}
