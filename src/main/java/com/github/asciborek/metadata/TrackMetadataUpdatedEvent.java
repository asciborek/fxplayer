package com.github.asciborek.metadata;

public record TrackMetadataUpdatedEvent(Track oldTrack, Track newTrack) {}
