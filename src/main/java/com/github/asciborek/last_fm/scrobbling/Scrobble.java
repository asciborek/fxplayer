package com.github.asciborek.last_fm.scrobbling;

public record Scrobble(String artist, String album, String track, long timestamp) {
}
