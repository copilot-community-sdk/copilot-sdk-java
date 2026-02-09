#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────
# format-and-test.sh
#
# Convenience script that runs the full quality pipeline:
#   1. spotless:apply  (auto-format code)
#   2. mvn clean verify (compile + test + checkstyle + spotbugs)
#
# Usage:  ./.github/scripts/build/format-and-test.sh
#         ./.github/scripts/build/format-and-test.sh --format-only
#         ./.github/scripts/build/format-and-test.sh --test-only
#         ./.github/scripts/build/format-and-test.sh --debug   (uses -Pdebug)
# ──────────────────────────────────────────────────────────────
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT_DIR"

FORMAT=true
TEST=true
MVN_PROFILE=""

for arg in "$@"; do
    case "$arg" in
        --format-only) TEST=false ;;
        --test-only)   FORMAT=false ;;
        --debug)       MVN_PROFILE="-Pdebug" ;;
        *)             echo "Unknown option: $arg"; exit 1 ;;
    esac
done

if $FORMAT; then
    echo "▸ Running Spotless (format)…"
    mvn spotless:apply
fi

if $TEST; then
    echo "▸ Running mvn clean verify $MVN_PROFILE …"
    mvn clean verify $MVN_PROFILE
    echo ""
    echo "✅  All checks passed."
else
    echo "✅  Formatting complete."
fi
