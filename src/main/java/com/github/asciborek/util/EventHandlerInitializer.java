package com.github.asciborek.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EventHandlerInitializer implements TypeListener {

  private static final Logger LOG = LoggerFactory.getLogger(EventHandlerInitializer.class);
  private final EventBus eventBus;

  public EventHandlerInitializer(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
    encounter.register(new EventHandlerInjectionListener<>(eventBus, type));
  }

  private static final class EventHandlerInjectionListener<I> implements InjectionListener<I> {

    private final EventBus eventBus;
    private final TypeLiteral<I> type;

    EventHandlerInjectionListener(EventBus eventBus, TypeLiteral<I> type) {
      this.eventBus = eventBus;
      this.type = type;
    }

    @Override
    public void afterInjection(I injectable) {
      if (injectable instanceof EventHandler) {
        LOG.info("Registering {} with EventBus", type.getRawType().getSimpleName());
        eventBus.register(injectable);
      }
    }
  }
}


