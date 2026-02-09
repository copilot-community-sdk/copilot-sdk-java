#!/usr/bin/env bash
# Parse the GitHub owner and repository name from the git remote URL.
# Outputs two lines: owner on the first, repo on the second.
# Handles both HTTPS and SSH remote URL formats.
#
# Usage:
#   eval "$(./parse-repo-info.sh)"
#   echo "Owner: $REPO_OWNER  Repo: $REPO_NAME"

set -euo pipefail

remote_url=$(git remote get-url origin 2>/dev/null) || {
  echo "ERROR: No git remote named 'origin' found." >&2
  exit 1
}

# Strip trailing .git if present
remote_url="${remote_url%.git}"

if [[ "$remote_url" =~ github\.com[:/]([^/]+)/([^/]+)$ ]]; then
  owner="${BASH_REMATCH[1]}"
  repo="${BASH_REMATCH[2]}"
else
  echo "ERROR: Could not parse owner/repo from remote URL: $remote_url" >&2
  exit 1
fi

echo "REPO_OWNER=$owner"
echo "REPO_NAME=$repo"
