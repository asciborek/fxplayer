FxPlayer is my music player written for fun. Currently it handles
only with mp3 files.

## How to build the application
In order to build the application you need JDK 16, and
the "api_keys.properties" file (which is put in .gitignore) stored in the ```src/main/resources```.
In the current moment only the last.fm api key is required. Set ```last.fm``` property value to 
your api key as shown below: ```last.fm=<Your API key>```.  
You can obtain the key on the [last.fm api page](https://www.last.fm/api).
I've tested the application only on the  Linux System (Kubuntu 20.04), so I can't guarantee
that it will work on other operating systems.

## Application design
### Package structure
Core package(subpackages of ```com.github.asciborek```) are seperated by features, not technical
details (like data, services, controllers etc). This kind of design Robert C. Martin describes as
[Screaming Architecture](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html).

### Components decoupling
In order to implement component decoupling I use "event-driven architecture" approach using Guava
EventBus. For instance when a new track is playing, the ```artist_info``` subcomponent is not called directly,
but is notified by subscribing  ```StartPlayignTrackEvent```. 
### Dependency Injection

In order to ensure testability and loose coupling I use dependency injection implemented with 
[Guice Framework](https://github.com/google/guice). Guice offers modular approach without 
component scanning. 

## Implemented features:
- [x] Playing mp3 file.
- [x] Loading single files and directories as well.
- [x] Saving and loading playlist files. Load and save the last playlist.
- [x] Storing and loading the last application settings(e.g: volume level).
- [x] Fetching artist info from last.fm and displaying it.

## Features to implement
- Scrobbling played tracks to last.fm
- "repeat track" and "repeat playlist" options
- Display the current track album cover
- Shuffle playlist option
- Add more supported file extensions(if it is possible)