package bx.sql.duckdb;

import bx.util.BxTest;

import java.sql.Connection;
import java.sql.SQLException;

import org.assertj.core.api.Assertions;
import org.duckdb.DuckDBConnection;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckDbTest extends BxTest {

	@Test
	public void testIt() throws Exception {

		var t = DuckTable.of(db().getDataSource(), "test");

		Assertions.assertThat(t.exists()).isFalse();

		db().getJdbcClient().sql("create table test (abc int)").update();

		Assertions.assertThat(t.exists()).isTrue();

		Assertions.assertThat(t.rowCount()).isEqualTo(0);

		// Create a DataSource to access an in-memory DuckDB instance
		var ds = DuckDataSource.createInMemory();

		// Use Spring JDBC to access the database
		var client = JdbcClient.create(ds);

	}

	@Test
	public void testX() {

		var t = DuckTable.of(db().getDataSource(), "book");

		var c = t.getJdbcClient();

		c.sql("create table book( name varchar(30), author varchar(30))").update();

		c.sql("insert into book (name,author) values (:name,:author)").param("name", "Moby Dick")
				.param("author", "Herman Melville").update();

		t.selectPretty(System.out);
		
	
	}

	@Test
	public void testUnwrap() throws SQLException {
		var ds = db().getDataSource();

		Assertions.assertThat(ds.getConnection().unwrap(Connection.class)).isInstanceOf(DuckDBConnection.class);
		Assertions.assertThat(ds.getConnection().unwrap(DuckDBConnection.class)).isInstanceOf(DuckDBConnection.class);
	}
}
