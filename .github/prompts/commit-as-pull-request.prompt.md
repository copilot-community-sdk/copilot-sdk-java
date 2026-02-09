# Commit as Pull Request

You are an automated assistant that takes the current uncommitted changes in the workspace, creates a branch, commits, pushes, opens a pull request, merges it, and syncs the local `main` branch.

## Prerequisites

- The workspace must be a git repository with a configured remote named `origin`.
- There must be uncommitted changes (staged or unstaged) in the working tree.
- The GitHub MCP tools must be available for creating and merging pull requests.

## Workflow

Execute the following steps **in order**. Stop immediately if any step fails.

### Step 1: Verify there are changes to commit

Run `git status --porcelain` to confirm there are uncommitted changes. If the output is empty, inform the user there is nothing to commit and stop.

### Step 2: Determine the repository owner and name

Read the repository remote URL to extract the GitHub `owner` and `repo`:

```bash
git remote get-url origin
```

Parse the owner and repo from the URL (handles both HTTPS and SSH formats).

### Step 3: Auto-detect branch name and commit message

Analyze the changed files using `git diff` (and `git diff --cached` for staged changes) to understand what was modified. Generate:

- **Branch name**: A short, kebab-case branch name prefixed with an appropriate category (`fix/`, `feat/`, `docs/`, `refactor/`, `chore/`). Example: `fix/cliurl-auto-correct-usestdio`.
- **Commit message**: A clear, descriptive commit message following the project conventions:
  - First line: imperative verb, under 72 characters (e.g., "Fix cliUrl to auto-correct useStdio")
  - Body (if needed): explain *why* the change was made

If the user has provided an explicit branch name or commit message, use those instead.

### Step 4: Run code formatter

If the project has a formatter configured, run it before committing:

```bash
mvn spotless:apply
```

Only run this if a `pom.xml` exists with Spotless configured. Skip for non-Maven projects.

### Step 5: Create branch, stage, and commit

```bash
git checkout -b <branch-name>
git add -A
git commit -m "<commit-message>"
```

### Step 6: Push the branch

```bash
git push -u origin <branch-name>
```

If the push reports "Everything up-to-date", verify with `git log --oneline -1` that the commit exists, then retry with `git push -u origin <branch-name> 2>&1`.

### Step 7: Create a pull request

Use the GitHub MCP `create_pull_request` tool with:

- **owner** and **repo**: from Step 2
- **title**: the first line of the commit message
- **head**: the branch name
- **base**: `main` (or the repository's default branch)
- **body**: A well-structured PR description including:
  - **Summary**: What the change does and why
  - **Changes**: Bullet list of files/areas modified
  - **Testing**: How the changes were verified

### Step 8: Merge the pull request

Use the GitHub MCP `merge_pull_request` tool with:

- **merge_method**: `squash`
- **commit_title**: `<PR title> (#<PR number>)`

### Step 9: Sync local main

```bash
git checkout main
git pull
```

### Step 10: Clean up the local branch (optional)

```bash
git branch -d <branch-name>
```

## Error Handling

- If the branch name already exists, append a numeric suffix (e.g., `fix/my-change-2`).
- If the push fails due to authentication, inform the user and stop.
- If the PR creation fails, provide the error and stop.
- If the merge fails (e.g., merge conflicts, required checks), inform the user and leave the PR open.

## Output

After completion, provide a brief summary:

1. Branch name
2. PR URL and number
3. Merge commit SHA
4. Confirmation that local `main` is up to date
