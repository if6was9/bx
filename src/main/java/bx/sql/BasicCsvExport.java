package bx.sql;

import bx.util.BxException;
import bx.util.Defer;
import bx.util.S;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSink;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.CsvWriter.CsvWriterBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public  class BasicCsvExport implements CsvExport<BasicCsvExport>{

  ByteSink byteSink;
  List<Consumer<CsvWriterBuilder>> configList = Lists.newArrayList();
  DataSource dataSource;
  Function<JdbcClient, StatementSpec> selectFunction;
  String table;
  boolean gzip = false;

  public static BasicCsvExport from(DataSource ds) {
    Preconditions.checkNotNull(ds);
    BasicCsvExport export = new BasicCsvExport();
    export.dataSource = ds;
    return export;

  }

  class DefaultSelect implements Function<JdbcClient, JdbcClient.StatementSpec> {
    @Override
    public StatementSpec apply(JdbcClient t) {
      return t.sql(SqlUtil.interpolateTable("SELECT * from {{table}}", table));

    }
  }

  public BasicCsvExport() {
    
    selectFunction = new DefaultSelect();
    
  }

  @SuppressWarnings("unchecked")
  @Override
  public BasicCsvExport sql(String sql, Function<StatementSpec, StatementSpec> statement) {

    return (BasicCsvExport) sql(
        jdbc -> {
          StatementSpec spec = jdbc.sql(sql);

          if (statement != null) {
            return statement.apply(spec);
          }
          return spec;
        });
  }

  public BasicCsvExport sql(String sql) {

    return  sql(jdbc -> jdbc.sql(sql));
  }

  public BasicCsvExport table(String table) {
    this.table = table;
    
    return this;
  }
  public BasicCsvExport sql(Function<JdbcClient, StatementSpec> f) {
    this.selectFunction = f;
    return this;
  }

  public void export() {

     selectFunction.apply(BxJdbcClient.create(dataSource,table)).query( new ExportResultSetExtractor());
  }

  public String exportToString() {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    to(baos);

    export();

    return new String(baos.toByteArray());
  }

  CsvWriterBuilder applyConfig(CsvWriterBuilder b) {
    configList.forEach(c -> c.accept(b));
    return b;
  }

  public BasicCsvExport withConfig(Consumer<CsvWriterBuilder> config) {
    configList.add(config);
    return this;
  }

  public BasicCsvExport gzip(boolean b) {
    this.gzip = b;
    return this;
  }
  public BasicCsvExport to(File output) {
    if (output.getName().endsWith(".gz")) {
      this.gzip = true;
    } else {
      this.gzip = false;
    }
    return to(com.google.common.io.Files.asByteSink(output));
  }

  public BasicCsvExport to(OutputStream w) {
    ByteSink sink =
        new ByteSink() {

          @Override
          public OutputStream openStream() throws IOException {
            return w;
          }
        };

    return to(sink);
  }

  public BasicCsvExport to(ByteSink sink) {
    this.byteSink = sink;
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


  public ResultSetExtractor<Integer> newResultSetExtractor() {
    return new ExportResultSetExtractor();
  }
  public class ExportResultSetExtractor implements ResultSetExtractor<Integer> {
    @Override
    public Integer extractData(ResultSet rs) throws SQLException {

      Preconditions.checkArgument(byteSink != null, "destination must be set");
      try (Defer defer = Defer.create()) {
        OutputStream os = byteSink.openStream();

        if (gzip) {
          os = new GZIPOutputStream(os);
        }
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
 
}
