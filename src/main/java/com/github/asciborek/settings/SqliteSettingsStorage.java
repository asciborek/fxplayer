package com.github.asciborek.settings;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import java.util.Optional;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

final class SqliteSettingsStorage implements SettingsStorage {

  static final String SETTINGS_TABLE = "settings";

  private static final String VOLUME_LEVEL_COLUMN = "volume_level";
  private static final String ADD_DIRECTORY_DIRECTORY_CHOOSER_INIT_DIRECTORY_COLUMN = "add_directory_directory_chooser_init_directory";
  private static final String ADD_TRACK_FILE_CHOOSER_INIT_DIRECTORY_COLUMN = "add_track_file_chooser_init_directory";
  private static final String OPEN_FILE_FILE_CHOOSER_INIT_DIRECTORY_COLUMN = "open_file_file_chooser_init_directory";
  private static final String ID_COLUMN = "id";
  private static final int SETTINGS_ROW_ID = 1;

  private final DataSource dataSource;

  public SqliteSettingsStorage(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void save(Settings settings) {
    var context =  DSL.using(dataSource, SQLDialect.SQLITE);
    context.transaction(configuration -> {
      DSLContext transactionContext = DSL.using(configuration);
      int rowsCount = countRowsWithSettingsRowId(transactionContext);
      if (rowsCount == 0) {
        insertSettings(settings, transactionContext);
      } else {
        updateSettingsRow(settings, transactionContext);
      }
    });
  }

  @Override
  public Optional<Settings> loadSettings() {
    var context =  DSL.using(dataSource, SQLDialect.SQLITE);
    return context.select(field(VOLUME_LEVEL_COLUMN, Double.class),
          field(ADD_DIRECTORY_DIRECTORY_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
          field(ADD_TRACK_FILE_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
          field(OPEN_FILE_FILE_CHOOSER_INIT_DIRECTORY_COLUMN, String.class))
        .from(table(SETTINGS_TABLE))
        .where(field(ID_COLUMN).eq(SETTINGS_ROW_ID))
        .fetchOptional()
        .map(this::fromRecord);
  }

  private void insertSettings(Settings settings, DSLContext transactionContext) {
    transactionContext.insertInto(table(SETTINGS_TABLE), field(ID_COLUMN, Integer.class),
        field(VOLUME_LEVEL_COLUMN, Double.class),
        field(ADD_DIRECTORY_DIRECTORY_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
        field(ADD_TRACK_FILE_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
        field(OPEN_FILE_FILE_CHOOSER_INIT_DIRECTORY_COLUMN, String.class)
    ).values(
        SETTINGS_ROW_ID,
        getVolumeLevel(settings),
        getAddDirectoryDirectoryChooserInitDirectory(settings),
        getAddTrackFileChooserInitDirectory(settings),
        getOpenFileFileChooserInitDirectory(settings)
    ).execute();
  }

  private void updateSettingsRow(Settings settings, DSLContext transactionContext) {
    transactionContext.update(table(SETTINGS_TABLE))
        .set(field(VOLUME_LEVEL_COLUMN, Double.class), getVolumeLevel(settings))
        .set(field(ADD_DIRECTORY_DIRECTORY_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
            getAddDirectoryDirectoryChooserInitDirectory(settings))
        .set(field(OPEN_FILE_FILE_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
            getOpenFileFileChooserInitDirectory(settings))
        .set(field(ADD_TRACK_FILE_CHOOSER_INIT_DIRECTORY_COLUMN, String.class),
            getAddTrackFileChooserInitDirectory(settings))
        .where(field(ID_COLUMN).eq(SETTINGS_ROW_ID))
        .execute();
  }

  private Double getVolumeLevel(Settings settings) {
    return settings.getVolumeLevel().orElse(null);
  }

  private String getAddDirectoryDirectoryChooserInitDirectory(Settings settings) {
    return settings.getAddDirectoryDirectoryChooserInitDirectory().orElse(null);
  }

  private String getAddTrackFileChooserInitDirectory(Settings settings) {
    return settings.getAddTrackFileChooserInitDirectory().orElse(null);
  }

  private String getOpenFileFileChooserInitDirectory(Settings settings) {
    return settings.getOpenFileFileChooserInitDirectory().orElse(null);
  }

  private Settings fromRecord(Record4<Double, String, String, String> record) {
    var settings = new Settings();
    settings.setVolumeLevel(record.get(0, Double.class));
    settings.setAddDirectoryDirectoryChooserInitDirectory(record.get(1, String.class));
    settings.setAddTrackFileChooserInitDirectory(record.get(2, String.class));
    settings.setOpenFileFileChooserInitDirectory(record.get(3, String.class));
    return settings;
  }

  private Integer countRowsWithSettingsRowId(DSLContext transactionContext) {
    return transactionContext.selectCount()
        .from(SETTINGS_TABLE)
        .where(field(ID_COLUMN).eq(SETTINGS_ROW_ID))
        .fetchOptional()
        .map(integerRecord1 -> integerRecord1.get(0, Integer.class))
        .orElse(0);
  }
}
