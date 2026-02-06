# Advanced Usage

> ⚠️ **Disclaimer:** This is an **unofficial, community-driven SDK** and is **not supported or endorsed by GitHub**. Use at your own risk.

This guide covers advanced scenarios for extending and customizing your Copilot integration.

---

## Custom Tools

Let the AI call back into your application to fetch data or perform actions.

```java
// Define strongly-typed arguments with a record
record IssueArgs(String id) {}

var lookupTool = ToolDefinition.create(
    "lookup_issue",
    "Fetch issue details from our tracker",
    Map.of(
        "type", "object",
        "properties", Map.of(
            "id", Map.of("type", "string", "description", "Issue identifier")
        ),
        "required", List.of("id")
    ),
    invocation -> {
        IssueArgs args = invocation.getArgumentsAs(IssueArgs.class);
        return CompletableFuture.completedFuture(fetchIssue(args.id()));
    }
);

var session = client.createSession(
    new SessionConfig()
        .setTools(List.of(lookupTool))
).get();
```

See [ToolDefinition](apidocs/com/github/copilot/sdk/ToolDefinition.html) Javadoc for schema details.

---

## System Messages

Customize the AI's behavior by adding rules or replacing the default prompt.

### Adding Rules

Use `APPEND` mode to add constraints while keeping default guardrails:

```java
var session = client.createSession(
    new SessionConfig()
        .setSystemMessage(new SystemMessageConfig()
            .setMode(SystemMessageMode.APPEND)
            .setContent("""
                <rules>
                - Always check for security vulnerabilities
                - Suggest performance improvements
                </rules>
            """))
).get();
```

### Full Control

Use `REPLACE` mode for complete control (removes default guardrails):

```java
var session = client.createSession(
    new SessionConfig()
        .setSystemMessage(new SystemMessageConfig()
            .setMode(SystemMessageMode.REPLACE)
            .setContent("You are a helpful coding assistant."))
).get();
```

---

## File Attachments

Include files as context for the AI to analyze.

```java
session.send(new MessageOptions()
    .setPrompt("Review this file for bugs")
    .setAttachments(List.of(
        new Attachment()
            .setType("file")
            .setPath("/path/to/file.java")
            .setDisplayName("MyService.java")
    ))
).get();
```

---

## Bring Your Own Key (BYOK)

Use your own OpenAI or Azure OpenAI API key instead of GitHub Copilot.

```java
var session = client.createSession(
    new SessionConfig()
        .setProvider(new ProviderConfig()
            .setType("openai")
            .setBaseUrl("https://api.openai.com/v1")
            .setApiKey("sk-..."))
).get();
```

---

## Infinite Sessions

Run long conversations without hitting context limits.

When enabled (default), the session automatically compacts older messages as the context window fills up.

```java
var session = client.createSession(
    new SessionConfig()
        .setInfiniteSessions(new InfiniteSessionConfig()
            .setEnabled(true)
            .setBackgroundCompactionThreshold(0.80)  // Start compacting at 80%
            .setBufferExhaustionThreshold(0.95))     // Block at 95%
).get();

// Access the workspace where session state is persisted
var workspace = session.getWorkspacePath();
```

### Compaction Events

When compaction occurs, the session emits events that you can listen for:

```java
session.on(SessionCompactionStartEvent.class, start -> {
    System.out.println("Compaction started");
});
session.on(SessionCompactionCompleteEvent.class, complete -> {
    var data = complete.getData();
    System.out.println("Compaction completed - success: " + data.isSuccess() 
        + ", tokens removed: " + data.getTokensRemoved());
});
```

For short conversations, disable to avoid overhead:

```java
new InfiniteSessionConfig().setEnabled(false)
```

---

## MCP Servers

Extend the AI with external tools via the Model Context Protocol.

```java
Map<String, Object> server = Map.of(
    "type", "local",
    "command", "npx",
    "args", List.of("-y", "@modelcontextprotocol/server-filesystem", "/tmp"),
    "tools", List.of("*")
);

var session = client.createSession(
    new SessionConfig()
        .setMcpServers(Map.of("filesystem", server))
).get();
```

📖 **[Full MCP documentation →](mcp.html)** for local/remote servers and all options.

---

## Skills Configuration

Load custom skills from directories to extend the AI's capabilities with domain-specific knowledge.

### Loading Skills

Skills are loaded from `SKILL.md` files in subdirectories of the specified skill directories:

```java
var session = client.createSession(
    new SessionConfig()
        .setSkillDirectories(List.of("/path/to/skills"))
).get();
```

Each skill subdirectory should contain a `SKILL.md` file with YAML frontmatter:

```markdown
---
name: my-skill
description: A skill that provides domain-specific knowledge
---

# Skill Instructions

Your skill instructions go here...
```

### Disabling Skills

Disable specific skills by name:

```java
var session = client.createSession(
    new SessionConfig()
        .setSkillDirectories(List.of("/path/to/skills"))
        .setDisabledSkills(List.of("my-skill"))
).get();
```

---

## Custom Configuration Directory

Use a custom configuration directory for session settings:

```java
var session = client.createSession(
    new SessionConfig()
        .setConfigDir("/path/to/custom/config")
).get();
```

This is useful when you need to isolate session configuration or use different settings for different environments.

---

## User Input Handling

Handle user input requests when the AI uses the `ask_user` tool to gather information from the user.

```java
var session = client.createSession(
    new SessionConfig()
        .setOnUserInputRequest((request, invocation) -> {
            System.out.println("Agent asks: " + request.getQuestion());
            
            // Check if choices are provided
            if (request.getChoices() != null && !request.getChoices().isEmpty()) {
                System.out.println("Options: " + request.getChoices());
                // Return one of the provided choices
                var selectedChoice = request.getChoices().get(0);
                return CompletableFuture.completedFuture(
                    new UserInputResponse()
                        .setAnswer(selectedChoice)
                        .setWasFreeform(false)
                );
            }
            
            // Freeform input
            var userAnswer = getUserInput(); // your input method
            return CompletableFuture.completedFuture(
                new UserInputResponse()
                    .setAnswer(userAnswer)
                    .setWasFreeform(true)
            );
        })
).get();
```

The `UserInputRequest` contains:
- `getQuestion()` - The question the AI is asking
- `getChoices()` - Optional list of choices for the user to select from

The `UserInputResponse` should include:
- `setAnswer(String)` - The user's answer
- `setWasFreeform(boolean)` - `true` if the answer was freeform text, `false` if it was from the provided choices

See [UserInputHandler](apidocs/com/github/copilot/sdk/json/UserInputHandler.html) Javadoc for more details.

---

## Permission Handling

Approve or deny permission requests from the AI.

```java
var session = client.createSession(
    new SessionConfig()
        .setOnPermissionRequest((request, invocation) -> {
            // Inspect request and approve/deny
            var result = new PermissionRequestResult();
            result.setKind("user-approved");
            return CompletableFuture.completedFuture(result);
        })
).get();
```

---

## Session Hooks

Intercept tool execution and session lifecycle events using hooks.

```java
var hooks = new SessionHooks()
    .setOnPreToolUse((input, invocation) -> {
        System.out.println("Tool: " + input.getToolName());
        return CompletableFuture.completedFuture(
            new PreToolUseHookOutput().setPermissionDecision("allow")
        );
    })
    .setOnPostToolUse((input, invocation) -> {
        System.out.println("Result: " + input.getToolResult());
        return CompletableFuture.completedFuture(null);
    });

var session = client.createSession(
    new SessionConfig().setHooks(hooks)
).get();
```

📖 **[Full Session Hooks documentation →](hooks.html)** for all 5 hook types, inputs/outputs, and examples.

---

## Manual Server Control

Control the CLI lifecycle yourself instead of auto-start.

```java
var client = new CopilotClient(
    new CopilotClientOptions().setAutoStart(false)
);

client.start().get();   // Start manually
// ... use client ...
client.stop().get();    // Stop manually
```

---

## Session Lifecycle Events

Subscribe to lifecycle events to be notified when sessions are created, deleted, updated, or change foreground/background state.

### Subscribing to All Lifecycle Events

```java
var subscription = client.onLifecycle(event -> {
    System.out.println("Session " + event.getSessionId() + ": " + event.getType());
    
    if (event.getMetadata() != null) {
        System.out.println("  Summary: " + event.getMetadata().getSummary());
    }
});

// Later, when done listening:
subscription.close();
```

### Subscribing to Specific Event Types

```java
import com.github.copilot.sdk.json.SessionLifecycleEventTypes;

// Listen only for session creation
var subscription = client.onLifecycle(
    SessionLifecycleEventTypes.CREATED,
    event -> System.out.println("New session: " + event.getSessionId())
);
```

Available event types:
- `SessionLifecycleEventTypes.CREATED` - Session was created
- `SessionLifecycleEventTypes.DELETED` - Session was deleted
- `SessionLifecycleEventTypes.UPDATED` - Session was updated
- `SessionLifecycleEventTypes.FOREGROUND` - Session moved to foreground (TUI+server mode)
- `SessionLifecycleEventTypes.BACKGROUND` - Session moved to background (TUI+server mode)

---

## Foreground Session Control (TUI+Server Mode)

When connecting to a server running in TUI+server mode (`--ui-server`), you can control which session is displayed in the TUI.

### Getting the Foreground Session

```java
var sessionId = client.getForegroundSessionId().get();
if (sessionId != null) {
    System.out.println("TUI is displaying session: " + sessionId);
}
```

### Setting the Foreground Session

```java
client.setForegroundSessionId("session-123").get();
```

---

## Error Handling

All SDK methods return `CompletableFuture`. Errors surface via `ExecutionException`:

```java
try {
    session.send(new MessageOptions().setPrompt("Hello")).get();
} catch (ExecutionException ex) {
    System.err.println("Error: " + ex.getCause().getMessage());
}
```

For reactive error handling, use `exceptionally()` or `handle()`:

```java
session.send(new MessageOptions().setPrompt("Hello"))
    .exceptionally(ex -> {
        System.err.println("Failed: " + ex.getMessage());
        return null;
    });
```

### Event Handler Exceptions

If an event handler throws an exception, the SDK catches it, logs it at
`SEVERE` level, and continues dispatching to remaining handlers. This means
one faulty handler will never block others from receiving events:

```java
// This handler throws, but the second handler still runs
session.on(AssistantMessageEvent.class, msg -> {
    throw new RuntimeException("bug in handler 1");
});

session.on(AssistantMessageEvent.class, msg -> {
    // This handler executes normally despite the exception above
    System.out.println(msg.getData().getContent());
});
```

> **Note:** This exception isolation behavior is consistent with the Node.js,
> Go, and Python Copilot SDKs, which all catch handler errors per-handler. The
> .NET SDK is an exception — handler errors propagate there and can prevent
> subsequent handlers from running.

### Custom Event Error Handler

By default, handler exceptions are logged at `SEVERE` level using
`java.util.logging`. You can replace this with a custom
`EventErrorHandler` to integrate with your own logging, metrics, or
error-reporting systems:

```java
session.setEventErrorHandler((event, exception) -> {
    metrics.increment("handler.errors");
    logger.error("Handler failed on {}: {}",
        event.getType(), exception.getMessage());
});
```

The error handler receives both the event that was being dispatched and the
exception that was thrown. If the error handler itself throws, that exception
is silently caught and logged to prevent cascading failures.

Pass `null` to restore the default logging behavior:

```java
session.setEventErrorHandler(null);
```
