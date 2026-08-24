# Server-To-do

Stand: 24.08.2026

Diese Liste ersetzt die frühere Gemini-Ingame-Liste. Deren Warp-, Teleport- und teilweise Plot-Befehle waren für die installierten Mods nicht bestätigt.

## Erledigt

- [x] Community-Server mit frischer Default-Welt eingerichtet.
- [x] Übernommene Warpdateien geleert; neue Reiseziele werden passend zu den neuen Welten ingame angelegt.
- [x] Glutwacht als Namen des zentralen Hubs festgelegt; der technische Weltname `himmelsinsel` bleibt zur Kompatibilität bestehen.
- [x] Glymera Structures auf den Schwebenden Inseln getestet und aktiviert.
- [x] Under übernommen; Limbo-Mod geladen.
- [x] Händlerprofile, Preise und passende NPC-Skins vorbereitet.
- [x] Gast-Onboarding, dauerhafte Gästeanzeige, Regeltest, LuckPerms und Befehls-Failsafe geladen.
- [x] Interaktives Ingame-Handbuch mit `/handbuch` und sechs Kapiteln implementiert und gebaut.
- [x] Globalen `/shop`-Befehl für Spieler deaktiviert; Handel erfolgt künftig direkt an den NPC-Ständen in Glutwacht.
- [x] Leere Markt-Testwelt archiviert und sechs kopierte Händlerpositionen für den sauberen Neuaufbau entfernt.
- [x] QuestLines Claims installiert und für Spieler auf `default` begrenzt.
- [x] Öffentliche Händlerwirtschaft statisch auf Arbitrage und ungewollte Ankäufe geprüft.
- [x] Worker, Farmer und Chunkloader für den Community-Betrieb begrenzt.
- [x] Spielerhandbuch, Issue-Formulare und private Sicherheitsmeldungen veröffentlicht.
- [x] Serverlisten-Paket mit Positionierung, deutschen und englischen Eintragstexten, Eintragszielen und Protokoll vorbereitet.

## Vor der breiten Freigabe

- [ ] **Spawn-Safezone in Default mit vorläufig 300 Blöcken Radius anlegen.** Zentrum ist der künftige öffentliche Portal-/Spawn-Bereich und wird nach Besichtigung der neuen Map festgelegt. Die kreisförmige Region soll über die gesamte Welthöhe reichen. Innerhalb der Zone müssen `BUILD`, `BREAK` und `SPAWN` gesperrt sein: kein Bauen, kein Abbauen und keine Mob-Spawns. Den Radius beim Ingame-Ausbau nochmals prüfen und bei Bedarf anpassen.
- [ ] Entscheiden, ob in der Safezone zusätzlich PvP, Explosionen, Feuer, Flüssigkeiten und Containerzugriffe gesperrt werden.
- [ ] Portalpaar zwischen Glutwacht und Default ingame bauen und beide Richtungen testen.
- [ ] Portalpaar zwischen Glutwacht und Schwebenden Inseln ingame bauen und beide Richtungen testen.
- [ ] Vorgesehene Zugänge für Under und Limbo ingame aufbauen und testen.
- [ ] Händler an ihren Marktständen in Glutwacht ingame erzeugen, Aussehen kontrollieren und ausrichten.
- [ ] Mit Gast und Spieler prüfen: Gäste können Händler nicht benutzen; Spieler handeln am NPC; `/shop` ist gesperrt.
- [ ] Bauwelt erzeugen und Generierung kontrollieren.
- [ ] Hub-Portal zur Bauwelt bauen und beide Richtungen testen.
- [ ] Grundstückskauf mit allen Preisstufen 0/1.000/3.000/7.500 Gold praktisch testen.
- [ ] Plot-Schutz, Trust/Untrust, Merge, Flüssigkeiten und Kreaturengrenzen mit zwei Accounts testen.
- [ ] Gast- und Spielerrechte mit einem zweiten Account vollständig prüfen.
- [ ] Ingame-Handbuch mit Gast und Spieler vollständig prüfen: Navigation, Textumbruch, Scrollen, Schließen und erneutes Öffnen.
- [ ] Survival-Claims mit Mitgliedern, Tieren, Flüssigkeiten, Explosionen, Portalen, Ablauf und Reset-Bereinigung testen.
- [ ] Entscheiden, ob Spieler `questlinesclaims.home.use` und damit Claim-Heimpunkte erhalten.
- [ ] Alle Händler im Hub praktisch öffnen sowie Kauf und erlaubte Verkäufe testen.
- [ ] Seltene Drops, Questbelohnungen und Goldzuwachs über echte Spielsitzungen messen.
- [ ] Limbo über den vorgesehenen Mod-Zugang erzeugen und Hin-/Rückweg testen.
- [ ] Abschlussbackup erstellen und Wiederherstellung in einen leeren Testpfad prüfen.

## Öffentliche Serverlisten

Die Texte, Zielportale, bekannten Anforderungen und das Eintragsprotokoll stehen in [SERVER_LISTINGS.md](SERVER_LISTINGS.md).

- [ ] Eigenen öffentlichen Server-Domainnamen festlegen und auf `65.109.56.119:5520` führen; DNS-TXT-Verifikation für die offizielle Discovery vorbereiten.
- [ ] Öffentliche Kontaktadresse und optional Discord festlegen.
- [ ] Altersziel, Inhaltsmerkmale und Monetarisierungsstatus verbindlich festlegen.
- [ ] Öffentliche Seiten für Datenschutz, Support und Erstattung beziehungsweise „keine Käufe“ ergänzen und inhaltlich prüfen.
- [ ] Logo, Banner, Screenshots und optional kurzen Trailer aus der fertig eingerichteten 5520-Welt erstellen.
- [ ] Offizielle Hytale Server Discovery einreichen, moderieren lassen und Heartbeat sicher konfigurieren; Discovery-Token niemals versionieren.
- [ ] Priorität-A-Serverlisten eintragen und die öffentlichen Profil-URLs dokumentieren.
- [ ] Priorität-B-Serverlisten nach erneuter Prüfung ergänzen.
- [ ] Vote-Belohnungen nur nach separater Economy- und Exploitprüfung aktivieren.
- [ ] Alle öffentlichen Einträge monatlich auf Erreichbarkeit, Version, Text, Bilder und korrekte Angaben prüfen.

## Nach der Freigabe beobachten

- [ ] Serverlast durch Chunkloader, Worker, Farmer, Structures und größere Spielerbauten beobachten.
- [ ] Goldmenge pro aktiver Spielstunde und tatsächliche Landkaufdauer beobachten.
- [ ] Fehlerberichte nach Welt und Mod auswerten und reproduzierbare Regressionstests ergänzen.
- [ ] Reset der Default-Welt rechtzeitig ankündigen und Claims vor dem Reset dokumentiert bereinigen.

## Übergang auf WorldGen V2

Ziel: Eine neue Wildnis ohne vollständigen Server-Wipe. Glutwacht, Bauwelt und dauerhafte Communityprojekte bleiben bestehen.

- [ ] WorldGen V2 zunächst auf einer Serverkopie mit allen Welt-, RPG-, Structure-, Claim-, Portal- und Economy-Mods testen.
- [ ] Vollständiges Backup und geprüften Wiederherstellungspunkt vor der ersten V2-Welterzeugung anlegen.
- [ ] Neue V2-Survivalwelt unter einem eigenen Namen erzeugen; die bestehende `default`-Welt nicht überschreiben.
- [ ] Transport von Inventar, Blöcken und Gegenständen zwischen V1, V2, Hub und Bauwelt prüfen und nicht übertragbare Inhalte dokumentieren.
- [ ] Claims, LuckPerms-Weltkontexte, Spawn-Safezone und Hub-Portal erst nach erfolgreicher Abnahme auf die neue V2-Welt umstellen.
- [ ] Alte Survivalwelt während einer angekündigten Übergangsfrist erhalten und anschließend kontrolliert archivieren; kein unangekündigter Komplett-Wipe.
- [ ] Spielerhandbuch, Serverlisten und Reset-/Transferregeln vor der öffentlichen Umschaltung aktualisieren.
