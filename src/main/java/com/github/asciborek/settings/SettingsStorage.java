package com.github.asciborek.settings;

import java.util.Optional;

interface SettingsStorage {

  void save(Settings settings);

  Optional<Settings> loadSettings();

}
