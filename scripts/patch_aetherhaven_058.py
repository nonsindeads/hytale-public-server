#!/usr/bin/env python3
"""Apply the narrowly scoped Hytale 0.5.8 Bard animation compatibility fix."""

from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import shutil
import sys
import tempfile
import zipfile


EXPECTED_SOURCE_SHA256 = "f0b5a55862767f7057b3817de18112ab2e8339717754c8d1b74d54d87f48a937"
ENTRY = "Server/Models/Villager/Bard.json"
OLD_ANIMATION = "Characters/Animations/PlayLute.blockyanim"
NEW_ANIMATION = "Characters/Animations/Expressions/Talk/Talk3.blockyanim"


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            value.update(block)
    return value.hexdigest()


def main() -> int:
    if len(sys.argv) != 2:
        print(f"usage: {Path(sys.argv[0]).name} /path/to/Aetherhaven-2.7.2.jar", file=sys.stderr)
        return 2

    target = Path(sys.argv[1]).resolve()
    source_hash = digest(target)
    if source_hash != EXPECTED_SOURCE_SHA256:
        raise SystemExit(f"refusing unknown source JAR: {source_hash}")

    backup = target.with_suffix(target.suffix + ".pre-hytale-0.5.8-bard-fix")
    if not backup.exists():
        shutil.copy2(target, backup)

    fd, temporary_name = tempfile.mkstemp(prefix=target.name + ".", dir=target.parent)
    os.close(fd)
    temporary = Path(temporary_name)
    try:
        with zipfile.ZipFile(target, "r") as source, zipfile.ZipFile(temporary, "w") as output:
            replaced = False
            for info in source.infolist():
                payload = source.read(info.filename)
                if info.filename == ENTRY:
                    document = json.loads(payload)
                    animation = document["AnimationSets"]["PlayLute"]["Animations"][0]
                    if animation.get("Animation") != OLD_ANIMATION:
                        raise SystemExit("Bard animation does not match the expected source")
                    animation["Animation"] = NEW_ANIMATION
                    payload = (json.dumps(document, indent=2) + "\n").encode("utf-8")
                    replaced = True
                output.writestr(info, payload)
            if not replaced:
                raise SystemExit(f"missing JAR entry: {ENTRY}")
        os.chmod(temporary, target.stat().st_mode)
        os.replace(temporary, target)
    finally:
        temporary.unlink(missing_ok=True)

    print(f"patched {target}")
    print(f"backup  {backup}")
    print(f"sha256  {digest(target)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
