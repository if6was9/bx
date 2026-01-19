package bx.sql;

import bx.util.BxException;
import bx.util.Zones;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.dao.DataAccessException;

/**
 * Simple wrapper for ResultSet
 */
public class Results {

  ResultSet rs;

  public static Results create(ResultSet rs) {
    Preconditions.checkNotNull(rs);
    Results r = new Results();
    r.rs = rs;
    return r;
  }

  public ResultSet getResultSet() {
    return rs;
  }

  public boolean prev() {
    try {

      return rs.previous();
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public boolean next() {
    try {

      return rs.next();
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<String> getString(int name) {
    try {
      return Optional.ofNullable(rs.getString(name));
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<String> getString(String name) {
    try {
      return Optional.ofNullable(rs.getString(name));
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<BigDecimal> getBigDecimal(int c) {

    try {
      BigDecimal bd = rs.getBigDecimal(c);
      if (bd == null) {
        return Optional.empty();
      }
      return Optional.of(bd);
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<BigDecimal> getBigDecimal(String name) {

    try {
      BigDecimal bd = rs.getBigDecimal(name);
      if (bd == null) {
        return Optional.empty();
      }
      return Optional.of(bd);
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Double> getDouble(String name) {
    try {
      double d = rs.getDouble(name);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(d);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Double> getDouble(int name) {
    try {
      double d = rs.getDouble(name);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(d);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Long> getLong(int col) {
    try {
      long v = rs.getLong(col);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(v);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Date> getDate(int col) {
    try {
      return Optional.ofNullable(rs.getDate(col));

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Date> getDate(String col) {
    try {
      return Optional.ofNullable(rs.getDate(col));

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Time> getTime(int col) {
    try {
      return Optional.ofNullable(rs.getTime(col));

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Time> getTime(String col) {
    try {
      return Optional.ofNullable(rs.getTime(col));

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Long> getLong(String name) {
    try {
      long d = rs.getLong(name);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(d);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Integer> getInt(int col) {
    try {
      int v = rs.getInt(col);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(v);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Integer> getInt(String name) {
    try {
      int v = rs.getInt(name);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(v);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Boolean> getBoolean(String name) {
    try {
      boolean v = rs.getBoolean(name);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(v);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Boolean> getBoolean(int col) {
    try {
      boolean v = rs.getBoolean(col);
      if (rs.wasNull()) {
        return Optional.empty();
      }
      return Optional.of(v);

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  private static Optional<LocalDate> toLocalDate(Object input, ZoneId zone) {
    if (input == null) {
      return Optional.empty();
    }
    if (input instanceof LocalDate) {
      return Optional.of((LocalDate) input);
    }
    if (input instanceof java.sql.Date) {
      java.sql.Date d = (java.sql.Date) input;
      return Optional.of(d.toLocalDate());
    }
    if (input instanceof java.sql.Timestamp) {
      java.sql.Timestamp ts = (java.sql.Timestamp) input;
      return Optional.of(ts.toLocalDateTime().toLocalDate());
    }
    if (input instanceof Instant) {
      Instant t = (Instant) input;
      ZonedDateTime dt = t.atZone(zone);
      return Optional.of(dt.toLocalDate());
    }
    if (input instanceof ZonedDateTime) {
      ZonedDateTime t = (ZonedDateTime) input;
      return Optional.of(t.toLocalDate());
    }
    if (input instanceof OffsetDateTime) {
      OffsetDateTime t = (OffsetDateTime) input;
      return Optional.of(t.toLocalDate());
    }
    throw new BxException("unable to convert " + input.getClass() + " to LocalDate");
  }

  public ZoneId getSessionZone() {
    return Zones.UTC;
  }

  public Optional<ZonedDateTime> toZonedDateTime(Object input) {
    return toZonedDateTime(input, getSessionZone());
  }

  private Optional<ZonedDateTime> toZonedDateTime(Object input, ZoneId zone) {
    if (input == null) {
      return Optional.empty();
    }
    if (zone == null) {
      zone = getSessionZone();
    }

    if (input instanceof LocalDate) {
      LocalDate d = (LocalDate) input;
      return Optional.of(d.atStartOfDay(zone));
    }
    if (input instanceof java.sql.Date) {
      java.sql.Date d = (java.sql.Date) input;
      return Optional.of(d.toLocalDate().atStartOfDay(zone));
    }
    if (input instanceof java.sql.Timestamp) {
      java.sql.Timestamp ts = (java.sql.Timestamp) input;
      return Optional.of(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), zone));
    }
    if (input instanceof Instant) {
      Instant t = (Instant) input;
      return Optional.of(ZonedDateTime.ofInstant(t, zone));
    }
    if (input instanceof ZonedDateTime) {
      ZonedDateTime t = (ZonedDateTime) input;
      return Optional.of(t);
    }
    if (input instanceof OffsetDateTime) {
      OffsetDateTime t = (OffsetDateTime) input;
      return Optional.of(t.toZonedDateTime());
    }
    throw new BxException("unable to convert " + input.getClass() + " to ZonedDateTime");
  }

  public Optional<Instant> toInstant(Object input) {
    return toInstant(input, getSessionZone());
  }

  private Optional<Instant> toInstant(Object input, ZoneId zone) {
    if (input == null) {
      return Optional.empty();
    }
    if (zone == null) {
      zone = getSessionZone();
    }
    if (input instanceof LocalDate) {
      LocalDate d = (LocalDate) input;
      return Optional.of(d.atStartOfDay(zone).toInstant());
    }
    if (input instanceof java.sql.Date) {
      java.sql.Date d = (java.sql.Date) input;
      return Optional.of(d.toLocalDate().atStartOfDay(zone).toInstant());
    }
    if (input instanceof java.sql.Timestamp) {
      java.sql.Timestamp ts = (java.sql.Timestamp) input;
      return Optional.of(ts.toInstant());
    }
    if (input instanceof Instant) {
      Instant t = (Instant) input;
      return Optional.of(t);
    }
    if (input instanceof ZonedDateTime) {
      ZonedDateTime t = (ZonedDateTime) input;
      return Optional.of(t.toInstant());
    }
    if (input instanceof OffsetDateTime) {
      OffsetDateTime t = (OffsetDateTime) input;
      return Optional.of(t.toInstant());
    }
    throw new BxException("unable to convert " + input.getClass() + " to ZonedDateTime");
  }

  public Optional<LocalDate> getLocalDate(int c) {
    try {
      Object x = rs.getObject(c);

      return toLocalDate(x, getSessionZone());

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<LocalDate> getLocalDate(String name) {
    try {
      Object x = rs.getObject(name);

      return toLocalDate(x, getSessionZone());

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }







  public Optional<Timestamp> getTimestamp(int col) {
    try {
      Timestamp ts = rs.getTimestamp(col);
      return Optional.ofNullable(ts);
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Timestamp> getTimestamp(String name) {
    try {
      Timestamp ts = rs.getTimestamp(name);
      return Optional.ofNullable(ts);
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }





  
  
  private String getColumnName(int c) {
    try {
      // some drivers don't throw SQLException 
      // and instead throw RuntimeException for out-of-bounds conditons
      // we handle that here
      return rs.getMetaData().getColumnName(c);
    }
    catch (SQLException e) {
      throw new DbException(e);
    }
    catch (DataAccessException e) {
      throw e;
    }
    catch (RuntimeException e) {
      throw new DbException(e);
    }
  }
  
  
 ///////////
 /// ////////
  public Optional<OffsetDateTime> getOffsetDateTime(String c) {
    return getOffsetDateTime(c,null);
  }
  public Optional<OffsetDateTime> getOffsetDateTime(int c) {
    return getOffsetDateTime(c,null);
  }
  public Optional<OffsetDateTime> getOffsetDateTime(int c, ZoneOffset offset) {
    return getOffsetDateTime(getColumnName(c),offset);
  }
  public Optional<OffsetDateTime> getOffsetDateTime(String c, ZoneOffset offset) {
    try {
      Object val = rs.getObject(c);
      if (val == null) {
        return Optional.empty();
      }

      if (val instanceof OffsetDateTime) {
        return Optional.of((OffsetDateTime) val);
      } 
      
      if (offset!=null) {
        Optional<Timestamp> ts = getTimestamp(c);
        
        if (ts.isPresent()) {
          return Optional.of(OffsetDateTime.of(ts.get().toLocalDateTime(),offset));
        }
      }
      
      
      
    } catch (SQLException e) {
      throw new DbException(e);
    }
    
    throw new DbException("could not obtain OffsetDateTime");
  }
  
  public Optional<ZonedDateTime> getZonedDateTime(String c) {
    return getZonedDateTime(c,null);
  }
  public Optional<ZonedDateTime> getZonedDateTime(int c) {
    return getZonedDateTime(c,null);
  }
  public Optional<ZonedDateTime> getZonedDateTime(int c, ZoneId zone) {
    return getZonedDateTime(getColumnName(c),zone);
  }
  public Optional<ZonedDateTime> getZonedDateTime(String c, ZoneId zone) {
    try {
      Object val = rs.getObject(c);
      if (val == null) {
        return Optional.empty();
      }

      if (val instanceof OffsetDateTime) {
        return toZonedDateTime((OffsetDateTime)val,zone);
      } 
      else if (val instanceof Timestamp) {
        return toZonedDateTime((Timestamp)val,zone); 
      }
   
      
        Timestamp ts = rs.getTimestamp(c);
        if (ts!=null) {
          return toZonedDateTime((Timestamp)val,zone); 
        }

      
    } catch (SQLException e) {
      throw new DbException(e);
    }
    
    throw new DbException("could not obtain OffsetDateTime");
  }
  
  public Optional<Instant> getInstant(int c, ZoneId zone) {
    return getInstant(getColumnName(c),zone);
  }
  public Optional<Instant> getInstant(int c) {
    return getInstant(c ,null);
  }
  public Optional<Instant> getInstant(String c) {
    return getInstant(c,null);
  }
  public Optional<Instant> getInstant(String c, ZoneId zone) {

    Optional<ZonedDateTime> zdt = getZonedDateTime(c,zone);
    
    if (zdt.isEmpty()) {
      return Optional.empty();
    }
    
    return Optional.of(zdt.get().toInstant());
  
  }
}
