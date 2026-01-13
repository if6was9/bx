package bx.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;

public class Defer implements AutoCloseable {

  static Logger logger = Slogger.forEnclosingClass();
  private List<Object> deferredList = Lists.newArrayList();
  private boolean swallow = false;

  public Defer withSwallow(boolean swallow) {
    this.swallow = swallow;
    return this;
  }

  public boolean isSwallowEnabled() {
    return swallow;
  }

  public static Defer create() {

    Defer d = new Defer();
    return d;
  }

  public void register(Object obj) {
    Preconditions.checkNotNull(obj);
    this.deferredList.add(obj);
  }

  @Override
  public void close() {

    Collections.reverse(deferredList);

    List<Throwable> exceptions = null;
    for (Object obj : deferredList) {
      try {
        if (obj instanceof AutoCloseable) {
          ((AutoCloseable) obj).close();
        } else if (obj instanceof Closeable) {
          ((Closeable) obj).close();
        } else if (obj instanceof Connection) {
          ((Connection) obj).close();
        } else if (obj instanceof ResultSet) {
          ((ResultSet) obj).close();
        } else if (obj instanceof Statement) {
          ((Statement) obj).close();
        } else {
          throw new UnsupportedOperationException("do not know how to close " + obj);
        }

      } catch (Throwable e) {
        if (exceptions == null) {
          exceptions = new LinkedList<Throwable>();
        }
        exceptions.add(e);
      }
    }
    deferredList.clear();

    if (exceptions == null) {
      return;
    }

    for (Throwable t : exceptions) {

      if (swallow == false) {
        if (t instanceof RuntimeException) {
          RuntimeException re = (RuntimeException) t;
          throw re;
        } else {
          BxException wrapper = new BxException(t);
          throw wrapper;
        }
      } else {
        logger.atWarn().setCause(t).log("problem closing resource");
      }
    }
  }
}
