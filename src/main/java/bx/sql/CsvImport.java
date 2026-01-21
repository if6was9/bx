package bx.sql;

import bx.util.BxException;
import bx.util.Defer;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class CsvImport {

  Logger logger = Slogger.forEnclosingClass();

  JdbcClient jdbc;
  String table;

  ByteSource byteSource;
  boolean gzip = false;

  Map<String, Integer> columnTypeMap = Maps.newHashMap();
  Map<String, String> columnTypeNameMap = Maps.newHashMap();

  CsvImport() {}

  public static CsvImport into(DataSource dataSource) {

    return into(JdbcClient.create(dataSource));
  }

  public static CsvImport into(JdbcClient c) {
    CsvImport imp = new CsvImport();
    imp.jdbc = c;
    return imp;
  }

  public CsvImport from(File file) {
    Preconditions.checkNotNull(file);
    if (file.getName().endsWith(".gz")) {
      gzip = true;
    } else {
      gzip = false;
    }
    return from(Files.asByteSource(file));
  }

  public CsvImport from(ByteSource source) {
    this.byteSource = source;
    return this;
  }

  public CsvImport fromString(String data) {
    this.byteSource = ByteSource.wrap(data.getBytes(StandardCharsets.UTF_8));
    return this;
  }

  public CsvImport into(String table) {
    return table(table);
  }

  public CsvImport table(String table) {
    this.table = table;
    return this;
  }

  private void collectTableMetadata() {

    columnTypeMap = Maps.newHashMap();
    columnTypeNameMap = Maps.newHashMap();

    Preconditions.checkArgument(S.isNotBlank(table), "table name must be set");
    String sql = "select * from {{table}} limit 1";
    if (table != null) {
      sql = sql.replace("{{table}}", table);
    }

    this.jdbc
        .sql(sql)
        .query(
            rse -> {
              ResultSetMetaData md = rse.getMetaData();
              for (int i = 1; i <= md.getColumnCount(); i++) {
                String name = md.getColumnName(i);
                String typeName = md.getColumnTypeName(i);
                columnTypeNameMap.put(name.toLowerCase(), typeName);
                columnTypeMap.put(name.toLowerCase(), md.getColumnType(i));
              }
              while (rse.next()) {
                // consume it;
              }
              return 0;
            });
  }

  private String buildInsertSql(NamedCsvRecord r) {
    String sql =
        """
        insert into {{table}} (
        {{columns}}
        )
        values (
        {{placeholders}}
        )
        """;
    sql = sql.replace("{{table}}", table);

    List<String> fields = r.getHeader();

    fields = fields.stream().filter(f -> columnTypeMap.containsKey(f.toLowerCase())).toList();
    String columnString = Joiner.on(",\n").join(fields);

    sql = sql.replace("{{columns}}", columnString);

    List<String> bindVars = fields.stream().map(s -> ":" + s).toList();
    String bindString = Joiner.on(",\n").join(bindVars);

    sql = sql.replace("{{placeholders}}", bindString);

    return sql;
  }

  Object convertBindVal(Object val, int columnType) {

    return SqlBindTypeConverter.convert(val, columnType);
  }

  Object convertBindVal(String column, Object val) {
    // this is where we can look up the column
    // and perform a type conversion on val

    Integer columnType = columnTypeMap.get(column.toLowerCase());
    String columnTypeName = columnTypeNameMap.get(column.toLowerCase());

    if (columnType == null) {
      return null;
    }
    Object x = convertBindVal(val, columnType);

    logger
        .atTrace()
        .log(
            "convert {}({}) to {} ==> {}",
            val == null ? "" : val.getClass().getSimpleName(),
            val,
            columnTypeName,
            x);

    return x;
  }

  void bind(StatementSpec spec, Map<String, String> params) {

    params.forEach(
        (k, v) -> {
          if (columnTypeMap.containsKey(k)) {
            spec.param(k, convertBindVal(k, v));
          } else {

            // spec.param(k,null);
          }
        });

    Sets.difference(columnTypeMap.keySet(), params.keySet())
        .forEach(
            name -> {
              spec.param(name, null);
            });
  }

  private InputStream gunzip(InputStream in) throws IOException {
    if (gzip) {
      return new GZIPInputStream(in);
    } else {
      return in;
    }
  }

  public CsvImport gzip(boolean gz) {
    this.gzip = gz;
    return this;
  }

  public int importData() {

    collectTableMetadata();

    try (Defer defer = Defer.create()) {
      AtomicInteger count = new AtomicInteger();

      CsvReader<NamedCsvRecord> r =
          CsvReader.builder().ofNamedCsvRecord(gunzip(byteSource.openStream()));
      defer.register(r);

      String sql = null;
      Iterator<NamedCsvRecord> t = r.iterator();
      StatementSpec statementSpec = null;

      while (t.hasNext()) {
        NamedCsvRecord record = t.next();

        if (sql == null) {
          sql = buildInsertSql(record);

          statementSpec = this.jdbc.sql(sql);
        }
        bind(statementSpec, record.getFieldsAsMap());
        statementSpec.update();
      }

      return count.get();

    } catch (IOException e) {
      throw new BxException(e);
    }
  }
}
