Interval Music Compositor
=========================

Interval Music Compositor, a multi-platform software for creating interval music compilations out of multiple sound files.

![Interval Music Compositor screenshot](https://github.com/nwaldispuehl/interval-music-compositor/raw/master/intervalmusiccompositor.build/footage/imc_screenshot.png)



How to download binary?
-----------------------

There are pre-built distributions for the major operating systems to be found on the github releases page: [Interval Music Compositor releases](https://github.com/nwaldispuehl/interval-music-compositor/releases).
A small user manual can be found on the Github [project page](http://nwaldispuehl.github.io/interval-music-compositor/).


How to build?
-------------

You certainly need a Java development kit (>= 16) installed on your system. Then, clone the repository and use the packaged gradle binary to either run the program,

    $ ./gradlew run
  
execute its tests, 

    $ ./gradlew check
  
or create the distribution for the supported operating system (either Linux, Windows, or Mac). In this case, you also need to place the respective JDK and 
JavaFX jmods (the 'target platform') on your system according to the `jlink` task in the `intervalmusiccompositor.app/build.gradle` file. Then, build with:

    $ ./gradlew distAll
  
The distribution archives are then to be found in the `intervalmusiccompositor.app/build/distributions/` folder.

