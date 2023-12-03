Interval Music Compositor
=========================

Interval Music Compositor, a multi-platform software for creating interval music compilations out of multiple sound files.

![Interval Music Compositor screenshot](https://github.com/nwaldispuehl/interval-music-compositor/raw/main/intervalmusiccompositor.build/footage/imc_screenshot.png)



How to download binary?
-----------------------

There are pre-built distributions for the major operating systems to be found on the github releases page: [Interval Music Compositor releases](https://github.com/nwaldispuehl/interval-music-compositor/releases).
A small user manual can be found on the Github [project page](http://nwaldispuehl.github.io/interval-music-compositor/).


How to build?
-------------

You certainly need a Java development kit (>= 17) installed on your system. Alternatively use `jenv` to control the used JDK. Then, clone the repository and use the packaged gradle binary to either run the program,

    $ ./gradlew run
  
execute its tests, 

    $ ./gradlew check
  
or create the distribution for the supported operating system (Linux, Windows, and Mac). In this case, you also need to:

- place the respective [JDKs](https://adoptium.net/releases.html) and [JavaFX jmods](https://gluonhq.com/products/javafx/) (the 'target platform') on your system according to the `jlink` task in the `intervalmusiccompositor.app/build.gradle` file, and
- set up a Rust build environment in the `intervalmusiccompositor.updater/native-src` with the respective toolchains (see README file there).

Then, build with:

    $ ./gradlew distAll
  
The distribution archives are then to be found in the `intervalmusiccompositor.app/build/distributions/` folder.

Note: The assumption is that we are using a Linux system to build the software. If you have another operating system you might need to adapt the OS-specific dependencies in `intervalmusiccompositor.ui.fx/build.gradle` which are only used to directly run the software with the build tool Gradle.
