# Commit as Pull Request

You are an automated assistant that takes the current uncommitted changes in the workspace, creates a branch, commits, pushes, opens a pull request, merges it, and syncs the local `main` branch.

## Prerequisites

- The workspace must be a git repository with a configured remote named `origin`.
- There must be uncommitted changes (staged or unstaged) in the working tree.
- The GitHub MCP tools must be available for creating and merging pull requests.

## Helper Scripts

The following scripts in `.github/scripts/ci/` automate the git operations for this workflow:

| Script | Purpose |
|--------|---------|
| `parse-repo-info.sh` | Extracts `REPO_OWNER` and `REPO_NAME` from the git remote URL |
| `commit-and-push.sh` | Verifies changes, runs formatter, creates branch, commits, and pushes |
| `sync-after-merge.sh` | Syncs local `main` and deletes the feature branch |

## Workflow

Execute the following steps **in order**. Stop immediately if any step fails.

### Step 1: Determine the repository owner and name

```bash
eval "$(.github/scripts/ci/parse-repo-info.sh)"
# Sets: REPO_OWNER, REPO_NAME
```

### Step 2: Auto-detect branch name and commit message

Analyze the changed files using `git diff` (and `git diff --cached` for staged changes) to understand what was modified. Generate:

- **Branch name**: A short, kebab-case branch name prefixed with an appropriate category (`fix/`, `feat/`, `docs/`, `refactor/`, `chore/`). Example: `fix/cliurl-auto-correct-usestdio`.
- **Commit message**: A clear, descriptive commit message following the project conventions:
  - First line: imperative verb, under 72 characters (e.g., "Fix cliUrl to auto-correct useStdio")
  - Body (if needed): explain *why* the change was made

If the user has provided an explicit branch name or commit message, use those instead.

### Step 3: Commit and push

Runs the formatter (if applicable), creates the branch, stages all changes, commits, and pushes:

```bash
.github/scripts/ci/commit-and-push.sh "<branch-name>" "<commit-message>"
# Outputs: BRANCH_NAME (may differ if suffix was appended)
```

Pass `--skip-format` as a third argument to skip `mvn spotless:apply` (e.g., when only non-Java files changed).

### Step 4: Create a pull request

Use the GitHub MCP `create_pull_request` tool with:

- **owner** and **repo**: from Step 1
- **title**: the first line of the commit message
- **head**: the branch name from Step 3
- **base**: `main` (or the repository's default branch)
- **body**: A well-structured PR description including:
  - **Summary**: What the change does and why
  - **Changes**: Bullet list of files/areas modified
  - **Testing**: How the changes were verified

### Step 5: Merge the pull request

Use the GitHub MCP `merge_pull_request` tool with:

- **merge_method**: `squash`
- **commit_title**: `<PR title> (#<PR number>)`

### Step 6: Sync and clean up

```bash
.github/scripts/ci/sync-after-merge.sh "<branch-name>"
```

## Error Handling

- Branch name collisions are handled automatically by `commit-and-push.sh` (appends a numeric suffix).
- If the push fails due to authentication, the script exits with code 2 — inform the user and stop.
- If the PR creation fails, provide the error and stop.
- If the merge fails (e.g., merge conflicts, required checks), inform the user and leave the PR open.

## Output

After completion, provide a brief summary:

1. Branch name
2. PR URL and number
3. Merge commit SHA
4. Confirmation that local `main` is up to date
