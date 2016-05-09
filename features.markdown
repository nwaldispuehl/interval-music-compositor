---
layout: default
title: Features
link: features
---

Der retorte.ch **Interval Music Compositor** hat die folgenden Eigenschaften.

## Was es macht

* Liest Musikdateien (Tracks) im aktuellen Verzeichnis
* Per Drag and Drop können der Liste weitere Tracks hinzugefügt werden
* Die Liste kann sortiert oder gemischt werden
* Die Geschwindigkeit der Tracks (bpm) wird aus den Metadaten geladen oder berechnet (erkennt Tempi zwischen 60 und 185 bpm). Sie kann auch manuell eingegeben oder durch Klicken ermittelt werden. Der Wert kann in der Datei gespeichert werden.
* Ignoriert die ersten und letzten 5 Sekunden eines Tracks
* Verwirft Tracks die zu kurz sind
* Wählt einen zufälligen Ausschnitt jedes Tracks oder der Reihe nach Ausschnitte nach vorgegebener Dauer
* Normalisiert die Lautstärke der Ausschnitte
* Fügt die Ausschnitte zu einer einzigen grossen Zusammenstellung zusammen wobei Intervalle und Pausen der Zusammenstellung gewählt werden können.
* Unterstützt Intervallmuster
* Falls nicht genügend Tracks vorhanden sind um die gewünschte Anzahl Wiederholungen zu erreichen wird einfach wieder von vorne begonnen
* Die Liste der gewählten Tracks wird in einer Textdatei gespeichert
* Die Hüllkurve der erzeugten Zusammenstellung wird zur Kontrolle angezeigt

## Systemvoraussetzungen

* Massenhaft Festplattenspeicher (1 Minute Musik der Zusammenstellung kostet 10MB). Mit 1GB ist man also dabei
* Massenhaft Hauptspeicher (> 1GB)
* Installiertes Java Runtime Environment (Version 8)

## Unterstützte Eingabeformate

* WAV
* MP3
* OGG
* FLAC
* AAC

## Ausgabeformat

Die Zusammenstellung ist eine 16bit, 44.1KHz, stereo, 256kbit MP3, WAV oder OGG Datei. 
