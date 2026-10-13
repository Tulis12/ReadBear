#!/usr/bin/env python3

import os
import re
import subprocess
import sys
from pathlib import Path

APK = Path("app/build/outputs/apk/release/app-release.apk")

PERMISSIONS = {}

def find_aapt() -> Path | None:
    sdk = (
            os.environ.get("ANDROID_HOME")
            or os.environ.get("ANDROID_SDK_ROOT")
            or str(Path.home() / "Android" / "Sdk")
    )

    build_tools = Path(sdk) / "build-tools"
    if not build_tools.is_dir():
        return None

    executables = ("aapt.exe", "aapt") if os.name == "nt" else ("aapt",)

    versions = sorted(
        (p for p in build_tools.iterdir() if p.is_dir()),
        key=lambda p: tuple(
            int(x) if x.isdigit() else x
            for x in re.findall(r"\d+|[^\d]+", p.name)
        ),
    )

    for version in reversed(versions):
        for executable in executables:
            candidate = version / executable
            if candidate.is_file():
                return candidate

    return None


def main() -> int:
    apk = Path(sys.argv[1]) if len(sys.argv) > 1 else APK

    if not apk.is_file():
        print(f"ERROR: APK not found: {apk}", file=sys.stderr)
        return 1

    aapt = find_aapt()
    if aapt is None:
        print(
            "ERROR: aapt not found. Install Android SDK Build-Tools "
            "or set ANDROID_HOME / ANDROID_SDK_ROOT.",
            file=sys.stderr,
        )
        return 1

    result = subprocess.run(
        [str(aapt), "dump", "permissions", str(apk)],
        capture_output=True,
        text=True,
        check=False,
    )

    if result.returncode != 0:
        print("ERROR: aapt failed:", file=sys.stderr)
        print(result.stderr, file=sys.stderr)
        return 1

    actual = set(
        re.findall(
            r"uses-permission[^:]*:\s*name='([^']+)'",
            result.stdout,
        )
    )

    expected = {
        permission
        for permission, allowed in PERMISSIONS.items()
        if allowed
    }

    extra = sorted(actual - expected)
    missing = sorted(expected - actual)

    if extra:
        print("ERROR: APK contains unapproved permissions:")
        for permission in extra:
            print(f"  + {permission}")

    if missing:
        print("WARNING: Allowlisted permissions not present in APK:")
        for permission in missing:
            print(f"  - {permission}")

    if extra:
        return 1

    print("OK: All APK permissions are allowlisted.")
    return 0


if __name__ == "__main__":
    sys.exit(main())