# Changelog

## In Vorbereitung

- Oeffentliche Serverstruktur definiert
- Gast-Onboarding und Regeltest vorbereitet
- Rechtekonzept mit LuckPerms und Command-Failsafe vorbereitet
- Geschuetzte Spieler-Claims auf der Default-Welt vorgesehen
- Economy-Auditregeln und GitHub-Issueformulare angelegt

## 0.2.3 - 23.08.2026

- QuestLines Claims ist nur in der Welt `default` fuer Spieler freigegeben.
- Spieler starten mit 5 Claim-Chunks und koennen bis maximal 25 Chunks erweitern.
- Claim-Erweiterungen kosten standardmaessig 32 Claim Shards pro Chunk.

## 0.2.2 - 23.08.2026

- Spieler erhalten nicht mehr versehentlich die Umgehungsberechtigung der Befehls-Whitelist.
- Sichere Spielbefehle fuer Claims, Economy und installierte Gameplay-Mods sind explizit freigegeben.
- Administrative Hauptbefehle wie `/op`, `/merchant` und `/nonsinn` bleiben fuer normale Spieler gesperrt.
## 0.2.1 - 23.08.2026

- Die LuckPerms-Initialisierung laeuft nun in der Startphase, nachdem LuckPerms seine API bereitgestellt hat.

## 0.2.0 - 23.08.2026

- LuckPerms-Basisgruppen und Vererbungen werden beim Start reproduzierbar sichergestellt.
- NonSinn wird anhand der festen Spieler-UUID der Gruppe `owner` zugewiesen.
- Der Build erwartet neben dem Hytale-Server-JAR nun auch das LuckPerms-JAR als Compile-Abhaengigkeit.
