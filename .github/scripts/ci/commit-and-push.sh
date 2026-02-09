#!/usr/bin/env bash
# Verify changes, optionally run the code formatter, create a branch,
# stage all changes, commit, and push to origin.
#
# Usage:
#   ./commit-and-push.sh <branch-name> <commit-message> [--skip-format]
#
# Arguments:
#   branch-name     The branch to create (e.g., fix/my-change)
#   commit-message  The commit message (may include newlines via $'...' syntax)
#   --skip-format   Skip running mvn spotless:apply
#
# Exit codes:
#   0  Success
#   1  No changes to commit / general error
#   2  Push failed

set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <branch-name> <commit-message> [--skip-format]" >&2
  exit 1
fi

branch_name="$1"
commit_message="$2"
skip_format="${3:-}"

# -- Step 1: Verify there are changes to commit ---------------------------------
if [[ -z "$(git status --porcelain)" ]]; then
  echo "ERROR: No uncommitted changes found. Nothing to commit." >&2
  exit 1
fi

echo "✓ Uncommitted changes detected."

# -- Step 4: Run code formatter (if applicable) ----------------------------------
if [[ "$skip_format" != "--skip-format" ]] && [[ -f pom.xml ]]; then
  if grep -q 'spotless-maven-plugin' pom.xml 2>/dev/null; then
    echo "Running Spotless formatter..."
    mvn -q spotless:apply
    echo "✓ Spotless formatting applied."
  fi
fi

# -- Step 5: Create branch, stage, and commit ------------------------------------
# If the branch already exists locally, append a numeric suffix
actual_branch="$branch_name"
suffix=2
while git show-ref --verify --quiet "refs/heads/$actual_branch" 2>/dev/null; do
  actual_branch="${branch_name}-${suffix}"
  suffix=$((suffix + 1))
done

git checkout -b "$actual_branch"
git add -A
git commit -m "$commit_message"

echo "✓ Committed on branch '$actual_branch'."

# -- Step 6: Push the branch -----------------------------------------------------
if ! git push -u origin "$actual_branch" 2>&1; then
  echo "ERROR: Push failed." >&2
  exit 2
fi

# Verify the push actually transferred the commit
remote_sha=$(git ls-remote origin "refs/heads/$actual_branch" | awk '{print $1}')
local_sha=$(git rev-parse HEAD)

if [[ "$remote_sha" != "$local_sha" ]]; then
  echo "WARNING: Remote SHA does not match local. Retrying push..." >&2
  git push -u origin "$actual_branch" 2>&1 || { echo "ERROR: Retry push failed." >&2; exit 2; }
fi

echo "✓ Pushed to origin/$actual_branch."
echo "BRANCH_NAME=$actual_branch"
