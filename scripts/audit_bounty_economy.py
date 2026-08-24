#!/usr/bin/env python3
"""Audit MMO bounty rotations, procurement quotas and central Gold rewards."""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
COMMAND_RE = re.compile(r"^/nspceconomyreward \{player\} ([1-9][0-9]{0,3}) bounty$")


def load(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("bounty_pack", type=Path)
    args = parser.parse_args()

    policy = load(ROOT / "config/bounties/reward-policy.json")
    expected_procurement = {
        row["id"]: row for row in load(ROOT / "config/bounties/procurement-quests.json")["quests"]
    }
    quest_dir = args.bounty_pack / "Server/MMOSkillTree/Quests"
    failures: list[str] = []
    counts: Counter[str] = Counter()
    gold_totals: Counter[str] = Counter()
    found_procurement: set[str] = set()

    board = load(args.bounty_pack / "Server/MMOSkillTree/BountyBoards/Bihourly.json")
    period = board.get("Payload", {}).get("rotation", {}).get("period")
    if period != f"{policy['hourlyRotationSeconds']}s":
        failures.append(f"Schnellauftraege rotieren nach {period} statt stuendlich")

    for path in sorted(quest_dir.glob("*.json")):
        payload = load(path).get("Payload", {})
        params = payload.get("params", {})
        board_id = params.get("boards")
        difficulty = params.get("difficulty")
        expected_gold = policy["goldByBoardAndDifficulty"].get(board_id, {}).get(difficulty)
        if expected_gold is None:
            continue
        counts[board_id] += 1
        commands = []
        for reward in payload.get("rewards", []):
            if reward.get("type") != "COMMAND":
                continue
            match = COMMAND_RE.match(reward.get("command", ""))
            if match:
                commands.append(int(match.group(1)))
        if commands != [expected_gold]:
            failures.append(f"{path.name}: Goldbelohnung {commands}, erwartet {expected_gold}")
        else:
            gold_totals[board_id] += expected_gold

        quest_id = payload.get("id")
        if quest_id in expected_procurement:
            found_procurement.add(quest_id)
            objectives = payload.get("objectiveOverrides", {})
            if payload.get("extends") != "bounty_turnin_standard":
                failures.append(f"{quest_id}: kein verbrauchender TURN_IN-Auftrag")
            if objectives.get("main", {}).get("amount") != expected_procurement[quest_id]["amount"]:
                failures.append(f"{quest_id}: falsches persönliches Abgabelimit")

    missing = sorted(set(expected_procurement) - found_procurement)
    if missing:
        failures.append("Fehlende Beschaffungsauftraege: " + ", ".join(missing))

    print("BOUNTY-ECONOMY")
    print(f"  Auftraege nach Board: {dict(counts)}")
    print(f"  Beschaffungsauftraege: {len(found_procurement)}")
    print("  Goldstufen: " + json.dumps(policy["goldByBoardAndDifficulty"], ensure_ascii=False))
    print("  Schnellauftrag rotiert alle 60 Minuten; Abgabe und Belohnung sind pro Quest begrenzt.")
    if failures:
        for failure in failures:
            print("FAIL: " + failure)
        return 1
    print("PASS: alle Board-Auftraege besitzen genau eine begrenzte, Vault-gebundene Goldbelohnung")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
