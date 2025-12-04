# bx

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.if6was9/bx)](https://central.sonatype.com/artifact/io.github.if6was9/bx) [![Build](https://github.com/if6was9/bx/actions/workflows/build.yml/badge.svg)](https://github.com/if6was9/bx/actions)

BX is a collection of frequently-used utility code.  It integrates the following 3rd party packages:

* [guava](https://github.com/google/guava/blob/master/README.md)
* [unirest-java](https://github.com/Kong/unirest-java/blob/main/README.md)
* [jackson v3](https://github.com/FasterXML/jackson/blob/main/README.md)
* [spring-jdbc](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
* [HikariCP](https://github.com/brettwooldridge/HikariCP/blob/dev/README.md)
* [DuckDB](https://duckdb.org/)
<<<<<<< HEAD
=======
<<<<<<< Updated upstream
=======
>>>>>>> 695a75a (update)


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
```

Count rows:
```java
table.rowCount();
```

Rename 'employee' table to 'worker':

```java
table.rename("employee","worker");
```

Rename a column:
```java
table.rename("id","employee_id");
```

Drop a column:
```java
table.dropColumn("last_name");
```

Drop all columns except those specified:

```java
table.dropColumnsExcept("id","first_name","last_name");
```

Rename the table:
```java
var newTable = table.renameTable("literary_work");
```

Create a table, insert a row, and look at the contents:

```java
c.sql("create table book( name varchar(30), author varchar(30))").update();

		c.sql("insert into book (name,author) values (:name,:author)").param("name", "Moby Dick")
				.param("author", "Herman Melville").update();

		t.selectPretty(System.out);
```

```shell
┌─────────────┬───────────────────┐
│    name     │      author       │
│   varchar   │      varchar      │
├─────────────┼───────────────────┤
│  Moby Dick  │  Herman Melville  │
├─────────────┴───────────────────┤
│ 1 row                           │
└─────────────────────────────────┘
```


Add two more books using DuckDB's appender interface:
```java
var appender = t.createAppender();

appender.beginRow();
appender.append("Thus Spoke Zarathustra");
appender.append("Friedrich Nietzsche");
appender.endRow();

appender.beginRow();
appender.append("As I Lay Dying");
appender.append("William Faulkner");
appender.endRow();

appender.close();
		
t.selectPretty(System.out);
```

Output:

```shell
┌──────────────────────────┬───────────────────────┐
│           name           │        author         │
│         varchar          │        varchar        │
├──────────────────────────┼───────────────────────┤
│  Moby Dick               │  Herman Melville      │
│  Thus Spoke Zarathustra  │  Friedrich Nietzsche  │
│  As I Lay Dying          │  William Faulkner     │
├──────────────────────────┴───────────────────────┤
│ 3 rows                                           │
└──────────────────────────────────────────────────┘
```

