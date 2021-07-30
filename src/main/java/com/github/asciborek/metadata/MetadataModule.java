package com.github.asciborek.metadata;

import com.google.inject.AbstractModule;

public final class MetadataModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TrackMetadataProvider.class).toInstance(new TrackMetadataProvider());
    bind(TrackMetadataUpdater.class).toInstance(new TrackMetadataUpdater());
  }

}
