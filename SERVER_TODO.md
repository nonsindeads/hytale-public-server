# Server-TODO vor öffentlicher Freigabe

Stand: 25.08.2026

## Offene Punkte vor Serverlisten-Freigabe

### Ingame-Abnahme auf 5520

- [x] Globalen Standard-Spawn auf Himmelsinsel (`/warp hub`) setzen.
- [x] Alle 6 Händler im Hub spawnen und verifizieren (`smith`, `provisions`, `alchemist`, `tavern`, `builder`, `curios`).
- [x] Kopfgeld-Boards (stündlich, täglich, wöchentlich) im Hub aufstellen.
- [x] Bauwelt mit unzerstörbaren 64×64-Plots erzeugen und die 9 zentralen Plots als `NonSinn-Adminreserve` reservieren.
- [x] Dörfer- und NPC-Spawns in der Bauwelt dauerhaft sperren.
- [x] Hub-Portale zu Bauwelt, Survival und Schwebenden Inseln platzieren und beschriften.
- [x] Rückreise-Portale in der Survival-Safezone (`default`) freigeben (`PORTAL: ALLOW`).
- [x] Erstes Gast-Onboarding (Regeln, Quiz `/freischalten`, Klassenauswahl) durchspielen.
- [ ] Gast- und Spielerrechte mit einem zweiten Account vollständig gegenprüfen.
- [ ] Ingame-Handbuch mit Gast und Spieler vollständig prüfen: Navigation, Textumbruch, Scrollen, Schließen und erneutes Öffnen.
- [ ] Survival-Claims mit Mitgliedern, Tieren, Flüssigkeiten, Explosionen, Ablauf und Reset-Bereinigung testen.
- [ ] Entscheiden, ob Spieler `questlinesclaims.home.use` und damit Claim-Heimpunkte erhalten.
- [ ] Alle Händler im Hub praktisch öffnen sowie Kauf und erlaubte Verkäufe testen.
- [ ] Je einen stündlichen, täglichen und wöchentlichen Auftrag sowie einen vollständigen Beschaffungsauftrag annehmen, abschließen und Gold-/Token-/XP-Auszahlung prüfen.
- [ ] Seltene Drops, Questbelohnungen und Goldzuwachs über echte Spielsitzungen messen.
- [ ] Limbo über den vorgesehenen Mod-Zugang erzeugen und Hin-/Rückweg testen.
- [ ] Abschlussbackup erstellen und Wiederherstellung in einen leeren Testpfad prüfen.

## Öffentliche Serverlisten

Die Texte, Zielportale, bekannten Anforderungen und das Eintragsprotokoll stehen in [SERVER_LISTINGS.md](SERVER_LISTINGS.md).

- [x] Server-Listing-Texte, Altersziel (`Teen`) und Monetarisierungsstatus (EULA-konform) festlegen.
- [x] Offizielle Hytale Server Discovery einreichen; Server steht auf **Review**.
- [ ] Nach Discovery-Freigabe: Heartbeat und Status regelmäßig prüfen.
- [ ] Priorität-A-Serverlisten nach finaler Freigabe eintragen und die öffentlichen Profil-URLs dokumentieren.
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
