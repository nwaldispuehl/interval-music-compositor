---
layout: default
title: Instructions
link: instructions
---

Der retorte.ch **Interval Music Compositor** wird in einer Archivdatei ausgeliefert. Man entpackt das Archiv auf der Festplatte und startet dann das Startscript im `bin`-Verzeichnis.

## Gebrauch

1. Starte den **Interval Music Compositor** (siehe [Download](download))
1. Ziehe alle Musikstücke, die du in der Tracklist haben möchtest, vom Desktop oder einem Ordner in die Liste. Alternativ benutze das Menu ('Datei' -> 'Hinzufügen ...') oder den '+'-Knopf. Beachte, dass nur Musikstücke der unterstützten Formate der Liste hinzugefügt werden können.
1. Falls du eine Pausenmusik haben möchtest, ziehe ein Musikstück in das Pausenmusikfeld.
1. Warte bis alle Tracks komplett eingelesen wurden. Du siehst das daran, dass das Lied ein blaues Icon erhält.
1. Falls du bei der Geschwindigkeit (bpm) eines Tracks unsicher bist, ermittle die Geschwindigkeit via Kontext-Menü ("Wähle BPM...") manuell.
1. Wähle die Reihenfolge der Tracks indem du die Tracks herumschiebst, oder Buttons für Sortieren ('Sort') oder Mischen ('Shuffle') drückst.
1. Wähle die Art, wie die Extrakte extrahiert werden ('Enumeration'): "Single Extract" wenn du pro Stück zufällig einen Ausschnitt haben möchtest, oder "Continuous", wenn du in jedem Track der Reihe nach Ausschnitte haben möchtest.
1. Wähle ein Zielverzeichnis für die Kompilationsdatei indem du auf den 'Wähle Ausgabeverzeichnis...' Button drückst und im erscheinenden Dialog ein Verzeichnis anwählst.
1. Gib nun die gewünschten Werte für die Länge der Musikausschnitte (z.B. 30 Sekunden), die Art und Dauer der Ein- und Ausblendung (z.B. 1 Sekunde), die Dauer der Pausen (z.B. 10 Sekunden) und die Anzahl der Wiederholungen (z.B. 12) an. Oder aber wähle den 'Advanced' Reiter und gib Muster von Musik und Pausen ein (z.B. 10,20,30,20).
1. Falls gewünscht füge einen oder mehrere Soundeffekte mit dem entsprechenden Knopf hinzu und stelle deren gewünschte Position ein.
1. Drück den 'Verarbeiten' Knopf
1. Warte ein wenig ...
1. Wenn im Fortschrittsbalken 'Finished' erscheint, wurden die Zusammenstellung (in diesem Beispiel '30s_10b-x12.imc_out.wav' (resp. '30s_10b-x12.imc_out.mp3'), die Zahlen entsprechen stets den eingegebenen Werten) und die Liste der Tracks ('30s_10b-x12.playlist.txt') erzeugt (ausser natürlich es gab Probleme). Du kannst den Inhalt der Datei anhand der Hüllkurve kontrollieren.

## Legende

Folgendes sind die Bedeutungen der Icons, die vor den eingelesenen Dateien angezeigt werden:
![Icon Legende](/interval-music-compositor/img/imc_icon_legend.png)

Folgendes sind die Bedeutungen der Farben der angezeigten Geschwindigkeit (bpm):
![BPM Legende](/interval-music-compositor/img/imc_bpm_legend.png)
