package com.github.asciborek.player;

import com.github.asciborek.GenericTestEventListener;
import com.github.asciborek.TestUtils;
import com.github.asciborek.metadata.Track;
import com.github.asciborek.player.PlayerEvent.TracksAddedEvent;
import com.github.asciborek.player.TracksFilesWatcher.TracksFilesDeletedEvent;
import com.github.asciborek.player.TracksFilesWatcher.WatchDirectoryTask;
import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class TracksFilesWatcherTest {

  private static final long WATCH_TIME_INTERVAL = 100;

  private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
  private final EventBus eventBus = new EventBus();
  private final ConcurrentMap<Path, WatchDirectoryTask> watchDirectoryTasks = new ConcurrentHashMap<>();
  private final TracksFilesWatcher tracksFilesWatcher = new TracksFilesWatcher(
      eventBus, executorService, watchDirectoryTasks, WATCH_TIME_INTERVAL
  );
  private final GenericTestEventListener<TracksFilesDeletedEvent> testListener = new GenericTestEventListener<>(TracksFilesDeletedEvent.class);

  @BeforeAll
  void setUp() {
    eventBus.register(tracksFilesWatcher);
    eventBus.register(testListener);
  }

  @AfterAll
  void tearDown() throws Exception {
    executorService.shutdownNow();
  }

  @AfterEach
  void clearEventsFromTestListener() {
    testListener.clearEvents();
  }

  @Test
  void sendTracksFilesDeletedEventOnDeletedTracksInDirectory(@TempDir(cleanup = CleanupMode.NEVER)
      Path tempDir) throws IOException {
    //given
    var firstTrack = tempDir.resolve("first.mp3");
    var secondTrack= tempDir.resolve("second.mp4");
    var thirdTrack = tempDir.resolve("third.mp3");
    var fourthTrack = tempDir.resolve("fourth.mp3");
    var tracksCollection = pathsToTracks(firstTrack, secondTrack, thirdTrack, fourthTrack);
    TestUtils.createFiles(firstTrack, secondTrack, thirdTrack, fourthTrack);

    //when
    eventBus.post(new TracksAddedEvent(tracksCollection));
    awaitForWatchTrackFilesTaskRegistration(tempDir);
    TestUtils.deleteFiles(firstTrack, fourthTrack);

    //then
    Awaitility.await()
        .pollDelay(Duration.ofMillis(200))
        .pollInterval(Duration.ofMillis(50))
        .atMost(Duration.ofSeconds(3))
        .untilAsserted(() -> {
          List<TracksFilesDeletedEvent> events = testListener.getEventsSnapshot();
          TracksFilesDeletedEvent deletedEvent = events.getFirst();
          Set<Path> deletedPaths = deletedEvent.trackPaths();
          Assertions.assertThat(events).hasSize(1);
          Assertions.assertThat(deletedPaths).containsExactlyInAnyOrder(firstTrack, fourthTrack);
        });
  }

  @Test
  void sendTracksFilesDeletedEventOnDeletedDirectory(@TempDir(cleanup = CleanupMode.NEVER) Path tempDir) throws IOException {
    //given
    var firstTrack = tempDir.resolve("first.mp3");
    var secondTrack= tempDir.resolve("second.mp4");
    var tracksCollection = pathsToTracks(firstTrack, secondTrack);
    TestUtils.createFiles(firstTrack, secondTrack);

    //when
    eventBus.post(new TracksAddedEvent(tracksCollection));
    awaitForWatchTrackFilesTaskRegistration(tempDir);
    TestUtils.deleteFiles(firstTrack, secondTrack, tempDir);

    //then
    Awaitility.await()
        .pollDelay(Duration.ofMillis(200))
        .pollInterval(Duration.ofMillis(50))
        .atMost(Duration.ofSeconds(3))
        .untilAsserted(() -> {
          List<TracksFilesDeletedEvent> events = testListener.getEventsSnapshot();
          TracksFilesDeletedEvent deletedEvent = events.getFirst();
          Set<Path> deletedPaths = deletedEvent.trackPaths();
          Assertions.assertThat(events).hasSize(1);
          Assertions.assertThat(deletedPaths).containsExactlyInAnyOrder(firstTrack, secondTrack);
        });


  }

  private void awaitForWatchTrackFilesTaskRegistration(Path tempDir) {
    Awaitility.await()
        .atLeast(Duration.ofMillis(1))
        .pollInterval(Duration.ofMillis(1))
        .until(() -> watchDirectoryTasks.containsKey(tempDir));
  }

  private Set<Track> pathsToTracks(Path... paths) {
    return Set.of(paths)
        .stream()
        .map(path -> Track.builder().withFilePath(path).build())
        .collect(Collectors.toUnmodifiableSet());
  }

}
