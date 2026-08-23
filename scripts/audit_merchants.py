#!/usr/bin/env python3
"""Fail closed on direct and cross-merchant Gold arbitrage."""

from __future__ import annotations

import argparse
import json
import sys
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONFIG = ROOT / "config/economy/GlymeraMerchant.public.json"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("config", nargs="?", type=Path, default=DEFAULT_CONFIG)
    args = parser.parse_args()

    with args.config.open("r", encoding="utf-8") as handle:
        config = json.load(handle)

    failures: list[str] = []
    warnings: list[str] = []
    rows: list[dict] = []
    by_item: dict[str, list[dict]] = defaultdict(list)

    for merchant_id, merchant in config.get("merchants", {}).items():
        for trade in merchant.get("trades", []):
            item_id = trade.get("itemId")
            buy = trade.get("buyPrice", 0)
            sell = trade.get("sellPrice", 0)
            row = {"merchant": merchant_id, "item": item_id, "buy": buy, "sell": sell}
            rows.append(row)
            by_item[item_id].append(row)

            if not item_id:
                failures.append(f"{merchant_id}: Handel ohne itemId")
            if not isinstance(buy, int) or not isinstance(sell, int):
                failures.append(f"{merchant_id}/{item_id}: Preise muessen ganze Zahlen sein")
                continue
            if buy < 0 or sell < 0:
                failures.append(f"{merchant_id}/{item_id}: negativer Preis")
            if buy == 0 and sell == 0:
                warnings.append(f"{merchant_id}/{item_id}: weder Kauf noch Verkauf aktiv")
            if buy > 0 and sell >= buy:
                failures.append(f"{merchant_id}/{item_id}: direkter Gewinn buy={buy}, sell={sell}")

    for item_id, item_rows in by_item.items():
        buys = [row for row in item_rows if row["buy"] > 0]
        sells = [row for row in item_rows if row["sell"] > 0]
        if not buys or not sells:
            continue
        cheapest = min(buys, key=lambda row: row["buy"])
        best = max(sells, key=lambda row: row["sell"])
        if best["sell"] >= cheapest["buy"]:
            failures.append(
                f"{item_id}: haendleruebergreifender Gewinn "
                f"buy={cheapest['buy']} bei {cheapest['merchant']}, "
                f"sell={best['sell']} bei {best['merchant']}"
            )

    allowed_sell_items = {
        "Aetherhaven_Gold_Coin",
        "Food_Fish_Raw_Uncommon",
        "Food_Fish_Raw_Rare",
        "Food_Fish_Raw_Epic",
        "Food_Fish_Raw_Legendary",
        "Ingredient_Forest_Essence",
        "Hedera_Gem",
        "Dragon_Heart",
    }
    disallowed_sales = [
        row for row in rows if row["sell"] > 0 and row["item"] not in allowed_sell_items
    ]
    for row in disallowed_sales:
        failures.append(
            f"{row['merchant']}/{row['item']}: Ankauf ist nicht auf der seltenen Freigabeliste"
        )

    print(
        f"AUDIT: {len(config.get('merchants', {}))} Haendler, "
        f"{len(rows)} Handelszeilen, {len(by_item)} eindeutige Items"
    )
    for warning in warnings:
        print(f"WARN: {warning}")
    if failures:
        for failure in failures:
            print(f"FAIL: {failure}")
        return 1
    print("PASS: kein direkter oder haendleruebergreifender Gold-Arbitrageweg")
    return 0


if __name__ == "__main__":
    sys.exit(main())
