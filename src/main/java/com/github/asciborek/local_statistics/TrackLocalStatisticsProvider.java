package com.github.asciborek.local_statistics;

import static org.jooq.impl.DSL.field;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

final class TrackLocalStatisticsProvider {

  private static final String COUNT = "COUNT(*)";
  private static final String MIN_PLAYED_AT = "min(played_at)";
  private static final String MAX_PLAYED_AT = "max(played_at)";
  private static final String PLAYED_TRACKS_HISTORY_TABLE = "played_tracks_history";
  private static final String ARTIST_COLUMN = "artist";

  private final ExecutorService executorService;
  private final DSLContext context;

  TrackLocalStatisticsProvider(ExecutorService executorService, DataSource dataSource) {
    this.executorService = executorService;
    this.context = DSL.using(dataSource, SQLDialect.SQLITE);
  }

  CompletableFuture<TrackLocalStatistics> getTrackLocalStatistics(String artist, String title) {
    return CompletableFuture.supplyAsync(() -> loadTrackStatistics(artist, title), executorService);
  }

  private TrackLocalStatistics loadTrackStatistics(String artist, String title) {
    var record = context.select(field(COUNT, Long.class), field(MIN_PLAYED_AT, Long.class), field(MAX_PLAYED_AT, Long.class))
              .from(DSL.table(PLAYED_TRACKS_HISTORY_TABLE))
              .where(field(ARTIST_COLUMN, String.class).eq(artist)
              .and(field("track_title", String.class).eq(title)))
              .fetchOne();
    return new TrackLocalStatistics(record.get(COUNT, Long.class), record.get(MIN_PLAYED_AT, Long.class), record.get(MAX_PLAYED_AT, Long.class));

  }
}
