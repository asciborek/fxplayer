package com.github.asciborek.artist_info;

import java.util.Collections;
import java.util.List;

public record ArtistInfo(String description, List<String> similarArtist) {
  public static ArtistInfo NOT_FOUND = new ArtistInfo("couldn't find the artist",
      Collections.emptyList());
  public static ArtistInfo UNREACHABLE = new ArtistInfo("couldn't fetch the artist data",
      Collections.emptyList());
}
