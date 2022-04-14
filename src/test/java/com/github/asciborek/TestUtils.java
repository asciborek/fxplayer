package com.github.asciborek;

import com.github.asciborek.util.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class TestUtils {

  private TestUtils() {}

  public static Path getTempSqliteFile() {
    return Paths.get(FileUtils.getTempDirectory(), "fx-database" + Instant.now().toEpochMilli() + ".db");
  }

  public static HikariDataSource createSqliteDatasource(Path dbFile) {
    String jdbcUrl =  "jdbc:sqlite:" + dbFile;
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(jdbcUrl);
    config.setMaximumPoolSize(1);
    return new HikariDataSource(config);
  }

  public static void initDb(DataSource dataSource) {
    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .load();
    flyway.migrate();
  }
}
