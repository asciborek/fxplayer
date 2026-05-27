package com.github.asciborek.last_fm.scrobbling;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.google.inject.Inject;
import java.util.List;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public final class ScrobblesDao {

  private static final String SCROBBLES_TABLE = "scrobbles";
  private static final String ARTIST_COLUMN = "artist";
  private static final String ALBUM_COLUMN = "album";
  private static final String TRACK_COLUMN =  "track";
  private static final String TIMESTAMP_COLUMN =  "timestamp";

  private static final int LAST_FM_BATCH_MAX_SIZE = 50;

  private final DSLContext context;
  private final int batchSize;

  @Inject
  public ScrobblesDao(DataSource dataSource) {
    this(dataSource, LAST_FM_BATCH_MAX_SIZE);
  }

  public ScrobblesDao(DataSource dataSource, int batchSize){
    context = DSL.using(dataSource, SQLDialect.SQLITE);
    this.batchSize = batchSize;
  }

  public void insertScrobble(Scrobble scrobble) {
    context.insertInto(table(SCROBBLES_TABLE),
        field(ARTIST_COLUMN, String.class),
        field(ALBUM_COLUMN, String.class),
        field(TRACK_COLUMN, String.class),
        field(TIMESTAMP_COLUMN, Long.class)
    ).values(
        scrobble.artist(),
        scrobble.album(),
        scrobble.track(),
        scrobble.timestamp()
    ).execute();
  }

  int deleteByTimestampLessThanEqual(long timestamp) {
    return context
        .deleteFrom(table(SCROBBLES_TABLE))
        .where(field(TIMESTAMP_COLUMN).le(timestamp))
        .execute();
  }

  public List<Scrobble> getNewestScrobbles() {
    Result<Record4<String, String, String, Long>> result = context.select(field(ARTIST_COLUMN, String.class), field(ALBUM_COLUMN, String.class), field(TRACK_COLUMN, String.class), field(TIMESTAMP_COLUMN, Long.class))
        .from(table(SCROBBLES_TABLE))
        .orderBy(field(TIMESTAMP_COLUMN).desc())
        .limit(batchSize)
        .fetch();
    return result.stream().map(this::mapRecord).toList();
  }

  private Scrobble mapRecord(Record4<String, String, String, Long> record) {
    return new Scrobble(
        record.component1(),
        record.component2(),
        record.component3(),
        record.component4()
    );
  }

}
