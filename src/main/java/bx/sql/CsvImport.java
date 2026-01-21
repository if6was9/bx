package bx.sql;

import java.io.File;
import javax.sql.DataSource;

public interface CsvImport<T extends CsvImport<?>> {

  T table(String table);

  T into(String table);

  void importData();

  T gzip(boolean gzip);

  T fromString(String input);

  T from(File f);

  public static CsvImport<GenericCsvImport> into(DataSource ds) {
    return GenericCsvImport.into(ds);
  }
}
