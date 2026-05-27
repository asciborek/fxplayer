package com.github.asciborek.last_fm.scrobbling;


import com.github.asciborek.TestUtils;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScrobblesDaoTest {

  private Path dbFile;
  private ScrobblesDao scrobblesDao;

  @BeforeEach
  void setUp() {
    dbFile = TestUtils.getTempSqliteFile();
    HikariDataSource dataSource = TestUtils.createSqliteDatasource(dbFile);
    TestUtils.initDb(dataSource);
    scrobblesDao = new ScrobblesDao(dataSource, 5);
  }

  @AfterEach
  void cleanUp() throws Exception {
    Files.delete(dbFile);
  }

  @Test
  void insertLoadAndDeleteScrobbles() {
    Instant timestamp = Instant.now();

    Scrobble scrobble1 = new Scrobble("Haken", "Restoration", "Darkest Light", timestamp.getEpochSecond());
    Scrobble scrobble2 = new Scrobble("Haken", "Restoration", "Earthlings",  toTimestamp(timestamp, Duration.ofMinutes(3)));
    Scrobble scrobble3 = new Scrobble("Haken", "Restoration", "Crystallised",  toTimestamp(timestamp, Duration.ofMinutes(7)));
    insertScrobbles(scrobble1, scrobble2, scrobble3);

    List<Scrobble> scrobblesBeforeDelete = scrobblesDao.getNewestScrobbles();
    scrobblesDao.deleteByTimestampLessThanEqual(scrobble3.timestamp());
    List<Scrobble> scrobblesAfterDelete = scrobblesDao.getNewestScrobbles();

    Assertions.assertThat(scrobblesBeforeDelete).hasSize(3);
    Assertions.assertThat(scrobblesBeforeDelete).containsExactly(scrobble3, scrobble2, scrobble1);
    Assertions.assertThat(scrobblesAfterDelete).isEmpty();

  }

  @Test
  void insertAndLoadScrobblesWithBatchLimit() {
    Instant timestamp = Instant.now();

    Scrobble scrobble1 = new Scrobble("Porcupine Tree", "Up the Downstair", "What You Are Listening To...", timestamp.getEpochSecond());
    Scrobble scrobble2 = new Scrobble("Porcupine Tree", "Up the Downstair", "Synesthesia", toTimestamp(timestamp, Duration.ofMinutes(5)));
    Scrobble scrobble3 = new Scrobble("Porcupine Tree", "Up the Downstair", "Monuments Burn into Moments", toTimestamp(timestamp, Duration.ofMinutes(11)));
    Scrobble scrobble4 = new Scrobble("Porcupine Tree", "Up the Downstair", "Always Never", toTimestamp(timestamp, Duration.ofMinutes(15)));
    Scrobble scrobble5 = new Scrobble("Porcupine Tree", "Up the Downstair", "Up the Downstair", toTimestamp(timestamp, Duration.ofMinutes(18)));
    Scrobble scrobble6 = new Scrobble("Porcupine Tree", "Up the Downstair", "Not Beautiful Anymore", toTimestamp(timestamp, Duration.ofMinutes(29)));
    Scrobble scrobble7 = new Scrobble("Porcupine Tree", "Up the Downstair", "Siren", toTimestamp(timestamp, Duration.ofMinutes(33)));
    Scrobble scrobble8 = new Scrobble("Porcupine Tree", "Up the Downstair", "Small Fish", toTimestamp(timestamp, Duration.ofMinutes(36)));
    Scrobble scrobble9 = new Scrobble("Porcupine Tree", "Up the Downstair", "Burning Sky", toTimestamp(timestamp, Duration.ofMinutes(39)));
    Scrobble scrobble10 = new Scrobble("Porcupine Tree", "Up the Downstair", "Fadeaway", toTimestamp(timestamp, Duration.ofMinutes(50)));

    insertScrobbles(scrobble1, scrobble2, scrobble3, scrobble4, scrobble5, scrobble6, scrobble7, scrobble8, scrobble9, scrobble10);

    List<Scrobble> scrobbles = scrobblesDao.getNewestScrobbles();
    Assertions.assertThat(scrobbles).hasSize(5);
    Assertions.assertThat(scrobbles).containsExactly(scrobble10, scrobble9, scrobble8, scrobble7, scrobble6);
  }

  private long toTimestamp(Instant instant, Duration addedTime) {
    return instant.plus(addedTime).getEpochSecond();
  }

  private void insertScrobbles(Scrobble... scrobbles) {
   for (Scrobble scrobble: scrobbles) {
     scrobblesDao.insertScrobble(scrobble);
   }
  }
}