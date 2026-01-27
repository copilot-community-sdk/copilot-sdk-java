# Copilot SDK for Java - Documentation

> ⚠️ **Disclaimer:** This is an **unofficial, community-driven SDK** and is **not supported or endorsed by GitHub**. Use at your own risk.

This document provides detailed API reference and usage examples for the Copilot SDK for Java.

## Table of Contents

- [API Reference](#API_Reference)
  - [CopilotClient](#CopilotClient)
  - [CopilotSession](#CopilotSession)
- [Event Types](#Event_Types)
- [Streaming](#Streaming)
- [Listing Models](#Listing_Models)

📖 **[Advanced Usage →](advanced.html)** - Tools, BYOK, MCP Servers, Infinite Sessions, and more.

## API Reference

### CopilotClient

#### Constructor

```java
new CopilotClient()
new CopilotClient(CopilotClientOptions options)
```

**Options:**

- `cliPath` - Path to CLI executable (default: "copilot" from PATH)
- `cliArgs` - Extra arguments prepended before SDK-managed flags
- `cliUrl` - URL of existing CLI server to connect to (e.g., `"localhost:8080"`). When provided, the client will not spawn a CLI process.
- `port` - Server port (default: 0 for random)
- `useStdio` - Use stdio transport instead of TCP (default: true)
- `logLevel` - Log level (default: "info")
- `autoStart` - Auto-start server (default: true)
- `autoRestart` - Auto-restart on crash (default: true)
- `cwd` - Working directory for the CLI process
- `environment` - Environment variables to pass to the CLI process

#### Methods

##### `start(): CompletableFuture<Void>`

Start the CLI server and establish connection.

##### `stop(): CompletableFuture<Void>`

Stop the server and close all sessions.

##### `forceStop(): CompletableFuture<Void>`

Force stop the CLI server without graceful cleanup.

##### `createSession(SessionConfig config): CompletableFuture<CopilotSession>`

Create a new conversation session.

**Config:**

- `sessionId` - Custom session ID
- `model` - Model to use ("gpt-5", "claude-sonnet-4.5", etc.)
- `tools` - Custom tools exposed to the CLI
- `systemMessage` - System message customization
- `availableTools` - List of tool names to allow
- `excludedTools` - List of tool names to disable
- `provider` - Custom API provider configuration (BYOK)
- `streaming` - Enable streaming of response chunks (default: false)
- `mcpServers` - MCP server configurations
- `customAgents` - Custom agent configurations
- `onPermissionRequest` - Handler for permission requests

##### `resumeSession(String sessionId, ResumeSessionConfig config): CompletableFuture<CopilotSession>`

Resume an existing session.

##### `ping(String message): CompletableFuture<PingResponse>`

Ping the server to check connectivity.

##### `getStatus(): CompletableFuture<GetStatusResponse>`

Get CLI status including version and protocol information.

##### `getAuthStatus(): CompletableFuture<GetAuthStatusResponse>`

Get current authentication status.

##### `listModels(): CompletableFuture<List<ModelInfo>>`

List available models with their metadata (id, name, capabilities, billing info).

##### `getState(): ConnectionState`

Get current connection state. Returns one of: `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `ERROR`.

##### `listSessions(): CompletableFuture<List<SessionMetadata>>`

List all available sessions.

##### `deleteSession(String sessionId): CompletableFuture<Void>`

Delete a session and its data from disk.

##### `getLastSessionId(): CompletableFuture<String>`

Get the ID of the most recently used session.

---

### CopilotSession

Represents a single conversation session.

#### Properties

- `getSessionId()` - The unique identifier for this session

#### Methods

##### `send(String prompt): CompletableFuture<String>`

Convenience method to send a simple text message. Equivalent to `send(new MessageOptions().setPrompt(prompt))`.

##### `send(MessageOptions options): CompletableFuture<String>`

Send a message to the session.

**Options:**

- `prompt` - The message/prompt to send
- `attachments` - File attachments
- `mode` - Delivery mode ("enqueue" or "immediate")

Returns the message ID.

##### `sendAndWait(String prompt): CompletableFuture<AssistantMessageEvent>`

Convenience method to send a simple text message and wait for the session to become idle. Equivalent to `sendAndWait(new MessageOptions().setPrompt(prompt))`.

##### `sendAndWait(MessageOptions options): CompletableFuture<AssistantMessageEvent>`

Send a message and wait for the session to become idle. Default timeout is 60 seconds.

##### `sendAndWait(MessageOptions options, long timeoutMs): CompletableFuture<AssistantMessageEvent>`

Send a message and wait for the session to become idle with custom timeout.

##### `on(Consumer<AbstractSessionEvent> handler): Closeable`

Subscribe to session events. Returns a `Closeable` to unsubscribe.

```java
var subscription = session.on(evt -> {
    System.out.println("Event: " + evt.getType());
});

// Later...
subscription.close();
```

##### `abort(): CompletableFuture<Void>`

Abort the currently processing message in this session.

##### `getMessages(): CompletableFuture<List<AbstractSessionEvent>>`

Get all events/messages from this session.

##### `close()`

Dispose the session and free resources.

---

## Event Types

Sessions emit various events during processing. Each event type extends `AbstractSessionEvent`:

- `UserMessageEvent` - User message added
- `AssistantMessageEvent` - Assistant response
- `AssistantMessageDeltaEvent` - Streaming response chunk
- `ToolExecutionStartEvent` - Tool execution started
- `ToolExecutionCompleteEvent` - Tool execution completed
- `SessionStartEvent` - Session started
- `SessionIdleEvent` - Session is idle
- `SessionErrorEvent` - Session error occurred
- `SessionResumeEvent` - Session was resumed
- And more...

Use pattern matching (Java 21+) to handle specific event types:

```java
session.on(evt -> {
    if (evt instanceof AssistantMessageEvent msg) {
        System.out.println(msg.getData().getContent());
    } else if (evt instanceof SessionErrorEvent err) {
        System.out.println("Error: " + err.getData().getMessage());
    }
});
```

## Streaming

Enable streaming to receive assistant response chunks as they're generated:

```java
var session = client.createSession(
    new SessionConfig()
        .setModel("gpt-5")
        .setStreaming(true)
).get();

var done = new CompletableFuture<Void>();

session.on(evt -> {
    if (evt instanceof AssistantMessageDeltaEvent delta) {
        // Streaming message chunk - print incrementally
        System.out.print(delta.getData().getDeltaContent());
    } else if (evt instanceof AssistantMessageEvent msg) {
        // Final message - complete content
        System.out.println("\n--- Final message ---");
        System.out.println(msg.getData().getContent());
    } else if (evt instanceof SessionIdleEvent) {
        done.complete(null);
    }
});

session.send(new MessageOptions().setPrompt("Tell me a short story")).get();
done.get();
```

## Listing Models

Query available models and their capabilities before creating a session:

```java
try (var client = new CopilotClient()) {
    client.start().get();

    // List all available models
    List<ModelInfo> models = client.listModels().get();
    
    for (ModelInfo model : models) {
        System.out.println("Model: " + model.getId());
        System.out.println("  Name: " + model.getName());
        
        if (model.getCapabilities() != null) {
            System.out.println("  Max Output Tokens: " + model.getCapabilities().getMaxOutputTokens());
        }
        
        if (model.getPolicy() != null) {
            System.out.println("  State: " + model.getPolicy().getState());
        }
    }
    
    // Use a specific model from the list
    var session = client.createSession(
        new SessionConfig().setModel(models.get(0).getId())
    ).get();
}
```

Each `ModelInfo` contains:

- `id` - Model identifier (e.g., "claude-sonnet-4.5", "gpt-4o")
- `name` - Human-readable display name
- `capabilities` - Model limits including max output tokens
- `policy` - Policy state information
- `billing` - Billing/usage information

---

## Next Steps

📖 **[Advanced Usage →](advanced.html)** - Learn about Tools, BYOK, MCP Servers, Infinite Sessions, Permission Handling, and more.

