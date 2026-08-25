# Changelog

## 0.6.0 – 25.08.2026

### Hub-Ausbau, Portale & Händler

- **Globaler Standard-Spawn:** Neuer Standard-Einstiegspunkt für alle Erstbesucher von `default` auf die Himmelsinsel (`Glutwacht`) umgestellt – exakt ausgerichtet auf Position und Blickwinkel von `/warp hub`.
- **Welten-Portale & Schwebeschrift:** Portale zu Bauwelt, Survival und den Schwebenden Inseln (Endgame) auf der Himmelsinsel platziert, mit Wächter-NPCs (`Klops_Miner`, `Feran_Windwalker`, `Temple_Mithril_Guard`) besetzt und mit 3D-Schwebetiteln versehen.
- **Rückreise-Portale:** Safezone-Rechte in der Survival-Welt (`default`) aktualisiert (`PORTAL: ALLOW`), sodass die Rückkehr zur Himmelsinsel für alle freigeschalteten Spieler funktioniert.
- **Händler & Kopfgelder:** Alle 6 offiziellen Glymera-Händler (`smith`, `provisions`, `alchemist`, `tavern`, `builder`, `curios`) im Hub aufgestellt; stündliche, tägliche und wöchentliche MMO-Kopfgeld-Boards aufgestellt.

### Bauwelt & Grundstücke

- **Bauwelt frisch initialisiert:** Bauwelt mit unzerstörbaren 64×64-Grundstücken über `GlymeraPlotWorld` sauber generiert.
- **Spawnschutz vor Dörfern & NPCs:** Unerwünschte Dorf-, Struktur- und Monster-Spawns in der Bauwelt dauerhaft unterbunden (`IsSpawningNPC: false`, `protectStructure: true`, `Env_Default_Flat`, Natural20 World-Boundaries).
- **Admin-Reserve gesperrt:** Die zentralen 9 Grundstücke rund um den Bauwelt-Spawn (`0:0`, `0:1`, `1:0`, `0:-1`, `1:1`, `1:-1`, `-1:-1`, `-1:1`, `-1:0`) dauerhaft als `NonSinn-Adminreserve` blockiert.

### Server-Listing & Discovery

- **Hytale Discovery:** Server-Listing für *Der Waldbrand* im offiziellen Hytale Serververzeichnis eingereicht (`EU Central`, Port 5520, RPG/Adventure, Audience: Teen); Server befindet sich im Review-Prozess.
- **Gast-Onboarding validiert:** Vollständiger First-Join-Ablauf (Regelwerk, Fragebogen-Freischaltung `/freischalten`, Klassenauswahl, Teleport) erfolgreich getestet und verifiziert.

## 0.5.0 – 24.08.2026

### Vernetzte Wirtschaft

- Dauerhaften Händlerankauf von 8 auf 92 ausgewählte Gegenstände erweitert.
- Nahrung, Tränke sowie Holz-, Stein- und Kupferausrüstung erhalten niedrige, geprüfte Ankaufspreise.
- Beschädigte Ausrüstung wird nach verbleibender Haltbarkeit abgewertet.
- Alle vorhandenen MMO-Aufträge zusätzlich mit serverweitem Gold verbunden; Bounty-Token und Skill-XP bleiben erhalten.
- 13 persönliche Beschaffungsaufträge für Dirt, Cobble, Kies, Sand, Lehm und weitere Baustoffe ergänzt.
- Schnellaufträge auf stündlichen Wechsel gesetzt; tägliche und wöchentliche Aufträge bleiben getrennt.
- Unbegrenzten Rückverkauf von Massenmaterial weiterhin verhindert.

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
- Erste öffentliche Economy-Fassung mit 8 kontrollierten Ankaufquellen eingeführt; später durch das vernetzte Profil 0.5.0 ersetzt.
- Worker und Farmer ohne automatisches Crafting und mit je einer aktiven Truhe; ein Chunkloader pro Spieler.
- Bauwelt-Konfiguration und gestaffelte Grundstückspreise vorbereitet; Welt und Portal noch offen.
- Dauerhafte Gästeanzeige im oberen Bildschirmbereich ergänzt; sie verweist auf `/regeln` und `/freischalten` und verschwindet nach bestandener Freischaltung.
- Interaktives Ingame-Handbuch unter `/handbuch` mit sechs navigierbaren Kapiteln für Start, Regeln, Welten, Claims, Wirtschaft und Hilfe ergänzt.
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
