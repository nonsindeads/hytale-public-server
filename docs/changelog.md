# Changelog

## 0.4.0 – 24.08.2026

### Dokumentation

- GitHub Pages als vollständiges, responsives Spielerhandbuch neu aufgebaut.
- Eigene Seiten für Einstieg und Befehle, Welten und Claims, Wirtschaft, Mods sowie Status und Änderungen ergänzt.
- Inhalte mit laufendem Dienst, Weltordnern, aktiven Plugins, Konfigurationen und Startprotokollen des Community-Servers abgeglichen.
- Unbestätigte Gemini-Befehle wie `/warp`, `/tp world`, `/sethome` und mehrere falsche `/plot`-Varianten entfernt.
- Aktive, konfigurierte, noch nicht erzeugte und deaktivierte Inhalte sichtbar voneinander getrennt.
- Technisches README und Betriebsdokument an den realen Stand angepasst.
- WorldGen-V2-Strategie dokumentiert: neue Survivalwelt statt vollständigem Server-Wipe; Hub, Bauwelt und dauerhafte Projekte bleiben bestehen.

### Community-Server 5520

- Frische Default-Welt und Glutwacht als zentralen Hub übernommen.
- Veraltete, aus den früheren Welten übernommene Warp-Ziele vollständig entfernt.
- Gast-/Quizsystem, LuckPerms, GlymeraPermissions und NonSinnPublicCore geladen.
- QuestLines Claims und GlymeraPlotWorld installiert; Survival-Claims auf `default` begrenzt.
- Öffentliche Economy auf 8 kontrollierte Ankaufquellen begrenzt; gewöhnliche Materialien und Ausrüstung unverkäuflich.
- Worker und Farmer ohne automatisches Crafting und mit je einer aktiven Truhe; ein Chunkloader pro Spieler.
- Bauwelt-Konfiguration und gestaffelte Grundstückspreise vorbereitet; Welt und Portal noch offen.
- Dauerhafte Gästeanzeige im oberen Bildschirmbereich ergänzt; sie verweist auf `/regeln` und `/freischalten` und verschwindet nach bestandener Freischaltung.
- Globalen `/shop`-Zugriff für Spieler deaktiviert; Handel findet nach der Freischaltung direkt an den Händlerständen in Glutwacht statt.
- Leere Markt-Testwelt und kopierte Händlerpositionen zur bereinigten Neueinrichtung in Glutwacht aus dem aktiven Stand entfernt und gesichert.

### Welten und Modpack

- Glutwacht als künftigen Hub übernommen; Portale zu Default und den Floating Islands müssen auf 5520 noch ingame gebaut werden.
- Floating Islands zusammen mit Glymera Structures als extrem schwere Abenteuerwelt bestätigt.
- Under als End-ähnliche und Limbo als Nether-ähnliche Spezialwelt eingeordnet; beide nutzen ihre mod-eigenen Zugänge.
- Fahrzeuge auf Boote, Flöße und Luftschiffe begrenzt; Autos deaktiviert.
- GlymeraStatues wegen fehlerhafter schwarzer Blöcke deaktiviert.
- GlymeraRaces wegen Konflikten mit MMO Skill Tree und Natural20 nicht installiert.

## 0.3.0 – 23.08.2026

- Glutwacht als geschützten zentralen Hub festgelegt.
- Dauerhafte Bauwelt mit 64×64-Grundstücken zusätzlich zu Survival-Claims geplant.
- Grundstückspreise auf 0, 1.000, 3.000 und 7.500 Gold festgelegt.
- Default als resetbare Survivalwelt mit 3 bis 9 Claim-Chunks konfiguriert.
- Vollständiger geplanter Landausbau auf 12.460 Gold berechnet.

## 0.2.2 – 23.08.2026

- Command-Failsafe korrigiert: Spieler erhalten keine Umgehungsberechtigung.
- Sichere Spielbefehle für Claims, Economy und Gameplay-Mods explizit freigegeben.
- Administrative Hauptbefehle wie `/op`, `/merchant` und `/nonsinn` bleiben gesperrt.

## 0.2.0 – 23.08.2026

- Reproduzierbare LuckPerms-Gruppen und Vererbungen ergänzt.
- Owner-Zuweisung anhand fester Spieler-UUID implementiert.
- NonSinnPublicCore um LuckPerms-Anbindung erweitert.
