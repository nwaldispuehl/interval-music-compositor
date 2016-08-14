---
layout: default
title: Feedback / Support
link: feedback_support
---


## Feedback

Schreib mir wenn du einen Fehler gefunden hast, einen Verbesserungsvorschlag machen möchtest, Hilfe brauchst oder sonst etwas auf dem Herzen hast.

    interval_music_compositor at retorte dot ch

### Wie Feedback geben?

Falls das Programm nicht richtig funktioniert, sende bitte immer auch den Inhalt der Ereignisanzeige (öffnen mittels Menü 'Hilfe' -> 'Ereignisanzeige') mit deiner Anfrage mit.
 
## Bugtracker
Wenn du einen Bug findest, und GitHub-Mitglied bist, kannst du ihn gleich selber als Issue erfassen: <br/>
[github.com/nwaldispuehl/interval-music-compositor/issues/](https://github.com/nwaldispuehl/interval-music-compositor/issues)

## Bekannte Probleme

Ist das Starten des Programms nicht möglich, solltest du zuerst die neuste Java-Version herunterladen und installieren: [java.com](http://www.java.com).

### Generell

* Dateien deren Dateinamen unangenehme Zeichen aufweisen werden ignoriert.

### Linux

* Unter Linux spielt die Vorhörfunktion lediglich Musik, wenn auf dem System keine andere Musikquelle aktiv ist.

### Mac OS X

* Auf Mac OS X Systemen kann es vorkommen dass die falsche Java Version ausgewählt ist. Das Programm startet dann nur ganz kurz und schliesst sich nach einer Sekunde wieder. Öffne die 'Java-Einstellungen' (Programme -> Dienstprogramme) und schiebe die neuste Version (Java 8) ganz nach oben. Falls dort keine Version 7 vorhanden ist, musst du möglicherweise dein System upgraden. Oder eine alte Version der Software benutzen.
* Lässt sich das Programm unter Mac OS X aus Sicherheits- oder Zertifikatsgründen nicht starten, kann man entweder in den Java-Einstellungen unter 'Sicherheit' das Sicherheitslevel senken, oder aber das Programm mittels der rechten Maustaste und 'Öffnen' starten.
* Erscheint beim Starten auf Mac OS X eine Fehlermeldung "JREloadError", ist eine zu alte Java-Version installiert. Lade und installiere die neuste Version (s.o.).

### Microsoft Windows

* Ist es unter Windows (64-bit) nicht möglich, sehr lange Stücke zu erzeugen, obwohl genügend Hauptspeicher vorhanden ist, ist möglicherweise die 32-bit Version von Java aktiv. Benutze die 64-bit Version von Java um das Programm zu starten.
