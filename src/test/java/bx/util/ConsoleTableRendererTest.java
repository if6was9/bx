package bx.util;

import bx.sql.duckdb.DuckTable;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class ConsoleTableRendererTest extends BxTest {

  List<String> tableToCol(String table, int col) {

    try {
      List<String> lines = Lists.newArrayList(CharSource.wrap(table).readLines());
      lines.remove(0);
      lines.remove(0);
      lines.remove(0);
      lines.remove(0);

      lines.removeLast();
      lines.removeLast();
      lines.removeLast();

      var tmp =
          lines.stream()
              .map(
                  line -> {
                    return Splitter.on('│')
                        .trimResults()
                        .omitEmptyStrings()
                        .splitToList(line)
                        .get(col);
                  })
              .toList();
      System.out.println(tmp);
      return tmp;
    } catch (IOException e) {
      throw new BxException(e);
    }
  }

  @Test
  public void testIt() {

    List<String> ids =
        loadAdsbTable("adsb")
            .select("select id from adsb order by id")
            .query()
            .listOfRows()
            .stream()
            .map(m -> (String) m.get("id"))
            .toList();

    JdbcClient jdbc = JdbcClient.create(dataSource());

    ConsoleTableRenderer ct = new ConsoleTableRenderer();
    ct.maxRows(40);
    String s = jdbc.sql("select id,hex,flight from adsb order by id").query(ct);

    System.out.println(s);

    List<String> tableVals = tableToCol(s, 0).stream().toList();

    List<String> top = Lists.newArrayList();
    List<String> bottom = Lists.newArrayList();
    List<String> insertionPoint = top;
    for (String val : tableVals) {
      if (val.trim().equals(".")) {
        insertionPoint = bottom;
      } else {
        insertionPoint.add(val);
      }
    }

    Assertions.assertThat(top.size() + bottom.size()).isEqualTo(ct.maxRows);

    Assertions.assertThat(ids).startsWith(top.toArray(new String[0]));
    Assertions.assertThat(ids).endsWith(bottom.toArray(new String[0]));
  }

  @Test
  public void testSmallTable() {
    JdbcClient jdbc = JdbcClient.create(dataSource());
    String s = jdbc.sql("Select null as a, 2 as b").query(new ConsoleTableRenderer());
    System.out.println(s);

    Assertions.assertThat(s).doesNotEndWith("\n").doesNotEndWith("\r");
    Assertions.assertThat(s.endsWith(Character.toString(ConsoleTableRenderer.BOTTOM_RIGHT_CORNER)));

    String expected =
        """
        ┌───────────┬───────────┐
        │     a     │     b     │
        │  integer  │  integer  │
        ├───────────┼───────────┤
        │     NULL  │        2  │
        └───────────┴───────────┘
        """;
    expected = expected.trim();

    checkTable(s);
    Assertions.assertThat(s).isEqualTo(expected);
  }

  @Test
  public void testSkinnyTable() {
    DuckTable t = loadAdsbTable("adsb");
    String s =
        t.getJdbcClient().sql("select 0 as a from adsb limit 10").query(new ConsoleTableRenderer());

    String expected =
        """
        ┌───────────┐
        │     a     │
        │  integer  │
        ├───────────┤
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        │        0  │
        ├───────────┤
        │  10 rows  │
        └───────────┘
        """
            .trim();

    System.out.println(s);
    checkTable(s);
    Assertions.assertThat(s).isEqualTo(expected);
  }

  private void checkTableLine(String line) {
    line = line.trim();
    Set<Character> VALID_FIRST_CHARACTERS =
        Set.of(
            ConsoleTableRenderer.LEFT_TEE,
            ConsoleTableRenderer.LEFT_BORDER,
            ConsoleTableRenderer.TOP_LEFT_CORNER,
            ConsoleTableRenderer.BOTTOM_LEFT_CORNER);
    Set<Character> VALID_LAST_CHARACTERS =
        Set.of(
            ConsoleTableRenderer.RIGHT_TEE,
            ConsoleTableRenderer.RIGHT_BORDER,
            ConsoleTableRenderer.TOP_RIGHT_CORNER,
            ConsoleTableRenderer.BOTTOM_RIGHT_CORNER);
    Assertions.assertThat(VALID_FIRST_CHARACTERS.contains(line.charAt(0)))
        .withFailMessage(line)
        .isTrue();
    Assertions.assertThat(VALID_LAST_CHARACTERS.contains(line.charAt(line.length() - 1)))
        .withFailMessage("invalid last char: %s", line)
        .isTrue();

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == ConsoleTableRenderer.LEFT_BORDER && i < line.length() - 2) {
        Assertions.assertThat(line.substring(i + 1, i + 2)).isEqualTo(" ");
      }
      if (c == ConsoleTableRenderer.LEFT_BORDER && i > 2) {
        Assertions.assertThat(line.substring(i - 2, i - 1)).isEqualTo(" ");
      }
    }
    if (line.charAt(0) == ConsoleTableRenderer.BOTTOM_LEFT_CORNER) {
      Assertions.assertThat(line.trim().charAt(line.trim().length() - 1))
          .isEqualTo(ConsoleTableRenderer.BOTTOM_RIGHT_CORNER);
    }
    if (line.charAt(0) == ConsoleTableRenderer.TOP_LEFT_CORNER) {
      Assertions.assertThat(line.trim().charAt(line.trim().length() - 1))
          .isEqualTo(ConsoleTableRenderer.BOTTOM_RIGHT_CORNER);
    }
    if (line.charAt(line.length() - 1) == ConsoleTableRenderer.BOTTOM_RIGHT_CORNER) {
      Assertions.assertThat(line.charAt(0)).isEqualTo(ConsoleTableRenderer.BOTTOM_LEFT_CORNER);
    }
    if (line.charAt(line.length() - 1) == ConsoleTableRenderer.TOP_RIGHT_CORNER) {
      Assertions.assertThat(line.charAt(0)).isEqualTo(ConsoleTableRenderer.TOP_LEFT_CORNER);
    }
  }

  private void checkTable(String text) {

    Assertions.assertThat(text)
        .startsWith(Character.toString(ConsoleTableRenderer.TOP_LEFT_CORNER));
    Assertions.assertThat(text)
        .endsWith(Character.toString(ConsoleTableRenderer.BOTTOM_RIGHT_CORNER));

    try {
      List<String> lines = CharSource.wrap(text).lines().toList();
      for (int i = 1; i < lines.size(); i++) {
        Assertions.assertThat(lines.get(i).length())
            .withFailMessage("table should have consistent width")
            .isEqualTo(lines.get(i - 1).length());
        checkTableLine(lines.get(i));
      }

    } catch (IOException e) {
      throw new BxException(e);
    }
  }

  @Test
  public void testRender() {

    String s =
        JdbcClient.create(dataSource())
            .sql("select 123.456E-7 as foo")
            .query(new ConsoleTableRenderer());
    System.out.println(s);

    String val = Hashing.sha256().hashBytes(new byte[0]).toString();

    s =
        JdbcClient.create(dataSource())
            .sql("select '" + val + "' as foo")
            .query(new ConsoleTableRenderer());
    Assertions.assertThat(s).contains("b855  ");

    s =
        JdbcClient.create(dataSource())
            .sql("select '" + (val + "XXX") + "' as foo")
            .query(new ConsoleTableRenderer());
    Assertions.assertThat(s).contains("b855...  ");
  }
}
