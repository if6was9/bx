package bx.sql;

import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class DbException extends DataAccessException {

  public DbException(String s) {
    super(s);
  }

  public DbException(SQLException e) {

    super(e.getMessage(), e);
  }
}
