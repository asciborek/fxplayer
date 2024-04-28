package com.github.asciborek;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.asciborek.util.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

public final class TestUtils {

  private TestUtils() {}

  public static Path getTempSqliteFile() {
    return Paths.get(FileUtils.getTempDirectory(),
        STR."fx-database\{Instant.now().toEpochMilli()}.db");
  }

  public static void createFiles(Path... paths) throws IOException{
    for (Path path : paths) {
        Files.createFile(path);
    }
  }

  public static void deleteFiles(Path... paths) throws IOException {
    for (Path path : paths) {
      Files.deleteIfExists(path);
    }
  }

  public static ObjectReader objectReader(){
    var objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.reader();
  }

  public static HikariDataSource createSqliteDatasource(Path dbFile) {
    String jdbcUrl = STR."jdbc:sqlite:\{dbFile}";
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
