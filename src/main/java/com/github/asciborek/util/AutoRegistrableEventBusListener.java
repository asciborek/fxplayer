package com.github.asciborek.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an EventBus listener.
 * Classes annotated with this will be automatically registered with the EventBus
 * when instantiated by Guice.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegistrableEventBusListener {

}

