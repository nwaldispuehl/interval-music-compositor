Interval Music Compositor
=========================

Interval Music Compositor, a multi-platform software for creating interval music compilations out of multiple sound files.

![Interval Music Compositor screenshot](https://github.com/nwaldispuehl/interval-music-compositor/raw/master/intervalmusiccompositor.build/footage/imc_screenshot_small.png)



How to download binary?
-----------------------

There are pre-built distributions for the major operating systems to be found on the github releases page: [Interval Music Compositor releases](https://github.com/nwaldispuehl/interval-music-compositor/releases).
A small user manual can be found on the Github [project page](http://nwaldispuehl.github.io/interval-music-compositor/).


How to build?
-------------

You certainly need a Java 7 development kit installed on your system. Then, clone the repository and use the packaged gradle binary to either run the program,

    $ ./gradlew run
  
execute its tests, 

    $ ./gradlew check
  
or create the distributions:

    $ ./gradlew distAll
  
The distribution archives are then to be found in the `intervalmusiccompositor.app/build/distributions/` folder.


Current build status
--------------------
[![Build Status](https://travis-ci.org/nwaldispuehl/interval-music-compositor.png?branch=master)](https://travis-ci.org/nwaldispuehl/interval-music-compositor)
[![Coverage Status](https://coveralls.io/repos/nwaldispuehl/interval-music-compositor/badge.png)](https://coveralls.io/r/nwaldispuehl/interval-music-compositor)
