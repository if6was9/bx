package bx.util;

import bx.sql.DbException;
import bx.sql.Results;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class ConsoleTableRenderer implements ResultSetExtractor<String> {

  static Logger logger = Slogger.forEnclosingClass();

  public static final int DEFAULT_MAX_ROWS = 40;

  // ╷╵│╶┌└├╴┐┘┤─┬┴┼
  static char TOP_LEFT_CORNER = 9484; // '┌';
  static char TOP_RIGHT_CORNER = 9488; // '┐';
  static char TOP_TEE = 9516; // '┬';
  static char TOP_BORDER = 9472; // '─';

  static char BOTTOM_LEFT_CORNER = 9492; // '└';
  static char BOTTOM_RIGHT_CORNER = 9496; // '┘';
  static char BOTTOM_TEE = 9524; // '┴';
  static char BOTTOM_BORDER = 9472; // '─';
  static char LEFT_BORDER = 9474; // '│';
  static char LEFT_TEE = 9500; // '├';
  static char RIGHT_BORDER = 9474; // '│';
  static char RIGHT_TEE = 9508; // '┤';
  static char INNER_CROSS = 9532; // '┼';

  int cols = 0;
  int maxRows = DEFAULT_MAX_ROWS;
  int rowCount = 0;
  int tableWidth = 0;
  List<String> colNames = Lists.newArrayList();
  List<String> types = Lists.newArrayList();
  List<Integer> colWidths = Lists.newArrayList();

  List<List<String>> topRows = Lists.newArrayList();
  List<List<String>> bottomRows = new LinkedList<List<String>>();

  public ConsoleTableRenderer() {}

  public ConsoleTableRenderer maxRows(int n) {
    this.maxRows = n;
    System.out.println();
    return this;
  }

  @Override
  public String extractData(ResultSet rs) throws SQLException, DataAccessException {

    try {
      ResultSetMetaData md = rs.getMetaData();
      cols = md.getColumnCount();

      for (int i = 1; i <= cols; i++) {
        colNames.add(md.getColumnLabel(i));
        types.add(md.getColumnTypeName(i));
      }

    } catch (SQLException e) {
      throw new DbException(e);
    }

    Results r = Results.create(rs);
    while (r.next()) {
      List<String> row = Lists.newArrayList();
      for (int i = 1; i <= cols; i++) {

        String val = r.getString(i).orElse("NULL");
        row.add(val);
      }

      if (topRows.size() < maxRows / 2) {
        topRows.add(row);
      } else {
        bottomRows.add(row);
        if (bottomRows.size() > topRows.size()) {
          bottomRows.remove(0);
        }
      }

      rowCount++;
    }

    return render();
  }

  public String right(String input, int width) {
    String s = input;
    while (s.length() < width) {
      s = " " + s;
    }
    return s;
  }

  Set<String> rightJustifiedTypes =
      Set.of("DOUBLE", "BIGINT", "DECIMAL", "INT", "INT64", "INT32", "BOOL", "INTEGER");

  public String justify(String input, int col) {

    String type = types.get(col);
    if (type == null) {
      return leftJustify(input, colWidths.get(col));
    }
    if (rightJustifiedTypes.contains(type.toUpperCase())
        || type.toUpperCase().contains("NUMERIC")) {
      return right(input, colWidths.get(col));
    }
    return leftJustify(input, colWidths.get(col));
  }

  public String leftJustify(String input, int width) {
    String s = input;
    while (s.length() < width) {
      s = s + " ";
    }
    return s;
  }

  public String center(String input, int width) {
    String s = input != null ? input : "null";
    int c = 0;

    while (s.length() < width) {
      if (c++ % 2 == 0) {
        s = s + " ";
      } else {
        s = " " + s;
      }
    }

    return s;
  }

  public void computeWidths() {
    colWidths = Lists.newArrayList();
    for (int i = 0; i < cols; i++) {
      colWidths.add(0);
    }
    for (int i = 0; i < cols; i++) {
      colWidths.set(i, Math.max(colWidths.get(i), types.get(i).length()));
      colWidths.set(i, Math.max(colWidths.get(i), colNames.get(i).length()));
    }

    for (List<String> row : Stream.concat(topRows.stream(), bottomRows.stream()).toList()) {
      for (int c = 0; c < cols; c++) {
        String val = row.get(c);
        if (val == null) {
          val = "NULL";
        }
        int width = val.length();
        if (width > colWidths.get(c)) {
          colWidths.set(c, width);
        }
      }
    }

    tableWidth = 2; // left and right border
    for (int w : colWidths) {
      tableWidth += w;
      tableWidth += 4; // for left and right cell padding
    }
    tableWidth += cols - 1; // interior border
  }

  public String render() {
    computeWidths();
    StringWriter w = new StringWriter();
    int width = 0;

    {
      // example: ┌─────────────────────┬───────────┬───────────┐

      w.append(TOP_LEFT_CORNER);
      for (int i = 0; i < cols; i++) {

        for (int j = 0; j < colWidths.get(i) + 4; j++) {
          w.append(TOP_BORDER);
        }

        if (i == cols - 1) {
          w.append(TOP_RIGHT_CORNER);
        } else {
          w.append(TOP_TEE);
        }
      }
      String firstLine = w.toString();
      w = new StringWriter();
      width = firstLine.length();
      w.append(firstLine);
      w.append(System.lineSeparator());
    }
    {

      // EXAMPLE: │ id │ hex │ flight │

      w.append(LEFT_BORDER);
      for (int c = 0; c < cols; c++) {
        w.append("  ");
        w.append(center(colNames.get(c), colWidths.get(c)));
        w.append("  ");
        w.append(RIGHT_BORDER);
      }
      w.append(System.lineSeparator());
    }

    {

      // example: ├─────────────────────┼───────────┼───────────┤
      w.append(LEFT_BORDER);
      for (int c = 0; c < cols; c++) {
        w.append("  ");
        w.append(center(types.get(c).toLowerCase(), colWidths.get(c)));
        w.append("  ");
        w.append(RIGHT_BORDER);
      }
      w.append(System.lineSeparator());
    }
    {

      // EXAMPLE: │ varchar │ varchar │ varchar │
      w.append(LEFT_TEE);
      for (int c = 0; c < cols; c++) {
        for (int i = 0; i < colWidths.get(c) + 4; i++) {
          w.append(TOP_BORDER);
        }
        if (c < cols - 1) {
          w.append(INNER_CROSS);
        }
      }

      w.append(RIGHT_TEE);
      w.append(System.lineSeparator());
    }

    {
      // example: │ 1764559020-aca97c │ aca97c │ N915CM │
      for (List<String> row : topRows) {

        w.append(LEFT_BORDER);
        for (int c = 0; c < cols; c++) {

          String val = Objects.toString(row.get(c));
          val = justify(val, c);
          val = String.format("  %s  ", val);

          w.append(val);

          if (c < cols - 1) {
            w.append(RIGHT_BORDER);
          }
        }
        w.append(RIGHT_BORDER);
        w.append(System.lineSeparator());
      }
    }

    {
      // example │ . │ . │ . │
      if (rowCount > topRows.size() + bottomRows.size()) {

        List<String> tmp = Lists.newArrayList();
        for (int c = 0; c < cols; c++) {
          tmp.add(justify(".", c));
        }

        tmp = tmp.stream().map(n -> String.format("  %s  ", n)).toList();

        String x = Joiner.on(RIGHT_BORDER).join(tmp);
        x = LEFT_BORDER + x + RIGHT_BORDER;

        for (int i = 0; i < 3; i++) {
          w.append(x);
          w.append(System.lineSeparator());
        }
      }
    }

    {
      // example │ 1764613261-a63f3a │ a63f3a │ N501SC │

      for (List<String> row : bottomRows) {

        w.append(LEFT_BORDER);

        for (int c = 0; c < cols; c++) {

          String val = Objects.toString(row.get(c));
          val = String.format("  %s  ", justify(val, c));

          w.append(val);

          if (c < cols - 1) {
            w.append(RIGHT_BORDER);
          }
        }
        w.append(RIGHT_BORDER);
        w.append(System.lineSeparator());
      }
    }

    // only show the footer row if there are 10 or more rows
    if (hasFooter()) {
      {
        // example: ├─────────────────────┴───────────┴───────────┤
        w.append(LEFT_TEE);
        for (int c = 0; c < cols; c++) {
          for (int i = 0; i < colWidths.get(c) + 4; i++) {
            w.append(BOTTOM_BORDER);
          }
          if (c < cols - 1) {
            w.append(BOTTOM_TEE);
          }
        }
        w.append(RIGHT_TEE);

        w.append(System.lineSeparator());
      }
      {

        // example: │ 1000 rows (40 shown)                        │
        w.append(LEFT_BORDER);

        w.append("  ");

        String text = getFooterRowText();
        while (text.length() < tableWidth - 6) {
          text = text + " ";
        }
        w.append(text);
        w.append("  ");

        w.append(RIGHT_BORDER);
        w.append(System.lineSeparator());
      }
      {
        // example: └─────────────────────────────────────────────┘
        w.append(BOTTOM_LEFT_CORNER);
        int count = 0;
        for (int c = 0; c < cols; c++) {
          count += colWidths.get(c);
          count += 5;
        }

        for (int c = 0; c < count - 1; c++) {
          w.append(BOTTOM_BORDER);
        }
        w.append(BOTTOM_RIGHT_CORNER);
      }
    } else {
      {
        // example:└───────────┴───────────┘
        w.append(BOTTOM_LEFT_CORNER);
        for (int c = 0; c < cols; c++) {
          for (int i = 0; i < colWidths.get(c) + 4; i++) {
            w.append(BOTTOM_BORDER);
          }
          if (c < cols - 1) {
            w.append(BOTTOM_TEE);
          }
        }
        w.append(BOTTOM_RIGHT_CORNER);
      }
    }

    return w.toString();
  }

  boolean isTruncated() {
    return rowCount > (topRows.size() + bottomRows.size());
  }

  boolean footerFits(String footerText) {
    return footerText.length()
        <= tableWidth - 6; // 4 = 1 left border, 1 right border, 2 left padding, 2 right padding
  }

  String getFooterRowText() {

    String footerText = Plural.toCount(rowCount, "row");

    if (!footerFits(footerText)) {
      return "";
    }

    if (isTruncated()) {
      String footerTextWithRowTruncation =
          String.format("%s (%d shown)", footerText, topRows.size() + bottomRows.size());
      if (footerFits(footerTextWithRowTruncation)) {
        footerText = footerTextWithRowTruncation;
      }
    }

    return footerText;
  }

  boolean hasFooter() {
    if (rowCount < 10) {
      return false;
    }

    String s = getFooterRowText();

    if (S.isBlank(s)) {
      return false;
    }
    return true;
  }
}
