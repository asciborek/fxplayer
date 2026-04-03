package com.github.asciborek.last_fm;

public sealed interface UserAuthenticationEvent {

  record WaitingForBrowserConfirmationEvent() implements UserAuthenticationEvent {}

  record BrowserConfirmationTimeoutEvent() implements UserAuthenticationEvent {}

  record NotRetryableAuthenticationErrorEvent() implements UserAuthenticationEvent {}

  record UserAuthenticatedEvent(UserSession userSession) implements UserAuthenticationEvent {}
}
