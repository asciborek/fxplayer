package com.github.asciborek.local_statistics;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.github.asciborek.player.PlayerEvent.TrackPlayedEvent;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

final class PlayedTracksHistoryCollector {

  private final ExecutorService executorService;
  private final DSLContext dslContext;

  PlayedTracksHistoryCollector(ExecutorService executorService, DataSource dataSource) {
    this.executorService = executorService;
    dslContext = DSL.using(dataSource, SQLDialect.SQLITE);
  }

  @SuppressWarnings("unused")
  @Subscribe
  public void onTrackPlayed(TrackPlayedEvent trackPlayedEvent) {
    executorService.submit(() -> saveTrackToStatistics(trackPlayedEvent));
  }

  private void saveTrackToStatistics(TrackPlayedEvent trackPlayedEvent) {
    dslContext.insertInto(table("played_tracks_history"),
            field("artist", String.class),
            field("album", String.class),
            field("track_title", String.class),
            field("played_at", Long.class)
        ).values(
            trackPlayedEvent.track().artist(),
            trackPlayedEvent.track().album(),
            trackPlayedEvent.track().title(),
            trackPlayedEvent.timestamp()
        ).execute();
  }
}
