# Merge Upstream SDK Changes

You are an expert Java developer tasked with porting changes from the official Copilot SDK (primarily the .NET implementation) to this Java SDK.

## ⚠️ IMPORTANT: Java SDK Design Takes Priority

**The current design and architecture of the Java SDK is the priority.** When porting changes from upstream:

1. **Adapt, don't copy** - Translate upstream features to fit the Java SDK's existing patterns, naming conventions, and architecture
2. **Preserve Java idioms** - The Java SDK should feel natural to Java developers, not like a C# port
3. **Maintain consistency** - New code must match the existing codebase style and structure
4. **Evaluate before porting** - Not every upstream change needs to be ported; some may not be applicable or may conflict with Java SDK design decisions

Before making any changes, **read and understand the existing Java SDK implementation** to ensure new code integrates seamlessly.

## Workflow Overview

1. Create a new branch from main
2. Update Copilot CLI to latest version
3. Update README with minimum CLI version requirement
4. Clone upstream repository
5. Analyze diff since last merge
6. Apply changes to Java SDK (commit as you go)
7. Test and fix issues
8. Update documentation
9. Push branch to remote for Pull Request review

---

## Step 1: Create a New Branch

Before starting any work, create a new branch from `main` to isolate the merge changes:

```bash
# Ensure we're on main and up to date
git checkout main
git pull origin main

# Create a new branch with a descriptive name including the date
BRANCH_NAME="merge-upstream-$(date +%Y%m%d)"
git checkout -b "$BRANCH_NAME"
echo "Created branch: $BRANCH_NAME"
```

**Important:** All changes will be committed to this branch as you work. This allows for proper review via Pull Request.

## Step 2: Update Copilot CLI

Update the locally installed GitHub Copilot CLI to the latest version:

```bash
copilot update
```

After updating, capture the new version and update the README.md to reflect the minimum version requirement:

```bash
# Get the current version
CLI_VERSION=$(copilot --version | head -n 1 | awk '{print $NF}')
echo "Updated Copilot CLI to version: $CLI_VERSION"
```

Update the Requirements section in `README.md` to specify the minimum CLI version requirement. Locate the line mentioning "GitHub Copilot CLI installed" and update it to include the version information.

Commit this change before proceeding:

```bash
git add README.md
git commit -m "Update Copilot CLI minimum version requirement to $CLI_VERSION"
```

## Step 3: Clone Upstream Repository

Clone the official Copilot SDK repository into a temporary folder:

```bash
UPSTREAM_REPO="https://github.com/github/copilot-sdk.git"
TEMP_DIR=$(mktemp -d)
git clone --depth=100 "$UPSTREAM_REPO" "$TEMP_DIR/copilot-sdk"
```

## Step 4: Read Last Merge Commit

Read the commit hash from `.lastmerge` file in the Java SDK root:

```bash
LAST_MERGE_COMMIT=$(cat .lastmerge)
echo "Last merged commit: $LAST_MERGE_COMMIT"
```

## Step 5: Analyze Changes

Generate a diff between the last merged commit and HEAD of main:

```bash
cd "$TEMP_DIR/copilot-sdk"
git fetch origin main
git log --oneline "$LAST_MERGE_COMMIT"..origin/main
git diff "$LAST_MERGE_COMMIT"..origin/main --stat
```

Focus on analyzing:
- `dotnet/src/` - Primary reference implementation
- `dotnet/test/` - Test cases to port
- `docs/` - Documentation updates
- `sdk-protocol-version.json` - Protocol version changes

## Step 6: Identify Changes to Port

For each change in the upstream diff, determine:

1. **New Features**: New methods, classes, or capabilities added to the SDK
2. **Bug Fixes**: Corrections to existing functionality
3. **API Changes**: Changes to public interfaces or method signatures
4. **Protocol Updates**: Changes to the JSON-RPC protocol or message types
5. **Test Updates**: New or modified test cases

### Key Files to Compare

| Upstream (.NET)                    | Java SDK Equivalent                                    |
|------------------------------------|--------------------------------------------------------|
| `dotnet/src/Client.cs`             | `src/main/java/com/github/copilot/sdk/CopilotClient.java` |
| `dotnet/src/Session.cs`            | `src/main/java/com/github/copilot/sdk/CopilotSession.java` |
| `dotnet/src/Types.cs`              | `src/main/java/com/github/copilot/sdk/types/*.java`    |
| `dotnet/src/Generated/*.cs`        | `src/main/java/com/github/copilot/sdk/types/*.java`    |
| `dotnet/test/*.cs`                 | `src/test/java/com/github/copilot/sdk/*Test.java`      |
| `docs/getting-started.md`          | `README.md` and `src/site/markdown/*.md`               |
| `docs/*.md` (new files)            | `src/site/markdown/*.md` + update `src/site/site.xml`  |
| `sdk-protocol-version.json`        | (embedded in Java code or resource file)               |

> **⚠️ Important:** When adding new documentation pages, always update `src/site/site.xml` to include them in the navigation menu.

## Step 7: Apply Changes to Java SDK

When porting changes:

### ⚠️ Priority: Preserve Java SDK Design

Before modifying any code:

1. **Read the existing Java implementation first** - Understand current patterns, class structure, and naming
2. **Identify the Java equivalent approach** - Don't replicate C# patterns; find the idiomatic Java way
3. **Check for existing abstractions** - The Java SDK may already have mechanisms that differ from .NET
4. **Preserve backward compatibility** - Existing API signatures should not break unless absolutely necessary
5. **When in doubt, match existing code** - Follow what's already in the Java SDK, not the upstream

### Commit Changes Incrementally

**Important:** Commit your changes as you work, grouping related changes together:

```bash
# After porting a feature or fix, commit with a descriptive message
git add <changed-files>
git commit -m "Port <feature/fix name> from upstream"

# Example commits:
# git commit -m "Port new authentication flow from upstream"
# git commit -m "Add new message types from upstream protocol update"
# git commit -m "Port bug fix for session handling from upstream"
```

This creates a clear history of changes that can be reviewed in the Pull Request.

### General Guidelines

- **Naming Conventions**: Convert C# PascalCase to Java camelCase for methods/variables
- **Async Patterns**: C# `async/await` → Java `CompletableFuture` or synchronous equivalents
- **Nullable Types**: C# `?` nullable → Java `@Nullable` annotations or `Optional<T>`
- **Properties**: C# properties → Java getters/setters or records
- **Records**: C# records → Java records (Java 17+)
- **Events**: C# events → Java callbacks or listeners

### Type Mappings

| C# Type                | Java Equivalent              |
|------------------------|------------------------------|
| `string`               | `String`                     |
| `int`                  | `int` / `Integer`            |
| `bool`                 | `boolean` / `Boolean`        |
| `Task<T>`              | `CompletableFuture<T>`       |
| `CancellationToken`    | (custom implementation)      |
| `IAsyncEnumerable<T>`  | `Stream<T>` or `Iterator<T>` |
| `JsonElement`          | `JsonNode` (Jackson)         |
| `Dictionary<K,V>`      | `Map<K,V>`                   |
| `List<T>`              | `List<T>`                    |

### Code Style

Follow the existing Java SDK patterns:
- Use Jackson for JSON serialization (`ObjectMapper`)
- Use Java records for DTOs where appropriate
- Follow the existing package structure under `com.github.copilot.sdk`
- Maintain backward compatibility when possible
- **Match the style of surrounding code** - Consistency with existing code is more important than upstream patterns
- **Prefer existing abstractions** - If the Java SDK already solves a problem differently than .NET, keep the Java approach

## Step 7.5: Port Tests

After porting implementation changes, **always check for new or updated tests** in the upstream repository:

### Check for New Tests

```bash
cd "$TEMP_DIR/copilot-sdk"
git diff "$LAST_MERGE_COMMIT"..origin/main --stat -- dotnet/test/
git diff "$LAST_MERGE_COMMIT"..origin/main --stat -- test/snapshots/
```

### Port Test Cases

For each new or modified test file in `dotnet/test/`:

1. **Create corresponding Java test class** in `src/test/java/com/github/copilot/sdk/`
2. **Follow existing test patterns** - Look at existing tests like `PermissionsTest.java` for structure
3. **Use the E2ETestContext** infrastructure for tests that need the test harness
4. **Match snapshot directory names** - Test snapshots in `test/snapshots/` must match the directory name used in `ctx.configureForTest()`

### Test File Mapping

| Upstream Test (.NET)        | Java SDK Test                                          |
|-----------------------------|--------------------------------------------------------|
| `dotnet/test/AskUserTests.cs`  | `src/test/java/com/github/copilot/sdk/AskUserTest.java`  |
| `dotnet/test/HooksTests.cs`    | `src/test/java/com/github/copilot/sdk/HooksTest.java`    |
| `dotnet/test/ClientTests.cs`   | `src/test/java/com/github/copilot/sdk/CopilotClientTest.java` |
| `dotnet/test/*Tests.cs`        | `src/test/java/com/github/copilot/sdk/*Test.java`        |

### Test Snapshot Compatibility

New test snapshots are stored in `test/snapshots/` in the upstream repository. These snapshots are automatically cloned during the Maven build process.

If tests fail with errors like `TypeError: Cannot read properties of undefined`, the test harness may not yet support the new RPC methods. In this case:

1. **Mark tests as `@Disabled`** with a clear reason (e.g., `@Disabled("Requires test harness update with X support - see upstream PR #NNN")`)
2. **Document the dependency** in the test class Javadoc
3. **Enable tests later** once the harness is updated

### Unit Tests vs E2E Tests

- **Unit tests** (like auth option validation) can run without the test harness
- **E2E tests** require the test harness with matching snapshots

Commit tests separately or together with their corresponding implementation changes.

## Step 8: Format and Run Tests

After applying changes, format the code and run the test suite:

```bash
mvn spotless:apply
mvn clean test
```

**Important:** Always run `mvn spotless:apply` before testing to ensure code formatting is consistent with project standards.

### If Tests Fail

1. Read the test output carefully
2. Identify the root cause (compilation error, runtime error, assertion failure)
3. Fix the issue in the Java code
4. Re-run tests
5. Repeat until all tests pass

### Common Issues

- **Missing imports**: Add required import statements
- **Type mismatches**: Ensure proper type conversions
- **Null handling**: Add null checks where C# had nullable types
- **JSON serialization**: Verify Jackson annotations are correct

## Step 9: Build the Package

Once tests pass, build the complete package:

```bash
mvn clean package -DskipTests
```

Verify:
- No compilation errors
- No warnings (if possible)
- JAR file is generated in `target/`

## Step 10: Update Documentation

Review and update documentation as needed:

1. **README.md**: Update if there are new features or API changes
2. **src/site/markdown/documentation.md**: Update detailed documentation
3. **Javadoc**: Add/update Javadoc comments for new/changed public APIs
4. **CHANGELOG**: (if exists) Add entry for the changes

## Step 11: Update Last Merge Reference

Update the `.lastmerge` file with the new HEAD commit and commit this change:

```bash
cd "$TEMP_DIR/copilot-sdk"
NEW_COMMIT=$(git rev-parse origin/main)
cd -  # Return to Java SDK directory
echo "$NEW_COMMIT" > .lastmerge

# Commit the .lastmerge update
git add .lastmerge
git commit -m "Update .lastmerge to $NEW_COMMIT"
```

## Step 12: Push Branch and Create Pull Request

Push the branch to remote so the changes can be reviewed via Pull Request:

```bash
# Push the branch to remote
git push -u origin "$BRANCH_NAME"

echo "Branch '$BRANCH_NAME' pushed to remote."
echo "Create a Pull Request to review and merge the changes."
```

**Important:** After pushing, provide the user with:
1. The branch name that was pushed
2. A summary of the changes that were ported
3. Instructions to create a Pull Request comparing the branch against `main`

## Step 13: Final Review

Before finishing:

1. Run `git log --oneline main..$BRANCH_NAME` to review all commits
2. Run `git diff main..$BRANCH_NAME --stat` to see a summary of all changes
3. Ensure no unintended changes were made
4. Verify code follows project conventions
5. Confirm the branch was pushed to remote

---

## Checklist

- [ ] New branch created from `main`
- [ ] Copilot CLI updated to latest version
- [ ] README.md updated with minimum CLI version requirement
- [ ] Upstream repository cloned
- [ ] Diff analyzed between `.lastmerge` commit and HEAD
- [ ] New features/fixes identified
- [ ] Changes ported to Java SDK following conventions
- [ ] **New/updated tests ported from upstream** (check `dotnet/test/` and `test/snapshots/`)
- [ ] Tests marked `@Disabled` if harness doesn't support new features yet
- [ ] Changes committed incrementally with descriptive messages
- [ ] `mvn test` passes
- [ ] `mvn package` builds successfully
- [ ] Documentation updated
- [ ] `src/site/site.xml` updated if new documentation pages were added
- [ ] `.lastmerge` file updated with new commit hash
- [ ] Branch pushed to remote
- [ ] User informed about Pull Request creation

---

## Notes

- The upstream SDK is at: `https://github.com/github/copilot-sdk.git`
- Primary reference implementation is in `dotnet/` folder
- This Java SDK targets Java 17+
- Uses Jackson for JSON processing
- Uses JUnit 5 for testing
- **Java SDK design decisions take precedence over upstream patterns**
- **Adapt upstream changes to fit Java idioms, not the other way around**

