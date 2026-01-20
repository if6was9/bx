package bx.sql;

import bx.util.BxException;
import com.google.common.collect.Lists;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.slf4j.Logger;

public class SqlCloser implements AutoCloseable {

  static Logger logger = bx.util.Slogger.forEnclosingClass();

  List<Connection> connections = Lists.newArrayList();
  List<Statement> statements = Lists.newArrayList();
  List<ResultSet> resultSets = Lists.newArrayList();
  List<Throwable> exceptions = Lists.newArrayList();

  public static SqlCloser create() {
    return new SqlCloser();
  }

  @Override
  public void close() {

    for (ResultSet rs : resultSets.reversed()) {
      try {

        logger.atDebug().log("closing {}", rs);
        rs.close();
      } catch (RuntimeException | SQLException e) {
        exceptions.add(e);
      }
    }

    for (Statement st : statements.reversed()) {
      try {
        logger.atDebug().log("closing {}", st);
        st.close();
      } catch (RuntimeException | SQLException e) {
        exceptions.add(e);
      }
    }

    for (Connection c : connections.reversed()) {
      try {

        logger.atDebug().log("closing {}", c);
        c.close();

      } catch (RuntimeException | SQLException e) {
        exceptions.add(e);
      }
    }

    exceptions.forEach(
        t -> {
          logger.atDebug().setCause(t).log("problem closing resource");
        });

    if (!exceptions.isEmpty()) {
      Throwable t = exceptions.get(0);
      if (t instanceof RuntimeException) {
        RuntimeException e = ((RuntimeException) t);
        throw e;

      } else if (t instanceof SQLException) {
        throw new DbException((SQLException) t);
      } else {
        throw new BxException(t);
      }
    }
  }

  public void register(Results rs) {
    register(rs.getResultSet());
  }

  public void register(Connection c) {
    connections.add(c);
  }

  public void register(Statement st) {
    statements.add(st);
  }

  public void register(ResultSet rs) {
    resultSets.add(rs);
  }
}
