# Economy-Audit vom 23.08.2026

## Ergebnis

Die vollstaendig konfigurierte Goldwirtschaft ist statisch geschlossen: Gewoehnliche Materialien, Ausruestung, Waffen und Werkzeuge erzeugen kein Gold. Es existiert kein direkter oder haendleruebergreifender Kauf-Verkauf-Gewinn. Lose Plot-Tokens sind weder craftbar noch handelbar.

## Kontrollierte Goldquellen

| Gegenstand | Ankauf |
|---|---:|
| Aetherhaven-Goldmuenze | 1 Gold |
| Ungewoehnlicher Fisch | 8 Gold |
| Seltener Fisch | 20 Gold |
| Epischer Fisch | 50 Gold |
| Legendaerer Fisch | 120 Gold |
| Waldessenz | 140 Gold |
| Hedera-Edelstein | 320 Gold |
| Drachenherz | 800 Gold |

Alle anderen 154 Handelszeilen sind reine Goldsenken oder deaktivierte Eintraege. Beschaedigte Waffen und Werkzeuge bleiben unverkaeuflich, weil ihr Basis-Ankaufswert null ist. Verzauberungen erzeugen keinen zusaetzlichen Verkaufswert.

## Landkosten

| Ausbau | Rechnung | Gesamt |
|---|---:|---:|
| Survival von 3 auf 9 Chunks | 6 Chunks x 32 Scherben x 5 Gold | 960 Gold |
| Bauwelt-Grundstuecke | 0 + 1.000 + 3.000 + 7.500 Gold | 11.500 Gold |
| Voller Landausbau | 960 + 11.500 Gold | 12.460 Gold |

Der vollstaendige Landausbau entspricht beispielsweise rund 104 legendaeren Fischen, 39 Hedera-Edelsteinen oder 16 Drachenherzen. Diese Gegenwerte sind keine Drop-Prognose, sondern nur ein Preisvergleich.

## Automatisierung

- Worker und Farmer duerfen jeweils hoechstens eine aktive Truhe verwenden.
- Automatisches Crafting ist deaktiviert.
- Pro Spieler ist hoechstens ein Chunkloader vorgesehen.
- Gewoehnliche Worker-, Mining- und Farmer-Ertraege haben keinen Gold-Ankaufswert und koennen deshalb keinen direkten automatischen Geldkreislauf bilden.

## Automatische Pruefungen

```text
python3 scripts/validate_configs.py
python3 scripts/audit_merchants.py
python3 scripts/audit_progression_economy.py
```

Die Pruefungen kontrollieren JSON-Struktur, doppelte Handelswaren, negative Preise, direkten Wiederverkauf, haendleruebergreifende Arbitrage, erlaubte Ankaufsquellen, Claim-Scherben, Plot-Tokens, aufsteigende Grundstueckspreise und die Gesamtkosten.

## Praktische Abnahme

Statisch nicht beweisbar sind die tatsaechlichen seltenen Drops pro Spielstunde, Questbelohnungen pro Stunde und Laufzeit-Duplikationsfehler. Diese Werte werden nach dem Start mit echten Spielsitzungen beobachtet; bei zu hoher Geldmenge werden ausschliesslich kontrollierte Ankaufspreise oder Dropquellen angepasst.
