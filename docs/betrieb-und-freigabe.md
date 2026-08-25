# Betrieb und Freigabe

Stand: 25.08.2026

## Aktueller Betrieb

- **Community-Server 5520:** Dienst `hytale.service` aktiv und öffentlich gebunden (`65.109.56.119:5520`).
- **Hub & Spawnpunkt:** Zentraler Einstiegspunkt für alle neuen Spieler liegt dauerhaft in Glutwacht (`himmelsinsel`) an `/warp hub`.
- **Portale:** Welten-Portale zu Bauwelt, Survival und den Schwebenden Inseln sind im Hub platziert und mit Wächtern/Titeln versehen. Rückreise über die Survival-Safezone ist für Spieler freigegeben.
- **Wirtschaft:** Alle 6 offiziellen Händler (`smith`, `provisions`, `alchemist`, `tavern`, `builder`, `curios`) sowie die Kopfgeld-Boards sind im Hub aufgestellt.
- **Bauwelt:** 64×64-Grundstücke aktiv, Dörfer- und NPC-Spawns unterbunden, die 9 zentralen Plots sind als Admin-Reserve blockiert.
- **Server Discovery:** Offizielles Listing für *Der Waldbrand* eingereicht; Listing steht aktuell auf Review.

## Durchgeführte und noch offene Abnahmeschritte

- [x] Globalen Standard-Spawn auf Himmelsinsel (`/warp hub`) setzen.
- [x] Alle 6 Händler im Hub spawnen und verifizieren.
- [x] Kopfgeld-Boards (stündlich, täglich, wöchentlich) aufstellen.
- [x] Bauwelt frisch erzeugen, Strukturschutz aktivieren und 9 mittlere Plots reservieren.
- [x] Hub-Portale und Rückreise-Safezones in der Survivalwelt einrichten und testen.
- [x] Gast-Onboarding (Regeln, Quiz, Freischaltung, Klassenauswahl) durchspielen.
- [ ] Gast- und Spielerrechte mit einem zweiten externen Test-Account gegenprüfen.
- [ ] Ingame-Handbuch unter `/handbuch` mit Gast und Spieler durchblättern.
- [ ] Survival-Claims in `default` mit Mitgliedern, Tieren, Flüssigkeiten und Explosionen testen.
- [ ] Abschlussbackup erstellen und Wiederherstellung in einen leeren Testpfad prüfen.

## Freigabekriterien

- Keine Plugin-Ladefehler der öffentlichen Schutzschicht.
- Gast kann ausschließlich den vorgesehenen Einstieg und Regeltest nutzen.
- Spieler kann nur in `default` claimen und keine Admin-, Economy- oder Wildcard-Rechte erhalten.
- Grundstückskauf in der Bauwelt belastet Gold und Claim atomar.
- Nur die ausdrücklich freigegebenen 92 Gegenstände besitzen einen dauerhaften Ankaufspreis; Massenmaterial läuft ausschließlich über begrenzte Beschaffungsaufträge.
- Discovery-Heartbeat und Server-Listing sind aktiv verknüpft.

## Bekannte technische Hinweise

- Der Aetherhaven-Bard-Modellfehler ist auf 5520 korrigiert. Eine fehlende optionale `bard_songs.json` wird weiterhin als Warnung protokolliert.
- GlymeraStatues bleibt deaktiviert; GlymeraRaces bleibt uninstalliert.
- Zur Gastprüfung von Betreiber-Accounts kann die Test-Flagge `mods/de.nonsinn_NonSinnPublicCore/owner-as-guest.flag` genutzt werden.
