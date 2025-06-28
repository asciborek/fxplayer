FxPlayer is my music player written for fun. Currently, it supports mp3, mp4 and wav files.

## How to build the application
In order to build the application you need JDK 24, and
the "last_fm.properties" file (which is put in .gitignore) stored in the ```src/main/resources```.
In the current moment only the api key is required. Set ```last.fm``` property value to 
your api key as shown below: ```api_key=<Your API key>```.  
You can obtain the key on the [last.fm api page](https://www.last.fm/api).
I've tested the application only on the  Linux System (Kubuntu 20.04), so I can't guarantee
that it will work on other operating systems.

## Application design
### Package structure
Core packages(subpackages of ```com.github.asciborek```) are seperated by features, not technical
details (like data, services, controllers etc.). This kind of design Robert C. Martin describes as
[Screaming Architecture](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html).

### Components decoupling
In order to implement component decoupling I use "event-driven architecture" approach using Guava
EventBus. For instance when a new track is playing, the ```artist_info``` subcomponent is not called directly,
but is notified by subscribing  ```StartPlayingTrackEvent```. 
### Dependency Injection

In order to ensure testability and loose coupling I use dependency injection implemented with 
[Guice Framework](https://github.com/google/guice). Guice offers modular approach without 
component scanning. 

## Implemented features:
- [x] Playing mp3, mp4 or wav file.
- [x] Displaying metadata (artist, album, title, track length).
- [x] Loading single files and directories as well.
- [x] Saving and loading playlist files. Load and save the last playlist.
- [x] Storing and loading the last application settings(e.g: volume level).
- [x] "repeat a track" and "repeat a playlist" options.
- [x] Shuffling the  playlist 
- [x] Editing tracks metadata
- [x] Collecting track playing history
- [x] Fetching artist info from last.fm and displaying it.
- [x] Display the current track album cover
- [x] Removing tracks from playlist after track files being deleted
