#!/usr/bin/env bash
# After a PR has been merged, sync the local main branch and
# optionally delete the local feature branch.
#
# Usage:
#   ./sync-after-merge.sh [branch-name]
#
# Arguments:
#   branch-name   The local branch to delete (optional). Skipped if omitted.

set -euo pipefail

branch_name="${1:-}"

# -- Step 9: Sync local main ----------------------------------------------------
git checkout main
git pull

echo "✓ Local main is up to date."

# -- Step 10: Clean up the local branch ------------------------------------------
if [[ -n "$branch_name" ]]; then
  if git show-ref --verify --quiet "refs/heads/$branch_name" 2>/dev/null; then
    git branch -d "$branch_name" 2>/dev/null || git branch -D "$branch_name"
    echo "✓ Deleted local branch '$branch_name'."
  else
    echo "Branch '$branch_name' does not exist locally (already cleaned up)."
  fi
fi
