# Der Waldbrand – Hytale-Server

Nachvollziehbare Konfiguration, Dokumentation und Prüfwerkzeuge für den privat betriebenen Hytale-Community-Server **Der Waldbrand**.

- [Spielerhandbuch auf GitHub Pages](https://nonsindeads.github.io/hytale-public-server/)
- [Aktueller Ausbau und bekannte Einschränkungen](https://nonsindeads.github.io/hytale-public-server/status.html)
- [Technische Server-To-do-Liste](SERVER_TODO.md)
- [Vorbereitung für öffentliche Serverlisten](SERVER_LISTINGS.md)
- [Serverkonzept 1.0](SERVER_CONCEPT.md)
- [Fehler oder Vorschlag melden](https://github.com/nonsindeads/hytale-public-server/issues/new/choose)

Dieses Repository ist keine vollständige Serversicherung und enthält keine Weltdaten, Spielerdaten, Zugangsdaten oder privaten Backups. Es dokumentiert die bewusst angepassten Teile reproduzierbar.

## Server

| Port | Aufgabe | Besonderheiten |
|---:|---|---|
| 5520 | Community-Server | Hub, aktuelle Survivalwelt `default`, Gast-Onboarding, Claims und kontrollierte Economy |

Vor einem Neustart oder einer Änderung wird geprüft, ob Spieler online sind.

## Öffentliche Leitplanken

- **Arkadien** ist als dauerhafte friedliche Bau- und Siedlungswelt vorgesehen.
- **`default`** bleibt die aktuelle Survivalwelt und wird jetzt nicht zurückgesetzt.
- **Glutwacht** bleibt vorerst der bestehende Hub; ein Lobby- oder Spawn-Neubau ist kein Beta-Ziel.
- Eine **Farmwelt** ist derzeit nicht vorgesehen. Sie wird erst bei realem Ressourcen- oder Spielerbedarf neu bewertet.
- **WorldGen V2** wird erst nach offiziellem Release und interner Prüfung betrachtet; interne Testserver gehören nicht zur öffentlichen Kommunikation.
- Spielerprojekte, Straßen und Events sollen mit der Community entstehen, nicht vorab künstlich simuliert werden.

## Verifizierter Stand vom 27.08.2026

- Der Community-Server läuft.
- 5520 lädt NonSinnPublicCore 0.8.0 mit Gästeanzeige, interaktivem Ingame-Handbuch und abgesicherter Goldanbindung, LuckPerms 5.5.53, GlymeraPermissions 2.0.0, QuestLines Claims 1.5.0 und GlymeraPlotWorld 17.0.0.
- Vorhandene Weltdaten auf 5520: `default`, Glutwacht (technisch `himmelsinsel`), `oakhaven`, `schwebende_inseln` und `under`.
- Arkadien bleibt die geplante dauerhafte Bau- und Siedlungswelt; die öffentliche Dokumentation verspricht keine noch nicht abgenommenen Bauwelt-Details.
- GlymeraLimbo ist aktiv; die Limbo-Welt wird erst über ihren vorgesehenen Zugang erzeugt.
- Survival-Claims sind für Spieler nur in `default` freigegeben: 3 Start-Chunks, maximal 9, 32 Claim-Scherben pro Erweiterung.
- Die öffentliche Economy verbindet 92 ausgewählte Händlerankäufe mit stündlichen, täglichen und wöchentlichen Aufträgen. Massenmaterial wird über begrenzte Beschaffungsaufträge statt durch unbegrenzten Händlerverkauf verwertet.
- Worker und Farmer dürfen auf 5520 je eine aktive Truhe verwenden und nicht automatisch craften; Nicht-OP-Spieler dürfen einen Chunkloader setzen.

Die aktuelle Spieleransicht steht im [Statusbereich der Website](https://nonsindeads.github.io/hytale-public-server/status.html). Technische Abnahmepunkte stehen in [docs/betrieb-und-freigabe.md](docs/betrieb-und-freigabe.md).

## Repository-Struktur

| Pfad | Inhalt |
|---|---|
| `docs/` | GitHub-Pages-Spielerhandbuch und technische Hintergrundtexte |
| `config/` | Versionierte Zielkonfigurationen für Rollen, Claims, Economy, Automation und Arkadien |
| `economy/` | Maschinenlesbare Balanceregeln |
| `mods/mod-lock.json` | Lockdaten der eigens ergänzten öffentlichen Schutzschicht; derzeit kein vollständiger Modpack-Lock |
| `mods/NonSinnPublicCore/` | Quellcode für Gastschutz, Regeltest und gestaffelten Grundstückskauf |
| `SERVER_CONCEPT.md` | Öffentliche Leitidee und Entwicklungsentscheidungen |
| `scripts/` | Struktur-, Händler- und Progressionsprüfungen |
| `.github/ISSUE_TEMPLATE/` | Formulare für Fehler und Vorschläge |

## Lokale Prüfungen

Voraussetzung ist Python 3. Die Skripte verändern weder Server noch Konfigurationen.

```bash
python3 scripts/validate_configs.py
python3 scripts/audit_merchants.py
python3 scripts/audit_progression_economy.py
python3 scripts/audit_bounty_economy.py /pfad/zum/MMOSkillBountyPack
```

Die Prüfungen decken unter anderem ungültiges JSON, doppelte Waren, negative Preise, direkten Wiederverkauf, Händler-Arbitrage, nicht freigegebene Ankaufquellen, Claim-Scherben, Plotpreise und Automationsgrenzen ab.

## Sicherheitsgrenzen

- Keine Secrets, Tokens, privaten Spielerinformationen oder vollständigen Produktionsdaten committen.
- Normale reproduzierbare Fehler gehören in ein [GitHub Issue](https://github.com/nonsindeads/hytale-public-server/issues/new/choose).
- Duplikations-, Economy- und Rechte-Exploits werden über eine [private Security Advisory](https://github.com/nonsindeads/hytale-public-server/security/advisories/new) gemeldet.
- Ein erfolgreicher Start beweist nicht, dass Rechte, Claims oder Economy im Spiel korrekt funktionieren. Dafür ist der Zwei-Account-Praxistest verpflichtend.

Hytale und alle genannten Mods gehören ihren jeweiligen Rechteinhabern. Dieses Projekt ist weder mit Hypixel Studios noch mit den Mod-Autoren offiziell verbunden.
