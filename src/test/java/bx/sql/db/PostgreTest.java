package bx.sql.db;

import bx.sql.Db;
import bx.util.BxTest;
import bx.util.Slogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestInstance(Lifecycle.PER_CLASS)
public class PostgreTest extends BxTest {

  static Logger logger = Slogger.forEnclosingClass();

  PostgreSQLContainer postgres;
  Db db;

  @BeforeEach
  public void setupPostgres() {
    try {
      if (postgres != null) {
        return;
      }
      postgres =
          new PostgreSQLContainer("postgres:16-alpine")
              .withDatabaseName("testdb")
              .withUsername("testuser")
              .withPassword("testpass");
      postgres.start();

      db = Db.create(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

    } catch (Exception e) {
      org.junit.jupiter.api.Assumptions.assumeTrue(false);
      postgres = null;
    }
  }

  @AfterAll
  public void tearDownPostgres() {

    if (db != null) {
      try {
        db.close();
        db = null;
      } catch (Throwable t) {
        logger.atWarn().setCause(t).log();
      }
    }

    if (postgres != null) {
      try {
        postgres.close();
      } catch (Throwable e) {
        postgres = null;
        logger.atWarn().setCause(e).log();
      }
    }
  }

  @Test
  public void testIt() {

    db.getJdbcClient().sql("create table dog (name varchar(20))").update();

    db.getJdbcClient().sql("insert into dog (name) values (:name)").param("name", "Homer").update();

    db.consoleQuery().select("select * from dog");
  }
}
