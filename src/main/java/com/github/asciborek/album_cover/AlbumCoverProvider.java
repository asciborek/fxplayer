package com.github.asciborek.album_cover;

import java.util.concurrent.CompletableFuture;
import javafx.scene.image.Image;

interface AlbumCoverProvider {

  CompletableFuture<Image> fetchAlbum(ArtistAlbum albumCoverRequest);

}
