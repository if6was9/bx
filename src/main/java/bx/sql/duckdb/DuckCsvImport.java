package bx.sql.duckdb;

import bx.sql.BxJdbcClient;
import bx.util.BxException;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class DuckCsvImport {

  static Logger logger = Slogger.forEnclosingClass();

  DataSource dataSource;
  DuckTable table;

  File inputFile;
  String inputS3Bucket;
  String inputS3Key;

  String orderByClause = null;
  List<String> columns = List.of("*");

  File tempFile = null;

  public DuckCsvImport(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DuckCsvImport(DuckTable table) {
    this.table = table;
    this.dataSource = table.getDataSource();
  }

  public DuckCsvImport from(File f) {
    this.inputFile = f;
    this.inputS3Bucket = null;
    this.inputS3Key = null;
    return this;
  }

  public DuckCsvImport table(DuckTable t) {
    this.table = t;
    this.dataSource = t.getDataSource();
    return this;
  }

  public DuckCsvImport orderBy(String orderBy) {
    if (orderBy == null) {
      this.orderByClause = " ";
      return this;
    }
    Pattern p =
        Pattern.compile("\\s*(order)\\s+(by)\\s+(.*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(orderBy);
    if (!m.matches()) {
      this.orderByClause = String.format(" order by %s", orderBy);
    } else {
      this.orderByClause = orderBy;
    }
    return this;
  }

  public DuckCsvImport fromString(String csv) {

    try {
      File tempCsvFile = Files.createTempFile("__bx_temp__", ".csv").toFile();
      com.google.common.io.Files.asCharSink(tempCsvFile, StandardCharsets.UTF_8).write(csv);
      this.inputFile = tempCsvFile;
      this.tempFile = tempCsvFile;
    } catch (IOException e) {
      throw new BxException(e);
    }
    return this;
  }

  public DuckCsvImport table(String name) {
    this.table(DuckTable.of(dataSource, name));
    return this;
  }

  public DuckCsvImport select(String sql, Function<StatementSpec, StatementSpec> paramConfig) {

    return this;
  }

  String toInput() {
    if (inputFile != null) {
      return inputFile.getAbsolutePath();
    }
    if (S.isNotBlank(inputS3Bucket) && S.isNotBlank(inputS3Key)) {
      Preconditions.checkArgument(!inputS3Key.startsWith("/"));
      return String.format("s3://%s/%s", inputS3Bucket, inputS3Key);
    }

    throw new IllegalArgumentException("file or s3 bucket+key must be specified");
  }

  public String toColumnSpec() {
    if (this.columns == null || this.columns.isEmpty()) {
      return "*";
    }
    return Joiner.on(", ").join(columns);
  }

  public DuckTable load() {
    return importData();
  }

  public static DuckCsvImport using(DataSource ds) {
    return new DuckCsvImport(ds);
  }

  public DuckTable importData() {

    try {
      if (this.table == null) {
        String tempTableName =
            String.format("temp_%s", Long.toHexString(System.currentTimeMillis()));
        this.table = DuckTable.of(dataSource, tempTableName);
      }

      if (this.table.exists()) {
        throw new UnsupportedOperationException("importing into existing table not yet supported");
      }

      String updateSql = "CREATE TABLE " + table.getName() + " AS ";

      String selectSql =
          String.format(
              " SELECT %s FROM read_csv('%s') %s",
              toColumnSpec(), toInput(), S.notNull(orderByClause).orElse(""));

      updateSql = updateSql + selectSql;

      logger.atInfo().log("SQL: {}", updateSql);

      BxJdbcClient.create(dataSource, table.getName()).sql(updateSql).update();

      return table;
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
  }
}
