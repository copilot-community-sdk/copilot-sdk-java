# Copilot SDK for Java - Documentation

> ⚠️ **Disclaimer:** This is an **unofficial, community-driven SDK** and is **not supported or endorsed by GitHub**. Use at your own risk.

This guide covers common use cases for the Copilot SDK for Java. For complete API details, see the [Javadoc](apidocs/index.html).

## Table of Contents

- [Basic Usage](#Basic_Usage)
- [Handling Responses](#Handling_Responses)
- [Event Types Reference](#Event_Types_Reference)
- [Streaming Responses](#Streaming_Responses)
- [Session Operations](#Session_Operations)
- [Choosing a Model](#Choosing_a_Model)
- [Session Management](#Session_Management)

---

## Basic Usage

Create a client, start a session, and send a message:

```java
import com.github.copilot.sdk.*;
import com.github.copilot.sdk.json.*;

try (var client = new CopilotClient()) {
    client.start().get();

    var session = client.createSession(
        new SessionConfig().setModel("gpt-4.1")
    ).get();

    var response = session.sendAndWait("Explain Java records in one sentence").get();
    System.out.println(response.getData().getContent());

    session.close();
}
```

The client manages the connection to the Copilot CLI. Sessions are independent conversations that can run concurrently.

---

## Handling Responses

### Simple Request-Response

For straightforward interactions, use `sendAndWait()`:

```java
var response = session.sendAndWait("What is the capital of France?").get();
System.out.println(response.getData().getContent());
```

### Event-Based Processing

For more control, subscribe to events and use `send()`:

> **Exception isolation:** If a handler throws an exception, the SDK logs the
> error and continues dispatching to remaining handlers. One misbehaving handler
> will never prevent others from executing. You can customize error handling with
> `session.setEventErrorHandler()` — see the
> [Advanced Usage](advanced.html#Custom_Event_Error_Handler) guide.

```java
var done = new CompletableFuture<Void>();

// Type-safe event handlers (recommended)
session.on(AssistantMessageEvent.class, msg -> {
    System.out.println("Response: " + msg.getData().getContent());
});

session.on(SessionErrorEvent.class, err -> {
    System.err.println("Error: " + err.getData().getMessage());
});

session.on(SessionIdleEvent.class, idle -> {
    done.complete(null);
});

session.send("Tell me a joke").get();
done.get(); // Wait for completion
```

You can also use a single handler for all events:

```java
session.on(event -> {
    switch (event) {
        case AssistantMessageEvent msg -> 
            System.out.println("Response: " + msg.getData().getContent());
        case SessionErrorEvent err -> 
            System.err.println("Error: " + err.getData().getMessage());
        case SessionIdleEvent idle -> 
            done.complete(null);
        default -> { }
    }
});
```

### Key Event Types

| Event | Description |
|-------|-------------|
| `AssistantMessageEvent` | Complete assistant response |
| `AssistantMessageDeltaEvent` | Streaming chunk (when streaming enabled) |
| `SessionIdleEvent` | Session finished processing |
| `SessionErrorEvent` | An error occurred |

For the complete list of all 33 event types, see [Event Types Reference](#Event_Types_Reference) below.

---

## Event Types Reference

The SDK supports 33 event types organized by category. All events extend `AbstractSessionEvent`.

### Session Events

| Event | Type String | Description |
|-------|-------------|-------------|
| `SessionStartEvent` | `session.start` | Session has started |
| `SessionResumeEvent` | `session.resume` | Session was resumed |
| `SessionIdleEvent` | `session.idle` | Session finished processing, ready for input |
| `SessionErrorEvent` | `session.error` | An error occurred in the session |
| `SessionInfoEvent` | `session.info` | Informational message from the session |
| `SessionShutdownEvent` | `session.shutdown` | Session is shutting down (includes reason and exit code) |
| `SessionModelChangeEvent` | `session.model_change` | The model was changed mid-session |
| `SessionHandoffEvent` | `session.handoff` | Session handed off to another agent |
| `SessionTruncationEvent` | `session.truncation` | Context was truncated due to limits |
| `SessionSnapshotRewindEvent` | `session.snapshot_rewind` | Session rewound to a previous snapshot |
| `SessionUsageInfoEvent` | `session.usage_info` | Token usage information |
| `SessionCompactionStartEvent` | `session.compaction_start` | Context compaction started (infinite sessions) |
| `SessionCompactionCompleteEvent` | `session.compaction_complete` | Context compaction completed |

### Assistant Events

| Event | Type String | Description |
|-------|-------------|-------------|
| `AssistantTurnStartEvent` | `assistant.turn_start` | Assistant began processing |
| `AssistantIntentEvent` | `assistant.intent` | Assistant's detected intent |
| `AssistantReasoningEvent` | `assistant.reasoning` | Full reasoning content (reasoning models) |
| `AssistantReasoningDeltaEvent` | `assistant.reasoning_delta` | Streaming reasoning chunk |
| `AssistantMessageEvent` | `assistant.message` | Complete assistant message |
| `AssistantMessageDeltaEvent` | `assistant.message_delta` | Streaming message chunk |
| `AssistantTurnEndEvent` | `assistant.turn_end` | Assistant finished processing |
| `AssistantUsageEvent` | `assistant.usage` | Token usage for this turn |

### Tool Events

| Event | Type String | Description |
|-------|-------------|-------------|
| `ToolUserRequestedEvent` | `tool.user_requested` | User requested a tool invocation |
| `ToolExecutionStartEvent` | `tool.execution_start` | Tool execution started |
| `ToolExecutionProgressEvent` | `tool.execution_progress` | Tool execution progress update |
| `ToolExecutionPartialResultEvent` | `tool.execution_partial_result` | Partial result from tool |
| `ToolExecutionCompleteEvent` | `tool.execution_complete` | Tool execution completed |

### User Events

| Event | Type String | Description |
|-------|-------------|-------------|
| `UserMessageEvent` | `user.message` | User sent a message |
| `PendingMessagesModifiedEvent` | `pending_messages.modified` | Pending message queue changed |

### Subagent Events

| Event | Type String | Description |
|-------|-------------|-------------|
| `SubagentStartedEvent` | `subagent.started` | Subagent was spawned |
| `SubagentSelectedEvent` | `subagent.selected` | Subagent was selected for task |
| `SubagentCompletedEvent` | `subagent.completed` | Subagent completed its task |
| `SubagentFailedEvent` | `subagent.failed` | Subagent failed |

### Other Events

| Event | Type String | Description |
|-------|-------------|-------------|
| `AbortEvent` | `abort` | Operation was aborted |
| `HookStartEvent` | `hook.start` | Hook execution started |
| `HookEndEvent` | `hook.end` | Hook execution completed |
| `SystemMessageEvent` | `system.message` | System-level message |
| `SkillInvokedEvent` | `skill.invoked` | A skill was invoked |

See the [events package Javadoc](apidocs/com/github/copilot/sdk/events/package-summary.html) for detailed event data structures.

---

## Streaming Responses

Enable streaming to receive response chunks as they're generated:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-4.1")
        .setStreaming(true)
).get();

var done = new CompletableFuture<Void>();

session.on(AssistantMessageDeltaEvent.class, delta -> {
    // Print each chunk as it arrives
    System.out.print(delta.getData().getDeltaContent());
});

session.on(SessionIdleEvent.class, idle -> {
    System.out.println(); // Newline at end
    done.complete(null);
});

session.send("Write a haiku about Java").get();
done.get();
```

---

## Session Operations

### Get Conversation History

Retrieve all messages and events from a session using `getMessages()`:

```java
var history = session.getMessages().get();

for (var event : history) {
    switch (event) {
        case UserMessageEvent user -> 
            System.out.println("User: " + user.getData().getContent());
        case AssistantMessageEvent assistant -> 
            System.out.println("Assistant: " + assistant.getData().getContent());
        case ToolExecutionCompleteEvent tool -> 
            System.out.println("Tool: " + tool.getData().getToolName());
        default -> { }
    }
}
```

This is useful for:
- Displaying conversation history in a UI
- Persisting conversations for later review
- Debugging and logging session state

### Abort Current Operation

Cancel a long-running operation using `abort()`:

```java
// Start a potentially long operation
var messageFuture = session.send("Analyze this large codebase...");

// User clicks cancel button
session.abort().get();

// The session will emit an AbortEvent
session.on(AbortEvent.class, evt -> {
    System.out.println("Operation was cancelled");
});
```

Use cases:
- User-initiated cancellation in interactive applications
- Timeout handling in automated pipelines
- Graceful shutdown when application is closing

### Custom Timeout

Use `sendAndWait` with a custom timeout for CI/CD pipelines:

```java
try {
    // 30-second timeout
    var response = session.sendAndWait(
        new MessageOptions().setPrompt("Quick question"),
        30000  // timeout in milliseconds
    ).get();
} catch (ExecutionException e) {
    if (e.getCause() instanceof TimeoutException) {
        System.err.println("Request timed out");
        session.abort().get();
    }
}
```

---

## Choosing a Model

### List Available Models

Query available models before creating a session:

```java
var models = client.listModels().get();

for (var model : models) {
    System.out.printf("%s (%s)%n", model.getId(), model.getName());
}
```

### Use a Specific Model

```java
var session = client.createSession(
    new SessionConfig().setModel("claude-sonnet-4")
).get();
```

---

## Session Management

### Multiple Concurrent Sessions

```java
var session1 = client.createSession(
    new SessionConfig().setModel("gpt-4.1")
).get();

var session2 = client.createSession(
    new SessionConfig().setModel("claude-sonnet-4")
).get();

// Send messages concurrently
var future1 = session1.sendAndWait("Summarize REST APIs");
var future2 = session2.sendAndWait("Summarize GraphQL");

System.out.println("GPT: " + future1.get().getData().getContent());
System.out.println("Claude: " + future2.get().getData().getContent());
```

### Resume a Previous Session

```java
// Get the last session ID
var lastSessionId = client.getLastSessionId().get();

// Or list all sessions
var sessions = client.listSessions().get();

// Resume a session
var session = client.resumeSession(lastSessionId).get();
```

### Clean Up Sessions

```java
// Delete a specific session
client.deleteSession(sessionId).get();
```

---

## Next Steps

- 📖 **[Advanced Usage](advanced.html)** - Tools, BYOK, MCP Servers, System Messages, Infinite Sessions, Skills
- 📖 **[Session Hooks](hooks.html)** - Intercept tool execution and session lifecycle events
- 📖 **[MCP Servers](mcp.html)** - Integrate external tools via Model Context Protocol
- 📖 **[API Javadoc](apidocs/index.html)** - Complete API reference