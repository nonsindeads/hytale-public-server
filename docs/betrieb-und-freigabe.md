# Betrieb und Freigabe

Stand: 24.08.2026

## Aktueller Betrieb

- Community-Server 5520: Dienst aktiv und öffentlich gebunden; praktische Abnahme läuft.
- Die früher beschriebene Overlay-/Wiederherstellungsphase ist beendet. `/home/hytale/hytale-server-2` ist die laufende 5520-Instanz.

## Noch offene Abnahme

1. Gast- und Spielerrechte mit einem zweiten Account prüfen: Bauen, Abbauen, Interagieren, Container, Handel, Crafting, Gegenstände, Kampf, Portale und Teleports.
2. Regeltest einschließlich Fehlversuch, Cooldown, Wiederanmeldung, Gruppenwechsel und erneuter Regelversion testen.
3. Bauwelt erzeugen, Hub-Portal bauen und alle Grundstückspreise 0/1.000/3.000/7.500 Gold testen.
4. Bauen und Abbauen außerhalb eigener oder freigegebener Bauwelt-Plots muss scheitern; Trust, Untrust, Home, Merge, Flüssigkeiten und Kreaturen prüfen.
5. Claims in `default` mit Mitgliedern, Tieren, Flüssigkeiten, Explosionen, Portalen, 30-Tage-Ablauf und Reset-Bereinigung testen. Vor Claim-Heimteleports die Berechtigung `questlinesclaims.home.use` bewusst freigeben oder die Funktion deaktiviert lassen.
6. Radius der Default-Spawn-Safezone festlegen, technisch schützen und mit Spielerrechten testen.
7. Economy-Audits ausführen und seltene Drops sowie Questbelohnungen zusätzlich über echte Spielzeit messen.
8. Abschlussbackup erstellen und eine Wiederherstellung in einen leeren Testpfad durchspielen.

## Freigabekriterien

- Keine Plugin-Ladefehler der öffentlichen Schutzschicht.
- Gast kann ausschließlich den vorgesehenen Einstieg und Regeltest nutzen.
- Spieler kann nur in `default` claimen und keine Admin-, Economy- oder Wildcard-Rechte erhalten.
- Grundstückskauf belastet Gold und Claim atomar: entweder beides erfolgreich oder keines von beiden.
- Gewöhnliche Materialien und Ausrüstung bleiben unverkäuflich; kein direkter oder händlerübergreifender Gewinnzyklus.
- Backup und Restore sind mit aktuellem Stand erfolgreich geprüft.

## Bekannte technische Hinweise

- Der Aetherhaven-Bard-Modellfehler ist auf 5520 korrigiert. Eine fehlende optionale `bard_songs.json` wird weiterhin als Warnung protokolliert.
- Geladene Plugins beweisen nicht, dass abhängige Welten bereits erzeugt wurden: Das betrifft aktuell `bauwelt` und `limbo`.
- GlymeraStatues bleibt deaktiviert; GlymeraRaces bleibt uninstalliert.
