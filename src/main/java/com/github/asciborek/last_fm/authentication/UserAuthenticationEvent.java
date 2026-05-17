package com.github.asciborek.last_fm.authentication;

import com.github.asciborek.last_fm.UserSession;

public sealed interface UserAuthenticationEvent {

  record WaitingForBrowserConfirmationEvent() implements UserAuthenticationEvent {}

  record BrowserConfirmationTimeoutEvent() implements UserAuthenticationEvent {}

  record NotRetryableAuthenticationErrorEvent() implements UserAuthenticationEvent {}

  record UserAuthenticatedEvent(UserSession userSession) implements UserAuthenticationEvent {}
}
