package com.github.asciborek.notifications;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public final class NotificationPublisherFactory implements Provider<NotificationsPublisher> {

  private final EventBus eventBus;

  @Inject
  public NotificationPublisherFactory(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public NotificationsPublisher get() {
    final var notificationsPublisher = new NotificationsPublisher(new NotificationsFactory());
    eventBus.register(notificationsPublisher);
    return notificationsPublisher;
  }
}
