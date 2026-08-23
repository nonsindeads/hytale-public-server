# Betrieb und Freigabe

## Noch nicht aktivieren

Floating Islands mit Structures ist freigegeben. Der Zielserver auf Port 5520 bleibt bis zum abgeschlossenen Rechte-, Onboarding-, Claim- und Economy-Abnahmetest offline. Der getrennte Ordner `/home/hytale/public-overlay` ist nur eine Bereitstellung und kein laufender Server.

## Reihenfolge nach der Inhaltsfreigabe

1. Testserver sauber stoppen und einen abschliessenden Snapshot erzeugen.
2. Snapshot als neue Instanz `/home/hytale/hytale-server-2` wiederherstellen.
3. Zielinstanz zunaechst nur auf `127.0.0.1` bzw. einem nicht oeffentlichen Abnahmeport starten.
4. LuckPerms, GlymeraPermissions und QuestLines Claims aus dem geprueften Overlay installieren.
5. `config/permissions/glymera-permissions.json` als `mods/de.glymera_GlymeraPermissions/config.json` ablegen.
6. `config/claims/questlinesclaims.conf` als oeffentliches Claim-Profil anwenden.
7. `config/economy/GlymeraMerchant.public.json` als Haendlerkonfiguration einspielen; gewoehnliche Gegenstaende duerfen keinen Verkaufspreis haben.
8. LuckPerms-Gruppen mit `config/permissions/luckperms-bootstrap.txt` anlegen.
9. Spawn-Safezone mit dem spaeter festgelegten Umfang erstellen und Claims mit Gast- und Spieler-Testaccount pruefen.
10. Economy-, Rechte-, Duplikations- und Rollback-Tests abschliessen.
11. Erst danach 5520 wieder oeffentlich binden und den Gastzugang freigeben.

## Pflichtpruefungen

- `gast`: nur Himmelsinsel, Regeln und Freischaltung; kein Handeln, Bauen, Crafting, Loot, Kampf, Portal oder Teleport.
- `spieler`: Claims nur auf `default`, anfangs 5 Chunks und maximal 25; keine Moderationsbefehle.
- `moderator`: keine Economy-, Dateisystem-, OP- oder Wildcard-Rechte.
- Claims: Zusammenhaengende Erweiterung, Mitglieder, Fluessigkeiten, Tiere, Portale, Explosionen und Spawn-Safezone testen.
- Economy: `scripts/audit_merchants.py` muss bestehen; Rezeptzyklen, Worker/Farmer/Chunkloader und Beute pro Stunde werden separat gemessen.
- Wiederherstellung: vor Freigabe einen Restore in einen leeren Testpfad durchspielen.

## Onboarding

Die HTML-Seite ist Dokumentation, keine Sicherheitsgrenze. Die Freischaltung muss serverseitig erfolgen: zufaellige Fragen, Cooldown, Speicherung von UUID und Regelversion, danach Zuweisung der LuckPerms-Gruppe `spieler`. Bei einer neuen wesentlichen Regelversion kann eine erneute Bestaetigung verlangt werden.

Bis `NonSinnPublicCore` auf der fertigen Kopie integriert und mit zwei Testaccounts geprueft ist, bleibt der Server nicht oeffentlich erreichbar.
