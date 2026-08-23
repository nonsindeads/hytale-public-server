# Hytale Public Server

Dieses Repository ist die nachvollziehbare Quelle fuer Dokumentation, freigabefaehige Serverkonfigurationen, Mod-Versionen, Rechte, Onboarding und Economy-Regeln.

## Grundsaetze

- Welten, Spielerdateien, Datenbanken, Zugangsdaten, Tokens und Mod-JARs werden nicht eingecheckt.
- Neue Spieler beginnen als `gast` und werden erst nach dem Regel-Onboarding zu `spieler`.
- Wirtschaftsaenderungen werden erst nach automatischem Zyklus- und Arbitrage-Test aktiviert.
- `default` bleibt Abenteuerwelt und erhaelt geschuetzte, zusammenhaengende Spieler-Claims.
- Floating Islands, Under und Limbo sind Hochrisiko-Welten; der Hub bleibt sicher.
- Sicherheits- und Economy-Exploits werden nicht als oeffentliche Issues mit reproduzierbaren Details gemeldet.

## Verzeichnisstruktur

- `docs/`: oeffentliche HTML-Dokumentation und Changelog
- `config/`: freigabefaehige Rollen-, Onboarding- und Claimvorgaben
- `economy/`: Balanceregeln und spaetere Preislisten
- `mods/`: Mod-Lockdatei mit Versionen, Quellen und Hashes
- `.github/`: Formulare fuer Fehler und Vorschlaege
- `scripts/`: lokale, nicht veraendernde Validierungswerkzeuge

Die drei externen Public-Layer-Mods und `NonSinnPublicCore` wurden am 23.08.2026 mit Server 0.5.8 gemeinsam in einer isolierten Laufzeit erfolgreich geladen. Floating Islands mit Structures wurde ebenfalls freigegeben. Die Zielinstanz bleibt bis zur geschlossenen Abnahme offline.

`NonSinnPublicCore` stellt beim Start die LuckPerms-Basisgruppen `gast`, `spieler`, `moderator`, `admin` und `owner` sowie die Owner-Zuweisung fuer NonSinn sicher. Die Operation ist idempotent und schreibt ueber die LuckPerms-API statt direkt in deren Datenbank.

Die produktiven Grundeinstellungen liegen unter `config/server/`. Der Start ist bewusst auf 25 gleichzeitige Spieler und 8 GB maximalen Java-Speicher begrenzt; nach Lastmessungen kann beides angehoben werden.

Worker und Farmer sind pro Station eng begrenzt und duerfen im Public-Profil nicht automatisch craften. Pro Spieler ist hoechstens ein Chunkloader vorgesehen. Dadurch bleibt Automation nuetzlich, ohne zusaetzliche Rezept- oder Geldzyklen zu erzeugen.

`scripts/patch_aetherhaven_058.py` enthaelt den reproduzierbaren Kompatibilitaetsfix fuer Aetherhaven 2.7.2: Die in Hytale 0.5.8 entfernte Bardenanimation wird auf eine vorhandene Gespraechsanimation abgebildet. Das Skript akzeptiert ausschliesslich den dokumentierten Original-Hash und legt vor der Aenderung eine Sicherung an.

## Freigabereihenfolge

1. Mod-Lock und Konfigurationssnapshot erzeugen.
2. Rechte-, Onboarding- und Plot-Tests mit `gast` und `spieler` ausfuehren.
3. Economy-Audit ohne offene Fehler abschliessen.
4. Geschlossenen Abnahmestart auf dem Zielserver durchfuehren.
5. Erst danach den Gastzugang oeffnen.

## Lokale Pruefung

```bash
python3 scripts/validate_configs.py
python3 scripts/audit_merchants.py
```
