package bx.sql.duckdb;

import bx.util.BxException;
import bx.util.S;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class DuckCsv {

  DataSource dataSource;

  String table = null;

  File sourceFile;
  File targetFile;

  String selectForExport;

  Consumer<StatementSpec> config;

  public DuckCsv(DataSource ds) {
    this.dataSource = ds;
    Preconditions.checkNotNull(dataSource, "DataSource cannot be null");
  }

  public static DuckCsv using(DataSource ds) {
    return new DuckCsv(ds);
  }

  public JdbcClient getJdbcClient() {
    return JdbcClient.create(dataSource);
  }

  public DuckCsv table(String name) {
    this.table = name;
    return this;
  }

  public DuckCsv table(DuckTable table) {
    this.table = table.getName();
    return this;
  }

  public DuckCsv from(File f) {
    this.sourceFile = f;
    return this;
  }

  public DuckCsv from(String content) {
    try {
      Path p = Files.createTempFile("temp", ".csv");
      com.google.common.io.Files.asCharSink(p.toFile(), StandardCharsets.UTF_8).write(content);
      return from(p.toFile());
      // DO NOT DELETE THE FILE
    } catch (IOException e) {
      throw new BxException(e);
    }
  }

  public DuckCsv to(File outputFile) {
    this.targetFile = outputFile;
    return this;
  }

  public DuckTable load() {
    if (table == null) {
      table = String.format("temp_%s", System.currentTimeMillis());
    }

    getJdbcClient()
        .sql(
            String.format(
                "create table %s as (select * from '%s')", table, sourceFile.getAbsolutePath()))
        .update();
    return DuckTable.of(dataSource, table);
  }

  public DuckCsv sql(String sql, Consumer<StatementSpec> spec) {
    this.selectForExport = sql;
    this.config = spec;
    return this;
  }

  public DuckCsv sql(String sql) {
    return sql(sql, (a) -> {});
  }

  public void export() {

    if (selectForExport == null) {
      Preconditions.checkState(S.isNotBlank(table), "table name not set");
      selectForExport = String.format("select * from %s", table);
    }
    Preconditions.checkState(targetFile != null, "output file not specified");
    String exportSql =
        String.format(
            "COPY (%s) TO '%s' (HEADER, DELIMITER ',')",
            selectForExport, targetFile.getAbsolutePath());

    if (targetFile == null) {
      throw new BxException("output file not specified");
    }
    StatementSpec s = getJdbcClient().sql(exportSql);
    if (this.config != null) {
      config.accept(s);
    }
    s.update();
  }

  public String exportString() {

    try {
      File tempFile = Files.createTempFile("temp", ".csv").toFile();
      to(tempFile).export();
      return com.google.common.io.Files.asCharSource(tempFile, StandardCharsets.UTF_8).read();
    } catch (IOException e) {
      throw new BxException(e);
    }
  }
}
