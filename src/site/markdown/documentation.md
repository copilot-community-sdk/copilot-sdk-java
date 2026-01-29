# Copilot SDK for Java - Documentation

> ⚠️ **Disclaimer:** This is an **unofficial, community-driven SDK** and is **not supported or endorsed by GitHub**. Use at your own risk.

This guide covers common use cases for the Copilot SDK for Java. For complete API details, see the [Javadoc](apidocs/index.html).

## Table of Contents

- [Basic Usage](#Basic_Usage)
- [Handling Responses](#Handling_Responses)
- [Streaming Responses](#Streaming_Responses)
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
| `ToolExecutionStartEvent` | Tool invocation started |
| `ToolExecutionCompleteEvent` | Tool invocation completed |
| `SessionCompactionStartEvent` | Context compaction started (infinite sessions) |
| `SessionCompactionCompleteEvent` | Context compaction completed |
| `SessionUsageInfoEvent` | Token usage information |

See the [events package Javadoc](apidocs/com/github/copilot/sdk/events/package-summary.html) for all event types.

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

session.on(event -> {
    if (event instanceof AssistantMessageDeltaEvent delta) {
        // Print each chunk as it arrives
        System.out.print(delta.getData().getDeltaContent());
    } else if (event instanceof SessionIdleEvent) {
        System.out.println(); // Newline at end
        done.complete(null);
    }
});

session.send("Write a haiku about Java").get();
done.get();
```

---

## Choosing a Model

### List Available Models

Query available models before creating a session:

```java
List<ModelInfo> models = client.listModels().get();

for (ModelInfo model : models) {
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
String lastSessionId = client.getLastSessionId().get();

// Or list all sessions
List<SessionMetadata> sessions = client.listSessions().get();

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
- 📖 **[MCP Servers](mcp.html)** - Integrate external tools via Model Context Protocol
- 📖 **[API Javadoc](apidocs/index.html)** - Complete API reference