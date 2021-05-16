package com.github.asciborek.playlist;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.time.Duration;
import java.util.List;

record Playlist(@JacksonXmlElementWrapper(useWrapping = false) List<PlaylistItem> tracks) { }

@JacksonXmlRootElement(namespace = "Track")
record PlaylistItem(@JacksonXmlProperty(localName = "Title") String title,
                    @JacksonXmlProperty(localName = "Album") String album,
                    @JacksonXmlProperty(localName = "Artist") String artist,
                    @JacksonXmlProperty(localName = "Duration") @JsonFormat(shape = Shape.STRING) Duration duration,
                    @JacksonXmlProperty(localName = "File") String filePath) {}
