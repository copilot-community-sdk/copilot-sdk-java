#!/bin/bash
set -e

# Script to update CHANGELOG.md during release process
# Usage: ./update-changelog.sh <version>
# Example: ./update-changelog.sh 1.0.8

if [ -z "$1" ]; then
    echo "Error: Version argument required"
    echo "Usage: $0 <version>"
    exit 1
fi

VERSION="$1"
CHANGELOG_FILE="CHANGELOG.md"
RELEASE_DATE=$(date +%Y-%m-%d)

echo "Updating CHANGELOG.md for version ${VERSION} (${RELEASE_DATE})"

# Check if CHANGELOG.md exists
if [ ! -f "$CHANGELOG_FILE" ]; then
    echo "Error: CHANGELOG.md not found"
    exit 1
fi

# Check if there's an [Unreleased] section
if ! grep -q "## \[Unreleased\]" "$CHANGELOG_FILE"; then
    echo "Error: No [Unreleased] section found in CHANGELOG.md"
    exit 1
fi

# Create a temporary file
TEMP_FILE=$(mktemp)

# Process the CHANGELOG
awk -v version="$VERSION" -v date="$RELEASE_DATE" '
BEGIN {
    unreleased_found = 0
    content_found = 0
    links_section = 0
    first_version_link = ""
}

# Track if we are in the links section at the bottom
/^\[/ {
    links_section = 1
}

# Replace [Unreleased] with the version and date
/^## \[Unreleased\]/ {
    if (!unreleased_found) {
        print "## [Unreleased]"
        print ""
        print "## [" version "] - " date
        unreleased_found = 1
        next
    }
}

# Capture the first version link to get the previous version
links_section && first_version_link == "" && /^\[[0-9]+\.[0-9]+\.[0-9]+\]:/ {
    match($0, /\[([0-9]+\.[0-9]+\.[0-9]+)\]:/, arr)
    if (arr[1] != "") {
        first_version_link = arr[1]
        # Insert Unreleased and new version links before first version link
        print "[Unreleased]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v" version "...HEAD"
        print "[" version "]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v" arr[1] "...v" version
    }
}

# Update existing [Unreleased] link if present
links_section && /^\[Unreleased\]:/ {
    # Get the previous version from the existing link
    match($0, /v([0-9]+\.[0-9]+\.[0-9]+)\.\.\.HEAD/, arr)
    if (arr[1] != "") {
        print "[Unreleased]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v" version "...HEAD"
        print "[" version "]: https://github.com/copilot-community-sdk/copilot-sdk-java/compare/v" arr[1] "...v" version
        next
    }
}

# Print all other lines unchanged
{ print }
' "$CHANGELOG_FILE" > "$TEMP_FILE"

# Replace the original file
mv "$TEMP_FILE" "$CHANGELOG_FILE"

echo "✓ CHANGELOG.md updated successfully"
echo "  - Added version ${VERSION} with date ${RELEASE_DATE}"
echo "  - Created new [Unreleased] section"
echo "  - Updated version comparison links"
