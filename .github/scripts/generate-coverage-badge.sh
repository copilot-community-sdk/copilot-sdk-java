#!/usr/bin/env bash
# Generates an SVG coverage badge from a JaCoCo CSV report.
#
# Usage: generate-coverage-badge.sh [jacoco.csv] [output-dir]
#   jacoco.csv  - Path to JaCoCo CSV report (default: target/site/jacoco-coverage/jacoco.csv)
#   output-dir  - Directory for the badge SVG (default: .github/badges)
set -euo pipefail

CSV="${1:-target/site/jacoco-coverage/jacoco.csv}"
BADGES_DIR="${2:-.github/badges}"

if [ ! -f "$CSV" ]; then
  echo "⚠️ No JaCoCo CSV report found at $CSV"
  exit 0
fi

# Sum INSTRUCTION_MISSED and INSTRUCTION_COVERED across all rows (skip header)
read -r missed covered <<< "$(awk -F',' 'NR>1 { m+=$4; c+=$5 } END { print m, c }' "$CSV")"
total=$((missed + covered))
if [ "$total" -eq 0 ]; then
  pct="0"
else
  pct=$(awk "BEGIN { printf \"%.1f\", ($covered / $total) * 100 }")
  # Drop trailing .0
  pct=$(echo "$pct" | sed 's/\.0$//')
fi
echo "Coverage: ${pct}%"

# Choose badge color based on coverage
color="#e05d44"  # red <60
if   awk "BEGIN{exit!($pct>=100)}"; then color="#4c1"     # bright green
elif awk "BEGIN{exit!($pct>=90)}";  then color="#97ca00"  # green
elif awk "BEGIN{exit!($pct>=80)}";  then color="#a4a61d"  # yellow-green
elif awk "BEGIN{exit!($pct>=70)}";  then color="#dfb317"  # yellow
elif awk "BEGIN{exit!($pct>=60)}";  then color="#fe7d37"  # orange
fi

# Generate SVG badge
mkdir -p "$BADGES_DIR"
label="coverage"
value="${pct}%"
lw=62; vw=46; tw=$((lw + vw))
cat > "${BADGES_DIR}/jacoco.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="${tw}" height="20">
  <linearGradient id="b" x2="0" y2="100%">
    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
    <stop offset="1" stop-opacity=".1"/>
  </linearGradient>
  <mask id="a"><rect width="${tw}" height="20" rx="3" fill="#fff"/></mask>
  <g mask="url(#a)">
    <rect width="${lw}" height="20" fill="#555"/>
    <rect x="${lw}" width="${vw}" height="20" fill="${color}"/>
    <rect width="${tw}" height="20" fill="url(#b)"/>
  </g>
  <g fill="#fff" text-anchor="middle" font-family="DejaVu Sans,Verdana,Geneva,sans-serif" font-size="11">
    <text x="$((lw/2))" y="15" fill="#010101" fill-opacity=".3">${label}</text>
    <text x="$((lw/2))" y="14">${label}</text>
    <text x="$((lw + vw/2))" y="15" fill="#010101" fill-opacity=".3">${value}</text>
    <text x="$((lw + vw/2))" y="14">${value}</text>
  </g>
</svg>
EOF

echo "Badge generated at ${BADGES_DIR}/jacoco.svg"
