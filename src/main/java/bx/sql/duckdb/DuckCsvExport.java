package bx.sql.duckdb;

import bx.sql.BxJdbcClient;
import bx.util.BxException;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class DuckCsvExport {

  static Logger logger = Slogger.forEnclosingClass();

  DataSource dataSource;

  DuckTable fromTable;

  File outputFile;

  String outputS3Bucket = null;
  String outputS3Key = null;

  String sql = "SELECT * from {{table}}";

  Supplier<StatementSpec> specSupplier = null;

  public static DuckCsvExport using(DuckTable table) {
    return new DuckCsvExport(table);
  }

  public static DuckCsvExport using(DataSource ds) {
    return new DuckCsvExport(ds);
  }

  public DuckCsvExport(DataSource ds) {
    Preconditions.checkNotNull(ds, "DataSource cannot be null");
    this.dataSource = ds;
  }

  public DuckCsvExport(DuckTable t) {
    Preconditions.checkNotNull(t, "table cannot be null");
    this.fromTable = t;
    this.dataSource = t.getDataSource();
  }

  public DuckCsvExport toS3(String bucket, String key) {
    this.outputFile = null;
    this.outputS3Bucket = bucket;
    this.outputS3Key = key;
    Preconditions.checkArgument(S.isNotBlank(bucket));
    Preconditions.checkArgument(S.isNotBlank(key));
    Preconditions.checkArgument(!key.startsWith("/"), "s3 key should not start with /");
    return this;
  }

  public DuckCsvExport to(File file) {
    this.outputFile = file;
    this.outputS3Bucket = null;
    this.outputS3Key = null;
    return this;
  }

  public DuckCsvExport table(String table) {
    this.fromTable = DuckTable.of(dataSource, table);
    return this;
  }

  public DuckCsvExport select(String sql) {
    return select(sql, null);
  }

  public DuckCsvExport select(String sql, Function<StatementSpec, StatementSpec> paramConfig) {

    specSupplier =
        new Supplier<JdbcClient.StatementSpec>() {

          @Override
          public StatementSpec get() {

            String fullSql = String.format("COPY (%s) TO '%s'", sql, toOutput());
            if (fromTable == null && fullSql.contains("{{table}}")) {
              throw new IllegalArgumentException("table not specified");
            }
            JdbcClient c =
                BxJdbcClient.create(dataSource, fromTable != null ? fromTable.getName() : null);
            StatementSpec spec = c.sql(fullSql);
            if (paramConfig != null) {
              spec = paramConfig.apply(spec);
            }

            return spec;
          }
        };
    return this;
  }

  private String toOutput() {
    if (this.outputFile != null) {
      return outputFile.getAbsolutePath();
    }

    if (S.isNotBlank(outputS3Bucket) && S.isNotBlank(outputS3Key)) {
      return String.format("s3://%s/%s", outputS3Bucket, outputS3Key);
    }

    if (S.isNotBlank(outputS3Bucket) && S.isBlank(outputS3Key)) {
      throw new IllegalArgumentException("S3 object key must be specified");
    }
    if (S.isNotBlank(outputS3Key)) {
      throw new IllegalArgumentException("S3 bucket must be specified");
    }
    throw new IllegalArgumentException("no output specified");
  }

  private StatementSpec buildStatement() {

    if (specSupplier == null) {
      select("select * from {{table}}");
    }
    return specSupplier.get();
  }

  public void export() {

    buildStatement().update();
  }

  public String exportAsString() {
    File tempOutputFile = null;
    try {
      tempOutputFile = Files.createTempFile("temp_", ".csv").toFile();
      tempOutputFile.delete(); // because createTempFile creates a 0-byte file
      this.to(tempOutputFile).export();

      return com.google.common.io.Files.asCharSource(tempOutputFile, StandardCharsets.UTF_8).read();
    } catch (IOException e) {
      throw new BxException(e);
    } finally {
      if (tempOutputFile != null) {
        tempOutputFile.delete();
      }
    }
  }
}
