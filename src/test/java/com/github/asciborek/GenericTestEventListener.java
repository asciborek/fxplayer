package com.github.asciborek;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;

public final class GenericTestEventListener<T> {

  private final Class<T> eventClass;
  private final List<T> events;

  public GenericTestEventListener(Class<T> eventClass) {
    this.eventClass = eventClass;
    this.events = new ArrayList<>();
  }

  @Subscribe
  public void registerEvent(T event) {
    if (eventClass.isInstance(event)) {
      events.add(event);
    }
  }

  public List<T> getEventsSnapshot() {
    return ImmutableList.copyOf(events);
  }

  public int getEventsCount() {
    return events.size();
  }

  public void clearEvents() {
    events.clear();
  }

}
