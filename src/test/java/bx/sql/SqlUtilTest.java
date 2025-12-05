package bx.sql;

import bx.util.BxTest;

public class SqlUtilTest extends BxTest {

  /*
  @Test
   public void testGenerateSql() {
     var t = loadAdsbTable("adsb");

     String tableName = "adsb";
     Assertions.assertThat(SqlUtil.generateSqlFromFragment(null,"adsb",SqlOperation.SELECT)).isEqualTo("select * from adsb");
     Assertions.assertThat(SqlUtil.generateSqlFromFragment("","adsb",SqlOperation.SELECT)).isEqualTo("select * from adsb");
     Assertions.assertThat(SqlUtil.generateSqlFromFragment(null,tableName,SqlOperation.DELETE)).isEqualTo("delete from adsb");
     Assertions.assertThat(SqlUtil.generateSqlFromFragment(" ",tableName,SqlOperation.DELETE)).isEqualTo("delete from adsb");
     Assertions.assertThat(SqlUtil.generateSqlFromFragment("where id=:id",tableName,SqlOperation.SELECT)).isEqualTo("SELECT * FROM adsb where id=:id");
     Assertions.assertThat(SqlUtil.generateSqlFromFragment("where id=:id",tableName,SqlOperation.DELETE)).isEqualTo("DELETE FROM adsb where id=:id");

     Assertions.assertThat(SqlUtil.generateSqlFromFragment("delete from {{TABLE}} where id=:id",tableName,SqlOperation.DELETE)).isEqualTo("delete from adsb where id=:id");
     Assertions.assertThat(SqlUtil.generateSqlFromFragment("select id,flight from {{TABLE}} where id=:id",tableName,SqlOperation.SELECT)).isEqualTo("select id,flight from adsb where id=:id");

     Assertions.assertThat(SqlUtil.generateSqlFromFragment("show tables",tableName,SqlOperation.SELECT)).isEqualTo("show tables");

     Assertions.assertThat(SqlUtil.generateSqlFromFragment("fizzbuzz {{TABLE}}",tableName,SqlOperation.SELECT)).isEqualTo("fizzbuzz adsb");
   }*/
}
