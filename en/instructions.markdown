---
layout: default
title: Instructions
url: instructions
---

The *Interval Music Compositor* comes as a single executable file. No installation is needed therefore. However, it is based on Java technology, thus a _Java Runtime Environment (JRE)_ must be present to run it.
If you don't have one installed, you are prompted to do so on the first start of the software on the Microsoft Windows version. Users of Linux and Mac OS X operating systems are expected to have one installed already.

## Usage

1. Start the *Interval Music Compositor* by double-clicking it
1. (If necessary, follow the instructions for installing the Java Runtime Environment >= version 7)
1. Drag all the audio files you'd like to have processed from your desktop or a folder into the music track list. Note that this is only possible for the supported audio file types MP3, OGG, FLAC and WAV.
1. If you want a track being played during breaks, drag one into the break track list
1. Wait until all tracks have been imported properly. This is indicated by a blue icon in front of each track
1. If you're not sure about the speed (bpm) of a track, determine it manually by right-clicking on the track and choosing 'Change BPM...'
1. Determine the order of the track by dragging them around or pressing the 'sort' or 'shuffle' buttons.
1. Determine the extract enumeration method by either choosing 'single extract per track' which selects a random extract in each track, or 'continuous' which takes a number of extracts in a row from the same track before stepping to the next track.
1. Choose a target directory for the audio compilation by clicking the according button and choosing one in the dialog
1. Enter the desired values for the the length of the sound samples (in seconds, e.g. 30 seconds), the fade-in/out mode and period (e.g. 1 second), the length of the breaks (in seconds, e.g. 10 seconds) and the number of sound samples you'd like to have in the end (e.g. 12 samples). Or switch to the 'advanced' tab and enter patterns of music and breaks (e.g. 10,20,30,20).
1. Press the 'Process' button
1. Wait some time ...
1. If it says 'Finished' in the progress bar, the output sound file (in this example '30_10_12_imc_out.wav' (resp. '30_10_12_imc_out.mp3'), the numbers match the entered values) and the according tracklist ('30_10_12_playlist.txt') have been generated (this is, unless there were some serious problems). You can check the audio envelope to determine if the output file is ok.

## Legend

These are the meanings of the icons displayed in front of imported tracks:
![Icon Legend](/interval-music-compositor/img/imc_icon_legend.png)

These are the meanings of the colors of the displayed speed (bpm) measurements:
![BPM Legend](/interval-music-compositor/img/imc_bpm_legend.png)

## MP3 Generation

Since the encoding of MP3 files is covered by software patents, it is easier to skip it in the software and let it add through the user himself.

To add the ability to directly encode MP3 output files, you need to add the converting tool [Lame](http://lame.sourceforge.net/) to the Interval Music Compositor directory. A small guide for it follows the [download](download) instructions.
