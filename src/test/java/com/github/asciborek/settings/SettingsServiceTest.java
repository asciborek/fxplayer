package com.github.asciborek.settings;

import static com.github.asciborek.settings.SqliteSettingsStorage.SETTINGS_TABLE;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.TestUtils;
import com.github.asciborek.util.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class SettingsServiceTest {

  private final Path dbFile = TestUtils.getTempSqliteFile();
  private final HikariDataSource dataSource = TestUtils.createSqliteDatasource(dbFile);

  @BeforeAll
  void initDb() {
    TestUtils.initDb(dataSource);
  }

  @Test
  @DisplayName("load the default settings if the settings row doesn't exist")
  void loadDefaultSettingsIfSettingsRowDoesNotExist() {
    var settingsService = new SettingsService(creteSqliteSettingsStorage());

    //default volume value is max
    assertThat(SettingsService.MAX_VOLUME_LEVEL).isEqualTo(settingsService.getVolume());
    //default directory should be home directory
    assertThat(settingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    assertThat(settingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
    assertThat(settingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(new File(FileUtils.getUserHome()));
  }

  @Test
  @DisplayName("save and read settings in database")
  void saveAndReadSettingsInDatabase() {
    var expectedVolumeValue = 0.3;
    var expectedAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var expectedAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var expectedOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Vector");

    var settingsService = new SettingsService(creteSqliteSettingsStorage());
    settingsService.setVolume(expectedVolumeValue);
    settingsService.setAddTrackFileChooserInitDirectory(expectedAddTrackFileChooserDirectory);
    settingsService.setAddDirectoryDirectoryChooserInitDirectory(expectedAddDirectoryDirectoryChooserDirectory);
    settingsService.setOpenFileFileChooserInitDirectory(expectedOpenFileFileChooserDirectory);
    settingsService.onCloseApplicationEvent(new CloseApplicationEvent());

    var newSettingsService = new SettingsService(creteSqliteSettingsStorage());
    assertThat(newSettingsService.getVolume()).isEqualTo(expectedVolumeValue);
    assertThat(newSettingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(expectedAddDirectoryDirectoryChooserDirectory);
    assertThat(newSettingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(expectedAddTrackFileChooserDirectory);
    assertThat(newSettingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(expectedOpenFileFileChooserDirectory);
  }

  @Test
  @DisplayName("update settings if the settings row already exists")
  void updateSettingsIfRowExists() {
    var initVolumeValue = 0.3;
    var initAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var initAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var initOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Vector");

    var settingsService = new SettingsService(creteSqliteSettingsStorage());
    settingsService.setVolume(initVolumeValue);
    settingsService.setAddTrackFileChooserInitDirectory(initAddTrackFileChooserDirectory);
    settingsService.setAddDirectoryDirectoryChooserInitDirectory(initAddDirectoryDirectoryChooserDirectory);
    settingsService.setOpenFileFileChooserInitDirectory(initOpenFileFileChooserDirectory);
    settingsService.onCloseApplicationEvent(new CloseApplicationEvent());

    var finalVolumeValue = 0.5;
    var finalAddTrackFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Affinity");
    var finalAddDirectoryDirectoryChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Virus");
    var finalOpenFileFileChooserDirectory = new File(FileUtils.getUserHome() + "/Music/Haken/Restoration");

    var newSettingsService = new SettingsService(creteSqliteSettingsStorage());
    newSettingsService.setVolume(finalVolumeValue);
    newSettingsService.setAddDirectoryDirectoryChooserInitDirectory(finalAddDirectoryDirectoryChooserDirectory);
    newSettingsService.setAddTrackFileChooserInitDirectory(finalAddTrackFileChooserDirectory);
    newSettingsService.setOpenFileFileChooserInitDirectory(finalOpenFileFileChooserDirectory);
    newSettingsService.onCloseApplicationEvent(new CloseApplicationEvent());

    var finalSettingsService = new SettingsService(creteSqliteSettingsStorage());
    assertThat(finalSettingsService.getVolume()).isEqualTo(finalVolumeValue);
    assertThat(finalSettingsService.getAddTrackFileChooserInitDirectory()).isEqualTo(finalAddTrackFileChooserDirectory);
    assertThat(finalSettingsService.getDirectoryDirectoryChooserInitDirectory()).isEqualTo(finalAddDirectoryDirectoryChooserDirectory);
    assertThat(finalSettingsService.getOpenFileFileChooserInitDirectory()).isEqualTo(finalOpenFileFileChooserDirectory);
  }

  @AfterEach
  void removeSettingsAfterTest() {
    var context = DSL.using(dataSource, SQLDialect.SQLITE);
    context.deleteFrom(DSL.table(SETTINGS_TABLE)).execute();
  }

  @AfterAll
  void tearDownDb() throws IOException {
    dataSource.close();
    Files.delete(dbFile);
  }

  private SqliteSettingsStorage creteSqliteSettingsStorage() {
    return new SqliteSettingsStorage(dataSource);
  }

}
