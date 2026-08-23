#!/usr/bin/env python3
"""Validate and calculate every configured land-progression Gold sink."""

from __future__ import annotations

import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def load_json(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def load_conf(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def main() -> int:
    merchant = load_json(ROOT / "config/economy/GlymeraMerchant.public.json")
    plots = load_json(ROOT / "config/plots/GlymeraPlotWorld.json")
    pricing = load_json(ROOT / "config/plots/property-pricing.json")
    claims = load_conf(ROOT / "config/claims/questlinesclaims.conf")
    failures: list[str] = []

    plot_prices = pricing.get("plotPrices", [])
    if not plot_prices or any(not isinstance(value, int) or value < 0 for value in plot_prices):
        failures.append("Grundstueckspreise muessen nichtnegative ganze Zahlen sein")
    elif plot_prices[0] != 0:
        failures.append("Das erste Bauwelt-Grundstueck muss kostenlos sein")
    elif any(right <= left for left, right in zip(plot_prices[1:], plot_prices[2:])):
        failures.append("Die bezahlten Grundstuecksstufen muessen strikt teurer werden")

    if plots.get("maxPlotsPerPlayer") != len(plot_prices):
        failures.append("maxPlotsPerPlayer stimmt nicht mit den Preisstufen ueberein")
    if plots.get("claimRequiresToken") is not True or plots.get("claimRecipeEnabled") is not False:
        failures.append("Plot Deeds muessen kontrolliert und nicht craftbar sein")
    if plots.get("mergeRequiresToken") is not False:
        failures.append("Zusammenfuehren darf keine zweite Flaechengebuehr erzeugen")
    if plots.get("economy", {}).get("use") is not False:
        failures.append("Die wirkungslose PlotWorld-Festpreis-Economy muss deaktiviert bleiben")

    all_trades = [
        (merchant_id, trade)
        for merchant_id, data in merchant.get("merchants", {}).items()
        for trade in data.get("trades", [])
    ]
    shard_trades = [row for row in all_trades if row[1].get("itemId") == "QuestLinesClaims_ClaimShard"]
    if len(shard_trades) != 1:
        failures.append("Es muss genau eine kontrollierte Claim-Scherben-Quelle geben")
        shard_price = 0
    else:
        shard_price = shard_trades[0][1].get("buyPrice", 0)
        if not isinstance(shard_price, int) or shard_price <= 0 or shard_trades[0][1].get("sellPrice") != 0:
            failures.append("Claim-Scherben muessen einen positiven Kaufpreis und keinen Verkaufspreis haben")

    forbidden_tokens = {
        "GlymeraPlotWorld_Deed",
        "GlymeraPlotWorld_MergePermit",
        "GlymeraPlotWorld_StylePermit",
    }
    for merchant_id, trade in all_trades:
        if trade.get("itemId") in forbidden_tokens:
            failures.append(f"{merchant_id}: lose Plot-Tokens duerfen nicht gehandelt werden")

    try:
        default_chunks = int(claims["default_max_chunks"])
        maximum_chunks = int(claims["max_chunks_cap"])
        shards_per_chunk = int(claims["chunk_cost_shards"])
    except (KeyError, ValueError):
        failures.append("Survival-Claim-Limits sind unvollstaendig")
        default_chunks = maximum_chunks = shards_per_chunk = 0

    if not (0 < default_chunks < maximum_chunks):
        failures.append("Survival muss kostenlose Start-Chunks und ein hoeheres festes Limit haben")
    if shards_per_chunk <= 0:
        failures.append("Survival-Erweiterungen muessen Claim-Scherben verbrauchen")

    start_balance = merchant.get("startBalance", 0)
    if len(plot_prices) > 1 and start_balance >= plot_prices[1]:
        failures.append("Startguthaben darf das zweite Bauwelt-Grundstueck nicht finanzieren")

    survival_extra_chunks = max(0, maximum_chunks - default_chunks)
    survival_shards = survival_extra_chunks * shards_per_chunk
    survival_gold = survival_shards * shard_price
    plot_total = sum(plot_prices)

    print("LAND-ECONOMY")
    print(f"  Survival: {default_chunks} Start-Chunks, {maximum_chunks} Maximum")
    print(f"  Survival-Ausbau: {survival_extra_chunks} Chunks x {shards_per_chunk} Scherben x {shard_price} Gold = {survival_gold} Gold")
    print("  Bauwelt-Stufen: " + " -> ".join(str(value) for value in plot_prices) + " Gold")
    print(f"  Bauwelt-Gesamtsenke fuer {len(plot_prices)} Grundstuecke: {plot_total} Gold")
    print(f"  Gesamte Landsenke pro voll ausgebautem Spieler: {survival_gold + plot_total} Gold")

    positive_sales = {
        trade["itemId"]: trade["sellPrice"]
        for _, trade in all_trades
        if trade.get("sellPrice", 0) > 0
    }
    for item_id in ("Food_Fish_Raw_Legendary", "Hedera_Gem", "Dragon_Heart"):
        value = positive_sales.get(item_id)
        if value:
            print(f"  Gegenwert {item_id}: Bauwelt gesamt {plot_total / value:.2f}, kompletter Landausbau {(plot_total + survival_gold) / value:.2f}")

    if failures:
        for failure in failures:
            print("FAIL: " + failure)
        return 1
    print("PASS: Landpreise sind progressiv, begrenzt und ohne Rueckverkaufsweg")
    return 0


if __name__ == "__main__":
    sys.exit(main())
