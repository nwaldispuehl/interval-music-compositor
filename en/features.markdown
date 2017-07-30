---
layout: default
title: Features
link: features
---

The retorte.ch **Interval Music Compositor** has the following properties.

## What it does

* Reads audio files (tracks) in current directory
* More tracks can be added to the list
* The track list can be sorted or shuffled
* The speed of a track (bpm) is loaded from the metadata or calculated (identifies speeds between 60 and 185 bpm). It can also be entered manually or determined by tapping. The value can be stored back into the original file.
* Ignores the first and the last 5 seconds of a track
* Discards tracks which are too short
* Takes a random extract or continuous extracts of the tracks according to the defined length
* Normalizes the volume of the extract
* Composes them together to one big output file (compilation) whereas the length of the extracts and the breaks can be freely chosen
* Supports patterns of intervals
* Sound effects (gong, whistle) can be added.
* If there are not enough tracks to fill the compilation, it starts from anew
* Stores the playlist data in a textfile
* Displays the audio envelope of the compilation for test purposes

## What it needs to be run

* Lots of diskspace (the temporary files and the output file need 10MB per minute music): 1GB
* Lots of main memory
* Installed Java Runtime Environment (at least version 8 update 60, preferably the 64bit version)

## Supported input file formats

* WAV
* MP3
* OGG
* FLAC
* AAC

## Output file format

The resulting sound file is a 16bit, 44.1KHz, stereo, 256kbit MP3, WAV, or OGG file.
