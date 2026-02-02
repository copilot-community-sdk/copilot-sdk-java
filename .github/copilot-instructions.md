# Copilot SDK for Java

This is a Java SDK for programmatic control of GitHub Copilot CLI, enabling AI-powered applications and agentic workflows.

## Build & Test Commands

```bash
# Build and run all tests
mvn clean verify

# Run a single test class
mvn test -Dtest=PermissionsTest

# Run a single test method
mvn test -Dtest=PermissionsTest#testDenyPermission

# Format code (required before commits)
mvn spotless:apply

# Check formatting without fixing
mvn spotless:check

# Build without tests
mvn clean package -DskipTests

# Run tests with debug logging
mvn test -Pdebug

# Run one test with debug logging
mvn test -Pdebug -Dtest=PermissionsTest#testDenyPermission
```

## Architecture

### Core Classes

- **`CopilotClient`** - Main entry point. Manages CLI process lifecycle and JSON-RPC communication. Implements `AutoCloseable`. Use `start()` to spawn CLI, then `createSession()` or `resumeSession()` to create conversation sessions.
- **`CopilotSession`** - Represents a single conversation. Register event handlers with `on()`, send messages with `send()` or `sendAndWait()`. Supports tools, permissions, and hooks callbacks.
- **`JsonRpcClient`** - Low-level JSON-RPC 2.0 protocol handler over stdin/stdout.

### Package Structure

- `com.github.copilot.sdk` - Core client and session classes
- `com.github.copilot.sdk.json` - DTOs for JSON-RPC messages (Jackson-annotated). Checkstyle exempted since they're self-documenting.
- `com.github.copilot.sdk.events` - Event types for session callbacks. Checkstyle exempted since they're self-documenting.

### Async Pattern

All operations return `CompletableFuture<T>`. Sessions emit events through registered listeners (`session.on(event -> ...)`) using Java pattern matching for event types.

### Test Infrastructure

Tests use the official [copilot-sdk](https://github.com/github/copilot-sdk) test harness, automatically cloned to `target/copilot-sdk` during build.

- **`E2ETestContext`** - Creates isolated test environments with a replaying HTTP proxy for deterministic API responses
- **`CapiProxy`** - Node.js-based proxy that replays recorded HTTP exchanges from YAML snapshots
- Snapshots stored in `target/copilot-sdk/test/snapshots/{category}/{test_name}.yaml`
- Test names converted to lowercase snake_case for snapshot filenames

E2E test pattern:
```java
@BeforeAll static void setup() { ctx = E2ETestContext.create(); }
@AfterAll static void teardown() { ctx.close(); }

@Test void testFoo() {
    ctx.configureForTest("category", "test_name");
    try (CopilotClient client = ctx.createClient()) {
        // ...
    }
}
```

## Conventions

### Code Style

- Uses Spotless with Eclipse formatter (4-space indentation)
- Pre-commit hook runs `mvn spotless:check` - enable with: `git config core.hooksPath .githooks`
- Public API classes require Javadoc (enforced by Checkstyle)
- The `json` and `events` packages are exempted from Javadoc requirements (self-documenting DTOs)

### Naming

- Test classes: `*Test.java` (e.g., `PermissionsTest.java`)
- DTOs use fluent setters returning `this` for chaining

### Dependencies

- **Jackson** for JSON serialization (ObjectMapper with JavaTimeModule)
- **JUnit 5** for testing
- Java 17+ (uses records, pattern matching, var)

## Upstream Synchronization

This SDK tracks the official [github/copilot-sdk](https://github.com/github/copilot-sdk) (primarily the .NET implementation). The `.lastmerge` file contains the last merged upstream commit hash.

Use the `/agentic-merge-upstream` skill (or `@workspace /agentic-merge-upstream`) to merge upstream changes. Key mapping:

| Upstream (.NET)                | Java SDK                                              |
|-------------------------------|-------------------------------------------------------|
| `dotnet/src/Client.cs`        | `src/main/java/.../CopilotClient.java`               |
| `dotnet/src/Session.cs`       | `src/main/java/.../CopilotSession.java`              |
| `dotnet/src/Types.cs`         | `src/main/java/.../json/*.java`                      |
| `dotnet/test/*Tests.cs`       | `src/test/java/.../*Test.java`                       |

When porting: adapt to Java idioms (CompletableFuture instead of Task, camelCase methods, etc.) rather than direct translation.
