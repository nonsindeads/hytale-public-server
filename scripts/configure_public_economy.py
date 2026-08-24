#!/usr/bin/env python3
"""Apply the reproducible public-economy profile to merchant and MMO bounty data."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MERCHANT = ROOT / "config/economy/GlymeraMerchant.public.json"


def load(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def save(path: Path, data: dict) -> None:
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def configure_merchant(path: Path) -> tuple[int, int]:
    merchant = load(path)
    price_profile = load(ROOT / "config/economy/permanent-sell-prices.json")
    prices = price_profile["prices"]
    seen: set[str] = set()

    for store in merchant.get("merchants", {}).values():
        for trade in store.get("trades", []):
            item_id = trade.get("itemId")
            if item_id in prices:
                if item_id in seen:
                    raise ValueError(f"Doppelter dauerhaft verkaufbarer Gegenstand: {item_id}")
                trade["sellPrice"] = prices[item_id]
                seen.add(item_id)

    missing = sorted(set(prices) - seen)
    if missing:
        raise ValueError("Verkaufspreise ohne Handelszeile: " + ", ".join(missing))

    all_sales = sorted(
        trade["itemId"]
        for store in merchant.get("merchants", {}).values()
        for trade in store.get("trades", [])
        if trade.get("sellPrice", 0) > 0
    )
    merchant["_guide"]["balanceProfile"] = "NonSinn Public RPG v2 - connected reward economy"
    merchant["_guide"]["pricingRule"] = (
        "Low-tier food, crafted consumables and wood/stone/copper equipment have small permanent "
        "Gold values. Damaged equipment scales by remaining durability. Mass blocks are accepted only "
        "through rotating hourly, daily and weekly procurement contracts."
    )
    merchant["_publicBalance"] = {
        "ordinaryItemSales": "curated-low-value-only",
        "massMaterialSales": "rotating-board-contracts-only",
        "damagedEquipment": "floor(baseSellPrice * remainingDurabilityRatio)",
        "allowedSellItems": all_sales,
        "auditedAt": "2026-08-24",
        "directArbitrage": "pending-script-audit",
        "crossMerchantArbitrage": "pending-script-audit",
        "reason": (
            "Everyday play should produce modest Gold while quotas on procurement contracts protect "
            "the economy from unlimited bulk and automation output."
        ),
    }
    save(path, merchant)
    return len(prices), len(all_sales)


def gold_reward(amount: int) -> dict:
    return {
        "type": "COMMAND",
        "command": f"/nspceconomyreward {{player}} {amount} bounty",
        "displayName": f"{amount} Gold",
        "description": "Serverweite Goldbelohnung",
        "icon": "Ingredient_Bar_Gold",
    }


def configure_bounties(pack: Path) -> tuple[int, int]:
    policy = load(ROOT / "config/bounties/reward-policy.json")
    procurement = load(ROOT / "config/bounties/procurement-quests.json")["quests"]
    quest_dir = pack / "Server/MMOSkillTree/Quests"
    board_file = pack / "Server/MMOSkillTree/BountyBoards/Bihourly.json"
    if not quest_dir.is_dir() or not board_file.is_file():
        raise FileNotFoundError(f"Kein gueltiges MMOSkillBountyPack: {pack}")

    board = load(board_file)
    board["Payload"]["rotation"]["period"] = f"{policy['hourlyRotationSeconds']}s"
    save(board_file, board)

    authored = 0
    for path in sorted(quest_dir.glob("*.json")):
        data = load(path)
        payload = data.get("Payload", {})
        params = payload.get("params", {})
        board_id = params.get("boards")
        difficulty = params.get("difficulty")
        amount = policy["goldByBoardAndDifficulty"].get(board_id, {}).get(difficulty)
        if amount is None:
            continue
        rewards = payload.setdefault("rewards", [])
        rewards[:] = [
            reward for reward in rewards
            if not (
                reward.get("type") == "COMMAND"
                and "nspceconomyreward" in reward.get("command", "")
            )
        ]
        rewards.append(gold_reward(amount))
        save(path, data)
        authored += 1

    for quest in procurement:
        cooldown = {
            "bihourly": policy["hourlyRotationSeconds"],
            "daily": 79_200,
            "weekly": 561_600,
        }[quest["board"]]
        amount = policy["goldByBoardAndDifficulty"][quest["board"]][quest["difficulty"]]
        data = {
            "Name": quest["name"],
            "Payload": {
                "extends": "bounty_turnin_standard",
                "id": quest["id"],
                "cooldownSeconds": cooldown,
                "params": {
                    "boards": quest["board"],
                    "difficulty": quest["difficulty"],
                    "weight": str(quest["weight"]),
                    "target": quest["item"],
                },
                "objectiveOverrides": {"main": {"amount": quest["amount"]}},
                "rewards": [
                    {"type": "CURRENCY", "currencyId": "bounty_token", "amount": quest["tokens"]},
                    {"type": "XP", "skill": quest["xpSkill"], "amount": quest["xp"]},
                    gold_reward(amount),
                ],
            },
        }
        save(quest_dir / f"{quest['name']}.json", data)

    locale_rows = {
        "de-DE": [(q["id"], q["titleDe"], q["flavorDe"]) for q in procurement],
        "en-US": [(q["id"], q["titleEn"], q["flavorEn"]) for q in procurement],
    }
    marker = "# NonSinn procurement contracts"
    for locale, rows in locale_rows.items():
        path = pack / f"Server/Languages/{locale}/mmoskilltree.lang"
        current = path.read_text(encoding="utf-8")
        current = current.split(marker, 1)[0].rstrip()
        extra = ["", marker]
        for quest_id, title, flavor in rows:
            extra.append(f"quest.{quest_id}.title = {title}")
            extra.append(f"quest.{quest_id}.flavor = {flavor}")
        path.write_text(current + "\n" + "\n".join(extra) + "\n", encoding="utf-8")

    return authored, len(procurement)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--merchant", type=Path, default=DEFAULT_MERCHANT)
    parser.add_argument("--bounty-pack", type=Path)
    args = parser.parse_args()

    permanent, total = configure_merchant(args.merchant)
    print(f"MERCHANT: {permanent} neue Preisvorgaben, {total} dauerhaft verkaufbare Items")
    if args.bounty_pack:
        existing, procurement = configure_bounties(args.bounty_pack)
        print(f"BOUNTIES: {existing} bestehende Auftraege mit Gold, {procurement} Beschaffungsauftraege")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
