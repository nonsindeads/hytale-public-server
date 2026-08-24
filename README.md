# Der Waldbrand – Hytale-Server

Nachvollziehbare Konfiguration, Dokumentation und Prüfwerkzeuge für den privat betriebenen Hytale-Community-Server **Der Waldbrand**.

- [Spielerhandbuch auf GitHub Pages](https://nonsindeads.github.io/hytale-public-server/)
- [Aktueller Ausbau und bekannte Einschränkungen](https://nonsindeads.github.io/hytale-public-server/status.html)
- [Technische Server-To-do-Liste](SERVER_TODO.md)
- [Vorbereitung für öffentliche Serverlisten](SERVER_LISTINGS.md)
- [Fehler oder Vorschlag melden](https://github.com/nonsindeads/hytale-public-server/issues/new/choose)

Dieses Repository ist keine vollständige Serversicherung und enthält keine Weltdaten, Spielerdaten, Zugangsdaten oder privaten Backups. Es dokumentiert die bewusst angepassten Teile reproduzierbar.

## Server

| Port | Aufgabe | Besonderheiten |
|---:|---|---|
| 5520 | Community-Server und Abnahme | Gast-Onboarding, Claims, geplante Bauwelt, kontrollierte Economy und begrenzte Automation |

Vor einem Neustart oder einer Änderung wird geprüft, ob Spieler online sind.

## Verifizierter Stand vom 24.08.2026

- Der Community-Server läuft.
- 5520 lädt NonSinnPublicCore 0.4.0 mit dauerhafter Gästeanzeige, LuckPerms 5.5.53, GlymeraPermissions 2.0.0, QuestLines Claims 1.5.0 und GlymeraPlotWorld 17.0.0.
- Vorhandene Weltdaten auf 5520: `default`, Glutwacht (technisch `himmelsinsel`), `oakhaven`, `schwebende_inseln` und `under`.
- `bauwelt` ist konfiguriert, aber noch nicht erzeugt oder praktisch abgenommen.
- GlymeraLimbo ist aktiv; die Limbo-Welt wird erst über ihren vorgesehenen Zugang erzeugt.
- Survival-Claims sind für Spieler nur in `default` freigegeben: 3 Start-Chunks, maximal 9, 32 Claim-Scherben pro Erweiterung.
- Die öffentliche Economy besitzt 8 kontrollierte Ankaufquellen. Gewöhnliche Materialien, Waffen, Werkzeuge und Ausrüstung haben keinen Ankaufspreis.
- Worker und Farmer dürfen auf 5520 je eine aktive Truhe verwenden und nicht automatisch craften; Nicht-OP-Spieler dürfen einen Chunkloader setzen.
- GlymeraStatues ist wegen fehlerhafter schwarzer Blöcke deaktiviert. GlymeraRaces ist wegen Überschneidungen mit MMO Skill Tree und Natural20 nicht installiert.

Die aktuelle Spieleransicht steht im [Statusbereich der Website](https://nonsindeads.github.io/hytale-public-server/status.html). Technische Abnahmepunkte stehen in [docs/betrieb-und-freigabe.md](docs/betrieb-und-freigabe.md).

## Repository-Struktur

| Pfad | Inhalt |
|---|---|
| `docs/` | GitHub-Pages-Spielerhandbuch und technische Hintergrundtexte |
| `config/` | Versionierte Zielkonfigurationen für Rollen, Claims, Economy, Automation und Bauwelt |
| `economy/` | Maschinenlesbare Balanceregeln |
| `mods/mod-lock.json` | Lockdaten der eigens ergänzten öffentlichen Schutzschicht; derzeit kein vollständiger Modpack-Lock |
| `mods/NonSinnPublicCore/` | Quellcode für Gastschutz, Regeltest und gestaffelten Grundstückskauf |
| `scripts/` | Struktur-, Händler- und Progressionsprüfungen |
| `.github/ISSUE_TEMPLATE/` | Formulare für Fehler und Vorschläge |

## Lokale Prüfungen

Voraussetzung ist Python 3. Die Skripte verändern weder Server noch Konfigurationen.

```bash
python3 scripts/validate_configs.py
python3 scripts/audit_merchants.py
python3 scripts/audit_progression_economy.py
```

Die Prüfungen decken unter anderem ungültiges JSON, doppelte Waren, negative Preise, direkten Wiederverkauf, Händler-Arbitrage, nicht freigegebene Ankaufquellen, Claim-Scherben, Plotpreise und Automationsgrenzen ab.

## Sicherheitsgrenzen

- Keine Secrets, Tokens, privaten Spielerinformationen oder vollständigen Produktionsdaten committen.
- Normale reproduzierbare Fehler gehören in ein [GitHub Issue](https://github.com/nonsindeads/hytale-public-server/issues/new/choose).
- Duplikations-, Economy- und Rechte-Exploits werden über eine [private Security Advisory](https://github.com/nonsindeads/hytale-public-server/security/advisories/new) gemeldet.
- Ein erfolgreicher Start beweist nicht, dass Rechte, Claims oder Economy im Spiel korrekt funktionieren. Dafür ist der Zwei-Account-Praxistest verpflichtend.

Hytale und alle genannten Mods gehören ihren jeweiligen Rechteinhabern. Dieses Projekt ist weder mit Hypixel Studios noch mit den Mod-Autoren offiziell verbunden.
