package bx.sql.duckdb;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckS3Extension {

  Logger logger = bx.util.Slogger.forEnclosingClass();

  JdbcClient client;

  DuckS3Extension(JdbcClient db) {
    super();
    this.client = db;
  }

  public static DuckS3Extension load(DataSource ds) {
    return load(JdbcClient.create(ds));
  }

  public static DuckS3Extension load(JdbcClient c) {
    return new DuckS3Extension(c).loadExtension();
  }

  private DuckS3Extension loadExtension() {

    client.sql("INSTALL aws").update();
    client.sql("LOAD aws").update();

    return this;
  }

  public DuckS3Extension useCredentialChain() {

    try {
      String sql =
          """
          CREATE SECRET bx_aws_secret (
              TYPE S3,
              PROVIDER CREDENTIAL_CHAIN
          )
          """;

      client.sql(sql).query(rs -> {});

    } catch (DataAccessException e) {
      if (e.getMessage().toLowerCase().contains("already exists")) {
        logger.atDebug().log("credential chain already loaded");
        return this;
      }
      throw e;
    }
    return this;
  }
}
