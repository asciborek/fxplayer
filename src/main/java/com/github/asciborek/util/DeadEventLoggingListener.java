package com.github.asciborek.util;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"UnstableApiUsage"})
@AutoRegistrableEventBusListener
public final class DeadEventLoggingListener {

  private static final Logger LOG = LoggerFactory.getLogger(DeadEventLoggingListener.class);

  @Subscribe
  public void onDeadEvent(DeadEvent deadEvent) {
    LOG.info("dead event occurred: {}", deadEvent);
  }

}
