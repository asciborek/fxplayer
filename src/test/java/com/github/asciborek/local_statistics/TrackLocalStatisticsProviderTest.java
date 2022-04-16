package com.github.asciborek.local_statistics;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.asciborek.TestUtils;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrackLocalStatisticsProviderTest {

  private Path dbFile;
  private HikariDataSource dataSource;
  private ExecutorService executorService;

  private static final Track IN_MEMORIAM = Track.builder()
      .withArtist("Haken")
      .withAlbum("The Mountain")
      .withTitle("In Memoriam")
      .withDuration(Duration.ofSeconds(4* 60 + 42))
      .withFilePath(null)
      .build();

  private static final Track THE_ARCHITECT = Track.builder()
      .withArtist("Haken")
      .withAlbum("Affinity")
      .withTitle("The Architect")
      .withDuration(Duration.ofSeconds(15* 60 + 42))
      .withFilePath(null)
      .build();

  @BeforeEach
  void setUp() {
    dbFile = TestUtils.getTempSqliteFile();
    dataSource = TestUtils.createSqliteDatasource(dbFile);
    TestUtils.initDb(dataSource);
    executorService = Executors.newSingleThreadExecutor();
  }

  @Test
  void shouldReadPlayedTracksStatistics() throws Exception {
    //given
    final PlayedTracksHistoryCollector collector = createCollector();
    final TrackLocalStatisticsProvider trackLocalStatisticsProvider = createTrackLocalStatisticsProvider();
    final Instant firstPlayed = Instant.now();
    final Instant secondPlayed = firstPlayed.plus( 1, ChronoUnit.HOURS);
    final Instant thirdPlayed = secondPlayed.plus(1, ChronoUnit.HOURS);

    collector.onTrackPlayed(new TrackPlayedEvent(IN_MEMORIAM, firstPlayed));
    collector.onTrackPlayed(new TrackPlayedEvent(IN_MEMORIAM, secondPlayed));
    collector.onTrackPlayed(new TrackPlayedEvent(IN_MEMORIAM, thirdPlayed));
    collector.onTrackPlayed(new TrackPlayedEvent(THE_ARCHITECT, thirdPlayed.plus(1, ChronoUnit.HOURS)));

    //when
    var statistics = trackLocalStatisticsProvider.getTrackLocalStatistics(IN_MEMORIAM.artist(), IN_MEMORIAM.title()).get();

    //then
    assertThat(statistics.totalCount()).isEqualTo(3);
    assertThat(statistics.firstPlayedEpochMilli()).isEqualTo(firstPlayed.toEpochMilli());
    assertThat(statistics.lastPlayedEpochMilli()).isEqualTo(thirdPlayed.toEpochMilli());
    assertThat(statistics.firstPlayed()).isNotEmpty();
    assertThat(statistics.lastPlayed()).isNotEmpty();
  }

  @Test
  void shouldReadZeroTotalCountForTrackNotPlayedYet() throws Exception {
    //given
    final TrackLocalStatisticsProvider trackLocalStatisticsProvider = createTrackLocalStatisticsProvider();

    //when
    var statistics = trackLocalStatisticsProvider.getTrackLocalStatistics(IN_MEMORIAM.artist(), IN_MEMORIAM.title()).get();

    //then
    assertThat(statistics.totalCount()).isEqualTo(0);
    assertThat(statistics.firstPlayedEpochMilli()).isNull();
    assertThat(statistics.lastPlayedEpochMilli()).isNull();
    assertThat(statistics.firstPlayed()).isEmpty();
    assertThat(statistics.lastPlayed()).isEmpty();
  }

  @AfterEach
  void tearDown() throws IOException {
    dataSource.close();
    Files.delete(dbFile);
  }

  private PlayedTracksHistoryCollector createCollector() {
    return new PlayedTracksHistoryCollector(executorService, dataSource);
  }

  private TrackLocalStatisticsProvider createTrackLocalStatisticsProvider() {
    return new TrackLocalStatisticsProvider(executorService, dataSource);
  }


}
