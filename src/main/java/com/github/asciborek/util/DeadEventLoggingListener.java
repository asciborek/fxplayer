package com.github.asciborek.util;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public final class DeadEventLoggingListener {

  private static final Logger LOG = LoggerFactory.getLogger(DeadEventLoggingListener.class);

  @Inject
  public DeadEventLoggingListener(EventBus eventBus) {
    eventBus.register(this);
    LOG.info("DeadEventLoggingListener has been registered");
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onDeadEvent(DeadEvent deadEvent) {
    LOG.info("dead event occurred: {}", deadEvent);
  }

}
