FxPlayer is my music player written for fun. Currently, it supports mp3, mp4 and wav files.

## How to build the application
In order to build the application you need JDK 26, and
the "last_fm.properties" file (which is put in .gitignore) stored in the ```src/main/resources```.
Set ```last.fm``` property value to your api key as shown below: ```api_key=<Your API key>```
and your ```shared secret``` as ```shared_secret=<Your shared secret```.  
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
- [x] Last.fm authentication, storing encrypted session key in the file
- [x] Signing out locally from last.fm (removing session key file)
- [x] Last.fm scrobbling 

## To do:
- [ ] Love tracks on Last.fm (Optional)

## CI / Tests

This project uses a GitHub Actions workflow at `.github/workflows/tests.yml` to run automated tests on push and pull requests.

What the workflow does:

- Runs on `ubuntu-latest`.
- Sets up Java (Temurin) JDK 26 using `actions/setup-java@v5`.
- Runs the Maven test suite with:

```bash
mvn clean test --batch-mode
```

- Generates a Surefire/site report with:

```bash
mvn site -DskipTests --batch-mode
```

- Uploads the `target/site/` directory as a workflow artifact (named `test-reports`) and retains it for 30 days.

Running tests locally

To run the same tests locally (or to reproduce CI behavior) use the commands above. After running `mvn site` the HTML test reports are available under `target/site/` and can be opened in your browser.
