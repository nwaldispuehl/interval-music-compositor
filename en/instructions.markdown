---
layout: default
title: Instructions
link: instructions
---

The **Interval Music Compositor** comes in an archive file. Unzip it to your harddrive and execute the starting script in the `bin` directory.

## Usage

1. Start the **Interval Music Compositor** (siehe [download](download))
1. Drag all the audio files you'd like to have processed from your desktop or a folder into the music track list. Alternatively use the menu ('File' -> 'Add ...') or the '+' button. Note that this is only possible for the supported audio file types.
1. If you want a track being played during breaks, drag one into the break track list
1. Wait until all tracks have been imported properly. This is indicated by a blue icon in front of each track
1. If you're not sure about the speed (bpm) of a track, determine it manually by right-clicking on the track and choosing 'Change BPM...'
1. Determine the order of the track by dragging them around or pressing the 'sort' or 'shuffle' buttons.
1. Determine the extract enumeration method by either choosing 'single extract per track' which selects a random extract in each track, or 'continuous' which takes a number of extracts in a row from the same track before stepping to the next track.
1. Choose a target directory for the audio compilation by clicking the according button and choosing one in the dialog
1. Enter the desired values for the the length of the sound samples (in seconds, e.g. 30 seconds), the fade-in/out mode and period (e.g. 1 second), the length of the breaks (in seconds, e.g. 10 seconds) and the number of sound samples you'd like to have in the end (e.g. 12 samples). Or switch to the 'advanced' tab and enter patterns of music and breaks (e.g. 10,20,30,20).
1. If desired add one or more sound effects with the respective button and set their start time.
1. Press the 'Process' button
1. Wait some time ...
1. If it says 'Finished' in the progress bar, the output sound file (in this example '30s_10b-x12.imc_out.wav' (resp. '30s_10b-x12.imc_out.mp3'), the numbers match the entered values) and the according tracklist ('30s_10b-x12.playlist.txt') have been generated (this is, unless there were some serious problems). You can check the audio envelope to determine if the output file is ok.

## Legend

These are the meanings of the icons displayed in front of imported tracks:
![Icon Legend](/interval-music-compositor/img/imc_icon_legend.png)

These are the meanings of the colors of the displayed speed (bpm) measurements:
![BPM Legend](/interval-music-compositor/img/imc_bpm_legend.png)
