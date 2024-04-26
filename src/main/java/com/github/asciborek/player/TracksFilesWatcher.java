package com.github.asciborek.player;

import com.github.asciborek.FxPlayer.CloseApplicationEvent;
import com.github.asciborek.player.PlayerEvent.PlaylistClearedEvent;
import com.github.asciborek.player.PlayerEvent.TrackAddedEvent;
import com.github.asciborek.player.PlayerEvent.TracksAddedEvent;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TracksFilesWatcher {

  private static final Logger LOG = LoggerFactory.getLogger(TracksFilesWatcher.class);

  private final EventBus eventBus;
  private final ExecutorService executorService;
  private final WatchService watchService;
  private final ConcurrentMap<Path, WatchDirectoryTask> watchDirectoriesTasksPool;
  private final long watchDirectoryTaskSuspensionTime;

  public TracksFilesWatcher(final EventBus eventBus, final ExecutorService executorService) {
    this(eventBus, executorService, new ConcurrentHashMap<>(), 1000);
  }

  public TracksFilesWatcher(final EventBus eventBus, final ExecutorService executorService,
      final ConcurrentMap<Path, WatchDirectoryTask> watchDirectoryWorkPool, long watchDirectoryTaskSuspensionTime) {
    this.eventBus = eventBus;
    this.executorService = executorService;
    this.watchService = createWatchService();
    this.watchDirectoriesTasksPool = watchDirectoryWorkPool;
    this.watchDirectoryTaskSuspensionTime = watchDirectoryTaskSuspensionTime;
  }

  @Subscribe
  void onTrackAdded(final TrackAddedEvent trackAddedEvent) {
    executorService.submit(() -> {
      var directoryPath = trackAddedEvent.track().filePath().getParent();
      registerWatchDirectoryTask(directoryPath);
    });
  }

  @Subscribe
  void onTracksAdded(final TracksAddedEvent tracksAddedEvent) {
    executorService.submit(() -> {
      var newDirectories = tracksAddedEvent.tracks().stream()
          .map(track -> track.filePath().getParent())
          .collect(Collectors.toSet());
      newDirectories.forEach(this::registerWatchDirectoryTask);
    });
  }

  @Subscribe
  void onPlaylistClearedEvent(PlaylistClearedEvent playlistClearedEvent) {
    LOG.info("received PlaylistClearedEvent, cancelling all watch WatchDirectoryTask tasks");
    cancelAllWatchTasks();
  }

  @Subscribe
  void onCloseApplicationEvent(CloseApplicationEvent closeApplicationEvent) throws IOException {
    LOG.info("received closeApplicationEvent, cancelling all WatchDirectoryTask tasks");
    cancelAllWatchTasks();
    watchService.close();
  }

  private WatchService createWatchService () {
    try {
      FileSystem fileSystem = FileSystems.getDefault();
      return fileSystem.newWatchService();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create WatchService", e);
    }
  }

  private void registerWatchDirectoryTask(Path directoryPath) {
    watchDirectoriesTasksPool.computeIfAbsent(directoryPath, this::watchDirectory);
  }

  private WatchDirectoryTask watchDirectory(Path directoryPath) {
    var task = new WatchDirectoryTask(watchService, eventBus, directoryPath, watchDirectoryTaskSuspensionTime);
    executorService.submit(task);
    return task;
  }

  private void cancelAllWatchTasks() {
    watchDirectoriesTasksPool.values().forEach(WatchDirectoryTask::cancel);
  }

  static final class WatchDirectoryTask implements Runnable {

    private final WatchService watchService;
    private final EventBus eventBus;
    private final Path directoryPath;
    private final long taskSuspensionTime;
    private volatile boolean running = true;

    public WatchDirectoryTask(WatchService watchService, EventBus eventBus, Path directoryPath,
        long taskSuspensionTime) {
      this.watchService = watchService;
      this.eventBus = eventBus;
      this.directoryPath = directoryPath;
      this.taskSuspensionTime = taskSuspensionTime;
    }

    @Override
    public void run() {
      var watchKey = registerWatchKey();
      while (running && !Thread.currentThread().isInterrupted()) {
        Set<Path> deletedPaths = new HashSet<>();
        try {
            Thread.sleep(taskSuspensionTime);
            for (WatchEvent<?> event : watchKey.pollEvents()) {
              LOG.info("Received event {}", event);
              if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                LOG.info("received entry delete event, context: {}", event.context());
                var trackFileName = event.context().toString();
                var trackPath = directoryPath.resolve(trackFileName);
                deletedPaths.add(trackPath);
              }
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          if (!deletedPaths.isEmpty()) {
            eventBus.post(new TracksFilesDeletedEvent(ImmutableSet.copyOf(deletedPaths)));
            deletedPaths.clear();
          }
          LOG.info("resetting watch key {}", watchKey);
          watchKey.reset();
        }
        watchKey.cancel();
      }

    public void cancel() {
      running = false;
    }

    private  WatchKey registerWatchKey() {
      try {
        return directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_DELETE);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public record TracksFilesDeletedEvent(Set<Path> trackPaths) {}
}
