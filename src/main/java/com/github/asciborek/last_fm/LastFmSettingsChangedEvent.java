package com.github.asciborek.last_fm;

import com.github.asciborek.settings.LastFmSettings;

public record LastFmSettingsChangedEvent (LastFmSettings newSettings){};
