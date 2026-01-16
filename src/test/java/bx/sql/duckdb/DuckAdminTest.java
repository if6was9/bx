package bx.sql.duckdb;

import bx.util.BxTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuckAdminTest extends BxTest {

  @Test
  public void testShowSettings() {

    loadAdsbTable("adsb");

    DuckAdmin.of(getDataSource()).showSettings();
  }

  @Test
  public void testGetSettings() {
    System.out.println(DuckAdmin.of(getDataSource()).getSetting("TimeZone").get());
  }

  @Test
  public void testModifySetting() {
    DuckAdmin admin = DuckAdmin.of(getDataSource());
    admin.modifySetting("threads", "8");
    Assertions.assertThat(admin.getSetting("threads").get()).isEqualTo("8");
    admin.modifySetting("threads", 12);
    Assertions.assertThat(admin.getSetting("threads").get()).isEqualTo("12");
  }

  @Test
  public void testCheckpoint() {
    DuckAdmin admin = DuckAdmin.of(getDataSource());
    admin.checkpoint();
  }
}
