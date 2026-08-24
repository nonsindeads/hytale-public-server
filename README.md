# Hytale Public Server (Repository & Configurations)

Dieses Repository enthält die nachvollziehbare Quelle für Serverkonfigurationen, Mod-Lockdateien, Rechte, Audits und Validierungswerkzeuge des offiziellen Hytale-Servers **Der Waldbrand**.

🌐 **Öffentliche Landingpage & Spieler-Dokumentation:**  
👉 **[https://nonsindeads.github.io/hytale-public-server/](https://nonsindeads.github.io/hytale-public-server/)**

---

## 🏗️ Verzeichnisstruktur

- `docs/`: Quellcode der öffentlichen HTML-Dokumentation & Landingpage (`github.io`)
- `config/`: Rollen-, Onboarding-, Claim- & Server-Konfigurationen (`Server 20`)
- `economy/`: Balanceregeln & Händlerpreis-Audits (`GlymeraMerchant`)
- `mods/`: Mod-Lockdatei mit Versionen, Hashes und Quellen
- `scripts/`: Lokale Validierungs- & Audit-Skripte

---

## ⚙️ Technische Grundsätze & Deployment

- **Server 20 (Produktiv):** IP `65.109.56.119:5520`, Max. 25 Spieler (8 GB JVM Heap), systemd Service `hytale.service`.
- **Wirtschaft:** Anti-Inflations-Regelwerk (`sellPrice = 0` für Grundressourcen & Erze; Verkauf nur für gezielte Trophäen und seltene Fische).
- **WorldMap Claims:** Alle Welten nutzen `"WorldMap": { "Type": "QuestLinesClaims" }`.
- **Welten-Setup:**
  - `himmelsinsel`: Geschützter öffentlicher Hub & Marktplatz
  - `bauwelt`: Dauerhafte Plot- & Bauwelt
  - `default`: Resetbare Survivalwelt
  - `schwebende_inseln`, `under`, `limbo`: Abenteuer- & High-Risk-Welten

---

## 🔍 Lokale Pruefwerkzeuge

```bash
python3 scripts/validate_configs.py
python3 scripts/audit_merchants.py
python3 scripts/audit_progression_economy.py
```
