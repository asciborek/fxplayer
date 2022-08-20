package com.github.asciborek.artist_info;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class LastFmArtistInfoProviderTest {

  private static final String API_KEY = "4r44dsd";
  private static final String REQUEST_URI_TEMPLATE =
      "http://localhost:8080/2.0/?method=artist.getinfo&artist=%s&api_key=%s&format=json";
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final HttpClient httpClient= httpClient();
  private final ArtistInfoProvider artistInfoProvider
      = new LastFmArtistInfoProvider(httpClient, executorService, API_KEY, REQUEST_URI_TEMPLATE);
  private final WireMockServer wireMockServer = new WireMockServer();

  @BeforeAll
  void beforeAll() {
    wireMockServer.start();
    WireMock.configureFor(8080);
    WireMock.stubFor(get("/2.0/?method=artist.getinfo&artist=Not_Found&api_key=4r44dsd&format=json")
            .willReturn(aResponse().withBodyFile("get_artist_info_not_found.json")));
    WireMock.stubFor(get("/2.0/?method=artist.getinfo&artist=Haken&api_key=4r44dsd&format=json")
            .willReturn(aResponse().withBodyFile("get_artist_info_success.json")));
  }

  @Test
  @DisplayName("return existing artist info")
  void returnExistingArtistInfo() throws Exception {
    //given
    var artist = "Haken";
    var expectedDescription = "Haken are English progressive metal band";
    var expectedSimilarBands = List.of("Caligula's Horse", "Leprous", "Between the Buried and Me",
        "Novena", "Thank You Scientist");
    var expectedArtistInfo = new ArtistInfo(expectedDescription, expectedSimilarBands);
    //when
    var artistInfo = artistInfoProvider.getArtistInfo(artist).get();
    //then
    assertThat(artistInfo).isEqualTo(expectedArtistInfo);
  }

  @Test
  @DisplayName("return NotFoundArtistInfo when artist was not found")
  void returnNotFoundArtistInfoWhenArtistWasNotFound() throws Exception {
    var artistInfo = artistInfoProvider.getArtistInfo("Not_Found").get();
    assertThat(artistInfo).isEqualTo(ArtistInfo.NOT_FOUND);
  }

  @AfterAll
  void closeExecutor() {
    wireMockServer.stop();
    executorService.shutdownNow();
  }

  private HttpClient httpClient() {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofMinutes(1))
        .build();
  }
}
