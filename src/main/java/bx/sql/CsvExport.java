package bx.sql;

import java.io.File;
import java.util.function.Function;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public interface CsvExport<T extends CsvExport<?>> {

  T table(String name);

  T to(File f);

  T sql(String sql);

  T sql(String sql, Function<StatementSpec, StatementSpec> config);

  void export();

  String exportToString();

  public static CsvExport<GenericCsvExport> from(DataSource dataSource) {
    return GenericCsvExport.from(dataSource);
  }
}
