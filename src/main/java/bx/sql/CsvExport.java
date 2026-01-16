package bx.sql;

import bx.util.BxException;
import bx.util.Defer;
import bx.util.S;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSink;
import com.google.common.io.CharSink;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.CsvWriter.CsvWriterBuilder;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class CsvExport implements ResultSetExtractor<Integer> {

  CharSink charSink;
  List<Consumer<CsvWriterBuilder>> configList = Lists.newArrayList();
  JdbcClient client;
  Function<JdbcClient, StatementSpec> selectFunction;

  public static CsvExport from(DataSource ds) {
    Preconditions.checkNotNull(ds);
    return from(JdbcClient.create(ds));
  }

  public static CsvExport from(JdbcClient client) {
    Preconditions.checkNotNull(client);
    CsvExport export = new CsvExport();
    export.client = client;
    return export;
  }

  public CsvExport() {}

  public CsvExport sql(String sql, Function<StatementSpec, StatementSpec> statement) {

    return sql(
        jdbc -> {
          StatementSpec spec = jdbc.sql(sql);

          if (statement != null) {
            return statement.apply(spec);
          }
          return spec;
        });
  }

  public CsvExport sql(String sql) {

    return sql(jdbc -> jdbc.sql(sql));
  }

  public CsvExport sql(Function<JdbcClient, StatementSpec> f) {
    this.selectFunction = f;
    return this;
  }

  public int export() {
    return selectFunction.apply(this.client).query(this);
  }

  public String exportToString() {
    StringWriter sw = new StringWriter();

    to(sw);

    export();

    return sw.toString();
  }

  CsvWriterBuilder applyConfig(CsvWriterBuilder b) {
    configList.forEach(c -> c.accept(b));
    return b;
  }

  public CsvExport withConfig(Consumer<CsvWriterBuilder> config) {
    configList.add(config);
    return this;
  }

  public CsvExport to(File output) {
    return to(com.google.common.io.Files.asCharSink(output, StandardCharsets.UTF_8));
  }

  public CsvExport to(Writer w) {
    CharSink sink =
        new CharSink() {

          @Override
          public Writer openStream() throws IOException {
            return w;
          }
        };

    return to(sink);
  }

  public CsvExport to(CharSink sink) {
    this.charSink = sink;
    return this;
  }

  String toColumnName(ResultSetMetaData md, int c) throws SQLException {

    String name = md.getColumnLabel(c);
    if (S.isBlank(name)) {
      name = md.getColumnName(c);
    }
    if (S.isBlank(name)) {
      name = String.format("col_%s", c);
    }
    return name;
  }

  ByteSink wrap(OutputStream out) {
    ByteSink x =
        new ByteSink() {

          @Override
          public OutputStream openStream() throws IOException {
            return out;
          }
        };

    return x;
  }

  void writeHeader(ResultSet rs, CsvWriter csvWriter) throws SQLException {
    ResultSetMetaData md = rs.getMetaData();

    int count = md.getColumnCount();
    List<String> names = Lists.newLinkedList();
    for (int i = 1; i <= count; i++) {
      String name = toColumnName(md, i);
      names.add(name);
    }
    csvWriter.writeRecord(names);
  }

  void writeRow(ResultSet rs, CsvWriter csvWriter) throws SQLException {

    int colCount = rs.getMetaData().getColumnCount();
    String vals[] = new String[colCount];
    for (int i = 1; i <= colCount; i++) {
      vals[i - 1] = rs.getString(i);
    }

    csvWriter.writeRecord(vals);
  }

  @Override
  public Integer extractData(ResultSet rs) throws SQLException {

    Preconditions.checkArgument(charSink != null, "destination must be set");
    try (Defer defer = Defer.create()) {
      Writer os = charSink.openStream();
      defer.register(os);
      CsvWriter csvWriter = applyConfig(CsvWriter.builder()).build(os);
      defer.register(csvWriter);

      writeHeader(rs, csvWriter);
      AtomicInteger count = new AtomicInteger();
      while (rs.next()) {
        count.incrementAndGet();
        writeRow(rs, csvWriter);
      }
      return count.get();
    } catch (IOException e) {
      throw new BxException(e);
    }
  }
}
