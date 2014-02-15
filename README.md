Interval Music Compositor
=========================

Interval Music Compositor, a multi-platform software for creating interval music compilations out of multiple sound files.

![Interval Music Compositor screenshot](https://raw2.github.com/nwaldispuehl/interval-music-compositor/master/intervalmusiccompositor.build/footage/imc_screenshot_small.jpg)



How to download binary?
-----------------------

There are pre-built distributions for the major operating systems to be found either on the projects webpage [retorte.ch download page](http://www.retorte.ch/tools/interval_music_compositor/download) or on the respective github releases page: [Interval Music Compositor releases](https://github.com/nwaldispuehl/interval-music-compositor/releases).
On the former there is also a small user manual.


How to build?
-------------

You certainly need a Java 7 development kit installed on your system. Then, clone the repository and use the packaged gradle binary to either run the program,

    $ ./gradlew run
  
execute its tests, 

    $ ./gradlew test
  
or create the distributions:

    $ ./gradlew distAll
  
The distribution archives are then to be found in the `intervalmusiccompositor.app/build/distributions/` folder.
