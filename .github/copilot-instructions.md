# Copilot Instructions for copilot-sdk-java

A Java SDK for programmatic control of GitHub Copilot CLI. This is a community-driven port of the official .NET SDK, targeting Java 17+.

## Build & Test Commands

```bash
# Build and run all tests
mvn clean verify

# Run a single test class
mvn test -Dtest=CopilotClientTest

# Run a single test method
mvn test -Dtest=ToolsTest#testToolInvocation

# Format code (required before commit)
mvn spotless:apply

# Check formatting only
mvn spotless:check

# Build without tests
mvn clean package -DskipTests

# Run tests with debug logging
mvn test -Pdebug
```

## Architecture

### Core Components

- **CopilotClient** - Main entry point. Manages connection to Copilot CLI server via JSON-RPC over stdio. Spawns CLI process or connects to existing server.
- **CopilotSession** - Represents a conversation session. Handles event subscriptions, tool registration, permissions, and message sending.
- **JsonRpcClient** - Low-level JSON-RPC protocol implementation using Jackson for serialization.

### Package Structure

- `com.github.copilot.sdk` - Core classes (CopilotClient, CopilotSession, JsonRpcClient)
- `com.github.copilot.sdk.json` - DTOs, request/response types, handler interfaces (SessionConfig, MessageOptions, ToolDefinition, etc.)
- `com.github.copilot.sdk.events` - Event types for session streaming (AssistantMessageEvent, SessionIdleEvent, ToolExecutionStartEvent, etc.)

### Test Infrastructure

Tests use the official copilot-sdk test harness from `https://github.com/github/copilot-sdk`. The harness is automatically cloned during `generate-test-resources` phase to `target/copilot-sdk/`.

- **E2ETestContext** - Manages test environment with CapiProxy for deterministic API responses
- **CapiProxy** - Node.js-based replaying proxy using YAML snapshots from `test/snapshots/`
- Test snapshots are stored in the upstream repo's `test/snapshots/` directory

## Key Conventions

### Upstream Merging

This SDK tracks the official .NET implementation at `github/copilot-sdk`. The `.lastmerge` file contains the last merged upstream commit hash. Use the `agentic-merge-upstream` skill (see `.github/prompts/agentic-merge-upstream.prompt.md`) to port changes.

When porting from .NET:
- Adapt to Java idioms, don't copy C# patterns directly
- Convert `async/await` → `CompletableFuture`
- Convert C# properties → Java getters/setters or fluent setters
- Use Jackson for JSON (`ObjectMapper`, `@JsonProperty`)

### Code Style

- 4-space indentation (enforced by Spotless with Eclipse formatter)
- Fluent setter pattern for configuration classes (e.g., `new SessionConfig().setModel("gpt-5").setTools(tools)`)
- Public APIs require Javadoc (enforced by Checkstyle, except `json` and `events` packages)
- Pre-commit hook runs `mvn spotless:check` - enable with: `git config core.hooksPath .githooks`

### Handler Pattern

Handlers use functional interfaces with `CompletableFuture` returns:

```java
session.createSession(new SessionConfig()
    .setOnPermissionRequest((request, invocation) -> 
        CompletableFuture.completedFuture(new PermissionRequestResult().setKind("allow")))
    .setOnUserInput((request, invocation) -> 
        CompletableFuture.completedFuture(new UserInputResponse().setResponse("user input")))
);
```

### Event Handling

Sessions emit typed events via `session.on()`:

```java
session.on(AssistantMessageEvent.class, msg -> System.out.println(msg.getData().getContent()));
session.on(SessionIdleEvent.class, idle -> done.complete(null));
```

### Sealed Event Hierarchy

`AbstractSessionEvent` is a sealed class permitting specific event types. Use pattern matching:

```java
switch (event) {
    case AssistantMessageEvent msg -> handleMessage(msg);
    case ToolExecutionStartEvent tool -> handleToolStart(tool);
    case SessionIdleEvent idle -> handleIdle();
    default -> { }
}
```

### Tool Definition Pattern

Custom tools use `ToolDefinition.create()` with JSON Schema parameters and a `ToolHandler`:

```java
var tool = ToolDefinition.create(
    "get_weather",
    "Get weather for a location",
    Map.of(
        "type", "object",
        "properties", Map.of("location", Map.of("type", "string")),
        "required", List.of("location")
    ),
    invocation -> {
        // Type-safe: invocation.getArgumentsAs(WeatherArgs.class)
        // Or Map-based: invocation.getArguments().get("location")
        return CompletableFuture.completedFuture(result);
    }
);
```

## Testing Conventions

### E2E Test Structure

Tests extend the shared context pattern:

```java
private static E2ETestContext ctx;

@BeforeAll
static void setup() throws Exception {
    ctx = E2ETestContext.create();
}

@AfterAll
static void teardown() throws Exception {
    if (ctx != null) ctx.close();
}

@Test
void testFeature() throws Exception {
    ctx.configureForTest("category", "test_name");  // Loads test/snapshots/category/test_name.yaml
    try (CopilotClient client = ctx.createClient()) {
        // Test logic
    }
}
```

### Snapshot Naming

Test method names are converted to lowercase snake_case for snapshot filenames to avoid case collisions on macOS/Windows.

## JSON Serialization

- Uses Jackson with `@JsonProperty` annotations
- `@JsonInclude(JsonInclude.Include.NON_NULL)` on DTOs to omit null fields
- `ObjectMapper` configured via `JsonRpcClient.getObjectMapper()` with:
  - `JavaTimeModule` for date/time handling
  - `FAIL_ON_UNKNOWN_PROPERTIES = false` for forward compatibility

## Documentation

- Site docs in `src/site/markdown/` (filtered for `${project.version}` substitution)
- Update `src/site/site.xml` when adding new documentation pages
- Javadoc required for public APIs except `json` and `events` packages (self-documenting DTOs)
