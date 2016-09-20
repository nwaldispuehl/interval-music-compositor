---
layout: default
title: Feedback / Support
link: feedback_support
---

## Feedback

Write me if you've found a bug, have a suggestion for improvements, need help or just want to drop some lines.

    interval_music_compositor at retorte dot ch


### How to give feedback?

If the software does not work correctly, please also send the contents of the debug log window (open via menu 'Help' -> 'Debug log') with your inquiry.

## Issuetracker

If you found a bug, and have a github account, you may file an issue directly: <br/>
[github.com/nwaldispuehl/interval-music-compositor/issues/](https://github.com/nwaldispuehl/interval-music-compositor/issues)

## Known problems

In case it is not possible to start the program, first of all download and install the latest Java version: [java.com](http://www.java.com). Use the 64bit version if possible.

### General

* If a filename has some nasty characters in it, the file is ignored.

### Linux

* On Linux, the pre-listen function only plays music if there is no other audio source active on the system.

### Mac OS X

* On Mac OS X computers it may be that the wrong Java version is selected. The program will then only run for a second and close again immediately after being started. Start the 'Java Settings' (Applications -> Utilities) and move the latest Java version (Java 8) to the top of the list to fix it. If there is no version 8 of Java you probably have to update your operating system to obtain it. Or use a legacy version of the software.
* If the software does not start on Mac OS X systems due to security or certificate constraints, one can either lower the Java security setting under the 'Security' tab of the Java settings, or open the program via the right mouse button and the 'Open' command.
* If there appears an error message saying "JREloadError" on startup, a too old Java version is installed. Download and install the latest version (see above).

### Microsoft Windows

* On Windows (64 bit), if it is not possible to generate long compilations even there is enough RAM, most likely the 32 bit version of Java is currently active. Use the 64 bit version of Java to start the program.
