#!/usr/bin/env python3
"""Read-only validation for the public Hytale server repository."""

from __future__ import annotations

import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def main() -> int:
    failures: list[str] = []
    json_files = sorted(ROOT.rglob("*.json"))

    for path in json_files:
        try:
            load_json(path)
        except Exception as exc:  # validation should report every broken file
            failures.append(f"{path.relative_to(ROOT)}: {exc}")

    questions_path = ROOT / "config/onboarding/questions.json"
    questions = load_json(questions_path)
    ids: set[str] = set()
    for index, question in enumerate(questions.get("questions", [])):
        question_id = question.get("id")
        answers = question.get("answers", [])
        correct = question.get("correctIndex")
        if not question_id or question_id in ids:
            failures.append(f"questions[{index}]: fehlende oder doppelte ID")
        ids.add(question_id)
        if len(answers) < 2:
            failures.append(f"questions[{index}]: weniger als zwei Antworten")
        if not isinstance(correct, int) or not 0 <= correct < len(answers):
            failures.append(f"questions[{index}]: correctIndex ungueltig")

    roles = load_json(ROOT / "config/permissions/roles.json")
    default_role = roles.get("defaultRole")
    if default_role not in roles.get("roles", {}):
        failures.append("roles.json: defaultRole existiert nicht")

    economy = load_json(ROOT / "economy/balance-rules.json")
    spread = economy.get("rules", {}).get("minimumBuySellSpreadPercent")
    if not isinstance(spread, (int, float)) or spread < 0:
        failures.append("balance-rules.json: ungueltiger Buy/Sell-Spread")

    if failures:
        print("VALIDATION FAILED")
        for failure in failures:
            print(f"- {failure}")
        return 1

    print(f"OK: {len(json_files)} JSON-Dateien, {len(ids)} Onboarding-Fragen")
    return 0


if __name__ == "__main__":
    sys.exit(main())
