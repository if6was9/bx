# bx

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.if6was9/bx)](https://central.sonatype.com/artifact/io.github.if6was9/bx) [![Build](https://github.com/if6was9/bx/actions/workflows/build.yml/badge.svg)](https://github.com/if6was9/bx/actions)

BX is a collection of frequently-used utility code.  It integrates the following 3rd party packages:

* [guava](https://github.com/google/guava/blob/master/README.md)
* [unirest-java](https://github.com/Kong/unirest-java/blob/main/README.md)
* [jackson v3](https://github.com/FasterXML/jackson/blob/main/README.md)
* [spring-jdbc](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
* [HikariCP](https://github.com/brettwooldridge/HikariCP/blob/dev/README.md)
* [DuckDB](https://duckdb.org/)


# DuckDB Support

DuckDB is a fantastic embedded analytics database. In addition to analytics queries, it's very useful
for CSV import/export and in-memory data transformation.

## Usage

### Obtaining DuckDB DataSource

```java
// Create a DataSource to access an in-memory DuckDB instance
var ds = DuckDataSource.createInMemory();
    
// Use Spring JDBC to access the database
var client = JdbcClient.create(ds);
```

In the example above, the DataSource is special in that it only contains a single connection and the connection
is protected from being closed. This enables it to be used in a standard JDBC pool without the databse being destroyed
after each operation.

To close the databse and the DataSource, the DataSource that is returned has a `close()` method that can be called.

```java
ds.close(); // closes the DataSource and the database
```

To obtain a file-based DuckDB DataSource:
```java
var ds = DuckDataSource.create("jdbc:duckdb:./mydb.duckdb");
```

## DuckTable

There is a class, DuckTable that provides many convenience methods for working with DuckDB:

```java

var table = DuckTable.of(dataSource,"employee");


// count rows
table.rowCount();

// rename 'employee' table to 'worker'
table.rename("employee","worker");

// rename a column
table.rename("id","employee_id");

// drop a column
table.dropColumn("last_name");

